package com.akkorhotel.hotel.configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import static com.akkorhotel.hotel.configuration.EnvConfiguration.getDatabaseName;
import static com.akkorhotel.hotel.configuration.EnvConfiguration.getMongoUri;

@Configuration
@Profile("!test")
public class MongoConfiguration {

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(getMongoUri());
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient(), getDatabaseName()));
    }

}