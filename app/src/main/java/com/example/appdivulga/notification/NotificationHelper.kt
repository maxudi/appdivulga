package com.example.appdivulga.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.appdivulga.MainActivity
import com.example.appdivulga.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID = "ofertas_proximas_channel"
        const val CHANNEL_NAME = "Ofertas Locais e Divulgações"
        const val CHANNEL_DESC = "Notificações de ofertas e serviços próximos com base nas suas preferências"
        private const val NOTIFICATION_ID_BASE = 1001

        // Cooldown: 30 minutos por publicação e 5 minutos de intervalo global entre alertas
        private const val POST_COOLDOWN_MS = 30 * 60 * 1000L
        private const val GLOBAL_COOLDOWN_MS = 5 * 60 * 1000L
    }

    private val lastNotifiedPostMap = ConcurrentHashMap<String, Long>()
    private var lastGlobalNotificationTime: Long = 0L

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showOfertaProximaNotification(
        publicacaoId: String,
        titulo: String,
        descricao: String,
        distanciaFormatada: String
    ) {
        val now = System.currentTimeMillis()

        // 1. Verificação de Cooldown por Publicação Específica
        val lastNotifiedTime = lastNotifiedPostMap[publicacaoId] ?: 0L
        if (now - lastNotifiedTime < POST_COOLDOWN_MS) {
            return // Ignora alerta repetido da mesma publicação dentro da janela de cooldown
        }

        // 2. Verificação de Cooldown Global entre Alertas
        if (now - lastGlobalNotificationTime < GLOBAL_COOLDOWN_MS) {
            return // Evita excesso de alertas sonoros frequentes ao se mover rápido
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        // Registra o timestamp da notificação
        lastNotifiedPostMap[publicacaoId] = now
        lastGlobalNotificationTime = now

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📍 Oferta próxima: $titulo ($distanciaFormatada)")
            .setContentText(descricao)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$descricao\n\nToque para ver os detalhes no app Divulga!")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(
                NOTIFICATION_ID_BASE + (publicacaoId.hashCode() and 0x7FFFFFFF) % 1000,
                builder.build()
            )
        }
    }
}
