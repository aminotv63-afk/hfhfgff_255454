package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.EpisodeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
    assertEquals("ANIME BALL", appName)
  }

  @Test
  fun `verify episodes count and initial state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = EpisodeRepository(context)
    val episodes = repository.episodes.value
    assertEquals(20, episodes.size)

    val ep1 = repository.getEpisodeById(1)
    assertNotNull(ep1)
    assertEquals("https://www.4s.io/web/embed/file/EUc98OZWjq", ep1?.embedUrl)

    val ep20 = repository.getEpisodeById(20)
    assertNotNull(ep20)
    assertEquals("https://www.4s.io/web/embed/file/Tx1vr1j2ge", ep20?.embedUrl)
  }
}
