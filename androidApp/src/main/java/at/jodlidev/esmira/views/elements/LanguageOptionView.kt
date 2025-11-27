package at.jodlidev.esmira.views.elements

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun LanguageOptionView(locale: Locale, isSelected: () -> Boolean, onSelected: () -> Unit) {
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