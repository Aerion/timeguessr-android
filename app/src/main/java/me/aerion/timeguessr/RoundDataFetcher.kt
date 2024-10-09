package me.aerion.timeguessr

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

data class RoundData(
    val URL: String,
    val Year: String,
    val Location: Location,
    val Description: String,
    val License: String,
    val Country: String
)

data class Location(
    val lat: Double,
    val lng: Double
)

interface RoundDataFetcher {
    fun fetchRounds(): List<RoundData>?
}

class NetworkRoundDataFetcher : RoundDataFetcher {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val roundDataAdapter = moshi.adapter(Array<RoundData>::class.java)

    override fun fetchRounds(): List<RoundData>? {
        val url = "https://timeguessr.com/getPlay"
        val requestBody = "{\"urlParameters\":null}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                response.body?.string()?.let { responseBody ->
                    roundDataAdapter.fromJson(responseBody)?.toList()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}

class StubbedRoundDataFetcher : RoundDataFetcher {
    override fun fetchRounds(): List<RoundData> {
        return listOf(
            RoundData(
                URL = "https://images.timeguessr.com/f58f4502-e13a-47e6-a7dd-f223951da34e.webp",
                Year = "2019",
                Location = Location(lat = 27.4722200350407, lng = 89.63841250287706),
                Description = "Two men carrying a painting of the King of Bhutan's family down a street in Thimphu, Bhutan.",
                License = "Keren Su/China Span / Alamy Stock Photo",
                Country = "Bhutan"
            ),
            RoundData(
                URL = "https://images.timeguessr.com/8483fd64-9f2f-4348-a7f5-7c5d6d9769ed.jpg",
                Year = "1938",
                Location = Location(lat = 51.41341015356205, lng = -0.68094228927702),
                Description = "Views at the 1938 Royal Ascot.",
                License = "Public domain, via Wikimedia Commons",
                Country = "UK"
            ),
            RoundData(
                URL = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/26/An_der_Frauenkirche_a_Webergasse_torkolata_felé_nézve._Fortepan_61138.jpg/2560px-An_der_Frauenkirche_a_Webergasse_torkolata_felé_nézve._Fortepan_61138.jpg",
                Year = "1971",
                Location = Location(lat = 51.05234028350409, lng = 13.741392102531325),
                Description = "A tour group in Dresden.",
                License = "FOTO:FORTEPAN / Lencse Zoltán, CC BY-SA 3.0, via Wikimedia Commons",
                Country = "Germany"
            ),
            RoundData(
                URL = "https://images.timeguessr.com/721247ff-2ee4-4a13-b031-983d1514572b.jpg",
                Year = "1998",
                Location = Location(lat = 48.871756934290524, lng = 2.301613984803676),
                Description = "French football fans celebrating the '98 World Cup victory.",
                License = "Public domain",
                Country = "France"
            ),
            RoundData(
                URL = "https://images.timeguessr.com/2d8f86b7-6fe5-4110-94cc-343ec915f6ce.jpeg",
                Year = "2015",
                Location = Location(lat = -6.143111913847529, lng = 106.81190521761249),
                Description = "Hawkers selling street food in Jakarta.",
                License = "Photo by CEphoto, Uwe Aranas",
                Country = "Indonesia"
            )
        )
    }
}