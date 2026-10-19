package com.example.appdivulga.data.repository

import com.example.appdivulga.data.model.LikeRpcResponse
import com.example.appdivulga.data.model.MensagemRpcResponse
import com.example.appdivulga.data.model.Post
import com.example.appdivulga.data.model.PostType
import com.example.appdivulga.data.model.Profile
import com.example.appdivulga.data.model.SeguirRpcResponse

interface PostRepository {
    suspend fun buscarOfertasProximas(
        lat: Double,
        lng: Double,
        tags: List<String>,
        raioMetros: Double
    ): Result<List<Post>>

    suspend fun curtirPublicacao(
        publicacaoId: String,
        usuarioId: String
    ): Result<LikeRpcResponse>

    suspend fun seguirDesseguirUsuario(
        seguidorId: String,
        seguindoId: String
    ): Result<SeguirRpcResponse>

    suspend fun enviarMensagemPrivada(
        remetenteId: String,
        destinatarioId: String,
        publicacaoId: String?,
        texto: String
    ): Result<MensagemRpcResponse>

    suspend fun criarPublicacao(
        autorId: String,
        titulo: String,
        descricao: String?,
        midiaBytes: ByteArray?,
        fileName: String?,
        tipoMidia: PostType,
        linkExterno: String?,
        tags: List<String>,
        lat: Double,
        lng: Double,
        expirar24h: Boolean = true,
        vencimentoPromocao: String? = null
    ): Result<String>

    suspend fun denunciarPublicacao(
        publicacaoId: String,
        denuncianteId: String,
        motivo: String,
        detalhes: String?
    ): Result<Unit>

    suspend fun excluirPublicacao(
        publicacaoId: String
    ): Result<Unit>

    suspend fun uploadMedia(
        bytes: ByteArray,
        fileName: String
    ): Result<String>

    suspend fun obterPerfil(
        usuarioId: String
    ): Result<Profile>

    suspend fun atualizarTagsInteresse(
        usuarioId: String,
        tags: List<String>
    ): Result<Unit>
}
