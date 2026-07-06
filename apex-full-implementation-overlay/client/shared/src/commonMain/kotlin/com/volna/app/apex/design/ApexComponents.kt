package com.volna.app.apex.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
    import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.volna.app.apex.core.error.UiError
import com.volna.app.apex.domain.model.BookingStatus
import com.volna.app.apex.domain.model.RideSlot
import com.volna.app.apex.domain.model.RideSlotStatus

@Composable
fun ApexScreen(
    screenCode: String,
    title: String,
    subtitle: String? = null,
    showBottomBar: Boolean = false,
    selectedTab: ApexTab = ApexTab.Rides,
    onRides: () -> Unit = {},
    onBookings: () -> Unit = {},
    onProfile: () -> Unit = {},
    content: @Composable Column.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexColors.Background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = ApexDimens.ScreenHorizontalPadding)
                .padding(top = ApexDimens.ScreenTopPadding)
                .padding(bottom = if (showBottomBar) 104.dp else 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            ScreenBadge(screenCode)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.headlineLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(28.dp))
            content()
        }

        if (showBottomBar) {
            ApexBottomBar(
                selected = selectedTab,
                onRides = onRides,
                onBookings = onBookings,
                onProfile = onProfile,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
fun ApexScreenWithBack(
    screenCode: String,
    title: String,
    onBack: () -> Unit,
    content: @Composable Column.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexColors.Background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = ApexDimens.ScreenHorizontalPadding)
                .padding(top = ApexDimens.ScreenTopPadding, bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            ScreenBadge(screenCode)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack)
                Spacer(Modifier.width(16.dp))
                Text(title, style = MaterialTheme.typography.headlineLarge)
            }
            Spacer(Modifier.height(28.dp))
            content()
        }
    }
}

@Composable
fun ScreenBadge(code: String) {
    Box(
        modifier = Modifier
            .clip(ApexShapes.Chip)
            .background(ApexColors.Black)
            .padding(horizontal = 34.dp, vertical = 8.dp),
    ) {
        Text(
            text = code,
            color = ApexColors.White,
            fontSize = 15.sp,
            lineHeight = 18.sp,
        )
    }
}

@Composable
fun BackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(ApexColors.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text("‹", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = ApexColors.Black)
    }
}

@Composable
fun ShadowCard(
    modifier: Modifier = Modifier,
    background: Color = ApexColors.Surface,
    content: @Composable Column.() -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 8.dp)
                .clip(ApexShapes.ScreenCard)
                .background(ApexColors.Shadow),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ApexShapes.ScreenCard)
                .background(background)
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
fun ApexPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(86.dp)
            .clip(ApexShapes.Button)
            .background(if (enabled) ApexColors.Black else ApexColors.Disabled)
            .clickable(enabled = enabled && !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(color = ApexColors.White)
        } else {
            Text(
                text = text,
                color = if (enabled) ApexColors.White else ApexColors.TextSecondary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun ApexSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(ApexShapes.Button)
            .background(ApexColors.White)
            .border(1.dp, ApexColors.Border, ApexShapes.Button)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (enabled) ApexColors.TextPrimary else ApexColors.TextTertiary,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ApexInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    enabled: Boolean = true,
    error: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(placeholder, color = ApexColors.TextTertiary)
            },
            enabled = enabled,
            singleLine = true,
            isError = error != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .border(2.dp, ApexColors.Black, ApexShapes.Input)
                .clip(ApexShapes.Input)
                .background(ApexColors.White),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ApexColors.White,
                unfocusedContainerColor = ApexColors.White,
                disabledContainerColor = ApexColors.White,
                errorContainerColor = ApexColors.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = ApexColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        if (error != null) {
            Text(error, color = ApexColors.Error, fontSize = 13.sp)
        }
    }
}

@Composable
fun ApexCheckRow(
    checked: Boolean,
    onToggle: () -> Unit,
    text: String,
    enabled: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(enabled = enabled, onClick = onToggle),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = ApexColors.White,
                uncheckedColor = ApexColors.Black,
                checkmarkColor = ApexColors.Black,
            ),
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = ApexColors.TextPrimary, fontSize = 16.sp)
    }
}

@Composable
fun Chip(
    text: String,
    color: Color = ApexColors.Accent,
    textColor: Color = ApexColors.TextPrimary,
) {
    Box(
        modifier = Modifier
            .clip(ApexShapes.Chip)
            .background(color)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SlotStatusChip(status: RideSlotStatus) {
    val (text, color, textColor) = when (status) {
        RideSlotStatus.AVAILABLE -> Triple("Есть места", ApexColors.Accent, ApexColors.TextPrimary)
        RideSlotStatus.NO_FREE_PLACES -> Triple("Мест нет", ApexColors.SurfaceMuted, ApexColors.Error)
        RideSlotStatus.CANCELLED -> Triple("Отменён", ApexColors.SurfaceMuted, ApexColors.TextSecondary)
        RideSlotStatus.UNKNOWN -> Triple("Неизвестно", ApexColors.SurfaceMuted, ApexColors.TextSecondary)
    }
    Chip(text, color, textColor)
}

@Composable
fun BookingStatusChip(status: BookingStatus) {
    val (text, color) = when (status) {
        BookingStatus.PENDING_CONFIRMATION -> "Ожидает" to ApexColors.WarningSoft
        BookingStatus.ACTIVE -> "Подтверждена" to ApexColors.AccentSoft
        BookingStatus.CANCELLED_BY_CLIENT -> "Отменена" to ApexColors.SurfaceMuted
        BookingStatus.CANCELLED_BY_CENTER -> "Отменена центром" to ApexColors.SurfaceMuted
        BookingStatus.REJECTED_BY_CENTER -> "Отклонена" to ApexColors.ErrorSoft
        BookingStatus.COMPLETED -> "Завершена" to ApexColors.AccentSoft
        BookingStatus.NO_SHOW -> "Неявка" to ApexColors.ErrorSoft
        BookingStatus.UNKNOWN -> "Неизвестно" to ApexColors.SurfaceMuted
    }
    Chip(text, color)
}

@Composable
fun TrackIllustration(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 90.dp else 166.dp)
            .clip(ApexShapes.Track)
            .background(ApexColors.TrackBlue),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.84f)
                .aspectRatio(4.3f),
        ) {
            val stroke = if (compact) 5.dp.toPx() else 8.dp.toPx()
            drawOval(
                color = ApexColors.Black,
                topLeft = Offset(stroke, size.height * 0.18f),
                size = Size(size.width - stroke * 2, size.height * 0.64f),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
fun KeyValueRow(
    label: String,
    value: String,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = ApexColors.TextSecondary, fontSize = 18.sp)
            Text(value, color = ApexColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ApexColors.Border),
        )
    }
}

@Composable
fun InfoPanel(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = ApexColors.SurfaceMuted,
    textColor: Color = ApexColors.TextPrimary,
) {
    ShadowCard(modifier = modifier, background = background) {
        Text(text, color = textColor, fontSize = 18.sp, lineHeight = 25.sp)
    }
}

@Composable
fun SuccessCircle(
    symbol: String,
    background: Color,
    symbolColor: Color = ApexColors.Black,
) {
    Box(
        modifier = Modifier
            .size(216.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = symbolColor, fontSize = 72.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun ActionErrorBlock(
    error: UiError,
) {
    ShadowCard(background = Color(0xFFFFEFF0)) {
        Text(
            text = "Без технических деталей: ${error.message}",
            color = ApexColors.Error,
            fontSize = 18.sp,
            lineHeight = 25.sp,
        )
    }
}

@Composable
fun SlotSummaryCard(slot: RideSlot) {
    ShadowCard {
        Text("Заезд", color = ApexColors.TextSecondary, fontSize = 18.sp)
        Text(
            text = "${slot.startAt} · ${slot.trackConfiguration.name}",
            color = ApexColors.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "1 место · ${slot.price.format()}",
            color = ApexColors.TextPrimary,
            fontSize = 18.sp,
        )
    }
}

enum class ApexTab {
    Rides,
    Bookings,
    Profile,
}

@Composable
fun ApexBottomBar(
    selected: ApexTab,
    onRides: () -> Unit,
    onBookings: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ApexDimens.BottomBarHeight)
            .clip(RoundedCornerShape(28.dp))
            .background(ApexColors.Black)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BottomTab("⌁", "Заезды", selected == ApexTab.Rides, onRides, Modifier.weight(1f))
        BottomTab("▣", "Брони", selected == ApexTab.Bookings, onBookings, Modifier.weight(1f))
        BottomTab("◌", "Профиль", selected == ApexTab.Profile, onProfile, Modifier.weight(1f))
    }
}

@Composable
private fun BottomTab(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(62.dp)
            .clip(ApexShapes.Chip)
            .background(if (selected) ApexColors.Accent else Color.Transparent)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(icon, color = if (selected) ApexColors.Black else ApexColors.White, fontSize = 18.sp)
        Text(label, color = if (selected) ApexColors.Black else ApexColors.White, fontSize = 15.sp)
    }
}

fun BookingStatus.titleForDetails(): String = when (this) {
    BookingStatus.PENDING_CONFIRMATION -> "Ожидает подтверждения"
    BookingStatus.ACTIVE -> "Подтверждена центром"
    BookingStatus.CANCELLED_BY_CLIENT -> "Отменена клиентом"
    BookingStatus.CANCELLED_BY_CENTER -> "Отменена центром"
    BookingStatus.REJECTED_BY_CENTER -> "Отклонена центром"
    BookingStatus.COMPLETED -> "Завершена"
    BookingStatus.NO_SHOW -> "Неявка"
    BookingStatus.UNKNOWN -> "Неизвестно"
}
