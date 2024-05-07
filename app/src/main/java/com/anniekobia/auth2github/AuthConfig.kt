package com.anniekobia.auth2github

import net.openid.appauth.ResponseTypeValues

object AuthConfig {
   const val AUTH_URL = "https://github.com/login/oauth/authorize"
   const val TOKEN_URL = "https://github.com/login/oauth/access_token"
   const val LOGOUT_URL = "https://github.com/logout"

   /**
    * ToDo 2: Set the unique custom URL that you had configured to redirect back to your app
    */
   const val REDIRECT_CALLBACK_URL = "com.anniekobia.auth2github://github/redirect_callback_url"

   const val LOGOUT_CALLBACK_URL = "com.anniekobia.auth2github://github.com/logout_callback_url"

   /**
    * “code” constant from the AppAuth library. This constant is responsible for returned value to
    * the client after successful authorization in the browser. Options: code, token, id_token.
    */
   const val RESPONSE_TYPE = ResponseTypeValues.CODE

   /**
    * Scope of access our app needs as per the GitHub documentation:
    * [https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/scopes-for-oauth-apps]
    * In our case the user’s information and repositories
    * ToDo: change this to "no scope" to only access public repositories
    */
   const val SCOPE = "(no scope)"

   /**
    * ToDo 3: Set the Client Id & Secret generated once you create your Github Auth app
    * In my case saved in my local properties file for security purposes
    */
   const val CLIENT_ID = BuildConfig.CLIENT_ID
   const val CLIENT_SECRET = BuildConfig.CLIENT_SECRET

//   const val CALLBACK_URL = "ru.kts.oauth://github.com/callback"
}