package com.maximeze.browser

import com.maximeze.browser.data.model.SearchEngine
import com.maximeze.browser.data.model.buildUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchEngineTest {

    @Test
    fun `Google builds correct search URL`() {
        val url = SearchEngine.GOOGLE.buildUrl("hello world")
        assertTrue(url.startsWith("https://www.google.com/search?q="))
        assertTrue(url.contains("hello"))
    }

    @Test
    fun `DuckDuckGo builds correct search URL`() {
        val url = SearchEngine.DUCKDUCKGO.buildUrl("privacy")
        assertTrue(url.startsWith("https://duckduckgo.com/?q="))
        assertTrue(url.contains("privacy"))
    }

    @Test
    fun `Bing builds correct search URL`() {
        val url = SearchEngine.BING.buildUrl("test query")
        assertTrue(url.startsWith("https://www.bing.com/search?q="))
        assertTrue(url.contains("test"))
    }
}

class UrlClassificationTest {

    private fun isUrl(input: String): Boolean {
        if (input.startsWith("http://") || input.startsWith("https://")) return true
        if (input.startsWith("localhost")) return true
        return input.matches(Regex("^[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})(/\\S*)?$")) && !input.contains(" ")
    }

    @Test
    fun `http URL is recognized`() {
        assertTrue(isUrl("http://example.com"))
    }

    @Test
    fun `https URL is recognized`() {
        assertTrue(isUrl("https://github.com/user/repo"))
    }

    @Test
    fun `bare domain is recognized`() {
        assertTrue(isUrl("example.com"))
    }

    @Test
    fun `search query is not a URL`() {
        assertFalse(isUrl("hello world"))
        assertFalse(isUrl("what is kotlin"))
    }
}
