package de.place2be.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.place2be.ui.theme.DarkInk
import de.place2be.ui.theme.LeafAccent
import de.place2be.ui.theme.LeafSurface
import de.place2be.ui.theme.Moss
import de.place2be.ui.theme.PureWhite
import de.place2be.ui.theme.SheetHandle
import de.place2be.ui.theme.WarmSurface

/**
 * Gemeinsamer, seitenweiser Erklärfluss für den ersten App-Start und die
 * später erneut aufrufbare Hilfe. Im Hilfe-Modus kann die Ansicht sofort wieder
 * geschlossen werden; beim ersten Start führt sie bewusst bis zur Karte.
 */
@Composable
fun OnboardingScreen(
    pages: List<OnboardingPageUiState>,
    mode: OnboardingMode,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (pages.isEmpty()) {
        onFinish()
        return
    }

    var currentPage by rememberSaveable(mode.name) { mutableStateOf(0) }
    val safePageIndex = currentPage.coerceIn(0, pages.lastIndex)
    val page = pages[safePageIndex]
    val isLastPage = safePageIndex == pages.lastIndex

    BackHandler {
        when {
            safePageIndex > 0 -> currentPage = safePageIndex - 1
            mode == OnboardingMode.HELP -> onFinish()
            else -> Unit
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = WarmSurface,
        contentColor = DarkInk,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (mode == OnboardingMode.FIRST_LAUNCH) "Willkommen" else "Hilfe & Hinweise",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (mode == OnboardingMode.FIRST_LAUNCH) {
                            "Vier kurze Schritte bis zur Karte"
                        } else {
                            "Die wichtigsten App-Regeln erneut erklärt"
                        },
                        color = DarkInk.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                    )
                }

                if (mode == OnboardingMode.HELP) {
                    TextButton(onClick = onFinish) {
                        Text("Schließen", color = Moss, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            OnboardingProgress(
                pageCount = pages.size,
                selectedPage = safePageIndex,
            )
            Spacer(Modifier.height(18.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(28.dp),
                color = LeafSurface.copy(alpha = 0.78f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SheetHandle.copy(alpha = 0.8f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(22.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(70.dp),
                        shape = CircleShape,
                        color = Moss,
                        contentColor = PureWhite,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = page.symbol,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = page.eyebrow,
                        color = LeafAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = page.title,
                        fontSize = 27.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = page.body,
                        color = DarkInk.copy(alpha = 0.74f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    )
                    Spacer(Modifier.height(20.dp))
                    page.highlights.forEach { highlight ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Surface(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(8.dp),
                                shape = CircleShape,
                                color = LeafAccent,
                            ) {}
                            Spacer(Modifier.size(10.dp))
                            Text(
                                text = highlight,
                                color = DarkInk.copy(alpha = 0.82f),
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (safePageIndex > 0) {
                    TextButton(onClick = { currentPage = safePageIndex - 1 }) {
                        Text("Zurück", color = Moss, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }

                if (safePageIndex > 0) Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinish()
                        } else {
                            currentPage = safePageIndex + 1
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Moss),
                ) {
                    Text(
                        text = when {
                            !isLastPage -> "Weiter"
                            mode == OnboardingMode.FIRST_LAUNCH -> "Karte öffnen"
                            else -> "Zurück zum Profil"
                        },
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingProgress(
    pageCount: Int,
    selectedPage: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            Surface(
                modifier = Modifier.size(if (index == selectedPage) 28.dp else 9.dp, 9.dp),
                shape = CircleShape,
                color = if (index == selectedPage) Moss else SheetHandle,
            ) {}
        }
    }
}
