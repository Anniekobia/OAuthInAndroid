package com.anniekobia.auth2github

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anniekobia.auth2github.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel by viewModels<AuthViewModel>()
    private val getAuthResponse =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val dataIntent = it.data ?: return@registerForActivityResult
            handleAuthResponseIntent(dataIntent)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onLoginClick()
        attachObservers()
    }

    private fun onLoginClick() {
        binding.logInBtn.setOnClickListener {
            Log.e("AuthPOCLogs: ", "LogInFragment: Login Button Clicked")
            authViewModel.openLoginPage()
        }
    }

    private fun attachObservers() {
        lifecycleScope.launch {
            authViewModel.openAuthPage.collect { openAuthPageIntent ->
                try {
                    getAuthResponse.launch(openAuthPageIntent)
                } catch (e: Exception) {
                    Log.e(
                        "AuthPOCLogs: ",
                        "LoginFrag: ${e.message} ${openAuthPageIntent.data.toString()}"
                    )
                }

            }
        }
        lifecycleScope.launch {
            authViewModel.authSuccess.collect {
                if (it) {
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
            }
        }
    }

    private fun handleAuthResponseIntent(intent: Intent) {
        val exception = AuthorizationException.fromIntent(intent)
        val tokenExchangeRequest = AuthorizationResponse.fromIntent(intent)
            ?.createTokenExchangeRequest()
        when {
            exception != null -> {
                Log.e("AuthPOCLogs: ", "LogInFragment: AuthCode failed ${exception.message}")
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            }

            tokenExchangeRequest != null ->
                authViewModel.onAuthCodeReceived(tokenExchangeRequest)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}