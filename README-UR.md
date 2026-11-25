# ضیاء آٹا چکی — مکمل Android Studio پروجیکٹ

یہ Android WebView پر مبنی آف لائن ایپ ہے۔ اصل انٹرفیس `app/src/main/assets/index.html` میں ہے، جبکہ Android کی native سہولیات Kotlin میں شامل ہیں۔

## شامل خصوصیات
- روزانہ پسائی، آٹا، فی کلو ریٹ، آمدنی، اخراجات اور نوٹس کا اندراج
- مقامی `localStorage` میں ڈیٹا محفوظ
- روزانہ ڈیش بورڈ اور ماہانہ خلاصہ
- ہر ریکارڈ کی A4 PDF/پرنٹ سہولت (Android Print Framework)
- CSV بیک اپ Downloads میں محفوظ
- انٹرنیٹ کی ضرورت نہیں؛ ایپ کے تمام اثاثے APK کے اندر ہیں

## APK بنانے کا طریقہ
1. کمپیوٹر پر Android Studio (JDK 17 کے ساتھ) انسٹال کریں۔
2. Android Studio میں **Open** منتخب کرکے اس فولڈر کو کھولیں جس میں `settings.gradle.kts` موجود ہے۔
3. Gradle Sync مکمل ہونے دیں۔ Android SDK Platform 35 انسٹال ہونا چاہیے۔
4. **Build > Build Bundle(s) / APK(s) > Build APK(s)** منتخب کریں۔
5. APK عام طور پر `app/build/outputs/apk/debug/app-debug.apk` میں بنے گا۔
6. APK فون پر منتقل کریں، پھر Android کی اجازت کے بعد انسٹال کریں۔

## اہم تکنیکی تفصیل
- Application ID: `pk.zia.attachaki.app`
- Minimum Android: Android 6.0 (API 23)
- Compile/Target SDK: 35
- Kotlin + Android Gradle Plugin
- ایپ مکمل طور پر offline ہے؛ Android `INTERNET` permission استعمال نہیں کرتی۔

## ڈیٹا بیک اپ
ریکارڈ اسی ایپ کے WebView localStorage میں رہتے ہیں۔ ایپ uninstall یا app data clear کرنے سے ڈیٹا ختم ہو سکتا ہے۔ اس لیے ریکارڈز والے صفحے سے CSV بیک اپ باقاعدگی سے بنائیں۔

## نوٹ
یہ مکمل Android Studio سورس پروجیکٹ ہے؛ اس ماحول میں Android SDK/Gradle build tool دستیاب نہ ہونے کی وجہ سے APK بائنری یہاں compile نہیں کی گئی۔ اسے Android Studio میں کھول کر APK بنائیں۔
