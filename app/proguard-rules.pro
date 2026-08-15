# Keep only the JNI bridge name/method required by the native vault.
-keep,allowoptimization class com.mtpali.chand.promo.Bx {
    native <methods>;
}

# Android/WorkManager instantiate these components outside normal direct call paths.
-keep class com.mtpali.chand.widget.** extends android.appwidget.AppWidgetProvider { *; }
-keep class com.mtpali.chand.work.PriceUpdateWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Stronger symbol/package flattening for distributable builds.
-repackageclasses 'c'
-allowaccessmodification
-adaptclassstrings
-renamesourcefileattribute SourceFile

# Keep metadata required by Kotlin/Compose/WorkManager while still allowing class renaming.
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault,Signature,InnerClasses,EnclosingMethod
