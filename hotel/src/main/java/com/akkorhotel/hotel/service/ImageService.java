package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.ImageDao;
import com.akkorhotel.hotel.model.Image;
import com.akkorhotel.hotel.model.ImageCategory;
import com.akkorhotel.hotel.model.ImageExtension;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageDao imageDao;
    private final UuidProvider uuidProvider;

    public String getImageUrlById(String imageId) {
        Optional<Image> optionalImage = imageDao.findById(imageId);
        return optionalImage.map(Image::getUrl).orElse(null);
    }

    public ImageExtension getImageExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf('.') + 1))
                .flatMap(ext -> {
                    try {
                        return Optional.of(ImageExtension.valueOf(ext));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                })
                .orElse(null);
    }

    public void saveNewImage(ImageCategory category, String filename, String url, ImageExtension extension, String userId) {
        Image image = Image.builder()
                .id(uuidProvider.generateUuid())
                .category(category)
                .name(filename)
                .url(url)
                .extension(extension)
                .uploaderId(userId)
                .build();

        imageDao.save(image);
    }

}
