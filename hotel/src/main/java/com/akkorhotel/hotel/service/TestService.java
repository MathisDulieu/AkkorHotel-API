package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.TestDao;
import com.akkorhotel.hotel.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Collections.singletonMap;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestDao testDao;
    private final UuidProvider uuidProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final String DATA_VERIFICATION = "f2cccd2f-5711-4356-a13a-f687dc983ce1";
    private static final String USER_PASSWORD = "AnyStrongP@ss1!";

    public ResponseEntity<Map<String, String>> generateTestData() {
        if (!testDao.doesDataAlreadyExists(DATA_VERIFICATION)) {
            testDao.saveDataVerification(DATA_VERIFICATION);
            generateUsers();
            generateHotels();
        }

        return ResponseEntity.ok().body(singletonMap("informations", "Test data generated successfully"));
    }

    private void generateHotels() {
        List<String> names = getHotelNames();
        List<String> descriptions = getHotelDescriptions();
        List<HotelLocation> hotelLocations = getHotelLocations();
        List<List<String>> picturesLists = getPictureLists();
        List<List<HotelAmenities>> hotelsAmenities = getHotelsAmenities();
        List<List<HotelRoom>> hotelsRooms = getHotelsRooms();
        List<Integer> stars = getHotelStars();

        for (int i = 0; i < 50; i++) {
            Hotel hotel = Hotel.builder()
                    .id(uuidProvider.generateUuid())
                    .name(names.get(i))
                    .description(descriptions.get(i))
                    .location(hotelLocations.get(i))
                    .picture_list(picturesLists.get(i))
                    .amenities(hotelsAmenities.get(i))
                    .rooms(hotelsRooms.get(i))
                    .stars(stars.get(i))
                    .build();

            testDao.saveHotel(hotel);
        }
    }

    private List<List<String>> getPictureLists() {
        return List.of(
                List.of("https://i.ibb.co/39N6PwMy/1.jpg", "https://i.ibb.co/3YRZvhHv/2.jpg", "https://i.ibb.co/mCQcZ18p/3.jpg"),
                List.of("https://i.ibb.co/7ddPGtkC/1.jpg", "https://i.ibb.co/0j3HvPwV/2.jpg", "https://i.ibb.co/QFgm31MQ/3.jpg"),
                List.of("https://i.ibb.co/pjqHRJvP/1.jpg", "https://i.ibb.co/k27VW00H/2.jpg", "https://i.ibb.co/krg91DK/3.jpg"),
                List.of("https://i.ibb.co/BHFQ2Y4t/1.jpg", "https://i.ibb.co/d43DgQHg/2.jpg", "https://i.ibb.co/GQTrwK34/3.jpg"),
                List.of("https://i.ibb.co/m57Bm7SF/1.jpg", "https://i.ibb.co/vxmqgKP3/2.jpg", "https://i.ibb.co/x8R0BfV3/3.jpg"),
                List.of("https://i.ibb.co/7dQLS3Q0/1.jpg", "https://i.ibb.co/6JTLFrFZ/2.jpg", "https://i.ibb.co/9HGv5qTd/3.jpg"),
                List.of("https://i.ibb.co/Z6yGR0Mt/1.jpg", "https://i.ibb.co/tTbCdY9r/2.jpg", "https://i.ibb.co/nq0PK5Sc/3.jpg"),
                List.of("https://i.ibb.co/JRhFxX6c/1.jpg", "https://i.ibb.co/DfhwVg2t/2.jpg", "https://i.ibb.co/dw09044F/3.jpg"),
                List.of("https://i.ibb.co/spvMvWV0/1.jpg", "https://i.ibb.co/PGFbBR6G/2.jpg", "https://i.ibb.co/PZYmSjzH/3.jpg"),
                List.of("https://i.ibb.co/20xxfg2T/1.jpg", "https://i.ibb.co/gKY3cBT/2.jpg", "https://i.ibb.co/nsj5fmKw/3.jpg"),
                List.of("https://i.ibb.co/HD4xDqqB/1.jpg", "https://i.ibb.co/j7VFg6n/2.jpg", "https://i.ibb.co/gL9YgZTt/3.jpg"),
                List.of("https://i.ibb.co/Kj35Fp1x/1.jpg", "https://i.ibb.co/HDxkwKCR/2.jpg", "https://i.ibb.co/kgk3LrS4/3.jpg"),
                List.of("https://i.ibb.co/qMj4WLMD/1.jpg", "https://i.ibb.co/MyfpwRxd/2.jpg", "https://i.ibb.co/Mx1P6y3k/3.jpg"),
                List.of("https://i.ibb.co/SDh1Cstc/1.jpg", "https://i.ibb.co/20P9NyRX/2.jpg", "https://i.ibb.co/RpTgPdG0/3.jpg"),
                List.of("https://i.ibb.co/W4xBh2nJ/1.jpg", "https://i.ibb.co/xcRnnDs/2.jpg", "https://i.ibb.co/F40FC2RC/3.jpg"),
                List.of("https://i.ibb.co/5td7HTN/1.jpg", "https://i.ibb.co/W4swsvbC/2.jpg", "https://i.ibb.co/q3sRWsvj/3.jpg"),
                List.of("https://i.ibb.co/8LRmKvNF/1.jpg", "https://i.ibb.co/mCNHCWPZ/2.jpg", "https://i.ibb.co/23pZnvZN/3.jpg"),
                List.of("https://i.ibb.co/5hvy88gC/1.jpg", "https://i.ibb.co/ZzJDN0nQ/2.jpg", "https://i.ibb.co/6c4qHhRf/3.jpg"),
                List.of("https://i.ibb.co/qLY8pwSS/1.jpg", "https://i.ibb.co/pvMbzBJ2/2.jpg", "https://i.ibb.co/HL6jH73d/3.jpg"),
                List.of("https://i.ibb.co/2YpXfwZG/1.jpg", "https://i.ibb.co/2YMMhQmg/2.jpg", "https://i.ibb.co/mFvnQswr/3.jpg"),
                List.of("https://i.ibb.co/v4kRxLXV/1.jpg", "https://i.ibb.co/prKttFRp/2.jpg", "https://i.ibb.co/BV0TXGSH/3.jpg"),
                List.of("https://i.ibb.co/4nNc1vBL/1.jpg", "https://i.ibb.co/nqmqsktg/2.jpg", "https://i.ibb.co/1JPKCPZ7/3.jpg"),
                List.of("https://i.ibb.co/rKq13rdH/1.jpg", "https://i.ibb.co/NnK4H2tM/2.jpg", "https://i.ibb.co/zWyrVSpP/3.jpg"),
                List.of("https://i.ibb.co/nNcSKvX1/1.jpg", "https://i.ibb.co/BVtD1cJN/2.jpg", "https://i.ibb.co/xq2d1jqg/3.jpg"),
                List.of("https://i.ibb.co/qMrZNGQq/1.jpg", "https://i.ibb.co/v4W9xXwz/2.jpg", "https://i.ibb.co/8npw0r8w/3.jpg"),
                List.of("https://i.ibb.co/LX84TZkS/1.jpg", "https://i.ibb.co/Xk3mY2vv/2.jpg", "https://i.ibb.co/Kc1h48pb/3.jpg"),
                List.of("https://i.ibb.co/pv2fCcHj/1.jpg", "https://i.ibb.co/MkrhKvZv/2.jpg", "https://i.ibb.co/3yr9SqKF/3.jpg"),
                List.of("https://i.ibb.co/5xsgWYrQ/1.jpg", "https://i.ibb.co/RpzhJC3D/2.jpg", "https://i.ibb.co/fVQWbK2R/3.jpg"),
                List.of("https://i.ibb.co/3YFtxdyk/1.jpg", "https://i.ibb.co/mVbvDQSb/2.jpg", "https://i.ibb.co/8LRt67K4/3.jpg"),
                List.of("https://i.ibb.co/KzyrM9zH/1.jpg", "https://i.ibb.co/2Qm4C3y/2.jpg", "https://i.ibb.co/XrkDSCSf/3.jpg"),
                List.of("https://i.ibb.co/spSgYq7F/1.jpg", "https://i.ibb.co/Q7JmjN55/2.jpg", "https://i.ibb.co/LD2XQ0P8/3.jpg"),
                List.of("https://i.ibb.co/35Qh5Dbk/1.jpg", "https://i.ibb.co/QvXhRWkT/2.jpg", "https://i.ibb.co/MyLcsP6Q/3.jpg"),
                List.of("https://i.ibb.co/VcpqS3QP/1.jpg", "https://i.ibb.co/1YyRrX36/2.jpg", "https://i.ibb.co/Z5Mh97Q/3.jpg"),
                List.of("https://i.ibb.co/Fk7HQ0YK/1.jpg", "https://i.ibb.co/b5nWShXp/2.jpg", "https://i.ibb.co/KzRQJ07c/3.jpg"),
                List.of("https://i.ibb.co/XZYKdqrC/1.jpg", "https://i.ibb.co/5ghsC410/2.jpg", "https://i.ibb.co/rR1nVNq5/3.jpg"),
                List.of("https://i.ibb.co/x855LyC2/1.jpg", "https://i.ibb.co/JwYYCcY0/2.jpg", "https://i.ibb.co/gLbhybCD/3.jpg"),
                List.of("https://i.ibb.co/5WDKZ8Hs/1.jpg", "https://i.ibb.co/bRD5CnPC/2.jpg", "https://i.ibb.co/W1M6P7q/3.jpg"),
                List.of("https://i.ibb.co/DDx9nTp9/1.jpg", "https://i.ibb.co/mkYzK0n/2.jpg", "https://i.ibb.co/N2Nn5068/3.jpg"),
                List.of("https://i.ibb.co/HfY3X9JF/1.jpg", "https://i.ibb.co/15knyhw/2.jpg", "https://i.ibb.co/zTLScX45/3.jpg"),
                List.of("https://i.ibb.co/fV9y3Crc/1.jpg", "https://i.ibb.co/gFXLbBN8/2.jpg", "https://i.ibb.co/ZpCB81wD/3.jpg"),
                List.of("https://i.ibb.co/C3hw9Ykn/1.jpg", "https://i.ibb.co/SDJQsGf1/2.jpg", "https://i.ibb.co/YBKTBN9H/3.jpg"),
                List.of("https://i.ibb.co/LzhZCg2S/1.jpg", "https://i.ibb.co/PsvmkWBj/2.jpg", "https://i.ibb.co/RG5RSQky/3.jpg"),
                List.of("https://i.ibb.co/yFrXZNwb/1.jpg", "https://i.ibb.co/WWYdJgCZ/2.jpg", "https://i.ibb.co/mVYv4KVK/3.jpg"),
                List.of("https://i.ibb.co/kgzpJSB8/1.jpg", "https://i.ibb.co/kVqTQMFB/2.jpg", "https://i.ibb.co/KxVWBL4r/3.jpg"),
                List.of("https://i.ibb.co/Vp51Hczw/1.jpg", "https://i.ibb.co/KjDxHbZS/2.jpg", "https://i.ibb.co/FLNrr4dS/3.jpg"),
                List.of("https://i.ibb.co/VcB0Vx6W/1.jpg", "https://i.ibb.co/200qjcHF/2.jpg", "https://i.ibb.co/N6KMTMNL/3.jpg"),
                List.of("https://i.ibb.co/YTTnZ3qw/1.jpg", "https://i.ibb.co/WWGrf9VD/2.jpg", "https://i.ibb.co/5xTSpVgR/3.jpg"),
                List.of("https://i.ibb.co/9JrpRXj/1.jpg", "https://i.ibb.co/VYS9R00g/2.jpg", "https://i.ibb.co/wZ6YjfmP/3.jpg"),
                List.of("https://i.ibb.co/xq1RH18g/1.jpg", "https://i.ibb.co/Ldvjqs2x/2.jpg", "https://i.ibb.co/zhpxHBrs/3.jpg"),
                List.of("https://i.ibb.co/jPxDPYDF/1.jpg", "https://i.ibb.co/SXR9DvQK/2.jpg", "https://i.ibb.co/21TkSypc/3.jpg")
        );
    }

    private List<List<HotelRoom>> getHotelsRooms() {
        List<List<HotelRoom>> hotelsRooms = new ArrayList<>();
        Random random = new Random();

        HotelRoomType[] roomTypes = HotelRoomType.values();
        HotelRoomFeatures[] roomFeatures = HotelRoomFeatures.values();

        for (int i = 0; i < 50; i++) {
            int numberOfRooms = random.nextInt(5) + 1;
            List<HotelRoom> hotelRooms = new ArrayList<>();

            for (int j = 0; j < numberOfRooms; j++) {
                int price = random.nextInt(1901) + 100;

                int maxOccupancy = random.nextInt(10) + 1;

                HotelRoomType roomType = roomTypes[random.nextInt(roomTypes.length)];

                List<HotelRoomFeatures> features;
                int numberOfFeatures = random.nextInt(roomFeatures.length) + 1;

                List<HotelRoomFeatures> featuresCopy = Arrays.asList(roomFeatures);
                Collections.shuffle(featuresCopy);
                features = new ArrayList<>(featuresCopy.subList(0, numberOfFeatures));

                HotelRoom room = HotelRoom.builder()
                        .id(uuidProvider.generateUuid())
                        .price(price)
                        .maxOccupancy(maxOccupancy)
                        .type(roomType)
                        .features(features)
                        .build();

                testDao.saveHotelRoom(room);

                hotelRooms.add(room);
            }

            hotelsRooms.add(hotelRooms);
        }

        return hotelsRooms;
    }

    private List<Integer> getHotelStars() {
        List<Integer> hotelStars = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 50; i++) {
            int stars = random.nextInt(5) + 1;
            hotelStars.add(stars);
        }

        return hotelStars;
    }

    private List<List<HotelAmenities>> getHotelsAmenities() {
        List<HotelAmenities> allAmenities = List.of(
                HotelAmenities.SMOKING_AREA,
                HotelAmenities.LAUNDRY,
                HotelAmenities.BUSINESS_CENTER,
                HotelAmenities.BAR,
                HotelAmenities.AIRPORT_SHUTTLE,
                HotelAmenities.PET_FRIENDLY,
                HotelAmenities.AIR_CONDITIONING,
                HotelAmenities.RESTAURANT,
                HotelAmenities.PARKING,
                HotelAmenities.SPA,
                HotelAmenities.GYM,
                HotelAmenities.POOL,
                HotelAmenities.WIFI
        );

        List<List<HotelAmenities>> hotelAmenitiesList = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 50; i++) {
            int numberOfAmenities = random.nextInt(allAmenities.size()) + 1;
            List<HotelAmenities> amenitiesCopy = new ArrayList<>(allAmenities);
            Collections.shuffle(amenitiesCopy);
            List<HotelAmenities> hotelAmenitiesSubset = amenitiesCopy.subList(0, numberOfAmenities);
            hotelAmenitiesList.add(new ArrayList<>(hotelAmenitiesSubset));
        }

        return hotelAmenitiesList;
    }

    private List<HotelLocation> getHotelLocations() {
        return List.of(
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Paris").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("75001").country("France").state("Île-de-France").address("1 Avenue des Champs-Élysées, 75001 Paris, France").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Nice").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("06000").country("France").state("Provence-Alpes-Côte d'Azur").address("10 Promenade des Anglais, 06000 Nice, France").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("New York").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("10001").country("USA").state("New York").address("123 Fifth Avenue, 10001 New York, USA").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Los Angeles").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("90001").country("USA").state("California").address("456 Sunset Boulevard, 90001 Los Angeles, USA").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Tokyo").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("100-0001").country("Japan").state("Tokyo").address("789 Shibuya, 100-0001 Tokyo, Japan").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("London").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("E1 6AN").country("UK").state("England").address("101 Buckingham Palace Road, E1 6AN London, UK").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Berlin").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("10115").country("Germany").state("Berlin").address("202 Alexanderplatz, 10115 Berlin, Germany").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Rome").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("00100").country("Italy").state("Lazio").address("30 Via del Corso, 00100 Rome, Italy").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Madrid").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("28001").country("Spain").state("Madrid").address("50 Gran Vía, 28001 Madrid, Spain").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Barcelona").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("08002").country("Spain").state("Catalonia").address("60 La Rambla, 08002 Barcelona, Spain").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Lisbon").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("1100-234").country("Portugal").state("Lisbon").address("100 Avenida da Liberdade, 1100-234 Lisbon, Portugal").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Vienna").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("1010").country("Austria").state("Vienna").address("10 Stephansplatz, 1010 Vienna, Austria").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Copenhagen").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("1000").country("Denmark").state("Capital Region").address("15 Nyhavn, 1000 Copenhagen, Denmark").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Amsterdam").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("1012").country("Netherlands").state("North Holland").address("123 Dam Square, 1012 Amsterdam, Netherlands").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Brussels").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("1000").country("Belgium").state("Brussels-Capital Region").address("25 Grand Place, 1000 Brussels, Belgium").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Zurich").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("8001").country("Switzerland").state("Zurich").address("10 Bahnhofstrasse, 8001 Zurich, Switzerland").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Stockholm").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("11121").country("Sweden").state("Stockholm County").address("20 Drottninggatan, 11121 Stockholm, Sweden").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Oslo").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("0152").country("Norway").state("Oslo").address("30 Karl Johans gate, 0152 Oslo, Norway").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Helsinki").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("00100").country("Finland").state("Uusimaa").address("40 Mannerheimintie, 00100 Helsinki, Finland").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Madrid").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("28013").country("Spain").state("Madrid").address("70 Calle de Gran Vía, 28013 Madrid, Spain").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Paris").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("75015").country("France").state("Île-de-France").address("80 Rue de Rivoli, 75015 Paris, France").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Sydney").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("2000").country("Australia").state("New South Wales").address("15 George Street, 2000 Sydney, Australia").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Melbourne").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("3000").country("Australia").state("Victoria").address("25 Collins Street, 3000 Melbourne, Australia").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Seoul").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("04524").country("South Korea").state("Seoul").address("35 Myeongdong, 04524 Seoul, South Korea").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Dubai").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("00000").country("UAE").state("Dubai").address("100 Sheikh Zayed Road, Dubai, UAE").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Bangkok").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("10100").country("Thailand").state("Bangkok").address("123 Sukhumvit Road, 10100 Bangkok, Thailand").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Mumbai").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("400001").country("India").state("Maharashtra").address("150 Marine Drive, 400001 Mumbai, India").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Singapore").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("018961").country("Singapore").state("Central Region").address("250 Orchard Road, 018961 Singapore").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Kuala Lumpur").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("50000").country("Malaysia").state("Wilayah Persekutuan").address("50 Jalan Bukit Bintang, 50000 Kuala Lumpur, Malaysia").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Hong Kong").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("00000").country("China").state("Hong Kong").address("1 Nathan Road, Kowloon, Hong Kong").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Moscow").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("101000").country("Russia").state("Moscow").address("200 Red Square, 101000 Moscow, Russia").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Cape Town").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("8001").country("South Africa").state("Western Cape").address("150 Long Street, 8001 Cape Town, South Africa").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Dubai").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("00000").country("UAE").state("Dubai").address("300 Palm Jumeirah, Dubai, UAE").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Toronto").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("M5G 2C8").country("Canada").state("Ontario").address("123 Bay Street, M5G 2C8 Toronto, Canada").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Montreal").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("H2X 3X2").country("Canada").state("Quebec").address("45 Rue Sainte-Catherine, H2X 3X2 Montreal, Canada").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Vancouver").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("V6B 1A1").country("Canada").state("British Columbia").address("50 Robson Street, V6B 1A1 Vancouver, Canada").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Mexico City").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("01000").country("Mexico").state("CDMX").address("100 Avenida Reforma, 01000 Mexico City, Mexico").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Buenos Aires").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("C1000").country("Argentina").state("Buenos Aires").address("123 Avenida 9 de Julio, C1000 Buenos Aires, Argentina").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Lagos").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("101233").country("Nigeria").state("Lagos").address("80 Victoria Island, 101233 Lagos, Nigeria").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Nairobi").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("00100").country("Kenya").state("Nairobi County").address("70 Kenyatta Avenue, 00100 Nairobi, Kenya").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Dubai").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("00000").country("UAE").state("Dubai").address("150 Sheikh Zayed Road, Dubai, UAE").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Hong Kong").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("00000").country("China").state("Hong Kong").address("20 Tsim Sha Tsui, Kowloon, Hong Kong").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Sydney").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("2000").country("Australia").state("New South Wales").address("5 Circular Quay, 2000 Sydney, Australia").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Cape Town").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("8001").country("South Africa").state("Western Cape").address("10 Victoria & Alfred Waterfront, 8001 Cape Town, South Africa").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Rio de Janeiro").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("20000-000").country("Brazil").state("Rio de Janeiro").address("100 Copacabana Beach, 20000-000 Rio de Janeiro, Brazil").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Mexico City").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("01000").country("Mexico").state("CDMX").address("200 Paseo de la Reforma, 01000 Mexico City, Mexico").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("São Paulo").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("01000-000").country("Brazil").state("São Paulo").address("350 Avenida Paulista, 01000-000 São Paulo, Brazil").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Berlin").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("10115").country("Germany").state("Berlin").address("150 Unter den Linden, 10115 Berlin, Germany").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Cairo").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("11511").country("Egypt").state("Cairo").address("30 Tahrir Square, 11511 Cairo, Egypt").build(),
                HotelLocation.builder().id(uuidProvider.generateUuid()).city("Buenos Aires").googleMapsUrl("https://goo.gl/maps/xyz").postalCode("C1000").country("Argentina").state("Buenos Aires").address("200 Avenida 9 de Julio, C1000 Buenos Aires, Argentina").build()
        );
    }


    private List<String> getHotelDescriptions() {
        return List.of(
                "A sumptuous hotel with spacious rooms, high-quality services, and stunning city views. Perfect for travelers seeking an unforgettable five-star experience.",
                "Located by the beach, this hotel offers oceanview rooms, an outdoor pool, and water activities. Ideal for a relaxing vacation by the water.",
                "A modern hotel in the city center, featuring upscale rooms and luxurious facilities. A spectacular view of skyscrapers, perfect for both business and leisure stays.",
                "A peaceful retreat for discerning travelers, offering refined suites and personalized service. Located in a quiet area, it’s the ideal place for a luxury getaway.",
                "The hotel offers a refined stay with modern amenities and impeccable service. A prestigious setting for events or dream vacations.",
                "A resort with private villas by the sea, sandy beaches, and a range of water activities. Perfect for a rejuvenating stay in an exceptional natural setting.",
                "A tranquil lodge in the mountains offering stunning sunset views. A perfect escape for hikers and those seeking peace and calm.",
                "A luxurious hotel inspired by historical palaces, with lavish rooms, world-class spas, and fine dining. A royal destination for an exceptional stay.",
                "Stylish, contemporary suites with cutting-edge amenities. Ideal for business travelers or those seeking comfort and elegance.",
                "Nestled in the mountains, this luxury resort offers outdoor activities like skiing, hiking, and mountain biking, with rooms offering breathtaking views of snow-capped peaks.",
                "A renovated historic hotel offering elegant rooms and a warm service. Perfect for travelers who want to explore the city while enjoying an authentic atmosphere.",
                "Situated on an island, this hotel offers rooms with lagoon views, a spa, and boat excursions. Ideal for idyllic tropical vacations surrounded by nature.",
                "A cozy lodge in the heart of the mountains, perfect for nature lovers and winter sports enthusiasts. The ideal place to relax after a day in the snow.",
                "A modern hotel with stylish rooms and refined service in the center of a bustling city. Perfect for those who want to explore the city with comfort and elegance.",
                "A luxury resort offering private villas, wellness centers, and gourmet restaurants. Ideal for an exclusive and refined holiday experience.",
                "A luxury hotel by the harbor, offering personalized service and a variety of water activities. A perfect escape for a tropical retreat.",
                "A boutique hotel with delicate, romantic décor. Perfect for a getaway with your loved one, offering intimate service and cozy rooms.",
                "Offering breathtaking views of the sky and mountains, this upscale hotel immerses guests in a celestial ambiance, perfect for a peaceful and refined stay.",
                "A secluded resort surrounded by nature, perfect for stargazing at night and relaxing in a peaceful, quiet environment.",
                "Situated in the Alps, this lodge offers comfort and nature with cozy rooms and exciting outdoor activities. A perfect retreat after a day in the snow.",
                "A tranquil hotel in an isolated valley, ideal for those seeking peace and relaxation in the heart of nature, away from city noise.",
                "With stunning sunset views, this five-star resort offers modern rooms and high-end activities for a memorable holiday by the sea.",
                "A peaceful haven with bright rooms and a calming natural setting. Perfect for those seeking a quiet retreat away from the hustle and bustle.",
                "An idyllic beachfront hotel offering comfortable rooms, a spa, and a private beach. Ideal for unwinding to the sound of waves and enjoying the sun.",
                "Nestled between dunes and the sea, this hotel offers elegant rooms with ocean views. The perfect place to relax while admiring breathtaking landscapes.",
                "A boutique hotel with a romantic atmosphere, featuring elegant rooms and personalized services. Perfect for a quiet and intimate getaway.",
                "A towering luxury resort offering unparalleled views and modern amenities. A perfect choice for those seeking sophistication and comfort in the sky.",
                "A calm retreat in a lush oasis, this hotel offers serene gardens and quiet rooms for relaxation. Ideal for those who want to recharge in a tranquil environment.",
                "This hotel promises a dreamlike stay with luxurious rooms and first-class service. A perfect place to indulge in comfort and serenity.",
                "Set amidst beautiful gardens, this resort combines opulence and nature. A perfect place for luxury lovers and nature enthusiasts alike.",
                "A sophisticated hotel with a pearl-white interior, featuring elegant rooms and fine dining. Perfect for those seeking elegance and peace.",
                "A hidden gem offering privacy and tranquility in a lush environment. Perfect for those who seek a unique and serene escape.",
                "A luxurious lodge with exceptional service and serene surroundings. Ideal for a lavish getaway in a peaceful setting.",
                "A luxurious beachfront hotel with elegant rooms, fine dining, and activities. Perfect for those looking for a refined seaside vacation.",
                "A desert oasis offering chic rooms and exceptional service, with activities like camel rides and stargazing. Ideal for a mystical escape.",
                "A charming inn surrounded by a secret garden, offering cozy rooms and a peaceful environment. Perfect for a quiet escape in nature.",
                "This hotel offers stunning panoramic views of the surrounding landscape, with luxurious rooms and top-notch amenities for a spectacular stay.",
                "A serene resort by the ocean, offering blue-hued decor, private beaches, and relaxing activities. A peaceful haven for those seeking coastal bliss.",
                "A lodge featuring warm amber tones, cozy rooms, and a welcoming atmosphere. Perfect for a charming getaway surrounded by nature.",
                "This wellness hotel offers luxurious spa services, relaxation zones, and peaceful surroundings for a truly rejuvenating experience.",
                "Nestled in the hills, this resort offers a magical experience with enchanting views, luxurious amenities, and activities to rejuvenate body and soul.",
                "Located by the sea, this inn offers a peaceful retreat with ocean views and calming sea breezes. Ideal for a relaxing coastal getaway.",
                "A majestic hotel offering luxurious rooms, fine dining, and a regal atmosphere. Perfect for those seeking a royal treatment during their stay.",
                "Offering a regal beach experience with elegant rooms, a private beach, and a range of luxury activities. Ideal for those who enjoy luxury by the sea.",
                "A secluded lodge set in lush surroundings, offering luxury amenities and activities like hiking, yoga, and wellness treatments. Perfect for nature lovers.",
                "A refined hotel with an ivory-white interior, offering sophisticated rooms, fine dining, and impeccable service in a peaceful setting.",
                "A high-end resort at the mountain summit, offering panoramic views, skiing, hiking, and ultimate luxury for the discerning traveler.",
                "A peaceful inn offering tranquil mornings with sunrise views, perfect for a quiet, rejuvenating stay in nature.",
                "A luxury hotel with rooms on the top floors of a skyscraper, offering breathtaking skyline views, ideal for guests seeking elegance and exclusivity.",
                "Located on a sapphire-blue coast, this resort offers luxury accommodations, private beaches, and relaxation in a pristine coastal environment."
        );
    }

    private List<String> getHotelNames() {
        return List.of(
                "GrandRoyalHotel", "OceanviewResort", "SkyLuxeHotel", "EliteHorizonInn", "PrestigeStayHotel",
                "CrystalBayResort", "SunsetHavenLodge", "EmeraldPalaceHotel", "SapphireSuitesHotel", "DiamondPeakResort",
                "GoldenCrestInn", "BlueLagoonHotel", "MountainGloryLodge", "UrbanEleganceHotel", "PlatinumStayResort",
                "ParadiseHarborHotel", "VelvetRoseInn", "CelestialViewHotel", "StarrySkyResort", "AlpineRetreatLodge",
                "SereneValleyHotel", "MajesticSunsetResort", "TranquilHorizonInn", "WhisperingWavesHotel",
                "AzureDreamsLodge", "SilverMoonHotel", "InfinityTowerResort", "BlissfulOasisInn", "CloudNineHotel",
                "RoyalOrchidResort", "IvoryPearlHotel", "HiddenGemResort", "SupremeHavenLodge", "OpulentShoresHotel",
                "MysticDunesResort", "SecretGardenInn", "HorizonVistaHotel", "EternalBlueResort", "AmberGlowLodge",
                "SerenitySpringsHotel", "EnchantedHillsResort", "OceanBreezeInn", "CelestiaPalaceHotel",
                "RegalWaveResort", "LushRetreatLodge", "IvoryCrestHotel", "EliteSummitResort", "RadiantDawnInn",
                "ZenithSkylineHotel", "SapphireCoastResort"
        );
    }

    private void generateUsers() {
        List<String> usernames = getUsernamesList();
        List<String> emails = getEmailsList();
        List<String> profileImages = getProfileImagesList();

        Random random = new Random();

        for (int i = 0; i < 50; i++) {
            String username = usernames.get(i);
            String email = emails.get(i);
            String profileImage = profileImages.get(i);
            boolean isValidEmail = random.nextBoolean();
            UserRole role = (i < 45) ? UserRole.USER : UserRole.ADMIN;

            User user = User.builder()
                    .id(uuidProvider.generateUuid())
                    .password(passwordEncoder.encode(USER_PASSWORD))
                    .username(username)
                    .role(role)
                    .profileImageUrl(profileImage)
                    .isValidEmail(isValidEmail)
                    .email(email)
                    .build();

            testDao.saveUser(user);
        }
    }

    private List<String> getProfileImagesList() {
        return List.of(
                "https://i.ibb.co/Vbqcr85/adele.jpg", "https://i.ibb.co/LDc3yYzy/amir.jpg", "https://i.ibb.co/FkQYJzQF/amywinehouse.jpg",
                "https://i.ibb.co/KjF7kF38/angele.jpg", "https://i.ibb.co/M56zN4bC/arianagrande.jpg", "https://i.ibb.co/6chwJvjF/barbara.jpg",
                "https://i.ibb.co/7tNtGYBz/benjaminbiolet.jpg", "https://i.ibb.co/d00pCr3F/billieeilish.jpg", "https://i.ibb.co/gb0Jjdv1/brunomars.jpg",
                "https://i.ibb.co/nNbTdrHR/byonce.jpg", "https://i.ibb.co/9H25GgVv/celinedion.jpg", "https://i.ibb.co/p6HTHwn3/charlesaznavour.jpg",
                "https://i.ibb.co/V0J8wbHZ/claraluciani.jpg", "https://i.ibb.co/N635Ckww/dualipa.jpg", "https://i.ibb.co/BHSwXN0n/edithpiaf.jpg",
                "https://i.ibb.co/gZTx8P9p/edsheeran.jpg", "https://i.ibb.co/mFR14LgR/eltonjohn.jpg", "https://i.ibb.co/ZRqbcPkT/elvispresley.jpg",
                "https://i.ibb.co/cK8GZ7pM/florentpagny.jpg", "https://i.ibb.co/nKMWvMm/franciscabrel.jpg", "https://i.ibb.co/Wvk4XrBf/hoshi.jpg",
                "https://i.ibb.co/Ldr5f8Kw/indila.jpg", "https://i.ibb.co/Jj43RDm8/jacquesbrel.jpg", "https://i.ibb.co/bgPR9hcy/jeanjacquesgoldman.jpg",
                "https://i.ibb.co/Ps7gxSjQ/harystyles.jpg", "https://i.ibb.co/JSKhHrB/jessie-murph.jpg", "https://i.ibb.co/C3y8wNqr/jhonnyhalliday.jpg",
                "https://i.ibb.co/939JjwGP/juliendore.jpg", "https://i.ibb.co/qLr9P7nT/justinbieber.jpg", "https://i.ibb.co/rKFjWqNq/justintimberlake.jpg",
                "https://i.ibb.co/prK0xfXv/kendjigirac.jpg", "https://i.ibb.co/v6kgXN0T/ladygaga.jpg", "https://i.ibb.co/Z6kc0sdQ/louane.jpg",
                "https://i.ibb.co/ycTP72jc/mickaeljackson.jpg", "https://i.ibb.co/WW8xkxS8/mylenefarmer.jpg", "https://i.ibb.co/zT6M7cj1/oliviarodrigo.jpg",
                "https://i.ibb.co/Z1tpVXfr/patrickbruel.jpg", "https://i.ibb.co/YBybWzgX/rihanna.jpg", "https://i.ibb.co/Q338VVph/robstewart.jpg",
                "https://i.ibb.co/gZPkgzYL/samsmith.jpg", "https://i.ibb.co/dJ7jfzPp/sergegainsbourg.jpg", "https://i.ibb.co/fdZRKXC5/shawnmendes.jpg",
                "https://i.ibb.co/7tM0vq1p/soprano.jpg", "https://i.ibb.co/qLry2FQz/stromae.jpg", "https://i.ibb.co/k6MHZFwp/tatemcrae.jpg",
                "https://i.ibb.co/5WNfMwsR/taylorswift.jpg", "https://i.ibb.co/XhHWrD1/theweekend.jpg", "https://i.ibb.co/21jb8mSL/vianney.jpg",
                "https://i.ibb.co/d4w34FJ2/yungblud.jpg", "https://i.ibb.co/Xr07MNDM/zazie.jpg"
        );
    }

    private List<String> getEmailsList() {
        return List.of(
                "john.smith@gmail.com", "emma.wilson@yahoo.com", "michael.brown@outlook.com",
                "sarah.jones@hotmail.com", "david.miller@protonmail.com", "lisa.taylor@icloud.com",
                "robert.anderson@mail.com", "jennifer.thomas@aol.com", "william.jackson@zoho.com",
                "olivia.white@fastmail.com", "james.harris@gmx.com", "sophia.martin@tutanota.com",
                "benjamin.garcia@yandex.com", "ava.martinez@inbox.com", "daniel.rodriguez@vivaldi.net",
                "mia.lopez@disroot.org", "alexander.lee@posteo.net", "charlotte.gonzalez@pm.me",
                "matthew.wilson@mailbox.org", "amelia.nelson@runbox.com", "ethan.baker@hey.com",
                "abigail.hill@startmail.com", "ryan.wright@ctemplar.com", "emily.mitchell@simplelogin.io",
                "nicholas.roberts@duck.com", "grace.phillips@tuta.io", "jonathan.campbell@skiff.com",
                "lily.parker@emailn.de", "samuel.evans@mailfence.com", "victoria.stewart@kolabnow.com",
                "zachary.sanchez@titanmail.cc", "natalie.morris@gigamail.com", "andrew.rogers@secmail.pro",
                "hannah.reed@mysecuremail.net", "joseph.cook@safeemail.xyz", "madison.morgan@privateemail.com",
                "christopher.bell@encryptedmail.io", "aubrey.murphy@secureinbox.net", "gabriel.bailey@messagelabs.com",
                "zoe.rivera@encryptedinbox.org", "kevin.cooper@safecommunication.com", "chloe.richardson@encmail.net",
                "tyler.cox@datasafemail.io", "leah.howard@privacyguard.mail", "brandon.ward@securemessage.org",
                "savannah.torres@confidentialmail.com", "justin.peterson@lockmail.net", "anna.gray@privatecorrespondence.com",
                "lucas.ramirez@secureconnect.mail", "stella.watson@guardian-mail.com"
        );
    }

    private List<String> getUsernamesList() {
        return List.of(
                "jsmith42", "emmaw", "mbrown7", "sjones21", "dmiller9", "lisat", "randerson", "jthomas",
                "wjackson", "oliviaw", "jharris4", "sophiam", "bengarcia", "avamart", "drodrig", "mialopez", "alexlee",
                "chargonz", "mattwil", "amelian", "ethanbkr", "abbyhill", "rwright", "emitch", "nickrob", "gracep",
                "jcamp", "lilyp", "samevans", "vickys", "zsanchez", "nataliem", "arogers", "hreed", "jcook3",
                "madimorg", "chrisb", "aubreym", "gabebail", "zoeriv", "kcooper", "chloer", "tylercox", "leahh",
                "bward23", "savannat", "jpete11", "annag", "lucasr", "stellaw"
        );
    }

}
