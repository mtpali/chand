package com.mtpali.chand

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtpali.chand.data.AppPreferences
import com.mtpali.chand.data.WidgetThemeMode
import com.mtpali.chand.date.JalaliDate
import com.mtpali.chand.util.PersianNumbers
import com.mtpali.chand.widget.date.PersianDateWidgetReceiver
import com.mtpali.chand.widget.dollar.DollarWidgetReceiver
import com.mtpali.chand.work.PriceUpdateScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChandRoot() }
    }

    override fun onStart() {
        super.onStart()
        // Opening Chand is the manual refresh action: no extra tap is needed.
        // Also refresh the persisted periodic schedule for users upgrading from
        // versions that checked the price more frequently.
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
    var themeMode by remember { mutableStateOf(prefs.widgetTheme()) }
    var apiToken by remember { mutableStateOf(prefs.userApiToken()) }
    var tokenSaved by remember { mutableStateOf(false) }
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
            "دو ویجت مینیمال برای تاریخ شمسی و قیمت دلار.",
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("بروزرسانی هوشمند", fontWeight = FontWeight.Bold)
                Text(
                    "نرخ دلار هر ۱ ساعت به‌صورت خودکار بررسی می‌شود. برای بروزرسانی زودتر، فقط برنامه چند را باز کنید؛ دریافت قیمت همان لحظه درخواست می‌شود.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedButton(
            onClick = { requestPinWidget(context, PersianDateWidgetReceiver::class.java) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("افزودن ویجت تاریخ") }

        OutlinedButton(
            onClick = { requestPinWidget(context, DollarWidgetReceiver::class.java) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("افزودن ویجت دلار") }

        Text("تنظیمات پیشرفته منبع قیمت", fontWeight = FontWeight.Bold)
        Text(
            "در حالت عادی نیازی به تنظیم چیزی نیست. اگر توکن رسمی AlanChand دارید می‌توانید آن را وارد کنید.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = apiToken,
            onValueChange = { apiToken = it; tokenSaved = false },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("AlanChand API Token (اختیاری)") },
            singleLine = true
        )
        OutlinedButton(onClick = {
            prefs.setUserApiToken(apiToken)
            tokenSaved = true
            PriceUpdateScheduler.enqueueNow(context)
        }) {
            Text(if (tokenSaved) "ذخیره شد ✓" else "ذخیره توکن")
        }

        // Kept for compatibility with preferences from older versions. The current
        // iOS-style widgets intentionally render as white cards in every phone theme.
        Spacer(Modifier.height(2.dp))

        Text("درباره من", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Button(
            onClick = { showInstagramDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("اینستاگرام موبایل تینا")
        }

        OutlinedButton(
            onClick = { openTelegram(context, "vpn963") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("توسعه دهنده برنامه")
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun InstagramAccountsDialog(
    onDismiss: () -> Unit,
    onOpen: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("اینستاگرام موبایل تینا") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("یکی از صفحه‌ها را انتخاب کنید:")
                TextButton(
                    onClick = { onOpen("mobile.tina") },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("@mobile.tina") }
                TextButton(
                    onClick = { onOpen("mobile.tina2") },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("@mobile.tina2") }
                TextButton(
                    onClick = { onOpen("mobile.tinaa") },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("@mobile.tinaa") }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        }
    )
}

@Composable
private fun PreviewCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(body, fontSize = 20.sp)
        }
    }
}

private fun requestPinWidget(context: Context, receiver: Class<*>) {
    val manager = AppWidgetManager.getInstance(context)
    if (manager.isRequestPinAppWidgetSupported) {
        manager.requestPinAppWidget(ComponentName(context, receiver), null, null)
    }
}

private fun openInstagram(context: Context, username: String) {
    val appIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("instagram://user?username=$username")
    ).apply {
        setPackage("com.instagram.android")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val opened = runCatching {
        context.startActivity(appIntent)
        true
    }.getOrDefault(false)

    if (!opened) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/$username/")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}

private fun openTelegram(context: Context, username: String) {
    val appIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("tg://resolve?domain=$username")
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

    val opened = runCatching {
        context.startActivity(appIntent)
        true
    }.getOrDefault(false)

    if (!opened) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$username")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
