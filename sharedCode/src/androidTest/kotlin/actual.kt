import androidx.test.core.app.ApplicationProvider
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.SQLite
import mock.MockDialogOpener
import mock.MockNotifications
import mock.MockPostponedActions
import mock.MockSmartphoneData
import mock.mockSql.MockDatabase

actual fun initMockNativeLink (
	sql: MockDatabase,
	smartphoneData: MockSmartphoneData,
	dialogOpener: MockDialogOpener,
	notifications: MockNotifications,
	postponedActions: MockPostponedActions
) {
	NativeLink.init(sql, smartphoneData, dialogOpener, notifications, postponedActions)
}