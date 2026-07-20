package de.place2be.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.place2be.domain.model.UserScoreResult
import de.place2be.ui.theme.AccessibilityGold
import de.place2be.ui.theme.DarkInk
import de.place2be.ui.theme.LeafAccent
import de.place2be.ui.theme.LeafSurface
import de.place2be.ui.theme.Moss
import de.place2be.ui.theme.PureWhite
import de.place2be.ui.theme.SafetyBlue
import de.place2be.ui.theme.SheetHandle
import de.place2be.ui.theme.WarmSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Datenschutzbewusste Profiloberfläche.
 *
 * Eigene Profile zeigen die vollständige private Historie sowie Hilfe und
 * Einstellungen. Öffentliche Profile beschränken sich auf aggregierte Werte.
 */
@Composable
fun ProfileScreen(
    profile: ProfileUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeDialogName by rememberSaveable { mutableStateOf<String?>(null) }
    val activeDialog = activeDialogName?.let(ProfileDialog::valueOf)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = WarmSurface,
        contentColor = DarkInk,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ProfileTopBar(
                    isOwnProfile = profile.isOwnProfile,
                    onBack = onBack,
                )
            }
            item { ProfileIdentityCard(profile) }
            item { ProfileScoreCard(profile.score, showDetailedBreakdown = profile.isOwnProfile) }
            item { ProfileStatistics(profile) }

            if (profile.isOwnProfile) {
                item {
                    ProfileActions(
                        onHelpClick = { activeDialogName = ProfileDialog.HELP.name },
                        onSettingsClick = { activeDialogName = ProfileDialog.SETTINGS.name },
                    )
                }
                item {
                    Column {
                        Text(
                            text = "Deine Bewertungs-Historie",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Nur für dich sichtbar · Neueste Einträge zuerst",
                            color = LeafAccent,
                            fontSize = 12.sp,
                        )
                    }
                }

                if (profile.history.isEmpty()) {
                    item { EmptyHistoryCard() }
                } else {
                    items(
                        items = profile.history,
                        key = { historyItem -> historyItem.reviewUuid.toString() },
                    ) { historyItem ->
                        ProfileHistoryCard(historyItem)
                    }
                }
            } else {
                item { PublicProfilePrivacyCard() }
            }
        }
    }

    activeDialog?.let { dialog ->
        ProfileInfoDialog(
            dialog = dialog,
            onDismiss = { activeDialogName = null },
        )
    }
}

@Composable
private fun ProfileTopBar(
    isOwnProfile: Boolean,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onBack,
            shape = CircleShape,
            color = LeafSurface,
            contentColor = Moss,
            modifier = Modifier.size(44.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Zurück",
                modifier = Modifier.padding(12.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isOwnProfile) "Dein Profil" else "Community-Profil",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (isOwnProfile) "Privat und nur für dich vollständig sichtbar" else "Datenschutzbewusst zusammengefasst",
                color = DarkInk.copy(alpha = 0.62f),
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun ProfileIdentityCard(profile: ProfileUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = LeafSurface.copy(alpha = 0.78f),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = Moss,
                contentColor = PureWhite,
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = profile.profileInitial,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.displayName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(5.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (profile.isOwnProfile) WarmSurface else SafetyBlue.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = if (profile.isOwnProfile) "Privates Profil" else "Öffentliche Zusammenfassung",
                        color = if (profile.isOwnProfile) LeafAccent else SafetyBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileScoreCard(
    score: UserScoreResult,
    showDetailedBreakdown: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Moss,
        contentColor = PureWhite,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Nutzer-Score",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite.copy(alpha = 0.78f),
            )
            Text(
                text = score.totalScore.toString(),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ScoreComponentCard(
                    label = "Aktivität",
                    value = score.activityPoints,
                    modifier = Modifier.weight(1f),
                )
                ScoreComponentCard(
                    label = "Reputation",
                    value = score.reputationPoints,
                    modifier = Modifier.weight(1f),
                )
            }

            if (showDetailedBreakdown) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Deine Aktivität im Detail",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite.copy(alpha = 0.82f),
                )
                Spacer(Modifier.height(7.dp))
                ScoreBreakdownRow("Ortsbewertungen", score.ratingActivityPoints)
                ScoreBreakdownRow("Textrezensionen", score.textReviewActivityPoints)
                ScoreBreakdownRow("Community-Reaktionen", score.reactionActivityPoints)
            }
        }
    }
}

@Composable
private fun ScoreComponentCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(17.dp),
        color = PureWhite.copy(alpha = 0.13f),
        contentColor = PureWhite,
    ) {
        Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp)) {
            Text(label, fontSize = 11.sp, color = PureWhite.copy(alpha = 0.76f))
            Text(value.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ScoreBreakdownRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = PureWhite.copy(alpha = 0.78f),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "+$value",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ProfileStatistics(profile: ProfileUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProfileStatistic(
            value = profile.reviewCount,
            label = "Bewertungen",
            modifier = Modifier.weight(1f),
        )
        ProfileStatistic(
            value = profile.textReviewCount,
            label = "mit Text",
            modifier = Modifier.weight(1f),
        )
        ProfileStatistic(
            value = profile.helpfulReactionCount,
            label = "hilfreich",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ProfileStatistic(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(17.dp),
        color = LeafSurface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Moss)
            Text(label, fontSize = 10.sp, color = DarkInk.copy(alpha = 0.62f), maxLines = 1)
        }
    }
}

@Composable
private fun ProfileActions(
    onHelpClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ProfileActionCard(
            symbol = "?",
            title = "Hilfe",
            subtitle = "App & Score erklärt",
            onClick = onHelpClick,
            modifier = Modifier.weight(1f),
        )
        ProfileActionCard(
            symbol = "⚙",
            title = "Einstellungen",
            subtitle = "Für Ausbau vorbereitet",
            onClick = onSettingsClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ProfileActionCard(
    symbol: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = WarmSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SheetHandle),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(symbol, color = Moss, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 10.sp, color = DarkInk.copy(alpha = 0.58f))
        }
    }
}

@Composable
private fun ProfileHistoryCard(item: ProfileHistoryItemUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LeafSurface.copy(alpha = 0.55f),
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.placeName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.locationHint,
                        color = DarkInk.copy(alpha = 0.58f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = formatProfileDate(item.timestampMillis),
                    color = DarkInk.copy(alpha = 0.54f),
                    fontSize = 10.sp,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HistoryRatingPill("Vibes", item.vibe, LeafAccent, Modifier.weight(1f))
                HistoryRatingPill("Sicher", item.safety, SafetyBlue, Modifier.weight(1f))
                HistoryRatingPill("Erreichbar", item.accessibility, AccessibilityGold, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = item.text ?: "Ohne Textrezension",
                color = if (item.text == null) DarkInk.copy(alpha = 0.48f) else DarkInk.copy(alpha = 0.82f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun HistoryRatingPill(
    label: String,
    value: Int,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(13.dp),
        color = accentColor.copy(alpha = 0.12f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, color = DarkInk.copy(alpha = 0.62f), fontSize = 9.sp, maxLines = 1)
            Text(value.toString(), color = accentColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LeafSurface.copy(alpha = 0.48f),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Noch keine Bewertungen", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Sobald du einen Ort bewertest, erscheint der Eintrag hier in deiner privaten Historie.",
                color = DarkInk.copy(alpha = 0.62f),
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun PublicProfilePrivacyCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SafetyBlue.copy(alpha = 0.10f),
        border = androidx.compose.foundation.BorderStroke(1.dp, SafetyBlue.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Text(
                text = "Privatsphäre geschützt",
                color = SafetyBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "Bewertungen bleiben am jeweiligen Ort sichtbar. Eine chronologische Orts- oder Bewegungshistorie wird in öffentlichen Profilen nicht zusammengeführt.",
                color = DarkInk.copy(alpha = 0.7f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
        }
    }
}

@Composable
private fun ProfileInfoDialog(
    dialog: ProfileDialog,
    onDismiss: () -> Unit,
) {
    val title: String
    val body: String
    when (dialog) {
        ProfileDialog.HELP -> {
            title = "Hilfe & Hinweise"
            body = "Bewerte Orte nach Vibes, Sicherheit und Erreichbarkeit. Neue Eindrücke zählen stärker als alte. Dein Nutzer-Score setzt sich aus Aktivität und Reputation zusammen; wiederholte oder massenhafte Aktionen werden gegen Score-Farming begrenzt."
        }

        ProfileDialog.SETTINGS -> {
            title = "Einstellungen"
            body = "Der Einstellungsbereich ist für den Local-first-MVP vorbereitet. Später können hier beispielsweise Datenschutz-, Darstellungs- und Kontoeinstellungen ergänzt werden, ohne das Dashboard mit einem Zahnrad zu überladen."
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(body, lineHeight = 20.sp) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Verstanden", color = Moss, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = WarmSurface,
    )
}

private fun formatProfileDate(timestampMillis: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date(timestampMillis))

private enum class ProfileDialog {
    HELP,
    SETTINGS,
}
