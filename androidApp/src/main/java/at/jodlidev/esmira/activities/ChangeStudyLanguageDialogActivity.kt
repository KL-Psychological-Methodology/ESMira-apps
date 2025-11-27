package at.jodlidev.esmira.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import at.jodlidev.esmira.views.ESMiraDialogContent
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.views.elements.LanguageOptionView
import kotlinx.coroutines.selects.select
import java.util.Locale


class ChangeStudyLanguageDialogActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val studyId = intent.extras?.getLong(EXTRA_STUDY_ID) ?: return

        val study = DbLogic.getStudy(studyId) ?: return
        val languages = study.getAvailableLangs().mapNotNull { lang ->
            val locale = Locale.forLanguageTag(lang)
            if(locale.language.isNotEmpty()) locale to lang else null
        }.toMap()
        var lang = study.lang

        setContent {
            ESMiraDialogContent(
                confirmButtonLabel = stringResource(R.string.save),
                onConfirmRequest = {
                    if(study.lang != lang) {
                        study.saveLanguage(lang)
                        Web.updateStudiesAsync(true, arrayOf(study.id))
                    }
                    finish()
                },
                dismissButtonLabel = stringResource(R.string.cancel),
                onDismissRequest = { finish() },
                title = "Choose Language", //TODO
                contentPadding = PaddingValues(),
            ) {
                LanguagesList(lang, languages, {selectedLanguage ->
                    lang = selectedLanguage
                })
            }
        }
    }

    @Composable
    fun LanguagesList(
        studyLang: String,
        languages: Map<Locale, String>,
        onSelected: (String) -> Unit,
    ) {
        val selectedLanguage = remember { mutableStateOf(studyLang) }
        Column(modifier = Modifier.fillMaxWidth()) {
            for((locale, language) in languages) {
                LanguageOptionView(
                    locale,
                    isSelected = {
                        language == selectedLanguage.value
                    },
                    onSelected = {
                        selectedLanguage.value = language
                        onSelected(language) }
                )
            }
        }
    }

    companion object {
        private const val EXTRA_STUDY_ID = "study_id"

        fun start(context: Context, studyId: Long) {
            val intent = Intent(context, ChangeStudyLanguageDialogActivity::class.java)
            if (context !is Activity)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra(EXTRA_STUDY_ID, studyId)
            context.startActivity(intent)
        }
    }
}