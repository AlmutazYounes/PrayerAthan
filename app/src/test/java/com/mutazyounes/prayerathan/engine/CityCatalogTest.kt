package com.mutazyounes.prayerathan.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CityCatalogTest {

    private val catalog = CityCatalog.fromTsv(
        countriesTsv = """
            US	United States
            JO	Jordan
            SA	Saudi Arabia
        """.trimIndent(),
        citiesTsv = """
            Albany	Albany	US	NY	42.6526	-73.7562	America/New_York	101228
            Albany	Albany	US	GA	31.5785	-84.1557	America/New_York	74843
            Amman	Amman	JO	16	31.9454	35.9284	Asia/Amman	1275857
            Makkah	Makkah	SA	14	21.4267	39.8261	Asia/Riyadh	1578722
        """.trimIndent(),
    )

    @Test
    fun albanyNyMatchesDefaultCoords() {
        val hit = catalog.match(42.6526, -73.7562, "Albany, NY")
        checkNotNull(hit)
        assertEquals("US", hit.first.code)
        assertEquals("Albany, NY", hit.second.label(hit.first.name))
        assertEquals("America/New_York", hit.second.timeZoneId)
    }

    @Test
    fun countrySearchFindsJordan() {
        val hits = catalog.searchCountries("jord")
        assertEquals("JO", hits.single().code)
    }

    @Test
    fun citySearchPrefersPrefixInsideCountry() {
        val hits = catalog.searchCities("US", "alba")
        assertEquals("NY", hits.first().admin1)
        assertTrue(hits.any { it.admin1 == "GA" })
    }

    @Test
    fun citySearchCapsResults() {
        val hits = catalog.searchCities("US", "al", limit = 1)
        assertEquals(1, hits.size)
        assertEquals("NY", hits.single().admin1)
    }

    @Test
    fun countrySearchCapsResults() {
        val hits = catalog.searchCountries("a", limit = 1)
        assertEquals(1, hits.size)
    }

    @Test
    fun emptyCountrySearchIsCapped() {
        val hits = catalog.searchCountries("", limit = 1)
        assertEquals(1, hits.size)
    }

    @Test
    fun nearestFindsAlbanyNy() {
        val hit = catalog.nearest(42.65, -73.76)
        checkNotNull(hit)
        assertEquals("Albany, NY", hit.second.label(hit.first.name))
    }

    @Test
    fun wallLabelNeverKeepsBareCoords() {
        assertEquals(
            "Albany, NY",
            catalog.wallLabel(42.6526, -73.7562, "42.6526, -73.7562"),
        )
    }

    @Test
    fun makkahLabelUsesCountryName() {
        val sa = catalog.country("SA")!!
        val makkah = catalog.searchCities("SA", "makkah").single()
        assertEquals("Makkah, Saudi Arabia", makkah.label(sa.name))
    }
}
