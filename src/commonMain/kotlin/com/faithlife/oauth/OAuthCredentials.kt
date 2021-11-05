package com.faithlife.oauth

/**
 * An OAuth 1.0 token secret pair
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc5849">RFC-5849</a>
 */
data class OAuthCredentials(val token: String, val secret: String)
