package com.anniekobia.auth2github

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anniekobia.auth2github.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private var _fragmentHomeBinding: FragmentHomeBinding? = null
    private val fragmentHomeBinding get() = _fragmentHomeBinding!!
    private val authViewModel by viewModels<AuthViewModel>()
    private val logoutResponse = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
//        authViewModel.webLogoutComplete()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return fragmentHomeBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showTokenValues()
        setListeners()
        setObservers()
    }

    private fun showTokenValues() {
        val sharedPref =
            requireContext().getSharedPreferences("SHARED_PREF", Context.MODE_PRIVATE)
        with(sharedPref) {
            val authToken = getString("AUTH_TOKEN", "")
            val refreshToken = getString("REFRESH_TOKEN", "")
            fragmentHomeBinding.authtokenValue.text = authToken
            fragmentHomeBinding.refreshtokenValue.text = refreshToken
        }
    }

    private fun setListeners() {
        fragmentHomeBinding.logOutBtn.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_LoginFragment)
        }
        fragmentHomeBinding.refreshBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Refresh Token Clicked", Toast.LENGTH_LONG).show()
        }
    }

    private fun setObservers() {
        lifecycleScope.launch {
            authViewModel.openLogoutPage.collect { openLogoutPageIntent ->
                try {
                    logoutResponse.launch(openLogoutPageIntent)
                } catch (e: Exception) {
                    Log.e(
                        "AuthPOCLogs: ",
                        "HomeFrag: ${e.message} ${openLogoutPageIntent.data.toString()}"
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentHomeBinding = null
    }
}