package at.jodlidev.esmira.views.welcome

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Study
import kotlinx.coroutines.selects.select
import java.util.Locale

@Composable
fun LanguageOptionLineView(locale: Locale, isSelected: () -> Boolean, onSelected: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.selectable(selected = isSelected(), onClick = onSelected, role = Role.RadioButton).padding(8.dp)
    ) {
        RadioButton(
            selected = isSelected(),
            onClick = null
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(locale.displayName, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LangQuestionView(study: Study, updateStudies: (lang: String) -> Unit, gotoPrevious: () -> Unit, gotoNext: () -> Unit) {
    val languages = remember {
        study.getAvailableLangs().mapNotNull { lang ->
            val locale = Locale.forLanguageTag(lang)
            if (locale.language.isNotEmpty()) locale to lang else null
        }.toMap()
    }

    val selectedLanguage = remember { mutableStateOf(study.lang) }

    ConstraintLayout(modifier = Modifier.fillMaxSize().padding(all = 20.dp)) {
        val (languageOptions, navigation) = createRefs()

        Column(modifier = Modifier
            .verticalScroll(rememberScrollState())
            .constrainAs(languageOptions) {
                top.linkTo(parent.top)
                bottom.linkTo(navigation.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            }
        ) {
            Text(text = stringResource(R.string.available_languages))

            Spacer(modifier = Modifier.height(10.dp))

            for((locale, language) in languages) {
                LanguageOptionLineView(
                    locale,
                    isSelected = { language == selectedLanguage.value },
                    onSelected = {
                        selectedLanguage.value = language
                        updateStudies(language)
                    },
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = stringResource(R.string.select_language_instruction))
        }

        NavigationView(
            gotoPrevious = gotoPrevious,
            gotoNext = gotoNext,
            modifier = Modifier.fillMaxWidth().constrainAs(navigation) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
            },
            nextEnabled = {true},
            nextIcon = { Icons.Default.Check},
            nextLabel = stringResource(R.string.participate)
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewLangQuestionView() {
    ESMiraSurface {
        LangQuestionView(
            Study.newInstance("", "", """{"id":1, "title": "Study1", "lang": "de", "langCodes": ["de", "en"]}"""),
            {}, {}, {}
        )
    }
}