package cat.descobreix.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Icones de línia del disseny (24×24, traç de 2). */
object Icones {
    private fun icona(nom: String, vararg camins: String, gruix: Float = 2f): ImageVector {
        val b = ImageVector.Builder(nom, 24.dp, 24.dp, 24f, 24f)
        for (d in camins) {
            b.addPath(
                pathData = addPathNodes(d),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = gruix,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        return b.build()
    }

    private fun cercle(cx: Float, cy: Float, r: Float) = "M${cx - r},${cy} a$r,$r 0 1,0 ${2 * r},0 a$r,$r 0 1,0 ${-2 * r},0"

    val Enrere = icona("enrere", "M15 5l-7 7 7 7")
    val Tancar = icona("tancar", "M6 6l12 12M18 6L6 18")
    val Mapa = icona("mapa", "M3 6l6-3 6 3 6-3v15l-6 3-6-3-6 3z", "M9 3v15M15 6v15")
    val Missions = icona("missions", "M9 6h11M9 12h11M9 18h11", "M3 6l1.5 1.5L7 5M3 12l1.5 1.5L7 11")
    val Perfil = icona("perfil", cercle(12f, 8f, 4f), "M4 21a8 8 0 0 1 16 0")
    val Cadenat = icona("cadenat", "M7 11h10a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-6a2 2 0 0 1 2-2z", "M8 11V8a4 4 0 0 1 8 0v3")
    val Camera = icona("camera", "M4 8h3l2-3h6l2 3h3v11H4z", cercle(12f, 13f, 3.5f))
    val Centrar = icona("centrar", cercle(12f, 12f, 3f), "M12 2v4M12 18v4M2 12h4M18 12h4")
    val Ubicacio = icona("ubicacio", "M12 21s-7-6.2-7-11.5a7 7 0 0 1 14 0C19 14.8 12 21 12 21z", cercle(12f, 9.5f, 2.5f))
    val Cercar = icona("cercar", cercle(11f, 11f, 7f), "M20 20l-4-4")
    val Fet = icona("fet", "M5 12l5 5L20 7", gruix = 3f)
    val Mes = icona("mes", "M12 5v14M5 12h14")
    val Sac = icona("sac", "M8 9h8c3 3 4 7 3 10a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2c-1-3 0-7 3-10z", "M9 4l3 2 3-2-1 5h-4z")
    val Punts = icona("punts", cercle(12f, 12f, 9f), "M12 7v10M9 9.5h4.5a1.75 1.75 0 0 1 0 3.5h-3a1.75 1.75 0 0 0 0 3.5H15")
    val Trofeu = icona("trofeu", "M8 21h8M12 17v4M7 4h10v5a5 5 0 0 1-10 0z", "M17 5h3v2a3 3 0 0 1-3 3M7 5H4v2a3 3 0 0 0 3 3")
    val Llum = icona(
        "llum",
        "M12 3v3M12 18v3M3 12h3M18 12h3M5.6 5.6l2.1 2.1M16.3 16.3l2.1 2.1M5.6 18.4l2.1-2.1M16.3 7.7l2.1-2.1",
        cercle(12f, 12f, 3f),
    )
    val Esborrar = icona("esborrar", "M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3")
    val Info = icona("info", cercle(12f, 12f, 9f), "M12 11v6M12 7.5v.5")
    val Exportar = icona("exportar", "M12 4v11M7 10l5 5 5-5", "M5 20h14")
    val Ull = icona("ull", "M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z", cercle(12f, 12f, 3f))
    val Estrella = icona("estrella", "M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1-4.4-4.3 6.1-.9z")
    val Casa = icona("casa", "M4 21V10l8-6 8 6v11", "M10 21v-6h4v6")
    val Imatge = icona("imatge", "M4 5h16v14H4z", cercle(9f, 10f, 1.5f), "M20 16l-5-5-8 8")
    val Gps = icona("gps", cercle(12f, 12f, 8f), cercle(12f, 12f, 2f), "M12 2v2M12 20v2M2 12h2M20 12h2")
    val Ratlla = icona("ratlla", "M5 12h14")
}
