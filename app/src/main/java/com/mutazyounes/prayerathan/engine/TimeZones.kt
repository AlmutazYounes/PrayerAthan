package com.mutazyounes.prayerathan.engine

import java.time.ZoneId

object TimeZones {
    const val AMERICA_NEW_YORK: String = "America/New_York"
    const val ASIA_AMMAN: String = "Asia/Amman"

    val americaNewYork: ZoneId = ZoneId.of(AMERICA_NEW_YORK)
    val asiaAmman: ZoneId = ZoneId.of(ASIA_AMMAN)
}
