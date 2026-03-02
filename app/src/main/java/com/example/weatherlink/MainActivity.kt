package com.example.weatherlink

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.unit.dp
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WeatherLinkScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherLinkScreen() {
    val context = LocalContext.current
    var city by rememberSaveable { mutableStateOf("") }
    val periods = listOf("1", "3", "5", "7", "10", "14")
    var selectedPeriod by rememberSaveable { mutableStateOf(periods[1]) }
    val providers = listOf("Gismeteo", "Яндекс Погода")
    var selectedProvider by rememberSaveable { mutableStateOf(providers[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Параметры прогноза",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Город") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )

        Spacer(modifier = Modifier.height(12.dp))

        DropdownSelector(
            label = "Период прогноза (дней)",
            options = periods,
            selected = selectedPeriod,
            onSelected = { selectedPeriod = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DropdownSelector(
            label = "Сайт прогноза",
            options = providers,
            selected = selectedProvider,
            onSelected = { selectedProvider = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (city.isBlank()) {
                    Toast.makeText(context, "Введите город", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val link = buildWeatherUrl(
                    city = city.trim(),
                    periodDays = selectedPeriod,
                    provider = selectedProvider
                )

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                try {
                    context.startActivity(Intent.createChooser(intent, "Открыть прогноз через"))
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(context, "На устройстве не найден браузер", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Открыть ссылку в браузере")
        }
    }
}

private fun buildWeatherUrl(city: String, periodDays: String, provider: String): String {
    return when (provider) {
        "Яндекс Погода" -> {
            val query = URLEncoder.encode("погода $city на $periodDays дней", StandardCharsets.UTF_8.toString())
            "https://yandex.ru/search/?text=$query"
        }

        else -> {
            val query = URLEncoder.encode("gismeteo $city прогноз на $periodDays дней", StandardCharsets.UTF_8.toString())
            "https://www.google.com/search?q=$query&hl=ru"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
