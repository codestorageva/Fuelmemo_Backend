package com.example.FuelMemo.Shared.Exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUnauthorizedException extends RuntimeException {

    private String fullName;
    private String vid;
    private String mobNo;
    private String email;
    private String userName;
    private Boolean isDocProvided;
    private String approvalStatus;
    private String rejectionReason;

    public UserUnauthorizedException(String msg) {
        super(msg);
    }

    // Constructor that accepts all details and the message
    public UserUnauthorizedException(String fullName, String vid, String mobNo, String email, String userName, Boolean isDocProvided, String approvalStatus, String msg) {
        super(msg);  // Initialize the parent RuntimeException with the message
        this.fullName = fullName;
        this.vid = vid;
        this.mobNo = mobNo;
        this.email = email;
        this.userName = userName;
        this.isDocProvided = isDocProvided;
        this.approvalStatus = approvalStatus;
    }

    public UserUnauthorizedException(String fullName, String vid, String mobNo, String email, String userName, String approvalStatus, String rejectionReason, String msg) {
        super(msg);  // Initialize the parent RuntimeException with the message
        this.fullName = fullName;
        this.vid = vid;
        this.mobNo = mobNo;
        this.email = email;
        this.userName = userName;
        this.approvalStatus = approvalStatus;
        this.rejectionReason = rejectionReason;
    }
}
