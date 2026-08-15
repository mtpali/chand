package com.mtpali.chand.widget.combined

import android.annotation.TargetApi
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.widget.RemoteViews
import com.mtpali.chand.R
import com.mtpali.chand.data.AppPreferences
import com.mtpali.chand.data.DollarRate
import com.mtpali.chand.date.JalaliDate
import com.mtpali.chand.util.PersianNumbers
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * One wide Android widget containing two iOS-like cards.
 *
 * Important: the visible cards now follow the real launcher host size. Previous versions capped
 * each card at 158dp, so resizing the host only changed the blue launcher frame while the cards
 * stayed visually unchanged. The card side is now calculated from both the current width and
 * current height, with only a generous safety cap that normal phone layouts never hit.
 */
object CombinedWidgetRenderer {

    private const val FALLBACK_WIDTH_DP = 300
    private const val FALLBACK_HEIGHT_DP = 150
    private const val MIN_WIDTH_DP = 190
    private const val MAX_WIDTH_DP = 480
    private const val MIN_HEIGHT_DP = 88
    private const val MAX_HEIGHT_DP = 480
    private const val MAX_CARD_SIDE_DP = 214f
    private const val MAX_BITMAP_SIDE_PX = 980

    fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, CombinedWidgetReceiver::class.java))
        update(context, manager, ids)
    }

    fun update(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        val date = JalaliDate.today()
        val rate = AppPreferences(context).cachedDollarRate()
        ids.forEach { id ->
            updateOne(context, manager, id, date, rate, manager.getAppWidgetOptions(id))
        }
    }

    fun update(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        updateOne(
            context,
            manager,
            id,
            JalaliDate.today(),
            AppPreferences(context).cachedDollarRate(),
            options
        )
    }

    private fun updateOne(
        context: Context,
        manager: AppWidgetManager,
        id: Int,
        date: JalaliDate,
        rate: DollarRate?,
        options: Bundle
    ) {
        val (widthDp, heightDp) = widgetSizeDp(context, options)
        val bitmap = renderCombined(context, date, rate, widthDp, heightDp)
        val views = RemoteViews(context.packageName, R.layout.widget_combined_ios)
        views.setImageViewBitmap(R.id.combined_widget_image, bitmap)

        val refreshIntent = Intent(context, CombinedWidgetReceiver::class.java).apply {
            action = CombinedWidgetReceiver.ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            5401 + id,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.combined_widget_card, pendingIntent)
        manager.updateAppWidget(id, views)
    }

    private fun widgetSizeDp(context: Context, options: Bundle): Pair<Int, Int> {
        val minWidth = positiveOption(options, AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, FALLBACK_WIDTH_DP)
        val maxWidth = positiveOption(options, AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minWidth)
        val minHeight = positiveOption(options, AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, FALLBACK_HEIGHT_DP)
        val maxHeight = positiveOption(options, AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minHeight)

        val portrait = context.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        val legacyWidth = if (portrait) minWidth else maxWidth
        val legacyHeight = if (portrait) maxHeight else minHeight

        val exact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            closestExactSize(options, legacyWidth, legacyHeight)
        } else {
            null
        }

        val width = (exact?.first ?: legacyWidth).coerceIn(MIN_WIDTH_DP, MAX_WIDTH_DP)
        val height = (exact?.second ?: legacyHeight).coerceIn(MIN_HEIGHT_DP, MAX_HEIGHT_DP)
        return width to height
    }

    private fun positiveOption(options: Bundle, key: String, fallback: Int): Int {
        val value = options.getInt(key, fallback)
        return if (value > 0) value else fallback
    }

    @TargetApi(Build.VERSION_CODES.S)
    @Suppress("DEPRECATION")
    private fun closestExactSize(options: Bundle, targetWidth: Int, targetHeight: Int): Pair<Int, Int>? {
        val sizes = options.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
            ?.filter { it.width > 0f && it.height > 0f }
            .orEmpty()
        if (sizes.isEmpty()) return null

        val best = sizes.minByOrNull { size ->
            abs(size.width - targetWidth) + abs(size.height - targetHeight)
        } ?: return null

        return best.width.roundToInt() to best.height.roundToInt()
    }

    private fun renderCombined(
        context: Context,
        date: JalaliDate,
        rate: DollarRate?,
        widthDp: Int,
        heightDp: Int
    ): Bitmap {
        val largestDp = max(widthDp, heightDp).toFloat()
        val scale = min(3f, MAX_BITMAP_SIDE_PX / largestDp).coerceAtLeast(1.35f)
        val widthPx = (widthDp * scale).roundToInt().coerceAtLeast(1)
        val heightPx = (heightDp * scale).roundToInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)

        val edgeDp = 2f
        val gapDp = 5f

        // Both dimensions participate in sizing. This is the key resize fix.
        val widthLimitedSide = (widthDp - edgeDp * 2f - gapDp) / 2f
        val heightLimitedSide = heightDp - edgeDp * 2f
        val availableSideDp = min(widthLimitedSide, heightLimitedSide).coerceAtLeast(68f)

        // Fill about 97% of the available square. There is no 158dp fixed-size ceiling anymore.
        // The safety cap only prevents absurdly large bitmaps on launchers that report huge sizes.
        val sideDp = min(availableSideDp * 0.97f, MAX_CARD_SIDE_DP)
        val sidePx = sideDp * scale
        val gapPx = gapDp * scale
        val pairWidth = sidePx * 2f + gapPx
        val startX = (widthPx - pairWidth) / 2f
        val top = (heightPx - sidePx) / 2f

        val dateCard = RectF(startX, top, startX + sidePx, top + sidePx)
        val dollarCard = RectF(
            startX + sidePx + gapPx,
            top,
            startX + sidePx * 2f + gapPx,
            top + sidePx
        )

        drawCardBackground(canvas, dateCard)
        drawCardBackground(canvas, dollarCard)
        drawDate(context, canvas, dateCard, date)
        drawDollar(context, canvas, dollarCard, rate)
        return bitmap
    }

    private fun drawCardBackground(canvas: Canvas, card: RectF) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isDither = true
        }
        val radius = card.width() * 0.195f
        canvas.drawRoundRect(card, radius, radius, paint)
    }

    private fun regularTypeface(context: Context): Typeface =
        runCatching { context.resources.getFont(R.font.vazirmatn_regular) }
            .getOrElse { Typeface.create("sans-serif", Typeface.NORMAL) }

    private fun boldTypeface(context: Context): Typeface =
        runCatching { context.resources.getFont(R.font.vazirmatn_bold) }
            .getOrElse { Typeface.create("sans-serif", Typeface.BOLD) }

    private fun textPaint(color: Int, sizePx: Float, typeface: Typeface, align: Paint.Align) =
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
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

    private fun drawDate(context: Context, canvas: Canvas, card: RectF, date: JalaliDate) {
        val side = card.width()
        val centerX = card.centerX()
        val regular = regularTypeface(context)
        val bold = boldTypeface(context)

        val weekday = textPaint(Color.rgb(5, 5, 5), side * 0.106f, regular, Paint.Align.CENTER)
        drawCenteredText(canvas, date.dayOfWeek, centerX, card.top + side * 0.252f, weekday)

        val number = PersianNumbers.digits(date.day)
        val numberPaint = textPaint(Color.BLACK, side * 0.322f, bold, Paint.Align.CENTER)
        fitText(numberPaint, number, side * 0.64f, side * 0.24f)
        drawCenteredText(canvas, number, centerX, card.top + side * 0.505f, numberPaint)

        val fullDate = "${date.monthName} ${PersianNumbers.digits(date.year)}"
        val fullDatePaint = textPaint(Color.rgb(5, 5, 5), side * 0.106f, regular, Paint.Align.CENTER)
        fitText(fullDatePaint, fullDate, side * 0.84f, side * 0.078f)
        drawCenteredText(canvas, fullDate, centerX, card.top + side * 0.792f, fullDatePaint)
    }

    private fun drawDollar(context: Context, canvas: Canvas, card: RectF, rate: DollarRate?) {
        val side = card.width()
        val regular = regularTypeface(context)
        val bold = boldTypeface(context)
        val left = card.left + side * 0.103f
        val right = card.right - side * 0.103f

        drawFlag(context, canvas, card.left + side * 0.103f, card.top + side * 0.103f, side * 0.205f)

        val title = textPaint(Color.rgb(5, 5, 5), side * 0.089f, regular, Paint.Align.RIGHT)
        drawCenteredText(canvas, "دلار آمریکا", right, card.top + side * 0.162f, title)

        val code = textPaint(
            Color.rgb(136, 136, 141),
            side * 0.068f,
            Typeface.create("sans-serif", Typeface.NORMAL),
            Paint.Align.RIGHT
        )
        drawCenteredText(canvas, "USD", right, card.top + side * 0.244f, code)

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
            side * if (rate == null) 0.071f else 0.078f,
            regular,
            Paint.Align.LEFT
        )
        fitText(deltaPaint, deltaText, side * 0.79f, side * 0.055f)
        drawCenteredText(canvas, deltaText, left, card.top + side * 0.592f, deltaPaint)

        val price = rate?.let { PersianNumbers.grouped(it.priceToman) } ?: "—"
        val pricePaint = textPaint(Color.BLACK, side * 0.255f, bold, Paint.Align.LEFT)
        fitText(pricePaint, price, side * 0.81f, side * 0.175f)
        drawCenteredText(canvas, price, left, card.top + side * 0.790f, pricePaint)
    }

    private fun drawFlag(context: Context, canvas: Canvas, left: Float, top: Float, size: Float) {
        val drawable: Drawable = context.getDrawable(R.drawable.us_flag_round) ?: return
        drawable.setBounds(left.toInt(), top.toInt(), (left + size).toInt(), (top + size).toInt())
        drawable.draw(canvas)
    }
}
