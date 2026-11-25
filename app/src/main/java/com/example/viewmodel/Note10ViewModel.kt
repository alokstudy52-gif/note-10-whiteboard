package com.example.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BoardSerializer
import com.example.data.NoteRepository
import com.example.data.NotebookEntity
import com.example.data.ResourceEntity
import com.example.engine.GeometryEngine
import com.example.model.ActiveTool
import com.example.model.AppSettings
import com.example.model.BackgroundStyle
import com.example.model.BoardItem
import com.example.model.BoardPage
import com.example.model.BohrAtomItem
import com.example.model.ChemApparatusItem
import com.example.model.ChemApparatusKind
import com.example.model.EnglishLineItem
import com.example.model.EraserType
import com.example.model.ForceVectorItem
import com.example.model.FunctionGraphItem
import com.example.model.ImageItem
import com.example.model.LaserTrail
import com.example.model.MindMapNode
import com.example.model.OpenPopover
import com.example.model.OverlayToolsState
import com.example.model.PageFlipEffect
import com.example.model.PageManagerViewMode
import com.example.model.PenType
import com.example.model.PhysicsCircuitItem
import com.example.model.PhysicsInstrumentKind
import com.example.model.Shape2DItem
import com.example.model.Shape2DType
import com.example.model.Shape3DItem
import com.example.model.Shape3DType
import com.example.model.StampKind
import com.example.model.StrokeItem
import com.example.model.StrokePoint
import com.example.model.SubjectMode
import com.example.model.TextItem
import com.example.model.WorkMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.math.hypot

data class Note10UiState(
    val notebookId: String = UUID.randomUUID().toString(),
    val notebookName: String = "Untitled Lesson",
    val pages: List<BoardPage> = listOf(BoardPage(title = "Page 1")),
    val currentPageIndex: Int = 0,
    val workMode: WorkMode = WorkMode.PRESENTING,
    val activeTool: ActiveTool = ActiveTool.PEN,
    val penType: PenType = PenType.MARKER,
    val stampKind: StampKind = StampKind.STAR,
    val penColorArgb: Int = 0xFF111827.toInt(),
    val penStrokeWidth: Float = 7f,
    val eraserType: EraserType = EraserType.LINE,
    val shape2DType: Shape2DType = Shape2DType.SMART_RECOGNIZER,
    val shape3DType: Shape3DType = Shape3DType.CUBOID,
    val shapeStrokeColorArgb: Int = 0xFF111827.toInt(),
    val shapeFillColorArgb: Int? = null,
    val shapeStrokeWidth: Float = 5f,
    val selectedItemId: String? = null,
    val zoomScale: Float = 1f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val openPopover: OpenPopover = OpenPopover.NONE,
    val pageManagerViewMode: PageManagerViewMode = PageManagerViewMode.LIST,
    val overlays: OverlayToolsState = OverlayToolsState(),
    val settings: AppSettings = AppSettings(),
    val laserTrails: List<LaserTrail> = emptyList(),
    val editingTextItem: TextItem? = null,
    val editingBannerMode: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val statusToast: String? = null,
    val subjectRailExpanded: Boolean = false
) {
    val currentPage: BoardPage
        get() = pages.getOrElse(currentPageIndex) { pages.first() }

    val selectedItem: BoardItem?
        get() = selectedItemId?.let { id -> currentPage.items.firstOrNull { it.id == id } }
}

class Note10ViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(Note10UiState())
    val uiState: StateFlow<Note10UiState> = _uiState.asStateFlow()

    val savedNotebooks: StateFlow<List<NotebookEntity>> = repository.notebooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savedResources: StateFlow<List<ResourceEntity>> = repository.resources.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val undoStacks = mutableMapOf<String, ArrayDeque<List<BoardItem>>>()
    private val redoStacks = mutableMapOf<String, ArrayDeque<List<BoardItem>>>()

    init {
        // Periodic laser trail decay + chemistry pouring tick
        viewModelScope.launch {
            while (true) {
                delay(120)
                val now = System.currentTimeMillis()
                val trails = _uiState.value.laserTrails
                if (trails.isNotEmpty()) {
                    val remaining = trails.filter { now - it.createdAtMs < 2000L }
                    if (remaining.size != trails.size) {
                        _uiState.update { it.copy(laserTrails = remaining) }
                    }
                }
                tickChemistryPouring()
            }
        }
    }

    private fun tickChemistryPouring() {
        val page = _uiState.value.currentPage
        val chemItems = page.items.filterIsInstance<ChemApparatusItem>()
        if (chemItems.isEmpty()) return

        val tiltedPourers = chemItems.filter {
            it.kind.canPour && !it.isPlugged && abs(it.rotationDeg) > 45f && it.liquidLevel > 0.02f
        }
        if (tiltedPourers.isEmpty()) return

        val updatedMap = chemItems.associateBy { it.id }.toMutableMap()
        var changed = false

        for (pourer in tiltedPourers) {
            val current = updatedMap[pourer.id] ?: continue
            val pourRate = 0.018f
            val pouredAmount = minOf(pourRate, current.liquidLevel)
            if (pouredAmount <= 0f) continue
            updatedMap[current.id] = current.copy(liquidLevel = (current.liquidLevel - pouredAmount).coerceAtLeast(0f))
            changed = true

            // Check if there is a receiving container directly below the tilted mouth
            val mouthX = current.x + if (current.rotationDeg > 0) 65f else -65f
            val receiver = updatedMap.values.firstOrNull { other ->
                other.id != current.id &&
                    other.kind.canPour &&
                    !other.isPlugged &&
                    other.y > current.y + 40f &&
                    abs(other.x - mouthX) < 110f &&
                    other.liquidLevel < 0.95f
            }
            if (receiver != null) {
                updatedMap[receiver.id] = receiver.copy(
                    liquidLevel = (receiver.liquidLevel + pouredAmount * 0.85f).coerceAtMost(0.95f),
                    liquidColorArgb = current.liquidColorArgb
                )
            }
        }

        if (changed) {
            val newItems = page.items.map { item ->
                if (item is ChemApparatusItem) updatedMap[item.id] ?: item else item
            }
            replaceCurrentPageItems(newItems, pushHistory = false)
        }
    }

    private fun pushUndoSnapshot(pageId: String, previousItems: List<BoardItem>) {
        val stack = undoStacks.getOrPut(pageId) { ArrayDeque() }
        if (stack.size >= 40) stack.removeFirst()
        stack.addLast(previousItems)
        redoStacks[pageId]?.clear()
        syncUndoRedoFlags(pageId)
    }

    private fun syncUndoRedoFlags(pageId: String) {
        val canU = (undoStacks[pageId]?.isNotEmpty()) == true
        val canR = (redoStacks[pageId]?.isNotEmpty()) == true
        _uiState.update { it.copy(canUndo = canU, canRedo = canR) }
    }

    private fun replaceCurrentPageItems(newItems: List<BoardItem>, pushHistory: Boolean = true) {
        val state = _uiState.value
        val page = state.currentPage
        if (pushHistory) {
            pushUndoSnapshot(page.id, page.items)
        }
        val updatedPages = state.pages.toMutableList()
        updatedPages[state.currentPageIndex] = page.copy(items = newItems)
        _uiState.update { it.copy(pages = updatedPages) }
        if (pushHistory && state.settings.autoSave) {
            autoSaveNotebook()
        }
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(statusToast = message) }
        viewModelScope.launch {
            delay(2200)
            if (_uiState.value.statusToast == message) {
                _uiState.update { it.copy(statusToast = null) }
            }
        }
    }

    // --- Tool & Popover Controls ---

    fun selectTool(tool: ActiveTool) {
        _uiState.update { state ->
            val nextPopover = when {
                state.activeTool == tool && tool == ActiveTool.PEN ->
                    if (state.openPopover == OpenPopover.PEN) OpenPopover.NONE else OpenPopover.PEN
                state.activeTool == tool && tool == ActiveTool.ERASER ->
                    if (state.openPopover == OpenPopover.ERASER) OpenPopover.NONE else OpenPopover.ERASER
                state.activeTool == tool && tool == ActiveTool.SHAPE_2D ->
                    if (state.openPopover == OpenPopover.SHAPE_2D) OpenPopover.NONE else OpenPopover.SHAPE_2D
                state.activeTool == tool && tool == ActiveTool.SHAPE_3D ->
                    if (state.openPopover == OpenPopover.SHAPE_3D) OpenPopover.NONE else OpenPopover.SHAPE_3D
                state.activeTool == tool && tool == ActiveTool.ROAM ->
                    if (state.openPopover == OpenPopover.ROAM) OpenPopover.NONE else OpenPopover.ROAM
                tool == ActiveTool.TEXT -> OpenPopover.TEXT_EDITOR
                else -> OpenPopover.NONE
            }
            state.copy(
                activeTool = tool,
                openPopover = nextPopover,
                selectedItemId = if (tool == ActiveTool.SELECT) state.selectedItemId else null,
                editingTextItem = if (tool == ActiveTool.TEXT) null else state.editingTextItem,
                editingBannerMode = false
            )
        }
    }

    fun togglePopover(popover: OpenPopover) {
        _uiState.update { state ->
            state.copy(
                openPopover = if (state.openPopover == popover) OpenPopover.NONE else popover,
                subjectRailExpanded = false
            )
        }
    }

    fun closePopovers() {
        _uiState.update { it.copy(openPopover = OpenPopover.NONE, subjectRailExpanded = false) }
    }

    fun toggleSubjectRail() {
        _uiState.update { it.copy(subjectRailExpanded = !it.subjectRailExpanded, openPopover = OpenPopover.NONE) }
    }

    fun setPenConfig(
        penType: PenType? = null,
        stampKind: StampKind? = null,
        colorArgb: Int? = null,
        width: Float? = null
    ) {
        _uiState.update {
            it.copy(
                activeTool = ActiveTool.PEN,
                penType = penType ?: it.penType,
                stampKind = stampKind ?: it.stampKind,
                penColorArgb = colorArgb ?: it.penColorArgb,
                penStrokeWidth = width ?: it.penStrokeWidth
            )
        }
    }

    fun setEraserType(type: EraserType) {
        _uiState.update { it.copy(activeTool = ActiveTool.ERASER, eraserType = type) }
    }

    fun setShape2DConfig(
        shapeType: Shape2DType? = null,
        strokeColorArgb: Int? = null,
        fillColorArgb: Int? = null,
        clearFill: Boolean = false,
        strokeWidth: Float? = null
    ) {
        _uiState.update {
            it.copy(
                activeTool = ActiveTool.SHAPE_2D,
                shape2DType = shapeType ?: it.shape2DType,
                shapeStrokeColorArgb = strokeColorArgb ?: it.shapeStrokeColorArgb,
                shapeFillColorArgb = if (clearFill) null else (fillColorArgb ?: it.shapeFillColorArgb),
                shapeStrokeWidth = strokeWidth ?: it.shapeStrokeWidth
            )
        }
    }

    fun setShape3DConfig(
        shapeType: Shape3DType? = null,
        strokeColorArgb: Int? = null,
        strokeWidth: Float? = null
    ) {
        _uiState.update {
            it.copy(
                activeTool = ActiveTool.SHAPE_3D,
                shape3DType = shapeType ?: it.shape3DType,
                shapeStrokeColorArgb = strokeColorArgb ?: it.shapeStrokeColorArgb,
                shapeStrokeWidth = strokeWidth ?: it.shapeStrokeWidth
            )
        }
    }

    fun setWorkMode(mode: WorkMode) {
        _uiState.update { it.copy(workMode = mode, openPopover = OpenPopover.NONE) }
        showToast("${mode.label} mode")
    }

    // --- Subject Mode Switching (Note3 Section 3.3) ---

    fun switchSubjectMode(mode: SubjectMode) {
        val state = _uiState.value
        val page = state.currentPage
        val updatedPage = page.copy(
            subjectMode = mode,
            backgroundColorArgb = mode.defaultBoardColor.toInt(),
            backgroundStyle = mode.defaultBgStyle,
            customBgUri = null
        )
        val updatedPages = state.pages.toMutableList()
        updatedPages[state.currentPageIndex] = updatedPage

        // Pick contrasting ink color for dark physics/chemistry boards vs light math/english boards
        val smartInk = when (mode) {
            SubjectMode.PHYSICS -> 0xFFFFFFFF.toInt()
            SubjectMode.CHEMISTRY -> 0xFF111827.toInt()
            else -> 0xFF111827.toInt()
        }

        _uiState.update {
            it.copy(
                pages = updatedPages,
                penColorArgb = smartInk,
                shapeStrokeColorArgb = smartInk,
                subjectRailExpanded = false,
                openPopover = OpenPopover.NONE
            )
        }
        showToast("${mode.title} Mode active")
    }

    // --- Drawing & Smart Shape Recognition ---

    fun commitStroke(points: List<StrokePoint>) {
        if (points.size < 2) return
        val state = _uiState.value

        if (state.penType == PenType.LASER) {
            val trail = LaserTrail(points = points, colorArgb = state.penColorArgb)
            _uiState.update { it.copy(laserTrails = it.laserTrails + trail) }
            return
        }

        val maxZ = (state.currentPage.items.maxOfOrNull { it.zIndex } ?: 0) + 1
        val item = StrokeItem(
            points = points,
            colorArgb = state.penColorArgb,
            strokeWidth = state.penStrokeWidth,
            penType = state.penType,
            stampKind = state.stampKind,
            zIndex = maxZ
        )
        replaceCurrentPageItems(state.currentPage.items + item)
    }

    fun commitSmartShapeStroke(points: List<StrokePoint>) {
        val state = _uiState.value
        val recognized = GeometryEngine.recognizeShape(
            points = points,
            strokeColorArgb = state.shapeStrokeColorArgb,
            fillColorArgb = state.shapeFillColorArgb,
            strokeWidth = state.shapeStrokeWidth
        )
        if (recognized != null) {
            val maxZ = (state.currentPage.items.maxOfOrNull { it.zIndex } ?: 0) + 1
            val item = recognized.copy(zIndex = maxZ)
            replaceCurrentPageItems(state.currentPage.items + item)
            _uiState.update { it.copy(selectedItemId = item.id) }
            showToast("Recognized: ${item.shapeType.label}")
        } else {
            showToast("Shape not recognized — try a clearer outline")
        }
    }

    fun addBoardItem(item: BoardItem, selectAfter: Boolean = true) {
        val state = _uiState.value
        val maxZ = (state.currentPage.items.maxOfOrNull { it.zIndex } ?: 0) + 1
        val zItem = withZIndex(item, maxZ)
        replaceCurrentPageItems(state.currentPage.items + zItem)
        if (selectAfter) {
            _uiState.update { it.copy(selectedItemId = zItem.id) }
        }
    }

    fun commitForceVector(origin: Offset, tip: Offset) {
        if (hypot(tip.x - origin.x, tip.y - origin.y) < 25f) return
        val page = _uiState.value.currentPage
        val existingForces = page.items.filterIsInstance<ForceVectorItem>()
        // Snap origin to existing force system origin if nearby so resultant force composes cleanly!
        val snappedOrigin = existingForces.firstOrNull()?.let { first ->
            if (hypot(origin.x - first.originX, origin.y - first.originY) < 160f) {
                Offset(first.originX, first.originY)
            } else origin
        } ?: origin
        val idx = existingForces.size + 1
        addBoardItem(
            ForceVectorItem(
                originX = snappedOrigin.x,
                originY = snappedOrigin.y,
                tipX = tip.x,
                tipY = tip.y,
                label = "F$idx"
            ),
            selectAfter = false
        )
    }

    // --- Erasing ---

    fun eraseAtPoint(boardPoint: Offset, radius: Float = 26f) {
        val page = _uiState.value.currentPage
        val remaining = page.items.filterNot { item ->
            if (item.locked) return@filterNot false
            when (item) {
                is StrokeItem -> item.points.any { pt ->
                    hypot((pt.x + item.offsetX) - boardPoint.x, (pt.y + item.offsetY) - boardPoint.y) <= radius + item.strokeWidth
                }
                else -> false
            }
        }
        if (remaining.size != page.items.size) {
            replaceCurrentPageItems(remaining)
        }
    }

    fun eraseInRect(left: Float, top: Float, right: Float, bottom: Float) {
        val page = _uiState.value.currentPage
        val remaining = page.items.filterNot { item ->
            if (item.locked) return@filterNot false
            val center = getItemCenter(item)
            center.x in left..right && center.y in top..bottom
        }
        if (remaining.size != page.items.size) {
            replaceCurrentPageItems(remaining)
            _uiState.update { it.copy(selectedItemId = null) }
        }
    }

    fun clearCurrentPage() {
        val page = _uiState.value.currentPage
        if (page.items.isNotEmpty()) {
            replaceCurrentPageItems(emptyList())
            _uiState.update { it.copy(selectedItemId = null, openPopover = OpenPopover.NONE) }
            showToast("Page cleared")
        }
    }

    // --- Selection & Object Manipulation (Note3 Section 3.1.6) ---

    fun selectItemAt(boardPoint: Offset): Boolean {
        val page = _uiState.value.currentPage
        val hit = page.items.sortedByDescending { it.zIndex }.firstOrNull { item ->
            hitTestItem(item, boardPoint)
        }
        // Special direct tap interactions for Physics Switch & Alcohol Lamp!
        if (hit is PhysicsCircuitItem && hit.kind == PhysicsInstrumentKind.SWITCH && !hit.locked) {
            updateItemById(hit.id) { (it as PhysicsCircuitItem).copy(isSwitchClosed = !it.isSwitchClosed) }
        } else if (hit is ChemApparatusItem && hit.kind == ChemApparatusKind.ALCOHOL_LAMP && !hit.locked) {
            updateItemById(hit.id) { (it as ChemApparatusItem).copy(isLit = !it.isLit) }
        }
        _uiState.update { it.copy(selectedItemId = hit?.id) }
        return hit != null
    }

    fun selectItemById(id: String?) {
        _uiState.update { it.copy(selectedItemId = id) }
    }

    fun moveSelectedItem(dx: Float, dy: Float) {
        val id = _uiState.value.selectedItemId ?: return
        val page = _uiState.value.currentPage
        val target = page.items.firstOrNull { it.id == id } ?: return
        if (target.locked) return
        val moved = translateItem(target, dx, dy)
        val updated = page.items.map { if (it.id == id) moved else it }
        replaceCurrentPageItems(updated, pushHistory = false)
    }

    fun commitMoveSnapshot(beforeItems: List<BoardItem>) {
        pushUndoSnapshot(_uiState.value.currentPage.id, beforeItems)
        if (_uiState.value.settings.autoSave) autoSaveNotebook()
    }

    fun scaleSelectedItem(factor: Float) {
        val id = _uiState.value.selectedItemId ?: return
        updateItemById(id) { item ->
            if (item.locked) return@updateItemById item
            when (item) {
                is Shape2DItem -> item.copy(
                    width = (item.width * factor).coerceIn(24f, 1200f),
                    height = (item.height * factor).coerceIn(24f, 1200f)
                )
                is Shape3DItem -> item.copy(
                    width = (item.width * factor).coerceIn(36f, 1000f),
                    height = (item.height * factor).coerceIn(36f, 1000f)
                )
                is ImageItem -> item.copy(
                    width = (item.width * factor).coerceIn(60f, 1200f),
                    height = (item.height * factor).coerceIn(60f, 1200f)
                )
                is TextItem -> item.copy(scale = (item.scale * factor).coerceIn(0.4f, 3.5f))
                is StrokeItem -> item.copy(scale = (item.scale * factor).coerceIn(0.4f, 3.5f))
                is FunctionGraphItem -> item.copy(
                    width = (item.width * factor).coerceIn(180f, 1000f),
                    height = (item.height * factor).coerceIn(140f, 800f)
                )
                is BohrAtomItem -> item.copy(scale = (item.scale * factor).coerceIn(0.5f, 2.5f))
                is ChemApparatusItem -> item.copy(scale = (item.scale * factor).coerceIn(0.6f, 2.2f))
                is PhysicsCircuitItem -> item.copy(scale = (item.scale * factor).coerceIn(0.6f, 2.2f))
                else -> item
            }
        }
    }

    fun rotateSelectedItem(deltaDeg: Float) {
        val id = _uiState.value.selectedItemId ?: return
        updateItemById(id) { item ->
            if (item.locked) return@updateItemById item
            when (item) {
                is Shape2DItem -> item.copy(rotationDeg = (item.rotationDeg + deltaDeg) % 360f)
                is Shape3DItem -> item.copy(rotationDeg = (item.rotationDeg + deltaDeg) % 360f)
                is TextItem -> item.copy(rotationDeg = (item.rotationDeg + deltaDeg) % 360f)
                is ImageItem -> item.copy(rotationDeg = (item.rotationDeg + deltaDeg) % 360f)
                is StrokeItem -> item.copy(rotationDeg = (item.rotationDeg + deltaDeg) % 360f)
                is ChemApparatusItem -> item.copy(rotationDeg = (item.rotationDeg + deltaDeg).coerceIn(-120f, 120f))
                else -> item
            }
        }
    }

    fun deleteSelectedItem() {
        val id = _uiState.value.selectedItemId ?: return
        val page = _uiState.value.currentPage
        val target = page.items.firstOrNull { it.id == id } ?: return
        if (target.locked) {
            showToast("Unlock item before deleting")
            return
        }
        replaceCurrentPageItems(page.items.filterNot { it.id == id })
        _uiState.update { it.copy(selectedItemId = null) }
    }

    fun cloneSelectedItem() {
        val target = _uiState.value.selectedItem ?: return
        val maxZ = (_uiState.value.currentPage.items.maxOfOrNull { it.zIndex } ?: 0) + 1
        val cloned = cloneItemWithOffset(target, 36f, 36f, maxZ)
        replaceCurrentPageItems(_uiState.value.currentPage.items + cloned)
        _uiState.update { it.copy(selectedItemId = cloned.id) }
        showToast("Cloned object")
    }

    fun toggleLockSelectedItem() {
        val id = _uiState.value.selectedItemId ?: return
        updateItemById(id) { item ->
            when (item) {
                is StrokeItem -> item.copy(locked = !item.locked)
                is Shape2DItem -> item.copy(locked = !item.locked)
                is Shape3DItem -> item.copy(locked = !item.locked)
                is TextItem -> item.copy(locked = !item.locked)
                is ImageItem -> item.copy(locked = !item.locked)
                is FunctionGraphItem -> item.copy(locked = !item.locked)
                is BohrAtomItem -> item.copy(locked = !item.locked)
                is ChemApparatusItem -> item.copy(locked = !item.locked)
                is PhysicsCircuitItem -> item.copy(locked = !item.locked)
                is ForceVectorItem -> item.copy(locked = !item.locked)
                is EnglishLineItem -> item.copy(locked = !item.locked)
            }
        }
    }

    fun bringSelectedToFront() {
        val id = _uiState.value.selectedItemId ?: return
        val maxZ = (_uiState.value.currentPage.items.maxOfOrNull { it.zIndex } ?: 0) + 1
        updateItemById(id) { withZIndex(it, maxZ) }
    }

    fun sendSelectedToBack() {
        val id = _uiState.value.selectedItemId ?: return
        val minZ = (_uiState.value.currentPage.items.minOfOrNull { it.zIndex } ?: 0) - 1
        updateItemById(id) { withZIndex(it, minZ) }
    }

    fun setSelectedItemColor(colorArgb: Int) {
        val id = _uiState.value.selectedItemId ?: return
        updateItemById(id) { item ->
            when (item) {
                is StrokeItem -> item.copy(colorArgb = colorArgb)
                is Shape2DItem -> item.copy(strokeColorArgb = colorArgb)
                is Shape3DItem -> item.copy(strokeColorArgb = colorArgb)
                is TextItem -> item.copy(colorArgb = colorArgb)
                is FunctionGraphItem -> item.copy(lineColorArgb = colorArgb)
                is ChemApparatusItem -> item.copy(liquidColorArgb = colorArgb)
                is ForceVectorItem -> item.copy(colorArgb = colorArgb)
                is EnglishLineItem -> item.copy(colorArgb = colorArgb)
                else -> item
            }
        }
    }

    fun setSelectedItemFillColor(colorArgb: Int?) {
        val id = _uiState.value.selectedItemId ?: return
        updateItemById(id) { item ->
            when (item) {
                is Shape2DItem -> item.copy(fillColorArgb = colorArgb)
                is Shape3DItem -> item.copy(fillColorArgb = colorArgb)
                is TextItem -> item.copy(bgColorArgb = colorArgb)
                else -> item
            }
        }
    }

    fun toggleSelectedPlanarFeature(feature: String) {
        val id = _uiState.value.selectedItemId ?: return
        updateItemById(id) { item ->
            if (item !is Shape2DItem) return@updateItemById item
            when (feature) {
                "center" -> item.copy(showCenter = !item.showCenter)
                "radius" -> item.copy(showRadius = !item.showRadius)
                "incircle" -> item.copy(showIncircle = !item.showIncircle)
                "circumcircle" -> item.copy(showCircumcircle = !item.showCircumcircle)
                else -> item
            }
        }
    }

    fun stepSelectedBohrAtom(delta: Int) {
        val id = _uiState.value.selectedItemId ?: return
        updateItemById(id) { item ->
            if (item !is BohrAtomItem) return@updateItemById item
            val next = (item.atomicNumber + delta).coerceIn(1, 118)
            item.copy(atomicNumber = next)
        }
    }

    fun setSelectedBohrAtom(atomicNumber: Int) {
        val id = _uiState.value.selectedItemId
        val selected = _uiState.value.selectedItem
        if (id != null && selected is BohrAtomItem) {
            updateItemById(id) { (it as BohrAtomItem).copy(atomicNumber = atomicNumber.coerceIn(1, 118)) }
        } else {
            addBoardItem(BohrAtomItem(x = 340f, y = 320f, atomicNumber = atomicNumber.coerceIn(1, 118)))
        }
    }

    fun updateSelectedChemApparatus(
        liquidLevel: Float? = null,
        togglePlug: Boolean = false,
        toggleFlame: Boolean = false,
        rotationDeg: Float? = null
    ) {
        val id = _uiState.value.selectedItemId ?: return
        updateItemById(id) { item ->
            if (item !is ChemApparatusItem) return@updateItemById item
            item.copy(
                liquidLevel = liquidLevel?.coerceIn(0f, 1f) ?: item.liquidLevel,
                isPlugged = if (togglePlug && item.kind.hasPlug) !item.isPlugged else item.isPlugged,
                isLit = if (toggleFlame && item.kind == ChemApparatusKind.ALCOHOL_LAMP) !item.isLit else item.isLit,
                rotationDeg = rotationDeg ?: item.rotationDeg
            )
        }
    }

    fun updateSelectedPhysicsInstrument(
        toggleSwitch: Boolean = false,
        resistanceOhm: Float? = null,
        voltageVolts: Float? = null
    ) {
        val id = _uiState.value.selectedItemId ?: return
        updateItemById(id) { item ->
            if (item !is PhysicsCircuitItem) return@updateItemById item
            item.copy(
                isSwitchClosed = if (toggleSwitch) !item.isSwitchClosed else item.isSwitchClosed,
                resistanceOhm = resistanceOhm?.coerceIn(1f, 30f) ?: item.resistanceOhm,
                voltageVolts = voltageVolts?.coerceIn(3f, 24f) ?: item.voltageVolts
            )
        }
    }

    fun setSelectedHyperlink(link: String?) {
        val id = _uiState.value.selectedItemId ?: return
        updateItemById(id) { item ->
            when (item) {
                is StrokeItem -> item.copy(hyperlink = link)
                is Shape2DItem -> item.copy(hyperlink = link)
                is Shape3DItem -> item.copy(hyperlink = link)
                is TextItem -> item.copy(hyperlink = link)
                is ImageItem -> item.copy(hyperlink = link)
                is FunctionGraphItem -> item.copy(hyperlink = link)
                is BohrAtomItem -> item.copy(hyperlink = link)
                is ChemApparatusItem -> item.copy(hyperlink = link)
                is PhysicsCircuitItem -> item.copy(hyperlink = link)
                is ForceVectorItem -> item.copy(hyperlink = link)
                is EnglishLineItem -> item.copy(hyperlink = link)
            }
        }
        _uiState.update { it.copy(openPopover = OpenPopover.NONE) }
        showToast(if (link.isNullOrBlank()) "Hyperlink removed" else "Hyperlink attached")
    }

    fun saveSelectedToResourceLibrary(title: String) {
        val item = _uiState.value.selectedItem ?: return
        viewModelScope.launch {
            repository.saveToResourceLibrary(
                title = title.ifBlank { "Saved Snippet" },
                category = _uiState.value.currentPage.subjectMode.title,
                item = item
            )
            showToast("Saved to Resource Library")
        }
    }

    fun insertFromResourceLibrary(entity: ResourceEntity) {
        val item = BoardSerializer.deserializeItem(entity.itemJson) ?: return
        val cloned = cloneItemWithOffset(item, 24f, 24f, (_uiState.value.currentPage.items.maxOfOrNull { it.zIndex } ?: 0) + 1)
        addBoardItem(cloned)
        closePopovers()
        showToast("Inserted ${entity.title}")
    }

    fun deleteResource(id: String) {
        viewModelScope.launch { repository.deleteResource(id) }
    }

    // --- Text & English Ruled Paper ---

    fun openTextEditor(existing: TextItem? = null, isBanner: Boolean = false) {
        _uiState.update {
            it.copy(
                editingTextItem = existing,
                editingBannerMode = isBanner,
                openPopover = OpenPopover.TEXT_EDITOR
            )
        }
    }

    fun saveTextItem(item: TextItem) {
        val page = _uiState.value.currentPage
        val exists = page.items.any { it.id == item.id }
        if (exists) {
            updateItemById(item.id) { item }
        } else {
            addBoardItem(item)
        }
        _uiState.update { it.copy(openPopover = OpenPopover.NONE, editingTextItem = null, activeTool = ActiveTool.SELECT) }
    }

    fun addOrUpdateEnglishLine(rowIndex: Int, text: String) {
        val page = _uiState.value.currentPage
        val existing = page.items.filterIsInstance<EnglishLineItem>().firstOrNull { it.rowIndex == rowIndex }
        if (text.isBlank()) {
            if (existing != null) {
                replaceCurrentPageItems(page.items.filterNot { it.id == existing.id })
            }
            return
        }
        if (existing != null) {
            updateItemById(existing.id) { existing.copy(text = text) }
        } else {
            addBoardItem(EnglishLineItem(rowIndex = rowIndex, text = text), selectAfter = false)
        }
    }

    fun deleteEnglishLine(id: String) {
        replaceCurrentPageItems(_uiState.value.currentPage.items.filterNot { it.id == id })
    }

    // --- Undo / Redo ---

    fun undo() {
        val page = _uiState.value.currentPage
        val stack = undoStacks[page.id] ?: return
        if (stack.isEmpty()) return
        val previous = stack.removeLast()
        redoStacks.getOrPut(page.id) { ArrayDeque() }.addLast(page.items)
        replaceCurrentPageItems(previous, pushHistory = false)
        syncUndoRedoFlags(page.id)
    }

    fun redo() {
        val page = _uiState.value.currentPage
        val stack = redoStacks[page.id] ?: return
        if (stack.isEmpty()) return
        val next = stack.removeLast()
        undoStacks.getOrPut(page.id) { ArrayDeque() }.addLast(page.items)
        replaceCurrentPageItems(next, pushHistory = false)
        syncUndoRedoFlags(page.id)
    }

    // --- Roaming (Pan & Zoom) ---

    fun updatePanZoom(zoom: Float? = null, panX: Float? = null, panY: Float? = null) {
        _uiState.update {
            it.copy(
                zoomScale = (zoom ?: it.zoomScale).coerceIn(0.45f, 3.5f),
                panX = panX ?: it.panX,
                panY = panY ?: it.panY
            )
        }
    }

    fun resetPanZoom() {
        _uiState.update { it.copy(zoomScale = 1f, panX = 0f, panY = 0f) }
    }

    // --- Backgrounds ---

    fun setPageBackground(
        style: BackgroundStyle? = null,
        colorArgb: Int? = null,
        customBgUri: String? = null
    ) {
        val state = _uiState.value
        val page = state.currentPage
        val updated = page.copy(
            backgroundStyle = style ?: page.backgroundStyle,
            backgroundColorArgb = colorArgb ?: page.backgroundColorArgb,
            customBgUri = if (style == BackgroundStyle.CUSTOM_IMAGE) customBgUri else if (style != null) null else page.customBgUri
        )
        val pages = state.pages.toMutableList()
        pages[state.currentPageIndex] = updated
        _uiState.update { it.copy(pages = pages) }
    }

    // --- Page Management (Note3 Section 3.1.14 - 3.1.17) ---

    fun addNewPage(groupName: String? = null) {
        val state = _uiState.value
        val current = state.currentPage
        val newPage = BoardPage(
            title = "Page ${state.pages.size + 1}",
            groupName = groupName ?: current.groupName,
            subjectMode = current.subjectMode,
            backgroundStyle = current.backgroundStyle,
            backgroundColorArgb = current.backgroundColorArgb,
            flipEffect = current.flipEffect
        )
        val pages = state.pages.toMutableList()
        val insertAt = state.currentPageIndex + 1
        pages.add(insertAt, newPage)
        _uiState.update {
            it.copy(
                pages = pages,
                currentPageIndex = insertAt,
                selectedItemId = null,
                zoomScale = 1f,
                panX = 0f,
                panY = 0f
            )
        }
        syncUndoRedoFlags(newPage.id)
        if (state.settings.autoSave) autoSaveNotebook()
    }

    fun duplicateCurrentPage() {
        val state = _uiState.value
        val current = state.currentPage
        val copy = current.copy(
            id = UUID.randomUUID().toString(),
            title = "${current.title} (Copy)",
            items = current.items.map { cloneItemWithOffset(it, 0f, 0f, it.zIndex) }
        )
        val pages = state.pages.toMutableList()
        val insertAt = state.currentPageIndex + 1
        pages.add(insertAt, copy)
        _uiState.update { it.copy(pages = pages, currentPageIndex = insertAt, selectedItemId = null) }
        syncUndoRedoFlags(copy.id)
        showToast("Page duplicated")
    }

    fun goToPage(index: Int) {
        val state = _uiState.value
        val clamped = index.coerceIn(0, state.pages.lastIndex)
        _uiState.update {
            it.copy(
                currentPageIndex = clamped,
                selectedItemId = null,
                zoomScale = 1f,
                panX = 0f,
                panY = 0f
            )
        }
        syncUndoRedoFlags(state.pages[clamped].id)
    }

    fun previousPage() {
        val idx = _uiState.value.currentPageIndex
        if (idx > 0) goToPage(idx - 1)
    }

    fun nextOrAddPage() {
        val state = _uiState.value
        if (state.currentPageIndex < state.pages.lastIndex) {
            goToPage(state.currentPageIndex + 1)
        } else {
            addNewPage()
        }
    }

    fun deletePage(index: Int) {
        val state = _uiState.value
        if (state.pages.size <= 1) {
            clearCurrentPage()
            return
        }
        val pages = state.pages.toMutableList()
        pages.removeAt(index)
        val nextIdx = state.currentPageIndex.coerceAtMost(pages.lastIndex)
        _uiState.update { it.copy(pages = pages, currentPageIndex = nextIdx, selectedItemId = null) }
        syncUndoRedoFlags(pages[nextIdx].id)
    }

    fun setPageGroup(pageIndex: Int, groupName: String) {
        val pages = _uiState.value.pages.toMutableList()
        if (pageIndex in pages.indices) {
            pages[pageIndex] = pages[pageIndex].copy(groupName = groupName.ifBlank { "Default Group" })
            _uiState.update { it.copy(pages = pages) }
        }
    }

    fun setPageFlipEffect(effect: PageFlipEffect) {
        val state = _uiState.value
        val pages = state.pages.toMutableList()
        pages[state.currentPageIndex] = state.currentPage.copy(flipEffect = effect)
        _uiState.update { it.copy(pages = pages) }
        showToast("Flip effect: ${effect.label}")
    }

    fun setPageManagerViewMode(mode: PageManagerViewMode) {
        _uiState.update { it.copy(pageManagerViewMode = mode) }
    }

    // --- Mind Map Nodes (Note3 Section 3.1.3.3.11) ---

    fun addMindMapNode(label: String, note: String, isBig: Boolean) {
        val state = _uiState.value
        val page = state.currentPage
        val count = page.mindMapNodes.size
        val frac = ((count + 1f) / (count + 2f)).coerceIn(0.12f, 0.88f)
        val node = MindMapNode(label = label.ifBlank { "Topic ${count + 1}" }, note = note, isBig = isBig, positionFraction = frac)
        val pages = state.pages.toMutableList()
        pages[state.currentPageIndex] = page.copy(mindMapNodes = page.mindMapNodes + node)
        _uiState.update { it.copy(pages = pages) }
    }

    fun deleteMindMapNode(nodeId: String) {
        val state = _uiState.value
        val page = state.currentPage
        val pages = state.pages.toMutableList()
        pages[state.currentPageIndex] = page.copy(mindMapNodes = page.mindMapNodes.filterNot { it.id == nodeId })
        _uiState.update { it.copy(pages = pages) }
    }

    // --- Floating Presentation & Subject Tool Overlays ---

    fun updateOverlays(transform: (OverlayToolsState) -> OverlayToolsState) {
        _uiState.update { it.copy(overlays = transform(it.overlays), openPopover = OpenPopover.NONE) }
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        _uiState.update { state ->
            val next = transform(state.settings)
            state.copy(settings = next, penColorArgb = next.defaultPenColorArgb)
        }
    }

    // --- Notebook File Operations (New / Save / Save As / Open / Delete) ---

    fun createNewNotebook(name: String = "Untitled Lesson") {
        val firstPage = BoardPage(
            title = "Page 1",
            backgroundColorArgb = _uiState.value.settings.defaultBoardColorArgb
        )
        undoStacks.clear()
        redoStacks.clear()
        _uiState.update {
            it.copy(
                notebookId = UUID.randomUUID().toString(),
                notebookName = name,
                pages = listOf(firstPage),
                currentPageIndex = 0,
                selectedItemId = null,
                zoomScale = 1f,
                panX = 0f,
                panY = 0f,
                openPopover = OpenPopover.NONE,
                canUndo = false,
                canRedo = false
            )
        }
        showToast("Created new notebook")
    }

    fun saveNotebookAs(newName: String) {
        val cleanName = newName.ifBlank { "Note 10 Lesson" }
        _uiState.update { it.copy(notebookName = cleanName) }
        viewModelScope.launch {
            val s = _uiState.value
            repository.saveNotebook(s.notebookId, cleanName, s.currentPage.subjectMode.title, s.pages)
            showToast("Saved \"$cleanName\"")
        }
    }

    private fun autoSaveNotebook() {
        viewModelScope.launch {
            val s = _uiState.value
            repository.saveNotebook(s.notebookId, s.notebookName, s.currentPage.subjectMode.title, s.pages)
        }
    }

    fun openNotebook(id: String) {
        viewModelScope.launch {
            val loaded = repository.loadNotebookPages(id) ?: return@launch
            val (entity, pages) = loaded
            if (pages.isEmpty()) return@launch
            undoStacks.clear()
            redoStacks.clear()
            _uiState.update {
                it.copy(
                    notebookId = entity.id,
                    notebookName = entity.name,
                    pages = pages,
                    currentPageIndex = 0,
                    selectedItemId = null,
                    openPopover = OpenPopover.NONE,
                    canUndo = false,
                    canRedo = false
                )
            }
            showToast("Opened \"${entity.name}\"")
        }
    }

    fun deleteNotebook(id: String) {
        viewModelScope.launch {
            repository.deleteNotebook(id)
            showToast("Notebook deleted")
        }
    }

    fun importNotebookFromJson(name: String, json: String) {
        val pages = BoardSerializer.deserializePages(json)
        if (pages.isEmpty()) {
            showToast("Invalid or empty .n10 notebook file")
            return
        }
        val cleanName = name.removeSuffix(".n10").removeSuffix(".json").ifBlank { "Imported Lesson" }
        val newId = UUID.randomUUID().toString()
        undoStacks.clear()
        redoStacks.clear()
        _uiState.update {
            it.copy(
                notebookId = newId,
                notebookName = cleanName,
                pages = pages,
                currentPageIndex = 0,
                selectedItemId = null,
                zoomScale = 1f,
                panX = 0f,
                panY = 0f,
                openPopover = OpenPopover.NONE,
                canUndo = false,
                canRedo = false
            )
        }
        viewModelScope.launch {
            repository.saveNotebook(newId, cleanName, pages.first().subjectMode.title, pages)
            showToast("Imported \"$cleanName\" (${pages.size} pages)")
        }
    }

    // --- Helpers ---

    private fun updateItemById(id: String, transform: (BoardItem) -> BoardItem) {
        val page = _uiState.value.currentPage
        val updated = page.items.map { if (it.id == id) transform(it) else it }
        replaceCurrentPageItems(updated)
    }

    private fun withZIndex(item: BoardItem, z: Int): BoardItem = when (item) {
        is StrokeItem -> item.copy(zIndex = z)
        is Shape2DItem -> item.copy(zIndex = z)
        is Shape3DItem -> item.copy(zIndex = z)
        is TextItem -> item.copy(zIndex = z)
        is ImageItem -> item.copy(zIndex = z)
        is FunctionGraphItem -> item.copy(zIndex = z)
        is BohrAtomItem -> item.copy(zIndex = z)
        is ChemApparatusItem -> item.copy(zIndex = z)
        is PhysicsCircuitItem -> item.copy(zIndex = z)
        is ForceVectorItem -> item.copy(zIndex = z)
        is EnglishLineItem -> item.copy(zIndex = z)
    }

    private fun translateItem(item: BoardItem, dx: Float, dy: Float): BoardItem = when (item) {
        is StrokeItem -> item.copy(offsetX = item.offsetX + dx, offsetY = item.offsetY + dy)
        is Shape2DItem -> item.copy(x = item.x + dx, y = item.y + dy)
        is Shape3DItem -> item.copy(x = item.x + dx, y = item.y + dy)
        is TextItem -> item.copy(x = item.x + dx, y = item.y + dy)
        is ImageItem -> item.copy(x = item.x + dx, y = item.y + dy)
        is FunctionGraphItem -> item.copy(x = item.x + dx, y = item.y + dy)
        is BohrAtomItem -> item.copy(x = item.x + dx, y = item.y + dy)
        is ChemApparatusItem -> item.copy(x = item.x + dx, y = item.y + dy)
        is PhysicsCircuitItem -> item.copy(x = item.x + dx, y = item.y + dy)
        is ForceVectorItem -> item.copy(originX = item.originX + dx, originY = item.originY + dy, tipX = item.tipX + dx, tipY = item.tipY + dy)
        is EnglishLineItem -> item
    }

    private fun cloneItemWithOffset(item: BoardItem, dx: Float, dy: Float, z: Int): BoardItem {
        val newId = UUID.randomUUID().toString()
        return when (item) {
            is StrokeItem -> item.copy(id = newId, offsetX = item.offsetX + dx, offsetY = item.offsetY + dy, locked = false, zIndex = z)
            is Shape2DItem -> item.copy(id = newId, x = item.x + dx, y = item.y + dy, locked = false, zIndex = z)
            is Shape3DItem -> item.copy(id = newId, x = item.x + dx, y = item.y + dy, locked = false, zIndex = z)
            is TextItem -> item.copy(id = newId, x = item.x + dx, y = item.y + dy, locked = false, zIndex = z)
            is ImageItem -> item.copy(id = newId, x = item.x + dx, y = item.y + dy, locked = false, zIndex = z)
            is FunctionGraphItem -> item.copy(id = newId, x = item.x + dx, y = item.y + dy, locked = false, zIndex = z)
            is BohrAtomItem -> item.copy(id = newId, x = item.x + dx, y = item.y + dy, locked = false, zIndex = z)
            is ChemApparatusItem -> item.copy(id = newId, x = item.x + dx, y = item.y + dy, locked = false, zIndex = z)
            is PhysicsCircuitItem -> item.copy(id = newId, x = item.x + dx, y = item.y + dy, locked = false, zIndex = z)
            is ForceVectorItem -> item.copy(id = newId, originX = item.originX + dx, originY = item.originY + dy, tipX = item.tipX + dx, tipY = item.tipY + dy, locked = false, zIndex = z)
            is EnglishLineItem -> item.copy(id = newId, locked = false, zIndex = z)
        }
    }

    fun getItemCenter(item: BoardItem): Offset = when (item) {
        is StrokeItem -> {
            if (item.points.isEmpty()) Offset.Zero
            else {
                val cx = item.points.map { it.x }.average().toFloat() + item.offsetX
                val cy = item.points.map { it.y }.average().toFloat() + item.offsetY
                Offset(cx, cy)
            }
        }
        is Shape2DItem -> Offset(item.x + item.width / 2f, item.y + item.height / 2f)
        is Shape3DItem -> Offset(item.x + item.width / 2f, item.y + item.height / 2f)
        is TextItem -> Offset(item.x + 80f, item.y + 28f)
        is ImageItem -> Offset(item.x + item.width / 2f, item.y + item.height / 2f)
        is FunctionGraphItem -> Offset(item.x + item.width / 2f, item.y + item.height / 2f)
        is BohrAtomItem -> Offset(item.x, item.y)
        is ChemApparatusItem -> Offset(item.x, item.y)
        is PhysicsCircuitItem -> Offset(item.x, item.y)
        is ForceVectorItem -> Offset((item.originX + item.tipX) / 2f, (item.originY + item.tipY) / 2f)
        is EnglishLineItem -> Offset(180f, 110f + item.rowIndex * 116f)
    }

    private fun hitTestItem(item: BoardItem, p: Offset): Boolean = when (item) {
        is StrokeItem -> item.points.any { pt ->
            hypot((pt.x + item.offsetX) - p.x, (pt.y + item.offsetY) - p.y) < 34f
        }
        is Shape2DItem -> {
            val minX = minOf(item.x, item.x + item.width) - 20f
            val maxX = maxOf(item.x, item.x + item.width) + 20f
            val minY = minOf(item.y, item.y + item.height) - 20f
            val maxY = maxOf(item.y, item.y + item.height) + 20f
            p.x in minX..maxX && p.y in minY..maxY
        }
        is Shape3DItem -> p.x in (item.x - 16f)..(item.x + item.width + 16f) && p.y in (item.y - 16f)..(item.y + item.height + 16f)
        is TextItem -> {
            val estW = (item.text.length * item.fontSizeSp * 0.65f * item.scale).coerceAtLeast(110f)
            val estH = (item.fontSizeSp * 1.8f * item.scale).coerceAtLeast(56f)
            p.x in (item.x - 18f)..(item.x + estW + 24f) && p.y in (item.y - 18f)..(item.y + estH + 18f)
        }
        is ImageItem -> p.x in item.x..(item.x + item.width) && p.y in item.y..(item.y + item.height)
        is FunctionGraphItem -> p.x in item.x..(item.x + item.width) && p.y in item.y..(item.y + item.height)
        is BohrAtomItem -> hypot(p.x - item.x, p.y - item.y) < 120f * item.scale
        is ChemApparatusItem -> hypot(p.x - item.x, p.y - item.y) < 88f * item.scale
        is PhysicsCircuitItem -> hypot(p.x - item.x, p.y - item.y) < 76f * item.scale
        is ForceVectorItem -> {
            val midX = (item.originX + item.tipX) / 2f
            val midY = (item.originY + item.tipY) / 2f
            hypot(p.x - midX, p.y - midY) < 60f || hypot(p.x - item.tipX, p.y - item.tipY) < 42f
        }
        is EnglishLineItem -> false
    }

    companion object {
        fun factory(repository: NoteRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return Note10ViewModel(repository) as T
                }
            }
    }
}
