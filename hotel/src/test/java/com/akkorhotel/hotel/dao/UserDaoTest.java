package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class UserDaoTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserDao userDao;

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection("USERS");
    }

    @Test
    void shouldSaveNewUser() {
        // Arrange
        User user = User.builder()
                .id("f2cccd2f-5711-4356-a13a-f687dc983ce0")
                .username("username")
                .email("email")
                .password("password")
                .build();

        // Act
        userDao.save(user);

        // Assert
        List<Map> savedUsers = mongoTemplate.findAll(Map.class, "USERS");
        assertThat((Map<String, Object>) savedUsers.getFirst())
                .containsExactlyInAnyOrderEntriesOf(ofEntries(
                        entry("_id", "f2cccd2f-5711-4356-a13a-f687dc983ce0"),
                        entry("username", "username"),
                        entry("email", "email"),
                        entry("password", "password"),
                        entry("isValidEmail", false),
                        entry("role", "USER")
                ));
    }

    @Test
    void shouldReturnUserRole() {
        // Arrange
        mongoTemplate.insert("""
                {
                    "_id": "f2cccd2f-5711-4356-a13a-f687dc983ce1",
                    "username": "username",
                    "password": "password",
                    "email": "email",
                    "isValidEmail": true,
                    "role": "ADMIN"
                }
                """, "USERS");

        // Act
        UserRole userRole = userDao.getUserRole("f2cccd2f-5711-4356-a13a-f687dc983ce1");

        // Assert
        assertThat(userRole).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void shouldReturnNull_whenUserDoesNotExist() {
        // Arrange
        String userId = "non-existent-user-id";

        // Act
        UserRole userRole = userDao.getUserRole(userId);

        // Assert
        assertThat(userRole).isNull();
    }

    @Test
    void shouldReturnTrue_whenUsernameExistsInDatabase() {
        // Arrange
        mongoTemplate.insert("""
            {
                "_id": "id",
                "username": "existingUsername",
                "password": "password",
                "email": "test@example.com",
                "isValidEmail": true,
                "role": "USER"
            }
            """, "USERS");

        // Act
        boolean isUserInDatabase = userDao.isUserInDatabase("existingUsername", "not.existing@example.com");

        // Assert
        assertThat(isUserInDatabase).isTrue();
    }

    @Test
    void shouldReturnTrue_whenUserEmailExistsInDatabase() {
        // Arrange
        mongoTemplate.insert("""
            {
                "_id": "id",
                "username": "username",
                "password": "password",
                "email": "existing.email@example.com",
                "isValidEmail": true,
                "role": "USER"
            }
            """, "USERS");

        // Act
        boolean isUserInDatabase = userDao.isUserInDatabase("notExistingUsername", "existing.email@example.com");

        // Assert
        assertThat(isUserInDatabase).isTrue();
    }

    @Test
    void shouldReturnFalse_whenUserDoesNotExistInDatabase() {
        // Arrange
        mongoTemplate.insert("""
            {
                "_id": "id",
                "username": "username",
                "password": "password",
                "email": "email@example.com",
                "isValidEmail": true,
                "role": "USER"
            }
            """, "USERS");

        // Act
        boolean exists = userDao.isUserInDatabase("nonExistentUser", "non.existent@example.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnUser_whenEmailExistsInDatabase() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "user-id",
            "username": "username",
            "password": "password",
            "email": "test@example.com",
            "isValidEmail": true,
            "role": "USER"
        }
        """, "USERS");

        // Act
        Optional<User> userOptional = userDao.findByEmail("test@example.com");

        // Assert
        assertThat(userOptional).isPresent();
        assertThat(userOptional.get().getId()).isEqualTo("user-id");
        assertThat(userOptional.get().getUsername()).isEqualTo("username");
        assertThat(userOptional.get().getPassword()).isEqualTo("password");
        assertThat(userOptional.get().getEmail()).isEqualTo("test@example.com");
        assertThat(userOptional.get().getIsValidEmail()).isEqualTo(true);
        assertThat(userOptional.get().getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void shouldReturnEmptyOptional_whenEmailDoesNotExistInDatabase() {
        // Act
        Optional<User> userOptional = userDao.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(userOptional).isEmpty();
    }

    @Test
    void shouldReturnUser_whenIdExistsInDatabase() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "userId",
            "username": "username",
            "password": "password",
            "email": "test@example.com",
            "isValidEmail": true,
            "role": "USER"
        }
        """, "USERS");

        // Act
        Optional<User> userOptional = userDao.findById("userId");

        // Assert
        assertThat(userOptional).isPresent();
        assertThat(userOptional.get().getId()).isEqualTo("userId");
        assertThat(userOptional.get().getUsername()).isEqualTo("username");
        assertThat(userOptional.get().getPassword()).isEqualTo("password");
        assertThat(userOptional.get().getEmail()).isEqualTo("test@example.com");
        assertThat(userOptional.get().getIsValidEmail()).isEqualTo(true);
        assertThat(userOptional.get().getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void shouldReturnEmptyOptional_whenIdDoesNotExistInDatabase() {
        // Act
        Optional<User> userOptional = userDao.findById("nonexistent_userId");

        // Assert
        assertThat(userOptional).isEmpty();
    }


}