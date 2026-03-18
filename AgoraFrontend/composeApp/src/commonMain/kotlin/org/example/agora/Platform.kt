package org.example.agora

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform