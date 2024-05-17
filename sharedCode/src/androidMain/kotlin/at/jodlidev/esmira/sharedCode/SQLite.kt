package at.jodlidev.esmira.sharedCode

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.SQLiteCursor
import at.jodlidev.esmira.sharedCode.SQLiteInterface
import at.jodlidev.esmira.sharedCode.SQLiteValues


/**
 * Created by JodliDev on 05.05.2020.
 */

class SQLite : SQLiteInterface, SQLiteOpenHelper {
	public constructor(context: Context?, name: String? = DbLogic.DATABASE_NAME, version: Int = DbLogic.DATABASE_VERSION):
			super(context, name, null, version) {}

	private class DirectWrapper(private val db: SQLiteDatabase) : SQLiteInterface {
		override fun select(
			table: String,
			values: Array<String>,
			selection: String?,
			selectionArgs: Array<String>?,
			groupBy: String?,
			having: String?,
			orderBy: String?,
			limit: String?
		): SQLiteCursor = AndroidCursorWrapper(db.query(table, values, selection, selectionArgs, groupBy, having, orderBy, limit))
		
		override fun execSQL(query: String) = db.execSQL(query)
		
		override fun beginTransaction() {
			db.beginTransaction()
		}
		override fun setTransactionSuccessful() {
			db.setTransactionSuccessful()
		}
		override fun endTransaction() {
			db.endTransaction()
		}
		
		override fun insert(table: String, values: SQLiteValues): Long = db.insert(table, null, values.getContent() as ContentValues)
		
		override fun update(table: String, values: SQLiteValues, selection: String?, selectionArgs: Array<String>?) {
			db.update(table, values.getContent() as ContentValues, selection, selectionArgs)
		}
		
		override fun delete(table: String, selection: String?, selectionArgs: Array<String>?) {
			db.delete(table, selection, selectionArgs)
		}
		
		override fun getValueBox(): SQLiteValues = AndroidValuesWrapper()
		
		override fun close() {
			db.close()
		}
	}
	
	override fun onCreate(db: SQLiteDatabase) {
		DbLogic.createTables(DirectWrapper(db))
	}
	
	override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
	
	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		DbLogic.updateFrom(DirectWrapper(db), oldVersion)
	}
	
	override fun select(
		table: String,
		values: Array<String>,
		selection: String?,
		selectionArgs: Array<String>?,
		groupBy: String?,
		having: String?,
		orderBy: String?,
		limit: String?
	): SQLiteCursor = AndroidCursorWrapper(readableDatabase.query(table, values, selection, selectionArgs, groupBy, having, orderBy, limit))
	
	override fun execSQL(query: String) = writableDatabase.execSQL(query)
	
	override fun beginTransaction() {
		writableDatabase.beginTransaction()
	}
	override fun setTransactionSuccessful() {
		writableDatabase.setTransactionSuccessful()
	}
	override fun endTransaction() {
		writableDatabase.endTransaction()
	}
	
	override fun insert(table: String, values: SQLiteValues): Long = writableDatabase.insert(table, null, values.getContent() as ContentValues)
	
	override fun update(table: String, values: SQLiteValues, selection: String?, selectionArgs: Array<String>?) {
		writableDatabase.update(table, values.getContent() as ContentValues, selection, selectionArgs)
	}
	
	override fun delete(table: String, selection: String?, selectionArgs: Array<String>?) {
		writableDatabase.delete(table, selection, selectionArgs)
	}
	
	override fun getValueBox(): SQLiteValues = AndroidValuesWrapper()
	
	companion object {
		class AndroidCursorWrapper(private val cursor: Cursor) : SQLiteCursor {
			override fun getBoolean(index: Int): Boolean {
				return cursor.getInt(index) == 1
			}
			override fun getInt(index: Int): Int {
				return cursor.getInt(index)
			}
			override fun getLong(index: Int): Long {
				return cursor.getLong(index)
			}
			override fun getDouble(index: Int): Double {
				return cursor.getDouble(index)
			}
			override fun getString(index: Int): String {
				return cursor.getString(index) ?: ""
			}
			
			override fun moveToNext(): Boolean {
				return cursor.moveToNext()
			}
			override fun moveToFirst(): Boolean {
				return cursor.moveToFirst()
			}
			
			override fun isNull(index: Int): Boolean {
				return cursor.isNull(index)
			}
			
			override fun close() {
				cursor.close()
			}
			
		}
		
		class AndroidValuesWrapper : SQLiteValues {
			private val content: ContentValues = ContentValues()
			
			override fun putBoolean(key: String, value: Boolean) {
				content.put(key, if(value) 1 else 0)
			}
			override fun putInt(key: String, value: Int) = content.put(key, value)
			override fun putLong(key: String, value: Long?) = content.put(key, value)
			override fun putDouble(key: String, value: Double) = content.put(key, value)
			override fun putString(key: String, value: String) = content.put(key, value)
			
			override fun getContent(): Any = content
		}
	}
}