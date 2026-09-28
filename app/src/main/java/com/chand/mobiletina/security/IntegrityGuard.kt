package com.chand.mobiletina.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import com.chand.mobiletina.BuildConfig
import com.chand.mobiletina.k.Q
import java.security.MessageDigest

/**
 * Runtime tamper guard for hardened builds.
 *
 * Checked at process startup and again from widget and background entry points. No network
 * operation or WorkManager initialization is performed by this guard.
 */
object IntegrityGuard {
    @Volatile
    private var cachedCertificateState = 0 // 0 unknown, 1 valid, -1 invalid

    fun verify(context: Context): Boolean {
        if (!BuildConfig.SECURE_RUNTIME) return true

        if (context.packageName != BuildConfig.APPLICATION_ID) return false
        if ((context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) return false
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) return false
        if (!certificateMatches(context.applicationContext)) return false

        // Native side independently checks the process identity, TracerPid and common hook maps.
        return Q.h()
    }

    private fun certificateMatches(context: Context): Boolean {
        when (cachedCertificateState) {
            1 -> return true
            -1 -> return false
        }

        val expected = BuildConfig.CERT_LOCK_SHA256
            .replace(":", "")
            .uppercase()
        if (expected.length != 64) {
            cachedCertificateState = -1
            return false
        }

        val actual = signingCertificateSha256(context)
        val ok = actual != null && constantTimeEquals(actual, expected)
        cachedCertificateState = if (ok) 1 else -1
        return ok
    }

    @Suppress("DEPRECATION")
    private fun signingCertificateSha256(context: Context): String? {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
        } else {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
        }

        val signature = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = packageInfo.signingInfo ?: return null
            // Pin the actual APK signer; signingCertificateHistory may include old keys.
            info.apkContentsSigners?.singleOrNull()
        } else {
            packageInfo.signatures?.singleOrNull()
        } ?: return null

        return MessageDigest.getInstance("SHA-256")
            .digest(signature.toByteArray())
            .joinToString(separator = "") { byte -> "%02X".format(byte) }
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }
}
