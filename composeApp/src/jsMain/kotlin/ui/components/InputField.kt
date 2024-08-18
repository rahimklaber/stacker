package ui.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.silk.components.forms.OutlinedInputVariant
import com.varabyte.kobweb.silk.components.forms.TextInput
import com.varabyte.kobweb.silk.style.common.PlaceholderColor
import org.jetbrains.compose.web.dom.Text


@Composable
fun InputField(
    description: String,
    text: String,
    onchange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier) { Text(description) }
        TextInput(
            text,
            onchange,
            modifier = modifier,
            variant = OutlinedInputVariant,
            placeholder = placeholder,
            placeholderColor = PlaceholderColor(Colors.Grey, 0.6)
        )
    }
}