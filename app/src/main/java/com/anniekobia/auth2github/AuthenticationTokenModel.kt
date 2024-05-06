package com.anniekobia.auth2github

data class AuthenticationTokenModel(
    val accessToken: String,
    val refreshToken: String,
    val idToken: String
)