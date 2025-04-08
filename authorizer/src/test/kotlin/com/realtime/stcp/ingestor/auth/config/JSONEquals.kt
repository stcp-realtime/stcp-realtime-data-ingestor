package com.realtime.stcp.ingestor.auth.config

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.skyscreamer.jsonassert.JSONAssert

class JSONEquals(
    private val expected: String?,
    private val strict: Boolean = true,
) : TypeSafeMatcher<String>() {
    override fun describeTo(description: Description?) {
        description?.appendText("JSON has the same values")
    }

    override fun matchesSafely(item: String?): Boolean {
        JSONAssert.assertEquals(
            this.expected,
            item,
            this.strict,
        )
        return true
    }
}
