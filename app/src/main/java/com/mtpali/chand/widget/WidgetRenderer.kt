package com.mtpali.chand.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.widget.RemoteViews
import com.mtpali.chand.R
import com.mtpali.chand.data.AppPreferences
import com.mtpali.chand.data.DollarRate
import com.mtpali.chand.date.JalaliDate
import com.mtpali.chand.util.PersianNumbers
import com.mtpali.chand.widget.date.PersianDateWidgetReceiver
import com.mtpali.chand.widget.dollar.DollarWidgetReceiver
import kotlin.math.max
import kotlin.math.min

/**
 * Bitmap rendering keeps the widgets stable on MIUI while allowing the launcher to expose
 * several grid sizes. The default target remains 2x2 (the iOS-like medium size), while the
 * provider can now move through smaller and larger grid spans without changing the design.
 */
object WidgetRenderer {

    private const val FALLBACK_SIZE_DP = 148
    private const val MAX_RENDER_DP = 250
    private const val MIN_RENDER_DP = 40
    private const val MAX_BITMAP_SIDE_PX = 620

    fun updateDateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, PersianDateWidgetReceiver::class.java))
        updateDate(context, manager, ids)
    }

    fun updateDate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        val date = JalaliDate.today()
        ids.forEach { id ->
            val size = widgetSizeDp(manager, id)
            val bitmap = renderDate(context, date, size.first, size.second)
            val views = RemoteViews(context.packageName, R.layout.widget_date_ios)
            views.setImageViewBitmap(R.id.date_widget_image, bitmap)
            manager.updateAppWidget(id, views)
        }
    }

    fun updateDollarAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, DollarWidgetReceiver::class.java))
        updateDollar(context, manager, ids)
    }

    fun updateDollar(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        val rate = AppPreferences(context).cachedDollarRate()
        ids.forEach { id ->
            val size = widgetSizeDp(manager, id)
            val bitmap = renderDollar(context, rate, size.first, size.second)
            val views = RemoteViews(context.packageName, R.layout.widget_dollar_ios)
            views.setImageViewBitmap(R.id.dollar_widget_image, bitmap)

            val refreshIntent = Intent(context, DollarWidgetReceiver::class.java).apply {
                action = DollarWidgetReceiver.ACTION_REFRESH
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2401 + id,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.dollar_widget_card, pendingIntent)
            manager.updateAppWidget(id, views)
        }
    }

    private fun widgetSizeDp(manager: AppWidgetManager, id: Int): Pair<Int, Int> {
        val options = manager.getAppWidgetOptions(id)
        val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, FALLBACK_SIZE_DP)
            .coerceIn(MIN_RENDER_DP, MAX_RENDER_DP)
        val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, FALLBACK_SIZE_DP)
            .coerceIn(MIN_RENDER_DP, MAX_RENDER_DP)
        return width to height
    }

    private data class Surface(
        val bitmap: Bitmap,
        val canvas: Canvas,
        val scale: Float,
        val card: RectF,
        val side: Float
    )

    private fun surface(widthDp: Int, heightDp: Int): Surface {
        val largestDp = max(widthDp, heightDp).toFloat()
        val scale = min(3f, MAX_BITMAP_SIDE_PX / largestDp).coerceAtLeast(1.35f)
        val widthPx = (widthDp * scale).toInt().coerceAtLeast(1)
        val heightPx = (heightDp * scale).toInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)

        val hostSide = min(widthPx, heightPx).toFloat()
        val outerInset = 0.65f * scale
        val side = (hostSide - outerInset * 2f).coerceAtLeast(1f)
        val left = (widthPx - side) / 2f
        val top = (heightPx - side) / 2f
        val card = RectF(left, top, left + side, top + side)

        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isDither = true
        }
        val radius = side * 0.195f
        canvas.drawRoundRect(card, radius, radius, cardPaint)

        return Surface(bitmap, canvas, scale, card, side)
    }

    private fun regularTypeface(context: Context): Typeface =
        runCatching { context.resources.getFont(R.font.vazirmatn_regular) }
            .getOrElse { Typeface.create("sans-serif", Typeface.NORMAL) }

    private fun boldTypeface(context: Context): Typeface =
        runCatching { context.resources.getFont(R.font.vazirmatn_bold) }
            .getOrElse { Typeface.create("sans-serif", Typeface.BOLD) }

    private fun textPaint(
        color: Int,
        sizePx: Float,
        typeface: Typeface,
        align: Paint.Align
    ) = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
        this.color = color
        textSize = sizePx
        this.typeface = typeface
        textAlign = align
        isDither = true
    }

    private fun drawCenteredText(canvas: Canvas, text: String, x: Float, centerY: Float, paint: Paint) {
        val fm = paint.fontMetrics
        val baseline = centerY - (fm.ascent + fm.descent) / 2f
        canvas.drawText(text, x, baseline, paint)
    }

    private fun fitText(paint: Paint, text: String, maxWidth: Float, minSize: Float) {
        while (paint.measureText(text) > maxWidth && paint.textSize > minSize) {
            paint.textSize -= 1f
        }
    }

    private fun renderDate(context: Context, date: JalaliDate, widthDp: Int, heightDp: Int): Bitmap {
        val s = surface(widthDp, heightDp)
        val centerX = s.card.centerX()
        val regular = regularTypeface(context)
        val bold = boldTypeface(context)

        val weekday = textPaint(
            Color.rgb(5, 5, 5),
            s.side * 0.106f,
            regular,
            Paint.Align.CENTER
        )
        drawCenteredText(s.canvas, date.dayOfWeek, centerX, s.card.top + s.side * 0.252f, weekday)

        val number = PersianNumbers.digits(date.day)
        val numberPaint = textPaint(
            Color.BLACK,
            s.side * 0.322f,
            bold,
            Paint.Align.CENTER
        )
        fitText(numberPaint, number, s.side * 0.64f, s.side * 0.24f)
        drawCenteredText(s.canvas, number, centerX, s.card.top + s.side * 0.505f, numberPaint)

        val fullDate = "${date.monthName} ${PersianNumbers.digits(date.year)}"
        val fullDatePaint = textPaint(
            Color.rgb(5, 5, 5),
            s.side * 0.106f,
            regular,
            Paint.Align.CENTER
        )
        fitText(fullDatePaint, fullDate, s.side * 0.84f, s.side * 0.078f)
        drawCenteredText(s.canvas, fullDate, centerX, s.card.top + s.side * 0.792f, fullDatePaint)

        return s.bitmap
    }

    private fun renderDollar(context: Context, rate: DollarRate?, widthDp: Int, heightDp: Int): Bitmap {
        val s = surface(widthDp, heightDp)
        val regular = regularTypeface(context)
        val bold = boldTypeface(context)
        val left = s.card.left + s.side * 0.103f
        val right = s.card.right - s.side * 0.103f

        drawFlag(
            context,
            s.canvas,
            s.card.left + s.side * 0.103f,
            s.card.top + s.side * 0.103f,
            s.side * 0.205f
        )

        val title = textPaint(
            Color.rgb(5, 5, 5),
            s.side * 0.089f,
            regular,
            Paint.Align.RIGHT
        )
        drawCenteredText(s.canvas, "دلار آمریکا", right, s.card.top + s.side * 0.162f, title)

        val code = textPaint(
            Color.rgb(136, 136, 141),
            s.side * 0.068f,
            Typeface.create("sans-serif", Typeface.NORMAL),
            Paint.Align.RIGHT
        )
        drawCenteredText(s.canvas, "USD", right, s.card.top + s.side * 0.244f, code)

        val delta = rate?.deltaToman
        val deltaText = when {
            rate == null -> "لمس برای بروزرسانی"
            delta != null && delta > 0 -> "↑${PersianNumbers.grouped(delta)}"
            delta != null && delta < 0 -> "↓${PersianNumbers.grouped(-delta)}"
            else -> "بدون تغییر"
        }
        val deltaColor = when {
            rate == null -> Color.rgb(136, 136, 141)
            delta != null && delta > 0 -> Color.rgb(190, 69, 69)
            delta != null && delta < 0 -> Color.rgb(75, 135, 103)
            else -> Color.rgb(136, 136, 141)
        }
        val deltaPaint = textPaint(
            deltaColor,
            s.side * if (rate == null) 0.071f else 0.078f,
            regular,
            Paint.Align.LEFT
        )
        fitText(deltaPaint, deltaText, s.side * 0.79f, s.side * 0.055f)
        drawCenteredText(s.canvas, deltaText, left, s.card.top + s.side * 0.592f, deltaPaint)

        val price = rate?.let { PersianNumbers.grouped(it.priceToman) } ?: "—"
        val pricePaint = textPaint(
            Color.BLACK,
            s.side * 0.255f,
            bold,
            Paint.Align.LEFT
        )
        fitText(pricePaint, price, s.side * 0.81f, s.side * 0.175f)
        drawCenteredText(s.canvas, price, left, s.card.top + s.side * 0.790f, pricePaint)

        return s.bitmap
    }

    private fun drawFlag(context: Context, canvas: Canvas, left: Float, top: Float, size: Float) {
        val drawable: Drawable = context.getDrawable(R.drawable.us_flag_round) ?: return
        drawable.setBounds(left.toInt(), top.toInt(), (left + size).toInt(), (top + size).toInt())
        drawable.draw(canvas)
    }
}
