package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.ImageDao;
import com.akkorhotel.hotel.model.Image;
import com.akkorhotel.hotel.model.ImageCategory;
import com.akkorhotel.hotel.model.ImageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private ImageDao imageDao;

    @Mock
    private UuidProvider uuidProvider;

    @Test
    void shouldReturnUrl_whenImageExists() {
        // Arrange
        String imageId = "existingImageId";
        String expectedUrl = "https://example.com/image.jpg";

        Image image = Image.builder()
                .id(imageId)
                .url(expectedUrl)
                .name("imageName")
                .uploaderId("uploaderId")
                .extension(ImageExtension.jpg)
                .category(ImageCategory.USER)
                .build();

        when(imageDao.findById(imageId)).thenReturn(Optional.of(image));

        // Act
        String resultUrl = imageService.getImageUrlById(imageId);

        // Assert
        assertThat(resultUrl).isEqualTo(expectedUrl);

        // Verify interactions
        verify(imageDao).findById(imageId);
        verifyNoMoreInteractions(imageDao);
    }

    @Test
    void shouldReturnNull_whenImageDoesNotExist() {
        // Arrange
        String nonExistentImageId = "nonExistentImageId";

        when(imageDao.findById(nonExistentImageId)).thenReturn(Optional.empty());

        // Act
        String resultUrl = imageService.getImageUrlById(nonExistentImageId);

        // Assert
        assertThat(resultUrl).isNull();

        // Verify interactions
        verify(imageDao).findById(nonExistentImageId);
        verifyNoMoreInteractions(imageDao);
    }

    @Test
    void shouldReturnExtension_whenFilenameHasValidExtension() {
        // Arrange
        String filename = "image.png";

        // Act
        ImageExtension result = imageService.getImageExtension(filename);

        // Assert
        assertThat(result).isEqualTo(ImageExtension.png);
    }

    @Test
    void shouldReturnNull_whenFilenameIsNull() {
        // Arrange
        String filename = null;

        // Act
        ImageExtension result = imageService.getImageExtension(filename);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNull_whenFilenameHasNoDot() {
        // Arrange
        String filename = "image";

        // Act
        ImageExtension result = imageService.getImageExtension(filename);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNull_whenExtensionIsNotValid() {
        // Arrange
        String filename = "document.docx";

        // Act
        ImageExtension result = imageService.getImageExtension(filename);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void shouldSaveNewImage() {
        // Arrange
        ImageCategory category = ImageCategory.USER;
        String filename = "profile-photo.jpg";
        String url = "https://example.com/images/profile-photo.jpg";
        ImageExtension extension = ImageExtension.jpg;
        String userId = "user123";

        String generatedUuid = "generated-uuid-123";

        when(uuidProvider.generateUuid()).thenReturn(generatedUuid);

        // Act
        imageService.saveNewImage(category, filename, url, extension, userId);

        // Assert
        InOrder inOrder = inOrder(uuidProvider, imageDao);
        inOrder.verify(uuidProvider).generateUuid();
        inOrder.verify(imageDao).save(Image.builder()
                .id("generated-uuid-123")
                .category(ImageCategory.USER)
                .url("https://example.com/images/profile-photo.jpg")
                .name("profile-photo.jpg")
                .extension(ImageExtension.jpg)
                .uploaderId("user123")
                .build()
        );
        inOrder.verifyNoMoreInteractions();
    }

}