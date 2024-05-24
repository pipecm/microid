package com.simplyfelipe.microid.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.simplyfelipe.microid.entity.RoleName;
import com.simplyfelipe.microid.entity.User;
import com.simplyfelipe.microid.filter.FindUsersFilters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static com.simplyfelipe.microid.repository.BaseRepositoryTest.DataSourceInitializer;

@Testcontainers
@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = DataSourceInitializer.class)
class UserRepositoryTest extends BaseRepositoryTest {

    private static final String USER_LIST_RESPONSE_PATH = "src/test/resources/responses/user_list_response.json";
    private static final String USER_REPOSITORY_FILTERS_PATH = "/csv/user_repository_filters.csv";
    private static final String NULL_PARAM = "null";

    @Autowired
    private UserRepository userRepository;

    private List<User> allUsers;

    @BeforeEach
    public void setUp() throws Exception {
        allUsers = userRepository.saveAll(readFile(USER_LIST_RESPONSE_PATH, new TypeReference<>() {}));
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void whenSearchWithoutFiltersThenReturnAllUsers() {
        List<User> foundUsers = userRepository.findAll();
        assertThat(foundUsers).containsAll(allUsers);
    }

    @Test
    void whenSearchWithEmptyFiltersThenReturnAllUsers() {
        List<User> foundUsers = userRepository.findAll(FindUsersFilters.builder().build().getSpecifications());
        assertThat(foundUsers).containsAll(allUsers);
    }

    @ParameterizedTest
    @CsvFileSource(resources = USER_REPOSITORY_FILTERS_PATH)
    void whenSearchWithFiltersThenReturnOnlyUsersThatFulfillTheFilters(String paramEmail, String paramActive, String paramRole, int expectedSize) {
        String email = NULL_PARAM.equals(paramEmail) ? null : paramEmail;
        Boolean isActive = NULL_PARAM.equals(paramActive) ? null : Boolean.parseBoolean(paramActive);
        RoleName roleName = NULL_PARAM.equals(paramRole) ? RoleName.UNDEFINED : RoleName.byName(paramRole);
        FindUsersFilters filters = FindUsersFilters.builder().email(email).active(isActive).roleName(roleName).build();
        List<User> foundUsers = userRepository.findAll(filters.getSpecifications());
        assertThat(foundUsers).hasSize(expectedSize);
    }
}
