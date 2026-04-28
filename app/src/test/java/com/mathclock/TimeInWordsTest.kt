package com.mathclock

import java.util.Calendar
import java.util.Date
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeInWordsTest {
    @Test
    fun test_0() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MINUTE, 0)
        val date: Date = cal.time

        val result = timeInWords(date)

        assertTrue(
            "0: Expected 'Uhr' in the output, but was: $result",
            result.contains("Uhr")
        )
        assertTrue(
            "0: Expected no 'vor' in the output, but was: $result",
            !result.contains("vor")
        )
        assertTrue(
            "0: Expected no 'nach' in the output, but was: $result",
            !result.contains("nach")
        )
    }

    @Test
    fun test_vor() {
        val cal = Calendar.getInstance()
        val minutes = (8..14) + (23..29) + (38..44) + (53..59)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(date)

            assertTrue(
                "$it: Expected 'vor' in the output, but was: $result",
                result.contains("vor")
            )
            assertTrue(
                "$it: Expected no 'Uhr' in the output, but was: $result",
                !result.contains("Uhr")
            )
        }
    }

    @Test
    fun test_nach() {
        val cal = Calendar.getInstance()
        val minutes = (1..7) + (16..22) + (31..37) + (46..52)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(date)

            assertTrue(
                "$it: Expected 'nach' in the output, but was: $result",
                result.contains("nach")
            )
            assertTrue(
                "$it: Expected no 'Uhr' in the output, but was: $result",
                !result.contains("Uhr")
            )
        }
    }

    @Test
    fun testNeitherNor_vor_nach() {
        val cal = Calendar.getInstance()
        val minutes = arrayOf(15, 30, 45)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(date)

            assertTrue(
                "$it: Expected no 'vor' in the output, but was: $result",
                !result.contains("vor")
            )
            assertTrue(
                "$it: Expected no 'nach' in the output, but was: $result",
                !result.contains("nach")
            )
            assertTrue(
                "$it: Expected no 'Uhr' in the output, but was: $result",
                !result.contains("Uhr")
            )
        }
    }

    @Test
    fun test_Viertel() {
        val cal = Calendar.getInstance()
        val minutes = (8..22)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(date)

            assertTrue(
                "$it: Expected 'Viertel' in the output, but was: $result",
                result.contains("Viertel")
            )
            assertTrue(
                "$it: Expected no 'Uhr' in the output, but was: $result",
                !result.contains("Uhr")
            )
        }
    }

    @Test
    fun test_Halb() {
        val cal = Calendar.getInstance()
        val minutes = (23..37)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(date)

            assertTrue(
                "$it: Expected 'Halb' in the output, but was: $result",
                result.contains("Halb")
            )
            assertTrue(
                "$it: Expected no 'Uhr' in the output, but was: $result",
                !result.contains("Uhr")
            )
        }
    }

    @Test
    fun test_Dreiviertel() {
        val cal = Calendar.getInstance()
        val minutes = (38..52)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(date)

            assertTrue(
                "$it: Expected 'Dreiviertel' in the output, but was: $result",
                result.contains("Dreiviertel")
            )
            assertTrue(
                "$it: Expected no 'Uhr' in the output, but was: $result",
                !result.contains("Uhr")
            )
        }
    }
}
