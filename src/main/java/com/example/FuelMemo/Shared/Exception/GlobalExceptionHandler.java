package com.example.FuelMemo.Shared.Exception;

import com.example.FuelMemo.Shared.Response.MessageResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import com.example.FuelMemo.Security.jwt.JwtService;

import java.io.IOException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ================= VALIDATION =================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handleValidationException(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid Request Data");

        return new MessageResponse(false, HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handleConstraintViolation(ConstraintViolationException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, "Request body is missing or malformed.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, "Invalid parameter type.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public MessageResponse handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return new MessageResponse(false, HttpStatus.METHOD_NOT_ALLOWED, "HTTP Method Not Allowed.");
    }

    // ================= BUSINESS EXCEPTIONS =================

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public MessageResponse handleNotFound(ResourceNotFoundException ex) {
        return new MessageResponse(false, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public MessageResponse handleAlreadyExist(ResourceAlreadyExistException ex) {
        return new MessageResponse(false, HttpStatus.CONFLICT, ex.getMessage());
    }

//    @ExceptionHandler(DuplicateEntryException.class)
//    @ResponseStatus(HttpStatus.CONFLICT)
//    public MessageResponse handleDuplicate(DuplicateEntryException ex) {
//        return new MessageResponse(false, HttpStatus.CONFLICT, ex.getMessage());
//    }

    @ExceptionHandler(InactiveStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handleInactive(InactiveStatusException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AlreadyDeletedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handleAlreadyDeleted(AlreadyDeletedException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResponse> handleRuntimeException(RuntimeException ex) {
        MessageResponse response = MessageResponse.builder()
                .success(false)
                .successCode(HttpStatus.BAD_REQUEST)
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<MessageResponse> handleIllegalState(IllegalStateException ex) {
        MessageResponse response = MessageResponse.builder()
                .success(false)
                .successCode(HttpStatus.BAD_REQUEST)
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<MessageResponse> handleDuplicate(DuplicateEntryException ex) {
        MessageResponse response = MessageResponse.builder()
                .success(false)
                .successCode(HttpStatus.CONFLICT)
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // fallback for other exceptions
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<MessageResponse> handleAll(Exception ex) {
//        MessageResponse response = MessageResponse.builder()
//                .success(false)
//                .successCode(HttpStatus.INTERNAL_SERVER_ERROR)
//                .message("Something went wrong: " + ex.getMessage())
//                .build();
//        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @ExceptionHandler(InvalidArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponse handleInvalidArgument(InvalidArgumentException ex) {
        return new MessageResponse(false, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ================= SECURITY =================

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public MessageResponse handleBadCredentials(BadCredentialsException ex) {
        return new MessageResponse(false, HttpStatus.UNAUTHORIZED, "Invalid username or password.");
    }

    @ExceptionHandler(TokenUnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public MessageResponse handleTokenUnauthorized(TokenUnauthorizedException ex) {
        return new MessageResponse(false, HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public MessageResponse handleAccessDenied(AccessDeniedException ex) {
        return new MessageResponse(false, HttpStatus.FORBIDDEN, "You do not have permission to access this resource.");
    }

    @ExceptionHandler({ExpiredJwtException.class, SignatureException.class, DecodingException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public MessageResponse handleJwtExceptions(Exception ex) {
        return new MessageResponse(false, HttpStatus.UNAUTHORIZED, "Invalid or Expired Token.");
    }

    @ExceptionHandler(LoggedOutException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public MessageResponse handleLoggedOut(LoggedOutException ex) {
        return new MessageResponse(false, HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // ================= FILE =================

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public MessageResponse handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return new MessageResponse(false, HttpStatus.PAYLOAD_TOO_LARGE, "Maximum upload size is 50MB.");
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageResponse handleIOException(IOException ex) {
        return new MessageResponse(false, HttpStatus.INTERNAL_SERVER_ERROR, "File processing error.");
    }

    // ================= FALLBACK =================
//
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public MessageResponse handleGlobalException(Exception ex) {
//        return new MessageResponse(false, HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please contact admin.");
//    }

    @ExceptionHandler(JwtService.TokenExpiredException.class)
    public ResponseEntity<?> handleTokenExpired(JwtService.TokenExpiredException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOtherExceptions(Exception ex) {
        return ResponseEntity
                .status(500)
                .body(Map.of(
                        "success", false,
                        "message", "Internal Server Error"
                ));
    }
}
