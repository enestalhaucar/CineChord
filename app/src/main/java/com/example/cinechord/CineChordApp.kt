package com.example.cinechord

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CineChordApp : Application() {
    
    companion object {
        private const val TAG = "CineChordApp"
        // Geliştirme/test aşamasında true, production aşamasında false yapın
        private const val IS_DEBUG = true
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Check for network connectivity first
            val hasNetwork = isNetworkAvailable()
            if (!hasNetwork) {
                Log.w(TAG, "No network connectivity detected. Firebase services may not work properly.")
            }
            
            // Firebase'i başlat
            FirebaseApp.initializeApp(this)
            
            // Debug modunda veya network yoksa Crashlytics'i devre dışı bırak
            if (IS_DEBUG || !hasNetwork) {
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
                Log.d(TAG, "Crashlytics devre dışı bırakıldı (debug mode veya network yok)")
                
                // In debug mode or no network, also disable Installations service
                try {
                    FirebaseInstallations.getInstance().delete()
                    Log.d(TAG, "Firebase Installations service reset (debug/offline mode)")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to reset Firebase Installations: ${e.message}")
                }
            }
            
            // App Check'i yapılandır - yalnızca debug build'lerde ve network varsa
            if (IS_DEBUG && hasNetwork) {
                val firebaseAppCheck = FirebaseAppCheck.getInstance()
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                Log.d(TAG, "Firebase App Check Debug mode etkinleştirildi")
            } else if (!hasNetwork) {
                Log.d(TAG, "Firebase App Check atlandı (network yok)")
            } else {
                // Production modunda farklı provider kullanılabilir
                Log.d(TAG, "Firebase App Check Debug mode devre dışı - production build")
            }
            
            Log.d(TAG, "Firebase başarıyla yapılandırıldı")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase yapılandırma hatası: ${e.message}", e)
        }
    }
} 

// change when release
//firebaseAppCheck.installAppCheckProviderFactory(
//       PlayIntegrityAppCheckProviderFactory.getInstance()
//   )