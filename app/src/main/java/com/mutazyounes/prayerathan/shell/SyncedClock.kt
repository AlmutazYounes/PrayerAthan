package com.mutazyounes.prayerathan.shell

import android.content.Context
import com.mutazyounes.prayerathan.engine.WallClock
import com.mutazyounes.prayerathan.engine.WallTime
import java.time.Instant

class SyncedClock(
    context: Context,
    private val query: () -> Long? = { NtpClient.queryCorrectionMs() },
) : WallClock {
    private val store = NtpTimeStore(context)

    @Volatile
    private var correctionMs: Long = store.correctionMs()

    override fun now(): Instant =
        WallTime.now(System.currentTimeMillis(), correctionMs)

    override fun alarmEpochMilli(civil: Instant): Long =
        WallTime.alarmEpochMilli(civil.toEpochMilli(), correctionMs)

    override fun syncIfDue(): Boolean {
        if (!store.isDue()) return false
        val next = query() ?: return false
        store.save(next)
        correctionMs = next
        return true
    }
}
