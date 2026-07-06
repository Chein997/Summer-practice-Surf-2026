package com.volna.app.apex.feature.errorstate

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.volna.app.apex.core.error.UiError
import com.volna.app.apex.design.ActionErrorBlock
import com.volna.app.apex.design.ApexColors
import com.volna.app.apex.design.ApexPrimaryButton
import com.volna.app.apex.design.ApexScreen
import com.volna.app.apex.design.ApexSecondaryButton
import com.volna.app.apex.design.SuccessCircle

@Composable
fun ErrorStateScreen(
    error: UiError,
    onRefreshSchedule: () -> Unit,
    onBack: () -> Unit,
) {
    ApexScreen(screenCode = "SCR-012", title = "Не удалось выполнить") {
        Spacer(Modifier.height(140.dp))
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        ) {
            SuccessCircle(symbol = "!", background = ApexColors.ErrorSoft, symbolColor = ApexColors.Error)
            Spacer(Modifier.height(68.dp))
            Text(
                text = "Действие недоступно",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = "Слот уже занят или отменён центром. Обновите список и выберите другой заезд.",
                modifier = Modifier.fillMaxWidth(0.82f),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(Modifier.height(94.dp))
        ActionErrorBlock(error)
        Spacer(Modifier.height(92.dp))
        ApexPrimaryButton(
            text = "Обновить расписание",
            onClick = onRefreshSchedule,
        )
        Spacer(Modifier.height(16.dp))
        ApexSecondaryButton(
            text = "Назад",
            onClick = onBack,
        )
    }
}
