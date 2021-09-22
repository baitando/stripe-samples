package com.baitando.samples.stripe.webhooktesting.webhooktesting

import com.stripe.exception.SignatureVerificationException
import com.stripe.net.Webhook
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class StripeEventsController(@Value("\${sample.stripe.key}") private val stripeKey: String) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostMapping("/stripe-events")
    fun processEvent(
        @RequestHeader(name = "Stripe-Signature", required = false) stripeSignature: String?,
        @RequestBody(required = false) payload: String?
    ) {
        logger.debug { "Processing incoming stripe event" }

        // Treat missing custom header as unauthorized (401) instead of simply using required true,
        // which would trigger bad request (400) in Spring MVC.
        if (stripeSignature == null) {
            logger.error { "Missing event signature" }
            throw ResponseStatusException(UNAUTHORIZED)
        }
        // Handle this explicitly here. Otherwise, missing signature and missing body would end
        // in bad request (400) instead of unauthorized (401), because Spring MVC would handle it
        // that way.
        if (payload == null) {
            logger.error { "Missing event payload" }
            throw ResponseStatusException(BAD_REQUEST)
        }

        val event = Webhook.constructEvent(payload, stripeSignature, stripeKey)
        logger.debug { "Successfully processed incoming stripe event { eventType = '${event.type}' }" }
    }

    @ExceptionHandler(SignatureVerificationException::class)
    fun handleSignatureVerificationException(): ResponseEntity<Void> {
        logger.error { "Event signature verification failed" }
        return ResponseEntity.status(FORBIDDEN).build()
    }
}
