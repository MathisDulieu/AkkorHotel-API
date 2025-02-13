package com.akkorhotel.hotel;

import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

public class AbstractContainerBaseTest {

    static final ComposeContainer container;

    static {
        container = new ComposeContainer(new File("src/test/resources/compose-test.yml"))
                .withExposedService("mongo-1", 27017, Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(3)));
        container.start();
    }
}
