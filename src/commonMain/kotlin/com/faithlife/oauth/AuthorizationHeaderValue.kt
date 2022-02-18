package com.faithlife.oauth

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import okio.ByteString.Companion.toByteString

/**
 * An OAuth 1.0 authorization header value
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc5849">RFC-5849</a>
 */
sealed class AuthorizationHeaderValue {
    /** The string value of the authorization header */
    abstract val rawValue: String
    /** The string value of the authorization header */
    abstract override fun toString(): String
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int

    /** A plaintext OAuth 1.0 authorization header value */
    class Plaintext private constructor(
        override val rawValue: String,
    ) : AuthorizationHeaderValue() {
        /**
         * Creates the header value
         * @see [rawValue]
         * */
        constructor(consumer: OAuthCredentials, user: OAuthCredentials? = null) : this(
            formatOAuthProperties(
                createPlaintextParameters(
                    consumer.token,
                    consumer.secret,
                    user?.token,
                    user?.secret
                )
            )
        )

        override fun toString(): String = rawValue
        override fun hashCode(): Int = rawValue.hashCode()
        override fun equals(other: Any?): Boolean {
            return (other as? Plaintext)?.rawValue == rawValue
        }
    }

    /** An HMAC-SHA1 OAuth 1.0 authorization header value */
    class HmacSha1 private constructor(
        override val rawValue: String
    ) : AuthorizationHeaderValue() {

        /**
         * Creates the header value
         * @see [rawValue]
         */
        constructor(
            httpMethod: String,
            uri: Uri,
            nonce: String,
            consumer: OAuthCredentials,
            user: OAuthCredentials?,
        ) : this(httpMethod, uri, nonce, Clock.System.now(), consumer, user)

        internal constructor(
            httpMethod: String,
            uri: Uri,
            nonce: String,
            timestampInstant: Instant,
            consumer: OAuthCredentials,
            user: OAuthCredentials?,
        ) : this(createHmacHeaderValue(httpMethod, uri, nonce, timestampInstant, consumer, user))

        override fun toString(): String = rawValue
        override fun equals(other: Any?): Boolean {
            return (other as? HmacSha1)?.rawValue == rawValue
        }
        override fun hashCode(): Int = rawValue.hashCode()
    }

    private companion object {
        const val OAUTH_VERSION = "1.0"
        const val HEADER_PREFIX = "OAuth"
        const val PARAMETER_PREFIX = "oauth_"
        const val TOKEN = "oauth_token"
        const val CONSUMER_KEY = "oauth_consumer_key"
        const val SIGNATURE_METHOD = "oauth_signature_method"
        const val VERSION = "oauth_version"
        const val NONCE = "oauth_nonce"
        const val TIMESTAMP = "oauth_timestamp"
        const val SIGNATURE = "oauth_signature"
        const val PLAINTEXT_METHOD = "PLAINTEXT"
        const val HMAC_SHA1_METHOD = "HMAC-SHA1"

        val UNRESERVED_PUNCTUATION = setOf<Byte>(
            0x2D, // -
            0x2E, // .
            0x5F, // _
            0x73, // ~
        )

        val Byte.isUnreserved: Boolean
            get() {
                return this in 48..57 || // 0 - 9
                    this in 65..90 || // A - Z
                    this in 97..122 || // a - z
                    UNRESERVED_PUNCTUATION.contains(this)
            }

        fun createPlaintextParameters(
            consumerToken: String,
            consumerSecret: String,
            accessToken: String?,
            accessSecret: String?
        ): Map<String, List<String>> {
            val parameters = mutableMapOf(
                VERSION to listOf(OAUTH_VERSION),
                SIGNATURE_METHOD to listOf(PLAINTEXT_METHOD),
                CONSUMER_KEY to listOf(consumerToken),
                SIGNATURE to listOf(createPlaintextSignature(consumerSecret, accessSecret))
            )

            if (accessToken != null) {
                parameters[TOKEN] = listOf(accessToken)
            }

            return parameters
        }

        fun createPlaintextSignature(consumerSecret: String, accessSecret: String?) = buildString {
            append(consumerSecret)
            append('&')
            if (accessSecret != null) {
                append(accessSecret)
            }
        }

        private fun createHmacHeaderValue(
            httpMethod: String,
            uri: Uri,
            nonce: String,
            timestampInstant: Instant,
            consumer: OAuthCredentials,
            user: OAuthCredentials?,
        ): String {
            val parameters = createHmacSha1Parameters(
                consumer.token,
                user?.token,
                nonce,
                timestampInstant.epochSeconds.toString(),
            )

            val signature = createHmacSha1Signature(
                uri,
                httpMethod,
                consumer.secret,
                user?.secret,
                parameters
            )

            val properties = parameters.toMutableMap()
            properties[SIGNATURE] = listOf(signature)

            return formatOAuthProperties(properties)
        }

        private fun createHmacSha1Parameters(
            consumerToken: String,
            accessToken: String?,
            nonce: String,
            timestamp: String,
        ): Map<String, List<String>> {
            val parameters = mutableMapOf(
                VERSION to listOf(OAUTH_VERSION),
                SIGNATURE_METHOD to listOf(HMAC_SHA1_METHOD),
                CONSUMER_KEY to listOf(consumerToken),
                NONCE to listOf(nonce),
                TIMESTAMP to listOf(timestamp)
            )

            if (accessToken != null) {
                parameters[TOKEN] = listOf(accessToken)
            }

            return parameters
        }

        fun createHmacSha1Signature(
            uri: Uri,
            httpMethod: String,
            consumerSecret: String,
            accessSecret: String?,
            parameters: Map<String, List<String>>,
        ): String {
            val allParameters = parameters.toMutableMap()

            val queryParameters = uri.query?.split("&") ?: emptyList()

            for (queryParameter in queryParameters) {
                val query = queryParameter.split("=")
                val key = query[0]
                val value = query.getOrNull(1)

                when {
                    value != null && !allParameters.containsKey(key) ->
                        allParameters[key] = mutableListOf(value)

                    value != null && allParameters.containsKey(key) ->
                        (allParameters[key] as MutableList<String>).add(value)

                    else -> allParameters[key] = emptyList()
                }
            }

            val normalizedParameters = allParameters.asSequence()
                .sortedBy(Map.Entry<String, List<String>>::key)
                .joinToString(separator = "&") { (key, value) ->
                    value.asSequence()
                        .map(::percentEncode)
                        .sorted()
                        .joinToString(separator = "&") { paramValue -> "$key=$paramValue" }
                }

            val normalizedUriBase = "${uri.scheme}://${uri.authority}${uri.path}"
            val signatureInput = httpMethod.uppercase() +
                "&${percentEncode(normalizedUriBase)}" +
                "&${percentEncode(normalizedParameters)}"

            val plaintextSignature = createPlaintextSignature(consumerSecret, accessSecret)

            val data = signatureInput.encodeToByteArray().toByteString()
            val key = plaintextSignature.encodeToByteArray().toByteString()

            return data.hmacSha1(key).base64()
        }

        fun formatOAuthProperties(properties: Map<String, List<String>>): String {
            val formattedProperties = properties.asSequence()
                .filter { it.key.startsWith(PARAMETER_PREFIX) }
                .joinToString(separator = ",") { (key, value) ->
                    "$key=\"${percentEncode(value.single())}\""
                }

            return "$HEADER_PREFIX $formattedProperties"
        }

        /**
         * This percent encoding implementation is slightly
         * different from standard form encoding.
         *
         * See [RFC 5849](https://tools.ietf.org/html/rfc5849)
         */
        fun percentEncode(value: String): String {
            val bytes = value.encodeToByteArray()

            return buildString(bytes.size * 2) {
                // Iterating UTF-8 bytes works here because the encoding scheme
                // encodes multi-byte characters as individual bytes
                for (byte in bytes) {
                    if (byte.isUnreserved) {
                        append(byte.toInt().toChar())
                    } else {
                        // e.g. %3A
                        append('%')

                        val unsigned = byte.toUByte()
                        if (unsigned < 16u) {
                            append('0')
                        }

                        append(unsigned.toString(16).uppercase())
                    }
                }
            }
        }
    }
}
