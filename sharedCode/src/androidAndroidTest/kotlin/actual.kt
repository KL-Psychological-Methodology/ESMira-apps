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
	//not needed
//	val sql = SQLite(ApplicationProvider.getApplicationContext(), null)
//	NativeLink.init(sql, smartphoneData, dialogOpener, notifications, postponedActions)
}