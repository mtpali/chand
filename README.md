# Chand — ویجت تاریخ شمسی و قیمت دلار برای اندروید

Chand یک اپ اندروید مینیمال است که فقط دو ویجت ارائه می‌دهد:

- **تاریخ شمسی**: روز هفته، روز ماه، ماه و سال به فارسی؛ کاملاً آفلاین.
- **قیمت دلار**: قیمت دلار آمریکا به تومان، تغییر نسبت به آخرین قیمت ذخیره‌شده و بروزرسانی دوره‌ای.

طراحی پروژه با الهام از تجربه مینیمال ویجت‌های iOS انجام شده، اما کد و UI برای Android به‌صورت مستقل ساخته شده است.

## فناوری‌ها

- Kotlin
- Jetpack Compose + Material 3
- Jetpack Glance App Widgets
- WorkManager
- minSdk 26 (Android 8)
- targetSdk / compileSdk 36

## منبع قیمت

برنامه دو مسیر دارد:

1. **AlanChand API رسمی** — اگر توکن تعریف شده باشد.
2. **AlanChand public currency page** — fallback بدون توکن.

طبق مستندات AlanChand، API رسمی با Bearer Token کار می‌کند. توکن را می‌توان از داخل خود برنامه ذخیره کرد یا هنگام build وارد کرد:

```bash
gradle -PALANCHAND_TOKEN="YOUR_TOKEN" :app:assembleDebug
```

> برای انتشار عمومی، API token را داخل APK قرار ندهید. بهتر است بعداً یک proxy کوچک سمت سرور اضافه شود.

## ساخت APK

GitHub Actions در هر push روی `main` تست‌ها را اجرا و APK دیباگ را می‌سازد. از بخش **Actions → Android CI → Artifacts** فایل `chand-debug-apk` قابل دریافت است.

## بروزرسانی

- ویجت قیمت با WorkManager هر ۱۵ دقیقه درخواست بروزرسانی می‌دهد.
- لمس ویجت دلار یک بروزرسانی فوری در صف قرار می‌دهد.
- اندروید برای حفظ باتری ممکن است اجرای دوره‌ای را کمی به تأخیر بیندازد.
- ویجت تاریخ از ساعت و منطقه زمانی خود دستگاه استفاده می‌کند.

## تست

```bash
gradle :app:testDebugUnitTest
```

## ساختار

```text
app/src/main/java/com/mtpali/chand/
├── data/        # دریافت، parse و cache قیمت
├── date/        # تبدیل تاریخ میلادی به جلالی
├── util/        # اعداد فارسی
├── widget/      # دو Glance widget
├── work/        # WorkManager scheduler/worker
└── MainActivity.kt
```

جزئیات بیشتر در `docs/ARCHITECTURE.md` است.
