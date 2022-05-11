package at.jodlidev.esmira.sharedCode

/**
 * Created by JodliDev on 07.05.2020.
 */
interface SQLiteInterface {

	fun select(
			table: String,
			values: Array<String>,
			selection: String?,
			selectionArgs:  Array<String>?,
			groupBy: String?,
			having: String?,
			orderBy: String?,
			limit: String?
	): SQLiteCursor
	
	fun insert(table: String, values: SQLiteValues): Long
	fun update(table: String, values: SQLiteValues, selection: String?, selectionArgs: Array<String>?)

	fun delete(table: String, selection: String?, selectionArgs: Array<String>?)
	fun execSQL(query: String)
	
	fun beginTransaction()
	fun setTransactionSuccessful()
	fun endTransaction()
	
	fun getValueBox(): SQLiteValues
	
	fun close()
}