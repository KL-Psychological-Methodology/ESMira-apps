package at.jodlidev.esmira.views.inputViews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.ErrorBox
import at.jodlidev.esmira.sharedCode.data_structure.Input


/**
 * Created by SelinaDev on 15.07.2024
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationView(input: Input, get: () -> String, save: (String) -> Unit) {
    var hours: Int? = null
    var minutes: Int? = null
    val value = get()
    if(value.isNotEmpty()) {
        try {
            val valueInt = value.toInt()
            hours = valueInt / 60
            minutes = valueInt % 60
        } catch (_: Throwable) {
            ErrorBox.warn("TimeView", "Value $value in Item ${input.name} is faulty")
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            Text(
                text = stringResource(R.string.duration_hours_abbreviation),
                modifier = Modifier.align(Alignment.CenterVertically).width(25.dp)
                )
            OutlinedTextField(
                value = hours?.toString() ?: "",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = {
                    val newValue = try {
                        valueFromHM(it.toIntOrNull(), minutes)
                    } catch (e: Throwable) {
                        get()
                    }
                    save(newValue)
                },
                modifier = Modifier.width(75.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
            )

            Spacer(modifier = Modifier.width(25.dp))

            Text(
                text = stringResource(R.string.duration_minutes_abbreviation),
                modifier = Modifier.align(Alignment.CenterVertically).width(25.dp)
            )
            OutlinedTextField(
                value = minutes?.toString() ?: "",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange =  {
                    val newValue = try {
                        val newMinutes = it.toIntOrNull()
                        if(newMinutes != null && newMinutes >= 60)
                            get()
                        else
                            valueFromHM(hours, it.toIntOrNull())
                    } catch (e: Throwable) {
                        get()
                    }
                    save(newValue)
                },
                modifier = Modifier.width(75.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
            )
        }
    }
}

fun valueFromHM(hours: Int?, minutes: Int?): String {
    if(hours == null && minutes == null)
        return ""

    return ((hours ?: 0) * 60 + (minutes ?: 0)).toString()
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewDurationView() {
    val input = DbLogic.createJsonObj<Input>("""
        {}
    """)
    ESMiraSurface {
        DurationView(input = input, get = {"158"}) {}
    }
}