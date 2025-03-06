package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.HotelDao;
import com.akkorhotel.hotel.model.Hotel;
import com.akkorhotel.hotel.model.HotelRoom;
import com.akkorhotel.hotel.model.request.GetHotelsFilter;
import com.akkorhotel.hotel.model.request.GetHotelsRequest;
import com.akkorhotel.hotel.model.response.GetAllHotelsHotelResponse;
import com.akkorhotel.hotel.model.response.GetAllHotelsResponse;
import com.akkorhotel.hotel.model.response.GetHotelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelDao hotelDao;

    public ResponseEntity<Map<String, GetHotelResponse>> getHotel(String hotelId) {
        GetHotelResponse response = GetHotelResponse.builder().build();

        Optional<Hotel> optionalHotel = hotelDao.findById(hotelId);
        if (optionalHotel.isEmpty()) {
            response.setError("Hotel not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", response));
        }

        response.setHotel(optionalHotel.get());

        return ResponseEntity.ok(singletonMap("informations", response));
    }

    public ResponseEntity<Map<String, GetAllHotelsResponse>> getHotels(GetHotelsRequest request) {
        GetAllHotelsResponse response = GetAllHotelsResponse.builder().build();
        request.setPageSize(getPageSizeValue(request.getPageSize()));

        String error = validateRequest(request);
        if (!isNull(error)) {
            response.setError(error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", response));
        }

        long totalHotels = hotelDao.countHotelsWithRequest(request.getFilters());
        response.setHotelsFound(totalHotels);
        if (totalHotels == 0) {
            response.setError("No hotel found");
            return ResponseEntity.ok(singletonMap("informations", response));
        }

        int totalPages = getTotalPages(totalHotels, request.getPageSize());
        response.setTotalPages(totalPages);

        if (request.getPage() > totalPages) {
            response.setError("Requested page exceeds the total number of available pages");
            return ResponseEntity.ok(singletonMap("warning", response));
        }

        List<Hotel> hotels = hotelDao.searchHotelsByRequest(request);

        response.setHotels(buildGetAllHotelsResponse(hotels, request.getFilters().getGuests(), request.getFilters().getBedrooms(), request.getFilters().getMinPrice(), request.getFilters().getMaxPrice(), response, request.getPageSize()));

        return ResponseEntity.ok(singletonMap("informations", response));
    }

    private List<GetAllHotelsHotelResponse> buildGetAllHotelsResponse(
            List<Hotel> hotels, int guests, int bedrooms, int minPrice, int maxPrice, GetAllHotelsResponse response, int pageSize) {

        return hotels.stream()
                .map(hotel -> {
                    double hotelMinPrice = findMinimumPrice(hotel.getRooms(), guests, bedrooms);

                    if (hotelMinPrice >= minPrice && hotelMinPrice <= maxPrice) {
                        return GetAllHotelsHotelResponse.builder()
                                .hotelId(hotel.getId())
                                .description(hotel.getDescription())
                                .price(hotelMinPrice)
                                .address(hotel.getLocation().getAddress())
                                .googleMapUrl(hotel.getLocation().getGoogleMapsUrl())
                                .firstPicture(hotel.getPicture_list().get(0))
                                .name(hotel.getName())
                                .stars(hotel.getStars())
                                .build();
                    } else {
                        long totalHotels = response.getHotelsFound();
                        totalHotels = totalHotels - 1;

                        response.setHotelsFound(totalHotels);
                        int totalPages = getTotalPages(totalHotels, pageSize);
                        response.setTotalPages(totalPages);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private double findMinimumPrice(List<HotelRoom> rooms, int guests, int bedrooms) {
        List<HotelRoom> validRooms = rooms.stream()
                .sorted(Comparator.comparingDouble(HotelRoom::getPrice))
                .toList();

        return getBestPriceCombination(validRooms, guests, bedrooms);
    }

    private double getBestPriceCombination(List<HotelRoom> rooms, int guests, int bedrooms) {
        double minPrice = Double.MAX_VALUE;

        List<List<HotelRoom>> combinations = getRoomCombinations(rooms, bedrooms);

        for (List<HotelRoom> combination : combinations) {
            int totalGuests = combination.stream().mapToInt(HotelRoom::getMaxOccupancy).sum();
            double totalPrice = combination.stream().mapToDouble(HotelRoom::getPrice).sum();

            if (totalGuests >= guests) {
                minPrice = Math.min(minPrice, totalPrice);
            }
        }

        return (minPrice == Double.MAX_VALUE) ? 0 : minPrice;
    }

    private List<List<HotelRoom>> getRoomCombinations(List<HotelRoom> rooms, int bedrooms) {
        List<List<HotelRoom>> result = new ArrayList<>();
        generateCombinations(rooms, new ArrayList<>(), result, 0, bedrooms);
        return result;
    }

    private void generateCombinations(List<HotelRoom> rooms, List<HotelRoom> current, List<List<HotelRoom>> result, int index, int bedrooms) {
        if (current.size() == bedrooms) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = index; i < rooms.size(); i++) {
            current.add(rooms.get(i));
            generateCombinations(rooms, current, result, i + 1, bedrooms);
            current.remove(current.size() - 1);
        }
    }

    private int getPageSizeValue(int pageSize) {
        return (pageSize == 0) ? 10 : pageSize;
    }

    private String validateRequest(GetHotelsRequest request) {
        if (request.getPage() < 0) return "Page number must be greater than or equal to zero";
        if (!isValidFilter(request.getFilter())) return "Invalid filter provided";
        if (request.getPageSize() < 0) return "Page size must be greater than or equal to zero";
        if (request.getFilters().getMinPrice() < 0) return "Minimum price must be greater than or equal to zero";
        if (request.getFilters().getMinPrice() >= request.getFilters().getMaxPrice()) return "Minimum price must be less than maximum price";
        if (request.getFilters().getGuests() < request.getFilters().getBedrooms()) return "Number of guests must be greater than or equal to the number of bedrooms";
        return null;
    }

    private boolean isValidFilter(String filter) {
        for (GetHotelsFilter validFilter : GetHotelsFilter.values()) {
            if (validFilter.name().equalsIgnoreCase(filter)) {
                return true;
            }
        }
        return false;
    }

    private int getTotalPages(long totalUsers, int pageSize) {
        return (int) Math.ceil((double) totalUsers / pageSize);
    }
}
