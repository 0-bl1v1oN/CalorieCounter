package com.maks.caloriecounter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.maks.caloriecounter.R
import com.maks.caloriecounter.domain.model.MealEntryDetails

@Composable
fun MealEntryCard(
    entry: MealEntryDetails,
    onDelete: () -> Unit,
    onUpdateGrams: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var gramsText by remember(entry.entry.grams) { mutableStateOf(entry.entry.grams.grams()) }
    var isEditing by remember(entry.entry.id) { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Color(0xFF171B22).copy(alpha = 0.98f), Color(0xFF0E1117).copy(alpha = 0.98f))))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FoodAvatar()
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = entry.product.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${entry.entry.grams.grams()} г · ${entry.calories.kcal()} ккал",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFA7A9B2),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MacroChip("Б", entry.protein, Color(0xFFFF6C9A))
                        MacroChip("Ж", entry.fat, Color(0xFFFFA51F))
                        MacroChip("У", entry.carbs, Color(0xFFFF8BB1))
                    }
                }
                RoundActionButton(onClick = { isEditing = !isEditing }, accent = Color.White.copy(alpha = 0.78f)) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Изменить граммы",
                        modifier = Modifier.size(22.dp),
                        tint = Color.White.copy(alpha = 0.9f),
                    )
                }
                RoundActionButton(onClick = onDelete, accent = Color(0xFFFF4D45)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Удалить запись",
                        modifier = Modifier.size(22.dp),
                        tint = Color(0xFFFF4D45),
                    )
                }
            }
            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = gramsText,
                        onValueChange = { gramsText = it },
                        label = { Text("Граммы") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                            focusedBorderColor = Color(0xFFFF6C9A),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        ),
                    )
                    TextButton(
                        onClick = {
                            onUpdateGrams(gramsText)
                            isEditing = false
                        },
                    ) { Text("Сохранить", color = Color(0xFFFF6C9A)) }
                }
            }
        }
    }
}

@Composable
private fun FoodAvatar() {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.radialGradient(listOf(Color(0xFFFF6C9A).copy(alpha = 0.26f), Color(0xFFFF6C9A).copy(alpha = 0.08f))))
            .border(BorderStroke(1.dp, Color(0xFFFF6C9A).copy(alpha = 0.46f)), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.today_product_plate),
            contentDescription = "Иконка продукта",
            modifier = Modifier.size(42.dp),
        )
    }
}

@Composable
private fun MacroChip(label: String, value: Double, color: Color) {
    Text(
        text = "$label ${value.grams()}",
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun RoundActionButton(
    onClick: () -> Unit,
    accent: Color,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(BorderStroke(1.dp, accent.copy(alpha = 0.42f)), CircleShape),
    ) { content() }
}
