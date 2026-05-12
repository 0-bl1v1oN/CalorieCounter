package com.maks.caloriecounter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
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
        shape = RoundedCornerShape(CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, CardStroke),
    ) {
        Column(
            modifier = Modifier
                .background(CardBrush)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                FoodAvatar()
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(
                        text = entry.product.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            lineBreak = LineBreak.Heading,
                            hyphens = Hyphens.None,
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${entry.entry.grams.grams()} г • ${entry.calories.kcal()} ккал",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 58.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MacroPill(label = "Б", value = entry.protein, color = ProteinPink, modifier = Modifier.weight(1f))
                MacroPill(label = "Ж", value = entry.fat, color = FatAmber, modifier = Modifier.weight(1f))
                MacroPill(label = "У", value = entry.carbs, color = CarbsPink, modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SecondaryActionButton(
                    onClick = { isEditing = !isEditing },
                    contentDescription = "Изменить граммы",
                    iconTint = SecondaryText,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Изменить граммы",
                        modifier = Modifier.size(18.dp),
                        tint = SecondaryText.copy(alpha = 0.72f),
                    )
                }
                SecondaryActionButton(
                    onClick = onDelete,
                    contentDescription = "Удалить запись",
                    iconTint = DangerText,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Удалить запись",
                        modifier = Modifier.size(18.dp),
                        tint = DangerText.copy(alpha = 0.68f),
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
                            focusedBorderColor = AccentColor,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        ),
                    )
                    TextButton(
                        onClick = {
                            onUpdateGrams(gramsText)
                            isEditing = false
                        },
                    ) { Text("Сохранить", color = AccentColor) }
                }
            }
        }
    }
}

@Composable
private fun FoodAvatar() {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.radialGradient(listOf(AccentColor.copy(alpha = 0.18f), Color.White.copy(alpha = 0.045f))))
            .border(BorderStroke(1.dp, AccentColor.copy(alpha = 0.22f)), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.today_product_plate),
            contentDescription = "Иконка продукта",
            modifier = Modifier.size(30.dp),
        )
    }
}

@Composable
private fun MacroPill(
    label: String,
    value: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.10f))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.18f)), RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$label ${value.grams()}",
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
    }
}

@Composable
private fun SecondaryActionButton(
    onClick: () -> Unit,
    contentDescription: String,
    iconTint: Color,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.055f))
                .border(BorderStroke(1.dp, iconTint.copy(alpha = 0.12f)), CircleShape),
            contentAlignment = Alignment.Center,
        ) { content() }
    }
}

private val CardCornerRadius = 22.dp
pprivate val AccentColor = Color(0xFFFF6C9A)
private val ProteinPink = Color(0xFFFF6C9A)
private val FatAmber = Color(0xFFFFA51F)
private val CarbsPink = Color(0xFFFF8BB1)
private val PrimaryText = Color(0xFFF4F6FA)
private val SecondaryText = Color(0xFFA7A9B2)
private val DangerText = Color(0xFFFF7C72)
private val CardStroke = Color.White.copy(alpha = 0.085f)
private val CardBrush = Brush.linearGradient(
    listOf(
        Color(0xFF151922).copy(alpha = 0.98f),
        Color(0xFF0C0F15).copy(alpha = 0.99f),
    ),
)
