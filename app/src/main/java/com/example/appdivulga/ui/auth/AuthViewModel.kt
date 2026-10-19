package com.example.appdivulga.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentSession()
    }

    private fun checkCurrentSession() {
        val currentSession = supabaseClient.auth.currentSessionOrNull()
        val currentUser = supabaseClient.auth.currentUserOrNull()

        if (currentSession != null && currentUser != null) {
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    isGuest = false,
                    userEmail = currentUser.email,
                    userId = currentUser.id
                )
            }
        }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(emailInput = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(passwordInput = value, errorMessage = null) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(nameInput = value, errorMessage = null) }
    }

    fun toggleAuthMode() {
        _uiState.update {
            it.copy(
                isSignUpMode = !it.isSignUpMode,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun openForgotPasswordDialog() {
        _uiState.update { it.copy(showForgotPasswordDialog = true) }
    }

    fun closeForgotPasswordDialog() {
        _uiState.update { it.copy(showForgotPasswordDialog = false) }
    }

    fun sendPasswordResetEmail(emailToReset: String? = null) {
        val email = emailToReset?.trim() ?: _uiState.value.emailInput.trim()

        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Insira o seu e-mail para recuperar a senha.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isResettingPassword = true, errorMessage = null, successMessage = null) }

            runCatching {
                supabaseClient.auth.resetPasswordForEmail(email)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isResettingPassword = false,
                        showForgotPasswordDialog = false,
                        successMessage = "Enviamos as instruções de redefinição de senha para $email"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isResettingPassword = false,
                        errorMessage = err.message ?: "Não foi possível enviar o e-mail de redefinição. Verifique o endereço digitado."
                    )
                }
            }
        }
    }

    fun executeAuthAction() {
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput.trim()

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor, preencha e-mail e senha.") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "A senha deve conter no mínimo 6 caracteres.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            runCatching {
                if (_uiState.value.isSignUpMode) {
                    supabaseClient.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }
                } else {
                    supabaseClient.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
                }
            }.onSuccess {
                val user = supabaseClient.auth.currentUserOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        isGuest = false,
                        userEmail = user?.email ?: email,
                        userId = user?.id ?: "00000000-0000-0000-0000-000000000001"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "E-mail ou senha incorretos. Verifique suas credenciais."
                    )
                }
            }
        }
    }

    fun continueAsGuest() {
        _uiState.update {
            it.copy(
                isLoggedIn = true,
                isGuest = true,
                userEmail = "visitante@promonear.com",
                userId = "00000000-0000-0000-0000-000000000001"
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { supabaseClient.auth.signOut() }
            _uiState.update {
                AuthUiState()
            }
        }
    }
}
