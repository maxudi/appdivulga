package com.example.appdivulga.ui.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appdivulga.R
import com.example.appdivulga.ui.components.CreatePostBottomSheet
import com.example.appdivulga.ui.components.DEFAULT_CATEGORIES
import com.example.appdivulga.ui.components.DenunciaDialog
import com.example.appdivulga.ui.components.PostCard
import com.example.appdivulga.ui.theme.PromoOrange
import com.example.appdivulga.ui.theme.TrustBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    userEmail: String? = null,
    onLogoutClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRadiusMenu by remember { mutableStateOf(false) }

    val raioOptions = listOf(
        1000.0 to "1 km",
        5000.0 to "5 km",
        10000.0 to "10 km",
        25000.0 to "25 km",
        50000.0 to "50 km"
    )

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.logo_promonear_horizontal),
                            contentDescription = "PromoNear Logo",
                            modifier = Modifier.size(width = 160.dp, height = 42.dp)
                        )
                    },
                    actions = {
                        Box {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Raio",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = raioOptions.find { it.first == uiState.raioMetros }?.second ?: "10 km",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { showRadiusMenu = true },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = "Alterar Raio",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            DropdownMenu(
                                expanded = showRadiusMenu,
                                onDismissRequest = { showRadiusMenu = false }
                            ) {
                                Text(
                                    text = "Raio de Proximidade:",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                                raioOptions.forEach { (meters, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            showRadiusMenu = false
                                            viewModel.setRaioMetros(meters)
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.carregarOfertasProximas(isRefresh = true) }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Atualizar Feed",
                                tint = TrustBlue
                            )
                        }

                        IconButton(onClick = onLogoutClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sair da Conta",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                PrimaryTabRow(
                    selectedTabIndex = uiState.activeMode.ordinal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = uiState.activeMode == FeedMode.RECEBEDOR,
                        onClick = { viewModel.setFeedMode(FeedMode.RECEBEDOR) },
                        text = {
                            Text(
                                text = "📥 Receber Ofertas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (uiState.activeMode == FeedMode.RECEBEDOR) PromoOrange else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = uiState.activeMode == FeedMode.DIVULGADOR,
                        onClick = { viewModel.setFeedMode(FeedMode.DIVULGADOR) },
                        text = {
                            Text(
                                text = "📢 Meus Anúncios",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (uiState.activeMode == FeedMode.DIVULGADOR) TrustBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DEFAULT_CATEGORIES.forEach { category ->
                        val isSelected = uiState.selectedTags.contains(category)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleTagFilter(category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PromoOrange,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.abrirSheetCriarPost() },
                icon = { Icon(Icons.Default.Add, contentDescription = "Criar") },
                text = { Text("Anunciar Oferta", fontWeight = FontWeight.Bold) },
                containerColor = PromoOrange,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PromoOrange
                    )
                }

                uiState.posts.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Sem ofertas",
                            modifier = Modifier.size(64.dp),
                            tint = PromoOrange.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhuma oferta encontrada no raio de ${raioOptions.find { it.first == uiState.raioMetros }?.second}!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Seja o primeiro a divulgar um produto ou serviço no PromoNear para as pessoas da região!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    val filteredPosts = if (uiState.activeMode == FeedMode.DIVULGADOR) {
                        uiState.posts.filter { it.autorId == viewModel.currentUserId }
                    } else {
                        uiState.posts
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = filteredPosts,
                            key = { it.id }
                        ) { post ->
                            PostCard(
                                post = post,
                                currentUserId = viewModel.currentUserId,
                                onLikeClick = { viewModel.curtirPublicacao(it) },
                                onReportClick = { viewModel.abrirDialogDenuncia(it) },
                                onDeleteClick = { viewModel.excluirPublicacao(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Diálogo de Denúncia
    uiState.postToReport?.let { post ->
        DenunciaDialog(
            publicacaoTitulo = post.titulo,
            onDismissRequest = { viewModel.fecharDialogDenuncia() },
            onConfirmDenuncia = { motivo, detalhes ->
                viewModel.denunciarPublicacao(post.id, motivo, detalhes)
            }
        )
    }

    // Bottom Sheet de Criar Nova Divulgação
    if (uiState.showCreateSheet) {
        CreatePostBottomSheet(
            onDismissRequest = { viewModel.fecharSheetCriarPost() },
            onSubmitPost = { titulo, descricao, midiaBytes, fileName, tipoMidia, linkExterno, tags ->
                viewModel.criarPublicacao(
                    titulo = titulo,
                    descricao = descricao,
                    midiaBytes = midiaBytes,
                    fileName = fileName,
                    tipoMidia = tipoMidia,
                    linkExterno = linkExterno,
                    tags = tags
                )
            }
        )
    }
}
