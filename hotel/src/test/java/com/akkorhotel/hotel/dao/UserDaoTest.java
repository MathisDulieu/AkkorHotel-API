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

}