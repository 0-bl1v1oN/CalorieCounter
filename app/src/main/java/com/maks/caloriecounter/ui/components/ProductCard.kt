package com.maks.caloriecounter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maks.caloriecounter.domain.model.Product

@Composable
fun ProductCard(product: Product, onEdit: () -> Unit, onDelete: () -> Unit, onQuickAdd: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(product.name)
            Text("${product.caloriesPer100g.kcal()} ккал на 100 г • Б ${product.proteinPer100g.grams()} • Ж ${product.fatPer100g.grams()} • У ${product.carbsPer100g.grams()}")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onQuickAdd) { Text("В дневник") }
                TextButton(onClick = onEdit) { Text("Редактировать") }
                TextButton(onClick = onDelete) { Text("Удалить") }
            }
        }
    }
}
