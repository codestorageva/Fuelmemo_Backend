package com.example.FuelMemo.Security;

import java.util.List;

public class UserPrincipal {

    private Integer id;
    private String username;
    private List<String> roles;      // e.g. ROLE_SUPERADMIN, ROLE_ADMIN
    private List<Integer> companyIds; // IDs of companies the user can access

    public UserPrincipal(Integer id, String username, List<String> roles, List<Integer> companyIds) {
        this.id = id;
        this.username = username;
        this.roles = roles;
        this.companyIds = companyIds;
    }

    // Getters
    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public List<String> getRoles() { return roles; }
    public List<Integer> getCompanyIds() { return companyIds; }

    // Optionally, setters if needed
}
