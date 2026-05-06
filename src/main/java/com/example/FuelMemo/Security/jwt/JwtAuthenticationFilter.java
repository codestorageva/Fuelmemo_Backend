package com.example.FuelMemo.Security.jwt;

import com.example.FuelMemo.AuthModule.Entity.Role;
import com.example.FuelMemo.AuthModule.Entity.User;
import com.example.FuelMemo.AuthModule.Repository.UserRepository;
import com.example.FuelMemo.Shared.Exception.LoggedOutException;
import com.example.FuelMemo.Shared.Exception.UserUnauthorizedException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserRepository userRepository,
                                   JwtAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        //  No token → continue
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);

            String username = jwtService.extractUsername(token);
            Integer companyId = jwtService.extractCompanyId(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByEmailIgnoreCase(username);

                if (user == null) {
                    throw new BadCredentialsException("User not found");
                }

                //  TOKEN VALIDATION
                if (!jwtService.isTokenValid(token, user.getEmail())) {
                    throw new BadCredentialsException("Invalid token");
                }

                //  MULTIPLE LOGIN CHECK
                String existingToken = user.getJwtToken();

                if (existingToken != null && !existingToken.equals(token)) {
                    // Old device automatically logout
                    logger.warn("User logged in from another device. Invalidating old token.");

                    // overwrite with new token (latest login wins)
                    user.setJwtToken(token);
                    userRepository.save(user);
                }


                //  LOGOUT CHECK
                if (Boolean.TRUE.equals(user.getIsLoggedOut())) {
                    throw new LoggedOutException(
                            "Due to multiple login you've been logged out automatically by system. Generate a new token and login again."
                    );
                }

                //  TOKEN MISMATCH CHECK
                if (user.getJwtToken() == null || !user.getJwtToken().equals(token)) {
                    throw new UserUnauthorizedException("Session expired. Please login again.");
                }

                Set<GrantedAuthority> authorities = new HashSet<>();

//  SUPER ADMIN BYPASS
                if (user.isSuperAdmin()) {

                    authorities.add(new SimpleGrantedAuthority("ROLE_SUPERADMIN"));

                } else {

                    user.getUserCompanies().stream()
                            .filter(uc ->
                                    uc.getCompany() != null &&
                                            uc.getCompany().getCompanyId().equals(companyId)
                            )
                            .findFirst()
                            .ifPresent(uc -> {

                                Role role = uc.getRole();

                                if (role != null && Boolean.TRUE.equals(role.getActive())) {

                                    // Role
                                    authorities.add(new SimpleGrantedAuthority(role.getRoleName()));

                                    // Permissions
                                    if (role.getPermissions() != null) {
                                        role.getPermissions().forEach(permission ->
                                                authorities.add(new SimpleGrantedAuthority(permission.getPermissionName()))
                                        );
                                    }
                                }
                            });
                }

                // ================= AUTH =================
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                //  store companyId separately
                request.setAttribute("companyId", companyId);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("Authenticated user: {} | companyId: {} | authorities: {}",
                        username, companyId, authorities);
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {

            logger.warn("JWT Expired: {}", ex.getMessage());
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("Token expired. Please login again.")
            );

        } catch (LoggedOutException ex) {

            logger.warn("Logged out: {}", ex.getMessage());
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException(ex.getMessage())
            );

        } catch (UserUnauthorizedException ex) {

            logger.warn("Unauthorized: {}", ex.getMessage());
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException(ex.getMessage())
            );

        } catch (SecurityException ex) {

            logger.error("Invalid Signature: {}", ex.getMessage());
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("Invalid token signature.")
            );

        } catch (MalformedJwtException ex) {

            logger.error("Malformed Token: {}", ex.getMessage());
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("Invalid token format.")
            );

        } catch (Exception ex) {

            logger.error("Authentication Error: {}", ex.getMessage());
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException(ex.getMessage())
            );
        }
    }
}