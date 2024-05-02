package org.task.clearsolutions.service;

import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.task.clearsolutions.dto.UserRequestDto;
import org.task.clearsolutions.dto.UserResponseDto;

@Service
public interface UserService {
    /***
     * Register a new user who are more than 18 years old.
     * @param userRequestDto The UserRegistrationRequestDto object representing the new user.
     * @return The created UserDto object.
     */
    UserResponseDto register(UserRequestDto userRequestDto);

    /***
     * Update all user fields
     * @param id The id of the user to be updated.
     * @param userRequestDto The UserRequestDto object representing the updated user.
     * @return The updated UserResponseDto object.
     */
    UserResponseDto updateAllFields(Long id, UserRequestDto userRequestDto);

    /***
     * Update one or some user`s field
     * @param id The id of the user to be updated
     * @param fields The fields of the user to be updated
     * @return The updated UserResponseDto object.
     */
    public UserResponseDto updatePartially(Long id, Map<String, Object> fields);

    /***
     * Deletes a user by its id
     * @param id The unique identifier of the user to be deleted
     */
    void deleteById(Long id);

    /***
     * Search for users by birthdate range.
     * @param from Range date start.
     * @param to Range date end.
     * @return A page of UserResponseDto objects.
     */
    Page<UserResponseDto> searchUsersByBirthdateBetween(@Past LocalDate from, @Past LocalDate to, Pageable pageable);
}
