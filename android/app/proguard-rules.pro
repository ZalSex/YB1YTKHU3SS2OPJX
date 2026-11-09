-keep class com.cloudsync.pro.mobile.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
