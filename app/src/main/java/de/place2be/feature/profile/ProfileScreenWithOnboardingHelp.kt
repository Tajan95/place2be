package de.place2be.feature.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.place2be.ui.theme.LeafSurface
import de.place2be.ui.theme.Moss

/**
 * Erweitert die bestehende Profilseite um einen dauerhaften Zugang zum
 * ausfuehrlichen Onboarding-/Hilfefluss. Die bisherige kompakte Hilfe in den
 * Profilaktionen bleibt als schnelle Score-Erklaerung bestehen; der runde
 * Fragezeichen-Button oeffnet das vollstaendige Tutorial erneut.
 *
 * Oeffentliche Profile erhalten diesen privaten Hilfezugang weiterhin nicht.
 */
@Composable
fun ProfileScreen(
    profile: ProfileUiState,
    onBack: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        ProfileScreen(
            profile = profile,
            onBack = onBack,
            modifier = Modifier.fillMaxSize(),
        )

        if (profile.isOwnProfile) {
            Surface(
                onClick = onHelpClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 14.dp, end = 20.dp)
                    .size(44.dp),
                shape = CircleShape,
                color = LeafSurface,
                contentColor = Moss,
                border = androidx.compose.foundation.BorderStroke(1.dp, Moss.copy(alpha = 0.45f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
