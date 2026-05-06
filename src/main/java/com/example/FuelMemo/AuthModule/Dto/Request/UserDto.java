package com.example.FuelMemo.AuthModule.Dto.Request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Integer userId;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "Password must contain uppercase, lowercase, number and special character"
    )
    private String password;

    @NotBlank(message="First Name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message="Last Name is Required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Mobile number must be 10 digits"
    )
    private String mobNo;

    @NotBlank(message="Address is required")
    @Size(max = 150, message = "Address Line 1 cannot exceed 150 characters")
    private String addressLine1;

    @Size(max = 150, message = "Address Line 2 cannot exceed 150 characters")
    private String addressLine2;

    @Size(max = 150, message = "Address Line 3 cannot exceed 150 characters")
    private String addressLine3;

    @NotEmpty(message = "At least one company must be assigned")
    private List<@NotNull(message = "Company ID cannot be null") Integer> companyIds;

    @NotEmpty(message = "At least one role must be assigned")
    private List<@NotNull(message = "Role ID cannot be null") Integer> roleIds;
}