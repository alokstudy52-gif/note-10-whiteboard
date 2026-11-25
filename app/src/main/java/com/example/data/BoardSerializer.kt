package com.example.data

import com.example.model.BackgroundStyle
import com.example.model.BoardItem
import com.example.model.BoardPage
import com.example.model.BohrAtomItem
import com.example.model.ChemApparatusItem
import com.example.model.ChemApparatusKind
import com.example.model.EnglishLineItem
import com.example.model.ForceVectorItem
import com.example.model.FunctionGraphItem
import com.example.model.ImageItem
import com.example.model.MindMapNode
import com.example.model.PageFlipEffect
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
import com.example.model.TextAlignOption
import com.example.model.TextItem
import org.json.JSONArray
import org.json.JSONObject

object BoardSerializer {

    fun serializePages(pages: List<BoardPage>): String {
        val arr = JSONArray()
        pages.forEach { page ->
            val obj = JSONObject()
            obj.put("id", page.id)
            obj.put("title", page.title)
            obj.put("groupName", page.groupName)
            obj.put("subjectMode", page.subjectMode.name)
            obj.put("backgroundStyle", page.backgroundStyle.name)
            obj.put("backgroundColorArgb", page.backgroundColorArgb)
            if (page.customBgUri != null) obj.put("customBgUri", page.customBgUri)
            obj.put("flipEffect", page.flipEffect.name)
            val itemsArr = JSONArray()
            page.items.forEach { itemsArr.put(serializeItemToJson(it)) }
            obj.put("items", itemsArr)
            val mmArr = JSONArray()
            page.mindMapNodes.forEach { node ->
                mmArr.put(JSONObject().apply {
                    put("id", node.id)
                    put("label", node.label)
                    put("note", node.note)
                    put("isBig", node.isBig)
                    put("pos", node.positionFraction.toDouble())
                })
            }
            obj.put("mindMapNodes", mmArr)
            arr.put(obj)
        }
        return arr.toString()
    }

    fun deserializePages(json: String): List<BoardPage> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { idx ->
                val obj = arr.getJSONObject(idx)
                val itemsArr = obj.optJSONArray("items") ?: JSONArray()
                val items = (0 until itemsArr.length()).mapNotNull { i ->
                    deserializeItemFromJson(itemsArr.getJSONObject(i))
                }
                val mmArr = obj.optJSONArray("mindMapNodes") ?: JSONArray()
                val nodes = (0 until mmArr.length()).map { i ->
                    val n = mmArr.getJSONObject(i)
                    MindMapNode(
                        id = n.optString("id"),
                        label = n.optString("label"),
                        note = n.optString("note"),
                        isBig = n.optBoolean("isBig", true),
                        positionFraction = n.optDouble("pos", 0.5).toFloat()
                    )
                }
                BoardPage(
                    id = obj.optString("id"),
                    title = obj.optString("title", "Page ${idx + 1}"),
                    groupName = obj.optString("groupName", "Default Group"),
                    subjectMode = enumValueOrDefault(obj.optString("subjectMode"), SubjectMode.STANDARD),
                    backgroundStyle = enumValueOrDefault(obj.optString("backgroundStyle"), BackgroundStyle.SOLID),
                    backgroundColorArgb = obj.optInt("backgroundColorArgb", 0xFF8BC34A.toInt()),
                    customBgUri = if (obj.has("customBgUri")) obj.optString("customBgUri") else null,
                    flipEffect = enumValueOrDefault(obj.optString("flipEffect"), PageFlipEffect.FADE),
                    items = items,
                    mindMapNodes = nodes
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun serializeItem(item: BoardItem): String = serializeItemToJson(item).toString()

    fun deserializeItem(json: String): BoardItem? = try {
        deserializeItemFromJson(JSONObject(json))
    } catch (_: Exception) {
        null
    }

    private fun serializeItemToJson(item: BoardItem): JSONObject {
        val obj = JSONObject()
        obj.put("id", item.id)
        obj.put("locked", item.locked)
        obj.put("zIndex", item.zIndex)
        if (item.hyperlink != null) obj.put("hyperlink", item.hyperlink)

        when (item) {
            is StrokeItem -> {
                obj.put("type", "stroke")
                obj.put("colorArgb", item.colorArgb)
                obj.put("strokeWidth", item.strokeWidth.toDouble())
                obj.put("penType", item.penType.name)
                obj.put("stampKind", item.stampKind.name)
                obj.put("offsetX", item.offsetX.toDouble())
                obj.put("offsetY", item.offsetY.toDouble())
                obj.put("scale", item.scale.toDouble())
                obj.put("rotationDeg", item.rotationDeg.toDouble())
                val pts = JSONArray()
                item.points.forEach { pt ->
                    pts.put(JSONArray().apply {
                        put(pt.x.toDouble())
                        put(pt.y.toDouble())
                        put(pt.widthFactor.toDouble())
                    })
                }
                obj.put("points", pts)
            }
            is Shape2DItem -> {
                obj.put("type", "shape2d")
                obj.put("x", item.x.toDouble())
                obj.put("y", item.y.toDouble())
                obj.put("w", item.width.toDouble())
                obj.put("h", item.height.toDouble())
                obj.put("shapeType", item.shapeType.name)
                obj.put("strokeColorArgb", item.strokeColorArgb)
                if (item.fillColorArgb != null) obj.put("fillColorArgb", item.fillColorArgb)
                obj.put("fillAlpha", item.fillAlpha.toDouble())
                obj.put("strokeWidth", item.strokeWidth.toDouble())
                obj.put("rotationDeg", item.rotationDeg.toDouble())
                obj.put("angleDegrees", item.angleDegrees.toDouble())
                obj.put("arcStartDeg", item.arcStartDeg.toDouble())
                obj.put("arcSweepDeg", item.arcSweepDeg.toDouble())
                obj.put("isSector", item.isSector)
                obj.put("showCenter", item.showCenter)
                obj.put("showRadius", item.showRadius)
                obj.put("showIncircle", item.showIncircle)
                obj.put("showCircumcircle", item.showCircumcircle)
            }
            is Shape3DItem -> {
                obj.put("type", "shape3d")
                obj.put("x", item.x.toDouble())
                obj.put("y", item.y.toDouble())
                obj.put("w", item.width.toDouble())
                obj.put("h", item.height.toDouble())
                obj.put("shape3DType", item.shape3DType.name)
                obj.put("strokeColorArgb", item.strokeColorArgb)
                if (item.fillColorArgb != null) obj.put("fillColorArgb", item.fillColorArgb)
                obj.put("strokeWidth", item.strokeWidth.toDouble())
                obj.put("rotationDeg", item.rotationDeg.toDouble())
            }
            is TextItem -> {
                obj.put("type", "text")
                obj.put("x", item.x.toDouble())
                obj.put("y", item.y.toDouble())
                obj.put("text", item.text)
                obj.put("fontSizeSp", item.fontSizeSp.toDouble())
                obj.put("colorArgb", item.colorArgb)
                if (item.bgColorArgb != null) obj.put("bgColorArgb", item.bgColorArgb)
                obj.put("isBold", item.isBold)
                obj.put("isItalic", item.isItalic)
                obj.put("isUnderline", item.isUnderline)
                obj.put("alignment", item.alignment.name)
                obj.put("isBanner", item.isBanner)
                obj.put("bannerAccentArgb", item.bannerAccentArgb)
                obj.put("rotationDeg", item.rotationDeg.toDouble())
                obj.put("scale", item.scale.toDouble())
            }
            is ImageItem -> {
                obj.put("type", "image")
                obj.put("x", item.x.toDouble())
                obj.put("y", item.y.toDouble())
                obj.put("w", item.width.toDouble())
                obj.put("h", item.height.toDouble())
                obj.put("uriString", item.uriString)
                obj.put("rotationDeg", item.rotationDeg.toDouble())
            }
            is FunctionGraphItem -> {
                obj.put("type", "func")
                obj.put("x", item.x.toDouble())
                obj.put("y", item.y.toDouble())
                obj.put("w", item.width.toDouble())
                obj.put("h", item.height.toDouble())
                obj.put("expression", item.expression)
                obj.put("xMin", item.xMin.toDouble())
                obj.put("xMax", item.xMax.toDouble())
                obj.put("yMin", item.yMin.toDouble())
                obj.put("yMax", item.yMax.toDouble())
                obj.put("showGrid", item.showGrid)
                obj.put("reverseX", item.reverseX)
                obj.put("reverseY", item.reverseY)
                obj.put("lineColorArgb", item.lineColorArgb)
            }
            is BohrAtomItem -> {
                obj.put("type", "bohr")
                obj.put("x", item.x.toDouble())
                obj.put("y", item.y.toDouble())
                obj.put("atomicNumber", item.atomicNumber)
                obj.put("scale", item.scale.toDouble())
            }
            is ChemApparatusItem -> {
                obj.put("type", "chem")
                obj.put("x", item.x.toDouble())
                obj.put("y", item.y.toDouble())
                obj.put("kind", item.kind.name)
                obj.put("liquidLevel", item.liquidLevel.toDouble())
                obj.put("liquidColorArgb", item.liquidColorArgb)
                obj.put("isPlugged", item.isPlugged)
                obj.put("isLit", item.isLit)
                obj.put("rotationDeg", item.rotationDeg.toDouble())
                obj.put("scale", item.scale.toDouble())
            }
            is PhysicsCircuitItem -> {
                obj.put("type", "phys")
                obj.put("x", item.x.toDouble())
                obj.put("y", item.y.toDouble())
                obj.put("kind", item.kind.name)
                obj.put("isSwitchClosed", item.isSwitchClosed)
                obj.put("resistanceOhm", item.resistanceOhm.toDouble())
                obj.put("voltageVolts", item.voltageVolts.toDouble())
                obj.put("scale", item.scale.toDouble())
            }
            is ForceVectorItem -> {
                obj.put("type", "force")
                obj.put("ox", item.originX.toDouble())
                obj.put("oy", item.originY.toDouble())
                obj.put("tx", item.tipX.toDouble())
                obj.put("ty", item.tipY.toDouble())
                obj.put("label", item.label)
                obj.put("colorArgb", item.colorArgb)
            }
            is EnglishLineItem -> {
                obj.put("type", "engline")
                obj.put("rowIndex", item.rowIndex)
                obj.put("text", item.text)
                obj.put("colorArgb", item.colorArgb)
            }
        }
        return obj
    }

    private fun deserializeItemFromJson(obj: JSONObject): BoardItem? {
        val id = obj.optString("id")
        val locked = obj.optBoolean("locked", false)
        val zIndex = obj.optInt("zIndex", 0)
        val link = if (obj.has("hyperlink")) obj.optString("hyperlink") else null
        return when (obj.optString("type")) {
            "stroke" -> {
                val ptsArr = obj.optJSONArray("points") ?: JSONArray()
                val pts = (0 until ptsArr.length()).map { i ->
                    val p = ptsArr.getJSONArray(i)
                    StrokePoint(
                        p.optDouble(0, 0.0).toFloat(),
                        p.optDouble(1, 0.0).toFloat(),
                        p.optDouble(2, 1.0).toFloat()
                    )
                }
                StrokeItem(
                    id = id,
                    points = pts,
                    colorArgb = obj.optInt("colorArgb", 0xFF111827.toInt()),
                    strokeWidth = obj.optDouble("strokeWidth", 6.0).toFloat(),
                    penType = enumValueOrDefault(obj.optString("penType"), PenType.MARKER),
                    stampKind = enumValueOrDefault(obj.optString("stampKind"), StampKind.STAR),
                    offsetX = obj.optDouble("offsetX", 0.0).toFloat(),
                    offsetY = obj.optDouble("offsetY", 0.0).toFloat(),
                    scale = obj.optDouble("scale", 1.0).toFloat(),
                    rotationDeg = obj.optDouble("rotationDeg", 0.0).toFloat(),
                    locked = locked,
                    zIndex = zIndex,
                    hyperlink = link
                )
            }
            "shape2d" -> Shape2DItem(
                id = id,
                x = obj.optDouble("x", 0.0).toFloat(),
                y = obj.optDouble("y", 0.0).toFloat(),
                width = obj.optDouble("w", 120.0).toFloat(),
                height = obj.optDouble("h", 120.0).toFloat(),
                shapeType = enumValueOrDefault(obj.optString("shapeType"), Shape2DType.RECTANGLE),
                strokeColorArgb = obj.optInt("strokeColorArgb", 0xFF111827.toInt()),
                fillColorArgb = if (obj.has("fillColorArgb")) obj.optInt("fillColorArgb") else null,
                fillAlpha = obj.optDouble("fillAlpha", 0.35).toFloat(),
                strokeWidth = obj.optDouble("strokeWidth", 5.0).toFloat(),
                rotationDeg = obj.optDouble("rotationDeg", 0.0).toFloat(),
                angleDegrees = obj.optDouble("angleDegrees", 45.0).toFloat(),
                arcStartDeg = obj.optDouble("arcStartDeg", 0.0).toFloat(),
                arcSweepDeg = obj.optDouble("arcSweepDeg", 360.0).toFloat(),
                isSector = obj.optBoolean("isSector", false),
                showCenter = obj.optBoolean("showCenter", false),
                showRadius = obj.optBoolean("showRadius", false),
                showIncircle = obj.optBoolean("showIncircle", false),
                showCircumcircle = obj.optBoolean("showCircumcircle", false),
                locked = locked,
                zIndex = zIndex,
                hyperlink = link
            )
            "shape3d" -> Shape3DItem(
                id = id,
                x = obj.optDouble("x", 0.0).toFloat(),
                y = obj.optDouble("y", 0.0).toFloat(),
                width = obj.optDouble("w", 160.0).toFloat(),
                height = obj.optDouble("h", 160.0).toFloat(),
                shape3DType = enumValueOrDefault(obj.optString("shape3DType"), Shape3DType.CUBOID),
                strokeColorArgb = obj.optInt("strokeColorArgb", 0xFF111827.toInt()),
                fillColorArgb = if (obj.has("fillColorArgb")) obj.optInt("fillColorArgb") else null,
                strokeWidth = obj.optDouble("strokeWidth", 4.5).toFloat(),
                rotationDeg = obj.optDouble("rotationDeg", 0.0).toFloat(),
                locked = locked,
                zIndex = zIndex,
                hyperlink = link
            )
            "text" -> TextItem(
                id = id,
                x = obj.optDouble("x", 100.0).toFloat(),
                y = obj.optDouble("y", 100.0).toFloat(),
                text = obj.optString("text", ""),
                fontSizeSp = obj.optDouble("fontSizeSp", 28.0).toFloat(),
                colorArgb = obj.optInt("colorArgb", 0xFF111827.toInt()),
                bgColorArgb = if (obj.has("bgColorArgb")) obj.optInt("bgColorArgb") else null,
                isBold = obj.optBoolean("isBold", false),
                isItalic = obj.optBoolean("isItalic", false),
                isUnderline = obj.optBoolean("isUnderline", false),
                alignment = enumValueOrDefault(obj.optString("alignment"), TextAlignOption.LEFT),
                isBanner = obj.optBoolean("isBanner", false),
                bannerAccentArgb = obj.optInt("bannerAccentArgb", 0xFF0284C7.toInt()),
                rotationDeg = obj.optDouble("rotationDeg", 0.0).toFloat(),
                scale = obj.optDouble("scale", 1.0).toFloat(),
                locked = locked,
                zIndex = zIndex,
                hyperlink = link
            )
            "image" -> ImageItem(
                id = id,
                x = obj.optDouble("x", 80.0).toFloat(),
                y = obj.optDouble("y", 80.0).toFloat(),
                width = obj.optDouble("w", 280.0).toFloat(),
                height = obj.optDouble("h", 220.0).toFloat(),
                uriString = obj.optString("uriString", ""),
                rotationDeg = obj.optDouble("rotationDeg", 0.0).toFloat(),
                locked = locked,
                zIndex = zIndex,
                hyperlink = link
            )
            "func" -> FunctionGraphItem(
                id = id,
                x = obj.optDouble("x", 80.0).toFloat(),
                y = obj.optDouble("y", 120.0).toFloat(),
                width = obj.optDouble("w", 460.0).toFloat(),
                height = obj.optDouble("h", 340.0).toFloat(),
                expression = obj.optString("expression", "sin(x)"),
                xMin = obj.optDouble("xMin", -10.0).toFloat(),
                xMax = obj.optDouble("xMax", 10.0).toFloat(),
                yMin = obj.optDouble("yMin", -6.0).toFloat(),
                yMax = obj.optDouble("yMax", 6.0).toFloat(),
                showGrid = obj.optBoolean("showGrid", true),
                reverseX = obj.optBoolean("reverseX", false),
                reverseY = obj.optBoolean("reverseY", false),
                lineColorArgb = obj.optInt("lineColorArgb", 0xFFDC2626.toInt()),
                locked = locked,
                zIndex = zIndex,
                hyperlink = link
            )
            "bohr" -> BohrAtomItem(
                id = id,
                x = obj.optDouble("x", 180.0).toFloat(),
                y = obj.optDouble("y", 180.0).toFloat(),
                atomicNumber = obj.optInt("atomicNumber", 20),
                scale = obj.optDouble("scale", 1.0).toFloat(),
                locked = locked,
                zIndex = zIndex,
                hyperlink = link
            )
            "chem" -> ChemApparatusItem(
                id = id,
                x = obj.optDouble("x", 180.0).toFloat(),
                y = obj.optDouble("y", 220.0).toFloat(),
                kind = enumValueOrDefault(obj.optString("kind"), ChemApparatusKind.TEST_TUBE),
                liquidLevel = obj.optDouble("liquidLevel", 0.55).toFloat(),
                liquidColorArgb = obj.optInt("liquidColorArgb", 0xFF22D3EE.toInt()),
                isPlugged = obj.optBoolean("isPlugged", false),
                isLit = obj.optBoolean("isLit", false),
                rotationDeg = obj.optDouble("rotationDeg", 0.0).toFloat(),
                scale = obj.optDouble("scale", 1.0).toFloat(),
                locked = locked,
                zIndex = zIndex,
                hyperlink = link
            )
            "phys" -> PhysicsCircuitItem(
                id = id,
                x = obj.optDouble("x", 180.0).toFloat(),
                y = obj.optDouble("y", 200.0).toFloat(),
                kind = enumValueOrDefault(obj.optString("kind"), PhysicsInstrumentKind.BULB),
                isSwitchClosed = obj.optBoolean("isSwitchClosed", true),
                resistanceOhm = obj.optDouble("resistanceOhm", 6.0).toFloat(),
                voltageVolts = obj.optDouble("voltageVolts", 12.0).toFloat(),
                scale = obj.optDouble("scale", 1.0).toFloat(),
                locked = locked,
                zIndex = zIndex,
                hyperlink = link
            )
            "force" -> ForceVectorItem(
                id = id,
                originX = obj.optDouble("ox", 300.0).toFloat(),
                originY = obj.optDouble("oy", 300.0).toFloat(),
                tipX = obj.optDouble("tx", 420.0).toFloat(),
                tipY = obj.optDouble("ty", 260.0).toFloat(),
                label = obj.optString("label", "F"),
                colorArgb = obj.optInt("colorArgb", 0xFFFACC15.toInt()),
                locked = locked,
                zIndex = zIndex,
                hyperlink = link
            )
            "engline" -> EnglishLineItem(
                id = id,
                rowIndex = obj.optInt("rowIndex", 0),
                text = obj.optString("text", ""),
                colorArgb = obj.optInt("colorArgb", 0xFF1F2937.toInt()),
                locked = locked,
                zIndex = zIndex,
                hyperlink = link
            )
            else -> null
        }
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(name: String, default: T): T {
        return try {
            enumValueOf<T>(name)
        } catch (_: Exception) {
            default
        }
    }
}
