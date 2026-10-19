package com.example.appdivulga.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Paleta Oficial PromoNear
val PromoOrange = Color(0xFFFF6600)
val PromoOrangeDark = Color(0xFFE05500)
val PromoOrangeLight = Color(0xFFFF8533)
val PromoOrangeContainer = Color(0xFFFFF0E6)

val TrustBlue = Color(0xFF0F52BA)
val TrustBlueDark = Color(0xFF0A3A86)
val TrustBlueLight = Color(0xFF3375E0)
val TrustBlueContainer = Color(0xFFEBF2FF)

val NeutralBackground = Color(0xFFF8FAFC)
val NeutralSurface = Color(0xFFFFFFFF)
val NeutralSurfaceVariant = Color(0xFFF1F5F9)

val TextPrimaryDark = Color(0xFF0F172A)
val TextSecondaryMuted = Color(0xFF475569)
val TextLightOffWhite = Color(0xFFF8FAFC)

val PointsGold = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFEF4444)
val LikeHeartRed = Color(0xFFEC4899)
val SuccessGreen = Color(0xFF10B981)
val PausedStatusOrange = Color(0xFFF97316)

// Degradê Conexão PromoNear (Azul Confiança <-> Laranja Promoção)
val PromoNearGradient = Brush.horizontalGradient(
    colors = listOf(TrustBlue, PromoOrange)
)

val PromoNearSoftGradient = Brush.linearGradient(
    colors = listOf(TrustBlueContainer, PromoOrangeContainer)
)
