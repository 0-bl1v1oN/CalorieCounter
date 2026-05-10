package com.maks.caloriecounter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maks.caloriecounter.domain.model.MealEntryDetails

@Composable
fun MealEntryCard(entry: MealEntryDetails, onDelete: () -> Unit, onUpdateGrams: (String) -> Unit, modifier: Modifier = Modifier) {
    var gramsText by remember(entry.entry.grams) { mutableStateOf(entry.entry.grams.grams()) }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(entry.product.name)
                Text(entry.entry.mealType.title)
            }
            Text("${entry.calories.kcal()} ккал • Б ${entry.protein.grams()} г • Ж ${entry.fat.grams()} г • У ${entry.carbs.grams()} г")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = gramsText, onValueChange = { gramsText = it }, label = { Text("Граммы") }, modifier = Modifier.weight(1f), singleLine = true)
                Button(onClick = { onUpdateGrams(gramsText) }) { Text("Изменить") }
            }
            TextButton(onClick = onDelete) { Text("Удалить") }
        }
    }
}
