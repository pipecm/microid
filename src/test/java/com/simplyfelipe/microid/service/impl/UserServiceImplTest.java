package com.simplyfelipe.microid.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.simplyfelipe.microid.BaseTest;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest extends BaseTest {

    private enum UserType { BASIC, ADMIN, BEFORE_SAVING, AFTER_SAVING };

    private static final String USER_LIST_RESPONSE_PATH = "src/test/resources/responses/user_list_response.json";
    private static final String USER_LIST_DTO_RESPONSE_PATH = "src/test/resources/responses/user_dto_list_response.json";

    private static final UUID USER_ID = UUID.fromString("d568123c-0520-460f-84d9-1b127569978a");
    private static final String EMAIL = "user@mail.com";
    private static final String DECODED_PASSWORD = "12345";
    private static final String ENCODED_PASSWORD = "$2a$10$VtLnU/ZYARTQOThiocEuue3w6xLMi0Z/PO3KvSBwwDEWuaHN2tfq2";
    private static final String NO_EXCEPTION_THROWN = "No exception thrown";
    private static final String USER_ALREADY_EXISTS_MSG = "User %s already exists";
    private static final String USER_DOES_NOT_EXIST_MSG = "User %s does not exist";

    private static final List<Role> BASIC_ROLES = List.of(new Role(RoleName.USER));
    private static final List<Role> ADMIN_ROLES = List.of(new Role(RoleName.USER), new Role(RoleName.ADMIN));

    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserMapper userMapper = spy(new UserMapper(passwordEncoder));

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void whenSearchWithoutFiltersThenReturnAllUsers() throws Exception {
        List<User> usersFound = readFile(USER_LIST_RESPONSE_PATH, new TypeReference<>() {});
        List<UserDto> usersFoundDto = readFile(USER_LIST_DTO_RESPONSE_PATH, new TypeReference<>() {});

        when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any())).thenReturn(usersFound);

        List<UserDto> userDtoList = userService.findUsers(FindUsersFilters.builder().build());

        verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any());

        assertThat(userDtoList)
                .isNotNull()
                .hasSize(usersFoundDto.size())
                .containsAll(usersFoundDto);
    }

    @ParameterizedTest
    @CsvSource({"true,USER", "true,ADMIN", "false,USER", "false,ADMIN"})
    void whenSearchWithFiltersThenReturnUsersThatFulfillTheFilters(boolean active, RoleName roleName) throws Exception {
        List<User> usersFound = readFile(USER_LIST_RESPONSE_PATH, new TypeReference<>() {});
        List<UserDto> usersFoundDto = readFile(USER_LIST_DTO_RESPONSE_PATH, new TypeReference<>() {});

        List<User> usersFiltered = usersFound.stream()
                .filter(u -> u.getActive() == active)
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getRoleName().equals(roleName)))
                .toList();

        List<UserDto> usersDtoFiltered = usersFoundDto.stream()
                .filter(u -> u.getActive() == active)
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.equals(roleName)))
                .toList();

        when(userRepository.findAll(ArgumentMatchers.<Specification<User>>any())).thenReturn(usersFiltered);

        List<UserDto> userDtoList = userService.findUsers(FindUsersFilters.builder()
                                                .active(active)
                                                .roleName(roleName)
                                                .build());

        verify(userRepository).findAll(ArgumentMatchers.<Specification<User>>any());

        assertThat(userDtoList)
                .isNotNull()
                .hasSize(usersDtoFiltered.size())
                .isEqualTo(usersDtoFiltered);
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
        User afterSaving = usersMap().get(UserType.AFTER_SAVING);

        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(afterSaving);
        when(roleService.processRoles(BASIC_ROLES)).thenReturn(BASIC_ROLES);
        when(passwordEncoder.encode(DECODED_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        UserDto created = userService.createUser(request);

        verify(userRepository).findByEmailIgnoreCase(EMAIL);
        verify(userRepository).save(any(User.class));
        verify(roleService).processRoles(BASIC_ROLES);
        verify(passwordEncoder).encode(DECODED_PASSWORD);

        assertUserDtoEquals(created, response);
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
        UserDto response = UserDto.builder().id(USER_ID).email(EMAIL).active(true).roles(List.of(RoleName.USER, RoleName.ADMIN)).build();

        User userFound = usersMap().get(UserType.BEFORE_SAVING);
        User afterSaving = usersMap().get(UserType.AFTER_SAVING);

        afterSaving.setRoles(ADMIN_ROLES);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userFound));
        when(userRepository.save(any(User.class))).thenReturn(afterSaving);
        when(roleService.processRoles(ADMIN_ROLES)).thenReturn(ADMIN_ROLES);

        UserDto updated = userService.updateUser(USER_ID, request);

        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(any(User.class));
        verify(roleService).processRoles(ADMIN_ROLES);
        verify(roleService).processRoles(ADMIN_ROLES);

        assertUserDtoEquals(updated, response);
    }

    @Test
    void whenUpdatingNonExistingUserThenExceptionIsThrown() {
        UserDto request = UserDto.builder().email(EMAIL).password(DECODED_PASSWORD).roles(List.of(RoleName.USER, RoleName.ADMIN)).active(true).build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        try {
            userService.updateUser(USER_ID, request);
            fail(NO_EXCEPTION_THROWN);
        } catch (UsernameNotFoundException exception) {
            assertThat(exception).hasMessage(String.format(USER_DOES_NOT_EXIST_MSG, USER_ID.toString()));
        }

        verify(userRepository).findById(USER_ID);
    }

    @Test
    void whenDeactivatingExistingUserThenUserDeactivatedSuccessfully() {
        User userFound = usersMap().get(UserType.BEFORE_SAVING);
        User afterSaving = usersMap().get(UserType.AFTER_SAVING);

        afterSaving.setActive(false);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userFound));
        when(userRepository.save(any(User.class))).thenReturn(afterSaving);

        userService.deactivateUser(USER_ID);

        verify(userRepository).findById(USER_ID);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void whenDeactivatingNonExistingUserThenExceptionIsThrown() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        try {
            userService.deactivateUser(USER_ID);
            fail(NO_EXCEPTION_THROWN);
        } catch (UsernameNotFoundException exception) {
            assertThat(exception).hasMessage(String.format(USER_DOES_NOT_EXIST_MSG, USER_ID.toString()));
        }

        verify(userRepository).findById(USER_ID);
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

    private void assertUserDtoEquals(UserDto actual, UserDto expected) {
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getEmail()).isEqualTo(expected.getEmail());
        assertThat(actual.getActive()).isEqualTo(expected.getActive());
        assertThat(actual.getRoles()).isEqualTo(expected.getRoles());
    }
}