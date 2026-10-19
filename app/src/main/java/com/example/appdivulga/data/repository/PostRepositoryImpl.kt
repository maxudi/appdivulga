package com.example.appdivulga.data.repository

import com.example.appdivulga.data.model.BuscarOfertasParams
import com.example.appdivulga.data.model.CriarPublicacaoParams
import com.example.appdivulga.data.model.CurtirPublicacaoParams
import com.example.appdivulga.data.model.DenunciaDto
import com.example.appdivulga.data.model.LikeRpcResponse
import com.example.appdivulga.data.model.Post
import com.example.appdivulga.data.model.PostRpcDto
import com.example.appdivulga.data.model.PostType
import com.example.appdivulga.data.model.Profile
import com.example.appdivulga.data.model.ProfileDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : PostRepository {

    companion object {
        private const val BUCKET_NAME = "midias_divulgacao"
    }

    override suspend fun buscarOfertasProximas(
        lat: Double,
        lng: Double,
        tags: List<String>,
        raioMetros: Double
    ): Result<List<Post>> = withContext(Dispatchers.IO) {
        runCatching {
            val params = BuscarOfertasParams(
                latitude = lat,
                longitude = lng,
                tags = tags,
                raioMetros = raioMetros
            )
            val dtoList = supabaseClient.postgrest.rpc(
                function = "buscar_ofertas_proximas",
                parameters = params
            ).decodeList<PostRpcDto>()

            dtoList.map { it.toDomain() }
        }
    }

    override suspend fun curtirPublicacao(
        publicacaoId: String,
        usuarioId: String
    ): Result<LikeRpcResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val params = CurtirPublicacaoParams(
                publicacaoId = publicacaoId,
                usuarioId = usuarioId
            )
            supabaseClient.postgrest.rpc(
                function = "curtir_publicacao",
                parameters = params
            ).decodeAs<LikeRpcResponse>()
        }
    }

    override suspend fun uploadMedia(
        bytes: ByteArray,
        fileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val bucket = supabaseClient.storage.from(BUCKET_NAME)
            val uniquePath = "${System.currentTimeMillis()}_$fileName"
            bucket.upload(path = uniquePath, data = bytes) {
                upsert = true
            }
            bucket.publicUrl(uniquePath)
        }
    }

    override suspend fun criarPublicacao(
        autorId: String,
        titulo: String,
        descricao: String?,
        midiaBytes: ByteArray?,
        fileName: String?,
        tipoMidia: PostType,
        linkExterno: String?,
        tags: List<String>,
        lat: Double,
        lng: Double
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            var mediaUrl: String? = null
            if (midiaBytes != null && fileName != null) {
                mediaUrl = uploadMedia(midiaBytes, fileName).getOrThrow()
            }

            val params = CriarPublicacaoParams(
                autorId = autorId,
                titulo = titulo,
                descricao = descricao,
                midiaUrl = mediaUrl,
                tipoMidia = tipoMidia.name.lowercase(),
                linkExterno = linkExterno,
                tags = tags,
                lat = lat,
                lng = lng
            )

            supabaseClient.postgrest.rpc(
                function = "criar_publicacao",
                parameters = params
            ).decodeAs<String>()
        }
    }

    override suspend fun denunciarPublicacao(
        publicacaoId: String,
        denuncianteId: String,
        motivo: String,
        detalhes: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val denuncia = DenunciaDto(
                publicacaoId = publicacaoId,
                denuncianteId = denuncianteId,
                motivo = motivo,
                detalhes = detalhes
            )
            supabaseClient.postgrest.from("denuncias").insert(denuncia)
            Unit
        }
    }

    override suspend fun excluirPublicacao(
        publicacaoId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            supabaseClient.postgrest.from("publicacoes").delete {
                filter {
                    eq("id", publicacaoId)
                }
            }
            Unit
        }
    }

    override suspend fun obterPerfil(
        usuarioId: String
    ): Result<Profile> = withContext(Dispatchers.IO) {
        runCatching {
            val dto = supabaseClient.postgrest.from("perfis").select {
                filter {
                    eq("id", usuarioId)
                }
            }.decodeSingle<ProfileDto>()

            dto.toDomain()
        }
    }

    override suspend fun atualizarTagsInteresse(
        usuarioId: String,
        tags: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            supabaseClient.postgrest.from("perfis").update(
                mapOf("tags_interesse" to tags)
            ) {
                filter {
                    eq("id", usuarioId)
                }
            }
            Unit
        }
    }
}
