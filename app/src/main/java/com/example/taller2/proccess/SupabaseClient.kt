package com.example.taller2.proccess

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://hjkkhwoddelulfaheqja.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imhqa2tod29kZGVsdWxmYWhlcWphIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU2MDE4NjksImV4cCI6MjA5MTE3Nzg2OX0.AbRLEc2BkQPwbtUSt40W8r6qY8PiSU6v5s8Zbk9uFDA"
    ){
        install(Postgrest)
        install(Auth)
    }
}