package org.task.clearsolutions.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.task.clearsolutions.dto.UserRequestDto;
import org.task.clearsolutions.dto.UserResponseDto;
import org.task.clearsolutions.entity.User;
import org.task.clearsolutions.exception.RegistrationException;
import org.task.clearsolutions.exception.UpdateException;
import org.task.clearsolutions.mapper.UserMapper;
import org.task.clearsolutions.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "email@email.com";
    private static final String USER_FIRST_NAME = "user";
    private static final String USER_LAST_NAME = "lastUser";
    private static final LocalDate USER_BIRTH_DATE = LocalDate.of(2000, Month.DECEMBER, 1);
    private static final String USER_ADDRESS = "Lviv, Market Square";
    private static final String USER_PHONE_NUMBER = "+380501235078";
    private static User user;
    private static UserRequestDto userRequestDto;
    private static UserResponseDto userResponseDto;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void beforeEach() throws NoSuchFieldException, IllegalAccessException {
        user = User.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .firstName(USER_FIRST_NAME)
                .lastName(USER_LAST_NAME)
                .birthDate(USER_BIRTH_DATE)
                .address(USER_ADDRESS)
                .phoneNumber(USER_PHONE_NUMBER)
                .build();

        userRequestDto = new UserRequestDto(
                USER_EMAIL,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_BIRTH_DATE,
                USER_ADDRESS,
                USER_PHONE_NUMBER
        );

        userResponseDto = new UserResponseDto(
                USER_ID,
                USER_EMAIL,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                USER_BIRTH_DATE,
                USER_ADDRESS,
                USER_PHONE_NUMBER
        );

        Field field = UserServiceImpl.class.getDeclaredField("MIN_AGE_ALLOWED_TO_REGISTER");
        field.setAccessible(true);
        field.set(userService, 18);
    }

    @Test
    @DisplayName("Testing to correct registration new user")
    void register_ValidParam_ShouldRegisterNewUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());
        when(userMapper.toEntity(any(UserRequestDto.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

        UserResponseDto actualUserDto = userService.register(userRequestDto);

        assertNotNull(actualUserDto);
        assertEquals(userResponseDto, actualUserDto);
    }

    @Test
    @DisplayName("Testing to register with email which was registered")
    void register_UserWithSameEmailWasRegistered_ShouldThrowRegistrationException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, (() -> userService.register(userRequestDto)));
    }

    @Test
    @DisplayName("Testing to register user with phone number which was registered")
    void register_UserWithTheSamePhoneNumberWasRegistered_ShouldThrowRegistrationException() {
        when(userRepository.findByPhoneNumber(anyString())).thenReturn(Optional.of(user));

        assertThrows(RegistrationException.class, (() -> userService.register(userRequestDto)));
    }

    @Test
    @DisplayName("Testing to register user which younger than allowed")
    void register_UserYoungerThanAllowed_ShouldThrowRegistrationException() {
        userRequestDto = new UserRequestDto(
                USER_EMAIL,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                LocalDate.now(),
                USER_ADDRESS,
                USER_PHONE_NUMBER
        );

        assertThrows(RegistrationException.class, () -> userService.register(userRequestDto));
    }

    @Test
    @DisplayName("Testing to correct update all fields")
    public void updateAllFields_ValidParam_ShouldUpdateAllField() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userMapper.toEntity(any(UserRequestDto.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);

        UserResponseDto response = userService.updateAllFields(1L, userRequestDto);

        assertEquals(userResponseDto, response);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Testing to update all fields in user. Should throw exception(EntityNotFoundException) when non existing user")
    public void updateAllFields_NonExistingUser_ShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updateAllFields(1L, userRequestDto));
    }

    @Test
    @DisplayName("Testing to update all fields in user. Should throw exception(UpdateException) when user's age less than allowed")
    public void updateAllFields_AgeLessThanAllowed_ShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        userRequestDto = new UserRequestDto(
                USER_EMAIL,
                USER_FIRST_NAME,
                USER_LAST_NAME,
                LocalDate.now().minusYears(2),
                USER_ADDRESS,
                USER_PHONE_NUMBER
        );

        assertThrows(UpdateException.class, () -> userService.updateAllFields(1L, userRequestDto));
    }

    @Test
    @DisplayName("Testing to correct update partially fields")
    public void updatePartially_ValidParam_ShouldUpdateSomeField() {
        User userToUpdate = new User();
        userToUpdate.setId(1L);
        userToUpdate.setEmail("updated@example.com");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userToUpdate));
        when(userMapper.toEntity(any(UserRequestDto.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userResponseDto);
        when(userRepository.save(any(User.class))).thenReturn(userToUpdate);

        UserRequestDto updatedUserRequestDto = new UserRequestDto("updated@example.com", "", "", null, "null", "");
        UserResponseDto response = userService.updatePartially(1L, updatedUserRequestDto);

        assertEquals(userResponseDto, response);
        verify(userRepository).save(userToUpdate);
    }
    @Test
    @DisplayName("Testing to update partially fields in user. Should throw exception(EntityNotFoundException) when non existing user")
    public void updatePartially_NonExistingUser_ShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updatePartially(1L, userRequestDto));
    }

    @Test
    @DisplayName("Testing to correct delete user")
    public void deleteById_ValidParam_ShouldDeleteUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        userService.deleteById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Testing to delete user. Should throw exception(EntityNotFoundException) when non existing user")
    public void deleteById_NonExistingUser_ShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.deleteById(1L));
    }


    @Test
    @DisplayName("Testing to correct search users in period")
    public void searchUsersByBirthdateBetween_ValidParam_ShouldReturnUsers() {
        LocalDate from = LocalDate.now().minusYears(30);
        LocalDate to = LocalDate.now().minusYears(20);
        Pageable pageable = PageRequest.of(0, 10);

        List<User> users = Arrays.asList(user);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAllByBirthDateBetween(from, to)).thenReturn(userPage.stream().toList());

        Page<UserResponseDto> response = userService.searchUsersByBirthdateBetween(from, to, pageable);

        assertEquals(1, response.getTotalElements());
        verify(userRepository).findAllByBirthDateBetween(from, to);
    }

    @Test
    @DisplayName("Testing to search users. Should throw exception(EntityNotFoundException) when non existing users in period")
    public void searchUsersByBirthdateBetween_InvalidParam_ShouldThrowException() {
        LocalDate from = LocalDate.now().minusYears(20);
        LocalDate to = LocalDate.now().minusYears(30);
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(EntityNotFoundException.class, () -> userService.searchUsersByBirthdateBetween(from, to, pageable));
    }
}