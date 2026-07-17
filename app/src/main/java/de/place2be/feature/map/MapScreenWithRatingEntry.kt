package de.place2be.feature.map

import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.place2be.domain.model.Review
import de.place2be.domain.model.ReviewReactionType
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
import kotlin.math.ceil
import kotlin.math.ln

/**
 * Integrationsschicht für die zusammenhängende Map-/Detail-/Bewertungs-UX.
 *
 * Die kompakte Ortsvorschau bleibt als Peek-Zustand über der Karte sichtbar.
 * Erst nach dem Hochziehen erscheinen Schnellbewertung, optionales Textfeld und
 * bestehende Rezensionen. Damit gibt es keinen konkurrierenden zweiten Button
 * und keine separate Bewertungsseite im regulären App-Flow.
 *
 * Das Herz in der Ortszusammenfassung ist eine echte Bookmark-Aktion. Der
 * persistierte Zustand wird von der App-Schicht erneut eingelesen und bleibt so
 * mit dem Dashboard-Shortcut „Gespeichert“ synchron.
 *
 * Review-Reaktionen sind accountgebunden. Die aktuell gewählte Reaktion wird
 * visuell hervorgehoben; eigene Rezensionen bleiben bewusst nicht bewertbar.
 * Autor-Icon und Name öffnen über einen UUID-Callback das eigene oder ein
 * datenschutzbewusst begrenztes öffentliches Profil.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MapScreenWithRatingEntry(
    places: List<MapPlaceUiState>,
    selectedPlaceUuid: UUID?,
    reviewsForSelectedPlace: List<Review>,
    reviewAuthorNames: Map<UUID, String>,
    currentUserReactionTypes: Map<UUID, ReviewReactionType?>,
    currentUserUuid: UUID,
    ratingCooldownRemainingMillis: Long,
    onPlaceSelected: (UUID) -> Unit,
    onSelectionCleared: () -> Unit,
    onBookmarkToggle: (placeUuid: UUID, bookmarked: Boolean) -> Unit,
    onReviewReactionToggle: (reviewUuid: UUID, type: ReviewReactionType) -> Unit,
    onReviewAuthorSelected: (userUuid: UUID) -> Unit,
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
    val detailScrollState = rememberScrollState()

    LaunchedEffect(selectedPlaceUuid) {
        detailScrollState.scrollTo(0)
        if (selectedPlaceUuid != null) sheetState.partialExpand()
    }

    LaunchedEffect(sheetState.currentValue, selectedPlaceUuid) {
        if (selectedPlaceUuid != null && sheetState.currentValue == SheetValue.PartiallyExpanded) {
            detailScrollState.scrollTo(0)
        }
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
                    currentUserReactionTypes = currentUserReactionTypes,
                    currentUserUuid = currentUserUuid,
                    cooldownRemainingMillis = ratingCooldownRemainingMillis,
                    scrollState = detailScrollState,
                    onClose = onSelectionCleared,
                    onBookmarkToggle = onBookmarkToggle,
                    onReviewReactionToggle = onReviewReactionToggle,
                    onReviewAuthorSelected = onReviewAuthorSelected,
                    onSubmitRating = onSubmitRating,
                )
            }
        },
    ) { _ ->
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
    currentUserReactionTypes: Map<UUID, ReviewReactionType?>,
    currentUserUuid: UUID,
    cooldownRemainingMillis: Long,
    scrollState: ScrollState,
    onClose: () -> Unit,
    onBookmarkToggle: (UUID, Boolean) -> Unit,
    onReviewReactionToggle: (UUID, ReviewReactionType) -> Unit,
    onReviewAuthorSelected: (UUID) -> Unit,
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
    val cooldownActive = place.canRate && cooldownRemainingMillis > 0L
    val canSubmitRating = place.canRate && !cooldownActive
    val nowMillis = remember(reviews, reviewSort) { System.currentTimeMillis() }
    val visibleReviews = remember(reviews, reviewSort, nowMillis) {
        reviews
            .filter { !it.text.isNullOrBlank() }
            .let { textReviews ->
                when (reviewSort) {
                    ReviewSort.RECENT -> textReviews.sortedByDescending(Review::timestampMillis)
                    ReviewSort.POPULAR -> textReviews.sortedWith(
                        compareByDescending<Review> { reviewPopularityScore(it, nowMillis) }
                            .thenByDescending(Review::timestampMillis),
                    )
                }
            }
            .take(MAX_VISIBLE_TEXT_REVIEWS)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(PLACE_DETAIL_EXPANDED_HEIGHT)
            .navigationBarsPadding()
            .verticalScroll(scrollState)
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

        PlaceSummary(
            place = place,
            onBookmarkToggle = onBookmarkToggle,
        )
        Spacer(Modifier.height(12.dp))
        AggregatedRatings(place)
        Spacer(Modifier.height(10.dp))
        Text(
            text = when {
                !place.canRate -> place.ratingEligibilityMessage
                cooldownActive -> "Du hast diesen Ort heute bereits bewertet"
                else -> "Nach oben ziehen und direkt vor Ort bewerten"
            },
            color = if (place.canRate && !cooldownActive) LeafAccent else DarkInk.copy(alpha = 0.62f),
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.height(22.dp))
        Text(
            text = recentReviewCountLabel(place.recentReviewCount),
            color = DarkInk.copy(alpha = 0.62f),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(18.dp))
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
        if (cooldownActive) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Eine neue Bewertung ist in ${formatCooldownRemaining(cooldownRemainingMillis)} möglich.",
                color = AccessibilityGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(14.dp))

        InlineRatingSlider(
            label = "Vibes",
            criterion = RatingCriterion.VIBE,
            value = vibe,
            color = LeafAccent,
            enabled = canSubmitRating,
            onValueChanged = { vibe = it },
        )
        InlineRatingSlider(
            label = "Sicherheit",
            criterion = RatingCriterion.SAFETY,
            value = safety,
            color = SafetyBlue,
            enabled = canSubmitRating,
            onValueChanged = { safety = it },
        )
        InlineRatingSlider(
            label = "Erreichbarkeit",
            criterion = RatingCriterion.ACCESSIBILITY,
            value = accessibility,
            color = AccessibilityGold,
            enabled = canSubmitRating,
            onValueChanged = { accessibility = it },
        )

        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = canSubmitRating) {
                    textReviewExpanded = !textReviewExpanded
                },
            shape = RoundedCornerShape(15.dp),
            color = if (canSubmitRating) LeafSurface.copy(alpha = 0.65f) else DisabledSurface,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (textReviewExpanded) {
                        "Textrezension ausblenden"
                    } else {
                        "Textrezension hinzufügen (optional)"
                    },
                    color = DarkInk.copy(alpha = if (canSubmitRating) 1f else 0.55f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (textReviewExpanded) "⌃" else "⌄",
                    color = if (canSubmitRating) Moss else DarkInk.copy(alpha = 0.4f),
                    fontSize = 18.sp,
                )
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
                enabled = canSubmitRating,
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
            enabled = canSubmitRating,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Moss,
                disabledContainerColor = if (cooldownActive) {
                    AccessibilityGold.copy(alpha = 0.78f)
                } else {
                    DisabledSurface
                },
                disabledContentColor = if (cooldownActive) DarkInk else DarkInk.copy(alpha = 0.48f),
            ),
        ) {
            Text(
                text = when {
                    !place.canRate -> "Bewerten · nur vor Ort"
                    cooldownActive -> "Bewertung wieder in ${formatCooldownRemaining(cooldownRemainingMillis)} möglich"
                    else -> "Bewertung speichern"
                },
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
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Es werden höchstens $MAX_VISIBLE_TEXT_REVIEWS Textrezensionen angezeigt.",
            color = DarkInk.copy(alpha = 0.5f),
            fontSize = 10.sp,
        )
        Spacer(Modifier.height(10.dp))

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
                    isOwnReview = review.userUuid == currentUserUuid,
                    currentReaction = currentUserReactionTypes[review.uuid],
                    onAuthorClick = { onReviewAuthorSelected(review.userUuid) },
                    onReactionToggle = { type -> onReviewReactionToggle(review.uuid, type) },
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun PlaceSummary(
    place: MapPlaceUiState,
    onBookmarkToggle: (UUID, Boolean) -> Unit,
) {
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
        Surface(
            onClick = { onBookmarkToggle(place.uuid, !place.isBookmarked) },
            modifier = Modifier.padding(start = 4.dp),
            shape = RoundedCornerShape(50),
            color = Color.Transparent,
            contentColor = Moss,
        ) {
            Text(
                text = if (place.isBookmarked) "♥" else "♡",
                fontSize = 25.sp,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Composable
private fun AggregatedRatings(place: MapPlaceUiState) {
    if (!place.hasReviews) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = LeafSurface.copy(alpha = 0.55f),
        ) {
            Text(
                text = "Noch nicht bewertet.",
                color = DarkInk.copy(alpha = 0.68f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            )
        }
        return
    }

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
    color: Color,
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
    color: Color,
    enabled: Boolean,
    onValueChanged: (Int) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RatingCriterionIcon(criterion = criterion, color = color)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: $value",
            color = DarkInk.copy(alpha = if (enabled) 1f else 0.52f),
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
    isOwnReview: Boolean,
    currentReaction: ReviewReactionType?,
    onAuthorClick: () -> Unit,
    onReactionToggle: (ReviewReactionType) -> Unit,
) {
    var expanded by rememberSaveable(review.uuid.toString()) { mutableStateOf(false) }
    val reviewText = review.text.orEmpty()
    val authorInitial = authorName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = LeafSurface.copy(alpha = 0.48f),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onAuthorClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(15.dp),
                    color = Color.Transparent,
                    contentColor = DarkInk,
                ) {
                    Row(
                        modifier = Modifier.padding(end = 8.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier.size(30.dp),
                            shape = CircleShape,
                            color = Moss,
                            contentColor = WarmSurface,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = authorInitial,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isOwnReview) "$authorName · Du" else authorName,
                                color = DarkInk,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = if (isOwnReview) "Dein Profil ansehen" else "Öffentliches Profil ansehen",
                                color = LeafAccent,
                                fontSize = 9.sp,
                            )
                        }
                    }
                }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                ReviewReactionButton(
                    symbol = "👍",
                    count = review.likes,
                    selected = currentReaction == ReviewReactionType.LIKE,
                    enabled = !isOwnReview,
                    selectedColor = Moss,
                    onClick = { onReactionToggle(ReviewReactionType.LIKE) },
                )
                Spacer(Modifier.width(8.dp))
                ReviewReactionButton(
                    symbol = "👎",
                    count = review.dislikes,
                    selected = currentReaction == ReviewReactionType.DISLIKE,
                    enabled = !isOwnReview,
                    selectedColor = AccessibilityGold,
                    onClick = { onReactionToggle(ReviewReactionType.DISLIKE) },
                )
            }
            if (isOwnReview) {
                Spacer(Modifier.height(5.dp))
                Text(
                    text = "Eigene Rezensionen können nicht bewertet werden.",
                    color = DarkInk.copy(alpha = 0.48f),
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun ReviewReactionButton(
    symbol: String,
    count: Int,
    selected: Boolean,
    enabled: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) selectedColor.copy(alpha = 0.18f) else Color.Transparent,
        contentColor = if (selected) selectedColor else DarkInk.copy(alpha = if (enabled) 0.65f else 0.35f),
    ) {
        Text(
            text = "$symbol $count",
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

/**
 * Vorläufiger Popularitätswert für Textrezensionen.
 *
 * Netto-Reaktionen werden durch eine logarithmisch wachsende Altersstrafe
 * geteilt. Die Strafe wächst anfangs merklich, flacht später aber ab. Dadurch
 * können aktuelle populäre Meinungen aufsteigen, ohne dass ältere hilfreiche
 * Rezensionen zwangsläufig vollständig bedeutungslos werden.
 */
private fun reviewPopularityScore(review: Review, nowMillis: Long): Double {
    val netPositiveReactions = (review.likes - review.dislikes).coerceAtLeast(0).toDouble()
    val ageDays = ((nowMillis - review.timestampMillis).coerceAtLeast(0L) / MILLIS_PER_DAY.toDouble())
    val logarithmicAgePenalty = 1.0 + ln(1.0 + ageDays) / POPULARITY_AGE_PENALTY_SCALE
    return netPositiveReactions / logarithmicAgePenalty
}

private fun recentReviewCountLabel(recentReviewCount: Int): String = when (recentReviewCount) {
    0 -> "Im letzten Jahr noch keine Bewertungen"
    1 -> "Im letzten Jahr insgesamt 1 Bewertung"
    else -> "Im letzten Jahr insgesamt $recentReviewCount Bewertungen"
}

private fun formatCooldownRemaining(remainingMillis: Long): String {
    val totalMinutes = ceil(remainingMillis.coerceAtLeast(1L) / MILLIS_PER_MINUTE.toDouble()).toLong()
    val hours = totalMinutes / MINUTES_PER_HOUR
    val minutes = totalMinutes % MINUTES_PER_HOUR
    return when {
        hours > 0 && minutes > 0 -> "$hours h $minutes min"
        hours > 0 -> "$hours h"
        else -> "${minutes.coerceAtLeast(1L)} min"
    }
}

private enum class ReviewSort {
    RECENT,
    POPULAR,
}

private val REVIEW_DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
private const val REVIEW_COLLAPSE_HINT_LENGTH = 120
private const val MAX_VISIBLE_TEXT_REVIEWS = 50
private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L
private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1_000L
private const val POPULARITY_AGE_PENALTY_SCALE = 4.0
private val PLACE_DETAIL_PEEK_HEIGHT = 270.dp
private val PLACE_DETAIL_EXPANDED_HEIGHT = 760.dp
