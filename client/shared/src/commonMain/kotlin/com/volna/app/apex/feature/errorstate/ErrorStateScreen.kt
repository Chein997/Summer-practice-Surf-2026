package com.volna.app.apex.feature.errorstate

import androidx.compose.runtime.Composable
import com.volna.app.apex.core.error.UiError
import com.volna.app.apex.core.ui.ApexErrorBlock
import com.volna.app.apex.core.ui.ApexScreen
import com.volna.app.apex.core.ui.ApexSecondaryButton

@Composable
fun ErrorStateScreen(
    error: UiError,
    onRetry: (() -> Unit)? = null,
    onPrimaryAction: (() -> Unit)? = null,
    onBack: () -> Unit,
) {
    ApexScreen(title = "Ошибка") {
        ApexErrorBlock(
            error = error,
            onRetry = onRetry,
            onPrimaryAction = onPrimaryAction,
        )
        ApexSecondaryButton(
            text = "Назад",
            onClick = onBack,
        )
    }
}
