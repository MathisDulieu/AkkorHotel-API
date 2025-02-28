package com.akkorhotel.hotel.dao;

import com.akkorhotel.hotel.model.Image;
import com.akkorhotel.hotel.model.ImageCategory;
import com.akkorhotel.hotel.model.ImageExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class ImageDaoTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ImageDao imageDao;

    @AfterEach
    void clean() {
        mongoTemplate.dropCollection("IMAGES");
    }

    @Test
    void shouldSaveNewImage() {
        // Arrange
        Image image = Image.builder()
                .id("id")
                .url("url")
                .name("name")
                .uploaderId("uploaderId")
                .extension(ImageExtension.png)
                .category(ImageCategory.USER)
                .build();

        // Act
        imageDao.save(image);

        // Assert
        List<Map> savedImages = mongoTemplate.findAll(Map.class, "IMAGES");
        assertThat((Map<String, Object>) savedImages.getFirst())
                .containsExactlyInAnyOrderEntriesOf(ofEntries(
                        entry("_id", "id"),
                        entry("url", "url"),
                        entry("name", "name"),
                        entry("uploaderId", "uploaderId"),
                        entry("extension", "png"),
                        entry("category", "USER")
                ));
    }

    @Test
    void shouldReturnImage_whenIdExistsInDatabase() {
        // Arrange
        mongoTemplate.insert("""
        {
            "_id": "imageId",
            "url": "url",
            "name": "name",
            "uploaderId": "uploaderId",
            "extension": "jpg",
            "category": "USER"
        }
        """, "IMAGES");

        // Act
        Optional<Image> imageOptional = imageDao.findById("imageId");

        // Assert
        assertThat(imageOptional).isPresent();
        assertThat(imageOptional.get().getId()).isEqualTo("imageId");
        assertThat(imageOptional.get().getUrl()).isEqualTo("url");
        assertThat(imageOptional.get().getName()).isEqualTo("name");
        assertThat(imageOptional.get().getUploaderId()).isEqualTo("uploaderId");
        assertThat(imageOptional.get().getExtension()).isEqualTo(ImageExtension.jpg);
        assertThat(imageOptional.get().getCategory()).isEqualTo(ImageCategory.USER);
    }

    @Test
    void shouldReturnEmptyOptional_whenIdDoesNotExistInDatabase() {
        // Act
        Optional<Image> imageOptional = imageDao.findById("nonexistent_imageId");

        // Assert
        assertThat(imageOptional).isEmpty();
    }

}