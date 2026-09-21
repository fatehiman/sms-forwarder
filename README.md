# Kargoyar SMS Forwarder

اپلیکیشن اندروید متن‌باز برای فوروارد کردن پیامک‌های ورودی بر اساس فیلترهایی که خودتان تعیین می‌کنید، به شماره‌های تلفن و/یا یک آدرس وب‌هوک. این برنامه به‌طور کامل آفلاین کار می‌کند، به‌جز زمانی که یک درخواست وب‌هوک ارسال می‌شود (که آن هم صریحاً توسط کاربر پیکربندی می‌شود).

## English summary

Kargoyar SMS Forwarder is an open-source, native Android app (Kotlin, no Compose) that
forwards incoming SMS messages matching user-defined filters to phone numbers and/or a
webhook URL. It works fully offline apart from the one explicit webhook POST the user
configures. No ads, no analytics, no crash reporting, no Google Play Services / Firebase.

## چگونه فیلترها کار می‌کنند

هر پیامک ورودی با دو فیلتر بررسی می‌شود و **هر دو** باید مطابقت داشته باشند (منطق AND، نه OR):

1. **ارسال‌کننده‌ها**: لیستی جدا شده با ویرگول از شماره‌ها یا نام فرستنده‌ها. اگر خالی باشد، همه فرستنده‌ها مطابقت دارند. مقایسه شماره‌ها با نادیده گرفتن کد کشور/صفر ابتدایی انجام می‌شود (مثلاً `09121234567` با `+989121234567` مطابقت دارد)، و نام‌ها (شناسه‌های غیرعددی فرستنده) به‌صورت دقیق و بدون حساسیت به حروف بزرگ/کوچک مقایسه می‌شوند.
2. **متن**: اگر خالی باشد، هر متنی مطابقت دارد. اگر پر باشد، متن پیامک باید شامل این عبارت باشد (بدون حساسیت به حروف بزرگ/کوچک).

اگر پیامکی هر دو فیلتر را رد کند، به مقصدهای زیر ارسال می‌شود:

- **فوروارد به**: لیستی از شماره تلفن‌ها که پیامک اصلی دوباره به آن‌ها پیامک می‌شود. اختیاری.
- **وب‌هوک**: یک آدرس URL که پیامک به‌صورت JSON با ساختار `{"from", "message", "receivedAt"}` به آن POST می‌شود. اختیاری.

حداقل یکی از این دو مقصد باید پر شود، وگرنه ذخیره‌سازی با خطا مواجه می‌شود.

## مجوزهای لازم و دلیل آن‌ها

| مجوز | دلیل |
|---|---|
| `RECEIVE_SMS` / `READ_SMS` | دریافت و خواندن پیامک‌های ورودی برای بررسی فیلترها |
| `SEND_SMS` | فوروارد پیامک به شماره‌های تعیین‌شده |
| `INTERNET` / `ACCESS_NETWORK_STATE` | ارسال درخواست وب‌هوک |
| `POST_NOTIFICATIONS` | نمایش اعلان سرویس در حال اجرا (اندروید ۱۳ به بعد) |
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_DATA_SYNC` | نگه‌داشتن سرویس در پس‌زمینه برای پایداری بیشتر |
| `RECEIVE_BOOT_COMPLETED` | شروع دوباره سرویس بعد از روشن‌شدن گوشی |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | درخواست غیرفعال‌سازی بهینه‌سازی باتری برای پایداری بیشتر |

## Build instructions

Requirements:
- JDK 17
- Android SDK with platform 34 and build-tools 34.0.0 (or compatible)
- Gradle 8.7 (fetched automatically via the Gradle wrapper, `./gradlew`)

```bash
export JAVA_HOME=/path/to/jdk-17
./gradlew assembleDebug
./gradlew assembleRelease   # requires keystore.properties, see below
```

`local.properties` (gitignored) must contain `sdk.dir=<path-to-your-Android-SDK>`.

### Signing a release build

A release build needs a `keystore.properties` file (gitignored, **never commit it**) at the
repo root:

```properties
storeFile=/absolute/path/to/your-release-key.jks
storePassword=...
keyAlias=...
keyPassword=...
```

If `keystore.properties` is missing, `assembleRelease` falls back to the debug signing config
so the build still succeeds (useful for CI smoke builds), but that output must not be
distributed as a real release.

## Persian font (Vazirmatn)

The app bundles the [Vazirmatn](https://github.com/rastikerdar/vazirmatn) font (OFL-licensed)
for fully offline Persian text rendering, under `app/src/main/res/font/`. The font-family
resource is `app/src/main/res/font/vazirmatn.xml`, referenced by the app theme's
`fontFamily`/`android:fontFamily`. If the TTF files are ever missing or corrupted, replace
them with matching lowercase, underscore-named files (`vazirmatn_regular.ttf`,
`vazirmatn_medium.ttf`, `vazirmatn_semibold.ttf`, `vazirmatn_bold.ttf`) and the app will
pick them up automatically — no other change needed. The full license text is included at
`licenses/OFL-Vazirmatn.txt`.

## Reliability caveats

Android (and especially some OEM skins like MIUI, EMUI, ColorOS) may still kill background
apps or block auto-start even with a foreground service running. To make forwarding more
reliable:

- Open the in-app **دسترسی‌ها** (Permissions) screen and disable battery optimization for
  this app.
- Use the same screen's "تنظیمات اجرای خودکار برنامه" shortcut to open the app's system
  settings page, then separately enable autostart / background activity in your phone
  manufacturer's own security or battery management app (there is no universal Android API
  for this).

## License

MIT — see [LICENSE](LICENSE). Vazirmatn font is licensed under the SIL Open Font License —
see [licenses/OFL-Vazirmatn.txt](licenses/OFL-Vazirmatn.txt).

## Distribution

This app is intentionally **not published on Google Play**. It is meant for direct APK
installation or distribution via open-source app stores/tools such as
[Obtainium](https://github.com/ImranR98/Obtainium) (pointed at this repo's GitHub Releases),
[IzzyOnDroid](https://apt.izzysoft.de/fdroid/), or F-Droid.
