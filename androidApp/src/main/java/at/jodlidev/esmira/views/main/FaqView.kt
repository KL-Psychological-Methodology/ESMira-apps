package at.jodlidev.esmira.views.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.HtmlHandler
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.inputViews.TextElView

@Composable
fun FaqView(
    getStudy: () -> Study,
    goBack: () -> Unit
) {
    val study = getStudy()

    DefaultScaffoldView(
        title = stringResource(R.string.frequently_asked_questions),
        goBack = goBack
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp).verticalScroll(rememberScrollState())
        ) {
            HtmlHandler.HtmlText(
                html = study.faq
            )
        }
    }
}