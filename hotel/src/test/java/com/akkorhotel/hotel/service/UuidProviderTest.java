package com.akkorhotel.hotel.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UuidProviderTest {

    private final UuidProvider uuidProvider = new UuidProvider();

    @Test
    void shouldGenerateUuid() {
        // Act
        String uuid = uuidProvider.generateUuid();

        // Assert
        assertThat(uuid).hasSize(36);
    }

}