import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.PhoneType
import at.jodlidev.esmira.sharedCode.SQLiteValues
import mock.MockDialogOpener
import mock.MockNotifications
import mock.MockPostponedActions
import mock.MockSmartphoneData
import mock.mockSql.MockDatabase
import mock.mockSql.MockValues
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Created by JodliDev on 31.03.2022.
 */
class MockTools {
	private var sql = MockDatabase()
	private var smartphoneData = MockSmartphoneData()
	private var dialogOpener = MockDialogOpener()
	private var notifications = MockNotifications()
	private var postponedActions = MockPostponedActions()
	
	init {
		initMockNativeLink(sql, smartphoneData, dialogOpener, notifications, postponedActions)
	}
	
	fun reset() {
		sql = MockDatabase()
		smartphoneData = MockSmartphoneData()
		dialogOpener = MockDialogOpener()
		notifications = MockNotifications()
		postponedActions = MockPostponedActions()
		
		initMockNativeLink(sql, smartphoneData, dialogOpener, notifications, postponedActions)
		NativeLink.resetSql(sql)
	}
	
	
	fun getDialogOpener(): MockDialogOpener {
		return dialogOpener
	}
	fun getNotifications(): MockNotifications {
		return notifications
	}
	fun getPostponedActions(): MockPostponedActions {
		return postponedActions
	}
	
	
	fun getSqlSelectMap(): HashMap<String, MutableList<MockDatabase.SelectStatement>> {
		return sql.selectData
	}
	fun getSqlDeleteMap(): HashMap<String, MutableList<MockDatabase.DeleteStatement>> {
		return sql.deletedData
	}
	fun getSqlSavedMap(): HashMap<String, MutableList<SQLiteValues>> {
		return sql.savedData
	}
	fun getSqlUpdateMap(): HashMap<String, MutableList<MockDatabase.UpdateStatement>> {
		return sql.updateData
	}
	fun getSqlQueries(): ArrayList<String> {
		return sql.queries
	}
	
	
	fun getSqlSavedValue(table: String, column: String): Any? {
		val savedData = getSqlSavedMap()
		val data = savedData[table]
		assertNotEquals(0, data?.size ?: 0, "$table has nothing saved")
		val dataSet = (data?.get(data.size-1) as MockValues).getValues()
		return dataSet[column]
	}
	fun assertSqlWasSaved(table: String, column: String, value: Any) {
		assertEquals(value, getSqlSavedValue(table, column))
	}
	fun assertSqlWasSelected(table: String, index: Int, value: String, entryIndex: Int = -1) {
		val selectData = getSqlSelectMap()
		assertTrue(selectData.containsKey(table), "$table has no selections")
		val values = selectData[table] ?: throw Exception("Empty")
		assertEquals(true, values.size > 0)
		
		val entry = values[if(entryIndex == -1) values.size-1 else entryIndex]
		assertEquals(value, entry.selectionArgs?.get(index) ?: "", "$table has another value")
	}
	fun assertSqlWasUpdated(table: String, column: String, value: Any, entryIndex: Int = -1) {
		val updatedData = getSqlUpdateMap()
		assertTrue(updatedData.containsKey(table), "$table has no updates")
		val values = updatedData[table] ?: throw Exception("Empty")
		assertEquals(true, values.size > 0)
		
		val entry = values[if(entryIndex == -1) values.size-1 else entryIndex]
		val mockValues = entry.values as MockValues
		assertEquals(value, mockValues.getValues()[column] ?: "", "$table has another value")
	}
	fun assertSqlWasDeleted(table: String, index: Int, value: String, entryIndex: Int = -1) {
		val deletedData = getSqlDeleteMap()
		assertTrue(deletedData.containsKey(table), "$table has no deletions")
		val values = deletedData[table] ?: throw Exception("Empty")
		assertEquals(true, values.size > 0)
		
		val entry = values[if(entryIndex == -1) values.size-1 else entryIndex]
		assertEquals(value, entry.selectionArgs?.get(index) ?: "", "$table has another value")
	}
	fun assertSqlDidQuery(searchQuery: String) {
		var found = false
		for(query in getSqlQueries()) {
			if(query == searchQuery) {
				found = true
				break
			}
		}
		assertTrue(found)
	}
	
	fun setPhoneType(phoneType: PhoneType) {
		smartphoneData.currentPhoneType = phoneType
	}
}