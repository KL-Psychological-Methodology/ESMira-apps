//
// Created by JodliDev on 11.05.20.
//

import Foundation
import SQLite3
import sharedCode

class SQLiteHelper: SQLiteInterface {
	static func getFileUrl() -> URL {
		try! FileManager.default.url(
			for: .documentDirectory,
			in: .userDomainMask,
			appropriateFor: nil,
			create: false
		).appendingPathComponent(DbLogic().DATABASE_NAME)
	}
	
	var db:OpaquePointer? = nil
	var queryCache = [String: OpaquePointer]()
	
	init() {
		sqlite3_shutdown()
		
		let fileURL = SQLiteHelper.getFileUrl()
		
		if(sqlite3_open_v2(fileURL.path, &db, SQLITE_OPEN_CREATE | SQLITE_OPEN_READWRITE | SQLITE_OPEN_FULLMUTEX, nil) != SQLITE_OK) {
			print("error opening database")
			db = nil
			return
		}

		let dbVersion = IosCursorWrapper(queryStatement: getPointer(query: "PRAGMA user_version", addToCache: false))
		
		if(dbVersion.moveToFirst()) {
			let version = dbVersion.getInt(index: 0)
			if(version < DbLogic().DATABASE_VERSION) {
				if(version != 0) {
					DbLogic().updateFrom(db: self, oldVersion: version)
				}
				else {
					DbLogic().createTables(db: self)
				}
				execSQL(query: "PRAGMA user_version = \(DbLogic().DATABASE_VERSION)")
			}
		}
		else { //I think that can never happen
			DbLogic().createTables(db: self)
			execSQL(query: "PRAGMA user_version = \(DbLogic().DATABASE_VERSION)")
		}
	}
	
	func select(table: String, values: KotlinArray<NSString>, selection: String?, selectionArgs: KotlinArray<NSString>?, groupBy: String?, having: String?, orderBy: String?, limit: String?) -> SQLiteCursor {
		var sqlValues = ""
		for i in 0...values.size-1 {
			if(i != 0) {
				sqlValues.append(",")
			}
            sqlValues.append(values.get(index: i)! as String)
		}
		
		var sqlWhere = ""
		if(selection != nil) {
			sqlWhere = " WHERE \(selection!)"
		}
		var sqlGroup = ""
		if(groupBy != nil) {
			sqlGroup = " GROUP BY \(groupBy!)"
		}
		var sqlHaving = ""
		if(having != nil) {
			sqlHaving = " HAVING \(having!)"
		}
		var sqlOrder = ""
		if orderBy != nil {
			sqlOrder = " ORDER BY \(orderBy!)"
		}
		var sqlLimit = ""
		if limit != nil {
			sqlLimit = " LIMIT \(limit!)"
		}
		
		let sqlQuery = "SELECT \(sqlValues) FROM \(table)\(sqlWhere)\(sqlGroup)\(sqlOrder)\(sqlHaving)\(sqlLimit);"
		let pointer = getPointer(query: sqlQuery)
		
		if(pointer != nil) {
			if(selectionArgs != nil) {
				for i in 0...selectionArgs!.size-1 {
                    sqlite3_bind_text(pointer, i+1, (selectionArgs!.get(index: i)!).utf8String, -1, nil)
				}
			}
			
			return IosCursorWrapper(queryStatement: pointer)
		}
		else {
			return IosCursorWrapper(queryStatement: nil)
		}
	}
	
	func insert(table: String, values: SQLiteValues) -> Int64 {
		let content = values.getContent() as! Array<(String, Any)>
		var sqlKeys = ""
		var sqlValues = ""
		
		var notFirstLine = false
		for (key, _) in content {
			if(notFirstLine) {
				sqlKeys.append(",")
				sqlValues.append(",")
			}
			else {
				notFirstLine = true
			}

			sqlKeys.append(key)
			sqlValues.append("?")
		}
		let sqlQuery = "INSERT INTO \(table) (\(sqlKeys)) VALUES (\(sqlValues))"

		guard let pointer: OpaquePointer = getPointer(query: sqlQuery) else {
			print("could not get pointer: \(sqlQuery) ")
			return -5
		}
		
		defer {
			sqlite3_reset(pointer)
		}

		var i: Int32 = 1
		for (_, value) in content {
			bind_value(value: value, pointer: pointer, index: i)
			i += 1
		}

		let state = sqlite3_step(pointer)
		if(state == SQLITE_DONE) {
			return sqlite3_last_insert_rowid(db)
		}
		else {
			print("SQL insert failed! State is \(state): \(sqlQuery)")
			return -10
		}
	}
	
	func update(table: String, values: SQLiteValues, selection: String?, selectionArgs: KotlinArray<NSString>?) {
		let content = values.getContent() as! Array<(String, Any)>
		var sqlValues = ""
		
		var notFirstLine = false
		for (key, _) in content {
			if(notFirstLine) {
				sqlValues.append(",")
			}
			else {
				notFirstLine = true
			}
			sqlValues.append(key)
			sqlValues.append("=?")
		}
		var sqlWhere = ""
		if(selection != nil) {
			sqlWhere = " WHERE \(selection!)"
		}
		
		let query = "UPDATE \(table) SET \(sqlValues)\(sqlWhere)"
		
		guard let pointer: OpaquePointer = getPointer(query: query) else {
			return
		}
		
		defer {
			sqlite3_reset(pointer)
		}
		
		var index: Int32 = 1
		for (_, value) in content {
			bind_value(value: value, pointer: pointer, index: index)
			index += 1
		}
		
		if(selectionArgs != nil) {
			for i in 0...selectionArgs!.size-1 {
                sqlite3_bind_text(pointer, index, (selectionArgs!.get(index: i)!).utf8String, -1, nil)
				index += 1
			}
		}
		let state = sqlite3_step(pointer)
		if(state != SQLITE_DONE) {
			print("Update query failed (state=\(state)):\n\(query)")
		}
	}
	
	func delete(table: String, selection: String?, selectionArgs: KotlinArray<NSString>?) {
		var sqlWhere = ""
		if(selection != nil) {
			sqlWhere = " WHERE \(selection!)"
		}
		let query: String = "DELETE FROM \(table)\(sqlWhere)"
		var pointer: OpaquePointer?
		
		if(sqlite3_prepare(db, query, -1, &pointer, nil) == SQLITE_OK) {
			if(selectionArgs != nil) {
				for i in 0...selectionArgs!.size - 1 {
                    sqlite3_bind_text(pointer, i+1, (selectionArgs!.get(index: i)!).utf8String, -1, nil)
				}
			}
			
			if(sqlite3_step(pointer) != SQLITE_DONE) {
				print("Delete query failed:\n\(query)")
			}
		}
		sqlite3_finalize(pointer)
	}
	
	func execSQL(query: String) {
		var pointer: OpaquePointer?
		sqlite3_prepare(db, query, -1, &pointer, nil)
		
		if(sqlite3_step(pointer) != SQLITE_DONE) {
			print("exec query failed:\n\(query)")
		}
		sqlite3_finalize(pointer)
	}
	
	
	func beginTransaction() {
		var pointer: OpaquePointer?
		sqlite3_prepare(db, "BEGIN", -1, &pointer, nil)
		
		if(sqlite3_step(pointer) != SQLITE_DONE) {
			print("BEGIN failed")
		}
		sqlite3_finalize(pointer)
	}
	func setTransactionSuccessful() {
		
	}
	func endTransaction() {
		var pointer: OpaquePointer?
		sqlite3_prepare(db, "COMMIT", -1, &pointer, nil)
		
		if(sqlite3_step(pointer) != SQLITE_DONE) {
			print("COMMIT failed")
		}
		sqlite3_finalize(pointer)
	}
	
	func getValueBox() -> SQLiteValues {
		IosValuesWrapper()
	}
	
	func close() {
		sqlite3_close_v2(db)
	}
	
	private func bind_value(value: Any, pointer: OpaquePointer, index: Int32) {
		switch value {
			case is Int64, is KotlinLong:
				sqlite3_bind_int64(pointer, index, value as! Int64)
			case is Int32, is KotlinInt:
				sqlite3_bind_int(pointer, index, value as! Int32)
			case is Bool, is KotlinBoolean:
				sqlite3_bind_int(pointer, index, (value as! Bool) ? 1 : 0)
			case is Float, is KotlinFloat:
				sqlite3_bind_double(pointer, index, Double(value as! Float))
			case is Double, is KotlinDouble:
				sqlite3_bind_double(pointer, index, Double(value as! Double))
			case is String, is NSString:
				sqlite3_bind_text(pointer, index, (value as! NSString).utf8String, -1, nil)
			case is NSNull:
				sqlite3_bind_text(pointer, index, nil, -1, nil)
			default:
				sqlite3_bind_text(pointer, index, nil, -1, nil)

		}
	}
	private func getPointer(query:String, addToCache:Bool = true) -> OpaquePointer? {
		//TODO: Using this cache leads to "bind on a busy prepared statement"
		// havent found the issue yet
//		if(addToCache && queryCache[query] != nil) {
//			return queryCache[query]
//		}
		
		var pointer: OpaquePointer?
		let prepareOk = sqlite3_prepare_v2(db, query, -1, &pointer, nil)
		if(prepareOk == SQLITE_OK) {
//			if(addToCache) {
//				queryCache[query] = pointer
//			}
			return pointer
		}
		else {
			print("Could not create pointer (returned \(prepareOk) instead of \(SQLITE_OK): \(query)")
			return nil
		}
	}
	
	class IosCursorWrapper: SQLiteCursor {
		let queryStatement: OpaquePointer?
		
		init(queryStatement: OpaquePointer?) {
			self.queryStatement = queryStatement
		}
		
		
		func close() {
//			sqlite3_finalize(queryStatement)
			sqlite3_reset(queryStatement)
		}
		
		func getBoolean(index: Int32) -> Bool {
			sqlite3_column_int(queryStatement, index) == 1
		}
		func getInt(index: Int32) -> Int32 {
			sqlite3_column_int(queryStatement, index)
		}
		func getLong(index: Int32) -> Int64 {
			sqlite3_column_int64(queryStatement, index)
		}
		func getDouble(index: Int32) -> Double {
			sqlite3_column_double(queryStatement, index)
		}
		func getString(index: Int32) -> String {
			let s = sqlite3_column_text(queryStatement, index)
			return s == nil ? "" : String(cString: s!)
		}
		
		func isNull(index: Int32) -> Bool {
			sqlite3_column_type(queryStatement, index) == SQLITE_NULL
		}
		
		func moveToFirst() -> Bool {
			queryStatement == nil ? false : sqlite3_step(queryStatement) == SQLITE_ROW
		}
		func moveToNext() -> Bool {
			queryStatement == nil ? false : sqlite3_step(queryStatement) == SQLITE_ROW
		}
	}
	class IosValuesWrapper: SQLiteValues {
		private var content: Array<(String, Any)>
		
		init() {
			content = []
		}
		
		func putBoolean(key: String, value: Bool) {
			content.append((key, Int32(value ? 1 : 0)))
		}
		func putInt(key: String, value: Int32) {
			content.append((key, value))
		}
		func putLong(key: String, value: KotlinLong?) {
			content.append((key, value as Any))
		}
		func putDouble(key: String, value: Double) {
			content.append((key, value))
		}
		func putString(key: String, value: String) {
			content.append((key, value))
		}
		
		func getContent() -> Any {
			content
		}
	}
}
