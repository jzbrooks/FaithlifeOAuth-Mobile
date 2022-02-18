package com.faithlife.oauth

import assertk.assertThat
import assertk.assertions.hasToString
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test

class AuthorizationHeaderValueTest {

    private val testTimestamp = LocalDateTime(2021, 11, 5, 12, 0).toInstant(TimeZone.UTC)

    private val consumerCredentials = OAuthCredentials(
        "984A8D4CEDCB7BC3B61119B234FF6",
        "29D8AE818E6A4BB378785D989C64A"
    )

    private val userCredentials = OAuthCredentials(
        "5B282E3EC82C47A4B92F58112B1CD",
        "7F458AF16C37249B15B63D8FA9189"
    )

    @Test
    fun `correct consumer-only plaintext header is generated`() {
        val header = AuthorizationHeaderValue.Plaintext(consumerCredentials)
        assertThat(header).hasToString(
            """OAuth oauth_version="1.0",
                |oauth_signature_method="PLAINTEXT",
                |oauth_consumer_key="984A8D4CEDCB7BC3B61119B234FF6",
                |oauth_signature="29D8AE818E6A4BB378785D989C64A%26""""
                .trimMargin().replace("\n", "")
        )
    }

    @Test
    fun `correct plaintext header is generated`() {
        val header = AuthorizationHeaderValue.Plaintext(consumerCredentials, userCredentials)

        assertThat(header).hasToString(
            """OAuth oauth_version="1.0",
                |oauth_signature_method="PLAINTEXT",
                |oauth_consumer_key="984A8D4CEDCB7BC3B61119B234FF6",
                |oauth_signature="29D8AE818E6A4BB378785D989C64A%267F458AF16C37249B15B63D8FA9189",
                |oauth_token="5B282E3EC82C47A4B92F58112B1CD""""
                .trimMargin().replace("\n", "")
        )
    }

    @Test
    fun `equivalent plaintext headers are considered equal`() {
        val first = AuthorizationHeaderValue.Plaintext(consumerCredentials, userCredentials)
        val second = AuthorizationHeaderValue.Plaintext(consumerCredentials, userCredentials)

        assertThat(first).isEqualTo(second)
    }

    @Test
    fun `non-equivalent plaintext headers are considered equal`() {
        val first = AuthorizationHeaderValue.Plaintext(consumerCredentials, userCredentials)
        val second = AuthorizationHeaderValue.Plaintext(consumerCredentials)

        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun `plaintext raw value is equivalent to toString`() {
        val first = AuthorizationHeaderValue.Plaintext(consumerCredentials, userCredentials)

        assertThat(first::rawValue).isEqualTo(first.toString())
    }

    @Test
    fun `hmac raw value is equivalent to toString`() {
        val header = AuthorizationHeaderValue.HmacSha1(
            "GET",
            Uri.parse("https://faithlife.com/v1/user/current"),
            "TestNonce",
            testTimestamp,
            consumerCredentials,
            null,
        )

        assertThat(header::rawValue).isEqualTo(header.toString())
    }

    @Test
    fun `equivalent hmac headers are considered equal`() {
        val groupUrls = Uri.parse("https://faithlife.com/v1/groups")
        val first = buildTestUserHmacAuthorization(groupUrls)
        val second = buildTestUserHmacAuthorization(groupUrls)

        assertThat(first).isEqualTo(second)
    }

    @Test
    fun `non-equivalent hmac headers are considered equal`() {
        val first = buildTestUserHmacAuthorization(Uri.parse("https://faithlife.com/v1/groups"))
        val second = buildTestUserHmacAuthorization(Uri.parse("https://faithlife.com/"))

        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun `correct consumer-only hmac header is generated`() {
        val header = AuthorizationHeaderValue.HmacSha1(
            "GET",
            Uri.parse("https://faithlife.com/v1/user/current"),
            "TestNonce",
            testTimestamp,
            consumerCredentials,
            null,
        )

        assertThat(header).hasToString(
            """OAuth oauth_version="1.0",
                |oauth_signature_method="HMAC-SHA1",
                |oauth_consumer_key="984A8D4CEDCB7BC3B61119B234FF6",
                |oauth_nonce="TestNonce",
                |oauth_timestamp="${testTimestamp.epochSeconds}",
                |oauth_signature="Yb2Bejtw2%2BJ2%2FiY%2Buto4xUeos%2BI%3D""""
                .trimMargin().replace("\n", "")
        )
    }

    @Test
    fun `correct hmac header is generated`() {
        val header = buildTestUserHmacAuthorization(
            Uri.parse("https://faithlife.com/v1/user/current"),
        )

        assertThat(header).hasToString(
            """OAuth oauth_version="1.0",
                |oauth_signature_method="HMAC-SHA1",
                |oauth_consumer_key="984A8D4CEDCB7BC3B61119B234FF6",
                |oauth_nonce="TestNonce",
                |oauth_timestamp="${testTimestamp.epochSeconds}",
                |oauth_token="5B282E3EC82C47A4B92F58112B1CD",
                |oauth_signature="GTt8QoqluMTUMWVql2Uzifn9Y6E%3D""""
                .trimMargin().replace("\n", "")
        )
    }

    @Test
    fun `single query parameter produces correct hmac header`() {
        val header = buildTestUserHmacAuthorization(
            Uri.parse("https://faithlife.com/v1/conversations/15/messages?offset=20"),
        )

        assertThat(header).hasToString(
            """OAuth oauth_version="1.0",
                |oauth_signature_method="HMAC-SHA1",
                |oauth_consumer_key="984A8D4CEDCB7BC3B61119B234FF6",
                |oauth_nonce="TestNonce",
                |oauth_timestamp="${testTimestamp.epochSeconds}",
                |oauth_token="5B282E3EC82C47A4B92F58112B1CD",
                |oauth_signature="IkeNQ1RwYZrKoG1UDCqA30gaVzY%3D""""
                .trimMargin().replace("\n", "")
        )
    }

    @Test
    fun `multiple query parameters produces correct hmac header`() {
        val header = buildTestUserHmacAuthorization(
            Uri.parse(
                "https://faithlife.com/v1/" +
                    "conversations/15/messages?offset=20&pageSize=25&markRead=true"
            ),
        )

        assertThat(header).hasToString(
            """OAuth oauth_version="1.0",
                |oauth_signature_method="HMAC-SHA1",
                |oauth_consumer_key="984A8D4CEDCB7BC3B61119B234FF6",
                |oauth_nonce="TestNonce",
                |oauth_timestamp="${testTimestamp.epochSeconds}",
                |oauth_token="5B282E3EC82C47A4B92F58112B1CD",
                |oauth_signature="x97fUSPCFuhgns1NE3K1bdL6WW4%3D""""
                .trimMargin().replace("\n", "")
        )
    }

    @Test
    fun `bytes are not represented as signed in percent encoding`() {
        // The text reaction byte values all wrap to negative representations in signed bytes
        val header = buildTestUserHmacAuthorization(
            Uri.parse(
                "https://faithlife.com/v1/" +
                    "conversations/1/messages/15/reactions?textReaction=%F0%9F%8C%9F"
            ),
        )

        assertThat(header).hasToString(
            """OAuth oauth_version="1.0",
                |oauth_signature_method="HMAC-SHA1",
                |oauth_consumer_key="984A8D4CEDCB7BC3B61119B234FF6",
                |oauth_nonce="TestNonce",
                |oauth_timestamp="${testTimestamp.epochSeconds}",
                |oauth_token="5B282E3EC82C47A4B92F58112B1CD",
                |oauth_signature="CsRqEQfx87Zmj7b3yH0ekQd4VBA%3D""""
                .trimMargin().replace("\n", "")
        )
    }

    @Test
    fun `trailing path slashes produce correct header`() {
        val header = buildTestUserHmacAuthorization(
            Uri.parse("https://faithlife.com/v1/"),
        )

        assertThat(header).hasToString(
            """OAuth oauth_version="1.0",
                |oauth_signature_method="HMAC-SHA1",
                |oauth_consumer_key="984A8D4CEDCB7BC3B61119B234FF6",
                |oauth_nonce="TestNonce",
                |oauth_timestamp="${testTimestamp.epochSeconds}",
                |oauth_token="5B282E3EC82C47A4B92F58112B1CD",
                |oauth_signature="T2TOxaSHluUV3Ec48zi7tIt%2BP2o%3D""""
                .trimMargin().replace("\n", "")
        )
    }

    @Test
    fun `non-http schemes produce correct hmac header value`() {
        val header = buildTestUserHmacAuthorization(
            Uri.parse("wss://faithlife.com/v1"),
        )

        assertThat(header).hasToString(
            """OAuth oauth_version="1.0",
                |oauth_signature_method="HMAC-SHA1",
                |oauth_consumer_key="984A8D4CEDCB7BC3B61119B234FF6",
                |oauth_nonce="TestNonce",
                |oauth_timestamp="${testTimestamp.epochSeconds}",
                |oauth_token="5B282E3EC82C47A4B92F58112B1CD",
                |oauth_signature="C%2BVbxjv0Sa9YSL3UlNDo%2FPT74Lw%3D""""
                .trimMargin().replace("\n", "")
        )
    }

    @Test
    fun `boolean query parameter produces correct hmac header`() {
        val header = buildTestUserHmacAuthorization(
            Uri.parse("https://faithlife.com/v1/groups?includeUser"),
        )

        assertThat(header).hasToString(
            """OAuth oauth_version="1.0",
                |oauth_signature_method="HMAC-SHA1",
                |oauth_consumer_key="984A8D4CEDCB7BC3B61119B234FF6",
                |oauth_nonce="TestNonce",
                |oauth_timestamp="${testTimestamp.epochSeconds}",
                |oauth_token="5B282E3EC82C47A4B92F58112B1CD",
                |oauth_signature="WXpMN2kQeNMk1fIH8CtNpOdG%2FWk%3D""""
                .trimMargin().replace("\n", "")
        )
    }

    @Test
    fun `duplicate query parameter keys produce correct hmac header`() {
        val header = buildTestUserHmacAuthorization(
            Uri.parse("https://faithlife.com/v1/groups?test=second&test=first"),
        )

        assertThat(header).hasToString(
            """OAuth oauth_version="1.0",
                |oauth_signature_method="HMAC-SHA1",
                |oauth_consumer_key="984A8D4CEDCB7BC3B61119B234FF6",
                |oauth_nonce="TestNonce",
                |oauth_timestamp="${testTimestamp.epochSeconds}",
                |oauth_token="5B282E3EC82C47A4B92F58112B1CD",
                |oauth_signature="LLyTa21Xu%2F6HOb0xLfFW7vnR2NU%3D""""
                .trimMargin().replace("\n", "")
        )
    }

    private fun buildTestUserHmacAuthorization(uri: Uri): AuthorizationHeaderValue {
        return AuthorizationHeaderValue.HmacSha1(
            "GET",
            uri,
            "TestNonce",
            testTimestamp,
            consumerCredentials,
            userCredentials,
        )
    }
}
