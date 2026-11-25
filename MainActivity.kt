package pk.zia.attachaki.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.setSupportMultipleWindows(false)
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(AppBridge(this), "Android")
        webView.loadUrl("file:///android_asset/index.html")
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }

    inner class AppBridge(private val context: Context) {
        @JavascriptInterface
        fun printReport(json: String) {
            runOnUiThread {
                try {
                    val r = JSONObject(json)
                    val html = makeReportHtml(r)
                    val printView = WebView(context)
                    printView.settings.javaScriptEnabled = true
                    printView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            val manager = getSystemService(Context.PRINT_SERVICE) as PrintManager
                            val adapter = view.createPrintDocumentAdapter("Zia_Atta_Chaki_Report")
                            manager.print(
                                "ضیاء آٹا چکی رپورٹ",
                                adapter,
                                PrintAttributes.Builder()
                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                    .setResolution(PrintAttributes.Resolution("report", "Report", 300, 300))
                                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                    .build()
                            )
                        }
                    }
                    printView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                } catch (e: Exception) {
                    Toast.makeText(context, "رپورٹ پرنٹ نہیں ہو سکی", Toast.LENGTH_LONG).show()
                }
            }
        }

        private fun makeReportHtml(r: JSONObject): String {
            val date = r.optString("date")
            val wheat = r.optDouble("wheat", 0.0)
            val flour = r.optDouble("flour", 0.0)
            val rate = r.optDouble("rate", 0.0)
            val income = r.optDouble("income", 0.0)
            val expense = r.optDouble("expense", 0.0)
            val notes = r.optString("notes", "").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            val fmt = NumberFormat.getNumberInstance(Locale("en", "PK"))
            val expected = wheat * rate
            val warning = if (kotlin.math.abs(expected - income) > 0.01)
                "<div class='warn'>حساب کی تصدیق: پسائی × ریٹ = ${fmt.format(expected)} روپے، جبکہ درج آمدنی ${fmt.format(income)} روپے ہے۔</div>" else ""
            return """
                <!doctype html><html lang="ur" dir="rtl"><meta charset="utf-8">
                <title>ضیاء آٹا چکی رپورٹ</title>
                <style>
                @page{size:A4;margin:15mm}body{font-family: sans-serif;color:#183b2b;padding:12px}
                header{background:#176b43;color:white;padding:24px;border-radius:12px;text-align:center}
                h1{font-size:28px;margin:0 0 10px}h2{font-size:20px;margin:0}
                table{width:100%;border-collapse:collapse;margin-top:24px}
                td,th{border:1px solid #ccd9cf;padding:13px;text-align:right;font-size:16px}
                th{background:#176b43;color:white}.net{background:#e7f2e9;padding:18px;margin-top:20px;font-size:20px;border-radius:10px}
                .warn{background:#fff2c8;padding:15px;margin-top:20px;border-radius:10px}
                </style><header><h1>ضیاء آٹا چکی</h1><h2>روزانہ کاروباری رپورٹ</h2><p>$date</p></header>
                <table><tr><th>تفصیل</th><th>مقدار / رقم</th></tr>
                <tr><td>گندم پسائی</td><td>${fmt.format(wheat)} کلو</td></tr>
                <tr><td>آٹا تیار</td><td>${fmt.format(flour)} کلو</td></tr>
                <tr><td>فی کلو پسائی ریٹ</td><td>${fmt.format(rate)} روپے</td></tr>
                <tr><td>کل آمدنی</td><td>${fmt.format(income)} روپے</td></tr>
                <tr><td>کل اخراجات</td><td>${fmt.format(expense)} روپے</td></tr></table>
                <div class="net">باقی رقم: <b>${fmt.format(income-expense)} روپے</b></div>
                $warning
                <p>اضافی تفصیلات: ${if (notes.isBlank()) "کوئی تفصیل درج نہیں" else notes}</p>
                </html>
            """.trimIndent()
        }

        @JavascriptInterface
        fun saveCsv(csv: String) {
            runOnUiThread {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val resolver = contentResolver
                        val values = ContentValues().apply {
                            put(android.provider.MediaStore.Downloads.DISPLAY_NAME, "zia-atta-chaki-records.csv")
                            put(android.provider.MediaStore.Downloads.MIME_TYPE, "text/csv")
                            put(android.provider.MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }
                        val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        if (uri != null) resolver.openOutputStream(uri)?.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
                    } else {
                        val file = java.io.File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "zia-atta-chaki-records.csv")
                        file.writeText(csv, Charsets.UTF_8)
                    }
                    Toast.makeText(context, "CSV فائل محفوظ ہوگئی", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "CSV محفوظ نہیں ہو سکی", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
