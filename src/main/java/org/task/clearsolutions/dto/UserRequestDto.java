package org.task.clearsolutions.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import org.springframework.validation.annotation.Validated;
import org.task.clearsolutions.validator.Phone;
@Validated
public record UserRequestDto(
        @NotBlank(message = "Email can't be empty")
        @Email
        String email,
        @NotBlank(message = "First name can't be can't be empty")
        String firstName,
        @NotBlank(message = "Last name can't be can't be empty")
        String lastName,
        @NotNull(message = "Birth date can't be null")
        @Past(message = "Birth date must be earlier than current date")
        LocalDate birthDate,
        @NotBlank(message = "Shipping address can't be empty")
        String address,
        @Phone(message = "Please write correct phone number format")
        String phoneNumber
) {
}
