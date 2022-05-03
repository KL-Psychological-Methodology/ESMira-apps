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
abstract class BaseInstrumentedTests : BaseTest() {
	
	abstract fun initDb()
	
	override fun reset() {
		super.reset()
		initDb()
	}
}