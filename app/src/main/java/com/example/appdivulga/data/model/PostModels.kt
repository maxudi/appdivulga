package com.example.appdivulga.data.model

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

enum class PostType {
    IMAGEM,
    VIDEO,
    TEXTO;

    companion object {
        fun fromString(value: String?): PostType {
            return when (value?.lowercase()) {
                "imagem", "image" -> IMAGEM
                "video" -> VIDEO
                else -> TEXTO
            }
        }
    }
}

enum class PostStatus {
    ATIVA,
    PAUSADA_EM_ANALISE,
    EXCLUIDA;

    companion object {
        fun fromString(value: String?): PostStatus {
            return when (value?.lowercase()) {
                "pausada_em_analise" -> PAUSADA_EM_ANALISE
                "excluida" -> EXCLUIDA
                else -> ATIVA
            }
        }
    }
}

enum class ReportReason(val label: String, val code: String) {
    CONTEUDO_IMPROPRIO("Conteúdo Impróprio / Inadequado", "conteudo_improprio"),
    GOLPE_FRAUDE("Suspeita de Golpe ou Fraude", "golpe_fraude"),
    PRODUTO_PROIBIDO("Produto ou Serviço Proibido/Ilegal", "produto_proibido"),
    SPAM("Spam ou Informação Enganosa", "spam"),
    OUTRO("Outro Motivo", "outro")
}

data class Post(
    val id: String,
    val autorId: String,
    val autorNome: String,
    val autorAvatarUrl: String? = null,
    val autorPontos: Long = 0,
    val autorTipoConta: String = "pf",
    val autorCnpj: String? = null,
    val autorNomeLoja: String? = null,
    val autorEnderecoLoja: String? = null,
    val titulo: String,
    val descricao: String? = null,
    val midiaUrl: String? = null,
    val tipoMidia: PostType = PostType.TEXTO,
    val linkExterno: String? = null,
    val tags: List<String> = emptyList(),
    val distanciaMetros: Double = 0.0,
    val totalCurtidas: Int = 0,
    val totalComentarios: Int = 0,
    val curtidoPeloUsuario: Boolean = false,
    val seguindoAutor: Boolean = false,
    val status: PostStatus = PostStatus.ATIVA,
    val vencimentoPromocao: String? = null,
    val dataExpiracao: String? = null,
    val createdAt: String = ""
) {
    fun formatDistancia(): String {
        return if (distanciaMetros >= 1000) {
            String.format("%.1f km", distanciaMetros / 1000)
        } else {
            "${distanciaMetros.toInt()} m"
        }
    }

    fun formatVencimentoPromocao(): String? {
        if (vencimentoPromocao.isNullOrEmpty()) return null
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val date = isoFormat.parse(vencimentoPromocao.take(10)) ?: return null
            "🏷️ Promoção válida até ${displayFormat.format(date)}"
        } catch (_: Exception) {
            "🏷️ Oferta por tempo limitado"
        }
    }

    fun formatTempoRestante(): String? {
        if (dataExpiracao.isNullOrEmpty()) return null
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val expDate = isoFormat.parse(dataExpiracao.take(19)) ?: return "⏱️ Expira em 24h"
            val diffMs = expDate.time - System.currentTimeMillis()
            if (diffMs <= 0) {
                "Expirado"
            } else {
                val totalMinutes = diffMs / (1000 * 60)
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                if (hours > 0) {
                    "⏱️ Expira em ${hours}h ${minutes}m"
                } else {
                    "⏱️ Expira em ${minutes}m"
                }
            }
        } catch (_: Exception) {
            "⏱️ Expira em 24h"
        }
    }
}

data class Profile(
    val id: String,
    val nome: String,
    val avatarUrl: String? = null,
    val tipoConta: String = "pf",
    val cnpj: String? = null,
    val nomeLoja: String? = null,
    val enderecoLoja: String? = null,
    val pontos: Long = 0,
    val tagsInteresse: List<String> = emptyList()
)

data class Comment(
    val id: String,
    val publicacaoId: String,
    val usuarioId: String,
    val usuarioNome: String,
    val usuarioAvatarUrl: String? = null,
    val texto: String,
    val createdAt: String
)

data class MensagemChat(
    val id: String,
    val remetenteId: String,
    val destinatarioId: String,
    val publicacaoId: String? = null,
    val texto: String,
    val lida: Boolean = false,
    val createdAt: String = ""
)
