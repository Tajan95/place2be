package de.place2be.feature.map

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.place2be.domain.model.Review
import de.place2be.feature.rating.RatingUiState
import de.place2be.ui.component.RatingCriterion
import de.place2be.ui.component.RatingCriterionIcon
import de.place2be.ui.theme.AccessibilityGold
import de.place2be.ui.theme.DarkInk
import de.place2be.ui.theme.DisabledSurface
import de.place2be.ui.theme.LeafAccent
import de.place2be.ui.theme.LeafSurface
import de.place2be.ui.theme.Moss
import de.place2be.ui.theme.SafetyBlue
import de.place2be.ui.theme.SheetHandle
import de.place2be.ui.theme.WarmSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Integrationsschicht für die zusammenhängende Map-/Detail-/Bewertungs-UX.
 *
 * Die kompakte Ortsvorschau bleibt als Peek-Zustand über der Karte sichtbar.
 * Erst nach dem Hochziehen erscheinen Schnellbewertung, optionales Textfeld und
 * bestehende Rezensionen. Damit gibt es keinen konkurrierenden zweiten Button
 * und keine separate Bewertungsseite im regulären App-Flow.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MapScreenWithRatingEntry(
    places: List<MapPlaceUiState>,
    selectedPlaceUuid: UUID?,
    reviewsForSelectedPlace: List<Review>,
    reviewAuthorNames: Map<UUID, String>,
    onPlaceSelected: (UUID) -> Unit,
    onSelectionCleared: () -> Unit,
    onSubmitRating: (
        placeUuid: UUID,
        vibe: Int,
        safety: Int,
        accessibility: Int,
        text: String?,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedPlace = places.firstOrNull { it.uuid == selectedPlaceUuid }
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    LaunchedEffect(selectedPlaceUuid) {
        if (selectedPlaceUuid != null) sheetState.partialExpand()
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        modifier = modifier.fillMaxSize(),
        sheetPeekHeight = if (selectedPlace == null) 0.dp else PLACE_DETAIL_PEEK_HEIGHT,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = WarmSurface,
        sheetContentColor = DarkInk,
        sheetShadowElevation = 16.dp,
        sheetSwipeEnabled = selectedPlace != null,
        sheetDragHandle = if (selectedPlace != null) {
            { BottomSheetDefaults.DragHandle(color = SheetHandle) }
        } else {
            null
        },
        sheetContent = {
            if (selectedPlace == null) {
                Spacer(Modifier.height(1.dp))
            } else {
                InlinePlaceDetailSheet(
                    place = selectedPlace,
                    reviews = reviewsForSelectedPlace,
                    reviewAuthorNames = reviewAuthorNames,
                    onClose = onSelectionCleared,
                    onSubmitRating = onSubmitRating,
                )
            }
        },
    ) { _ ->
        // Die ursprüngliche Dashboard-Map bleibt unangetastet. Ihre eigene
        // Orts-Preview wird hier deaktiviert, weil die äußere Sheet-Schicht die
        // ausgewählte Ortsansicht vollständig übernimmt.
        MapScreen(
            places = places,
            selectedPlaceUuid = null,
            onPlaceSelected = onPlaceSelected,
            onSelectionCleared = onSelectionCleared,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun InlinePlaceDetailSheet(
    place: MapPlaceUiState,
    reviews: List<Review>,
    reviewAuthorNames: Map<UUID, String>,
    onClose: () -> Unit,
    onSubmitRating: (UUID, Int, Int, Int, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var vibe by rememberSaveable(place.uuid.toString()) { mutableStateOf(RatingUiState.DEFAULT_RATING) }
    var safety by rememberSaveable(place.uuid.toString()) { mutableStateOf(RatingUiState.DEFAULT_RATING) }
    var accessibility by rememberSaveable(place.uuid.toString()) { mutableStateOf(RatingUiState.DEFAULT_RATING) }
    var reviewText by rememberSaveable(place.uuid.toString()) { mutableStateOf("") }
    var textReviewExpanded by rememberSaveable(place.uuid.toString()) { mutableStateOf(false) }
    var reviewSortName by rememberSaveable(place.uuid.toString()) { mutableStateOf(ReviewSort.RECENT.name) }
    var saveConfirmationVisible by remember(place.uuid) { mutableStateOf(false) }
    val reviewSort = ReviewSort.valueOf(reviewSortName)
    val visibleReviews = remember(reviews, reviewSort) {
        reviews
            .filter { !it.text.isNullOrBlank() }
            .let { textReviews ->
                when (reviewSort) {
                    ReviewSort.RECENT -> textReviews.sortedByDescending(Review::timestampMillis)
                    ReviewSort.POPULAR -> textReviews.sortedWith(
                        compareByDescending<Review> { it.likes - it.dislikes }
                            .thenByDescending(Review::timestampMillis),
                    )
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(PLACE_DETAIL_EXPANDED_HEIGHT)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "×",
                color = DarkInk.copy(alpha = 0.65f),
                fontSize = 26.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(onClick = onClose)
                    .padding(horizontal = 4.dp),
            )
        }

        PlaceSummary(place)
        Spacer(Modifier.height(12.dp))
        AggregatedRatings(place)
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (place.canRate) {
                "Nach oben ziehen und direkt vor Ort bewerten"
            } else {
                place.ratingEligibilityMessage
            },
            color = if (place.canRate) LeafAccent else DarkInk.copy(alpha = 0.62f),
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        // Alles ab hier liegt unterhalb des Peek-Bereichs und wird erst beim
        // Hochziehen zur Detailansicht sichtbar.
        Spacer(Modifier.height(26.dp))
        Text(
            text = "Deine Bewertung",
            color = DarkInk,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Vibes beschreibt Atmosphäre, Stimmung und ob man dort gerne Zeit verbringt.",
            color = DarkInk.copy(alpha = 0.68f),
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(14.dp))

        InlineRatingSlider(
            label = "Vibes",
            criterion = RatingCriterion.VIBE,
            value = vibe,
            color = LeafAccent,
            enabled = place.canRate,
            onValueChanged = { vibe = it },
        )
        InlineRatingSlider(
            label = "Sicherheit",
            criterion = RatingCriterion.SAFETY,
            value = safety,
            color = SafetyBlue,
            enabled = place.canRate,
            onValueChanged = { safety = it },
        )
        InlineRatingSlider(
            label = "Erreichbarkeit",
            criterion = RatingCriterion.ACCESSIBILITY,
            value = accessibility,
            color = AccessibilityGold,
            enabled = place.canRate,
            onValueChanged = { accessibility = it },
        )

        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = place.canRate) {
                    textReviewExpanded = !textReviewExpanded
                },
            shape = RoundedCornerShape(15.dp),
            color = LeafSurface.copy(alpha = 0.65f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (textReviewExpanded) "Textrezension ausblenden" else "Textrezension hinzufügen (optional)",
                    color = DarkInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(if (textReviewExpanded) "⌃" else "⌄", color = Moss, fontSize = 18.sp)
            }
        }

        if (textReviewExpanded) {
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = reviewText,
                onValueChange = { newText ->
                    if (newText.length <= RatingUiState.MAX_REVIEW_TEXT_LENGTH) reviewText = newText
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = place.canRate,
                label = { Text("Kurze Rezension") },
                placeholder = { Text("Was sollten andere über diesen Ort wissen?") },
                supportingText = {
                    Text("${reviewText.length}/${RatingUiState.MAX_REVIEW_TEXT_LENGTH} Zeichen")
                },
                minLines = 3,
                maxLines = 5,
            )
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                onSubmitRating(
                    place.uuid,
                    vibe,
                    safety,
                    accessibility,
                    reviewText.trim().takeIf(String::isNotEmpty),
                )
                reviewText = ""
                textReviewExpanded = false
                saveConfirmationVisible = true
            },
            enabled = place.canRate,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Moss,
                disabledContainerColor = DisabledSurface,
                disabledContentColor = DarkInk.copy(alpha = 0.48f),
            ),
        ) {
            Text(
                text = if (place.canRate) "Bewertung speichern" else "Bewerten · nur vor Ort",
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (saveConfirmationVisible) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Gespeichert – deine Rezension erscheint direkt unten.",
                color = LeafAccent,
                fontSize = 12.sp,
            )
        }

        Spacer(Modifier.height(24.dp))
        PlaceInformation(place)
        Spacer(Modifier.height(26.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Rezensionen",
                color = DarkInk,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            ReviewSortButton(
                label = "Rezent",
                selected = reviewSort == ReviewSort.RECENT,
                onClick = { reviewSortName = ReviewSort.RECENT.name },
            )
            Spacer(Modifier.width(6.dp))
            ReviewSortButton(
                label = "Beliebt",
                selected = reviewSort == ReviewSort.POPULAR,
                onClick = { reviewSortName = ReviewSort.POPULAR.name },
            )
        }
        Spacer(Modifier.height(12.dp))

        if (visibleReviews.isEmpty()) {
            Text(
                text = "Noch keine Textrezensionen vorhanden.",
                color = DarkInk.copy(alpha = 0.62f),
                fontSize = 13.sp,
            )
        } else {
            visibleReviews.forEach { review ->
                ReviewCard(
                    review = review,
                    authorName = reviewAuthorNames[review.userUuid] ?: "Community-Mitglied",
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun PlaceSummary(place: MapPlaceUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier
                .width(104.dp)
                .height(74.dp),
            shape = RoundedCornerShape(14.dp),
            color = LeafSurface,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = place.categoryLabel.take(1),
                    color = Moss,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.name,
                color = DarkInk,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = place.description,
                color = DarkInk.copy(alpha = 0.7f),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${place.categoryLabel} · ${place.locationHint.substringAfterLast(", ")}",
                color = LeafAccent,
                fontSize = 10.sp,
                maxLines = 1,
            )
        }
        Text(
            text = if (place.isBookmarked) "♥" else "♡",
            color = Moss,
            fontSize = 25.sp,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun AggregatedRatings(place: MapPlaceUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        AggregatedRatingPill(
            criterion = RatingCriterion.VIBE,
            label = "Vibes",
            score = place.vibeScore,
            color = LeafAccent,
            modifier = Modifier.weight(1f),
        )
        AggregatedRatingPill(
            criterion = RatingCriterion.SAFETY,
            label = "Sicher",
            score = place.safetyScore,
            color = SafetyBlue,
            modifier = Modifier.weight(1f),
        )
        AggregatedRatingPill(
            criterion = RatingCriterion.ACCESSIBILITY,
            label = "Erreichbar",
            score = place.accessibilityScore,
            color = AccessibilityGold,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AggregatedRatingPill(
    criterion: RatingCriterion,
    label: String,
    score: Double,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RatingCriterionIcon(criterion = criterion, color = color)
            Spacer(Modifier.width(5.dp))
            Column {
                Text(label, color = DarkInk.copy(alpha = 0.72f), fontSize = 9.sp, maxLines = 1)
                Text(
                    text = String.format(Locale.GERMANY, "%.1f", score),
                    color = DarkInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun InlineRatingSlider(
    label: String,
    criterion: RatingCriterion,
    value: Int,
    color: androidx.compose.ui.graphics.Color,
    enabled: Boolean,
    onValueChanged: (Int) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RatingCriterionIcon(criterion = criterion, color = color)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: $value",
            color = DarkInk,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChanged(it.toInt().coerceIn(1, 5)) },
        enabled = enabled,
        valueRange = 1f..5f,
        steps = 3,
    )
}

@Composable
private fun PlaceInformation(place: MapPlaceUiState) {
    Text(
        text = "Über diesen Ort",
        color = DarkInk,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(7.dp))
    Text(
        text = place.description,
        color = DarkInk.copy(alpha = 0.74f),
        fontSize = 13.sp,
    )
    Spacer(Modifier.height(12.dp))
    Text("Standort", color = DarkInk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Text(place.locationHint, color = DarkInk.copy(alpha = 0.68f), fontSize = 12.sp)
    if (place.attributes.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        Text("Ausstattung & Eigenschaften", color = DarkInk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(
            text = place.attributes.joinToString(" · ") { it.name.replace('_', ' ').lowercase() },
            color = LeafAccent,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ReviewSortButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Moss else LeafSurface,
        contentColor = if (selected) WarmSurface else DarkInk,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ReviewCard(
    review: Review,
    authorName: String,
) {
    var expanded by rememberSaveable(review.uuid.toString()) { mutableStateOf(false) }
    val reviewText = review.text.orEmpty()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = LeafSurface.copy(alpha = 0.48f),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = authorName,
                    color = DarkInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = REVIEW_DATE_FORMAT.format(Date(review.timestampMillis)),
                    color = DarkInk.copy(alpha = 0.55f),
                    fontSize = 10.sp,
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = "Vibes ${review.vibe} · Sicherheit ${review.safety} · Erreichbarkeit ${review.accessibility}",
                color = LeafAccent,
                fontSize = 10.sp,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = reviewText,
                color = DarkInk.copy(alpha = 0.82f),
                fontSize = 13.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { expanded = !expanded },
            )
            if (reviewText.length > REVIEW_COLLAPSE_HINT_LENGTH) {
                Text(
                    text = if (expanded) "Weniger anzeigen" else "Mehr anzeigen",
                    color = Moss,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(top = 5.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "👍 ${review.likes}   👎 ${review.dislikes}",
                color = DarkInk.copy(alpha = 0.6f),
                fontSize = 11.sp,
            )
        }
    }
}

private enum class ReviewSort {
    RECENT,
    POPULAR,
}

private val REVIEW_DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
private const val REVIEW_COLLAPSE_HINT_LENGTH = 120
private val PLACE_DETAIL_PEEK_HEIGHT = 270.dp
private val PLACE_DETAIL_EXPANDED_HEIGHT = 760.dp
