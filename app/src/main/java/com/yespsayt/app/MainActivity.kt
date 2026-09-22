package com.yespsayt.app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private val base = "https://boltcatdirilma.tekdemonx.workers.dev"
    private val neon = Color.rgb(45, 255, 85)
    private val bg = Color.rgb(3, 8, 8)
    private var web: WebView? = null
    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private val picker = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        val uris = if (result.resultCode == RESULT_OK) WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data) else null
        fileCallback?.onReceiveValue(uris); fileCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        showSplash()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val w = web
                if (w != null && w.visibility == View.VISIBLE && w.canGoBack()) w.goBack()
                else showWelcome()
            }
        })
    }

    private fun shell(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(24), dp(28), dp(24), dp(24)); setBackgroundColor(bg)
    }

    private fun showSplash() {
        val root = shell().apply { gravity = Gravity.CENTER }
        val logo = ImageView(this).apply { setImageResource(com.yespsayt.app.R.drawable.yespsayt_logo); adjustViewBounds = true }
        root.addView(logo, LinearLayout.LayoutParams(dp(190), dp(190)))
        root.addView(title("YESPSAYT", 38))
        root.addView(label("YOLDA HƏMİŞƏ SƏNİNLƏ", 15, Color.WHITE).apply { letterSpacing = .14f })
        val bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { isIndeterminate = true; progressTintList = android.content.res.ColorStateList.valueOf(neon) }
        root.addView(bar, LinearLayout.LayoutParams(dp(230), dp(8)).apply { topMargin = dp(42) })
        root.addView(label("Yüklənir...", 13, Color.LTGRAY).apply { setPadding(0, dp(10), 0, 0) })
        setContentView(root)
        Handler(Looper.getMainLooper()).postDelayed({ showWelcome() }, 1500)
    }

    private fun showWelcome() {
        web = null
        val root = shell()
        val logo = ImageView(this).apply { setImageResource(R.drawable.yespsayt_logo); adjustViewBounds = true }
        root.addView(logo, LinearLayout.LayoutParams(dp(110), dp(110)))
        root.addView(title("YESPSAYT", 36))
        root.addView(label("YOLDA HƏMİŞƏ SƏNİNLƏ", 14, Color.WHITE).apply { letterSpacing = .12f })
        root.addView(label("Sürücü ol, gəlirini artır", 18, neon).apply { setPadding(0, dp(34), 0, dp(18)) })
        root.addView(infoCard("⚡  Sürətli dəstək     🛡  Təhlükəsiz platforma"))
        root.addView(button("GİRİŞ") { showLogin() })
        root.addView(button("QEYDİYYAT", false) { showRegister() })
        root.addView(label("BİRLİKDƏ DAHA UZAĞA!", 16, neon).apply { setPadding(0, dp(30), 0, 0); setTypeface(typeface, Typeface.BOLD_ITALIC) })
        setContentView(ScrollView(this).apply { setBackgroundColor(bg); addView(root) })
    }

    private fun showLogin() {
        val root = shell()
        root.addView(back("Giriş"))
        root.addView(title("Xoş gəldin!", 30))
        root.addView(label("Hesabına daxil ol və yola davam et.", 14, Color.LTGRAY).apply { setPadding(0, 0, 0, dp(24)) })
        val plate = field("Avtomobil nömrəsi · 99-AA-999")
        val pass = field("Şifrə", true)
        root.addView(plate); root.addView(pass)
        root.addView(button("DAXİL OL") {
            if (plate.text.isBlank() || pass.text.isBlank()) toast("Avtomobil nömrəsi və şifrəni yaz")
            else postToWeb("/driver/login", mapOf("vehicle_plate" to plate.text.toString(), "password" to pass.text.toString()))
        })
        root.addView(TextView(this).apply {
            text = "Hesabın yoxdur?  QEYDİYYAT ET"; textSize = 15f; setTextColor(neon); gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, dp(10)); setOnClickListener { showRegister() }
        })
        setContentView(ScrollView(this).apply { setBackgroundColor(bg); addView(root) })
    }

    private fun showRegister() {
        val root = shell()
        root.addView(back("Sürücü qeydiyyatı"))
        root.addView(title("Sürücü qeydiyyatı", 28))
        root.addView(label("Qazanc dolu yola ilk addımını at!", 14, Color.LTGRAY).apply { setPadding(0, 0, 0, dp(18)) })
        val first = field("Ad")
        val last = field("Soyad")
        val plate = field("Avtomobil nömrəsi · 99-AA-999")
        val pass = field("Şifrə · minimum 6 simvol", true)
        val ref = field("İşçi referral kodu · varsa")
        listOf(first,last,plate,pass,ref).forEach { root.addView(it) }
        root.addView(label("Sənədləri qeydiyyatdan sonra kabinetdə kamera və ya qalereyadan yükləyə bilərsən.", 13, Color.LTGRAY).apply { setPadding(0, dp(4), 0, dp(10)) })
        root.addView(button("QEYDİYYAT ET") {
            if (first.text.isBlank() || last.text.isBlank() || plate.text.isBlank() || pass.text.length < 6) toast("Məlumatları tamamla; şifrə minimum 6 simvol olsun")
            else postToWeb("/driver/register", mapOf("first_name" to first.text.toString(), "last_name" to last.text.toString(), "vehicle_plate" to plate.text.toString(), "password" to pass.text.toString(), "referral_code" to ref.text.toString()))
        })
        root.addView(TextView(this).apply {
            text = "Artıq hesabın var?  GİRİŞ ET"; textSize = 15f; setTextColor(neon); gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, dp(20)); setOnClickListener { showLogin() }
        })
        setContentView(ScrollView(this).apply { setBackgroundColor(bg); addView(root) })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun postToWeb(path: String, fields: Map<String,String>) {
        val w = WebView(this); web = w
        CookieManager.getInstance().setAcceptCookie(true); CookieManager.getInstance().setAcceptThirdPartyCookies(w, true)
        w.settings.javaScriptEnabled = true; w.settings.domStorageEnabled = true; w.settings.allowFileAccess = true
        w.settings.mediaPlaybackRequiresUserGesture = false; w.settings.userAgentString += " YESPSAYT/1.1"
        w.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val u = request.url
                return if (u.host == "boltcatdirilma.tekdemonx.workers.dev") false else { try { startActivity(Intent(Intent.ACTION_VIEW, u)) } catch (_: Exception) {}; true }
            }
        }
        w.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(v: WebView?, cb: ValueCallback<Array<Uri>>?, p: FileChooserParams?): Boolean {
                fileCallback?.onReceiveValue(null); fileCallback = cb
                picker.launch(p?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply { type="image/*"; addCategory(Intent.CATEGORY_OPENABLE) }); return true
            }
        }
        setContentView(w)
        val body = fields.entries.joinToString("&") { URLEncoder.encode(it.key,"UTF-8") + "=" + URLEncoder.encode(it.value,"UTF-8") }
        w.postUrl(base + path, body.toByteArray(Charsets.UTF_8))
    }

    private fun back(text: String) = TextView(this).apply {
        this.text = "‹   $text"; textSize = 17f; setTextColor(Color.WHITE); gravity = Gravity.START
        setPadding(0, 0, 0, dp(22)); setOnClickListener { showWelcome() }
    }
    private fun title(s:String, size:Int)=label(s,size,Color.WHITE).apply { setTypeface(typeface,Typeface.BOLD); gravity=Gravity.CENTER; setShadowLayer(18f,0f,0f,neon) }
    private fun label(s:String,size:Int,color:Int)=TextView(this).apply { text=s; textSize=size.toFloat(); setTextColor(color); gravity=Gravity.CENTER }
    private fun field(hintText:String, password:Boolean=false)=EditText(this).apply {
        hint=hintText; setHintTextColor(Color.rgb(130,150,145)); setTextColor(Color.WHITE); textSize=16f; setPadding(dp(16),0,dp(16),0)
        inputType = if(password) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD else InputType.TYPE_CLASS_TEXT
        background = rounded(Color.rgb(5,18,17), neon, 1.4f, 16f)
        layoutParams = LinearLayout.LayoutParams(-1,dp(58)).apply { bottomMargin=dp(12) }
    }
    private fun button(s:String, bright:Boolean=true, click:()->Unit)=TextView(this).apply {
        text=s+"     →"; textSize=17f; setTypeface(typeface,Typeface.BOLD); gravity=Gravity.CENTER
        setTextColor(Color.WHITE); background=rounded(if(bright) Color.rgb(0,65,22) else Color.rgb(8,13,13), if(bright) neon else Color.LTGRAY,2f,16f)
        setShadowLayer(if(bright) 14f else 0f,0f,0f,neon); setOnClickListener { click() }
        layoutParams=LinearLayout.LayoutParams(-1,dp(60)).apply { topMargin=dp(14) }
    }
    private fun infoCard(s:String)=TextView(this).apply {
        text=s; textSize=14f; setTextColor(Color.WHITE); gravity=Gravity.CENTER; setPadding(dp(12),dp(18),dp(12),dp(18))
        background=rounded(Color.rgb(5,20,18),Color.rgb(20,95,65),1f,16f); layoutParams=LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=dp(12) }
    }
    private fun rounded(fill:Int, stroke:Int, sw:Float, radius:Float)=GradientDrawable().apply { setColor(fill); setStroke(dp(sw.toInt().coerceAtLeast(1)),stroke); cornerRadius=dp(radius.toInt()).toFloat() }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
}
