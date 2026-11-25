package com.example.ui.panels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubjectMode
import com.example.ui.theme.DockBorder
import com.example.ui.theme.DockCharcoal
import com.example.ui.theme.DockOnSurface
import com.example.ui.theme.DockSurface
import com.example.ui.theme.NoteLeafGreen

/**
 * Note3 Fig 3-3 Left-Edge Subject Mode Switcher (Standard, Math, English, Physics, Chemistry)
 */
@Composable
fun SubjectEdgeRail(
    activeSubject: SubjectMode,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onSelectSubject: (SubjectMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
        ) {
            Surface(
                shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                color = DockCharcoal.copy(alpha = 0.94f),
                shadowElevation = 10.dp,
                modifier = Modifier.border(
                    1.dp,
                    DockBorder,
                    RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SubjectMode.entries.forEach { mode ->
                        val isSelected = mode == activeSubject
                        val icon: ImageVector = when (mode) {
                            SubjectMode.STANDARD -> Icons.Default.Dashboard
                            SubjectMode.MATH -> Icons.Default.Calculate
                            SubjectMode.ENGLISH -> Icons.Default.Abc
                            SubjectMode.PHYSICS -> Icons.Default.Bolt
                            SubjectMode.CHEMISTRY -> Icons.Default.Science
                        }
                        val accent = when (mode) {
                            SubjectMode.STANDARD -> NoteLeafGreen
                            SubjectMode.MATH -> Color(0xFF38BDF8)
                            SubjectMode.ENGLISH -> Color(0xFFF59E0B)
                            SubjectMode.PHYSICS -> Color(0xFF818CF8)
                            SubjectMode.CHEMISTRY -> Color(0xFF2DD4BF)
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) accent.copy(alpha = 0.24f) else DockSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) accent else Color.Transparent,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelectSubject(mode) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("subject_mode_${mode.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(icon, contentDescription = mode.title, tint = if (isSelected) accent else DockOnSurface, modifier = Modifier.size(20.dp))
                            Text(
                                text = mode.title,
                                color = if (isSelected) accent else DockOnSurface,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Tactile Left-Edge Pull Handle
        Surface(
            shape = RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp),
            color = DockCharcoal.copy(alpha = 0.82f),
            shadowElevation = 6.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp))
                .clickable(onClick = onToggleExpand)
                .testTag("subject_rail_handle")
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (expanded) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Switch subject mode",
                    tint = NoteLeafGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
