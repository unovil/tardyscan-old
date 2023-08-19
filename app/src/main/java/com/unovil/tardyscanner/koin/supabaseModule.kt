package com.unovil.tardyscanner.koin

import com.unovil.tardyscanner.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import org.koin.core.qualifier.named
import org.koin.dsl.module

val supabaseModule = module {
    single(named("supabaseClient")) {(supabaseUrl: String, supabaseKey: String) ->
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(GoTrue) {
                scheme = BuildConfig.APPLICATION_ID // "com.unovil.tardyscanner"
                host = "login"
            }
            install(Postgrest) {
                propertyConversionMethod = PropertyConversionMethod.SERIAL_NAME
            }
        }
    }
}