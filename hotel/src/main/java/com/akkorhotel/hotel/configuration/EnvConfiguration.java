package com.akkorhotel.hotel.configuration;

import org.springframework.context.annotation.Configuration;

import static java.util.Objects.isNull;

@Configuration
public class EnvConfiguration {

    public static String getMongoUri() {
        return isNull(System.getenv("MONGO_URI")) ? "mongodb://localhost:27017" : System.getenv("MONGO_URI");
    }

    public static String getDatabaseName() {
        return isNull(System.getenv("DATABASE_NAME")) ? "akkorhotel_local" : System.getenv("DATABASE_NAME");
    }

    public static String getAllowedOrigins() {
        return isNull(System.getenv("ALLOWED_ORIGINS")) ? "*" : System.getenv("ALLOWED_ORIGINS");
    }

    public static String getAppEmail() {
        return isNull(System.getenv("APP_EMAIL")) ? "alice.test@gmail.com" : System.getenv("APP_EMAIL");
    }

    public static String getMailModifiedUsername() {
        return isNull(System.getenv("MAIL_MODIFIED_USERNAME")) ? "AkkorHotel" : System.getenv("MAIL_MODIFIED_USERNAME");
    }

    public static String getMailRegisterSubject() {
        return isNull(System.getenv("MAIL_REGISTER_SUBJECT")) ? "Confirmez votre inscription à AkkorHotel" : System.getenv("MAIL_REGISTER_SUBJECT");
    }

    public static String getMailRegisterConfirmationLink() {
        return isNull(System.getenv("MAIL_REGISTER_CONFIRMATION_LINK")) ? "https://www.example.com/link" : System.getenv("MAIL_REGISTER_CONFIRMATION_LINK");
    }

}
