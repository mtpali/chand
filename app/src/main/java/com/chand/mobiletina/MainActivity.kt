package com.chand.mobiletina

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chand.mobiletina.data.AppPreferences
import com.chand.mobiletina.data.WidgetThemeMode
import com.chand.mobiletina.date.JalaliDate
import com.chand.mobiletina.promo.PromoSecrets
import com.chand.mobiletina.util.PersianNumbers
import com.chand.mobiletina.work.PriceUpdateScheduler
import com.chand.mobiletina.widget.WidgetRenderer
import com.chand.mobiletina.widget.combined.CombinedWidgetRenderer
import kotlin.math.cos
import kotlin.math.sin

private val ChandFont = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private enum class SocialIconKind { INSTAGRAM, TELEGRAM }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply RTL before Compose draws its first frame. This also prevents the Instagram
        // account panel from briefly appearing LTR and then flipping to RTL.
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        setContent { ChandRoot() }
    }

    override fun onStart() {
        super.onStart()
        // Opening chand is also a manual dollar refresh gesture.
        PriceUpdateScheduler.schedule(this)
        PriceUpdateScheduler.enqueueNow(this)
    }
}

@Composable
private fun ChandRoot() {
    val dark = isSystemInDarkTheme()
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(modifier = Modifier.fillMaxSize()) {
                ChandScreen()
            }
        }
    }
}

@Composable
private fun ChandScreen() {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    var widgetTheme by remember { mutableStateOf(prefs.widgetTheme()) }
    var showInstagramPanel by remember { mutableStateOf(false) }
    val date = remember { JalaliDate.today() }
    val cachedRate = prefs.cachedDollarRate()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "chand",
                    fontFamily = ChandFont,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                WidgetThemePicker(
                    mode = widgetTheme,
                    onSelect = { mode ->
                        prefs.setWidgetTheme(mode)
                        widgetTheme = mode
                        WidgetRenderer.updateDateAll(context)
                        WidgetRenderer.updateDollarAll(context)
                        CombinedWidgetRenderer.updateAll(context)
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            Spacer(Modifier.height(16.dp))

            PreviewCard(
                title = "تاریخ شمسی",
                body = "${date.dayOfWeek}  •  ${PersianNumbers.digits(date.day)} ${date.monthName} ${PersianNumbers.digits(date.year)}"
            )

            Spacer(Modifier.height(10.dp))

            PreviewCard(
                title = "دلار آمریکا",
                body = cachedRate?.let { "${PersianNumbers.grouped(it.priceToman)} تومان" }
                    ?: "در حال دریافت آخرین قیمت..."
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "درباره من",
                fontFamily = ChandFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PromoButton(
                    title = PromoSecrets.instagramTitle,
                    icon = SocialIconKind.INSTAGRAM,
                    colors = listOf(
                        Color(0xFF6D28D9),
                        Color(0xFFD946EF),
                        Color(0xFFF97316)
                    ),
                    onClick = { showInstagramPanel = true }
                )

                PromoButton(
                    title = PromoSecrets.developerTitle,
                    icon = SocialIconKind.TELEGRAM,
                    colors = listOf(
                        Color(0xFF0284C7),
                        Color(0xFF2563EB)
                    ),
                    onClick = { openTelegram(context, PromoSecrets.telegramUser) }
                )
            }

            Spacer(Modifier.height(18.dp))
        }

        if (showInstagramPanel) {
            InstagramAccountsOverlay(
                onDismiss = { showInstagramPanel = false },
                onOpen = { username ->
                    showInstagramPanel = false
                    openInstagram(context, username)
                }
            )
        }
    }
}

@Composable
private fun WidgetThemePicker(
    mode: WidgetThemeMode,
    onSelect: (WidgetThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val name = when (mode) {
        WidgetThemeMode.LIGHT -> "روشن"
        WidgetThemeMode.DARK -> "تاریک"
        WidgetThemeMode.AUTO -> "خودکار"
    }

    Box(modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .semantics { contentDescription = "تم ویجت: $name؛ برای تغییر لمس کنید" }
        ) {
            ThemeIcon(mode)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(
                WidgetThemeMode.LIGHT to "روشن",
                WidgetThemeMode.DARK to "تاریک",
                WidgetThemeMode.AUTO to "خودکار (طبق گوشی)"
            ).forEach { (option, title) ->
                DropdownMenuItem(
                    text = { Text(title, fontFamily = ChandFont) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                    trailingIcon = {
                        if (mode == option) Text("✓", color = MaterialTheme.colorScheme.primary)
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeIcon(mode: WidgetThemeMode) {
    val color = MaterialTheme.colorScheme.onSurface
    val background = MaterialTheme.colorScheme.surfaceVariant
    Canvas(Modifier.size(21.dp)) {
        val u = size.minDimension / 24f
        val center = Offset(12f * u, 12f * u)
        when (mode) {
            WidgetThemeMode.LIGHT -> {
                drawCircle(color, radius = 4f * u, center = center)
                repeat(8) { index ->
                    val angle = index * Math.PI / 4.0
                    val x = cos(angle).toFloat()
                    val y = sin(angle).toFloat()
                    drawLine(
                        color,
                        Offset((12f + 7f * x) * u, (12f + 7f * y) * u),
                        Offset((12f + 9.5f * x) * u, (12f + 9.5f * y) * u),
                        strokeWidth = 1.8f * u,
                        cap = StrokeCap.Round
                    )
                }
            }
            WidgetThemeMode.DARK -> {
                drawCircle(color, radius = 9f * u, center = center)
                drawCircle(background, radius = 7.5f * u, center = Offset(16f * u, 8f * u))
            }
            WidgetThemeMode.AUTO -> {
                val bounds = Offset(3f * u, 3f * u)
                val diameter = Size(18f * u, 18f * u)
                drawArc(color, 90f, 180f, true, bounds, diameter)
                drawCircle(color, radius = 9f * u, center = center, style = Stroke(1.8f * u))
            }
        }
    }
}

@Composable
private fun SocialIcon(kind: SocialIconKind) {
    Canvas(Modifier.size(26.dp)) {
        val u = size.minDimension / 24f
        when (kind) {
            SocialIconKind.INSTAGRAM -> {
                drawRoundRect(
                    Color.White,
                    topLeft = Offset(3f * u, 3f * u),
                    size = Size(18f * u, 18f * u),
                    cornerRadius = CornerRadius(5f * u),
                    style = Stroke(width = 2.2f * u)
                )
                drawCircle(
                    Color.White,
                    radius = 4f * u,
                    center = Offset(12f * u, 12f * u),
                    style = Stroke(width = 2.2f * u)
                )
                drawCircle(Color.White, radius = 1.3f * u, center = Offset(17.4f * u, 6.8f * u))
            }
            SocialIconKind.TELEGRAM -> {
                val plane = Path().apply {
                    moveTo(2f * u, 10.8f * u)
                    lineTo(21f * u, 3.2f * u)
                    quadraticBezierTo(22.4f * u, 2.7f * u, 22f * u, 4.2f * u)
                    lineTo(18.5f * u, 20.5f * u)
                    quadraticBezierTo(18.3f * u, 21.2f * u, 17.6f * u, 20.8f * u)
                    lineTo(12.6f * u, 17.1f * u)
                    lineTo(10.1f * u, 19f * u)
                    quadraticBezierTo(9.5f * u, 19.5f * u, 9.4f * u, 18.5f * u)
                    lineTo(8.5f * u, 14.7f * u)
                    lineTo(2.4f * u, 12.5f * u)
                    quadraticBezierTo(1.2f * u, 12.1f * u, 2f * u, 10.8f * u)
                    close()
                }
                drawPath(plane, Color.White)
                drawLine(
                    Color(0xFF1D4ED8),
                    Offset(8.5f * u, 14.7f * u),
                    Offset(19.7f * u, 5.2f * u),
                    strokeWidth = 1.2f * u
                )
            }
        }
    }
}

@Composable
private fun PreviewCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                title,
                fontFamily = ChandFont,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Text(
                body,
                fontFamily = ChandFont,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PromoButton(
    title: String,
    icon: SocialIconKind,
    colors: List<Color>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(colors))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                SocialIcon(icon)
            }

            Spacer(Modifier.width(14.dp))

            Text(
                title,
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontFamily = ChandFont,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "‹",
                color = Color.White.copy(alpha = 0.92f),
                fontFamily = ChandFont,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * In-content overlay instead of a platform Dialog. Because it is part of the already-RTL
 * Compose hierarchy, there is no first-frame LTR flash on MIUI before the panel settles.
 */
@Composable
private fun InstagramAccountsOverlay(
    onDismiss: () -> Unit,
    onOpen: (String) -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.38f))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 18.dp
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF6D28D9),
                                        Color(0xFFD946EF),
                                        Color(0xFFF97316)
                                    )
                                )
                            )
                            .padding(horizontal = 22.dp, vertical = 21.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                SocialIcon(SocialIconKind.INSTAGRAM)
                            }

                            Text(
                                PromoSecrets.instagramTitle,
                                modifier = Modifier.weight(1f),
                                color = Color.White,
                                fontFamily = ChandFont,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PromoSecrets.instagramAccounts.forEachIndexed { index, account ->
                            InstagramAccountCard(
                                account = account,
                                index = index,
                                onClick = { onOpen(account) }
                            )
                        }

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            Text(
                                "بستن",
                                fontFamily = ChandFont,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstagramAccountCard(
    account: String,
    index: Int,
    onClick: () -> Unit
) {
    val accent = when (index % 3) {
        0 -> listOf(Color(0xFF7C3AED), Color(0xFFD946EF))
        1 -> listOf(Color(0xFFD946EF), Color(0xFFF97316))
        else -> listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(21.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(21.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(43.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(accent)),
                contentAlignment = Alignment.Center
            ) {
                SocialIcon(SocialIconKind.INSTAGRAM)
            }

            Spacer(Modifier.width(13.dp))

            Text(
                account,
                modifier = Modifier.weight(1f),
                fontFamily = ChandFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "‹",
                fontFamily = ChandFont,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun openInstagram(context: Context, username: String) {
    val appIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(PromoSecrets.instagramAppUri(username))
    ).apply {
        setPackage(PromoSecrets.instagramPackage)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val opened = runCatching {
        context.startActivity(appIntent)
        true
    }.getOrDefault(false)

    if (!opened) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(PromoSecrets.instagramWebUri(username))).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }
}

private fun openTelegram(context: Context, username: String) {
    val appIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(PromoSecrets.telegramAppUri(username))
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

    val opened = runCatching {
        context.startActivity(appIntent)
        true
    }.getOrDefault(false)

    if (!opened) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(PromoSecrets.telegramWebUri(username))).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }
}
