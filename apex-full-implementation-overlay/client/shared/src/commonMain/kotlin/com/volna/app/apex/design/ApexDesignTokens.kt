package com.volna.app.apex.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ApexColors {
    val Background = Color(0xFFF1ECE3)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFE9E1D4)
    val TextPrimary = Color(0xFF171717)
    val TextSecondary = Color(0xFF716D65)
    val TextTertiary = Color(0xFF9A9287)
    val Accent = Color(0xFFA7FF4F)
    val AccentSoft = Color(0xFFDDFDC6)
    val Warning = Color(0xFFFFE46B)
    val WarningSoft = Color(0xFFFFD76C)
    val Error = Color(0xFFFF433A)
    val ErrorSoft = Color(0xFFFFD2D0)
    val TrackBlue = Color(0xFFBFEFF7)
    val Disabled = Color(0xFFEDE5D8)
    val Border = Color(0xFFD8CEBF)
    val Shadow = Color(0xFFCFC5B3)
    val Black = Color(0xFF171717)
    val White = Color(0xFFFFFFFF)
}

object ApexDimens {
    val ScreenHorizontalPadding: Dp = 20.dp
    val ScreenTopPadding: Dp = 20.dp
    val CardRadius: Dp = 28.dp
    val ButtonRadius: Dp = 28.dp
    val ChipRadius: Dp = 999.dp
    val CardShadowOffset: Dp = 8.dp
    val LargeSpacing: Dp = 28.dp
    val MediumSpacing: Dp = 18.dp
    val SmallSpacing: Dp = 10.dp
    val BottomBarHeight: Dp = 78.dp
}

object ApexShapes {
    val ScreenCard = RoundedCornerShape(ApexDimens.CardRadius)
    val Button = RoundedCornerShape(ApexDimens.ButtonRadius)
    val Chip = RoundedCornerShape(ApexDimens.ChipRadius)
    val Input = RoundedCornerShape(18.dp)
    val Track = RoundedCornerShape(18.dp)
    val Circle = RoundedCornerShape(999.dp)
}

private val ApexColorScheme: ColorScheme = lightColorScheme(
    primary = ApexColors.Black,
    onPrimary = ApexColors.White,
    secondary = ApexColors.Accent,
    onSecondary = ApexColors.Black,
    background = ApexColors.Background,
    onBackground = ApexColors.TextPrimary,
    surface = ApexColors.Surface,
    onSurface = ApexColors.TextPrimary,
    error = ApexColors.Error,
    errorContainer = ApexColors.ErrorSoft,
    outline = ApexColors.Border,
)

private val ApexTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 36.sp,
        lineHeight = 42.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ApexColors.TextPrimary,
    ),
    headlineMedium = TextStyle(
        fontSize = 30.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ApexColors.TextPrimary,
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ApexColors.TextPrimary,
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ApexColors.TextPrimary,
    ),
    titleMedium = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        color = ApexColors.TextPrimary,
    ),
    bodyLarge = TextStyle(
        fontSize = 18.sp,
        lineHeight = 25.sp,
        fontWeight = FontWeight.Normal,
        color = ApexColors.TextSecondary,
    ),
    bodyMedium = TextStyle(
        fontSize = 15.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.Normal,
        color = ApexColors.TextSecondary,
    ),
    labelLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ApexColors.TextPrimary,
    ),
    labelMedium = TextStyle(
        fontSize = 13.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        color = ApexColors.TextPrimary,
    ),
)

@Composable
fun ApexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ApexColorScheme,
        typography = ApexTypography,
        content = content,
    )
}
