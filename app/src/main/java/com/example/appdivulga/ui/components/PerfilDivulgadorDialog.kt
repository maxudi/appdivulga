package com.example.appdivulga.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appdivulga.data.model.Post
import com.example.appdivulga.ui.theme.PointsGold
import com.example.appdivulga.ui.theme.PromoOrange
import com.example.appdivulga.ui.theme.TrustBlue

@Composable
fun PerfilDivulgadorDialog(
    post: Post,
    onDismissRequest: () -> Unit,
    onSendMessage: (destinatarioId: String, publicacaoId: String?, texto: String) -> Unit
) {
    val context = LocalContext.current
    var mensagemTexto by remember { mutableStateOf("Olá! Vi seu anúncio \"${post.titulo}\" no PromoNear e gostaria de saber mais informações.") }
    var mensagemEnviadaComSucesso by remember { mutableStateOf(false) }

    val isPJ = post.autorTipoConta.equals("pj", ignoreCase = true) || !post.autorCnpj.isNullOrEmpty()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            if (!post.autorAvatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = post.autorAvatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(TrustBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPJ) Icons.Default.Store else Icons.Default.Person,
                        contentDescription = "Tipo Conta",
                        tint = TrustBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isPJ && !post.autorNomeLoja.isNullOrEmpty()) post.autorNomeLoja else post.autorNome,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge PF vs PJ
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TrustBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (isPJ) "🏢 Pessoa Jurídica (Loja/CNPJ)" else "👤 Pessoa Física",
                            style = MaterialTheme.typography.labelSmall,
                            color = TrustBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Badge Pontuação do Autor
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PointsGold.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Pontos",
                                tint = PointsGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${post.autorPontos} pts",
                                style = MaterialTheme.typography.labelSmall,
                                color = PointsGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Detalhes Específicos se for Loja PJ
                if (isPJ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Informações do Estabelecimento:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TrustBlue
                            )

                            if (!post.autorCnpj.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = "CNPJ",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CNPJ: ${post.autorCnpj}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (!post.autorEnderecoLoja.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Endereço",
                                        modifier = Modifier.size(16.dp),
                                        tint = PromoOrange
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = post.autorEnderecoLoja,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(post.autorEnderecoLoja)}"))
                                            context.startActivity(mapIntent)
                                        } catch (_: Exception) {}
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("📍 Abrir Endereço no Mapa", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Caixa para Enviar Mensagem Privada
                Text(
                    text = "💬 Enviar Mensagem Privada ao Divulgador:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (mensagemEnviadaComSucesso) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PromoOrange.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✅ Mensagem privada enviada com sucesso! O divulgador responderá em breve.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PromoOrange,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = mensagemTexto,
                        onValueChange = { mensagemTexto = it },
                        placeholder = { Text("Digite sua mensagem...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TrustBlue)
                    )
                }
            }
        },
        confirmButton = {
            if (!mensagemEnviadaComSucesso) {
                Button(
                    onClick = {
                        if (mensagemTexto.isNotBlank()) {
                            onSendMessage(post.autorId, post.id, mensagemTexto)
                            mensagemEnviadaComSucesso = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PromoOrange)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enviar Mensagem")
                }
            } else {
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = TrustBlue)
                ) {
                    Text("Fechar")
                }
            }
        },
        dismissButton = {
            if (!mensagemEnviadaComSucesso) {
                TextButton(onClick = onDismissRequest) {
                    Text("Fechar")
                }
            }
        }
    )
}
