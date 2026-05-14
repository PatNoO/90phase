# TODO — Pending Manual Steps

## Urbanist Font (PH-14)

The Night Sky design system uses **Urbanist** as its font family. Currently `SleepTypography` falls back to
`FontFamily.Default` until the font files are added.

### How to add Urbanist

1. Download the Urbanist font from [Google Fonts](https://fonts.google.com/specimen/Urbanist)
2. Place the following TTF files in `presentation/src/main/res/font/`:
   - `urbanist_light.ttf` (weight 300)
   - `urbanist_regular.ttf` (weight 400)
   - `urbanist_medium.ttf` (weight 500)
   - `urbanist_semibold.ttf` (weight 600)
3. In `presentation/src/main/kotlin/com/example/a90phase/presentation/theme/Type.kt`,
   replace the placeholder with:
   ```kotlin
   import androidx.compose.ui.text.font.Font
   import com.example.a90phase.presentation.R

   private val Urbanist = FontFamily(
       Font(R.font.urbanist_light, FontWeight.Light),
       Font(R.font.urbanist_regular, FontWeight.Normal),
       Font(R.font.urbanist_medium, FontWeight.Medium),
       Font(R.font.urbanist_semibold, FontWeight.SemiBold),
   )
   ```
4. Remove the `// TODO` comment block above the `private val Urbanist` line.
