# UI Specifications

> Detailed screen-by-screen specifications for the Sleep Cycle Optimizer user interface

---

## Table of Contents
1. [Design System](#design-system)
2. [Screen Specifications](#screen-specifications)
3. [User Flows](#user-flows)
4. [Component Library](#component-library)
5. [Notification Designs](#notification-designs)

---

## Design System

### Color Palette: Deep Blue

**Primary Colors:**
```kotlin
object SleepColors {
    val NavyBlue = Color(0xFF0B1120)        // Main background
    val CyanBlue = Color(0xFF00D9FF)        // Accent color for CTAs, active states
    val MidnightBlue = Color(0xFF1A2332)    // Card backgrounds
    val SteelBlue = Color(0xFF4A5568)       // Secondary text
    val White = Color(0xFFFFFFFF)           // Primary text
    val Gray300 = Color(0xFFD1D5DB)         // Disabled states
    val ErrorRed = Color(0xFFEF4444)        // Warnings
    val SuccessGreen = Color(0xFF10B981)    // Confirmations
}
```

**Gradients:**
```kotlin
val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0B1120),
        Color(0xFF1E3A5F)
    )
)
```

### Typography

**Font Family:** Urbanist (modern readability)

```kotlin
object SleepTypography {
    val DisplayLarge = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.Bold,
        fontSize = 96.sp,        // Time display (07:00)
        letterSpacing = (-1.5).sp
    )
    
    val HeadlineLarge = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        letterSpacing = 0.sp
    )
    
    val BodyLarge = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    
    val LabelMedium = TextStyle(
        fontFamily = Urbanist,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    )
}
```

### Spacing System

```kotlin
object SleepSpacing {
    val XXS = 4.dp
    val XS = 8.dp
    val Small = 12.dp
    val Medium = 16.dp
    val Large = 24.dp
    val XL = 32.dp
    val XXL = 48.dp
}
```

### Border Radius

```kotlin
object SleepShapes {
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val ExtraLarge = RoundedCornerShape(24.dp)
    val Circle = CircleShape
}
```

---

## Screen Specifications

### 1. Home Screen (Calculator)

**Layout Hierarchy:**
```
┌─────────────────────────────────────────────────────────┐
│  TopAppBar                                              │
│  "Sleep Cycle Optimizer"                               │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌───────────────────────────────────────────────────┐ │
│  │   TIME SPINNER (Tappable)                         │ │
│  │                                                   │ │
│  │              07:00                                │ │
│  │                                                   │ │
│  │   [Switch: Aktiverad] [Switch: Daglig]           │ │
│  └───────────────────────────────────────────────────┘ │
│                                                         │
│  ┌───────────────────────────────────────────────────┐ │
│  │   REKOMMENDERADE SÄNGGÅNGSTIDER                   │ │
│  │   (Visible only after 18:00)                      │ │
│  │                                                   │ │
│  │   LazyColumn (Scrollable)                         │ │
│  │   ─────────────────────────────                   │ │
│  │   ✅ 21:45  (6 cykler) ← Optimal                  │ │
│  │   ⏰ 23:15  (5 cykler)                             │ │
│  │   ⏰ 00:45  (4 cykler) ← Minst                     │ │
│  │   ❌ 22:15  (Passerad tid — grå)                   │ │
│  │                                                   │ │
│  └───────────────────────────────────────────────────┘ │
│                                                         │
│  [Floating Action Button: Inställningar]               │
└─────────────────────────────────────────────────────────┘
```

#### Component Specifications

**1. Time Spinner:**
```kotlin
@Composable
fun TimeSpinner(
    selectedTime: LocalTime,
    isActive: Boolean,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                color = if (isActive) SleepColors.CyanBlue.copy(alpha = 0.1f) 
                        else SleepColors.MidnightBlue,
                shape = SleepShapes.Large
            )
            .clickable { /* Open time picker dialog */ },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            style = SleepTypography.DisplayLarge,
            color = if (isActive) SleepColors.CyanBlue else SleepColors.White
        )
    }
}
```

**2. Bedtime Recommendation Card:**
```kotlin
@Composable
fun BedtimeRecommendationCard(
    bedtime: LocalTime,
    cycleCount: Int,
    isOptimal: Boolean,
    isPassed: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SleepSpacing.XS)
            .clickable(enabled = !isPassed) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPassed -> SleepColors.Gray300.copy(alpha = 0.2f)
                isOptimal -> SleepColors.CyanBlue.copy(alpha = 0.15f)
                else -> SleepColors.MidnightBlue
            }
        ),
        shape = SleepShapes.Medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SleepSpacing.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        isPassed -> Icons.Default.Close
                        isOptimal -> Icons.Default.Check
                        else -> Icons.Default.Alarm
                    },
                    contentDescription = null,
                    tint = when {
                        isPassed -> SleepColors.Gray300
                        isOptimal -> SleepColors.SuccessGreen
                        else -> SleepColors.CyanBlue
                    }
                )
                
                Spacer(modifier = Modifier.width(SleepSpacing.Small))
                
                Text(
                    text = bedtime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = SleepTypography.HeadlineLarge,
                    color = if (isPassed) SleepColors.Gray300 else SleepColors.White,
                    textDecoration = if (isPassed) TextDecoration.LineThrough else null
                )
            }
            
            Text(
                text = "($cycleCount cykler)",
                style = SleepTypography.BodyLarge,
                color = SleepColors.SteelBlue
            )
        }
    }
}
```

**3. Switches:**
```kotlin
@Composable
fun SleepSwitchRow(
    isActive: Boolean,
    onActiveChanged: (Boolean) -> Unit,
    isDaily: Boolean,
    onDailyChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SleepSpacing.Large),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SwitchWithLabel(
            label = "Aktiverad",
            checked = isActive,
            onCheckedChange = onActiveChanged
        )
        
        SwitchWithLabel(
            label = "Daglig",
            checked = isDaily,
            onCheckedChange = onDailyChanged,
            enabled = isActive
        )
    }
}

@Composable
fun SwitchWithLabel(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.alpha(if (enabled) 1f else 0.5f)
    ) {
        Text(
            text = label,
            style = SleepTypography.LabelMedium,
            color = SleepColors.White
        )
        Spacer(modifier = Modifier.width(SleepSpacing.XS))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SleepColors.CyanBlue,
                checkedTrackColor = SleepColors.CyanBlue.copy(alpha = 0.5f)
            )
        )
    }
}
```

---

### 2. History Screen

**Layout:**
```
┌─────────────────────────────────────────────────────────┐
│  TopAppBar: "Sömnhistorik"                              │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌───────────────────────────────────────────────────┐ │
│  │   QUALITY OVERVIEW (This Week)                    │ │
│  │                                                   │ │
│  │   ┌─────────────────────────────────────┐         │ │
│  │   │    80%                              │         │ │
│  │   │   ─────                             │         │ │
│  │   │  │     │  Sleep Quality             │         │ │
│  │   └─────────────────────────────────────┘         │ │
│  │                                                   │ │
│  │   Avg: 7h 12m | Best: Torsdag (4.8⭐)            │ │
│  └───────────────────────────────────────────────────┘ │
│                                                         │
│  ┌───────────────────────────────────────────────────┐ │
│  │   SLEEP QUALITY CHART                             │ │
│  │                                                   │ │
│  │   5 ⭐  ●                       ●                  │ │
│  │   4 ⭐      ●           ●                          │ │
│  │   3 ⭐          ●                                  │ │
│  │   2 ⭐                                             │ │
│  │   1 ⭐                                             │ │
│  │      Mån  Tis  Ons  Tor  Fre  Lör  Sön           │ │
│  └───────────────────────────────────────────────────┘ │
│                                                         │
│  ┌───────────────────────────────────────────────────┐ │
│  │   DAILY LOGS (LazyColumn)                         │ │
│  │                                                   │ │
│  │   ┌─────────────────────────────────┐             │ │
│  │   │ Torsdag 8 Maj                  │             │ │
│  │   │ Sov: 23:15 → Vaknade: 07:25    │             │ │
│  │   │ Kvalitet: ⭐⭐⭐⭐⭐ (6 cykler)     │             │ │
│  │   └─────────────────────────────────┘             │ │
│  │                                                   │ │
│  │   ┌─────────────────────────────────┐             │ │
│  │   │ Onsdag 7 Maj                   │             │ │
│  │   │ Sov: 00:45 → Vaknade: 07:15    │             │ │
│  │   │ Kvalitet: ⭐⭐⭐ (4 cykler)        │             │ │
│  │   └─────────────────────────────────┘             │ │
│  └───────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

#### Component Specifications

**Sleep Quality Card:**
```kotlin
@Composable
fun SleepQualityOverview(
    averageQuality: Float,  // 0.0 - 1.0
    averageDuration: Duration,
    bestDay: String,
    bestDayRating: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SleepSpacing.Medium),
        colors = CardDefaults.cardColors(
            containerColor = SleepColors.MidnightBlue
        )
    ) {
        Column(
            modifier = Modifier.padding(SleepSpacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular progress indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                CircularProgressIndicator(
                    progress = averageQuality,
                    modifier = Modifier.fillMaxSize(),
                    color = SleepColors.CyanBlue,
                    strokeWidth = 8.dp
                )
                Text(
                    text = "${(averageQuality * 100).toInt()}%",
                    style = SleepTypography.HeadlineLarge,
                    color = SleepColors.White
                )
            }
            
            Spacer(modifier = Modifier.height(SleepSpacing.Medium))
            
            Text(
                text = "Sleep Quality",
                style = SleepTypography.BodyLarge,
                color = SleepColors.SteelBlue
            )
            
            Spacer(modifier = Modifier.height(SleepSpacing.Large))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Avg: ${formatDuration(averageDuration)}",
                    style = SleepTypography.BodyLarge,
                    color = SleepColors.White
                )
                Text(
                    text = "Best: $bestDay (${String.format("%.1f", bestDayRating)}⭐)",
                    style = SleepTypography.BodyLarge,
                    color = SleepColors.SuccessGreen
                )
            }
        }
    }
}
```

---

### 3. Settings Screen

**Layout:**
```
┌─────────────────────────────────────────────────────────┐
│  TopAppBar: "Inställningar"                             │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌───────────────────────────────────────────────────┐ │
│  │   SLEEP PREFERENCES                               │ │
│  │                                                   │ │
│  │   Cykel-längd: [90 min] ▼                         │ │
│  │   Insomning:   [15 min] ▼                         │ │
│  │   Påminnelse:  [18:00]  ▼                         │ │
│  └───────────────────────────────────────────────────┘ │
│                                                         │
│  ┌───────────────────────────────────────────────────┐ │
│  │   DISCOVERY PHASE                                 │ │
│  │                                                   │ │
│  │   [Switch: Aktivera Discovery Phase]              │ │
│  │                                                   │ │
│  │   Status: Inaktiv                                 │ │
│  │   Dagar kvar: -                                   │ │
│  │                                                   │ │
│  │   ℹ️ Discovery Phase justerar automatiskt din     │ │
│  │      sömncykel baserat på feedback.              │ │
│  │                                                   │ │
│  │   [Starta Discovery Phase]                        │ │
│  └───────────────────────────────────────────────────┘ │
│                                                         │
│  ┌───────────────────────────────────────────────────┐ │
│  │   SYNC & BACKUP                                   │ │
│  │                                                   │ │
│  │   [Switch: Firebase Synk]                         │ │
│  │   Senast synkad: 2 min sedan                      │ │
│  │                                                   │ │
│  │   [Exportera data]                                │ │
│  └───────────────────────────────────────────────────┘ │
│                                                         │
│  ┌───────────────────────────────────────────────────┐ │
│  │   ABOUT                                           │ │
│  │                                                   │ │
│  │   Version: 1.0.0 (MVP)                            │ │
│  │   [Privacy Policy]                                │ │
│  │   [GitHub Repository]                             │ │
│  └───────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

---

### 4. Onboarding Flow

**Screen 1: Welcome**
```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│                     🌙                                  │
│                                                         │
│          Välkommen till                                 │
│       Sleep Cycle Optimizer                             │
│                                                         │
│    Optimera din sömn baserat på                         │
│       naturliga 90-minuters cykler                      │
│                                                         │
│                                                         │
│              [Kom igång]                                │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Screen 2: Permissions**
```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│              Behörigheter                               │
│                                                         │
│  🔔 Notifikationer                                      │
│     För dagliga påminnelser kl 18:00                    │
│     [Tillåt]                                            │
│                                                         │
│  ⏰ Alarm-läsning                                        │
│     För att föreslå sömntider baserat på dina larm      │
│     [Tillåt] [Hoppa över]                               │
│                                                         │
│                                                         │
│              [Fortsätt]                                 │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Screen 3: Set First Wake Time**
```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│         När vaknar du vanligtvis?                       │
│                                                         │
│              ┌───────────┐                              │
│              │   07:00   │                              │
│              └───────────┘                              │
│                                                         │
│     Vi använder detta för att beräkna                   │
│       optimala sänggångstider åt dig                    │
│                                                         │
│                                                         │
│              [Fortsätt]                                 │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## User Flows

### Flow 1: Daily Check-in (18:00)

```
[18:00 Notification]
    "När ska du vakna imorgon?"
           ↓
    [User taps notification]
           ↓
    ┌──────────────────────────────────┐
    │ App opens with quick time picker │
    │                                  │
    │ Detected system alarm: 07:00     │
    │ [Use this time]  [Set custom]    │
    └──────────────────────────────────┘
           ↓
    [User selects 07:00]
           ↓
    ┌──────────────────────────────────┐
    │ Rekommenderade sänggångstider:   │
    │                                  │
    │ ✅ 21:45 (6 cykler) ← Optimal    │
    │ ⏰ 23:15 (5 cykler)               │
    │ ⏰ 00:45 (4 cykler)               │
    │                                  │
    │ [Sätt påminnelse för 21:45]      │
    └──────────────────────────────────┘
           ↓
    [User taps "Sätt påminnelse"]
           ↓
    [21:30 Notification scheduled]
    "Dags att varva ner"
```

### Flow 2: Morning Feedback (Discovery Phase)

```
[User wakes up at 07:00]
           ↓
[Morning notification @ 07:15]
    "Hur kändes sömnen?"
           ↓
    [User taps notification]
           ↓
    ┌──────────────────────────────────┐
    │ Hur utvilad känner du dig?       │
    │                                  │
    │  ⭐ ⭐ ⭐ ⭐ ⭐                      │
    │                                  │
    │ [Skicka]                         │
    └──────────────────────────────────┘
           ↓
    [Rating saved to database]
           ↓
    ┌──────────────────────────────────┐
    │ Tack! Din feedback hjälper appen │
    │ att optimera dina sömntider.     │
    └──────────────────────────────────┘
           ↓
    [If Discovery Phase active]
           ↓
    [Analyze if adjustment needed]
```

### Flow 3: Activate Discovery Phase

```
[User navigates to Settings]
           ↓
    [Taps "Starta Discovery Phase"]
           ↓
    ┌──────────────────────────────────┐
    │ Starta Discovery Phase?          │
    │                                  │
    │ Under 21 dagar kommer appen att  │
    │ testa olika sömncykler för att   │
    │ hitta din optimala längd.        │
    │                                  │
    │ [Starta]  [Avbryt]               │
    └──────────────────────────────────┘
           ↓
    [Discovery Phase begins]
           ↓
    Week 1: Test 30 min sleep latency
    Week 2: Test 105 min cycles
    Week 3: Test 5 cycles instead of 6
           ↓
    [After 21 days]
           ↓
    ┌──────────────────────────────────┐
    │ Discovery Phase Complete! 🎉     │
    │                                  │
    │ Baserat på din feedback:         │
    │ • Optimal cykel: 90 min          │
    │ • Insomning: 20 min              │
    │ • Antal cykler: 6                │
    │                                  │
    │ [Använd dessa inställningar]     │
    └──────────────────────────────────┘
```

---

## Component Library

### Buttons

**Primary Button:**
```kotlin
@Composable
fun SleepPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SleepColors.CyanBlue,
            contentColor = SleepColors.NavyBlue
        ),
        shape = SleepShapes.Medium
    ) {
        Text(
            text = text,
            style = SleepTypography.LabelMedium
        )
    }
}
```

**Secondary Button:**
```kotlin
@Composable
fun SleepSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        border = BorderStroke(1.dp, SleepColors.CyanBlue),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SleepColors.CyanBlue
        ),
        shape = SleepShapes.Medium
    ) {
        Text(
            text = text,
            style = SleepTypography.LabelMedium
        )
    }
}
```

### Cards

**Info Card:**
```kotlin
@Composable
fun SleepInfoCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SleepColors.MidnightBlue
        ),
        shape = SleepShapes.Large
    ) {
        Row(
            modifier = Modifier.padding(SleepSpacing.Large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SleepColors.CyanBlue,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.width(SleepSpacing.Medium))
            
            Column {
                Text(
                    text = title,
                    style = SleepTypography.HeadlineLarge,
                    color = SleepColors.White
                )
                Spacer(modifier = Modifier.height(SleepSpacing.XXS))
                Text(
                    text = description,
                    style = SleepTypography.BodyLarge,
                    color = SleepColors.SteelBlue
                )
            }
        }
    }
}
```

---

## Notification Designs

### 1. Daily Check-in Notification (18:00)

**Notification Channel:** `daily_sleep_reminder`  
**Priority:** Default  
**Sound:** Soft chime

**Layout:**
```
┌────────────────────────────────────────┐
│ 🌙 Sleep Cycle Optimizer               │
├────────────────────────────────────────┤
│ När ska du vakna imorgon?              │
│                                        │
│ [07:00 (Från larm)]  [Anpassa]         │
└────────────────────────────────────────┘
```

**Code:**
```kotlin
val notification = NotificationCompat.Builder(context, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_moon)
    .setContentTitle("Sleep Cycle Optimizer")
    .setContentText("När ska du vakna imorgon?")
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .addAction(
        R.drawable.ic_alarm, 
        "07:00 (Från larm)",
        useSystemAlarmPendingIntent
    )
    .addAction(
        R.drawable.ic_edit,
        "Anpassa",
        customTimePendingIntent
    )
    .setAutoCancel(true)
    .build()
```

### 2. Bedtime Reminder (21:30)

**Notification Channel:** `bedtime_reminder`  
**Priority:** High (to ensure delivery)  
**Sound:** Gentle bell

**Layout:**
```
┌────────────────────────────────────────┐
│ 🌙 Dags att varva ner                  │
├────────────────────────────────────────┤
│ För optimal sömn, lägg dig nu.         │
│ Lägg undan skärmar 15 min innan.       │
│                                        │
│ [Jag ligger redan]  [Påminn om 10 min] │
└────────────────────────────────────────┘
```

### 3. Morning Feedback (07:15)

**Notification Channel:** `morning_feedback`  
**Priority:** Default  
**Sound:** None (user just woke up)

**Layout:**
```
┌────────────────────────────────────────┐
│ ☀️ God morgon!                          │
├────────────────────────────────────────┤
│ Hur kändes sömnen?                     │
│                                        │
│ [⭐ ⭐ ⭐ ⭐ ⭐]  [Betygsätt senare]      │
└────────────────────────────────────────┘
```

---

## Accessibility Guidelines

### 1. Touch Targets
- Minimum touch target: 48x48 dp
- Spacing between interactive elements: 8dp minimum

### 2. Text Contrast
- All text must have minimum 4.5:1 contrast ratio against background
- White text on Navy Blue: 17.5:1 ✅
- Cyan Blue on Navy Blue: 8.2:1 ✅

### 3. Content Descriptions
```kotlin
Icon(
    imageVector = Icons.Default.Check,
    contentDescription = "Optimal bedtime",
    tint = SleepColors.SuccessGreen
)

Switch(
    checked = isActive,
    onCheckedChange = onActiveChanged,
    // Proper label association via Row with Text
)
```

### 4. Dark Mode
- App uses dark theme by default (better for nighttime use)
- All colors tested for AMOLED battery optimization

---

## Animation Guidelines

### 1. Time Transitions
```kotlin
val timeTransition = updateTransition(
    targetState = selectedTime,
    label = "time"
)

val scale by timeTransition.animateFloat(
    label = "scale",
    transitionSpec = { spring(stiffness = Spring.StiffnessLow) }
) { isSelected ->
    if (isSelected) 1.1f else 1.0f
}
```

### 2. List Animations
```kotlin
LazyColumn {
    items(
        items = bedtimes,
        key = { it.time }
    ) { bedtime ->
        BedtimeCard(
            bedtime = bedtime,
            modifier = Modifier.animateItemPlacement()
        )
    }
}
```

### 3. State Changes
- Loading states: Shimmer effect
- Success: Checkmark animation with bounce
- Error: Shake animation

---

## Responsive Design

### Screen Size Breakpoints
```kotlin
object ScreenSize {
    val Compact = 0.dp..600.dp     // Phones in portrait
    val Medium = 600.dp..840.dp    // Phones in landscape / small tablets
    val Expanded = 840.dp..Inf     // Tablets
}
```

### Adaptive Layout
```kotlin
@Composable
fun AdaptiveHomeScreen() {
    BoxWithConstraints {
        if (maxWidth < 600.dp) {
            CompactLayout()
        } else {
            ExpandedLayout()  // Two-column layout
        }
    }
}
```

---

*This document defines the complete visual language and interaction patterns for Sleep Cycle Optimizer. All implementations must follow these specifications to maintain consistency.*
