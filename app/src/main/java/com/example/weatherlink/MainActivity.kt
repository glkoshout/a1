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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

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

@Composable
private fun WeatherLinkScreen() {
    val context = LocalContext.current
    var city by rememberSaveable { mutableStateOf("") }

    val periods = listOf("3", "5", "7", "14")
    var selectedPeriod by rememberSaveable { mutableStateOf(periods[0]) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
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

        Spacer(modifier = Modifier.height(16.dp))
        Text("Период прогноза (дней)")
        periods.forEach { period ->
            OptionRow(
                text = period,
                selected = selectedPeriod == period,
                onClick = { selectedPeriod = period }
            )
        }


        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (city.isBlank()) {
                    Toast.makeText(context, "Введите город", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val link = buildWeatherUrl(
                    city = city.trim(),
                    periodDays = selectedPeriod
                )

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                }

                try {
                    context.startActivity(Intent.createChooser(intent, "Открыть прогноз через"))
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(context, "На устройстве не найден браузер", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                    Toast.makeText(context, "Не удалось открыть браузер", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Открыть ссылку в браузере")
        }
    }
}

@Composable
private fun OptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = text)
    }
}

private fun buildWeatherUrl(city: String, periodDays: String): String {
    val citySlug = toYandexCitySlug(city)
    val periodPath = toYandexPeriodPath(periodDays)
    return "https://yandex.ru/pogoda/ru/$citySlug/details/$periodPath"
}

private fun toYandexPeriodPath(periodDays: String): String {
    return when (periodDays) {
        "3" -> "3-day-weather"
        "5" -> "5-day-weather"
        "7" -> "7-day-weather"
        "14" -> "14-day-weather"
        else -> "7-day-weather"
    }
}

private fun toYandexCitySlug(city: String): String {
    val translit = mapOf(
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'е' to "e", 'ё' to "e",
        'ж' to "zh", 'з' to "z", 'и' to "i", 'й' to "y", 'к' to "k", 'л' to "l", 'м' to "m",
        'н' to "n", 'о' to "o", 'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t", 'у' to "u",
        'ф' to "f", 'х' to "h", 'ц' to "ts", 'ч' to "ch", 'ш' to "sh", 'щ' to "sch",
        'ъ' to "", 'ы' to "y", 'ь' to "", 'э' to "e", 'ю' to "yu", 'я' to "ya"
    )

    val raw = city.trim().lowercase()
    val slugBuilder = StringBuilder()

    raw.forEach { ch ->
        when {
            ch in translit -> slugBuilder.append(translit[ch])
            ch.isLetterOrDigit() -> slugBuilder.append(ch)
            ch == ' ' || ch == '-' || ch == '_' -> slugBuilder.append('-')
        }
    }

    return slugBuilder
        .toString()
        .replace(Regex("-+"), "-")
        .trim('-')
        .ifBlank { "moscow" }
}
