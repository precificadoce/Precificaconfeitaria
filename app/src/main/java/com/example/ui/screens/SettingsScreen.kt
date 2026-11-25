package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.BusinessSettings
import com.example.data.preferences.ThemeMode
import com.example.domain.PricingCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: BusinessSettings,
    onSaveSettings: (BusinessSettings) -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var businessName by remember(settings) { mutableStateOf(settings.businessName) }
    var hourlyRateText by remember(settings) { mutableStateOf(settings.hourlyRate.toString()) }
    var overheadText by remember(settings) { mutableStateOf(settings.defaultOverheadPercent.toString()) }
    var markupText by remember(settings) { mutableStateOf(settings.defaultProfitMarkupPercent.toString()) }
    var feeText by remember(settings) { mutableStateOf(settings.defaultCardFeePercent.toString()) }
    var themeMode by remember(settings) { mutableStateOf(settings.themeMode) }

    // Salary-to-hourly helper
    var monthlyGoalText by remember { mutableStateOf("4000") }
    var daysPerMonthText by remember { mutableStateOf("22") }
    var hoursPerDayText by remember { mutableStateOf("8") }

    Scaffold(
        modifier = modifier.testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Ajustes do Negócio") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Nome da Confeitaria") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_business_name")
                )
            }

            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Calculadora de Valor da Hora", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Descubra quanto cobrar pela sua mão de obra com base no salário mensal desejado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = monthlyGoalText,
                                onValueChange = { monthlyGoalText = it },
                                label = { Text("Meta (R$/mês)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1.3f)
                            )
                            OutlinedTextField(
                                value = daysPerMonthText,
                                onValueChange = { daysPerMonthText = it.filter { c -> c.isDigit() } },
                                label = { Text("Dias") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(0.85f)
                            )
                            OutlinedTextField(
                                value = hoursPerDayText,
                                onValueChange = { hoursPerDayText = it.filter { c -> c.isDigit() } },
                                label = { Text("Hrs/dia") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(0.85f)
                            )
                        }
                        val goal = monthlyGoalText.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val days = (daysPerMonthText.toIntOrNull() ?: 1).coerceAtLeast(1)
                        val hrs = (hoursPerDayText.toIntOrNull() ?: 1).coerceAtLeast(1)
                        val computedRate = goal / (days * hrs)
                        OutlinedButton(
                            onClick = { hourlyRateText = String.format(java.util.Locale.US, "%.2f", computedRate) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("apply_hourly_rate_btn")
                        ) {
                            Text("Aplicar ${PricingCalculator.formatBrl(computedRate)} / hora")
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Padrões de Precificação", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = hourlyRateText,
                            onValueChange = { hourlyRateText = it },
                            label = { Text("Valor da Hora de Trabalho (R$/h)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = overheadText,
                                onValueChange = { overheadText = it },
                                label = { Text("Indiretos (%)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = markupText,
                                onValueChange = { markupText = it },
                                label = { Text("Markup (%)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = feeText,
                                onValueChange = { feeText = it },
                                label = { Text("Taxa (%)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Text("Aparência do App", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = { themeMode = mode },
                            label = { Text(mode.label) }
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        onSaveSettings(
                            BusinessSettings(
                                businessName = businessName,
                                hourlyRate = hourlyRateText.replace(',', '.').toDoubleOrNull() ?: settings.hourlyRate,
                                defaultOverheadPercent = overheadText.replace(',', '.').toDoubleOrNull() ?: settings.defaultOverheadPercent,
                                defaultProfitMarkupPercent = markupText.replace(',', '.').toDoubleOrNull() ?: settings.defaultProfitMarkupPercent,
                                defaultCardFeePercent = feeText.replace(',', '.').toDoubleOrNull() ?: settings.defaultCardFeePercent,
                                themeMode = themeMode
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("settings_save_btn")
                ) {
                    Text("Salvar Ajustes")
                }
            }

            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.local_storage_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}
