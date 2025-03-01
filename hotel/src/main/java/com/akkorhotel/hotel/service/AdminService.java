package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.HotelDao;
import com.akkorhotel.hotel.dao.HotelRoomDao;
import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.*;
import com.akkorhotel.hotel.model.request.AdminUpdateUserRequest;
import com.akkorhotel.hotel.model.request.CreateHotelRequest;
import com.akkorhotel.hotel.model.request.CreateHotelRoomRequest;
import com.akkorhotel.hotel.model.response.GetAllUsersResponse;
import com.akkorhotel.hotel.model.response.GetUserByIdResponse;
import com.akkorhotel.hotel.utils.ImageUtils;
import com.akkorhotel.hotel.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserDao userDao;
    private final UserUtils userUtils;
    private final ImageService imageService;
    private final HotelDao hotelDao;
    private final UuidProvider uuidProvider;
    private final ImageUtils imageUtils;
    private final HotelRoomDao hotelRoomDao;

    public ResponseEntity<Map<String, GetAllUsersResponse>> getAllUsers(String keyword, int page, int pageSize) {
        GetAllUsersResponse response = GetAllUsersResponse.builder().build();
        pageSize = getPageSizeValue(pageSize);

        String error = validateRequest(keyword, pageSize, page);
        if (!isNull(error)) {
            response.setError(error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", response));
        }

        long totalUsers = userDao.countUsersByUsernamePrefix(keyword);
        if (totalUsers == 0) {
            response.setError("No users found");
            return ResponseEntity.ok(singletonMap("users", response));
        }

        int totalPages = getTotalPages(totalUsers, pageSize);

        if (page > totalPages) {
            response.setError("Requested page exceeds the total number of available pages");
            return ResponseEntity.ok(singletonMap("warning", response));
        }

        response.setUsers(userDao.searchUsersByUsernamePrefix(keyword, page, pageSize));
        response.setTotalPages(totalPages);

        return ResponseEntity.ok(singletonMap("users", response));
    }

    public ResponseEntity<Map<String, GetUserByIdResponse>> getUserById(String userId) {
        GetUserByIdResponse response = GetUserByIdResponse.builder().build();
        Optional<User> optionalUser = userDao.findById(userId);

        if (optionalUser.isEmpty()) {
            response.setError("User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", response));
        }

        User user = optionalUser.get();
        if (user.getRole().equals(UserRole.ADMIN)) {
            response.setError("Admin users cannot be retrieved");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(singletonMap("error", response));
        }

        user.setPassword(null);
        response.setUser(user);

        return ResponseEntity.ok(singletonMap("user", response));
    }

    public ResponseEntity<Map<String, String>> updateUser(String userId, AdminUpdateUserRequest request) {
        Optional<User> optionalUser = userDao.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", "User not found"));
        }

        User user = optionalUser.get();
        List<String> errors = new ArrayList<>();

        validateRequest(errors, request);
        if (errors.isEmpty()) {
            validateNewUsername(errors, request.getUsername(), user);
            validateNewEmail(errors, request.getEmail(), user);
            validateNewRole(errors, request.getRole(), user);
            validateIsValidEmailValue(errors, request.getIsValidEmail(), user);
            validateNewUserProfileImage(errors, request.getProfileImageUrl(), user);
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("errors", userUtils.getErrorsAsString(errors)));
        }

        userDao.save(user);

        return ResponseEntity.ok(singletonMap("message", "User with id: " + userId + " updated successfully"));
    }

    public ResponseEntity<Map<String, String>> createHotel(User authenticatedUser, CreateHotelRequest request, List<MultipartFile> picture_list) {
        List<String> errors = new ArrayList<>();

        validateName(errors, request.getName());
        validateDescription(errors, request.getDescription());
        validateLocation(errors, request.getCity(), request.getAddress(), request.getCountry(), request.getGoogleMapsUrl(), request.getState(), request.getPostalCode());
        validateAmenities(errors, request.getAmenities());

        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("errors", userUtils.getErrorsAsString(errors)));
        }

        List<String> pictureUrlsList = getPictureListUrls(picture_list, authenticatedUser, request.getName());
        if (isNull(pictureUrlsList)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", "At least one valid picture is required"));
        }

        Hotel hotel = buildHotel(request, pictureUrlsList);
        hotelDao.save(hotel);

        return ResponseEntity.ok(singletonMap("message", "Hotel created successfully"));
    }

    public ResponseEntity<Map<String, String>> addRoomToHotel(CreateHotelRoomRequest request) {
        List<String> errors = new ArrayList<>();
        validateRequest(errors, request);

        HotelRoomType hotelRoomType = getHotelRoomType(errors, request.getType());
        List<HotelRoomFeatures> hotelRoomFeatures = getHotelRoomFeatures(errors, request.getFeatures());

        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("errors", userUtils.getErrorsAsString(errors)));
        }

        Optional<Hotel> optionalHotel = hotelDao.findById(request.getHotelId());
        if (optionalHotel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", "Hotel not found"));
        }

        Hotel hotel = optionalHotel.get();

        HotelRoom hotelRoom = buildHotelRoom(request, hotelRoomType, hotelRoomFeatures);

        List<HotelRoom> hotelRooms = new ArrayList<>(hotel.getRooms());
        hotelRooms.add(hotelRoom);
        hotel.setRooms(hotelRooms);

        hotelRoomDao.save(hotelRoom);
        hotelDao.save(hotel);

        return ResponseEntity.ok(singletonMap("message", "HotelRoom added successfully"));
    }

    public ResponseEntity<Map<String, String>> deleteRoomFromHotel() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> addHotelPhoto() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> deleteHotelPhoto() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> updateHotel() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> deleteHotel() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> getAllUserBookings() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> getAllHotelBookings() {
        return ResponseEntity.ok().build();
    }

    private void validateRequest(List<String> errors, CreateHotelRoomRequest request) {
        if (isNull(request.getHotelId())) {
            errors.add("Hotel ID cannot be null");
        }

        if (request.getMaxOccupancy() <= 0) {
            errors.add("Room capacity must be greater than 0");
        }

        if (request.getPrice() <= 0) {
            errors.add("Room price must be greater than 0");
        }
    }

    private HotelRoom buildHotelRoom(CreateHotelRoomRequest request, HotelRoomType hotelRoomType, List<HotelRoomFeatures> hotelRoomFeatures) {
        return HotelRoom.builder()
                .id(uuidProvider.generateUuid())
                .price(request.getPrice())
                .maxOccupancy(request.getMaxOccupancy())
                .type(hotelRoomType)
                .features(hotelRoomFeatures)
                .build();
    }

    private List<HotelRoomFeatures> getHotelRoomFeatures(List<String> errors, List<String> features) {
        if (isNull(features) || features.isEmpty()) {
            errors.add("Room features cannot be null or empty");
            return null;
        }

        Set<String> validFeatures = Arrays.stream(HotelRoomFeatures.values())
                .map(Enum::name)
                .collect(toSet());

        return features.stream()
                .map(String::toUpperCase)
                .filter(feature -> {
                    if (!validFeatures.contains(feature)) {
                        errors.add("Invalid feature: " + feature);
                        return false;
                    }
                    return true;
                })
                .map(HotelRoomFeatures::valueOf)
                .collect(toList());
    }

    private HotelRoomType getHotelRoomType(List<String> errors, String type) {
        if (isNull(type)) {
            errors.add("Room type cannot be null");
            return null;
        }

        try {
            return HotelRoomType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            errors.add("Invalid type: " + type);
            return null;
        }
    }

    private int getTotalPages(long totalUsers, int pageSize) {
        return (int) Math.ceil((double) totalUsers / pageSize);
    }

    private List<String> getPictureListUrls(List<MultipartFile> pictureList, User authenticatedUser, String hotelName) {
        if (isNull(pictureList) || pictureList.isEmpty()) {
            return null;
        }

        List<String> urls = pictureList.stream()
                .map(file -> processSingleImage(file, authenticatedUser, hotelName))
                .filter(Objects::nonNull)
                .collect(toList());

        return urls.isEmpty() ? null : urls;
    }

    private String processSingleImage(MultipartFile file, User authenticatedUser, String hotelName) {
        ImageExtension imageExtension = imageService.getImageExtension(file.getOriginalFilename());
        if (isNull(imageExtension)) {
            return null;
        }

        String filename = "hotel-image-" + hotelName + "." + imageExtension.name();
        String url = uploadImage(file);

        if (url != null) {
            imageService.saveNewImage(ImageCategory.HOTEL, filename, url, imageExtension, authenticatedUser.getId());
        }

        return url;
    }

    private String uploadImage(MultipartFile file) {
        try {
            return imageUtils.uploadImage(file);
        } catch (IOException e) {
            return null;
        }
    }

    private Hotel buildHotel(CreateHotelRequest request, List<String> picture_list) {
        return Hotel.builder()
                .id(uuidProvider.generateUuid())
                .location(buildHotelLocation(request))
                .amenities(getHotelAmenities(request.getAmenities()))
                .name(request.getName())
                .description(request.getDescription())
                .picture_list(picture_list)
                .build();
    }

    private List<HotelAmenities> getHotelAmenities(List<String> amenities) {
        return amenities.stream()
                .map(String::toUpperCase)
                .map(HotelAmenities::valueOf)
                .collect(toList());
    }

    private HotelLocation buildHotelLocation(CreateHotelRequest request) {
        return HotelLocation.builder()
                .id(uuidProvider.generateUuid())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .googleMapsUrl(request.getGoogleMapsUrl())
                .build();
    }

    private void validateAmenities(List<String> errors, List<String> amenities) {
        if (isNull(amenities) || amenities.isEmpty()) {
            errors.add("The amenities list cannot be null or empty");
            return;
        }

        Set<String> validAmenities = Arrays.stream(HotelAmenities.values())
                .map(Enum::name)
                .collect(toSet());

        amenities.stream()
                .filter(amenity -> !validAmenities.contains(amenity))
                .forEach(invalidAmenity -> errors.add("Invalid amenity: " + invalidAmenity));
    }


    private void validateLocation(List<String> errors, String city, String address, String country, String googleMapsUrl, String state, String postalCode) {
        validateCity(errors, city);
        validateAddress(errors, address);
        validateCountry(errors, country);
        validateGoogleMapsUrl(errors, googleMapsUrl);
        validateState(errors, state);
        validatePostalCode(errors, postalCode);
    }

    private void validateCity(List<String> errors, String city) {
        if (isNull(city) || city.trim().isEmpty()) {
            errors.add("The city cannot be null or empty");
        }
    }

    private void validateAddress(List<String> errors, String address) {
        if (isNull(address) || address.trim().isEmpty()) {
            errors.add("The address cannot be null or empty");
        } else if (address.length() > 100) {
            errors.add("The address must be less than or equal to 100 characters long");
        }
    }

    private void validateCountry(List<String> errors, String country) {
        if (isNull(country) || country.trim().isEmpty()) {
            errors.add("The country cannot be null or empty");
        }
    }

    private void validateGoogleMapsUrl(List<String> errors, String googleMapsUrl) {
        if (isNull(googleMapsUrl) || googleMapsUrl.trim().isEmpty()) {
            errors.add("The Google Maps URL cannot be null or empty");
        } else {
            if (!googleMapsUrl.startsWith("https://")) {
                errors.add("Google Maps url must start with 'https://'");
            }
        }
    }

    private void validateState(List<String> errors, String state) {
        if (isNull(state) || state.trim().isEmpty()) {
            errors.add("The state cannot be null or empty");
        } else if (state.length() > 50) {
            errors.add("The state cannot be longer than 50 characters");
        }
    }

    private void validatePostalCode(List<String> errors, String postalCode) {
        if (isNull(postalCode) || postalCode.trim().isEmpty()) {
            errors.add("The postal code cannot be null or empty");
        } else if (postalCode.length() > 10) {
            errors.add("The postal code cannot be longer than 10 characters");
        }
    }

    private void validateDescription(List<String> errors, String description) {
        if (description.length() > 500) {
            errors.add("The hotel description must be less than or equal to 500 characters long");
        }
    }

    private void validateName(List<String> errors, String name) {
        if (isNull(name) || name.trim().isEmpty()) {
            errors.add("The hotel name cannot be null or empty");
        } else {
            if (name.length() < 3 || name.length() > 25) {
                errors.add("The hotel name must be between 3 and 25 characters long");
            }

            if (name.contains(" ")) {
                errors.add("The hotel name cannot contain spaces");
            }
        }
    }

    private String validateRequest(String keyword, int pageSize, int page) {
        if (pageSize < 0) return "Page size must be greater than or equal to zero";
        if (page < 0) return "Page number must be greater than or equal to zero";
        if (keyword.contains(" ")) return "Search keyword cannot contain spaces";
        return null;
    }

    private void validateRequest(List<String> errors, AdminUpdateUserRequest request) {
        if (isNull(request.getEmail()) && isNull(request.getUsername()) && isNull(request.getIsValidEmail()) && isNull(request.getRole()) && isNull(request.getProfileImageUrl())) {
            errors.add("No values provided for update. Please specify at least one field (email, username, isValidEmail, profileImageUrl or role)");
        }
    }

    private void validateNewUsername(List<String> errors, String username, User userToUpdate) {
        if (!isNull(username)) {
            if (userUtils.isInvalidUsername(username)) {
                errors.add("The provided username is invalid. It must be between 3 and 11 characters long and cannot contain spaces");
            }

            if (userDao.isUsernameAlreadyUsed(username)) {
                errors.add("The username '" + username + "' is already in use by another account. Please choose a different one");
            }

            if (username.equals(userToUpdate.getUsername())) {
                errors.add("The new username must be different from the current one");
            }

            userToUpdate.setUsername(username);
        }
    }

    private void validateNewUserProfileImage(List<String> errors, String profileImageUrl, User userToUpdate) {
        if (!isNull(profileImageUrl)) {
            if (!profileImageUrl.startsWith("https://")) {
                errors.add("The provided URL must start with 'https://'");
            }

            ImageExtension imageExtension = imageService.getImageExtension(profileImageUrl);
            if (isNull(imageExtension)) {
                errors.add("The provided URL does not have a valid image format. Please provide a valid image URL");
            }

            userToUpdate.setProfileImageUrl(profileImageUrl);
        }
    }

    private void validateNewEmail(List<String> errors, String email, User userToUpdate) {
        if (!isNull(email)) {
            if (userUtils.isInvalidEmail(email)) {
                errors.add("The provided email format is invalid. Please enter a valid email address");
            }

            if (userDao.isEmailAlreadyUsed(email)) {
                errors.add("The email address '" + email + "' is already associated with another account");
            }

            if (email.equals(userToUpdate.getEmail())) {
                errors.add("The new email address must be different from the current one");
            }

            userToUpdate.setEmail(email);
        }
    }

    private void validateNewRole(List<String> errors, String role, User userToUpdate) {
        if (!isNull(role)) {
            if (role.equals(userToUpdate.getRole().toString())) {
                errors.add("The new role must be different from the current one");
            }

            try {
                UserRole newRole = UserRole.valueOf(role.toUpperCase());
                userToUpdate.setRole(newRole);
            } catch (IllegalArgumentException e) {
                errors.add("Invalid role: " + role + ". Allowed values are: " + Arrays.toString(UserRole.values()));
            }
        }
    }

    private void validateIsValidEmailValue(List<String> errors, Boolean isValidEmail, User user) {
        if (!isNull(isValidEmail)) {
            if (user.getIsValidEmail().equals(isValidEmail)) {
                errors.add("The email verification status is already set to the provided value. No changes were made.");
            }

            user.setIsValidEmail(isValidEmail);
        }
    }

    private int getPageSizeValue(int pageSize) {
        return (pageSize == 0) ? 10 : pageSize;
    }

}
