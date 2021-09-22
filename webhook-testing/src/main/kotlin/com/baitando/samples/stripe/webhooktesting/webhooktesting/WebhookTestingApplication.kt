package com.baitando.samples.stripe.webhooktesting.webhooktesting

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebhookTestingApplication

fun main(args: Array<String>) {
    runApplication<WebhookTestingApplication>(*args)
}
