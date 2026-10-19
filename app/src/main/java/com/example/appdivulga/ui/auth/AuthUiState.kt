package com.example.appdivulga.ui.auth

data class AuthUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val nameInput: String = "",
    val isSignUpMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val isGuest: Boolean = false,
    val userEmail: String? = null,
    val userId: String? = null,
    val showForgotPasswordDialog: Boolean = false,
    val isResettingPassword: Boolean = false
)
