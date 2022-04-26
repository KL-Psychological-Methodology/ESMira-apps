package mock.mockSql

import at.jodlidev.esmira.sharedCode.SQLiteCursor
import at.jodlidev.esmira.sharedCode.SQLiteInterface
import at.jodlidev.esmira.sharedCode.SQLiteValues

/**
 * Created by JodliDev on 31.03.2022.
 */
class MockDatabase: SQLiteInterface {
	class DeleteStatement(
		val selection: String? = null,
		val selectionArgs: Array<String>? = null
	)
	class UpdateStatement(
		val values: SQLiteValues,
		val selection: String? = null,
		val selectionArgs: Array<String>? = null
	)
	class SelectStatement(
		val values: Array<String>,
		val selection: String?,
		val selectionArgs: Array<String>?,
		val groupBy: String?,
		val having: String?,
		val orderBy: String?,
		val limit: String?
	)
	private var idCounter = 1L
	val selectData = HashMap<String, MutableList<SelectStatement>>()
	val savedData = HashMap<String, MutableList<SQLiteValues>>()
	val updateData = HashMap<String, MutableList<UpdateStatement>>()
	val deletedData = HashMap<String, MutableList<DeleteStatement>>()
	val queries = ArrayList<String>()
	
	private fun isSimpleQuery(selection: String?, selectionArgs: Array<String>?): Boolean {
		return selection?.filterNot { it.isWhitespace() } == "_id=?" && selectionArgs?.size == 1
	}
	
	//Note: we ignore selection and just return everything
	override fun select(
		table: String,
		values: Array<String>,
		selection: String?,
		selectionArgs: Array<String>?,
		groupBy: String?,
		having: String?,
		orderBy: String?,
		limit: String?
	): SQLiteCursor {
		if(!selectData.containsKey(table))
			selectData[table] = ArrayList()
		
		selectData[table]?.add(SelectStatement(values, selection, selectionArgs, groupBy, having, orderBy, limit))
		
		return if(savedData.contains(table)) {
			if(isSimpleQuery(selection, selectionArgs)) {
				val list = ArrayList<SQLiteValues>()
				val id = selectionArgs!![0].toLong()
				
				for(line in savedData[table]!!) {
					val content = (line as MockValues).getValues()
					if(content["_id"] == id)
						list.add(line)
				}
				MockCursor(values, list)
			}
			else
				MockCursor(values, savedData[table]!!)
		}
		else
			MockCursor(values, ArrayList())
	}
	
	override fun insert(table: String, values: SQLiteValues): Long {
		if(!savedData.containsKey(table))
			savedData[table] = ArrayList()
		
		val id = ++idCounter
		values.putLong("_id", id)
		
		savedData[table]?.add(values)
		
		return id
	}
	
	override fun update(
		table: String,
		values: SQLiteValues,
		selection: String?,
		selectionArgs: Array<String>?
	) {
		if(!updateData.containsKey(table))
			updateData[table] = ArrayList()
		updateData[table]?.add(UpdateStatement(values, selection, selectionArgs))
		
		if(!savedData.containsKey(table))
			return
		
		if(selection == null || selection.isEmpty()) {
			val updates = (values as MockValues).getValues()
			for(line in savedData[table]!!) {
				val content = (line as MockValues).getValues()
				for((key, value) in updates) {
					content[key] = value
				}
			}
		}
		else if(isSimpleQuery(selection, selectionArgs)) {
			val updates = (values as MockValues).getValues()
			val id = selectionArgs!![0].toLong()
			for(line in savedData[table]!!) {
				val content = (line as MockValues).getValues()
				if(content["_id"] == id) {
					for((key, value) in updates) {
						content[key] = value
					}
				}
			}
		}
	}
	
	override fun delete(table: String, selection: String?, selectionArgs: Array<String>?) {
		if(!deletedData.containsKey(table))
			deletedData[table] = ArrayList()
		
		deletedData[table]?.add(DeleteStatement(selection, selectionArgs))
		
		if(!savedData.containsKey(table))
			return
		
		//because select() with complex queries does not work properly, this creates more problems, than it solves:
//		if(selection == null || selection.isEmpty()) {
//			savedData[table] = ArrayList()
//		}
//		else if(isSimpleQuery(selection, selectionArgs)) {
//			val tableData = savedData[table]!!
//			val id = selectionArgs!![0].toLong()
//			for((i, line) in tableData.reversed().withIndex()) {
//				val content = (line as MockValues).getValues()
//				if(content["_id"] == id) {
//					tableData.removeAt(i)
//				}
//			}
//		}
	}
	
	override fun execSQL(query: String) {
		queries.add(query)
	}
	
	override fun getValueBox(): SQLiteValues {
		return MockValues()
	}
	
	override fun close() {
		//do nothing
	}
}