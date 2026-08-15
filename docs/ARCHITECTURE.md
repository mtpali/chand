# معماری chand

## اجزای اصلی

برنامه سه App Widget دارد:

1. تاریخ شمسی مستقل
2. قیمت دلار مستقل
3. ویجت ترکیبی عریض «تاریخ و دلار - iOS» برای چیدمان دو کارت مساوی روی لانچرهایی که دو ویجت مستقل بزرگ را کنار هم نمی‌پذیرند.

## جریان قیمت

`PriceUpdateWorker -> DollarRepository -> AlanChandClient -> AppPreferences -> WidgetRenderer`

- منبع فعلی صفحه عمومی AlanChand است.
- آخرین قیمت موفق در SharedPreferences ذخیره می‌شود.
- WorkManager هر یک ساعت یک کار دوره‌ای ثبت می‌کند.
- لمس ویجت دلار یا ویجت ترکیبی و همچنین باز کردن برنامه، یک کار فوری expedited ایجاد می‌کند.

## رندر ویجت

برای جلوگیری از مشکلات RemoteViews و فونت در MIUI، محتوای هر کارت روی Bitmap/Canvas رندر و سپس در یک ImageView ساده داخل RemoteViews نمایش داده می‌شود.

ویجت ترکیبی اندازه واقعی Host را از AppWidget options می‌خواند و دو کارت مربع را متناسب با عرض و ارتفاع قابل‌استفاده بزرگ یا کوچک می‌کند.

## تاریخ شمسی

تبدیل Gregorian به Jalali داخل خود برنامه انجام می‌شود و به شبکه وابسته نیست.

## سخت‌سازی

- buildهای `release` و `hardened` با R8 و resource shrinking ساخته می‌شوند.
- هویت و مسیرهای تبلیغاتی در resource string table یا ثابت‌های plaintext Kotlin نگه‌داری نمی‌شوند.
- بخش حساس از یک کتابخانه native کوچک خوانده می‌شود و روی package/process اصلی برنامه قفل شده است.
- backup برنامه و cleartext network غیرفعال هستند.
- repository دارای مجوز اختصاصی و منع rebrand/redistribution است.

هیچ حفاظتی در یک APK سمت کاربر مطلق نیست. برای انتشار نهایی، مهم‌ترین لایه مالکیت یک کلید خصوصی release ثابت است که خارج از repository نگه‌داری شود و ترجیحاً از Play App Signing استفاده شود.
