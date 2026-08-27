package com.mutazyounes.prayerathan.engine

import java.text.Normalizer
import java.util.Locale

data class PlaceCountry(
    val code: String,
    val name: String,
)

data class PlaceCity(
    val name: String,
    val ascii: String,
    val countryCode: String,
    val admin1: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val population: Int,
) {
    fun label(countryName: String): String {
        return if (countryCode in REGION_ADMIN_LABELS && admin1.isNotBlank()) {
            "$name, $admin1"
        } else {
            "$name, $countryName"
        }
    }

    fun rowText(): String {
        return if (countryCode in REGION_ADMIN_LABELS && admin1.isNotBlank()) {
            "$name  ·  $admin1"
        } else {
            name
        }
    }

    companion object {
        private val REGION_ADMIN_LABELS = setOf("US", "CA", "AU")
    }
}

class CityCatalog(
    val countries: List<PlaceCountry>,
    private val cities: List<PlaceCity>,
) {
    private val countryByCode: Map<String, PlaceCountry> =
        countries.associateBy { it.code }

    private val citiesByCountry: Map<String, List<PlaceCity>> =
        cities.groupBy { it.countryCode }

    fun country(code: String): PlaceCountry? = countryByCode[code]

    fun searchCountries(query: String): List<PlaceCountry> {
        val q = fold(query)
        if (q.isEmpty()) return countries
        return countries.filter { country ->
            fold(country.name).contains(q) || country.code.lowercase(Locale.ROOT).contains(q)
        }
    }

    fun searchCities(countryCode: String, query: String, limit: Int = 80): List<PlaceCity> {
        val pool = citiesByCountry[countryCode].orEmpty()
        val q = fold(query)
        if (q.isEmpty()) return pool.take(limit)
        val starts = ArrayList<PlaceCity>()
        val contains = ArrayList<PlaceCity>()
        for (city in pool) {
            val name = fold(city.name)
            val ascii = fold(city.ascii)
            when {
                name.startsWith(q) || ascii.startsWith(q) -> starts.add(city)
                name.contains(q) || ascii.contains(q) -> contains.add(city)
            }
        }
        return (starts + contains).take(limit)
    }

    fun match(latitude: Double?, longitude: Double?, label: String): Pair<PlaceCountry, PlaceCity>? {
        if (latitude != null && longitude != null) {
            var best: PlaceCity? = null
            var bestD = MATCH_DEG * MATCH_DEG
            for (city in cities) {
                val dlat = city.latitude - latitude
                val dlon = city.longitude - longitude
                val d = dlat * dlat + dlon * dlon
                if (d <= bestD) {
                    bestD = d
                    best = city
                }
            }
            val city = best
            val country = city?.let { countryByCode[it.countryCode] }
            if (city != null && country != null) return country to city
        }
        val wanted = fold(label)
        if (wanted.isEmpty()) return null
        for (city in cities) {
            val country = countryByCode[city.countryCode] ?: continue
            if (fold(city.label(country.name)) == wanted) return country to city
        }
        return null
    }

    companion object {
        private const val MATCH_DEG = 0.15

        @Volatile
        private var bundled: CityCatalog? = null

        fun bundled(countriesTsv: String, citiesTsv: String): CityCatalog {
            bundled?.let { return it }
            synchronized(this) {
                bundled?.let { return it }
                return fromTsv(countriesTsv, citiesTsv).also { bundled = it }
            }
        }

        fun fromTsv(countriesTsv: String, citiesTsv: String): CityCatalog {
            val countries = countriesTsv.lineSequence()
                .map { it.trimEnd() }
                .filter { it.isNotEmpty() }
                .mapNotNull { line ->
                    val cols = line.split('\t')
                    if (cols.size < 2) return@mapNotNull null
                    PlaceCountry(code = cols[0], name = cols[1])
                }
                .sortedBy { it.name.lowercase(Locale.ROOT) }
                .toList()

            val cities = citiesTsv.lineSequence()
                .map { it.trimEnd() }
                .filter { it.isNotEmpty() }
                .mapNotNull { line -> parseCity(line) }
                .sortedWith(compareBy<PlaceCity> { it.countryCode }.thenByDescending { it.population })
                .toList()

            return CityCatalog(countries, cities)
        }

        private fun parseCity(line: String): PlaceCity? {
            val cols = line.split('\t')
            if (cols.size < 8) return null
            val lat = cols[4].toDoubleOrNull() ?: return null
            val lon = cols[5].toDoubleOrNull() ?: return null
            val pop = cols[7].toIntOrNull() ?: 0
            if (cols[0].isBlank() || cols[2].isBlank() || cols[6].isBlank()) return null
            return PlaceCity(
                name = cols[0],
                ascii = cols[1].ifBlank { cols[0] },
                countryCode = cols[2],
                admin1 = cols[3],
                latitude = lat,
                longitude = lon,
                timeZoneId = cols[6],
                population = pop,
            )
        }

        fun fold(value: String): String {
            val normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            return normalized.replace("\\p{M}+".toRegex(), "").lowercase(Locale.ROOT)
        }
    }
}
