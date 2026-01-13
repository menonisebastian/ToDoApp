package com.example.todoapp.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// ================= MODELOS DE DATOS (DTOs) =================
// Estos modelos mapean la respuesta JSON de https://pokeapi.co/api/v2/pokemon/{nombre}

data class PokemonResponse(
    val name: String,
    val sprites: Sprites,
    val types: List<TypeSlot>,
    val stats: List<StatSlot>
)

data class Sprites(
    val front_default: String?
)

data class TypeSlot(
    val type: TypeName
)

data class TypeName(
    val name: String
)

data class StatSlot(
    val base_stat: Int,
    val stat: StatName
)

data class StatName(
    val name: String
)

// ================= INTERFAZ RETROFIT =================
interface PokeApi {
    @GET("pokemon/{name}")
    suspend fun getPokemon(@Path("name") name: String): PokemonResponse
}

// ================= INSTANCIA SINGLETON =================
object RetrofitClient {
    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    val instance: PokeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApi::class.java)
    }
}