package com.xioneko.android.nekoanime.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.xioneko.android.nekoanime.R

object NekoAnimeFontFamilies {
    val posterFontFamily = FontFamily(Font(R.font.poster_bold, FontWeight.Bold))
    val cuteFontFamily = FontFamily(Font(R.font.cute_regular, FontWeight.Normal))
    val heiFontFamily = FontFamily(
        Font(R.font.hei_bold, FontWeight.Bold),
        Font(R.font.hei_regular, FontWeight.Normal)
    )
}