package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val OutfitFontFamily = FontFamily(
  Font(R.font.outfit, FontWeight.Normal),
  Font(R.font.outfit, FontWeight.Medium),
  Font(R.font.outfit, FontWeight.SemiBold),
  Font(R.font.outfit, FontWeight.Bold)
)

val MonoFontFamily = FontFamily(
  Font(R.font.jetbrains_mono, FontWeight.Normal),
  Font(R.font.jetbrains_mono, FontWeight.SemiBold)
)

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 34.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.5).sp
  ),
  displayMedium = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp
  ),
  displaySmall = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 30.sp
  ),
  headlineMedium = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 26.sp
  ),
  headlineSmall = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp
  ),
  titleLarge = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 17.sp,
    lineHeight = 23.sp
  ),
  titleMedium = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 21.sp
  ),
  titleSmall = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 18.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp
  ),
  bodySmall = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp
  ),
  labelLarge = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp
  ),
  labelMedium = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp
  ),
  labelSmall = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 14.sp
  )
)
