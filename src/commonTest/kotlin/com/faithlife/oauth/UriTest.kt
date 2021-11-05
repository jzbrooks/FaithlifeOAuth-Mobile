package com.faithlife.oauth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Test

class UriTest {
    @Test
    fun `uri scheme is parsed`() {
        val uri = Uri.parse("https://faithlife.com")
        assertThat(uri, "uri").prop(Uri::scheme).isEqualTo("https")
    }

    @Test
    fun `uri authority is parsed`() {
        val uri = Uri.parse("https://faithlife.com")
        assertThat(uri, "uri").prop(Uri::authority).isEqualTo("faithlife.com")
    }

    @Test
    fun `uri path is parsed`() {
        val uri = Uri.parse("https://faithlife.com/groups")
        assertThat(uri, "uri").prop(Uri::path).isEqualTo("/groups")
    }

    @Test
    fun `uri path is parsed with trailing slash`() {
        val uri = Uri.parse("https://faithlife.com/groups/")
        assertThat(uri, "uri").prop(Uri::path).isEqualTo("/groups/")
    }

    @Test
    fun `uri query is parsed`() {
        val uri = Uri.parse("https://faithlife.com/?search=Community%20Church")
        assertThat(uri, "uri").prop(Uri::query).isEqualTo("search=Community%20Church")
    }
}
