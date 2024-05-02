package org.task.clearsolutions.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.task.clearsolutions.dto.UserRequestDto;
import org.task.clearsolutions.dto.UserResponseDto;
import org.task.clearsolutions.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public UserResponseDto register(@RequestBody @Valid UserRequestDto userRequestDto) {
        return userService.register(userRequestDto);
    }

    @PutMapping("/{id}")
    public UserResponseDto update(@PathVariable Long id, @RequestBody @Valid UserRequestDto userRequestDto) {
        return userService.updateAllFields(id, userRequestDto);
    }

    @PatchMapping("/{id}")
    public UserResponseDto updatePartially(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        return userService.updatePartially(id, fields);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.deleteById(id);
    }

    @GetMapping
    public Page<UserResponseDto> getUsersByBirthdateBetween(@Past @RequestParam LocalDate from, @Past @RequestParam LocalDate to, Pageable pageable) {
        return userService.searchUsersByBirthdateBetween(from, to, pageable);
    }
}
