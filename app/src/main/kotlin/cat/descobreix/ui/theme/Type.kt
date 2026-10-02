package cat.descobreix.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import cat.descobreix.R

/** Chakra Petch per als títols i els números. */
val ChakraPetch = FontFamily(
    Font(R.font.chakra_petch_medium, FontWeight.Medium),
    Font(R.font.chakra_petch_semibold, FontWeight.SemiBold),
    Font(R.font.chakra_petch_bold, FontWeight.Bold),
)

/** Atkinson Hyperlegible per al text. */
val Atkinson = FontFamily(
    Font(R.font.atkinson_hyperlegible_regular, FontWeight.Normal),
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.Bold),
)

private val titol = TextStyle(fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, color = Colors.Text)
private val text = TextStyle(fontFamily = Atkinson, fontWeight = FontWeight.Normal, color = Colors.Text)

val Tipografia = Typography(
    displayLarge = titol.copy(fontSize = 40.sp, lineHeight = 44.sp),
    displayMedium = titol.copy(fontSize = 36.sp, lineHeight = 40.sp),
    displaySmall = titol.copy(fontSize = 28.sp, lineHeight = 32.sp),
    headlineLarge = titol.copy(fontSize = 34.sp, lineHeight = 38.sp),
    headlineMedium = titol.copy(fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = titol.copy(fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = titol.copy(fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = titol.copy(fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = titol.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = text.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = text.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = text.copy(fontSize = 13.sp, lineHeight = 18.sp, color = Colors.TextSecundari),
    labelLarge = text.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),
    labelMedium = text.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold),
    labelSmall = titol.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em),
)
