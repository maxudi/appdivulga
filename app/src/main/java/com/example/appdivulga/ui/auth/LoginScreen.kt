package com.example.appdivulga.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appdivulga.R
import com.example.appdivulga.ui.theme.NeutralBackground
import com.example.appdivulga.ui.theme.PromoNearSoftGradient
import com.example.appdivulga.ui.theme.PromoOrange
import com.example.appdivulga.ui.theme.SuccessGreen
import com.example.appdivulga.ui.theme.TrustBlue

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var resetEmailInput by remember { mutableStateOf("") }

    if (uiState.isLoggedIn) {
        onAuthSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBackground)
    ) {
        // Fundo com iluminação suave em degradê no topo (sem blocos escuros pesados)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(PromoNearSoftGradient)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Logo Oficial PromoNear Flutuante (sem fundo branco / sem caixa)
            Image(
                painter = painterResource(id = R.drawable.logo_promonear),
                contentDescription = "PromoNear Logo",
                modifier = Modifier
                    .size(width = 230.dp, height = 85.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ofertas Relevantes por Proximidade",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TrustBlue,
                textAlign = TextAlign.Center
            )

            Text(
                text = "A ponte direta entre quem divulga e quem economiza.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card Limpo do Formulário de Acesso
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (uiState.isSignUpMode) "Criar Sua Conta" else "Entrar no PromoNear",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (uiState.isSignUpMode)
                            "Divulgue ou encontre ofertas perto do seu endereço"
                        else
                            "Acesse sua conta para ver promoções na sua regiao",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mensagem de Sucesso (ex: E-mail de redefinição enviado)
                    uiState.successMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SuccessGreen.copy(alpha = 0.15f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "✅ $msg",
                                style = MaterialTheme.typography.bodySmall,
                                color = SuccessGreen,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Nome Completo (Modo Cadastro)
                    AnimatedVisibility(visible = uiState.isSignUpMode) {
                        Column {
                            OutlinedTextField(
                                value = uiState.nameInput,
                                onValueChange = { authViewModel.onNameChanged(it) },
                                label = { Text("Nome Completo ou Nome do Negócio") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = TrustBlue)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Campo E-mail
                    OutlinedTextField(
                        value = uiState.emailInput,
                        onValueChange = { authViewModel.onEmailChanged(it) },
                        label = { Text("Seu E-mail") },
                        placeholder = { Text("seuemail@exemplo.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = TrustBlue)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrustBlue,
                            focusedLabelColor = TrustBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo Senha
                    OutlinedTextField(
                        value = uiState.passwordInput,
                        onValueChange = { authViewModel.onPasswordChanged(it) },
                        label = { Text("Sua Senha") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TrustBlue)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TrustBlue,
                            focusedLabelColor = TrustBlue
                        )
                    )

                    // Botão Esqueceu a Senha (no Login)
                    if (!uiState.isSignUpMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    resetEmailInput = uiState.emailInput
                                    authViewModel.openForgotPasswordDialog()
                                }
                            ) {
                                Text(
                                    text = "Esqueceu a senha?",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PromoOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Exibição do Erro de Autenticação
                    uiState.errorMessage?.let { errorMsg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⚠️ $errorMsg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )

                                // Se errou a senha no login, oferecer atalho para recuperar
                                if (!uiState.isSignUpMode) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    TextButton(
                                        onClick = {
                                            resetEmailInput = uiState.emailInput
                                            authViewModel.openForgotPasswordDialog()
                                        }
                                    ) {
                                        Text(
                                            text = "👉 Clique aqui para redefinir sua senha por e-mail",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = PromoOrange,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botão Principal (Laranja PromoNear para ação principal)
                    Button(
                        onClick = { authViewModel.executeAuthAction() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PromoOrange,
                            contentColor = Color.White
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (uiState.isSignUpMode) "Cadastrar e Acessar" else "Entrar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Alternar Modo (Entrar vs Cadastre-se)
                    TextButton(onClick = { authViewModel.toggleAuthMode() }) {
                        Text(
                            text = if (uiState.isSignUpMode)
                                "Já tem uma conta? Entrar"
                            else
                                "Não tem conta? Cadastre-se grátis",
                            color = TrustBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = " ou ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botão Modos Visitante (Navegar Sem Login)
                    OutlinedButton(
                        onClick = { authViewModel.continueAsGuest() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Navegar como Visitante",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PromoNear © 2025 - Conectando Negócios e Pessoas",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }

    // Modal Diálogo "Esqueceu a Senha"
    if (uiState.showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { authViewModel.closeForgotPasswordDialog() },
            icon = {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = "Recuperar Senha",
                    tint = PromoOrange,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Recuperar Senha",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Digite seu e-mail cadastrado. Enviaremos um link seguro para você redefinir sua senha.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = resetEmailInput,
                        onValueChange = { resetEmailInput = it },
                        label = { Text("E-mail Cadastrado") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = TrustBlue)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.sendPasswordResetEmail(resetEmailInput)
                    },
                    enabled = !uiState.isResettingPassword && resetEmailInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PromoOrange)
                ) {
                    if (uiState.isResettingPassword) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar Link")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { authViewModel.closeForgotPasswordDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
