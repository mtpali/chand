package com.mtpali.chand

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LocalLayoutDirection
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
    val date = remember { JalaliDate.today() }
    val cachedRate = prefs.cachedDollarRate()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("چند", fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text(
            "دو ویجت مینیمال برای تاریخ شمسی و قیمت دلار؛ بدون امکانات اضافه.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PreviewCard(
            title = "تاریخ شمسی",
            body = "${date.dayOfWeek}  •  ${PersianNumbers.digits(date.day)} ${date.monthName} ${PersianNumbers.digits(date.year)}"
        )
        PreviewCard(
            title = "دلار آمریکا",
            body = cachedRate?.let { "${PersianNumbers.grouped(it.priceToman)} تومان" }
                ?: "هنوز قیمتی ذخیره نشده؛ بروزرسانی را بزنید."
        )

        Text("تم ویجت", fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WidgetThemeMode.entries.forEach { mode ->
                val label = when (mode) {
                    WidgetThemeMode.AUTO -> "خودکار"
                    WidgetThemeMode.LIGHT -> "روشن"
                    WidgetThemeMode.DARK -> "تیره"
                }
                if (themeMode == mode) {
                    Button(onClick = {
                        themeMode = mode
                        prefs.setWidgetTheme(mode)
                    }) { Text(label) }
                } else {
                    OutlinedButton(onClick = {
                        themeMode = mode
                        prefs.setWidgetTheme(mode)
                    }) { Text(label) }
                }
            }
        }

        Text("منبع قیمت", fontWeight = FontWeight.Bold)
        Text(
            "به‌صورت پیش‌فرض از صفحه عمومی AlanChand خوانده می‌شود. اگر توکن رسمی API داشته باشید، آن را اینجا وارد کنید تا API در اولویت قرار بگیرد.",
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

        Button(
            onClick = { PriceUpdateScheduler.enqueueNow(context) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("بروزرسانی قیمت دلار") }

        OutlinedButton(
            onClick = { requestPinWidget(context, PersianDateWidgetReceiver::class.java) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("افزودن ویجت تاریخ") }

        OutlinedButton(
            onClick = { requestPinWidget(context, DollarWidgetReceiver::class.java) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("افزودن ویجت دلار") }

        Spacer(Modifier.height(12.dp))
        Text(
            "نکته: بروزرسانی خودکار قیمت با WorkManager هر ۱۵ دقیقه درخواست می‌شود؛ زمان دقیق اجرا را اندروید با توجه به باتری و شبکه مدیریت می‌کند.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
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
