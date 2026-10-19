package com.example.appdivulga.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostRpcDto(
    @SerialName("id") val id: String,
    @SerialName("autor_id") val autorId: String,
    @SerialName("autor_nome") val autorNome: String? = null,
    @SerialName("autor_avatar_url") val autorAvatarUrl: String? = null,
    @SerialName("autor_pontos") val autorPontos: Long = 0,
    @SerialName("titulo") val titulo: String,
    @SerialName("descricao") val descricao: String? = null,
    @SerialName("midia_url") val midiaUrl: String? = null,
    @SerialName("tipo_midia") val tipoMidia: String = "texto",
    @SerialName("link_externo") val linkExterno: String? = null,
    @SerialName("tags") val tags: List<String> = emptyList(),
    @SerialName("distancia_metros") val distanciaMetros: Double = 0.0,
    @SerialName("total_curtidas") val totalCurtidas: Int = 0,
    @SerialName("total_comentarios") val totalComentarios: Int = 0,
    @SerialName("curtido_pelo_usuario") val curtidoPeloUsuario: Boolean = false,
    @SerialName("seguindo_autor") val seguindoAutor: Boolean = false,
    @SerialName("data_expiracao") val dataExpiracao: String? = null,
    @SerialName("created_at") val createdAt: String = ""
) {
    fun toDomain(): Post {
        return Post(
            id = id,
            autorId = autorId,
            autorNome = autorNome ?: "Usuário",
            autorAvatarUrl = autorAvatarUrl,
            autorPontos = autorPontos,
            titulo = titulo,
            descricao = descricao,
            midiaUrl = midiaUrl,
            tipoMidia = PostType.fromString(tipoMidia),
            linkExterno = linkExterno,
            tags = tags,
            distanciaMetros = distanciaMetros,
            totalCurtidas = totalCurtidas,
            totalComentarios = totalComentarios,
            curtidoPeloUsuario = curtidoPeloUsuario,
            seguindoAutor = seguindoAutor,
            status = PostStatus.ATIVA,
            dataExpiracao = dataExpiracao,
            createdAt = createdAt
        )
    }
}

@Serializable
data class BuscarOfertasParams(
    @SerialName("p_lat") val latitude: Double,
    @SerialName("p_lng") val longitude: Double,
    @SerialName("p_tags") val tags: List<String>,
    @SerialName("p_raio_metros") val raioMetros: Double
)

@Serializable
data class CurtirPublicacaoParams(
    @SerialName("p_publicacao_id") val publicacaoId: String,
    @SerialName("p_usuario_id") val usuarioId: String
)

@Serializable
data class SeguirUsuarioParams(
    @SerialName("p_seguidor_id") val seguidorId: String,
    @SerialName("p_seguindo_id") val seguindoId: String
)

@Serializable
data class LikeRpcResponse(
    @SerialName("sucesso") val sucesso: Boolean = false,
    @SerialName("acao") val acao: String = "",
    @SerialName("total_curtidas") val totalCurtidas: Int = 0,
    @SerialName("pontos_autor") val pontosAutor: Long = 0,
    @SerialName("mensagem") val mensagem: String? = null
)

@Serializable
data class SeguirRpcResponse(
    @SerialName("sucesso") val sucesso: Boolean = false,
    @SerialName("acao") val acao: String = "",
    @SerialName("seguindo") val seguindo: Boolean = false,
    @SerialName("mensagem") val mensagem: String? = null
)

@Serializable
data class CriarPublicacaoParams(
    @SerialName("p_autor_id") val autorId: String,
    @SerialName("p_titulo") val titulo: String,
    @SerialName("p_descricao") val descricao: String? = null,
    @SerialName("p_midia_url") val midiaUrl: String? = null,
    @SerialName("p_tipo_midia") val tipoMidia: String,
    @SerialName("p_link_externo") val linkExterno: String? = null,
    @SerialName("p_tags") val tags: List<String>,
    @SerialName("p_lat") val lat: Double,
    @SerialName("p_lng") val lng: Double,
    @SerialName("p_expirar_24h") val expirar24h: Boolean = true
)

@Serializable
data class DenunciaDto(
    @SerialName("publicacao_id") val publicacaoId: String,
    @SerialName("denunciante_id") val denuncianteId: String,
    @SerialName("motivo") val motivo: String,
    @SerialName("detalhes") val detalhes: String? = null
)

@Serializable
data class ProfileDto(
    @SerialName("id") val id: String,
    @SerialName("nome") val nome: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("pontos") val pontos: Long = 0,
    @SerialName("tags_interesse") val tagsInteresse: List<String> = emptyList()
) {
    fun toDomain(): Profile {
        return Profile(
            id = id,
            nome = nome,
            avatarUrl = avatarUrl,
            pontos = pontos,
            tagsInteresse = tagsInteresse
        )
    }
}

@Serializable
data class CommentDto(
    @SerialName("id") val id: String,
    @SerialName("publicacao_id") val publicacaoId: String,
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("texto") val texto: String,
    @SerialName("created_at") val createdAt: String
)
