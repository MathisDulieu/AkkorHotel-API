package com.akkorhotel.hotel.configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
@Profile("!test")
public class MongoConfiguration {

//    private static final String MONGO_URI = System.getenv("MONGO_URI");
    private static final String MONGO_URI = "mongodb://mongo:iXIDuPAsIPNPpSOkGzhuhRitkPcrIQBJ@autorack.proxy.rlwy.net:55986";

//    private static final String DATABASE_NAME = System.getenv("DATABASE_NAME");
    private static final String DATABASE_NAME = "hotel";

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(MONGO_URI);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient(), DATABASE_NAME));
    }

}