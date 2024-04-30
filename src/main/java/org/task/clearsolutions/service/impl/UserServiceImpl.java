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
import org.task.clearsolutions.mapper.UserMapper;
import org.task.clearsolutions.repository.UserRepository;
import org.task.clearsolutions.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String MESSAGE_FOR_ENTITY_NOT_FOUND_EXCEPTION = "User with id: %d not found";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    @Value("${user.min_age}")
    private Integer MIN_AGE_ALLOWED_TO_REGISTER;

    @Transactional
    @Override
    public UserResponseDto register(UserRequestDto userRequestDto) {
        if (userRepository.findByEmail(userRequestDto.email()).isPresent() || userRepository.findByPhoneNumber(userRequestDto.phoneNumber()).isPresent()) {
            throw new RegistrationException("Can't register user. User with same email or phone number was registered");
        }

        if (calculateAge(userRequestDto.birthDate()) > MIN_AGE_ALLOWED_TO_REGISTER) {
            User user = userMapper.toEntity(userRequestDto);
            User savedUser = userRepository.save(user);
            return userMapper.toDto(savedUser);
        } else {
            throw new RegistrationException("Can't register user. User must be older than 18");
        }
    }

    @Transactional
    @Override
    public UserResponseDto updateAllFields(Long id, UserRequestDto userRequestDto) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setEmail(userRequestDto.email());
                    existingUser.setFirstName(userRequestDto.firstName());
                    existingUser.setLastName(userRequestDto.lastName());
                    existingUser.setBirthDate(userRequestDto.birthDate());
                    existingUser.setAddress(userRequestDto.address());
                    existingUser.setPhoneNumber(userRequestDto.phoneNumber());
                    return userRepository.save(existingUser);
                })
                .map(userMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format(MESSAGE_FOR_ENTITY_NOT_FOUND_EXCEPTION, id)));
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
}
