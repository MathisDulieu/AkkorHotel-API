package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.Hotel;
import com.akkorhotel.hotel.model.HotelRoom;
import com.akkorhotel.hotel.model.request.GetHotelsFilter;
import com.akkorhotel.hotel.model.request.GetHotelsFilters;
import com.akkorhotel.hotel.model.request.GetHotelsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    public long countHotelsWithRequest(GetHotelsFilters filters) {
        Criteria criteria = new Criteria();

        List<Integer> selectedStars = new ArrayList<>();
        if (filters.isOneStar()) selectedStars.add(1);
        if (filters.isTwoStars()) selectedStars.add(2);
        if (filters.isThreeStars()) selectedStars.add(3);
        if (filters.isFourStars()) selectedStars.add(4);
        if (filters.isFiveStars()) selectedStars.add(5);

        if (!selectedStars.isEmpty()) {
            criteria = criteria.and("stars").in(selectedStars);
        }

        if (filters.getHotelAmenities() != null && !filters.getHotelAmenities().isEmpty()) {
            criteria = criteria.and("amenities").all(filters.getHotelAmenities());
        }

        if (filters.getCity() != null && !filters.getCity().trim().isEmpty()) {
            criteria = criteria.and("location.city").is(filters.getCity());
        }

        if (filters.getMinPrice() > 0 && filters.getMaxPrice() > 0 && filters.getMinPrice() < filters.getMaxPrice()) {
            criteria = criteria.and("rooms.price").gte(filters.getMinPrice()).lte(filters.getMaxPrice());
        }

        Query query = new Query();
        if (!criteria.equals(new Criteria())) {
            query.addCriteria(criteria);
        }

        List<Hotel> hotels = mongoTemplate.find(query, Hotel.class, HOTEL_COLLECTION);

        return hotels.stream()
                .filter(hotel -> {
                    boolean hasEnoughRooms = filters.getBedrooms() <= 0 || hotel.getRooms().size() >= filters.getBedrooms();
                    boolean hasEnoughCapacity = true;
                    if (filters.getGuests() > 0) {
                        List<HotelRoom> roomsWithMaxOccupancy = hotel.getRooms().stream()
                                .sorted((r1, r2) -> Integer.compare(r2.getMaxOccupancy(), r1.getMaxOccupancy()))
                                .limit(filters.getBedrooms())
                                .toList();
                        int totalCapacity = roomsWithMaxOccupancy.stream()
                                .mapToInt(HotelRoom::getMaxOccupancy)
                                .sum();
                        hasEnoughCapacity = totalCapacity >= filters.getGuests();
                    }
                    return hasEnoughRooms && hasEnoughCapacity;
                })
                .count();
    }

    public List<Hotel> searchHotelsByRequest(GetHotelsRequest request) {
        Criteria criteria = new Criteria();
        GetHotelsFilters filters = request.getFilters();

        List<Integer> selectedStars = new ArrayList<>();
        if (filters.isOneStar()) selectedStars.add(1);
        if (filters.isTwoStars()) selectedStars.add(2);
        if (filters.isThreeStars()) selectedStars.add(3);
        if (filters.isFourStars()) selectedStars.add(4);
        if (filters.isFiveStars()) selectedStars.add(5);

        if (!selectedStars.isEmpty()) {
            criteria = criteria.and("stars").in(selectedStars);
        }

        if (filters.getHotelAmenities() != null && !filters.getHotelAmenities().isEmpty()) {
            criteria = criteria.and("amenities").all(filters.getHotelAmenities());
        }

        if (filters.getCity() != null && !filters.getCity().trim().isEmpty()) {
            criteria = criteria.and("location.city").is(filters.getCity());
        }

        if (filters.getMinPrice() > 0 && filters.getMaxPrice() > 0 && filters.getMinPrice() < filters.getMaxPrice()) {
            criteria = criteria.and("rooms.price").gte(filters.getMinPrice()).lte(filters.getMaxPrice());
        }

        Query query = new Query();
        if (!criteria.equals(new Criteria())) {
            query.addCriteria(criteria);
        }

        if (request.getFilter() != null) {
            if (request.getFilter().equals(GetHotelsFilter.PRICE_LOW_TO_HIGH.name())) {
                query.with(Sort.by(Sort.Direction.ASC, "rooms.price"));
            } else if (request.getFilter().equals(GetHotelsFilter.PRICE_HIGH_TO_LOW.name())) {
                query.with(Sort.by(Sort.Direction.DESC, "rooms.price"));
            }
        }

        if (request.getPage() >= 0 && request.getPageSize() > 0) {
            query.skip((long) request.getPage() * request.getPageSize()).limit(request.getPageSize());
        }

        List<Hotel> hotels = mongoTemplate.find(query, Hotel.class, HOTEL_COLLECTION);

        return hotels.stream()
                .filter(hotel -> {
                    boolean hasEnoughRooms = filters.getBedrooms() <= 0 || hotel.getRooms().size() >= filters.getBedrooms();
                    boolean hasEnoughCapacity = true;
                    if (filters.getGuests() > 0) {
                        List<HotelRoom> roomsWithMaxOccupancy = hotel.getRooms().stream()
                                .sorted((r1, r2) -> Integer.compare(r2.getMaxOccupancy(), r1.getMaxOccupancy()))
                                .limit(filters.getBedrooms())
                                .toList();
                        int totalCapacity = roomsWithMaxOccupancy.stream()
                                .mapToInt(HotelRoom::getMaxOccupancy)
                                .sum();
                        hasEnoughCapacity = totalCapacity >= filters.getGuests();
                    }
                    return hasEnoughRooms && hasEnoughCapacity;
                })
                .toList();
    }

}
