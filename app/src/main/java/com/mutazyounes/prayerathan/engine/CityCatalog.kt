package com.mutazyounes.prayerathan.engine

import java.text.Normalizer
import java.util.Locale

data class PlaceCountry(
    val code: String,
    val name: String,
    val nameKey: String = CityCatalog.fold(name),
    val codeKey: String = code.lowercase(Locale.ROOT),
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
    val nameKey: String = CityCatalog.fold(name),
    val asciiKey: String = CityCatalog.fold(ascii),
    val labelKey: String = "",
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

    private val cityByLabel: Map<String, PlaceCity> =
        cities.associateBy { it.labelKey }.filterKeys { it.isNotEmpty() }

    private val prefixByCountry: Map<String, Map<String, List<PlaceCity>>> =
        citiesByCountry.mapValues { (_, pool) ->
            val buckets = HashMap<String, ArrayList<PlaceCity>>()
            for (city in pool) {
                bucket(city.nameKey, 2)?.let { buckets.getOrPut(it) { ArrayList() }.add(city) }
                val ascii = bucket(city.asciiKey, 2)
                if (ascii != null && ascii != bucket(city.nameKey, 2)) {
                    buckets.getOrPut(ascii) { ArrayList() }.add(city)
                }
            }
            buckets
        }

    private val geoCells: Map<Long, List<PlaceCity>> =
        cities.groupBy { cellKey(it.latitude, it.longitude) }

    fun country(code: String): PlaceCountry? = countryByCode[code]

    fun searchCountries(query: String, limit: Int = 80): List<PlaceCountry> {
        val q = fold(query)
        if (q.isEmpty()) return countries.take(limit)
        val out = ArrayList<PlaceCountry>(limit.coerceAtMost(countries.size))
        for (country in countries) {
            if (country.nameKey.contains(q) || country.codeKey.contains(q)) {
                out.add(country)
                if (out.size >= limit) break
            }
        }
        return out
    }

    fun searchCities(countryCode: String, query: String, limit: Int = 50): List<PlaceCity> {
        val q = fold(query)
        if (q.isEmpty()) return citiesByCountry[countryCode].orEmpty().take(limit)
        val pool = if (q.length >= 2) {
            prefixByCountry[countryCode]?.get(q.take(2)).orEmpty()
        } else {
            citiesByCountry[countryCode].orEmpty()
        }
        val starts = ArrayList<PlaceCity>(limit)
        val contains = ArrayList<PlaceCity>(limit)
        for (city in pool) {
            when {
                city.nameKey.startsWith(q) || city.asciiKey.startsWith(q) -> {
                    starts.add(city)
                    if (starts.size >= limit) break
                }
                contains.size < limit &&
                    (city.nameKey.contains(q) || city.asciiKey.contains(q)) -> contains.add(city)
            }
        }
        return if (starts.size >= limit) starts else (starts + contains).take(limit)
    }

    fun nearest(latitude: Double, longitude: Double): Pair<PlaceCountry, PlaceCity>? {
        var best: PlaceCity? = null
        var bestD = Double.POSITIVE_INFINITY
        fun consider(lat: Double, lon: Double) {
            val cell = geoCells[cellKey(lat, lon)] ?: return
            for (city in cell) {
                val d = dist2(city.latitude, city.longitude, latitude, longitude)
                if (d < bestD) {
                    bestD = d
                    best = city
                }
            }
        }
        for (dlat in -1..1) {
            for (dlon in -1..1) {
                consider(latitude + dlat, longitude + dlon)
            }
        }
        if (best == null) {
            for (radius in 2..8) {
                for (dlat in -radius..radius) {
                    for (dlon in -radius..radius) {
                        if (dlat != -radius && dlat != radius && dlon != -radius && dlon != radius) {
                            continue
                        }
                        consider(latitude + dlat, longitude + dlon)
                    }
                }
                if (best != null) break
            }
        }
        if (best == null) {
            for (city in cities) {
                val d = dist2(city.latitude, city.longitude, latitude, longitude)
                if (d < bestD) {
                    bestD = d
                    best = city
                }
            }
        }
        val city = best ?: return null
        val country = countryByCode[city.countryCode] ?: return null
        return country to city
    }

    fun wallLabel(latitude: Double, longitude: Double, storedLabel: String): String {
        val hit = match(latitude, longitude, storedLabel) ?: nearest(latitude, longitude)
        return if (hit != null) {
            hit.second.label(hit.first.name)
        } else {
            storedLabel.trim()
        }
    }

    fun match(latitude: Double?, longitude: Double?, label: String): Pair<PlaceCountry, PlaceCity>? {
        if (latitude != null && longitude != null) {
            val near = nearest(latitude, longitude)
            if (near != null) {
                if (dist2(near.second.latitude, near.second.longitude, latitude, longitude) <=
                    MATCH_DEG * MATCH_DEG
                ) {
                    return near
                }
            }
        }
        val wanted = fold(label)
        if (wanted.isEmpty()) return null
        val labeled = cityByLabel[wanted]
        if (labeled != null) {
            val country = countryByCode[labeled.countryCode]
            if (country != null) return country to labeled
        }
        return null
    }

    companion object {
        private const val MATCH_DEG = 0.15

        private fun dist2(alat: Double, alon: Double, blat: Double, blon: Double): Double {
            val dlat = alat - blat
            val dlon = alon - blon
            return dlat * dlat + dlon * dlon
        }

        private fun cellKey(latitude: Double, longitude: Double): Long {
            val lat = kotlin.math.floor(latitude).toInt()
            val lon = kotlin.math.floor(longitude).toInt()
            return (lat.toLong() shl 32) xor (lon.toLong() and 0xffffffffL)
        }

        private fun bucket(key: String, size: Int): String? {
            if (key.length < size) return null
            return key.take(size)
        }

        @Volatile
        private var bundled: CityCatalog? = null

        fun cached(): CityCatalog? = bundled

        fun loadBundled(readAsset: (String) -> String): CityCatalog {
            cached()?.let { return it }
            return bundled(readAsset("countries.tsv"), readAsset("cities.tsv"))
        }

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

            val countryNames = countries.associate { it.code to it.name }
            val cities = citiesTsv.lineSequence()
                .map { it.trimEnd() }
                .filter { it.isNotEmpty() }
                .mapNotNull { line -> parseCity(line, countryNames) }
                .sortedWith(compareBy<PlaceCity> { it.countryCode }.thenByDescending { it.population })
                .toList()

            return CityCatalog(countries, cities)
        }

        private fun parseCity(line: String, countryNames: Map<String, String>): PlaceCity? {
            val cols = line.split('\t')
            if (cols.size < 8) return null
            val lat = cols[4].toDoubleOrNull() ?: return null
            val lon = cols[5].toDoubleOrNull() ?: return null
            val pop = cols[7].toIntOrNull() ?: 0
            if (cols[0].isBlank() || cols[2].isBlank() || cols[6].isBlank()) return null
            val name = cols[0]
            val ascii = cols[1].ifBlank { name }
            val countryCode = cols[2]
            val admin1 = cols[3]
            val nameKey = fold(name)
            val asciiKey = fold(ascii)
            val labeled = PlaceCity(
                name = name,
                ascii = ascii,
                countryCode = countryCode,
                admin1 = admin1,
                latitude = lat,
                longitude = lon,
                timeZoneId = cols[6],
                population = pop,
                nameKey = nameKey,
                asciiKey = asciiKey,
            )
            val countryName = countryNames[countryCode] ?: countryCode
            return labeled.copy(labelKey = fold(labeled.label(countryName)))
        }

        private val COMBINING_MARKS = "\\p{M}+".toRegex()

        fun fold(value: String): String {
            val normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            return COMBINING_MARKS.replace(normalized, "").lowercase(Locale.ROOT)
        }
    }
}
