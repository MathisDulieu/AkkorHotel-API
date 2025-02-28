package com.akkorhotel.hotel.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "akkorhotel.properties")
public class EnvConfiguration {
    private String mongoUri;
    private String databaseName;
    private String allowedOrigins;
    private String mailModifiedUsername;
    private String mailRegisterSubject;
    private String mailRegisterConfirmationLink;
    private String cloudinaryApiKey;
    private String cloudinaryCloudName;
    private String cloudinaryApiSecret;
    private String defaultUserProfileImage;
    private String appEmail;

    public Map<String, String> getCloudinaryConfig() {
        return Map.of(
                "cloud_name", cloudinaryCloudName,
                "api_key", cloudinaryApiKey,
                "api_secret", cloudinaryApiSecret
        );
    }
}
