package de.place2be.feature.map

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.place2be.domain.model.PlaceAttribute
import de.place2be.domain.model.PlaceCategory
import de.place2be.ui.component.RatingCriterion
import de.place2be.ui.component.RatingCriterionIcon
import de.place2be.ui.theme.AccessibilityGold
import de.place2be.ui.theme.DarkInk
import de.place2be.ui.theme.DisabledSurface
import de.place2be.ui.theme.LeafAccent
import de.place2be.ui.theme.LeafSurface
import de.place2be.ui.theme.MapBlock
import de.place2be.ui.theme.MapCream
import de.place2be.ui.theme.MapRiver
import de.place2be.ui.theme.MapRoad
import de.place2be.ui.theme.MapRoadOutline
import de.place2be.ui.theme.Moss
import de.place2be.ui.theme.PureWhite
import de.place2be.ui.theme.SafetyBlue
import de.place2be.ui.theme.SheetHandle
import de.place2be.ui.theme.Terracotta
import de.place2be.ui.theme.ThumbnailGreen
import de.place2be.ui.theme.ThumbnailGround
import de.place2be.ui.theme.ThumbnailSand
import de.place2be.ui.theme.WarmSurface
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

/**
 * Interaktive MVP-Karte. Die Kartenzeichnung ist eine reine Presentation-
 * Komponente; Marker und Preview erhalten ausschliesslich aufbereitete Daten.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MapScreen(
    places: List<MapPlaceUiState>,
    selectedPlaceUuid: UUID?,
    onPlaceSelected: (UUID) -> Unit,
    onSelectionCleared: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var activePanelName by rememberSaveable { mutableStateOf(MapBottomPanel.DEFAULT.name) }
    var selectedCategoriesValue by rememberSaveable { mutableStateOf("") }
    var selectedAttributesValue by rememberSaveable { mutableStateOf("") }
    var viewportRevision by rememberSaveable { mutableStateOf(0) }
    val activePanel = MapBottomPanel.valueOf(activePanelName)
    val selectedCategoryNames = selectedCategoriesValue.toSelectionSet()
    val selectedAttributeNames = selectedAttributesValue.toSelectionSet()
    val visiblePlaces = places.filteredBy(
        MapFilterSelection(
            categoryNames = selectedCategoryNames,
            attributeNames = selectedAttributeNames,
        ),
    )
    val selectedPlace = places.firstOrNull { it.uuid == selectedPlaceUuid }
    val mapPlaces = if (selectedPlace != null && visiblePlaces.none { it.uuid == selectedPlace.uuid }) {
        visiblePlaces + selectedPlace
    } else {
        visiblePlaces
    }
    val activeFilterCount = selectedCategoryNames.size + selectedAttributeNames.size
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val sheetCanExpand = selectedPlace != null || activePanel != MapBottomPanel.DEFAULT
    val sheetPeekHeight = when {
        selectedPlace != null -> PLACE_PREVIEW_PEEK_HEIGHT
        activePanel == MapBottomPanel.DEFAULT -> DEFAULT_PANEL_PEEK_HEIGHT
        else -> LIST_PANEL_PEEK_HEIGHT
    }

    LaunchedEffect(activePanel, selectedPlaceUuid) {
        sheetState.partialExpand()
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        modifier = modifier.fillMaxSize(),
        sheetPeekHeight = sheetPeekHeight,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = WarmSurface,
        sheetContentColor = DarkInk,
        sheetShadowElevation = 16.dp,
        sheetSwipeEnabled = sheetCanExpand,
        sheetDragHandle = if (sheetCanExpand) {
            { BottomSheetDefaults.DragHandle(color = SheetHandle) }
        } else {
            null
        },
        sheetContent = {
            if (selectedPlace != null) {
                PlaceMiniPreview(
                    place = selectedPlace,
                    onClose = onSelectionCleared,
                )
            } else {
            when (activePanel) {
                MapBottomPanel.DEFAULT -> DefaultMapControls(
                    activeFilterCount = activeFilterCount,
                    onFilterClick = { activePanelName = MapBottomPanel.FILTER.name },
                    onPopularClick = { activePanelName = MapBottomPanel.POPULAR.name },
                    onSavedClick = { activePanelName = MapBottomPanel.SAVED.name },
                )

                MapBottomPanel.FILTER -> FilterSheet(
                    places = places,
                    visiblePlaceCount = visiblePlaces.size,
                    selectedCategoryNames = selectedCategoryNames,
                    selectedAttributeNames = selectedAttributeNames,
                    onCategoryToggle = { name ->
                        selectedCategoriesValue = selectedCategoriesValue.toggleSelection(name)
                        viewportRevision++
                    },
                    onAttributeToggle = { name ->
                        selectedAttributesValue = selectedAttributesValue.toggleSelection(name)
                        viewportRevision++
                    },
                    onReset = {
                        selectedCategoriesValue = ""
                        selectedAttributesValue = ""
                        viewportRevision++
                    },
                    onClose = { activePanelName = MapBottomPanel.DEFAULT.name },
                )

                MapBottomPanel.POPULAR -> PlaceListSheet(
                    title = "Beliebte Orte",
                    subtitle = "Nach Community-Score sortiert",
                    places = places.popularFirst(),
                    emptyMessage = "Noch keine beliebten Orte vorhanden.",
                    onPlaceSelected = onPlaceSelected,
                    onClose = { activePanelName = MapBottomPanel.DEFAULT.name },
                )

                MapBottomPanel.SAVED -> PlaceListSheet(
                    title = "Gespeicherte Orte",
                    subtitle = "Zuletzt gespeichert zuerst",
                    places = places.newestBookmarksFirst(),
                    emptyMessage = "Du hast noch keine Orte gespeichert.",
                    onPlaceSelected = onPlaceSelected,
                    onClose = { activePanelName = MapBottomPanel.DEFAULT.name },
                )
            }
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MockMapLayer(
                places = mapPlaces,
                focusedPlace = selectedPlace,
                viewportResetKey = viewportRevision,
                onPlaceSelected = onPlaceSelected,
            )
            MapHeader()
        }
    }
}

@Composable
private fun MockMapLayer(
    places: List<MapPlaceUiState>,
    focusedPlace: MapPlaceUiState?,
    viewportResetKey: Int,
    onPlaceSelected: (UUID) -> Unit,
) {
    val markerPositions = remember(places) { places.fitIntoMapViewport() }
    val focusedPosition = focusedPlace?.let { markerPositions[it.uuid] }
    var panOffset by remember(viewportResetKey, focusedPlace?.uuid) { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MapCream)
            .pointerInput(viewportResetKey, focusedPlace?.uuid) {
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Initial,
                    )
                    var previousPosition = down.position
                    var accumulatedDrag = Offset.Zero
                    var dragging = false

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break

                        val dragAmount = change.position - previousPosition
                        previousPosition = change.position
                        if (!dragging) {
                            accumulatedDrag += dragAmount
                            if (accumulatedDrag.getDistance() > viewConfiguration.touchSlop) {
                                dragging = true
                                panOffset += accumulatedDrag
                                change.consume()
                            }
                        } else {
                            panOffset += dragAmount
                            change.consume()
                        }
                    }
                }
            }
            .clipToBounds(),
    ) {
        val targetOffsetX = focusedPosition?.let { maxWidth * (MAP_FOCUS_X - it.x) } ?: 0.dp
        val targetOffsetY = focusedPosition?.let { maxHeight * (MAP_FOCUS_Y - it.y) } ?: 0.dp
        val animatedOffsetX by animateDpAsState(targetOffsetX, tween(MAP_MOVE_DURATION_MILLIS), label = "map-x")
        val animatedOffsetY by animateDpAsState(targetOffsetY, tween(MAP_MOVE_DURATION_MILLIS), label = "map-y")

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = animatedOffsetX.roundToPx() + panOffset.x.roundToInt(),
                        y = animatedOffsetY.roundToPx() + panOffset.y.roundToInt(),
                    )
                },
        ) {
            MockCityMap(modifier = Modifier.fillMaxSize())

            places.forEach { place ->
                val position = markerPositions.getValue(place.uuid)
                PlaceMarker(
                    place = place,
                    selected = place.uuid == focusedPlace?.uuid,
                    onClick = { onPlaceSelected(place.uuid) },
                    modifier = Modifier.offset(
                        x = maxWidth * position.x - 27.dp,
                        y = maxHeight * position.y - 36.dp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun MockCityMap(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(MapCream)) {
        drawRect(MapCream)

        val blockSize = size.width / 5f
        repeat(6) { row ->
            repeat(5) { column ->
                if ((row + column) % 4 != 0) {
                    drawRoundRect(
                        color = MapBlock.copy(alpha = 0.72f),
                        topLeft = Offset(column * blockSize + 13f, row * blockSize + 26f),
                        size = Size(blockSize * 0.7f, blockSize * 0.54f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(9f, 9f),
                    )
                }
            }
        }

        val river = Path().apply {
            moveTo(size.width * 0.76f, -20f)
            cubicTo(
                size.width * 0.64f,
                size.height * 0.26f,
                size.width * 0.86f,
                size.height * 0.58f,
                size.width * 0.68f,
                size.height + 20f,
            )
        }
        drawPath(river, MapRiver, style = Stroke(width = size.width * 0.075f, cap = StrokeCap.Round))

        val roadColor = MapRoad.copy(alpha = 0.96f)
        val roadWidth = size.width * 0.035f
        listOf(
            Pair(Offset(-40f, size.height * 0.24f), Offset(size.width + 60f, size.height * 0.74f)),
            Pair(Offset(-40f, size.height * 0.67f), Offset(size.width + 40f, size.height * 0.18f)),
            Pair(Offset(size.width * 0.18f, -30f), Offset(size.width * 0.36f, size.height + 30f)),
            Pair(Offset(-20f, size.height * 0.45f), Offset(size.width + 20f, size.height * 0.44f)),
        ).forEach { (start, end) ->
            drawLine(roadColor, start, end, strokeWidth = roadWidth, cap = StrokeCap.Round)
            drawLine(MapRoadOutline, start, end, strokeWidth = 2f, cap = StrokeCap.Round)
        }

        drawCircle(LeafSurface, radius = size.width * 0.19f, center = Offset(size.width * 0.2f, size.height * 0.18f))
        drawCircle(LeafAccent.copy(alpha = 0.16f), radius = size.width * 0.13f, center = Offset(size.width * 0.42f, size.height * 0.55f))
        repeat(22) { index ->
            val x = ((index * 97) % 100) / 100f * size.width
            val y = ((index * 53 + 17) % 100) / 100f * size.height
            drawCircle(LeafAccent.copy(alpha = 0.28f), radius = 5f + index % 3, center = Offset(x, y))
        }
    }
}

@Composable
private fun MapHeader() {
    Card(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = WarmSurface.copy(alpha = 0.97f)),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menü",
                tint = DarkInk,
                modifier = Modifier.padding(end = 8.dp)
            )
            Box(
                modifier = Modifier
                    .size(27.dp)
                    .clip(CircleShape)
                    .background(LeafAccent),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "↗",
                    color = PureWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(y = (-1).dp))
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "place2be",
                    color = DarkInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.6).sp,
                    modifier = Modifier.offset(y = 3.dp)
                )
                Text(text = "Deine Orte. Dein Wohlbefinden.", color = LeafAccent, fontSize = 10.sp)
            }
            Surface(
                shape = CircleShape,
                color = LeafSurface,
                border = androidx.compose.foundation.BorderStroke(2.dp, LeafAccent),
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("M", color = Moss, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun PlaceMarker(
    place: MapPlaceUiState,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val markerColor = when (place.category) {
        PlaceCategory.PARK -> LeafAccent
        PlaceCategory.PROMENADE -> SafetyBlue
        PlaceCategory.SQUARE -> AccessibilityGold
        else -> Terracotta
    }
    Row(
        modifier = modifier
            .semantics {
                contentDescription = buildString {
                    append(place.name)
                    append(", Vibes ${formatScore(place.vibeScore)} von 5")
                    append(", Sicherheit ${formatScore(place.safetyScore)} von 5")
                    append(", Erreichbarkeit ${formatScore(place.accessibilityScore)} von 5")
                }
                role = Role.Button
            }
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(55.dp, 66.dp)) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(size.width * 0.18f, size.height * 0.53f)
                    cubicTo(0f, size.height * 0.2f, size.width * 0.22f, 0f, size.width / 2f, 0f)
                    cubicTo(size.width * 0.78f, 0f, size.width, size.height * 0.2f, size.width * 0.82f, size.height * 0.53f)
                    close()
                }
                drawPath(path, markerColor)
                drawPath(path, PureWhite, style = Stroke(width = if (selected) 5f else 3f))
                drawCircle(PureWhite.copy(alpha = 0.92f), radius = size.width * 0.22f, center = Offset(size.width / 2f, size.height * 0.36f))
            }
            Text(
                text = categorySymbol(place.category),
                color = markerColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.offset(y = (-10).dp),
            )
        }
        Spacer(Modifier.width(4.dp))
        MarkerPlaceOverview(place = place, selected = selected)
    }
}

@Composable
private fun MarkerPlaceOverview(
    place: MapPlaceUiState,
    selected: Boolean,
) {
    Surface(
        color = if (selected) DarkInk else WarmSurface.copy(alpha = 0.96f),
        contentColor = if (selected) PureWhite else DarkInk,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) LeafAccent else MapRoadOutline,
        ),
        shadowElevation = if (selected) 8.dp else 4.dp,
        modifier = Modifier.width(148.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)) {
            Text(
                text = place.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MarkerRating(RatingCriterion.VIBE, place.vibeScore, LeafAccent)
                MarkerRating(RatingCriterion.SAFETY, place.safetyScore, SafetyBlue)
                MarkerRating(RatingCriterion.ACCESSIBILITY, place.accessibilityScore, AccessibilityGold)
            }
        }
    }
}

@Composable
private fun MarkerRating(
    criterion: RatingCriterion,
    score: Double,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RatingCriterionIcon(
            criterion = criterion,
            color = color,
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = formatScore(score),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DefaultMapControls(
    activeFilterCount: Int,
    onFilterClick: () -> Unit,
    onPopularClick: () -> Unit,
    onSavedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, top = 10.dp),
    ) {
        Text(
            text = "Was möchtest du entdecken?",
            color = DarkInk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        )
        Spacer(Modifier.height(10.dp))
        NavigationBar(
            containerColor = LeafSurface,
            contentColor = DarkInk,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            NavigationBarItem(
                selected = false,
                onClick = onFilterClick,
                icon = {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Filter",
                    )
                },
                label = {
                    Text(
                        text = if (activeFilterCount == 0) "Filtern" else "Filter ($activeFilterCount)",
                        fontSize = 12.sp,
                    )
                },
            )
            NavigationBarItem(
                selected = false,
                onClick = onPopularClick,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Beliebt",
                    )
                },
                label = {
                    Text(
                        text = "Beliebt",
                        fontSize = 12.sp,
                    )
                },
            )
            NavigationBarItem(
                selected = false,
                onClick = onSavedClick,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Gespeichert",
                    )
                },
                label = {
                    Text(
                        text = "Gespeichert",
                        fontSize = 12.sp,
                    )
                },
            )
        }
        if (activeFilterCount > 0) {
            Text(
                text = "$activeFilterCount Filter aktiv · Tippe zum Bearbeiten",
                color = LeafAccent,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 10.dp),
            )
        }
    }
}

@Composable
private fun FilterSheet(
    places: List<MapPlaceUiState>,
    visiblePlaceCount: Int,
    selectedCategoryNames: Set<String>,
    selectedAttributeNames: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onAttributeToggle: (String) -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = places
        .distinctBy(MapPlaceUiState::category)
        .sortedBy(MapPlaceUiState::categoryLabel)
    val attributes = places
        .flatMap { it.attributes }
        .distinct()
        .sortedBy(PlaceAttribute::toDisplayLabel)

    MapPanelCard(modifier = modifier) {
        MapPanelHeader(
            title = "Orte filtern",
            subtitle = "$visiblePlaceCount von ${places.size} Orten sichtbar",
            actionLabel = if (selectedCategoryNames.isEmpty() && selectedAttributeNames.isEmpty()) null else "Zurücksetzen",
            onAction = onReset,
            onClose = onClose,
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(FILTER_PANEL_CONTENT_HEIGHT),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 18.dp,
                end = 18.dp,
                bottom = 18.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterSectionTitle(
                    title = "Kategorien",
                    hint = "Mehrere Kategorien werden gemeinsam angezeigt",
                )
            }
            items(categories, key = { it.category.name }) { place ->
                FilterOptionRow(
                    label = place.categoryLabel,
                    selected = place.category.name in selectedCategoryNames,
                    onClick = { onCategoryToggle(place.category.name) },
                )
            }
            item {
                FilterSectionTitle(
                    title = "Ortseigenschaften",
                    hint = "Ein Ort muss alle gewählten Eigenschaften erfüllen",
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(attributes, key = PlaceAttribute::name) { attribute ->
                FilterOptionRow(
                    label = attribute.toDisplayLabel(),
                    selected = attribute.name in selectedAttributeNames,
                    onClick = { onAttributeToggle(attribute.name) },
                )
            }
        }
    }
}

@Composable
private fun PlaceListSheet(
    title: String,
    subtitle: String,
    places: List<MapPlaceUiState>,
    emptyMessage: String,
    onPlaceSelected: (UUID) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MapPanelCard(modifier = modifier) {
        MapPanelHeader(
            title = title,
            subtitle = subtitle,
            onClose = onClose,
        )
        if (places.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PLACE_LIST_CONTENT_HEIGHT),
                contentAlignment = Alignment.TopStart,
            ) {
                Text(
                    text = emptyMessage,
                    color = DarkInk.copy(alpha = 0.65f),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PLACE_LIST_CONTENT_HEIGHT),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 18.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                items(places, key = { it.uuid.toString() }) { place ->
                    PlaceListRow(
                        place = place,
                        onClick = { onPlaceSelected(place.uuid) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MapPanelCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        content()
    }
}

@Composable
private fun MapPanelHeader(
    title: String,
    subtitle: String,
    onClose: () -> Unit,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = DarkInk, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = DarkInk.copy(alpha = 0.62f), fontSize = 12.sp)
            }
            actionLabel?.let { label ->
                Text(
                    text = label,
                    color = Moss,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onAction)
                        .padding(8.dp),
                )
            }
            IconButton(onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Schließen",
                    tint = DarkInk.copy(alpha = 0.65f),
                )
            }
        }
    }
}

@Composable
private fun FilterSectionTitle(
    title: String,
    hint: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 5.dp)) {
        Text(title, color = DarkInk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(hint, color = DarkInk.copy(alpha = 0.58f), fontSize = 10.sp)
    }
}

@Composable
private fun FilterOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (selected) LeafSurface else WarmSurface,
        contentColor = DarkInk,
        shape = RoundedCornerShape(15.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Moss else SheetHandle,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = if (selected) Moss else WarmSurface,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (selected) Moss else SheetHandle),
                modifier = Modifier.size(22.dp),
            ) {
                if (selected) Box(contentAlignment = Alignment.Center) {
                    Text("✓", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.width(11.dp))
            Text(label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
private fun PlaceListRow(
    place: MapPlaceUiState,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = LeafSurface.copy(alpha = 0.55f),
        contentColor = DarkInk,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaceThumbnail(
                category = place.category,
                variant = 0,
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${place.categoryLabel} · ${place.locationHint.substringAfterLast(", ")}",
                    color = DarkInk.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    maxLines = 1,
                )
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Moss, contentColor = PureWhite) {
                Text(
                    text = formatScore(place.currentScore),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
            Text("›", color = Moss, fontSize = 25.sp, modifier = Modifier.padding(start = 7.dp))
        }
    }
}

@Composable
private fun ShortcutButton(
    label: String,
    symbol: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(66.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (active) Moss else LeafSurface,
        contentColor = if (active) PureWhite else DarkInk,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(symbol, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
private fun PlaceMiniPreview(
    place: MapPlaceUiState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(PLACE_PREVIEW_EXPANDED_HEIGHT)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 4.dp),
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
        Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .width(145.dp)
                        .height(82.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PlaceThumbnail(place.category, variant = 0, modifier = Modifier.weight(1f).fillMaxHeight())
                    PlaceThumbnail(place.category, variant = 1, modifier = Modifier.weight(1f).fillMaxHeight())
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.name,
                        color = DarkInk,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = place.description,
                        color = DarkInk.copy(alpha = 0.72f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp,
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
                    color = if (place.isBookmarked) Terracotta else DarkInk,
                    fontSize = 25.sp,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                RatingPill(RatingCriterion.VIBE, "Vibes", place.vibeScore, LeafAccent, Modifier.weight(1f))
                RatingPill(RatingCriterion.SAFETY, "Sicher", place.safetyScore, SafetyBlue, Modifier.weight(1f))
                RatingPill(RatingCriterion.ACCESSIBILITY, "Erreichbar", place.accessibilityScore, AccessibilityGold, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { },
                enabled = place.canRate,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Moss,
                    disabledContainerColor = DisabledSurface,
                    disabledContentColor = DarkInk.copy(alpha = 0.48f),
                ),
            ) {
                Text(
                    text = if (place.canRate) "Ort bewerten" else "Bewerten · nur vor Ort",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        Spacer(Modifier.height(26.dp))
        Text(
            text = "Über diesen Ort",
            color = DarkInk,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = place.description,
            color = DarkInk.copy(alpha = 0.75f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Standort",
            color = DarkInk,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = place.locationHint,
            color = DarkInk.copy(alpha = 0.68f),
            fontSize = 13.sp,
        )
        if (place.attributes.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Ausstattung & Eigenschaften",
                color = DarkInk,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = place.attributes.sortedBy(PlaceAttribute::toDisplayLabel)
                    .joinToString(" · ", transform = PlaceAttribute::toDisplayLabel),
                color = LeafAccent,
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
        }
    }
}

@Composable
private fun PlaceThumbnail(
    category: PlaceCategory,
    variant: Int,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(LeafSurface)) {
        drawRect(if (variant == 0) ThumbnailGreen else ThumbnailSand)
        drawRect(ThumbnailGround, topLeft = Offset(0f, size.height * 0.57f), size = Size(size.width, size.height * 0.43f))
        if (category == PlaceCategory.PROMENADE) {
            drawRect(MapRiver, topLeft = Offset(0f, size.height * 0.5f), size = Size(size.width, size.height * 0.28f))
        }
        repeat(3) { index ->
            val x = size.width * (0.18f + index * 0.31f)
            drawLine(Moss, Offset(x, size.height * 0.38f), Offset(x, size.height * 0.72f), strokeWidth = 3f)
            drawCircle(LeafAccent, radius = size.width * (0.11f + index * 0.01f), center = Offset(x, size.height * (0.3f + variant * 0.04f)))
        }
        if (category == PlaceCategory.SQUARE) {
            drawRoundRect(Terracotta, topLeft = Offset(size.width * 0.28f, size.height * 0.38f), size = Size(size.width * 0.45f, size.height * 0.35f))
        }
    }
}

@Composable
private fun RatingPill(
    criterion: RatingCriterion,
    label: String,
    score: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.12f)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RatingCriterionIcon(criterion = criterion, color = color)
            Spacer(Modifier.width(5.dp))
            Column {
                Text(label, color = DarkInk.copy(alpha = 0.72f), fontSize = 9.sp, maxLines = 1)
                Text(formatScore(score), color = DarkInk, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatScore(score: Double): String = String.format(Locale.GERMANY, "%.1f", score)

private fun categorySymbol(category: PlaceCategory): String = when (category) {
    PlaceCategory.PARK -> "♣"
    PlaceCategory.PROMENADE -> "≈"
    PlaceCategory.SQUARE -> "●"
    PlaceCategory.SHOPPING_STREET, PlaceCategory.SHOPPING_CENTER -> "▦"
    PlaceCategory.DISTRICT -> "⌂"
    PlaceCategory.OTHER_PUBLIC_PLACE -> "+"
}

private fun PlaceAttribute.toDisplayLabel(): String = when (this) {
    PlaceAttribute.PUBLIC_TOILETS -> "Öffentliche Toiletten"
    PlaceAttribute.ACCESSIBLE -> "Barrierefrei"
    PlaceAttribute.SHADE -> "Schatten"
    PlaceAttribute.SEATING -> "Sitzmöglichkeiten"
    PlaceAttribute.FOOD_AND_DRINK -> "Essen & Trinken"
    PlaceAttribute.COVERED -> "Überdacht"
    PlaceAttribute.AIR_CONDITIONED -> "Klimatisiert"
    PlaceAttribute.LANGUAGE_AND_CULTURE -> "Sprache & Kultur"
    PlaceAttribute.LOCAL_EVENTS -> "Lokale Veranstaltungen"
    PlaceAttribute.TIME_DEPENDENT_EVENT -> "Zeitabhängiges Event"
}

private fun String.toSelectionSet(): Set<String> =
    split(SELECTION_SEPARATOR).filter(String::isNotBlank).toSet()

private fun String.toggleSelection(value: String): String {
    val selection = toSelectionSet().toMutableSet()
    if (!selection.add(value)) selection.remove(value)
    return selection.sorted().joinToString(SELECTION_SEPARATOR)
}

/**
 * Entspricht einem "fit bounds" einer echten Karten-API: Alle aktuell
 * sichtbaren Treffer werden in den freien Kartenausschnitt eingepasst. Der
 * rechte Innenabstand ist groesser, weil die Ortsübersicht rechts am Marker
 * sitzt; unten bleibt Platz fuer das Bottom Sheet.
 */
private fun List<MapPlaceUiState>.fitIntoMapViewport(): Map<UUID, MapViewportPosition> {
    if (isEmpty()) return emptyMap()

    val minX = minOf(MapPlaceUiState::mapXFraction)
    val maxX = maxOf(MapPlaceUiState::mapXFraction)
    val minY = minOf(MapPlaceUiState::mapYFraction)
    val maxY = maxOf(MapPlaceUiState::mapYFraction)

    return associate { place ->
        place.uuid to MapViewportPosition(
            x = place.mapXFraction.fitFraction(minX, maxX, MAP_VIEWPORT_START_X, MAP_VIEWPORT_END_X),
            y = place.mapYFraction.fitFraction(minY, maxY, MAP_VIEWPORT_START_Y, MAP_VIEWPORT_END_Y),
        )
    }
}

private fun Float.fitFraction(
    sourceMin: Float,
    sourceMax: Float,
    targetMin: Float,
    targetMax: Float,
): Float = if (sourceMin == sourceMax) {
    (targetMin + targetMax) / 2f
} else {
    targetMin + ((this - sourceMin) / (sourceMax - sourceMin)) * (targetMax - targetMin)
}

private data class MapViewportPosition(val x: Float, val y: Float)

private enum class MapBottomPanel {
    DEFAULT,
    FILTER,
    POPULAR,
    SAVED,
}

private const val SELECTION_SEPARATOR = "|"
private const val MAP_MOVE_DURATION_MILLIS = 550
private const val MAP_FOCUS_X = 0.5f
private const val MAP_FOCUS_Y = 0.34f
private const val MAP_VIEWPORT_START_X = 0.10f
private const val MAP_VIEWPORT_END_X = 0.52f
private const val MAP_VIEWPORT_START_Y = 0.20f
private const val MAP_VIEWPORT_END_Y = 0.66f
private val DEFAULT_PANEL_PEEK_HEIGHT = 180.dp
private val LIST_PANEL_PEEK_HEIGHT = 260.dp
private val PLACE_PREVIEW_PEEK_HEIGHT = 270.dp
private val FILTER_PANEL_CONTENT_HEIGHT = 500.dp
private val PLACE_LIST_CONTENT_HEIGHT = 480.dp
private val PLACE_PREVIEW_EXPANDED_HEIGHT = 560.dp
