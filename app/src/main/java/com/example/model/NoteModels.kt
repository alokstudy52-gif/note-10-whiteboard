package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.util.UUID

enum class SubjectMode(
    val title: String,
    val shortLabel: String,
    val defaultBoardColor: Long,
    val defaultBgStyle: BackgroundStyle
) {
    STANDARD("Standard", "Board", 0xFF8BC34AL, BackgroundStyle.SOLID),
    MATH("Math", "Math", 0xFFF9FBF8L, BackgroundStyle.GRID),
    ENGLISH("English", "Eng", 0xFFF4F1E4L, BackgroundStyle.ENGLISH_FOUR_LINE),
    PHYSICS("Physics", "Phys", 0xFF1D3B8BL, BackgroundStyle.SOLID),
    CHEMISTRY("Chemistry", "Chem", 0xFF5D92C7L, BackgroundStyle.SOLID)
}

enum class WorkMode(val label: String) {
    PRESENTING("Presenting"),
    PREPARATION("Preparation"),
    FOCUS("Clean Board")
}

enum class ActiveTool {
    SELECT,
    PEN,
    ERASER,
    SHAPE_2D,
    SHAPE_3D,
    TEXT,
    ROAM,
    FORCE_VECTOR
}

enum class PenType(val label: String) {
    MARKER("Marker"),
    PAINTBRUSH("Brush"),
    HIGHLIGHTER("Highlight"),
    CALLIGRAPHY("Soft Pen"),
    LASER("Laser"),
    STAMP("Stamp")
}

enum class StampKind(val label: String, val glyph: String) {
    STAR("Star", "★"),
    HEART("Heart", "♥"),
    CHECK("Check", "✓"),
    SPARKLE("Spark", "✦"),
    SMILE("Smile", "☺"),
    ARROW("Arrow", "➤"),
    FLOWER("Flower", "✿"),
    DIAMOND("Gem", "◆")
}

enum class EraserType(val label: String) {
    LINE("Line Eraser"),
    SELECT_AREA("Area Eraser")
}

enum class Shape2DType(val label: String, val isLineLike: Boolean = false) {
    SMART_RECOGNIZER("Smart Auto"),
    CIRCLE("Circle"),
    ELLIPSE("Ellipse"),
    RECTANGLE("Rectangle"),
    PARALLELOGRAM("Parallelogram"),
    RIGHT_TRIANGLE("Right Tri"),
    ISOSCELES_TRIANGLE("Iso Tri"),
    TRAPEZOID("Trapezoid"),
    LINE_SEGMENT("Line", true),
    DOTTED_LINE("Dotted", true),
    WAVE_LINE("Wave", true),
    ARROW("Arrow", true),
    ANGLE_MARK("Angle", true)
}

enum class Shape3DType(val label: String) {
    CUBOID("Cuboid"),
    TRIANGULAR_PYRAMID("Tri Pyramid"),
    RECTANGULAR_PYRAMID("Rect Pyramid"),
    CYLINDER("Cylinder"),
    CONE("Cone"),
    TRUNCATED_CONE("Frustum"),
    HEMISPHERE("Hemisphere"),
    SPHERE("Sphere"),
    DIHEDRON("Dihedron")
}

enum class BackgroundStyle(val label: String) {
    SOLID("Solid Color"),
    GRID("Square Grid"),
    RULED("Ruled Lines"),
    ENGLISH_FOUR_LINE("English 4-Line"),
    MUSIC_STAVE("Music Stave"),
    COORDINATE_AXES("Graph Axes"),
    CHESSBOARD("Chessboard"),
    SCENIC_INK("Ink Wash"),
    SCENIC_SKY("Sky Studio"),
    CUSTOM_IMAGE("Custom Photo")
}

enum class PageFlipEffect(val label: String) {
    NONE("Instant"),
    FADE("Fade"),
    SLIDE("Slide"),
    SHUTTER("Shutter"),
    RIPPLE_ZOOM("Zoom Ripple")
}

enum class PageManagerViewMode {
    LIST,
    CRESCENT,
    PAGE_BALL
}

enum class ChemApparatusKind(val label: String, val canPour: Boolean, val hasPlug: Boolean) {
    TEST_TUBE("Test Tube", true, true),
    BEAKER("Beaker", true, false),
    CONICAL_FLASK("Conical Flask", true, true),
    ALCOHOL_LAMP("Alcohol Lamp", false, false)
}

enum class PhysicsInstrumentKind(val label: String) {
    BATTERY("Power 12V"),
    SWITCH("Switch"),
    BULB("Light Bulb"),
    AMMETER("Ammeter (A)"),
    VOLTMETER("Voltmeter (V)"),
    RHEOSTAT("Rheostat (Ω)")
}

enum class TextAlignOption(val label: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right")
}

data class StrokePoint(
    val x: Float,
    val y: Float,
    val widthFactor: Float = 1f
)

sealed interface BoardItem {
    val id: String
    val locked: Boolean
    val zIndex: Int
    val hyperlink: String?
}

data class StrokeItem(
    override val id: String = UUID.randomUUID().toString(),
    val points: List<StrokePoint>,
    val colorArgb: Int,
    val strokeWidth: Float,
    val penType: PenType,
    val stampKind: StampKind = StampKind.STAR,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val rotationDeg: Float = 0f,
    override val locked: Boolean = false,
    override val zIndex: Int = 0,
    override val hyperlink: String? = null
) : BoardItem

data class Shape2DItem(
    override val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val shapeType: Shape2DType,
    val strokeColorArgb: Int,
    val fillColorArgb: Int? = null,
    val fillAlpha: Float = 0.35f,
    val strokeWidth: Float = 5f,
    val rotationDeg: Float = 0f,
    val angleDegrees: Float = 45f,
    val arcStartDeg: Float = 0f,
    val arcSweepDeg: Float = 360f,
    val isSector: Boolean = false,
    val showCenter: Boolean = false,
    val showRadius: Boolean = false,
    val showIncircle: Boolean = false,
    val showCircumcircle: Boolean = false,
    override val locked: Boolean = false,
    override val zIndex: Int = 0,
    override val hyperlink: String? = null
) : BoardItem

data class Shape3DItem(
    override val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val shape3DType: Shape3DType,
    val strokeColorArgb: Int,
    val fillColorArgb: Int? = null,
    val strokeWidth: Float = 4.5f,
    val rotationDeg: Float = 0f,
    override val locked: Boolean = false,
    override val zIndex: Int = 0,
    override val hyperlink: String? = null
) : BoardItem

data class TextItem(
    override val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val text: String,
    val fontSizeSp: Float = 28f,
    val colorArgb: Int = Color(0xFF111827).toArgb(),
    val bgColorArgb: Int? = null,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val alignment: TextAlignOption = TextAlignOption.LEFT,
    val isBanner: Boolean = false,
    val bannerAccentArgb: Int = 0xFF0284C7.toInt(),
    val rotationDeg: Float = 0f,
    val scale: Float = 1f,
    override val locked: Boolean = false,
    override val zIndex: Int = 0,
    override val hyperlink: String? = null
) : BoardItem

data class ImageItem(
    override val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val uriString: String,
    val rotationDeg: Float = 0f,
    override val locked: Boolean = false,
    override val zIndex: Int = 0,
    override val hyperlink: String? = null
) : BoardItem

data class FunctionGraphItem(
    override val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val expression: String,
    val xMin: Float = -10f,
    val xMax: Float = 10f,
    val yMin: Float = -6f,
    val yMax: Float = 6f,
    val showGrid: Boolean = true,
    val reverseX: Boolean = false,
    val reverseY: Boolean = false,
    val lineColorArgb: Int = 0xFFDC2626.toInt(),
    override val locked: Boolean = false,
    override val zIndex: Int = 0,
    override val hyperlink: String? = null
) : BoardItem

data class BohrAtomItem(
    override val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val atomicNumber: Int = 20, // Calcium default just like Note3!
    val scale: Float = 1f,
    override val locked: Boolean = false,
    override val zIndex: Int = 0,
    override val hyperlink: String? = null
) : BoardItem

data class ChemApparatusItem(
    override val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val kind: ChemApparatusKind,
    val liquidLevel: Float = 0.55f,
    val liquidColorArgb: Int = 0xFF22D3EE.toInt(),
    val isPlugged: Boolean = false,
    val isLit: Boolean = false,
    val rotationDeg: Float = 0f,
    val scale: Float = 1f,
    override val locked: Boolean = false,
    override val zIndex: Int = 0,
    override val hyperlink: String? = null
) : BoardItem

data class PhysicsCircuitItem(
    override val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val kind: PhysicsInstrumentKind,
    val isSwitchClosed: Boolean = true,
    val resistanceOhm: Float = 6f,
    val voltageVolts: Float = 12f,
    val scale: Float = 1f,
    override val locked: Boolean = false,
    override val zIndex: Int = 0,
    override val hyperlink: String? = null
) : BoardItem

data class ForceVectorItem(
    override val id: String = UUID.randomUUID().toString(),
    val originX: Float,
    val originY: Float,
    val tipX: Float,
    val tipY: Float,
    val label: String = "F",
    val colorArgb: Int = 0xFFFACC15.toInt(),
    override val locked: Boolean = false,
    override val zIndex: Int = 0,
    override val hyperlink: String? = null
) : BoardItem

data class EnglishLineItem(
    override val id: String = UUID.randomUUID().toString(),
    val rowIndex: Int,
    val text: String,
    val colorArgb: Int = 0xFF1F2937.toInt(),
    override val locked: Boolean = false,
    override val zIndex: Int = 0,
    override val hyperlink: String? = null
) : BoardItem

data class LaserTrail(
    val id: String = UUID.randomUUID().toString(),
    val points: List<StrokePoint>,
    val colorArgb: Int,
    val createdAtMs: Long = System.currentTimeMillis()
)

data class MindMapNode(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val note: String = "",
    val isBig: Boolean = true,
    val positionFraction: Float = 0.5f
)

data class BoardPage(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Page 1",
    val groupName: String = "Default Group",
    val subjectMode: SubjectMode = SubjectMode.STANDARD,
    val backgroundStyle: BackgroundStyle = BackgroundStyle.SOLID,
    val backgroundColorArgb: Int = 0xFF8BC34A.toInt(),
    val customBgUri: String? = null,
    val flipEffect: PageFlipEffect = PageFlipEffect.FADE,
    val items: List<BoardItem> = emptyList(),
    val mindMapNodes: List<MindMapNode> = emptyList()
)

data class AppSettings(
    val defaultPenColorArgb: Int = 0xFF111827.toInt(),
    val defaultFillColorArgb: Int = 0xFF2563EB.toInt(),
    val defaultBoardColorArgb: Int = 0xFF8BC34A.toInt(),
    val defaultFontSizeSp: Float = 28f,
    val ttsSpeechRate: Float = 1.0f,
    val showClock: Boolean = true,
    val showToolbarLabels: Boolean = false,
    val autoSave: Boolean = true
)

data class OverlayToolsState(
    val spotlightActive: Boolean = false,
    val spotlightIsRect: Boolean = false,
    val spotlightDimAlpha: Float = 0.78f,
    val screenWrapActive: Boolean = false,
    val magnifierActive: Boolean = false,
    val magnifierZoom: Float = 2.0f,
    val clockActive: Boolean = false,
    val calendarActive: Boolean = false,
    val timerActive: Boolean = false,
    val writingBoardActive: Boolean = false,
    val mindMapActive: Boolean = false,
    val calculatorActive: Boolean = false,
    val rulerActive: Boolean = false,
    val setSquare30Active: Boolean = false,
    val setSquare45Active: Boolean = false,
    val protractorActive: Boolean = false,
    val compassActive: Boolean = false,
    val screenshotSelectActive: Boolean = false,
    val periodicTableOpen: Boolean = false,
    val functionEditorOpen: Boolean = false,
    val mathFormulaEditorOpen: Boolean = false,
    val chemFormulaEditorOpen: Boolean = false,
    val encyclopediaOpen: Boolean = false
)

enum class OpenPopover {
    NONE,
    PEN,
    ERASER,
    SHAPE_2D,
    SHAPE_3D,
    ROAM,
    MAIN_MENU,
    TREASURE_CHEST,
    RESOURCE_LIBRARY,
    PAGE_MANAGER,
    TEXT_EDITOR,
    SETTINGS,
    NOTEBOOKS,
    HELP_GUIDE,
    ABOUT,
    HYPERLINK_EDITOR
}
