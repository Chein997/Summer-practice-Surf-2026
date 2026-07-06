package com.volna.app.apex.feature.push

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.volna.app.apex.design.ApexColors
import com.volna.app.apex.design.ApexPrimaryButton
import com.volna.app.apex.design.ApexScreen
import com.volna.app.apex.design.ApexSecondaryButton
import com.volna.app.apex.design.ShadowCard

@Composable
fun PushPermissionScreen(
    isLoading: Boolean,
    onAllowClicked: () -> Unit,
    onSkipClicked: () -> Unit,
) {
    ApexScreen(screenCode = "SCR-013", title = "Уведомления") {
        Spacer(Modifier.height(150.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(258.dp)
                    .clip(RoundedCornerShape(52.dp))
                    .background(ApexColors.White),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("▯", fontSize = 76.sp, fontWeight = FontWeight.ExtraBold)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(ApexColors.Accent)
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                    ) {
                        Text("Push", fontSize = 16.sp)
                    }
                }
            }
            Spacer(Modifier.height(58.dp))
            Text(
                text = "Получать статусы броней",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = "Мы будем присылать уведомления, когда центр подтвердит или отменит бронь.",
                modifier = Modifier.fillMaxWidth(0.82f),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(Modifier.height(100.dp))
        ShadowCard {
            Text("Пример уведомления", style = MaterialTheme.typography.bodyLarge)
            Text("Бронь подтверждена", style = MaterialTheme.typography.titleLarge)
            Text("Заезд завтра в 20:00", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.height(104.dp))
        ApexPrimaryButton(
            text = "Включить уведомления",
            onClick = onAllowClicked,
            enabled = !isLoading,
            loading = isLoading,
        )
        Spacer(Modifier.height(16.dp))
        ApexSecondaryButton(
            text = "Позже",
            onClick = onSkipClicked,
            enabled = !isLoading,
        )
    }
}
