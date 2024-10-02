package tech.turso.libsql.examples.todo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0XFF4FF8D2),

    primaryContainer = Color(0XFF4FF8D2),
    onPrimaryContainer = Color(0xFF00483b),

    secondaryContainer = Color(0xFFCC454F),
    onSecondaryContainer = Color(0xFFFFFFFF),

    surfaceVariant = Color(0xFF15262B),
)

@Composable
fun TodoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}