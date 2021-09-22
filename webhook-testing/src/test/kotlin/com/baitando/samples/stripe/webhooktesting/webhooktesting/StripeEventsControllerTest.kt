package com.baitando.samples.stripe.webhooktesting.webhooktesting

import com.stripe.net.Webhook
import com.stripe.net.Webhook.Util.computeHmacSha256
import com.stripe.net.Webhook.Util.getTimeNow
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.util.ResourceUtils

@WebMvcTest(controllers = [StripeEventsController::class])
@TestPropertySource(properties = ["sample.stripe.key: dummy"])
class StripeEventsControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Value("\${sample.stripe.key}") private val stripeKey: String
) {

    @Test
    fun `Sending event without signature and without body ends with unauthorized`() {
        mockMvc.post("/stripe-events") {
            contentType = APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `Sending event with any signature and without body ends with bad request`() {
        mockMvc.post("/stripe-events") {
            headers { header("Stripe-Signature", "any-value") }
            contentType = APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `Sending event with invalid signature and any body ends with forbidden`() {
        mockMvc.post("/stripe-events") {
            headers { header("Stripe-Signature", "any-value") }
            contentType = APPLICATION_JSON
            content = "{}"
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `Sending event with valid signature and any body ends with ok`() {
        val payload = ResourceUtils.getFile("classpath:charge-suceeded-event.json").readText()
        val validSignature = generateSigHeader(payload, stripeKey)

        mockMvc.post("/stripe-events") {
            headers { header("Stripe-Signature", validSignature) }
            contentType = APPLICATION_JSON
            content = payload
        }.andExpect {
            status { isOk() }
        }
    }

    private fun generateSigHeader(payload: String, key: String): String {
        // Inspired by https://github.com/stripe/stripe-java/blob/master/src/test/java/com/stripe/net/WebhookTest.java
        val timestamp = getTimeNow()
        val payloadToSign = String.format("%d.%s", timestamp, payload)
        val signature = computeHmacSha256(key, payloadToSign)

        return String.format("t=%d,%s=%s", timestamp, Webhook.Signature.EXPECTED_SCHEME, signature)
    }
}
