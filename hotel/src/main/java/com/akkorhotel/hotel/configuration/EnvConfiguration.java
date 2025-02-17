package com.akkorhotel.hotel.configuration;

import org.springframework.context.annotation.Configuration;

import static java.util.Objects.isNull;

@Configuration
public class EnvConfiguration {

    private static final String MONGO_URI = System.getenv("MONGO_URI");
    private static final String DATABASE_NAME = System.getenv("DATABASE_NAME");
    private static final String ALLOWED_ORIGINS = System.getenv("ALLOWED_ORIGINS");
    private static final String APP_EMAIL = System.getenv("APP_EMAIL");
    private static final String MAIL_MODIFIED_USERNAME = System.getenv("MAIL_MODIFIED_USERNAME");
    private static final String MAIL_REGISTER_SUBJECT = System.getenv("MAIL_REGISTER_SUBJECT");
    private static final String MAIL_REGISTER_CONFIRMATION_LINK = System.getenv("MAIL_REGISTER_CONFIRMATION_LINK");

    public static String getMongoUri() {
        return isNull(MONGO_URI) ? "mongodb://localhost:27017" : MONGO_URI;
    }

    public static String getDatabaseName() {
        return isNull(DATABASE_NAME) ? "akkorhotel_local" : DATABASE_NAME;
    }

    public static String getAllowedOrigins() {
        return isNull(ALLOWED_ORIGINS) ? "*" : ALLOWED_ORIGINS;
    }

    public static String getAppEmail() {
        return isNull(APP_EMAIL) ? "alice.test@gmail.com" : APP_EMAIL;
    }

    public static String getMailModifiedUsername() {
        return isNull(MAIL_MODIFIED_USERNAME) ? "AkkorHotel" : MAIL_MODIFIED_USERNAME;
    }

    public static String getMailRegisterSubject() {
        return isNull(MAIL_REGISTER_SUBJECT) ? "Confirmez votre inscription à AkkorHotel" : MAIL_REGISTER_SUBJECT;
    }

    public static String getMailRegisterConfirmationLink() {
        return isNull(MAIL_REGISTER_CONFIRMATION_LINK) ? "https://www.example.com/link" : MAIL_REGISTER_CONFIRMATION_LINK;
    }

}
