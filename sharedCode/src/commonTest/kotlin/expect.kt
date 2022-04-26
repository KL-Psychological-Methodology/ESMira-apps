import mock.MockDialogOpener
import mock.MockNotifications
import mock.MockPostponedActions
import mock.MockSmartphoneData
import mock.mockSql.MockDatabase

/**
 * Created by JodliDev on 31.03.2022.
 */

expect fun initMockNativeLink(
	sql: MockDatabase,
	smartphoneData: MockSmartphoneData,
	dialogOpener: MockDialogOpener,
	notifications: MockNotifications,
	postponedActions: MockPostponedActions
)