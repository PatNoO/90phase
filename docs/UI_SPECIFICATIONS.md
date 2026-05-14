# UI Specifications

> Detailed screen-by-screen specifications for the Sleep Cycle Optimizer user interface.
> Visual direction: **premium & polished** — rich dark surfaces, softened cyan accents, nebula-inspired backgrounds, glass-morphism cards, and fluid micro-interactions.

---

## Table of Contents
1. [Design System](#design-system)
2. [Screen Specifications](#screen-specifications)
3. [User Flows](#user-flows)
4. [Component Library](#component-library)
5. [Notification Designs](#notification-designs)
6. [Animation & Motion](#animation--motion)

---

## Design System

### Color Palette: Night Sky

**Base Colors:**
```kotlin
object SleepColors {
    // Backgrounds
    val DeepSpace    = Color(0xFF080E1A)   // True AMOLED black-blue — screen base
    val NavyBlue     = Color(0xFF0B1120)   // Default background
    val MidnightBlue = Color(0xFF131D2E)   // Card surface (slightly raised)
    val GlassSurface = Color(0xFF1A2640)   // Glass card overlay base

    // Accent — DIMMED from original #00D9FF, less electric/neon
    val CyanGlow     = Color(0xFF00B8D9)   // Primary accent — CTAs, active states
    val CyanSoft     = Color(0xFF0096B3)   // Secondary accent — icons, borders

    // Nebula palette — inspired by the night sky reference image
    val NebulaViolet = Color(0xFF7C3AED)   // Bright violet — onboarding glow centers
    val NebulaPurple = Color(0xFF4C1D95)   // Deep purple — onboarding gradient mid
    val NebulaDeep   = Color(0xFF1E1040)   // Near-black purple — onboarding bg base

    val IndigoGlow   = Color(0xFF6366F1)   // Discovery Phase accent

    // Text
    val White        = Color(0xFFFFFFFF)   // Primary text
    val Silver       = Color(0xFFB0BEC5)   // Secondary text
    val SlateBlue    = Color(0xFF4A5568)   // Tertiary / placeholder text

    // Semantic
    val OptimalGreen = Color(0xFF10B981)   // "Optimal" bedtime label
    val GoodAmber    = Color(0xFFF59E0B)   // "Good" bedtime label
    val MinimalSlate = Color(0xFF64748B)   // "Minimal" bedtime label
    val PassedGray   = Color(0xFF374151)   // Passed / unavailable state
    val ErrorRed     = Color(0xFFEF4444)   // Errors / warnings
}
```

> **Why CyanGlow was dimmed:** `#00D9FF` was too electric/neon on a dark background. `#00B8D9` reads as premium rather than garish while still being clearly cyan.

**Gradients:**
```kotlin
// Main app screens — dark navy with very subtle purple nebula tint
val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        SleepColors.DeepSpace,
        SleepColors.NavyBlue,
        Color(0xFF0F1A30)   // Hint of blue-purple at bottom
    )
)

// Onboarding screens — rich nebula atmosphere (deep navy → purple/violet)
val OnboardingBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        SleepColors.NebulaDeep,    // #1E1040 near-black purple at top
        Color(0xFF120B30),         // deep purple-navy mid
        SleepColors.NavyBlue       // familiar navy at bottom
    )
)

// Onboarding nebula color wash (top-right quadrant, like reference image)
val OnboardingNebulaWash = Brush.radialGradient(
    colors = listOf(
        SleepColors.NebulaViolet.copy(alpha = 0.25f),  // #7C3AED
        SleepColors.NebulaPurple.copy(alpha = 0.12f),  // #4C1D95
        Color.Transparent
    ),
    center = Offset(x = 0.75f, y = 0.25f),  // top-right, normalized
    radius = 600f
)

// Active time display — glow bloom behind the time digits
val TimeGlowGradient = Brush.radialGradient(
    colors = listOf(
        SleepColors.CyanGlow.copy(alpha = 0.15f),  // dimmed from 0.18
        Color.Transparent
    ),
    radius = 320f
)

// Optimal card highlight
val OptimalCardGradient = Brush.horizontalGradient(
    colors = listOf(
        SleepColors.OptimalGreen.copy(alpha = 0.12f),
        SleepColors.CyanGlow.copy(alpha = 0.05f)   // dimmed
    )
)

// Glass card surface
val GlassCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x22FFFFFF),  // top edge highlight
        Color(0x08FFFFFF)   // body
    )
)
```

**Elevation & Glass:**
```kotlin
// Glass-morphism modifier — apply to premium cards
fun Modifier.glassCard(cornerRadius: Dp = 16.dp): Modifier = this
    .background(
        brush = GlassCardGradient,
        shape = RoundedCornerShape(cornerRadius)
    )
    .border(
        width = 0.5.dp,
        brush = Brush.verticalGradient(
            listOf(
                Color(0x44FFFFFF),  // top border catch-light
                Color(0x11FFFFFF)   // bottom edge fade
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
```

**Star Sparkles:**

Small scattered white dots rendered on the background layer of main app screens (Calculator, History, Settings). Not on onboarding — onboarding uses the nebula wash instead.

```kotlin
// StarField — drawn on Canvas behind all content
// ~40 stars per screen, random positions seeded by screen name for consistency
// Three size tiers: small (1.5dp), medium (2.5dp), large (3.5dp)
// Alpha range: 0.15 – 0.55 (very subtle, not distracting)
// Occasional soft glow halo: blur radius 4dp, alpha 0.08
@Composable
fun StarFieldBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val stars = remember { generateStars(count = 40, seed = 90) }
        stars.forEach { star ->
            drawCircle(
                color = Color.White.copy(alpha = star.alpha),
                radius = star.radius.toPx(),
                center = Offset(size.width * star.x, size.height * star.y)
            )
        }
    }
}

data class Star(val x: Float, val y: Float, val radius: Dp, val alpha: Float)

fun generateStars(count: Int, seed: Int): List<Star> {
    val rng = Random(seed)
    return List(count) {
        Star(
            x = rng.nextFloat(),
            y = rng.nextFloat(),
            radius = listOf(1.5.dp, 2.5.dp, 3.5.dp).random(rng),
            alpha = rng.nextFloat() * 0.4f + 0.15f
        )
    }
}
```

---

### Typography

**Font Family:** Urbanist — clean, modern, excellent for large numerals

```kotlin
object SleepTypography {
    // Time display — hero element on Calculator screen
    val DisplayLarge = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.Light,       // Light weight reads as premium at large size
        fontSize = 88.sp,
        letterSpacing = (-2).sp
    )

    // Section time (bedtime results, history entries)
    val DisplayMedium = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        letterSpacing = (-0.5).sp
    )

    val HeadlineLarge = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        letterSpacing = 0.sp
    )

    val HeadlineMedium = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        letterSpacing = 0.sp
    )

    val BodyLarge = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )

    val BodyMedium = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    // Labels, tags, quality badges
    val LabelMedium = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.sp
    )
}
```

---

### Spacing System

```kotlin
object Spacing {
    val XXS    = 4.dp
    val XS     = 8.dp
    val Small  = 12.dp
    val Medium = 16.dp
    val Large  = 24.dp
    val XL     = 32.dp
    val XXL    = 48.dp
    val XXXL   = 64.dp
}
```

### Shape System

```kotlin
object SleepShapes {
    val XSmall     = RoundedCornerShape(6.dp)
    val Small      = RoundedCornerShape(8.dp)
    val Medium     = RoundedCornerShape(12.dp)
    val Large      = RoundedCornerShape(16.dp)
    val ExtraLarge = RoundedCornerShape(24.dp)
    val Pill       = RoundedCornerShape(50)
    val Circle     = CircleShape
}
```

---

## Screen Specifications

### 1. Calculator Screen (Home)

The primary screen — open at any time, used most at 18:00 after the daily check-in notification.

**Layout:**
```
┌──────────────────────────────────────────────────┐
│                                                  │
│  Sleep Cycle Optimizer          [⚙ Settings]     │  ← TopAppBar, minimal
│                                                  │
├──────────────────────────────────────────────────┤
│                                                  │
│           WAKE-UP TIME SECTION                   │
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║                                       ║      │
│   ║   [radial glow behind digits]         ║      │
│   ║                                       ║      │
│   ║              07:00                    ║      │  ← 88sp Urbanist Light, CyanGlow
│   ║                                       ║      │
│   ║   Vakna-tid  ·  Tryck för att ändra  ║      │  ← LabelMedium, Silver
│   ║                                       ║      │
│   ╚═══════════════════════════════════════╝      │  ← Glass card, CyanGlow border
│                                                  │
│   [○ Alarm aktiv]     [○ Daglig påminnelse]      │  ← Toggle row below card
│                                                  │
├──────────────────────────────────────────────────┤
│                                                  │
│   REKOMMENDERADE SÄNGGÅNGSTIDER                  │  ← Section label, LabelMedium, ALLCAPS
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║  23:15           6 cykler             ║      │
│   ║  ─────────────────────────────────── ║      │
│   ║  ★ OPTIMAL       7h 30min sömn       ║      │  ← Green badge, optimal card gradient
│   ╚═══════════════════════════════════════╝      │
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║  00:45           5 cykler             ║      │
│   ║  ─────────────────────────────────── ║      │
│   ║  ◆ BRA           6h 00min sömn       ║      │  ← Amber badge
│   ╚═══════════════════════════════════════╝      │
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║  02:15           4 cykler             ║      │
│   ║  ─────────────────────────────────── ║      │
│   ║  · MINIMUM       4h 30min sömn       ║      │  ← Slate badge, muted
│   ╚═══════════════════════════════════════╝      │
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║  21:45  ─────────────────────────── ║      │  ← Passed time: strikethrough,
│   ║  Passerad tid                         ║      │     dimmed card
│   ╚═══════════════════════════════════════╝      │
│                                                  │
└──────────────────────────────────────────────────┘
```

#### Component: WakeTimeCard
```kotlin
@Composable
fun WakeTimeCard(
    time: LocalTime,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium)
    ) {
        // Glow bloom — only visible when active
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(240.dp)
                    .background(TimeGlowGradient, CircleShape)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerRadius = 24.dp)
                .border(
                    width = if (isActive) 1.dp else 0.5.dp,
                    color = if (isActive) SleepColors.CyanGlow.copy(alpha = 0.6f)
                            else SleepColors.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable(onClick = onClick)
                .padding(vertical = Spacing.XXL, horizontal = Spacing.Large),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = SleepTypography.DisplayLarge,
                    color = if (isActive) SleepColors.CyanGlow else SleepColors.White
                )
                Spacer(modifier = Modifier.height(Spacing.XS))
                Text(
                    text = "VAKNA-TID  ·  TRYCK FÖR ATT ÄNDRA",
                    style = SleepTypography.LabelMedium,
                    color = SleepColors.Silver.copy(alpha = 0.5f)
                )
            }
        }
    }
}
```

#### Component: BedtimeResultCard
```kotlin
enum class BedtimeQuality { OPTIMAL, GOOD, MINIMAL, PASSED }

@Composable
fun BedtimeResultCard(
    time: LocalTime,
    cycleCount: Int,
    durationLabel: String,     // e.g. "7h 30min sömn"
    quality: BedtimeQuality,
    onClick: () -> Unit
) {
    val isPassed = quality == BedtimeQuality.PASSED

    val cardBackground = when (quality) {
        BedtimeQuality.OPTIMAL -> OptimalCardGradient
        else -> Brush.horizontalGradient(listOf(SleepColors.MidnightBlue, SleepColors.MidnightBlue))
    }

    val (badgeColor, badgeLabel) = when (quality) {
        BedtimeQuality.OPTIMAL -> SleepColors.OptimalGreen to "OPTIMAL"
        BedtimeQuality.GOOD    -> SleepColors.GoodAmber   to "BRA"
        BedtimeQuality.MINIMAL -> SleepColors.MinimalSlate to "MINIMUM"
        BedtimeQuality.PASSED  -> SleepColors.PassedGray   to "PASSERAD"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.XXS)
            .glassCard(cornerRadius = 16.dp)
            .background(cardBackground, RoundedCornerShape(16.dp))
            .clickable(enabled = !isPassed, onClick = onClick)
            .padding(Spacing.Medium)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = SleepTypography.DisplayMedium,
                    color = if (isPassed) SleepColors.SlateBlue else SleepColors.White,
                    textDecoration = if (isPassed) TextDecoration.LineThrough else null
                )
                Text(
                    text = "$cycleCount cykler",
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.Silver
                )
            }

            Spacer(modifier = Modifier.height(Spacing.XS))
            Divider(color = SleepColors.White.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(Spacing.XS))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quality badge
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), SleepShapes.Pill)
                        .border(0.5.dp, badgeColor.copy(alpha = 0.4f), SleepShapes.Pill)
                        .padding(horizontal = Spacing.Small, vertical = Spacing.XXS)
                ) {
                    Text(
                        text = badgeLabel,
                        style = SleepTypography.LabelMedium,
                        color = badgeColor
                    )
                }

                Text(
                    text = durationLabel,
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.Silver
                )
            }
        }
    }
}
```

---

### 2. History Screen

Shows the user's sleep log with a quality chart and per-day entries.

**Layout:**
```
┌──────────────────────────────────────────────────┐
│                                                  │
│  Sömnhistorik                     [Week ▼]       │  ← TopAppBar + period picker
│                                                  │
├──────────────────────────────────────────────────┤
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║  DENNA VECKA                          ║      │
│   ║                                       ║      │
│   ║       ╭─────────╮                     ║      │
│   ║      ╱     82%   ╲   Snitt: 7h 12m   ║      │  ← Circular quality indicator
│   ║     │   ●●●●◐   │   Bäst: Torsdag   ║      │     CyanGlow stroke
│   ║      ╲          ╱   (4.8 / 5.0)     ║      │
│   ║       ╰─────────╯                     ║      │
│   ║                                       ║      │
│   ╚═══════════════════════════════════════╝      │
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║  SÖMNKVALITET — 7 DAGAR               ║      │
│   ║                                       ║      │
│   ║  5 ★  ·         ●       ●            ║      │
│   ║  4 ★      ●         ●               ║      │
│   ║  3 ★          ●                     ║      │
│   ║  2 ★                               ║      │
│   ║  1 ★                               ║      │
│   ║     Mån Tis Ons Tor Fre Lör Sön    ║      │
│   ║                                       ║      │
│   ╚═══════════════════════════════════════╝      │
│                                                  │
│   DAGLIGA LOGGNINGAR                             │
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║  Torsdag  8 Maj                  ⭐⭐⭐⭐⭐  ║      │
│   ║  Sov 23:15  →  Vaknade 07:25         ║      │
│   ║  6 cykler  ·  8h 10min              ║      │
│   ╚═══════════════════════════════════════╝      │
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║  Onsdag  7 Maj                   ⭐⭐⭐    ║      │
│   ║  Sov 00:45  →  Vaknade 07:15         ║      │
│   ║  4 cykler  ·  6h 30min              ║      │
│   ╚═══════════════════════════════════════╝      │
│                                                  │
└──────────────────────────────────────────────────┘
```

#### Component: SleepLogCard
```kotlin
@Composable
fun SleepLogCard(log: SleepLog) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.XXS)
            .glassCard()
            .padding(Spacing.Medium)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.date.format(DateTimeFormatter.ofPattern("EEEE  d MMM", Locale("sv"))),
                    style = SleepTypography.BodyLarge,
                    color = SleepColors.White
                )
                StarRating(rating = log.qualityRating)
            }
            Spacer(modifier = Modifier.height(Spacing.XS))
            Text(
                text = "Sov ${log.bedtime.format("HH:mm")}  →  Vaknade ${log.wakeTime.format("HH:mm")}",
                style = SleepTypography.BodyMedium,
                color = SleepColors.Silver
            )
            Text(
                text = "${log.cycleCount} cykler  ·  ${log.formattedDuration}",
                style = SleepTypography.BodyMedium,
                color = SleepColors.SlateBlue
            )
        }
    }
}
```

---

### 3. Settings Screen

Full-screen, no bottom nav. Reached from the FAB/gear icon on the Calculator screen.
All toggles are independent — no bundled switches.

**Layout:**
```
┌──────────────────────────────────────────────────┐
│  ← Inställningar                                 │
├──────────────────────────────────────────────────┤
│                                                  │
│   SÖMNPREFERENSER                                │  ← SectionHeader
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║  Cykel-längd                          ║      │
│   ║  ○────────────────────●──── 90 min   ║      │  ← Slider 60–120 min
│   ╠═══════════════════════════════════════╣      │
│   ║  Insomningstid                        ║      │
│   ║  ○────────●──────────────── 15 min   ║      │  ← Slider 5–45 min
│   ╚═══════════════════════════════════════╝      │
│                                                  │
│   NOTIFIKATIONER                                 │  ← Every toggle independent
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║  Daglig check-in (18:00)  [ ON/OFF ] ║      │
│   ║  Ändra tid:  [ 18:00 ▼ ]             ║      │  ← Tappable, opens time picker
│   ╠═══════════════════════════════════════╣      │
│   ║  Sänggångspåminnelse      [ ON/OFF ] ║      │
│   ║  (skickas 15 min innan vald tid)     ║      │  ← Dim helper text
│   ╠═══════════════════════════════════════╣      │
│   ║  Morgonbetyg (1–5 stjärnor) [ON/OFF] ║      │
│   ╠═══════════════════════════════════════╣      │
│   ║  Morgon: läggdagslogg    [ ON/OFF ] ║      │  ← Independent from rating
│   ╚═══════════════════════════════════════╝      │
│                                                  │
│   FUNKTIONER                                     │
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║  Smart Wake Window        [ ON/OFF ] ║      │
│   ║  (accelerometer, telefonen måste     ║      │
│   ║   ligga i sängen)                    ║      │
│   ╠═══════════════════════════════════════╣      │
│   ║  Discovery Phase          [ Aktivera → ] ║  │  ← Available after 7+ days data
│   ║  (tillgänglig efter 7+ dagars data)  ║      │
│   ╠═══════════════════════════════════════╣      │
│   ║  Mönsterinsikter i Historik [ ON/OFF ] ║    │  ← Visible in History tab only
│   ╠═══════════════════════════════════════╣      │
│   ║  Konsistenspoäng i Historik [ ON/OFF ] ║    │  ← Visible in History tab only
│   ╚═══════════════════════════════════════╝      │
│                                                  │
│   DATA & INTEGRITET                              │
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║  Firebase Synk            [ ON/OFF ] ║      │
│   ║  Senast synkad: 2 min sedan           ║      │
│   ╠═══════════════════════════════════════╣      │
│   ║  Exportera data               →       ║      │
│   ╚═══════════════════════════════════════╝      │
│                                                  │
│   OM APPEN                                       │
│                                                  │
│   ╔═══════════════════════════════════════╗      │
│   ║  Version  1.0.0 (MVP)                 ║      │
│   ║  Integritetspolicy            →       ║      │
│   ║  GitHub                       →       ║      │
│   ╚═══════════════════════════════════════╝      │
│                                                  │
└──────────────────────────────────────────────────┘
```

**Rules:**
- Morgonbetyg and Morgon: läggdagslogg are fully independent — neither requires the other
- Discovery Phase row is greyed out with a lock icon until 7+ days of rating data exists
- Mönsterinsikter and Konsistenspoäng only affect the History tab — clarify this in the label

---

### 4. Onboarding Flow (8 screens)

> **Design principles (non-negotiable):**
> - Every feature is a conscious opt-in — nothing is silently enabled
> - Skip is always available and always equal visual prominence to Turn on — no guilt
> - Plain language only — no jargon
> - After the last card the app opens directly to the Home screen — done
> - Every choice can be changed later in Settings

**Flow order:**
```
Welcome → Permissions → Wake Time → Card 1 → Card 2 → Card 3 → Card 4 → Card 5 → Home
```

#### Onboarding Visual Style

All 8 screens share one background template — the **nebula night sky**:
- Background: `OnboardingBackgroundGradient` (deep purple-navy, top → navy bottom)
- Top-right color wash: `OnboardingNebulaWash` (violet/purple radial, ~25% opacity)
- No star sparkles on onboarding — the nebula wash replaces them
- No card chrome — content floats directly on the background

#### Onboarding Screen Template

All feature cards (screens 4–8) use this unified layout — matching the Welcome screen style:

```
┌──────────────────────────────────────────────────┐
│  ← (back arrow, all except screen 1)             │
│                                                  │
│                                                  │
│          [icon glow bloom — icon color]          │
│                   ICON                           │  ← 80dp, centered
│                                                  │
│           Screen Title                           │  ← 28sp SemiBold, White, centered
│                                                  │
│   Floating body text — explains the feature      │  ← 16sp, Silver, centered
│   in plain language. Two to three sentences.     │
│                                                  │
│   Optional secondary note (smaller, muted)       │  ← 14sp, SlateBlue, centered
│                                                  │
│                                                  │
│        ┌──────────────────────────────┐          │
│        │  Feature toggle  [ OFF / ON ]│          │  ← SleepToggle, centered
│        └──────────────────────────────┘          │
│          (or Continue / Got it button)           │
│                                                  │
│              ● ○ ○ ○ ○ ○ ○ ○                    │  ← page dots
└──────────────────────────────────────────────────┘
```

**Icon per screen:**

| # | Screen | Icon (Material Symbols Rounded) | Glow color |
|---|---|---|---|
| 1 | Welcome | `nightlight` | `CyanGlow` |
| 2 | Permissions | `shield` | `CyanGlow` |
| 3 | Wake Time | `alarm` | `CyanGlow` |
| 4 | Daily Check-in | `edit_calendar` | `CyanGlow` |
| 5 | Bedtime Reminder | `bedtime` | `CyanGlow` |
| 6 | Morning Check-in | `wb_sunny` | `GoodAmber` |
| 7 | Smart Wake Window | `vibration` | `CyanGlow` |
| 8 | Discovery Phase | `auto_awesome` | `IndigoGlow` |

**Icon glow bloom** — radial gradient behind each icon, same color as icon at ~20% opacity, radius ~120dp.

Page indicator dots shown on all screens. Back arrow on all except Welcome.

---

**Screen 1 — Welcome**
```
┌──────────────────────────────────────────────────┐
│                                                  │
│          [radial glow bloom, indigo]             │
│                                                  │
│                   ◑                              │  ← Nightlight icon, 72dp, CyanGlow
│                                                  │
│         Sleep Cycle Optimizer                    │  ← 28sp SemiBold, White
│                                                  │
│   Calculate your ideal bedtime based on          │  ← 16sp, Silver
│   natural 90-minute sleep cycles.               │
│                                                  │
│   Simple. Private. No sensors required.          │  ← 14sp, SlateBlue
│                                                  │
│   ████████████████████████████████████████      │  ← CyanGlow fill
│   │           Get started                 │      │
│   ████████████████████████████████████████      │
│                                                  │
│              ● ○ ○ ○ ○ ○ ○ ○                    │  ← 8 dots
└──────────────────────────────────────────────────┘
```

---

**Screen 2 — Permissions**
```
┌──────────────────────────────────────────────────┐
│  ←                     [nebula background]       │
│                                                  │
│          [CyanGlow glow bloom]                   │
│               🛡  shield icon  80dp              │
│                                                  │
│           A few permissions                      │  ← 28sp SemiBold, centered
│                                                  │
│   We need these to send reminders and            │  ← 16sp, Silver, centered
│   detect your alarm. You choose what             │
│   to allow — nothing is forced.                  │
│                                                  │
│   ╔═══════════════════════════════════════╗      │  ← Floating glass card, no bg color
│   ║  Notifications          [ Allow ]     ║      │
│   ║  Exact alarms           [ Allow ]     ║      │
│   ║  Read alarm (optional)  [ Allow / Skip]║     │
│   ╚═══════════════════════════════════════╝      │
│                                                  │
│   ████████████████████████████████████████      │  ← CyanGlow button
│   │              Continue                 │      │
│   ████████████████████████████████████████      │
│                                                  │
│              ○ ● ○ ○ ○ ○ ○ ○                    │
└──────────────────────────────────────────────────┘
```

---

**Screen 3 — Wake Time**
```
┌──────────────────────────────────────────────────┐
│  ←                     [nebula background]       │
│                                                  │
│          [CyanGlow glow bloom]                   │
│               ⏰  alarm icon  80dp               │
│                                                  │
│        When do you usually wake up?              │  ← 28sp SemiBold, centered
│                                                  │
│   We use this to calculate your bedtimes.        │  ← 16sp, Silver, centered
│                                                  │
│              07:00                               │  ← 88sp Light, CyanGlow, tappable
│   Pre-filled from your system alarm              │  ← 12sp, SlateBlue (if detected)
│                                                  │
│   ████████████████████████████████████████      │
│   │              Continue                 │      │
│   ████████████████████████████████████████      │
│                                                  │
│              ○ ○ ● ○ ○ ○ ○ ○                    │
└──────────────────────────────────────────────────┘
```

---

**Screen 4 — Daily Check-in**
```
┌──────────────────────────────────────────────────┐
│  ←                     [nebula background]       │
│                                                  │
│          [CyanGlow glow bloom]                   │
│            📅  edit_calendar icon  80dp          │
│                                                  │
│           Daily Check-in                         │  ← 28sp SemiBold, centered
│                                                  │
│   Every day at 18:00 we ask when you want        │  ← 16sp, Silver, centered
│   to wake up tomorrow. We calculate              │
│   your bedtimes. That's it.                      │
│                                                  │
│   Change the time or turn off in Settings.       │  ← 14sp, SlateBlue, centered
│                                                  │
│        Daily Check-in        [ OFF ]             │  ← SleepToggle, centered
│                                                  │
│              ○ ○ ○ ● ○ ○ ○ ○                    │
└──────────────────────────────────────────────────┘
```
> Toggle ON → schedules daily check-in alarm. Toggle stays OFF → feature never activates, never re-prompted.

---

**Screen 5 — Bedtime Reminder**
```
┌──────────────────────────────────────────────────┐
│  ←                     [nebula background]       │
│                                                  │
│          [CyanGlow glow bloom]                   │
│              🌙  bedtime icon  80dp              │
│                                                  │
│           Bedtime Reminder                       │
│                                                  │
│   15 minutes before your chosen bedtime          │
│   we send a gentle nudge to start                │
│   winding down.                                  │
│                                                  │
│   Only fires on nights you've set a bedtime.     │  ← 14sp, SlateBlue
│                                                  │
│        Bedtime Reminder       [ OFF ]            │  ← SleepToggle, centered
│                                                  │
│              ○ ○ ○ ○ ● ○ ○ ○                    │
└──────────────────────────────────────────────────┘
```

---

**Screen 6 — Morning Check-in**
```
┌──────────────────────────────────────────────────┐
│  ←                     [nebula background]       │
│                                                  │
│          [GoodAmber glow bloom]                  │
│              ☀️  wb_sunny icon  80dp             │  ← Amber tint
│                                                  │
│           Morning Check-in                       │
│                                                  │
│   A quick optional notification each morning.    │
│   Enable what you want — or neither.             │
│                                                  │
│   How did you sleep? (1–5)    [ OFF ]            │  ← SleepToggle
│   What time did you go to bed? [ OFF ]           │  ← SleepToggle, independent
│                                                  │
│   ████████████████████████████████████████      │
│   │              Continue                 │      │  ← Always shown, both can stay OFF
│   ████████████████████████████████████████      │
│                                                  │
│              ○ ○ ○ ○ ○ ● ○ ○                    │
└──────────────────────────────────────────────────┘
```
> Both OFF → no morning notification. Rating ON + clock OFF → star notification only.
> Both ON → star rating first, clock roller bottom sheet appears after tap.

---

**Screen 7 — Feature Card: Smart Wake Window**
```
┌──────────────────────────────────────────────────┐
│  ←                     [nebula background]       │
│                                                  │
│          [CyanGlow glow bloom]                   │
│             📳  vibration icon  80dp             │  ← Material Symbols Rounded
│                                                  │
│           Smart Wake Window                      │  ← 28sp SemiBold, centered
│                                                  │
│   Your phone detects movement to wake you        │  ← 16sp, Silver, centered
│   at a lighter moment within a window            │
│   you set. No microphone. No uploads.            │
│   Completely local.                              │
│                                                  │
│   ⚠ Phone must be on your bed to work.           │  ← 14sp, SlateBlue, centered
│   If you charge it away from bed —               │    not fine print — normal body size
│   skip this, it won't help you.                  │
│                                                  │
│        Smart Wake Window      [ OFF ]            │  ← SleepToggle, centered
│                                                  │
│              ○ ○ ○ ○ ○ ○ ● ○                    │
└──────────────────────────────────────────────────┘
```
> Toggle ON → schedules accelerometer monitoring. Toggle stays OFF → feature never activates, never re-prompted.

---

**Screen 8 — Feature Card: Discovery Phase (info only)**
```
┌──────────────────────────────────────────────────┐
│  ←                     [nebula background]       │
│                                                  │
│          [IndigoGlow glow bloom]                 │
│            ✨  auto_awesome icon  80dp           │  ← Indigo tint, Material Symbols Rounded
│                                                  │
│           Discovery Phase                        │  ← 28sp SemiBold, centered
│                                                  │
│   After a few weeks the app can test small       │  ← 16sp, Silver, centered
│   adjustments to find your personal sleep        │
│   cycle length — 85, 90, or 105 minutes.         │
│                                                  │
│   Completely optional. Activate it yourself      │  ← 14sp, SlateBlue, centered
│   in Settings when you're ready.                 │
│                                                  │
│   ████████████████████████████████████████      │  ← CyanGlow fill button
│   │              Got it                   │      │  ← Single action, no toggle, no skip
│   ████████████████████████████████████████      │
│                                                  │
│              ○ ○ ○ ○ ○ ○ ○ ●                    │
└──────────────────────────────────────────────────┘
```
> No toggle. No opt-in. Awareness only. App opens to Home screen immediately after tapping Got it.

---

## User Flows

### Flow 1: Daily Check-in (18:00)
```
[18:00 Notification]
    "When do you want to wake up tomorrow?"
    [07:00 (From alarm)]  [Custom]
           ↓
    App opens → Calculator screen
    Wake time pre-filled if user tapped alarm suggestion
           ↓
    Bedtime results animate in (staggered, 80ms per card)
           ↓
    User taps a bedtime card (e.g. "23:15 · OPTIMAL")
           ↓
    Bottom sheet: "Set a bedtime reminder?"
    [21:30 — 15 min before]  [Custom time]  [Skip]
           ↓
    Bedtime reminder notification scheduled
    Haptic confirmation + checkmark animation
```

### Flow 2: Morning Rating (if enabled)
```
[Morning notification — fires ~15 min after set wake time]
    ☀️  How did you sleep?
    [ ⭐1 ]  [ ⭐2 ]  [ ⭐3 ]  [ ⭐4 ]  [ ⭐5 ]     [ Skip ]
    Turn off these reminders
           ↓
    User taps a star (e.g. ⭐⭐⭐⭐)
           ↓
    If "bedtime log" switch is ON in Settings:
    → Bottom sheet opens: "What time did you go to bed?"
       [ clock roller ]
       [ Save ]     [ Skip this ]
    If "bedtime log" switch is OFF:
    → Rating saved immediately, done
           ↓
    Rating + optional bedtime saved to Room
    WorkManager queues Firebase sync
    If Discovery Phase active → algorithm reads new data point

    Skip tap → notification dismissed, nothing logged
    "Turn off these reminders" → disables morning rating toggle in Settings
```

### Flow 3: Activate Discovery Phase
```
Settings → Discovery Phase row → [ Activate → ]
(Row is locked/greyed until 7+ days of rating data)
           ↓
    Bottom sheet confirmation:
    "Over 21 days the app tests 3 small variations
     to find your personal sleep cycle length."
    [ Start ]  [ Cancel ]
           ↓
    Discovery Phase active
    Week 1 (days 1–7):   Test longer sleep latency (30 min)
    Week 2 (days 8–14):  Test longer cycles (105 min)
    Week 3 (days 15–21): Test fewer cycles (5 instead of 6)
           ↓
    After 21 days → Results bottom sheet or screen
    "Based on your feedback: 90 min cycles, 20 min latency."
    [ Apply these settings ]  [ Keep current ]
```

---

## Component Library

### Buttons

```kotlin
// Primary — CyanGlow fill, DeepSpace text
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SleepColors.CyanGlow,
            contentColor = SleepColors.DeepSpace
        ),
        shape = SleepShapes.Medium
    ) {
        Text(text = text, style = SleepTypography.LabelMedium)
    }
}

// Secondary — transparent fill, CyanGlow outline
@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        border = BorderStroke(1.dp, SleepColors.CyanGlow.copy(alpha = 0.6f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleepColors.CyanGlow),
        shape = SleepShapes.Medium
    ) {
        Text(text = text, style = SleepTypography.LabelMedium)
    }
}
```

### Toggles

```kotlin
@Composable
fun SleepToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.4f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = SleepTypography.BodyLarge, color = SleepColors.White)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SleepColors.DeepSpace,
                checkedTrackColor = SleepColors.CyanGlow,
                uncheckedThumbColor = SleepColors.Silver,
                uncheckedTrackColor = SleepColors.MidnightBlue
            )
        )
    }
}
```

### Section Header

```kotlin
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        style = SleepTypography.LabelMedium,
        color = SleepColors.SlateBlue,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Small)
    )
}
```

### Quality Badge

```kotlin
@Composable
fun QualityBadge(quality: BedtimeQuality) {
    val (color, label) = when (quality) {
        BedtimeQuality.OPTIMAL -> SleepColors.OptimalGreen to "OPTIMAL"
        BedtimeQuality.GOOD    -> SleepColors.GoodAmber   to "BRA"
        BedtimeQuality.MINIMAL -> SleepColors.MinimalSlate to "MINIMUM"
        BedtimeQuality.PASSED  -> SleepColors.PassedGray   to "PASSERAD"
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), SleepShapes.Pill)
            .border(0.5.dp, color.copy(alpha = 0.4f), SleepShapes.Pill)
            .padding(horizontal = Spacing.Small, vertical = Spacing.XXS)
    ) {
        Text(text = label, style = SleepTypography.LabelMedium, color = color)
    }
}
```

---

## Notification Designs

### 1. Daily Check-in (18:00)
```
┌────────────────────────────────────────────────┐
│  🌙  Sleep Cycle Optimizer                     │
│  När ska du vakna imorgon?                     │
│                                                │
│  [07:00 (Från larm)]    [Anpassa]              │
└────────────────────────────────────────────────┘
Channel: daily_sleep_reminder · Priority: Default
```

### 2. Bedtime Reminder (e.g. 21:30)
```
┌────────────────────────────────────────────────┐
│  🌙  Dags att varva ner                        │
│  Din sänggångstid är om 15 min.                │
│  Lägg undan skärmar och ljusdimma.             │
│                                                │
│  [Jag ligger redan]    [Påminn om 10 min]      │
└────────────────────────────────────────────────┘
Channel: bedtime_reminder · Priority: High
```

### 3. Morning Feedback (07:15)
```
┌────────────────────────────────────────────────┐
│  ☀️  God morgon!                               │
│  Hur kändes sömnen i natt?                     │
│                                                │
│  [⭐]  [⭐⭐]  [⭐⭐⭐]  [⭐⭐⭐⭐]  [⭐⭐⭐⭐⭐]         │
└────────────────────────────────────────────────┘
Channel: morning_feedback · Priority: Default · Sound: None
```

---

## Animation & Motion

### Principles
- **Purposeful** — every animation communicates state, not decoration
- **Fast** — enter: 250–350ms, exit: 150–200ms
- **Eased** — `FastOutSlowIn` for enter, `LinearOutSlowIn` for exit

### Key Animations

**Bedtime cards — staggered entrance:**
```kotlin
// Each card delays by 80ms × index
LaunchedEffect(bedtimes) {
    bedtimes.forEachIndexed { index, _ ->
        delay(80L * index)
        visibleCards.add(index)
    }
}

AnimatedVisibility(
    visible = index in visibleCards,
    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
    exit = fadeOut()
)
```

**Wake time digit transition:**
```kotlin
// Time digits animate out upward when changed
AnimatedContent(
    targetState = selectedTime,
    transitionSpec = {
        slideInVertically { -it } + fadeIn() togetherWith
        slideOutVertically { it } + fadeOut()
    }
)
```

**Glow pulse on active WakeTimeCard:**
```kotlin
val glowAlpha by animateFloat(
    target = if (isActive) 0.18f else 0f,
    animationSpec = infiniteRepeatable(
        animation = tween(2000, easing = FastOutSlowIn),
        repeatMode = RepeatMode.Reverse
    )
)
```

**Confirmation haptic + checkmark:**
- On bedtime selection: `VibrationEffect.EFFECT_CLICK`
- Animated checkmark icon: draw path animation, 300ms, `OvershootInterpolator`

### Loading States
- Shimmer: horizontal gradient sweep, 1200ms loop, `LinearEasing`
- Shimmer color: `SleepColors.White.copy(alpha = 0.06f)` → `0.12f` → `0.06f`

---

## Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Min touch target | 48×48 dp on all interactive elements |
| Text contrast | White on DeepSpace: 18:1 ✅ · CyanGlow on DeepSpace: 9:1 ✅ |
| Content descriptions | All icons have `contentDescription` set |
| Screen reader order | Semantic ordering via `Modifier.semantics` |
| Reduce motion | Check `LocalInspectionMode` + `DisableAnimations` |

---

## Responsive Design

```kotlin
object ScreenSize {
    val Compact  = 0.dp..600.dp     // Portrait phone — default layout
    val Medium   = 600.dp..840.dp   // Landscape phone / small tablet
    val Expanded = 840.dp..Dp.Infinity  // Tablet — two-column
}

@Composable
fun AdaptiveCalculatorScreen() {
    BoxWithConstraints {
        when {
            maxWidth < 600.dp -> SingleColumnCalculator()
            else              -> TwoColumnCalculator()   // time picker left, results right
        }
    }
}
```

---

## Material3 Color Token Mapping

Maps `SleepColors` tokens to the Material3 `ColorScheme` slots used in the Compose theme.

```kotlin
// presentation/theme/Theme.kt
private val NightSkyColorScheme = darkColorScheme(
    // Primary — main actions, active states, CTAs
    primary          = SleepColors.CyanGlow,        // 0xFF00D9FF
    onPrimary        = SleepColors.DeepSpace,        // 0xFF080E1A
    primaryContainer = SleepColors.CyanGlow.copy(alpha = 0.15f),
    onPrimaryContainer = SleepColors.CyanGlow,

    // Secondary — Discovery Phase accent, secondary actions
    secondary          = SleepColors.IndigoGlow,    // 0xFF6366F1
    onSecondary        = SleepColors.White,
    secondaryContainer = SleepColors.IndigoGlow.copy(alpha = 0.15f),
    onSecondaryContainer = SleepColors.IndigoGlow,

    // Tertiary — unused, map to CyanSoft as fallback
    tertiary           = SleepColors.CyanSoft,      // 0xFF00A8CC
    onTertiary         = SleepColors.DeepSpace,

    // Background & Surface
    background         = SleepColors.NavyBlue,      // 0xFF0B1120
    onBackground       = SleepColors.White,
    surface            = SleepColors.MidnightBlue,  // 0xFF131D2E
    onSurface          = SleepColors.White,
    surfaceVariant     = SleepColors.GlassSurface,  // 0xFF1A2640
    onSurfaceVariant   = SleepColors.Silver,        // 0xFFB0BEC5

    // Outlines
    outline            = SleepColors.White.copy(alpha = 0.12f),
    outlineVariant     = SleepColors.White.copy(alpha = 0.06f),

    // Semantic
    error              = SleepColors.ErrorRed,      // 0xFFEF4444
    onError            = SleepColors.White,
    errorContainer     = SleepColors.ErrorRed.copy(alpha = 0.15f),
    onErrorContainer   = SleepColors.ErrorRed,

    // Scrim (dialogs, bottom sheets)
    scrim              = SleepColors.DeepSpace.copy(alpha = 0.8f),

    // Inverse (snackbars)
    inverseSurface     = SleepColors.Silver,
    inverseOnSurface   = SleepColors.DeepSpace,
    inversePrimary     = SleepColors.CyanSoft,
)

@Composable
fun NightSkyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NightSkyColorScheme,
        typography  = NightSkyTypography,   // Urbanist-based, defined in Type.kt
        shapes      = NightSkyShapes,       // defined in Shape.kt
        content     = content
    )
}
```

**Quick reference — token to slot:**

| Usage | SleepColors token | MaterialTheme slot |
|---|---|---|
| Primary buttons, active time | `CyanGlow` | `colorScheme.primary` |
| Button text on CyanGlow | `DeepSpace` | `colorScheme.onPrimary` |
| Glass card tint | `CyanGlow.copy(0.15f)` | `colorScheme.primaryContainer` |
| Discovery Phase accent | `IndigoGlow` | `colorScheme.secondary` |
| Screen background | `NavyBlue` | `colorScheme.background` |
| Card surface | `MidnightBlue` | `colorScheme.surface` |
| Glass card / elevated surface | `GlassSurface` | `colorScheme.surfaceVariant` |
| Primary text | `White` | `colorScheme.onSurface` |
| Secondary text | `Silver` | `colorScheme.onSurfaceVariant` |
| Dividers, card borders | `White.copy(0.06f–0.12f)` | `colorScheme.outlineVariant` |
| Errors | `ErrorRed` | `colorScheme.error` |

> `OptimalGreen`, `GoodAmber`, `MinimalSlate`, `PassedGray` are semantic-only — they do not map to Material3 slots. Use them directly via `SleepColors.*` in component code.

---

## Navigation Graph

```
                    ┌─────────────────┐
                    │   Splash / Init  │
                    └────────┬────────┘
                             │
              ┌──────────────▼──────────────┐
              │  Has completed onboarding?   │
              └──────┬────────────┬─────────┘
                   No │            │ Yes
                      ▼            ▼
           ┌──────────────┐   ┌──────────────────────────┐
           │  Onboarding  │   │   Main (BottomNavHost)   │
           │  (PagerHost) │   └──┬──────────┬────────────┘
           └──────┬───────┘      │          │
                  │              │          │
          ┌───────┴──────┐       │          │
          │  Welcome     │       │          │
          │  Permissions │       │          │
          │  WakeTime    │       │          │
          └──────┬───────┘       │          │
                 │               │          │
                 └───────────────▼          ▼
                          ┌──────────┐  ┌─────────┐
                          │Calculator│  │ History │
                          └────┬─────┘  └────┬────┘
                               │              │
                    ┌──────────▼─┐       ┌────▼────────┐
                    │  Settings  │       │  LogDetail  │
                    │ (fullscreen│       │ (fullscreen │
                    │  from FAB) │       │  sheet)     │
                    └────────────┘       └─────────────┘
```

**Route constants:**
```kotlin
object Routes {
    const val SPLASH      = "splash"
    const val ONBOARDING  = "onboarding"

    // Bottom nav
    const val CALCULATOR  = "calculator"
    const val HISTORY     = "history"

    // Top-level full screen
    const val SETTINGS    = "settings"
    const val LOG_DETAIL  = "log_detail/{logId}"
}
```

**Back stack rules:**
- Onboarding → Calculator: `popUpTo(SPLASH) { inclusive = true }` — no back to onboarding
- Settings: opened from Calculator FAB, back returns to Calculator
- LogDetail: opened from History, back returns to History
- Bottom nav tabs use `saveState = true` / `restoreState = true` to preserve scroll position

---

## Icon Reference

All icons use `androidx.compose.material.icons`. Prefer `Icons.Rounded` for filled UI elements, `Icons.Outlined` for secondary / inactive states.

| Location | Icon | Variant | Color |
|---|---|---|---|
| Calculator — settings FAB | `Icons.Rounded.Settings` | Rounded | `Silver` |
| Calculator — alarm active | `Icons.Rounded.Alarm` | Rounded | `CyanGlow` |
| Calculator — alarm inactive | `Icons.Outlined.AlarmOff` | Outlined | `SlateBlue` |
| Bedtime card — OPTIMAL | `Icons.Rounded.AutoAwesome` | Rounded | `OptimalGreen` |
| Bedtime card — BRA | `Icons.Rounded.Check` | Rounded | `GoodAmber` |
| Bedtime card — MINIMUM | `Icons.Rounded.Remove` | Rounded | `MinimalSlate` |
| Bedtime card — PASSERAD | `Icons.Rounded.Block` | Rounded | `PassedGray` |
| History — star rating (filled) | `Icons.Rounded.Star` | Rounded | `GoodAmber` |
| History — star rating (empty) | `Icons.Outlined.StarBorder` | Outlined | `SlateBlue` |
| History — period picker | `Icons.Rounded.KeyboardArrowDown` | Rounded | `Silver` |
| Settings — row arrow | `Icons.Rounded.ChevronRight` | Rounded | `SlateBlue` |
| Settings — Discovery info | `Icons.Rounded.Info` | Rounded | `IndigoGlow` |
| Settings — sync status OK | `Icons.Rounded.CloudDone` | Rounded | `OptimalGreen` |
| Settings — sync status pending | `Icons.Rounded.CloudSync` | Rounded | `GoodAmber` |
| Onboarding — welcome | `Icons.Rounded.Nightlight` | Rounded | `CyanGlow` |
| Onboarding — notifications | `Icons.Rounded.Notifications` | Rounded | `CyanGlow` |
| Onboarding — alarm permission | `Icons.Rounded.Alarm` | Rounded | `CyanGlow` |
| Notification small icon | `R.drawable.ic_moon` | Custom vector | system tray white |

> `ic_moon` is a custom vector asset — a simple crescent silhouette. Add to `res/drawable/ic_moon.xml` as a 24×24dp monochrome path.

---

*Premium dark-first design. All implementations follow this spec. Hardcoded colors, spacing, or typography values in Compose are not permitted — always use `SleepColors`, `SleepTypography`, and `Spacing` tokens.*
