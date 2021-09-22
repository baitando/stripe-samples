package com.baitando.samples.stripe.webhooktesting.webhooktesting

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = ["sample.stripe.key: dummy"])
class WebhookTestingApplicationTests {

    @Test
    fun contextLoads() {
    }

}
