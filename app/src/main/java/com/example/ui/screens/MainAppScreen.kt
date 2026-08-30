package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Device
import com.example.data.model.MeterReading
import com.example.ui.viewmodel.ElectricityViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Dashboard("dashboard", "الرئيسية", Icons.Default.Dashboard),
    BillCalculator("calculator", "الفاتورة", Icons.Default.Calculate),
    DeviceManager("devices", "الأجهزة", Icons.Default.Devices),
    TopConsumers("charts", "الأكثر استهلاكاً", Icons.Default.BarChart),
    QuickCalculator("quick_calc", "حاسبة سريعة", Icons.Default.Bolt),
    Settings("settings", "الإعدادات", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: ElectricityViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }
    val settingsState by viewModel.settings.collectAsStateWithLifecycle()

    // Listen for ad triggers
    LaunchedEffect(key1 = true) {
        viewModel.showAdTriggerFlow.collect { message ->
            Toast.makeText(context, "🎬 $message", Toast.LENGTH_LONG).show()
        }
    }

    // Force RTL for Arabic app consistency
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                val isDark = MaterialTheme.colorScheme.background != LightBg
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF2563EB), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚡",
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "حاسب الكهرباء",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = if (isDark) TextPrimaryDark else Color(0xFF1E293B)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.toggleDarkMode(!settingsState.darkMode)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (settingsState.darkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                    RoundedCornerShape(50)
                                )
                        ) {
                            Icon(
                                imageVector = if (settingsState.darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "الوضع المظلم",
                                tint = if (settingsState.darkMode) Color(0xFFFBBF24) else Color(0xFF475569)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                val isDark = MaterialTheme.colorScheme.background != LightBg
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(
                        BorderStroke(
                            1.dp,
                            if (isDark) BorderDark else Color(0xFFF1F5F9)
                        )
                    )
                ) {
                    Screen.values().forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF2563EB),
                                selectedTextColor = Color(0xFF2563EB),
                                unselectedIconColor = if (isDark) TextSecondaryDark.copy(alpha = 0.6f) else Color(0xFF94A3B8),
                                unselectedTextColor = if (isDark) TextSecondaryDark.copy(alpha = 0.6f) else Color(0xFF94A3B8),
                                indicatorColor = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Main content switcher
                Crossfade(
                    targetState = currentScreen,
                    animationSpec = tween(250),
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    when (screen) {
                        Screen.Dashboard -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToCalculator = { currentScreen = Screen.BillCalculator },
                            onNavigateToDevices = { currentScreen = Screen.DeviceManager }
                        )
                        Screen.BillCalculator -> BillCalculatorScreen(viewModel = viewModel)
                        Screen.DeviceManager -> DeviceManagerScreen(viewModel = viewModel)
                        Screen.TopConsumers -> TopConsumersScreen(viewModel = viewModel)
                        Screen.QuickCalculator -> QuickCalculatorScreen(viewModel = viewModel)
                        Screen.Settings -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD SCREEN (الشاشة الرئيسية)
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: ElectricityViewModel,
    onNavigateToCalculator: () -> Unit,
    onNavigateToDevices: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val devicesList by viewModel.devices.collectAsStateWithLifecycle()
    val totalConsumption by viewModel.totalMonthlyConsumption.collectAsStateWithLifecycle()
    val estimatedCost by viewModel.estimatedCost.collectAsStateWithLifecycle()
    val dailyAvg by viewModel.dailyAverage.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Intro
        item {
            Column {
                Text(
                    text = "نظرة عامة على الاستهلاك",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "ملخص استهلاكك الحالي والتكلفة التقديرية.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Stats Grid
        item {
            val isDark = MaterialTheme.colorScheme.background != LightBg
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Monthly Consumption (Blue theme)
                    VibrantStatsCard(
                        title = "الاستهلاك الشهري",
                        value = String.format(Locale.US, "%.1f", totalConsumption),
                        unit = "كيلوواط / ساعة",
                        dotColor = CardBlueDot,
                        backgroundColor = if (isDark) CardBlueBgDark else CardBlueBg,
                        borderColor = if (isDark) CardBlueBorderDark else CardBlueBorder,
                        textColor = if (isDark) TextPrimaryDark else CardBlueText,
                        titleColor = if (isDark) TextSecondaryDark else Color(0xFF1D4ED8), // blue-700
                        modifier = Modifier.weight(1f)
                    )

                    // Card 2: Estimated Cost (Emerald theme)
                    VibrantStatsCard(
                        title = "التكلفة التقديرية",
                        value = String.format(Locale.US, "%.2f", estimatedCost),
                        unit = settings.currencySymbol,
                        dotColor = CardEmeraldDot,
                        backgroundColor = if (isDark) CardEmeraldBgDark else CardEmeraldBg,
                        borderColor = if (isDark) CardEmeraldBorderDark else CardEmeraldBorder,
                        textColor = if (isDark) TextPrimaryDark else CardEmeraldText,
                        titleColor = if (isDark) TextSecondaryDark else Color(0xFF047857), // emerald-700
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 3: Daily Avg (Amber theme)
                    VibrantStatsCard(
                        title = "المتوسط اليومي",
                        value = String.format(Locale.US, "%.2f", dailyAvg),
                        unit = "kWh / يوم",
                        dotColor = CardAmberDot,
                        backgroundColor = if (isDark) CardAmberBgDark else CardAmberBg,
                        borderColor = if (isDark) CardAmberBorderDark else CardAmberBorder,
                        textColor = if (isDark) TextPrimaryDark else CardAmberText,
                        titleColor = if (isDark) TextSecondaryDark else Color(0xFFB45309), // amber-700
                        modifier = Modifier.weight(1f)
                    )

                    // Card 4: Registered Devices (Indigo theme)
                    VibrantStatsCard(
                        title = "الأجهزة المسجلة",
                        value = devicesList.size.toString(),
                        unit = "أجهزة مفعلة",
                        dotColor = CardIndigoDot,
                        backgroundColor = if (isDark) CardIndigoBgDark else CardIndigoBg,
                        borderColor = if (isDark) CardIndigoBorderDark else CardIndigoBorder,
                        textColor = if (isDark) TextPrimaryDark else CardIndigoText,
                        titleColor = if (isDark) TextSecondaryDark else Color(0xFF4338CA), // indigo-700
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Action Buttons
        item {
            val isDark = MaterialTheme.colorScheme.background != LightBg
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "إجراءات سريعة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onNavigateToCalculator,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("nav_calculator_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text("📝", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "احسب استهلاكك (الفاتورة)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = onNavigateToDevices,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = 2.dp,
                                color = if (isDark) BorderDark else Color(0xFFDBEAFE),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("nav_devices_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
                            contentColor = if (isDark) PrimaryBlueDark else Color(0xFF2563EB)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("🔌", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حاسبة الأجهزة الكهربائية",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Ad banner stub
        item {
            AdMobBannerStub(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun VibrantStatsCard(
    title: String,
    value: String,
    unit: String,
    dotColor: Color,
    backgroundColor: Color,
    borderColor: Color,
    textColor: Color,
    titleColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(128.dp)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Indicator Dot & Title Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(dotColor, RoundedCornerShape(50))
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
            }

            // Value & Unit Column
            Column {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
            }
        }
    }
}


// ==========================================
// 2. BILL CALCULATOR SCREEN (حاسبة الفاتورة)
// ==========================================
@Composable
fun BillCalculatorScreen(viewModel: ElectricityViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val readings by viewModel.meterReadings.collectAsStateWithLifecycle()

    var prevInput by remember { mutableStateOf("") }
    var currInput by remember { mutableStateOf("") }
    var daysInput by remember { mutableStateOf("30") }

    var prevError by remember { mutableStateOf<String?>(null) }
    var currError by remember { mutableStateOf<String?>(null) }
    var daysError by remember { mutableStateOf<String?>(null) }

    // Computed calculation results displayed in UI
    var showResults by remember { mutableStateOf(false) }
    var resultTotalCons by remember { mutableStateOf(0.0) }
    var resultDailyAvg by remember { mutableStateOf(0.0) }
    var resultMonthlyExpected by remember { mutableStateOf(0.0) }
    var resultEstimatedCost by remember { mutableStateOf(0.0) }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "حاسبة الفاتورة",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "أدخل قراءات العداد لحساب استهلاكك المتوقع.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Form Inputs Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "قراءات العداد",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Previous Reading Input
                    OutlinedTextField(
                        value = prevInput,
                        onValueChange = {
                            prevInput = it
                            prevError = viewModel.validateInput(it, "reading", 0.01, 9999999.0)
                        },
                        label = { Text("القراءة السابقة (kWh)") },
                        leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                        isError = prevError != null,
                        supportingText = { prevError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prev_reading_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Current Reading Input
                    OutlinedTextField(
                        value = currInput,
                        onValueChange = {
                            currInput = it
                            currError = viewModel.validateInput(it, "reading", 0.01, 9999999.0)
                        },
                        label = { Text("القراءة الحالية (kWh)") },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                        isError = currError != null,
                        supportingText = { currError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("curr_reading_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Days Input
                    OutlinedTextField(
                        value = daysInput,
                        onValueChange = {
                            daysInput = it
                            daysError = viewModel.validateInput(it, "days", 1.0, 365.0)
                        },
                        label = { Text("عدد الأيام") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        isError = daysError != null,
                        supportingText = { daysError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("days_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Calculate Button
                    Button(
                        onClick = {
                            // Validate all fields
                            val err1 = viewModel.validateInput(prevInput, "reading", 0.01, 9999999.0)
                            val err2 = viewModel.validateInput(currInput, "reading", 0.01, 9999999.0)
                            val err3 = viewModel.validateInput(daysInput, "days", 1.0, 365.0)

                            prevError = err1
                            currError = err2
                            daysError = err3

                            if (err1 == null && err2 == null && err3 == null) {
                                val prev = prevInput.toDouble()
                                val curr = currInput.toDouble()
                                val days = daysInput.toInt()

                                if (curr <= prev) {
                                    currError = "القراءة الحالية يجب أن تكون أكبر من السابقة"
                                    showResults = false
                                } else {
                                    // Math
                                    resultTotalCons = curr - prev
                                    resultDailyAvg = resultTotalCons / days
                                    resultMonthlyExpected = resultDailyAvg * 30
                                    resultEstimatedCost = resultMonthlyExpected * settings.pricePerKWh
                                    showResults = true
                                }
                            } else {
                                showResults = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("calculate_bill_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("احسب الاستهلاك", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Calculation Results Card
        if (showResults) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.07f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "النتائج التقديرية",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

                        ResultRow(label = "الاستهلاك الإجمالي", value = "${String.format(Locale.US, "%.1f", resultTotalCons)} kWh")
                        ResultRow(label = "المتوسط اليومي", value = "${String.format(Locale.US, "%.2f", resultDailyAvg)} kWh/يوم")
                        ResultRow(label = "المتوقع الشهري", value = "${String.format(Locale.US, "%.1f", resultMonthlyExpected)} kWh")
                        ResultRow(
                            label = "التكلفة التقديرية",
                            value = "${String.format(Locale.US, "%.2f", resultEstimatedCost)} ${settings.currencySymbol}",
                            isHighlight = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Save Button
                        OutlinedButton(
                            onClick = {
                                val prev = prevInput.toDouble()
                                val curr = currInput.toDouble()
                                val days = daysInput.toInt()
                                viewModel.addMeterReading(prev, curr, days)
                                Toast.makeText(context, "✅ تم حفظ القراءة الأخيرة بنجاح", Toast.LENGTH_SHORT).show()
                                showResults = false
                                prevInput = ""
                                currInput = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_reading_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حفظ آخر القراءات", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // List of last saved readings
        if (readings.isNotEmpty()) {
            item {
                Text(
                    text = "سجل القراءات السابقة",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(readings.take(5)) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "الاستهلاك: ${String.format(Locale.US, "%.1f", item.currentReading - item.previousReading)} kWh",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "من ${String.format(Locale.US, "%.0f", item.previousReading)} إلى ${String.format(Locale.US, "%.0f", item.currentReading)} kWh",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { viewModel.deleteMeterReading(item) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        item {
            AdMobBannerStub(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ResultRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = if (isHighlight) 18.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}


// ==========================================
// 3. DEVICE MANAGER SCREEN (إدارة الأجهزة)
// ==========================================
@Composable
fun DeviceManagerScreen(viewModel: ElectricityViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val devicesList by viewModel.devices.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingDevice by remember { mutableStateOf<Device?>(null) }

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إدارة الأجهزة الكهربائية",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "أضف أجهزتك الكهربائية لمعرفة استهلاك كل منها.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        editingDevice = null
                        showAddDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("add_device_fab"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إضافة جهاز جديد", fontWeight = FontWeight.Bold)
                }
            }

            // Empty state
            if (devicesList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DevicesOther,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد أجهزة مسجلة حالياً",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "أضف جهازك الأول للبدء بالحساب والتحليل.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(devicesList) { device ->
                    DeviceListItem(
                        device = device,
                        pricePerKWh = settings.pricePerKWh,
                        currencySymbol = settings.currencySymbol,
                        onEdit = {
                            editingDevice = device
                            showAddDialog = true
                        },
                        onDuplicate = {
                            viewModel.duplicateDevice(device)
                            Toast.makeText(context, "تم نسخ الجهاز بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            viewModel.deleteDevice(device)
                            Toast.makeText(context, "تم حذف الجهاز", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Summary footer card
                item {
                    val totalMonthlyCons = devicesList.sumOf { (it.powerW / 1000.0) * it.hoursPerDay * it.daysPerMonth * it.quantity }
                    val totalCost = totalMonthlyCons * settings.pricePerKWh

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "الملخص الشهري للأجهزة",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("إجمالي الاستهلاك الشهري", fontSize = 14.sp)
                                Text("${String.format(Locale.US, "%.1f", totalMonthlyCons)} kWh", fontWeight = FontWeight.Bold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("إجمالي التكلفة المتوقعة", fontSize = 14.sp)
                                Text("${String.format(Locale.US, "%.2f", totalCost)} ${settings.currencySymbol}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            item {
                AdMobBannerStub(modifier = Modifier.fillMaxWidth())
            }
        }

        // Add / Edit Dialog
        if (showAddDialog) {
            DeviceFormDialog(
                device = editingDevice,
                onDismiss = { showAddDialog = false },
                onSave = { name, power, hours, days, qty ->
                    if (editingDevice == null) {
                        viewModel.addDevice(name, power, hours, days, qty)
                        Toast.makeText(context, "✅ تم إضافة الجهاز بنجاح", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.updateDevice(
                            editingDevice!!.copy(
                                name = name,
                                powerW = power,
                                hoursPerDay = hours,
                                daysPerMonth = days,
                                quantity = qty,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        Toast.makeText(context, "✅ تم تعديل الجهاز بنجاح", Toast.LENGTH_SHORT).show()
                    }
                    showAddDialog = false
                },
                validate = viewModel::validateInput
            )
        }
    }
}

@Composable
fun DeviceListItem(
    device: Device,
    pricePerKWh: Double,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val dailyCons = (device.powerW / 1000.0) * device.hoursPerDay * device.quantity
    val monthlyCons = dailyCons * device.daysPerMonth
    val monthlyCost = monthlyCons * pricePerKWh

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                device.name.contains("مكيف") || device.name.contains("تكييف") -> Icons.Default.AcUnit
                                device.name.contains("ثلاجة") || device.name.contains("براد") -> Icons.Default.Kitchen
                                device.name.contains("سخان") -> Icons.Default.LocalFireDepartment
                                device.name.contains("تلفزيون") || device.name.contains("شاشة") -> Icons.Default.Tv
                                device.name.contains("غسالة") -> Icons.Default.LocalLaundryService
                                else -> Icons.Default.Power
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column {
                        Text(
                            text = "${device.name} (${device.quantity} وحدات)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.0f", device.powerW)}W | ${String.format(Locale.US, "%.1f", device.hoursPerDay)} ساعة/يوم",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "قائمة")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("تعديل") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("نسخ مكرر") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onDuplicate()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("حذف", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("الاستهلاك الشهري", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format(Locale.US, "%.1f", monthlyCons)} kWh", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Column {
                    Text("الاستهلاك اليومي", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format(Locale.US, "%.1f", dailyCons)} kWh", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("التكلفة المتوقعة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format(Locale.US, "%.2f", monthlyCost)} $currencySymbol", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun DeviceFormDialog(
    device: Device?,
    onDismiss: () -> Unit,
    onSave: (name: String, power: Double, hours: Double, days: Int, qty: Int) -> Unit,
    validate: (String, String, Double, Double) -> String?
) {
    var name by remember { mutableStateOf(device?.name ?: "") }
    var powerInput by remember { mutableStateOf(device?.powerW?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "") }
    var hoursInput by remember { mutableStateOf(device?.hoursPerDay?.toString() ?: "") }
    var daysInput by remember { mutableStateOf(device?.daysPerMonth?.toString() ?: "30") }
    var qtyInput by remember { mutableStateOf(device?.quantity?.toString() ?: "1") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var powerError by remember { mutableStateOf<String?>(null) }
    var hoursError by remember { mutableStateOf<String?>(null) }
    var daysError by remember { mutableStateOf<String?>(null) }
    var qtyError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (device == null) "إضافة جهاز جديد" else "تعديل جهاز") },
        text = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = if (it.trim().length in 2..50) null else "الاسم يجب أن يكون 2-50 حرف"
                        },
                        label = { Text("اسم الجهاز (مثال: مكيف الهواء)") },
                        isError = nameError != null,
                        supportingText = { nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Power Watts
                    OutlinedTextField(
                        value = powerInput,
                        onValueChange = {
                            powerInput = it
                            powerError = validate(it, "power", 10.0, 10000.0)
                        },
                        label = { Text("القدرة الكهربائية (وات W)") },
                        isError = powerError != null,
                        supportingText = { powerError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Hours per day
                    OutlinedTextField(
                        value = hoursInput,
                        onValueChange = {
                            hoursInput = it
                            hoursError = validate(it, "hours", 0.1, 24.0)
                        },
                        label = { Text("ساعات التشغيل يومياً") },
                        isError = hoursError != null,
                        supportingText = { hoursError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Days per month
                        OutlinedTextField(
                            value = daysInput,
                            onValueChange = {
                                daysInput = it
                                daysError = validate(it, "days", 1.0, 30.0)
                            },
                            label = { Text("أيام التشغيل") },
                            isError = daysError != null,
                            supportingText = { daysError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Quantity
                        OutlinedTextField(
                            value = qtyInput,
                            onValueChange = {
                                qtyInput = it
                                qtyError = validate(it, "quantity", 1.0, 100.0)
                            },
                            label = { Text("العدد") },
                            isError = qtyError != null,
                            supportingText = { qtyError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val errName = if (name.trim().length in 2..50) null else "الاسم يجب أن يكون 2-50 حرف"
                    val errPower = validate(powerInput, "power", 10.0, 10000.0)
                    val errHours = validate(hoursInput, "hours", 0.1, 24.0)
                    val errDays = validate(daysInput, "days", 1.0, 30.0)
                    val errQty = validate(qtyInput, "quantity", 1.0, 100.0)

                    nameError = errName
                    powerError = errPower
                    hoursError = errHours
                    daysError = errDays
                    qtyError = errQty

                    if (errName == null && errPower == null && errHours == null && errDays == null && errQty == null) {
                        onSave(
                            name,
                            powerInput.toDouble(),
                            hoursInput.toDouble(),
                            daysInput.toInt(),
                            qtyInput.toInt()
                        )
                    }
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}


// ==========================================
// 4. TOP CONSUMERS SCREEN (الأكثر استهلاكاً)
// ==========================================
@Composable
fun TopConsumersScreen(viewModel: ElectricityViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val devicesList by viewModel.devices.collectAsStateWithLifecycle()

    val totalConsumption by viewModel.totalMonthlyConsumption.collectAsStateWithLifecycle()
    val estimatedCost by viewModel.estimatedCost.collectAsStateWithLifecycle()

    // Sort devices descending by monthly consumption
    val sortedDevices = remember(devicesList) {
        devicesList.sortedByDescending { (it.powerW / 1000.0) * it.hoursPerDay * it.daysPerMonth * it.quantity }
    }

    val totalDevicesCons = sortedDevices.sumOf { (it.powerW / 1000.0) * it.hoursPerDay * it.daysPerMonth * it.quantity }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "الأجهزة الأكثر استهلاكاً",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "تحليل استهلاك الطاقة الشهري للأجهزة.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("إجمالي الاستهلاك الشهري للأجهزة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.1f", totalDevicesCons)} kWh",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("التكلفة التقديرية للأجهزة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.2f", totalDevicesCons * settings.pricePerKWh)} ${settings.currencySymbol}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Top Header Title
        item {
            Text(
                text = "الأجهزة الأعلى استهلاكاً",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Empty State
        if (sortedDevices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد أجهزة مسجلة لحساب نسب الاستهلاك.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(sortedDevices.indices.toList()) { index ->
                val device = sortedDevices[index]
                val deviceCons = (device.powerW / 1000.0) * device.hoursPerDay * device.daysPerMonth * device.quantity
                val percentage = if (totalDevicesCons > 0) (deviceCons / totalDevicesCons) else 0.0

                // Custom rank badge symbols
                val rankText = when (index) {
                    0 -> "🏆 1️⃣"
                    1 -> "🥈 2️⃣"
                    2 -> "🥉 3️⃣"
                    else -> "${index + 1}️⃣"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(rankText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(device.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("${String.format(Locale.US, "%.1f", deviceCons)} kWh", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${String.format(Locale.US, "%.0f", percentage * 100)}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom animated progress bar
                        LinearProgressIndicator(
                            progress = { percentage.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when (index) {
                                0 -> MaterialTheme.colorScheme.primary
                                1 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.tertiary
                            },
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}


// ==========================================
// 5. QUICK CALCULATOR SCREEN (الحاسبة السريعة)
// ==========================================
@Composable
fun QuickCalculatorScreen(viewModel: ElectricityViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var powerInput by remember { mutableStateOf("") }
    var hoursInput by remember { mutableStateOf("") }
    var daysInput by remember { mutableStateOf("30") }

    var powerError by remember { mutableStateOf<String?>(null) }
    var hoursError by remember { mutableStateOf<String?>(null) }
    var daysError by remember { mutableStateOf<String?>(null) }

    var calculatedDailyCons by remember { mutableStateOf(0.0) }
    var calculatedPeriodCons by remember { mutableStateOf(0.0) }
    var calculatedCost by remember { mutableStateOf(0.0) }
    var hasResult by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "الحاسبة السريعة",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "احسب التكلفة والاستهلاك لأي جهاز في ثوانٍ معدودة.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Form Inputs Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Power Watts
                    OutlinedTextField(
                        value = powerInput,
                        onValueChange = {
                            powerInput = it
                            powerError = viewModel.validateInput(it, "power", 1.0, 100000.0)
                        },
                        label = { Text("القدرة الكهربائية (وات W)") },
                        leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null) },
                        isError = powerError != null,
                        supportingText = { powerError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_power_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Hours per day
                    OutlinedTextField(
                        value = hoursInput,
                        onValueChange = {
                            hoursInput = it
                            hoursError = viewModel.validateInput(it, "hours", 0.1, 24.0)
                        },
                        label = { Text("ساعات التشغيل اليومية") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        isError = hoursError != null,
                        supportingText = { hoursError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_hours_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Days
                    OutlinedTextField(
                        value = daysInput,
                        onValueChange = {
                            daysInput = it
                            daysError = viewModel.validateInput(it, "days", 1.0, 365.0)
                        },
                        label = { Text("عدد الأيام") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        isError = daysError != null,
                        supportingText = { daysError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_days_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Calculate button
                    Button(
                        onClick = {
                            val err1 = viewModel.validateInput(powerInput, "power", 1.0, 100000.0)
                            val err2 = viewModel.validateInput(hoursInput, "hours", 0.1, 24.0)
                            val err3 = viewModel.validateInput(daysInput, "days", 1.0, 365.0)

                            powerError = err1
                            hoursError = err2
                            daysError = err3

                            if (err1 == null && err2 == null && err3 == null) {
                                val p = powerInput.toDouble()
                                val h = hoursInput.toDouble()
                                val d = daysInput.toInt()

                                calculatedDailyCons = (p / 1000.0) * h
                                calculatedPeriodCons = calculatedDailyCons * d
                                calculatedCost = calculatedPeriodCons * settings.pricePerKWh
                                hasResult = true
                            } else {
                                hasResult = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("quick_calculate_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("احسب الآن", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Quick calculation results
        if (hasResult) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "النتائج التقديرية",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Copy button
                            IconButton(onClick = {
                                val resultText = "💡 نتيجة حاسب الكهرباء السريع:\n" +
                                        "⚡ الاستهلاك اليومي: ${String.format(Locale.US, "%.2f", calculatedDailyCons)} kWh\n" +
                                        "📅 الاستهلاك للفترة ($daysInput يوم): ${String.format(Locale.US, "%.1f", calculatedPeriodCons)} kWh\n" +
                                        "💰 التكلفة التقديرية: ${String.format(Locale.US, "%.2f", calculatedCost)} ${settings.currencySymbol}\n" +
                                        "سعر الكيلوواط الحالي: ${settings.pricePerKWh} ${settings.currencySymbol}"

                                clipboardManager.setText(AnnotatedString(resultText))
                                Toast.makeText(context, "📋 تم نسخ النتيجة بنجاح", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "نسخ النتائج",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        ResultRow(label = "الاستهلاك اليومي", value = "${String.format(Locale.US, "%.2f", calculatedDailyCons)} kWh")
                        ResultRow(label = "الاستهلاك للفترة ($daysInput يوم)", value = "${String.format(Locale.US, "%.1f", calculatedPeriodCons)} kWh")
                        ResultRow(
                            label = "التكلفة التقديرية",
                            value = "${String.format(Locale.US, "%.2f", calculatedCost)} ${settings.currencySymbol}",
                            isHighlight = true
                        )
                    }
                }
            }
        }
    }
}


// ==========================================
// 6. SETTINGS SCREEN (الإعدادات)
// ==========================================
@Composable
fun SettingsScreen(viewModel: ElectricityViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var showEditPrice by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "الإعدادات والخيارات",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Settings Groups
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // Item 1: Price
                    SettingsItem(
                        title = "سعر الكيلوواط (kWh)",
                        description = "السعر الحالي: ${settings.pricePerKWh} ${settings.currencySymbol}",
                        icon = Icons.Default.Payments,
                        onClick = { showEditPrice = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Item 2: Currency info
                    SettingsItem(
                        title = "اللغة والعملة",
                        description = "العملة: ${settings.currency} (${settings.currencySymbol})",
                        icon = Icons.Default.CurrencyExchange,
                        onClick = {
                            Toast.makeText(context, "العملة الافتراضية حالياً هي الجنيه المصري EGP", Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Item 3: Dark Mode toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Column {
                                Text("الوضع الداكن", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("تفعيل مظهر التطبيق المظلم لراحة العين", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Switch(
                            checked = settings.darkMode,
                            onCheckedChange = { viewModel.toggleDarkMode(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // Legal & Info card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    SettingsItem(
                        title = "عن التطبيق والترخيص",
                        description = "حاسب الكهرباء v1.0.0",
                        icon = Icons.Default.Info,
                        onClick = { showAboutDialog = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    SettingsItem(
                        title = "سياسة الخصوصية والأمان",
                        description = "100% محلي وآمن بالكامل",
                        icon = Icons.Default.Policy,
                        onClick = { showPrivacyDialog = true }
                    )
                }
            }
        }

        // Danger zone Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.25f),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f))
            ) {
                SettingsItem(
                    title = "خطر - حذف جميع البيانات",
                    description = "سيتم مسح الأجهزة، سجل القراءات والاستهلاك بالكامل.",
                    icon = Icons.Default.DeleteForever,
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteConfirm = true }
                )
            }
        }
    }

    // Price Edit dialog
    if (showEditPrice) {
        var priceInput by remember { mutableStateOf(settings.pricePerKWh.toString()) }
        var priceError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showEditPrice = false },
            title = { Text("تعديل سعر kWh") },
            text = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "هذا السعر يُستخدم في حساب الفاتورة والأجهزة الكهربائية.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = {
                                priceInput = it
                                priceError = viewModel.validateInput(it, "price", 0.01, 100.0)
                            },
                            label = { Text("سعر كيلووات ساعة (ج.م)") },
                            isError = priceError != null,
                            supportingText = { priceError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("price_input_field"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val err = viewModel.validateInput(priceInput, "price", 0.01, 100.0)
                        priceError = err
                        if (err == null) {
                            viewModel.updatePrice(priceInput.toDouble())
                            Toast.makeText(context, "تم تحديث سعر الكهرباء", Toast.LENGTH_SHORT).show()
                            showEditPrice = false
                        }
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPrice = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("حول تطبيق حاسب الكهرباء") },
            text = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "تطبيق عربي لحساب استهلاك الكهرباء والتكاليف بسهولة وسرعة وبدون إنترنت.",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "📱 الإصدار: 1.0.0", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "🔒 الخصوصية: 100% محلي بدون خوادم خارجية", fontSize = 13.sp)
                        Text(text = "📜 الترخيص: MIT Open Source License", fontSize = 13.sp)
                        Text(text = "📅 سنة الإنشاء: 2026", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("حسناً")
                }
            }
        )
    }

    // Privacy Policy dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("سياسة الخصوصية والأمان") },
            text = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "🔒 نحن نحترم خصوصيتك بالكامل:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "✓ لا نجمع أي بيانات شخصية على الإطلاق.")
                        Text(text = "✓ لا توجد أي عملية تسجيل دخول أو كلمات مرور.")
                        Text(text = "✓ جميع حساباتك وأجهزتك وبيانات عدادك تُحفظ محلياً وبأمان داخل جهازك فقط.")
                        Text(text = "✓ لا يرسل التطبيق أي بيانات لأي خادم خارجي.")
                        Text(text = "✓ لا نستخدم أي تتبع جغرافي أو ملفات تعريف ارتباط.")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) {
                    Text("متفق")
                }
            }
        )
    }

    // Danger Delete Confirm Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("تأكيد حذف جميع البيانات", color = MaterialTheme.colorScheme.error) },
            text = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "⚠️ تنبيه هام!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text(text = "سيؤدي هذا الإجراء إلى حذف:")
                        Text(text = "• جميع الأجهزة الكهربائية المسجلة.")
                        Text(text = "• سجل قراءات العداد السابقة.")
                        Text(text = "• سجل الاستهلاك والتواريخ.")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "هذه العملية نهائية ولا يمكن التراجع عنها مطلقاً!", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        Toast.makeText(context, "🗑️ تم حذف جميع بياناتك بنجاح", Toast.LENGTH_LONG).show()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف نهائي")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titleColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (titleColor == MaterialTheme.colorScheme.error) titleColor else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = titleColor)
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}


// ==========================================
// ADMOB PLACEHOLDER BANNER (إعلان)
// ==========================================
@Composable
fun AdMobBannerStub(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(vertical = 12.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLabel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "🎬 [ مساحة إعلان Google AdMob - يظهر عند التفعيل ]",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
