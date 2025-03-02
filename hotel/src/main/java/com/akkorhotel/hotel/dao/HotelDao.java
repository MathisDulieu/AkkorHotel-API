package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.Hotel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HotelDao {

    private final MongoTemplate mongoTemplate;

    private static final String HOTEL_COLLECTION = "HOTELS";

    public void save(Hotel hotel) {
        mongoTemplate.save(hotel, HOTEL_COLLECTION);
    }

    public Optional<Hotel> findById(String hotelId) {
        return Optional.ofNullable(mongoTemplate.findById(hotelId, Hotel.class, HOTEL_COLLECTION));
    }

    public void delete(String hotelId) {
        mongoTemplate.remove(new Query(Criteria.where("_id").is(hotelId)), HOTEL_COLLECTION);
    }

    public long countHotelsByNamePrefix(String keyword) {
        Query query = new Query(Criteria.where("name").regex("(?i)^" + keyword));

        return mongoTemplate.count(query, Hotel.class, HOTEL_COLLECTION);
    }

    public List<Hotel> searchHotelsByNamePrefix(String keyword, int page, int pageSize) {
        Aggregation aggregation = buildUserSearchAggregation(keyword, page, pageSize);
        return mongoTemplate.aggregate(aggregation, HOTEL_COLLECTION, Hotel.class).getMappedResults();
    }

    private Aggregation buildUserSearchAggregation(String keyword, int page, int pageSize) {
        int offset = page * pageSize;

        return Aggregation.newAggregation(
                Aggregation.match(Criteria.where("name").regex("(?i)^" + keyword)),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "name")),
                Aggregation.skip(offset),
                Aggregation.limit(pageSize)
        );
    }
}
