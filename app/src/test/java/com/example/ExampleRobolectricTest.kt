package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.IntervalPresetEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Fast Alarm", appName)
    }

    @Test
    fun `preset entity duration calculation`() {
        val preset = IntervalPresetEntity(
            id = 1,
            label = "1.5h",
            durationMinutes = 90f,
            sortOrder = 2
        )
        val expectedMillis = (90 * 60 * 1000).toLong()
        val calculatedMillis = (preset.durationMinutes * 60 * 1000).toLong()
        assertEquals(expectedMillis, calculatedMillis)
    }
}
