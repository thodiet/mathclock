package com.mathclock

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.Date

@RunWith(AndroidJUnit4::class)
class TimeInWordsTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun test_0() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MINUTE, 0)
        val date: Date = cal.time

        val result = timeInWords(context, date)

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

            val result = timeInWords(context, date)

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

            val result = timeInWords(context, date)

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

            val result = timeInWords(context, date)

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

            val result = timeInWords(context, date)

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

            val result = timeInWords(context, date)

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

            val result = timeInWords(context, date)

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

@RunWith(AndroidJUnit4::class)
class TimeInWordsTest5 {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun test_0() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MINUTE, 0)
        val date: Date = cal.time

        val result = timeInWords(context, date, 5)

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
        val minutes = arrayOf(3, 4, 8, 9, 13, 14, 18, 19, 23, 24, 28, 29, 33, 34, 38, 39, 43, 44, 48, 49, 53, 54, 58, 59)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

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
        val minutes = arrayOf(1, 2, 6, 7, 11, 12, 16, 17, 21, 22, 26, 27, 31, 32, 36, 37, 41, 42, 46, 47, 51, 52, 56, 57)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

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
        val minutes = arrayOf(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

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
    fun test_Zwoelftel() {
        val cal = Calendar.getInstance()
        val minutes = (3..7)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

            assertTrue(
                "$it: Expected 'Zwölftel' in the output, but was: $result",
                result.contains("Zwölftel")
            )
            assertTrue(
                "$it: Expected no 'Uhr' in the output, but was: $result",
                !result.contains("Uhr")
            )
        }
    }

    @Test
    fun test_Sechstel() {
        val cal = Calendar.getInstance()
        val minutes = (8..12)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

            assertTrue(
                "$it: Expected 'Sechstel' in the output, but was: $result",
                result.contains("Sechstel")
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
        val minutes = (13..17)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

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
    fun test_Drittel() {
        val cal = Calendar.getInstance()
        val minutes = (18..22)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

            assertTrue(
                "$it: Expected 'Drittel' in the output, but was: $result",
                result.contains("Drittel")
            )
            assertTrue(
                "$it: Expected no 'Uhr' in the output, but was: $result",
                !result.contains("Uhr")
            )
        }
    }

    @Test
    fun test_FuenfZwoelftel() {
        val cal = Calendar.getInstance()
        val minutes = (23..27)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

            assertTrue(
                "$it: Expected 'Fünf Zwölftel' in the output, but was: $result",
                result.contains("Fünf Zwölftel")
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
        val minutes = (28..32)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

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
    fun test_SiebenZwoelftel() {
        val cal = Calendar.getInstance()
        val minutes = (33..37)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

            assertTrue(
                "$it: Expected 'Sieben Zwölftel' in the output, but was: $result",
                result.contains("Sieben Zwölftel")
            )
            assertTrue(
                "$it: Expected no 'Uhr' in the output, but was: $result",
                !result.contains("Uhr")
            )
        }
    }

    @Test
    fun test_ZweiDrittel() {
        val cal = Calendar.getInstance()
        val minutes = (38..42)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

            assertTrue(
                "$it: Expected 'Zwei Drittel' in the output, but was: $result",
                result.contains("Zwei Drittel")
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
        val minutes = (43..47)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

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

    @Test
    fun test_FuenfSechstel() {
        val cal = Calendar.getInstance()
        val minutes = (48..52)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

            assertTrue(
                "$it: Expected 'Fünf Sechstel' in the output, but was: $result",
                result.contains("Fünf Sechstel")
            )
            assertTrue(
                "$it: Expected no 'Uhr' in the output, but was: $result",
                !result.contains("Uhr")
            )
        }
    }

    @Test
    fun test_ElfZwoelftel() {
        val cal = Calendar.getInstance()
        val minutes = (53..57)

        minutes.forEach {
            cal.set(Calendar.MINUTE, it)
            val date: Date = cal.time

            val result = timeInWords(context, date, 5)

            assertTrue(
                "$it: Expected 'Elf Zwölftel' in the output, but was: $result",
                result.contains("Elf Zwölftel")
            )
            assertTrue(
                "$it: Expected no 'Uhr' in the output, but was: $result",
                !result.contains("Uhr")
            )
        }
    }
}
