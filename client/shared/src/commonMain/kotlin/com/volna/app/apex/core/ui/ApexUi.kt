package com.volna.app.apex.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.volna.app.apex.core.error.UiError
import com.volna.app.apex.domain.model.BookingStatus
import com.volna.app.apex.domain.model.RideSlotStatus

@Composable
fun ApexScreen(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable Column.() -> Unit,
) {
    Surface(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            content()
        }
    }
}

@Composable
fun ApexTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(error)
            }
        },
    )
}

@Composable
fun ApexPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.fillMaxWidth(),
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text(text)
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
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text)
    }
}

@Composable
fun ApexErrorBlock(
    error: UiError,
    onRetry: (() -> Unit)? = null,
    onPrimaryAction: (() -> Unit)? = null,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(error.title, fontWeight = FontWeight.Bold)
            Text(error.message)
            if (error.retryAvailable && onRetry != null) {
                ApexSecondaryButton(text = "Повторить", onClick = onRetry)
            }
            if (error.primaryActionTitle != null && onPrimaryAction != null) {
                ApexPrimaryButton(text = error.primaryActionTitle, onClick = onPrimaryAction)
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    actionTitle: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(message)
            if (actionTitle != null && onAction != null) {
                Spacer(Modifier.height(4.dp))
                ApexSecondaryButton(actionTitle, onAction)
            }
        }
    }
}

@Composable
fun StatusBadge(text: String) {
    Card {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

fun RideSlotStatus.title(): String = when (this) {
    RideSlotStatus.AVAILABLE -> "Есть места"
    RideSlotStatus.NO_FREE_PLACES -> "Мест нет"
    RideSlotStatus.CANCELLED -> "Отменён"
    RideSlotStatus.UNKNOWN -> "Неизвестно"
}

fun BookingStatus.title(): String = when (this) {
    BookingStatus.PENDING_CONFIRMATION -> "Ожидает подтверждения"
    BookingStatus.ACTIVE -> "Активна"
    BookingStatus.CANCELLED_BY_CLIENT -> "Отменена вами"
    BookingStatus.CANCELLED_BY_CENTER -> "Отменена центром"
    BookingStatus.REJECTED_BY_CENTER -> "Отклонена центром"
    BookingStatus.COMPLETED -> "Завершена"
    BookingStatus.NO_SHOW -> "Неявка"
    BookingStatus.UNKNOWN -> "Неизвестно"
}

@Composable
fun KeyValue(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
        )
    }
}
