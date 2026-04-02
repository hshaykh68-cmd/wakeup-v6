package com.wakeup.app.presentation.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.domain.model.BarcodeTemplate
import com.wakeup.app.domain.model.ObjectCategory

/**
 * Template selection grid for barcode mission setup.
 * Allows users to choose from predefined lifestyle templates or custom option.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BarcodeTemplateSelection(
    onTemplateSelected: (BarcodeTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<ObjectCategory?>(null) }
    var selectedTemplate by remember { mutableStateOf<BarcodeTemplate?>(null) }

    val templates = BarcodeTemplate.getAllExceptCustom()
    val categories = ObjectCategory.values().toList()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Choose an Object",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select a template to pair your alarm with a physical object",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WakeUpColors.iosBlue,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.1f),
                        labelColor = Color.White
                    )
                )
            }

            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WakeUpColors.iosBlue,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.1f),
                        labelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Template grid
        val filteredTemplates = if (selectedCategory != null) {
            templates.filter { it.category == selectedCategory }
        } else {
            templates
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            filteredTemplates.forEach { template ->
                TemplateCard(
                    template = template,
                    isSelected = selectedTemplate == template,
                    onClick = {
                        selectedTemplate = template
                        onTemplateSelected(template)
                    }
                )
            }

            // Custom option
            TemplateCard(
                template = null, // Custom
                isSelected = selectedTemplate == BarcodeTemplate.CUSTOM,
                onClick = {
                    selectedTemplate = BarcodeTemplate.CUSTOM
                    onTemplateSelected(BarcodeTemplate.CUSTOM)
                }
            )
        }

        // Selection hint
        selectedTemplate?.let { template ->
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(WakeUpColors.iosBlue.copy(alpha = 0.2f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Next: ${template.displayName}",
                        fontWeight = FontWeight.Medium,
                        color = WakeUpColors.iosBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = template.setupHint,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: BarcodeTemplate?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val displayName = template?.displayName ?: "Custom Object"
    val description = template?.description ?: "Scan any barcode you choose"
    val icon = template?.icon ?: androidx.compose.material.icons.Icons.Default.CheckCircle
    val categoryColor = when (template?.category) {
        ObjectCategory.KITCHEN -> WakeUpColors.iosOrange
        ObjectCategory.BATHROOM -> WakeUpColors.iosBlue
        ObjectCategory.BEDROOM -> WakeUpColors.iosPurple
        ObjectCategory.FITNESS -> WakeUpColors.iosGreen
        ObjectCategory.WORKSPACE -> WakeUpColors.iosYellow
        ObjectCategory.ENTRYWAY -> WakeUpColors.iosRed
        ObjectCategory.OTHER, null -> WakeUpColors.iosGray
    }

    Card(
        modifier = Modifier
            .size(160.dp, 140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) categoryColor.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, categoryColor)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon with category color
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp
            )

            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = categoryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
