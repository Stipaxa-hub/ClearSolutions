package org.task.clearsolutions.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.task.clearsolutions.dto.UserRequestDto;
import org.task.clearsolutions.dto.UserResponseDto;
import org.task.clearsolutions.entity.User;
import org.task.clearsolutions.exception.RegistrationException;
import org.task.clearsolutions.exception.UpdateException;
import org.task.clearsolutions.mapper.UserMapper;
import org.task.clearsolutions.repository.UserRepository;
import org.task.clearsolutions.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String MESSAGE_FOR_ENTITY_NOT_FOUND_EXCEPTION = "User with id: %d not found";
    private static final String MESSAGE_FOR_REGISTRATION_EXCEPTION_PHONE_EMAIL = "Can't register user. User with same email or phone number was registered";
    private static final String MESSAGE_FOR_REGISTRATION_EXCEPTION_YOUNGER_THAN_ALLOWED = "Can't register user. User must be older than %d";
    private static final String MESSAGE_FOR_UPDATE_EXCEPTION_YOUNGER_THAN_ALLOWED = "Can't update user. User must be older than %d";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    @Value("${user.min_age}")
    private Integer MIN_AGE_ALLOWED_TO_REGISTER;

    @Transactional
    @Override
    public UserResponseDto register(UserRequestDto userRequestDto) {
        if (userRepository.findByEmail(userRequestDto.email()).isPresent() || userRepository.findByPhoneNumber(userRequestDto.phoneNumber()).isPresent()) {
            throw new RegistrationException(MESSAGE_FOR_REGISTRATION_EXCEPTION_PHONE_EMAIL);
        }

        if (calculateAge(userRequestDto.birthDate()) > MIN_AGE_ALLOWED_TO_REGISTER) {
            User user = userMapper.toEntity(userRequestDto);
            User savedUser = userRepository.save(user);
            return userMapper.toDto(savedUser);
        } else {
            throw new RegistrationException(String.format(MESSAGE_FOR_REGISTRATION_EXCEPTION_YOUNGER_THAN_ALLOWED, MIN_AGE_ALLOWED_TO_REGISTER));
        }
    }

    @Transactional
    @Override
    public UserResponseDto updateAllFields(Long id, UserRequestDto userRequestDto) {
       userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(MESSAGE_FOR_ENTITY_NOT_FOUND_EXCEPTION, id)));
        if (calculateAge(userRequestDto.birthDate()) > MIN_AGE_ALLOWED_TO_REGISTER) {
            User user = userMapper.toEntity(userRequestDto);
            user.setId(id);
            User updatedUser = userRepository.save(user);
            return userMapper.toDto(updatedUser);
        } else {
            throw new UpdateException(String.format(MESSAGE_FOR_UPDATE_EXCEPTION_YOUNGER_THAN_ALLOWED, MIN_AGE_ALLOWED_TO_REGISTER));
        }
    }

    public UserResponseDto updatePartially(Long id, UserRequestDto userRequestDto) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(MESSAGE_FOR_ENTITY_NOT_FOUND_EXCEPTION, id)
                ));
        updateFieldsForPatch(userMapper.toEntity(userRequestDto), userToUpdate);
        User updatedUser = userRepository.save(userToUpdate);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(
                        MESSAGE_FOR_ENTITY_NOT_FOUND_EXCEPTION, id
                )));
        userRepository.deleteById(id);
    }

    @Override
    public Page<UserResponseDto> searchUsersByBirthdateBetween(@Past LocalDate from, @Past LocalDate to, Pageable pageable) {
        if (from.isBefore(to)) {
            List<UserResponseDto> userResponseDtos = userRepository.findAllByBirthDateBetween(from, to)
                    .stream()
                    .map(userMapper::toDto)
                    .toList();
            return new PageImpl<>(userResponseDtos, pageable, userResponseDtos.size());
        }
        throw new EntityNotFoundException("Can't find users in this period");
    }

    private Integer calculateAge(LocalDate birthDate) {
        Period period = Period.between(birthDate, LocalDate.now());
        return period.getYears();
    }

    private void updateFieldsForPatch(User source, User target) {
        String updatedEmail = source.getEmail();
        if (isNotEmpty(updatedEmail)) target.setEmail(updatedEmail);

        String updatedFirstName = source.getFirstName();
        if (isNotEmpty(updatedFirstName)) target.setFirstName(updatedFirstName);

        String updatedLastName = source.getLastName();
        if (isNotEmpty(updatedLastName)) target.setLastName(updatedLastName);

        LocalDate updatedBirthDate = source.getBirthDate();
        if (isNotEmpty(updatedBirthDate)) target.setBirthDate(updatedBirthDate);

        String updatedAddress = source.getAddress();
        if (isNotEmpty(updatedAddress)) target.setAddress(updatedAddress);

        String updatedPhoneNumber = source.getPhoneNumber();
        if (isNotEmpty(updatedPhoneNumber)) target.setPhoneNumber(updatedPhoneNumber);
    }

    private <T> boolean isNotEmpty(T field) {
        return field != null && !field.equals("");
    }
}
