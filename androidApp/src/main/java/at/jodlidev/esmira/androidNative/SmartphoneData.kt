package at.jodlidev.esmira.androidNative

import android.os.Build
import at.jodlidev.esmira.BuildConfig
import at.jodlidev.esmira.sharedCode.SmartphoneDataInterface
import java.util.*

/**
 * Created by JodliDev on 18.05.2020.
 */
object SmartphoneData: SmartphoneDataInterface {
	override val model: String = Build.MODEL
	override val osVersion: String = Build.VERSION.RELEASE
	override val manufacturer: String = Build.MANUFACTURER
	override val appVersion: String = BuildConfig.VERSION_NAME
	override val appType: String = "Android"
	override val lang: String get() {
		return Locale.getDefault().language
	}
}