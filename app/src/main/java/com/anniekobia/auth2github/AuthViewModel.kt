package com.anniekobia.auth2github

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.ClientSecretPost
import net.openid.appauth.EndSessionRequest
import net.openid.appauth.TokenRequest


class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val applicationContext = application.applicationContext
    private val authService: AuthorizationService = AuthorizationService(getApplication())
    private val authServiceConfiguration = AuthorizationServiceConfiguration(
        Uri.parse(AuthConfig.AUTH_URL),
        Uri.parse(AuthConfig.TOKEN_URL),
        null,
        Uri.parse(AuthConfig.LOGOUT_URL)
    )
    private val _loadingState = MutableStateFlow(false)

    private val _openAuthPage = MutableStateFlow(Intent())
    val openAuthPage get() = _openAuthPage.asStateFlow()

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess get() = _authSuccess.asStateFlow()

    private val _openLogoutPage = MutableStateFlow(Intent())
    val openLogoutPage get() = _openLogoutPage.asStateFlow()

    private val _logoutSuccess = MutableStateFlow(false)
    val logoutSuccess get() = _logoutSuccess.asStateFlow()

    private val sharedPref =
        applicationContext.getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)


    fun openLoginPage() {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        val authRequest = getAuthRequest()

        Log.e(
            "AuthPOCLogs: ",
            "1. AuthVM: AuthRequest Generated verifier=${authRequest.codeVerifier},challenge=${authRequest.codeVerifierChallenge}"
        )

        try {
            val openAuthPageIntent = authService.getAuthorizationRequestIntent(
                authRequest,
                customTabsIntent
            )
            Log.e(
                "AuthPOCLogs: ",
                "2. AuthVM: Intent Open auth page before: ${authRequest.toUri()}"
            )
            _openAuthPage.value = openAuthPageIntent
            Log.e("AuthPOCLogs: ", "2. AuthVM: Intent Open auth page after: ${authRequest.toUri()}")
        } catch (e: Exception) {
            Log.e("AuthPOCLogs: ", "AuthVM: Intent Open auth page failed")
            return
        }
    }

    private fun getAuthRequest(): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            authServiceConfiguration,
            AuthConfig.CLIENT_ID,
            AuthConfig.RESPONSE_TYPE,
            AuthConfig.REDIRECT_CALLBACK_URL.toUri()
        )
            .setScopes(AuthConfig.SCOPE)
            .build()
    }

    fun onAuthCodeReceived(tokenRequest: TokenRequest) {
        Log.e("AuthPOCLogs: ", "3. AuthVM: Received code = ${tokenRequest.authorizationCode}")

        viewModelScope.launch {
            _loadingState.value = true
            runCatching {
                Log.e(
                    "AuthPOCLogs: ",
                    "4. Change code to token. Url = ${tokenRequest.configuration.tokenEndpoint}, verifier = ${tokenRequest.codeVerifier}"
                )
                performTokenRequest(
                    authService = authService,
                    tokenRequest = tokenRequest
                )
            }.onSuccess {
                _loadingState.value = false
                _authSuccess.value = true
                Log.e("AuthPOCLogs: ", "AuthVM: onAuthCodeReceived authentication success")
            }.onFailure {
                _loadingState.value = false
                Log.e("AuthPOCLogs: ", "AuthVM: onAuthCodeReceived authentication failed")
            }
        }
    }

    private fun performTokenRequest(
        authService: AuthorizationService,
        tokenRequest: TokenRequest,
    ) {
        val tokens = authService.performTokenRequest(
            tokenRequest
//            getClientAuthentication()
        ) { response, ex ->
            when {
                response != null -> {
                    val tokens = AuthenticationTokenModel(
                        accessToken = response.accessToken.orEmpty(),
                        refreshToken = response.refreshToken.orEmpty(),
                        idToken = response.idToken.orEmpty()
                    )
                    Log.e("AuthPOCLogs: ", "AuthVM: onAuthCodeReceived authentication success")

                    Log.e(
                        "AuthPOCLogs: ",
                        "AuthVM: TokensResponse: \n ID Token-> ${response.idToken} \n" +
                                " AuthToken-> ${response.accessToken} \n " +
                                " RefreshToken-> ${response.refreshToken} \n " +
                                " Expiry -> ${response.accessTokenExpirationTime} \n " +
                                " TokenType -> ${response.tokenType}"
                    )
                    with(sharedPref.edit()) {
                        putString("AUTH_TOKEN", response.accessToken)
                        putString("REFRESH_TOKEN", response.refreshToken)
                        putString("ID_TOKEN", response.idToken)
                        apply()
                    }
//                    Log.e(
//                        "AuthPOCLogs: ",
//                        "AuthVM: Your AuthToken is ${response.accessToken}"
//                    )
//                    Log.e(
//                        "AuthPOCLogs: ",
//                        "AuthVM: Your RefreshToken is ${response.refreshToken}"
//                    )
//                    Log.e(
//                        "AuthPOCLogs: ",
//                        "AuthVM: Your IdToken is ${response.idToken}"
//                    )

                    Result.success(tokens)
                }

                ex != null -> {
                    Result.failure(ex)
                }

                else -> Result.failure(Exception("Unreachable"))
            }
        }
        return tokens
    }

//    private fun getClientAuthentication(): ClientAuthentication {
//        return ClientSecretPost(AuthConfig.CLIENT_SECRET)
//    }

    private fun getLogoutRequest(): EndSessionRequest {
        val idToken = sharedPref.getString("ID_TOKEN", "")
        return EndSessionRequest.Builder(authServiceConfiguration)
            .setIdTokenHint(idToken)
            .setPostLogoutRedirectUri(AuthConfig.LOGOUT_URL.toUri())
            .build()
    }

    fun openLogoutPage() {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        val logoutRequest = getLogoutRequest()

        Log.e(
            "AuthPOCLogs: ",
            "1. AuthVM: LogoutRequest Data:${logoutRequest.idTokenHint}"
        )

        try {
            val logoutIntent = authService.getEndSessionRequestIntent(
                logoutRequest,
                customTabsIntent
            )
            Log.e(
                "AuthPOCLogs: ",
                "2. AuthVM: Intent Open Logout page before: ${logoutRequest.toUri()}"
            )
            _openLogoutPage.value = logoutIntent
            Log.e(
                "AuthPOCLogs: ",
                "2. AuthVM: Intent Open Logout page after: ${logoutRequest.toUri()}"
            )
        } catch (e: Exception) {
            Log.e("AuthPOCLogs: ", "AuthVM: Intent Open Logout page failed")
            return
        }
    }

    fun clearData() {
        sharedPref.edit().clear().apply()
        _logoutSuccess.value = true
    }

    override fun onCleared() {
        super.onCleared()
        authService.dispose()
    }
}
