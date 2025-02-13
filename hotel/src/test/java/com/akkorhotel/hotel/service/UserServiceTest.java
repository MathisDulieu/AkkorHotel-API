package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UuidProvider uuidProvider;

    @Mock
    private UserDao userDao;

    @Test
    void shouldCreateNewUser() {
        // Arrange
        User user = User.builder()
                .username("username")
                .email("email")
                .password("password")
                .build();

        when(uuidProvider.generateUuid()).thenReturn("anyId");

        // Act
        userService.createUser(user);

        // Assert
        InOrder inOrder = inOrder(uuidProvider, userDao);
        inOrder.verify(uuidProvider).generateUuid();
        inOrder.verify(userDao).save(user);
        inOrder.verifyNoMoreInteractions();
    }

}