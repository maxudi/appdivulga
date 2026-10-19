package com.example.appdivulga.data.model

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
    val status: PostStatus = PostStatus.ATIVA,
    val createdAt: String = ""
) {
    fun formatDistancia(): String {
        return if (distanciaMetros >= 1000) {
            String.format("%.1f km", distanciaMetros / 1000)
        } else {
            "${distanciaMetros.toInt()} m"
        }
    }
}

data class Profile(
    val id: String,
    val nome: String,
    val avatarUrl: String? = null,
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
