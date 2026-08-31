package com.cloudorder.order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=ignored",
    "dynamodb.endpoint=ignored",
    "dynamodb.region=us-east-1",
    "inventory.service.url=http://localhost:8082"
})
class OrderServiceApplicationTests {

 //   @Test
    void contextLoads() {
    }

}
