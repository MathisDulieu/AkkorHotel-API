package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class UserDao {

    private final MongoTemplate mongoTemplate;

    private static final String USER_COLLECTION = "USERS";

    public void save(User user) {
        mongoTemplate.save(user, USER_COLLECTION);
    }

    public UserRole getUserRole(String userId) {
        Aggregation aggregation = newAggregation(
                match(where("_id").is(userId)),
                project("role")
        );

        return mongoTemplate.aggregate(aggregation, USER_COLLECTION, User.class)
                .getMappedResults()
                .stream()
                .findFirst()
                .map(User::getRole)
                .orElse(null);
    }

    public boolean isUserInDatabase(String username, String email) {
        return mongoTemplate.exists(new Query(new Criteria().orOperator(
                Criteria.where("username").is(username),
                Criteria.where("email").is(email)
        )), USER_COLLECTION);
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(mongoTemplate.findOne(new Query(Criteria.where("email").is(email)), User.class, USER_COLLECTION));
    }

    public Optional<User> findById(String userId) {
        return Optional.ofNullable(mongoTemplate.findById(userId, User.class, USER_COLLECTION));
    }

}
