package com.mtpali.chand

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtpali.chand.data.AppPreferences
import com.mtpali.chand.date.JalaliDate
import com.mtpali.chand.promo.PromoSecrets
import com.mtpali.chand.util.PersianNumbers
import com.mtpali.chand.work.PriceUpdateScheduler

private val ChandFont = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

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
            }

            // Keep only the first three useful information sections from the earlier screen.
            Text(
                "تاریخ شمسی و قیمت دلار، ساده و همیشه در دسترس.",
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
                fontFamily = ChandFont,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                    symbol = "◎",
                    colors = listOf(
                        Color(0xFF6D28D9),
                        Color(0xFFD946EF),
                        Color(0xFFF97316)
                    ),
                    onClick = { showInstagramPanel = true }
                )

                PromoButton(
                    title = PromoSecrets.developerTitle,
                    symbol = "➤",
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
    symbol: String,
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
                Text(
                    symbol,
                    color = Color.White,
                    fontFamily = ChandFont,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
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
                                Text(
                                    "◎",
                                    color = Color.White,
                                    fontFamily = ChandFont,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
                Text(
                    "◎",
                    color = Color.White,
                    fontFamily = ChandFont,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
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
