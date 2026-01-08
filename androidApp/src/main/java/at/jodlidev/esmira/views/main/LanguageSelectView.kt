package at.jodlidev.esmira.views.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.elements.LanguageOptionView
import java.util.Locale
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.views.TextButtonIconLeft
import kotlin.collections.iterator

@Composable
fun LanguageSelectView(getStudy: () -> Study, goBack: () -> Unit, afterUpdate: () -> Unit) {
    val study = getStudy()
    val languages = study.getAvailableLangs().mapNotNull { lang ->
        val locale = Locale.forLanguageTag(lang)
        if(locale.language.isNotEmpty()) locale to lang else null
    }.toMap()

    DefaultScaffoldView(
        title = stringResource(R.string.change_language),
        goBack = goBack,
    ) {
        val selectedLanguage = remember { mutableStateOf(study.lang) }
        Column(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(all = 10.dp)
        ) {
            Text(stringResource(R.string.select_language_instruction))

            Spacer(modifier = Modifier.height(5.dp))

            for((locale, language) in languages) {
                LanguageOptionView(
                    locale,
                    isSelected = {
                        language ==selectedLanguage.value
                    },
                    onSelected = {
                        selectedLanguage.value = language
                    }
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ){
                TextButtonIconLeft(
                    text = stringResource(R.string.save),
                    icon = Icons.Default.Save,
                    onClick = {
                        study.saveLanguage(selectedLanguage.value)
                        Web.updateStudiesAsync(true, arrayOf(study.id)) {
                            afterUpdate()
                        }
                        goBack()
                    })
            }
        }
    }
}