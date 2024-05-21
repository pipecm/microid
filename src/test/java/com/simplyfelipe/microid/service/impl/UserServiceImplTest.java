package com.simplyfelipe.microid.service.impl;

import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.entity.Role;
import com.simplyfelipe.microid.entity.RoleName;
import com.simplyfelipe.microid.entity.User;
import com.simplyfelipe.microid.exception.ServiceException;
import com.simplyfelipe.microid.filter.FindUsersFilters;
import com.simplyfelipe.microid.mapper.UserMapper;
import com.simplyfelipe.microid.repository.UserRepository;
import com.simplyfelipe.microid.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private enum UserType { BASIC, ADMIN, BEFORE_SAVING, AFTER_SAVING };

    private static final UUID USER_ID = UUID.fromString("d568123c-0520-460f-84d9-1b127569978a");
    private static final String EMAIL = "user@mail.com";
    private static final String DECODED_PASSWORD = "12345";
    private static final String ENCODED_PASSWORD = "$2a$10$VtLnU/ZYARTQOThiocEuue3w6xLMi0Z/PO3KvSBwwDEWuaHN2tfq2";
    private static final String NO_EXCEPTION_THROWN = "No exception thrown";
    private static final String USER_ALREADY_EXISTS_MSG = "User %s already exists";
    private static final String USER_DOES_NOT_EXIST_MSG = "User %s does not exist";

    private static final List<Role> BASIC_ROLES = List.of(new Role(RoleName.USER));
    private static final List<Role> ADMIN_ROLES = List.of(new Role(RoleName.USER), new Role(RoleName.ADMIN));

    private static final Specification<User> ALL_USERS_SPECIFICATION =
            (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.isTrue(root.isNotNull());

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;


    @Test
    void whenSearchWithoutFiltersThenReturnAllUsers() {
        User found = usersMap().get(UserType.AFTER_SAVING);
        UserDto foundDto = UserDto.builder().email(EMAIL).roles(List.of(RoleName.USER)).active(true).build();

        when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any())).thenReturn(List.of(found));
        when(userMapper.map(found)).thenReturn(foundDto);

        List<UserDto> userDtoList = userService.findUsers(FindUsersFilters.builder().roleName(RoleName.UNDEFINED).build());

        assertThat(userDtoList)
                .isNotNull()
                .hasSize(1)
                .contains(foundDto);
    }

    @Test
    void whenLoadUserByUsernameThenReturnUserSuccessfully() {
        User userFromRepository = usersMap().get(UserType.ADMIN);

        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(userFromRepository));

        UserDetails userFromService = userService.loadUserByUsername(EMAIL);

        verify(userRepository).findByEmailIgnoreCase(EMAIL);

        assertThat(userFromService).isNotNull();
        assertThat(userFromService.getUsername()).isEqualTo(EMAIL);
        assertThat(userFromService.getPassword()).isEqualTo(ENCODED_PASSWORD);
    }

    @Test
    void whenLoadUserByUsernameThenExceptionIsThrown() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        try {
            userService.loadUserByUsername(EMAIL);
            fail(NO_EXCEPTION_THROWN);
        } catch (UsernameNotFoundException exception) {
            assertThat(exception).hasMessage(String.format(USER_DOES_NOT_EXIST_MSG, EMAIL));
        }

        verify(userRepository).findByEmailIgnoreCase(EMAIL);
    }

    @Test
    void whenCreatingNonExistingUserThenUserCreatedSuccessfully() {
        UserDto request = UserDto.builder().email(EMAIL).password(DECODED_PASSWORD).roles(List.of(RoleName.USER)).active(true).build();
        UserDto response = UserDto.builder().id(USER_ID).email(EMAIL).roles(List.of(RoleName.USER)).active(true).build();
        User beforeSaving = usersMap().get(UserType.BEFORE_SAVING);
        User afterSaving = usersMap().get(UserType.AFTER_SAVING);

        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(beforeSaving)).thenReturn(afterSaving);
        when(roleService.processRoles(BASIC_ROLES)).thenReturn(BASIC_ROLES);
        when(userMapper.map(request)).thenReturn(beforeSaving);
        when(userMapper.map(afterSaving)).thenReturn(response);

        UserDto created = userService.createUser(request);

        verify(userRepository).findByEmailIgnoreCase(EMAIL);
        verify(userRepository).save(beforeSaving);
        verify(roleService).processRoles(BASIC_ROLES);
        verify(userMapper).map(request);
        verify(userMapper).map(afterSaving);

        assertThat(created).isEqualTo(response);
    }

    @Test
    void whenCreatingAlreadyExistingUserThenExceptionIsThrown() {
        UserDto request = UserDto.builder().email(EMAIL).password(DECODED_PASSWORD).active(true).build();
        User found = usersMap().get(UserType.AFTER_SAVING);

        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(found));

        try {
            userService.createUser(request);
            fail(NO_EXCEPTION_THROWN);
        } catch (ServiceException exception) {
            assertThat(exception).hasMessage(String.format(USER_ALREADY_EXISTS_MSG, EMAIL));
            assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        }

        verify(userRepository).findByEmailIgnoreCase(EMAIL);
    }

    @Test
    void whenUpdatingExistingUserThenUserUpdatedSuccessfully() {
        UserDto request = UserDto.builder().id(USER_ID).email(EMAIL).password(DECODED_PASSWORD).roles(List.of(RoleName.USER, RoleName.ADMIN)).active(true).build();
        UserDto userDtoFound = UserDto.builder().id(USER_ID).email(EMAIL).active(true).roles(List.of(RoleName.USER)).build();
        UserDto response = UserDto.builder().id(USER_ID).email(EMAIL).active(true).roles(List.of(RoleName.USER, RoleName.ADMIN)).build();

        User userFound = usersMap().get(UserType.BEFORE_SAVING);
        User beforeSaving = usersMap().get(UserType.BEFORE_SAVING);
        User afterSaving = usersMap().get(UserType.AFTER_SAVING);

        beforeSaving.setRoles(ADMIN_ROLES);
        afterSaving.setRoles(ADMIN_ROLES);

        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(userFound));
        when(userRepository.save(beforeSaving)).thenReturn(afterSaving);
        when(roleService.processRoles(ADMIN_ROLES)).thenReturn(ADMIN_ROLES);
        when(userMapper.map(userFound)).thenReturn(userDtoFound);
        when(userMapper.map(afterSaving)).thenReturn(response);

        UserDto updated = userService.updateUser(request);

        verify(userRepository).findByEmailIgnoreCase(EMAIL);
        verify(userRepository).save(beforeSaving);
        verify(roleService).processRoles(ADMIN_ROLES);
        verify(userMapper).map(userFound);
        verify(userMapper).map(afterSaving);

        assertThat(updated).isEqualTo(response);
    }

    @Test
    void whenUpdatingNonExistingUserThenExceptionIsThrown() {
        UserDto request = UserDto.builder().email(EMAIL).password(DECODED_PASSWORD).roles(List.of(RoleName.USER, RoleName.ADMIN)).active(true).build();

        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        try {
            userService.updateUser(request);
            fail(NO_EXCEPTION_THROWN);
        } catch (UsernameNotFoundException exception) {
            assertThat(exception).hasMessage(String.format(USER_DOES_NOT_EXIST_MSG, EMAIL));
        }

        verify(userRepository).findByEmailIgnoreCase(EMAIL);
    }

    @Test
    void whenDeactivatingExistingUserThenUserDeactivatedSuccessfully() {
        UserDto response = UserDto.builder().id(USER_ID).email(EMAIL).active(false).roles(List.of(RoleName.USER)).build();

        User userFound = usersMap().get(UserType.BEFORE_SAVING);
        User beforeSaving = usersMap().get(UserType.BEFORE_SAVING);
        User afterSaving = usersMap().get(UserType.AFTER_SAVING);

        afterSaving.setActive(false);

        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(userFound));
        when(userRepository.save(beforeSaving)).thenReturn(afterSaving);
        when(userMapper.map(afterSaving)).thenReturn(response);

        UserDto deactivatedUser = userService.deactivateUser(EMAIL);

        verify(userRepository).findByEmailIgnoreCase(EMAIL);
        verify(userRepository).save(beforeSaving);
        verify(userMapper).map(afterSaving);

        assertThat(deactivatedUser).isEqualTo(response);
    }

    @Test
    void whenDeactivatingNonExistingUserThenExceptionIsThrown() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        try {
            userService.deactivateUser(EMAIL);
            fail(NO_EXCEPTION_THROWN);
        } catch (UsernameNotFoundException exception) {
            assertThat(exception).hasMessage(String.format(USER_DOES_NOT_EXIST_MSG, EMAIL));
        }

        verify(userRepository).findByEmailIgnoreCase(EMAIL);
    }

    private Map<UserType, User> usersMap() {
        LocalDateTime localDateTime = LocalDateTime.now();

        User beforeSaving = new User(EMAIL, ENCODED_PASSWORD, BASIC_ROLES);
        beforeSaving.setActive(true);
        beforeSaving.setCreatedOn(localDateTime);
        beforeSaving.setLastUpdatedOn(localDateTime);

        User afterSaving = new User(EMAIL, ENCODED_PASSWORD, BASIC_ROLES);
        afterSaving.setActive(true);
        afterSaving.setCreatedOn(localDateTime);
        afterSaving.setLastUpdatedOn(localDateTime);
        afterSaving.setId(USER_ID);

        return Map.of(
                UserType.BASIC, new User(EMAIL, ENCODED_PASSWORD, BASIC_ROLES),
                UserType.ADMIN, new User(EMAIL, ENCODED_PASSWORD, ADMIN_ROLES),
                UserType.BEFORE_SAVING, beforeSaving,
                UserType.AFTER_SAVING, afterSaving
        );
    }
}