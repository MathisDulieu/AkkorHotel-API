package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ImageDao {

    private final MongoTemplate mongoTemplate;

    private static final String IMAGE_COLLECTION = "IMAGES";

    public void save(Image image) {
        mongoTemplate.save(image, IMAGE_COLLECTION);
    }

    public Optional<Image> findById(String imageId) {
        return Optional.ofNullable(mongoTemplate.findById(imageId, Image.class, IMAGE_COLLECTION));
    }

}
