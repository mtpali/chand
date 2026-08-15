package com.mtpali.chand

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mtpali.chand.data.AppPreferences
import com.mtpali.chand.date.JalaliDate
import com.mtpali.chand.promo.PromoSecrets
import com.mtpali.chand.util.PersianNumbers
import com.mtpali.chand.work.PriceUpdateScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChandRoot() }
    }

    override fun onStart() {
        super.onStart()
        // Opening the app is the manual refresh gesture.
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
    var showInstagramDialog by remember { mutableStateOf(false) }
    val date = remember { JalaliDate.today() }
    val cachedRate = prefs.cachedDollarRate()

    if (showInstagramDialog) {
        InstagramAccountsDialog(
            onDismiss = { showInstagramDialog = false },
            onOpen = { username ->
                showInstagramDialog = false
                openInstagram(context, username)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("چند", fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text(
            "تاریخ شمسی و قیمت دلار، ساده و همیشه در دسترس.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PreviewCard(
            title = "تاریخ شمسی",
            body = "${date.dayOfWeek}  •  ${PersianNumbers.digits(date.day)} ${date.monthName} ${PersianNumbers.digits(date.year)}"
        )
        PreviewCard(
            title = "دلار آمریکا",
            body = cachedRate?.let { "${PersianNumbers.grouped(it.priceToman)} تومان" }
                ?: "در حال دریافت آخرین قیمت..."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                Modifier.padding(17.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("بروزرسانی خودکار", fontWeight = FontWeight.Bold)
                Text(
                    "نرخ دلار هر ۱ ساعت بررسی می‌شود. برای بروزرسانی فوری روی ویجت دلار بزنید یا برنامه چند را باز کنید.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                "ویجت‌ها از بخش Widgets صفحه اصلی گوشی اضافه می‌شوند. اندازه پیشنهادی ۲×۲ است و امکان تغییر اندازه کنترل‌شده برای چیدمان دو ویجت کنار هم وجود دارد.",
                modifier = Modifier.padding(17.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(4.dp))
        Text("درباره من", fontWeight = FontWeight.Bold, fontSize = 19.sp)

        PromoButton(
            title = PromoSecrets.instagramTitle,
            subtitle = PromoSecrets.instagramSubtitle,
            symbol = "◎",
            colors = listOf(
                Color(0xFF6D28D9),
                Color(0xFFD946EF),
                Color(0xFFF97316)
            ),
            onClick = { showInstagramDialog = true }
        )

        PromoButton(
            title = PromoSecrets.developerTitle,
            subtitle = PromoSecrets.developerSubtitle,
            symbol = "➤",
            colors = listOf(
                Color(0xFF0284C7),
                Color(0xFF2563EB)
            ),
            onClick = { openTelegram(context, PromoSecrets.telegramUser) }
        )

        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun PromoButton(
    title: String,
    subtitle: String,
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
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.17f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    symbol,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.84f),
                    fontSize = 12.sp
                )
            }

            Text(
                "‹",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InstagramAccountsDialog(
    onDismiss: () -> Unit,
    onOpen: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                        .padding(horizontal = 22.dp, vertical = 22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(11.dp)
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
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    PromoSecrets.instagramTitle,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "صفحه موردنظرت را انتخاب کن",
                                    color = Color.White.copy(alpha = 0.88f),
                                    fontSize = 12.sp
                                )
                            }
                        }
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

                    Spacer(Modifier.height(2.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            "بستن",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(accent)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "◎",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "@$account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (index == 0) "صفحه اصلی موبایل تینا" else "صفحه موبایل تینا",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                "‹",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(body, fontSize = 20.sp)
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
