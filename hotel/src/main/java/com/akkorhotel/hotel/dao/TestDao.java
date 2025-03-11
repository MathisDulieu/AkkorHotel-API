package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.Hotel;
import com.akkorhotel.hotel.model.HotelRoom;
import com.akkorhotel.hotel.model.User;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class TestDao {

    private final MongoTemplate mongoTemplate;

    private static final String USER_COLLECTION = "USERS";
    private static final String HOTEL_COLLECTION = "HOTELS";
    private static final String HOTEL_ROOM_COLLECTION = "HOTEL_ROOMS";
    private static final String TEST_COLLECTION = "TEST";

    public boolean doesDataAlreadyExists(String dataVerification) {
        Query query = new Query();
        query.addCriteria(Criteria.where("verificationId").is(dataVerification));

        return mongoTemplate.exists(query, TEST_COLLECTION);
    }

    public void saveDataVerification(String dataVerification) {
        Document document = new Document();
        document.put("verificationId", dataVerification);
        document.put("createdAt", new Date());

        mongoTemplate.insert(document, TEST_COLLECTION);
    }

    public void saveUser(User user) {
        mongoTemplate.save(user, USER_COLLECTION);
    }

    public void saveHotel(Hotel hotel) {
        mongoTemplate.save(hotel, HOTEL_COLLECTION);
    }

    public void saveHotelRoom(HotelRoom hotelRoom) {
        mongoTemplate.save(hotelRoom, HOTEL_ROOM_COLLECTION);
    }
}
