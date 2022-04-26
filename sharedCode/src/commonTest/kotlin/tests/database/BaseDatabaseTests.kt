package tests.database

import BaseTest
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.PhoneType
import at.jodlidev.esmira.sharedCode.data_structure.Study
import mock.MockDialogOpener
import mock.MockNotifications
import mock.MockPostponedActions
import mock.MockSmartphoneData

/**
 * Created by JodliDev on 20.04.2022.
 */
abstract class BaseDatabaseTests : BaseTest() {
	internal var smartphoneData = MockSmartphoneData()
	internal var dialogOpener = MockDialogOpener()
	internal var notifications = MockNotifications()
	internal var postponedActions = MockPostponedActions()
	
	abstract fun initDb()
	
	override fun reset() {
		super.reset()
		smartphoneData = MockSmartphoneData()
		dialogOpener = MockDialogOpener()
		notifications = MockNotifications()
		postponedActions = MockPostponedActions()
		initDb()
	}
	
	fun setPhoneType(phoneType: PhoneType) {
		smartphoneData.currentPhoneType = phoneType
	}
}