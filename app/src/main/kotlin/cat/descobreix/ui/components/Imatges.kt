package cat.descobreix.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import cat.descobreix.ui.theme.Colors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

/** Mostra una imatge de l'emmagatzematge intern, descodificada fora del fil principal. */
@Composable
fun ImatgeLocal(
    cami: String,
    descripcio: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    midaMaxima: Int = 1024,
) {
    val imatge by produceState<ImageBitmap?>(null, cami, midaMaxima) {
        value = withContext(Dispatchers.IO) { descodifica(cami, midaMaxima) }
    }
    val i = imatge
    if (i != null) {
        Image(i, contentDescription = descripcio, modifier = modifier, contentScale = contentScale)
    } else {
        Box(modifier.background(Colors.Superficie))
    }
}

private fun descodifica(cami: String, midaMaxima: Int): ImageBitmap? {
    val mides = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(cami, mides)
    if (mides.outWidth <= 0) return null
    var mostra = 1
    while (max(mides.outWidth, mides.outHeight) / (mostra * 2) >= midaMaxima) mostra *= 2
    return BitmapFactory.decodeFile(cami, BitmapFactory.Options().apply { inSampleSize = mostra })?.asImageBitmap()
}
