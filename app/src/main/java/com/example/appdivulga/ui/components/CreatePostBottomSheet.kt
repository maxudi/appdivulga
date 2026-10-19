package com.example.appdivulga.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.appdivulga.data.model.PostType
import com.example.appdivulga.ui.theme.PromoOrange
import com.example.appdivulga.ui.theme.TrustBlue

val DEFAULT_CATEGORIES = listOf(
    "Alimentação", "Bebês/Fraldas", "Farmácia", "Serviços",
    "Eletrônicos", "Moda/Vestuário", "Supermercado", "Pet Shop", "Outros"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostBottomSheet(
    onDismissRequest: () -> Unit,
    onSubmitPost: (
        titulo: String,
        descricao: String?,
        midiaBytes: ByteArray?,
        fileName: String?,
        tipoMidia: PostType,
        linkExterno: String?,
        tags: List<String>,
        expirar24h: Boolean
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var linkExterno by remember { mutableStateOf("") }
    var selectedPostType by remember { mutableStateOf(PostType.TEXTO) }
    val selectedTags = remember { mutableStateListOf<String>() }

    // Mídia Selecionada da Galeria
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var selectedMediaBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    // Opção de Expiração de 24 Horas no Feed
    var expirar24h by remember { mutableStateOf(true) }

    // Termo de Responsabilidade do Divulgador
    var declaroResponsabilidade by remember { mutableStateOf(false) }

    // Launcher do Photo Picker nativo da Galeria
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUri = uri
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.use { it.readBytes() }
                selectedMediaBytes = bytes
                val mimeType = context.contentResolver.getType(uri)
                val ext = if (mimeType?.contains("video") == true) "mp4" else "jpg"
                selectedFileName = "midia_${System.currentTimeMillis()}.$ext"

                if (mimeType?.contains("video") == true) {
                    selectedPostType = PostType.VIDEO
                } else {
                    selectedPostType = PostType.IMAGEM
                }
            } catch (_: Exception) {}
        }
    }

    val isFormValid = titulo.isNotBlank() && selectedTags.isNotEmpty() && declaroResponsabilidade

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "📢 Nova Divulgação no PromoNear",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TrustBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Título
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título da Oferta / Produto / Serviço *") },
                placeholder = { Text("Ex: Promoção Fraldas Pampers G") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TrustBlue)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Descrição
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição e Detalhes") },
                placeholder = { Text("Descreva preços, horários, localização exata...") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            clipboardManager.getText()?.text?.let { copiedText ->
                                descricao = if (descricao.isBlank()) copiedText else "$descricao $copiedText"
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Colar texto",
                            tint = PromoOrange
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TrustBlue)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Configuração de Validade (Expirar em 24h)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PromoOrange.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Temporizador 24h",
                            tint = PromoOrange
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Column {
                            Text(
                                text = "Oferta temporária (24 horas)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Sairá do feed ativo após 24h (permanecerá salva na sua conta)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = expirar24h,
                        onCheckedChange = { expirar24h = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PromoOrange
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Seletor de Tipo de Conteúdo
            Text(
                text = "Tipo de Mídia:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PostType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = (selectedPostType == type),
                        onClick = { selectedPostType = type },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = PostType.entries.size)
                    ) {
                        Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Área de Mídia com Preview
            if (selectedPostType != PostType.TEXTO) {
                if (selectedMediaUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = selectedMediaUri,
                            contentDescription = "Mídia selecionada",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = {
                                selectedMediaUri = null
                                selectedMediaBytes = null
                                selectedFileName = null
                                selectedPostType = PostType.TEXTO
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Black.copy(alpha = 0.7f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remover",
                                    tint = Color.White,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val mediaType = if (selectedPostType == PostType.VIDEO) {
                                    ActivityResultContracts.PickVisualMedia.VideoOnly
                                } else {
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                }
                                mediaPickerLauncher.launch(
                                    PickVisualMediaRequest(mediaType)
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Abrir Galeria",
                                tint = PromoOrange
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text(
                                text = "Abrir Galeria e escolher ${selectedPostType.name.lowercase()}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TrustBlue
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Campo Link Externo
            OutlinedTextField(
                value = linkExterno,
                onValueChange = { linkExterno = it },
                label = { Text("Link Externo / WhatsApp / Site (Opcional)") },
                placeholder = { Text("https://wa.me/55...") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            clipboardManager.getText()?.text?.let { copiedText ->
                                linkExterno = copiedText
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Colar Link",
                            tint = PromoOrange
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TrustBlue)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Seleção de Tags/Categorias
            Text(
                text = "Categorias / Tags da Oferta *:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DEFAULT_CATEGORIES.forEach { category ->
                    val isSelected = selectedTags.contains(category)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedTags.remove(category)
                            else selectedTags.add(category)
                        },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PromoOrange,
                            selectedLabelColor = Color.White
                        ),
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox Termo de Responsabilidade
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { declaroResponsabilidade = !declaroResponsabilidade },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = declaroResponsabilidade,
                    onCheckedChange = { declaroResponsabilidade = it }
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(
                    text = "Declaro responsabilidade legal e veracidade pelo conteúdo anunciado e conformidade com as regras do PromoNear.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botão Publicar Oferta
            Button(
                onClick = {
                    if (isFormValid) {
                        onSubmitPost(
                            titulo,
                            descricao.ifBlank { null },
                            selectedMediaBytes,
                            selectedFileName,
                            selectedPostType,
                            linkExterno.ifBlank { null },
                            selectedTags.toList(),
                            expirar24h
                        )
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PromoOrange,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Publicar Oferta na Região",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
