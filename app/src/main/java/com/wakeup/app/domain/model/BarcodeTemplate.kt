package com.wakeup.app.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Bathroom
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Predefined templates for physical object barcode missions.
 * Each template provides context, guidance, and categorization for object pairing.
 */
enum class BarcodeTemplate(
    val displayName: String,
    val description: String,
    val icon: ImageVector,
    val category: ObjectCategory,
    val exampleObject: String,
    val setupHint: String
) {
    MORNING_COFFEE(
        displayName = "Morning Coffee",
        description = "Scan your coffee machine, mug, or coffee container",
        icon = Icons.Default.Coffee,
        category = ObjectCategory.KITCHEN,
        exampleObject = "Coffee machine",
        setupHint = "Point camera at the barcode on your coffee machine or coffee bag"
    ),

    MEDICINE_CABINET(
        displayName = "Medicine Cabinet",
        description = "Scan a medicine bottle or first aid item",
        icon = Icons.Default.LocalHospital,
        category = ObjectCategory.BATHROOM,
        exampleObject = "Medicine bottle",
        setupHint = "Scan the barcode on your medicine bottle or vitamin container"
    ),

    GYM_BAG(
        displayName = "Gym Bag",
        description = "Scan your gym bag, water bottle, or fitness equipment",
        icon = Icons.Default.FitnessCenter,
        category = ObjectCategory.FITNESS,
        exampleObject = "Water bottle",
        setupHint = "Scan a barcode on your gym bag, water bottle, or equipment"
    ),

    OFFICE_DESK(
        displayName = "Office Desk",
        description = "Scan an item on your desk or workspace",
        icon = Icons.Default.Work,
        category = ObjectCategory.WORKSPACE,
        exampleObject = "Notebook or supplies",
        setupHint = "Point camera at a barcode on your desk (notebook, supplies, etc.)"
    ),

    KITCHEN_APPLIANCE(
        displayName = "Kitchen Appliance",
        description = "Scan your toaster, microwave, or refrigerator",
        icon = Icons.Default.Kitchen,
        category = ObjectCategory.KITCHEN,
        exampleObject = "Toaster or microwave",
        setupHint = "Scan the barcode on any kitchen appliance or container"
    ),

    BATHROOM_ROUTINE(
        displayName = "Bathroom Routine",
        description = "Scan toothpaste, shampoo, or towel barcode",
        icon = Icons.Default.Bathroom,
        category = ObjectCategory.BATHROOM,
        exampleObject = "Toothpaste or shampoo",
        setupHint = "Point camera at toiletries barcode (toothpaste, shampoo, etc.)"
    ),

    BEDROOM_CHECK(
        displayName = "Bedroom Check",
        description = "Scan an item in another room to force getting up",
        icon = Icons.Default.Bed,
        category = ObjectCategory.BEDROOM,
        exampleObject = "Closet item or dresser",
        setupHint = "Place an item in another room and scan its barcode here"
    ),

    CAR_KEYS(
        displayName = "Car Keys",
        description = "Scan your car keys or something you need before leaving",
        icon = Icons.Default.DirectionsCar,
        category = ObjectCategory.ENTRYWAY,
        exampleObject = "Keychain or wallet",
        setupHint = "Scan your keys, wallet, or something you grab before leaving"
    ),

    CUSTOM(
        displayName = "Custom Object",
        description = "Scan any object of your choice",
        icon = Icons.Default.Kitchen,
        category = ObjectCategory.OTHER,
        exampleObject = "Any object",
        setupHint = "Point camera at any barcode you want to use"
    );

    companion object {
        fun getTemplatesByCategory(category: ObjectCategory): List<BarcodeTemplate> {
            return values().filter { it.category == category && it != CUSTOM }
        }

        fun getAllExceptCustom(): List<BarcodeTemplate> {
            return values().filter { it != CUSTOM }
        }
    }
}

/**
 * Categories for organizing barcode templates
 */
enum class ObjectCategory(val displayName: String) {
    KITCHEN("Kitchen"),
    BATHROOM("Bathroom"),
    BEDROOM("Bedroom"),
    FITNESS("Fitness"),
    WORKSPACE("Workspace"),
    ENTRYWAY("Entryway"),
    OTHER("Other")
}
