# معماری Chand

## هدف

یک اپ اندروید کوچک با دو App Widget مستقل:

1. تاریخ شمسی آفلاین
2. قیمت دلار آمریکا در ایران

## جریان قیمت

`PriceUpdateWorker -> DollarRepository -> AlanChandClient -> AppPreferences -> DollarWidget`

- اگر `ALANCHAND_TOKEN` موجود باشد، API رسمی AlanChand در اولویت است.
- اگر API در دسترس نباشد یا توکن تعریف نشده باشد، صفحه عمومی `alanchand.com/en/currencies-price` به عنوان fallback خوانده می‌شود.
- آخرین قیمت همیشه در SharedPreferences ذخیره می‌شود تا در قطعی اینترنت ویجت خالی نشود.
- WorkManager هر ۱۵ دقیقه یک اجرای دوره‌ای درخواست می‌کند؛ اندروید می‌تواند زمان واقعی اجرا را برای مصرف باتری جابه‌جا کند.

## تاریخ شمسی

تبدیل Gregorian به Jalali کاملاً داخل برنامه و بدون شبکه انجام می‌شود. تست واحد برای چند تاریخ مرجع وجود دارد.

## امنیت

برای نسخه شخصی می‌توان Token را از داخل اپ وارد کرد یا هنگام build با Gradle property `ALANCHAND_TOKEN` تزریق کرد. برای انتشار عمومی بهتر است توکن روی یک backend/proxy نگه‌داری شود، چون هر secret داخل APK در نهایت قابل استخراج است.
