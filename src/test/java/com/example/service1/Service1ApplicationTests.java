package com.example.service1;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Service1ApplicationTests {

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("DATA_DIRECTORY", "data");
        System.setProperty("SERVICE2_URL", "data");

    }

    @Test
    void contextLoads() {
    }

}
