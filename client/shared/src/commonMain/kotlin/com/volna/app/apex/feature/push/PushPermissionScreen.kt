package com.volna.app.apex.feature.push

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.volna.app.apex.core.ui.ApexPrimaryButton
import com.volna.app.apex.core.ui.ApexScreen
import com.volna.app.apex.core.ui.ApexSecondaryButton

@Composable
fun PushPermissionScreen(
    isLoading: Boolean,
    onAllowClicked: () -> Unit,
    onSkipClicked: () -> Unit,
) {
    ApexScreen(title = "Уведомления") {
        Text("Включите уведомления, чтобы получать подтверждения, отклонения, напоминания и информацию об отмене заезда центром.")
        Text("Если вы откажетесь, основной сценарий бронирования продолжит работать.")
        ApexPrimaryButton(
            text = "Разрешить уведомления",
            onClick = onAllowClicked,
            enabled = !isLoading,
            isLoading = isLoading,
        )
        ApexSecondaryButton(
            text = "Не сейчас",
            onClick = onSkipClicked,
            enabled = !isLoading,
        )
    }
}
