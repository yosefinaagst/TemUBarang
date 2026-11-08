package com.example.temubarang.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = "https://ocvdmpfluguevekhmmaz.supabase.co",
            supabaseKey = "sb_publishable_o-PUGDabW5cYfGKKuTznaw_rBBCw_jR"
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}