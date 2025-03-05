package com.akkorhotel.hotel.utils;

import com.akkorhotel.hotel.configuration.EnvConfiguration;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class ImageUtils {

    private final Cloudinary cloudinary;

    @Autowired
    public ImageUtils(EnvConfiguration envConfiguration) {
        this.cloudinary = new Cloudinary(envConfiguration.getCloudinaryConfig());
    }

    public String uploadImage(MultipartFile imageFile) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
        return (String) uploadResult.get("secure_url");
    }

}
