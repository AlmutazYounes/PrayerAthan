package com.mutazyounes.prayerathan.audio

import android.content.Context
import com.mutazyounes.prayerathan.R

enum class AthkarClip(
    val rawRes: Int,
    val caption: String,
) {
    SALAWAT(R.raw.athkar_salawat, "اللهم صل على محمد"),
}

class AthkarRotation(
    context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun next(): AthkarClip {
        val clips = AthkarClip.entries
        val index = prefs.getInt(KEY, 0).mod(clips.size)
        prefs.edit().putInt(KEY, (index + 1).mod(clips.size)).apply()
        return clips[index]
    }

    companion object {
        private const val PREFS = "athkar"
        private const val KEY = "clip_index"
    }
}
