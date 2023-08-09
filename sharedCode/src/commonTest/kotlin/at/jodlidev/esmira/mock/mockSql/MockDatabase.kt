package mock.mockSql

import at.jodlidev.esmira.sharedCode.SQLiteCursor
import at.jodlidev.esmira.sharedCode.SQLiteInterface
import at.jodlidev.esmira.sharedCode.SQLiteValues

/**
 * Created by JodliDev on 31.03.2022.
 * mocks basic features of sqlite assuming that database does not have many entries (Performance is at least n. Even more for joined entries!)
 * Does NOT support nested queries example: "a = b AND (a=b OR a=b)"
 * Only supports LEFT JOIN like: "table1 LEFT JOIN table2 ON table1.var1 = table2.var2"
 * Note: Because I am lazy and it is my style anyway: This class assumes that SQL commands are used in upperCase
 */
class MockDatabase: SQLiteInterface {
	class LineData(
		private val actualTable: String,
		actualLines: SQLiteValues,
		joinedTable: String? = null,
		joinedLines: SQLiteValues? = null
	) {
		private val tableLines: Map<String, SQLiteValues> = if(joinedLines == null || joinedTable == null)
			mapOf(Pair(actualTable, actualLines))
		else
			mapOf(Pair(actualTable, actualLines), Pair(joinedTable, joinedLines))
		
		
		fun getActualLines(): MutableMap<String, Comparable<*>?> {
			return (tableLines[actualTable] as MockValues).getValues()
		}
		
		fun getValue(column: String) = getValue(null, column)
		fun getValue(table: String? = null, column: String): Comparable<*>? {
			if(table != null && table.isNotEmpty()) {
				if(!tableLines.contains(table))
					return null
				val values = (tableLines[table] as MockValues).getValues()
				if(values.containsKey(column))
					return values[column]
			}
			else {
				for((_, tableValues) in tableLines) {
					val values = (tableValues as MockValues).getValues()
					if(values.containsKey(column))
						return values[column]
				}
			}
			return null
		}
		
		fun getSortableValue(table: String? = null, column: String): Long {
			return when(val value = getValue(table, column)) {
				is Long -> value
				is Int -> value.toLong()
				else -> value.toString().toLong()
			}
		}
	}
	private class QueryInterpreter(
		val savedData: HashMap<String, MutableList<SQLiteValues>>
	) {
		private class QueryPart(
			val actualTable: String?,
			val column: String,
			val operator: String,
			val value: String
		) {
			override fun toString(): String {
				return "$actualTable.$column $operator $value"
			}
		}
		private class Query(
			val canBeLooped: Boolean = false,
			val connectWithAnd: Boolean = false,
			val queries: MutableList<QueryPart> = ArrayList()
		)
		
		private val word = "[\\w_]+"
		private val tableJoinPattern = "^\\s*($word) LEFT JOIN ($word) ON ($word)\\.($word)\\s*=\\s*($word)\\.($word)\\s*$".toRegex()
		//whitespaces are removed beforehand:
		private val queryPattern = "^(?:($word)\\.)?($word)(=|IS|ISNOT|!=|>|<|>=|<=)(\\?|\\d|'.+'|\".+\")\$".toRegex()
		
		private var joinTable = false
		private var actualTable = ""
		private var joinedTable = ""
		private var actualTableVar = ""
		private var joinedTableVar = ""
		
		private fun prepareTable(tableString: String) {
			val regex = tableJoinPattern.find(tableString)
			if(regex != null) {
				val (actualTable, joinedTable, var1Table, var1, var2Table, var2) = regex.destructured
				
				this.actualTable = actualTable
				
				if(savedData.containsKey(joinedTable)) {
					joinTable = true
					this.joinedTable = joinedTable
					if(var1Table == actualTable && var2Table == joinedTable) {
						actualTableVar = var1
						joinedTableVar = var2
					}
					else if(var2Table == actualTable && var1Table == joinedTable) {
						joinedTableVar = var1
						actualTableVar = var2
					}
					else {
						println("MockDatabase: Could not decipher LEFT JOIN: $tableString")
						joinTable = false
					}
				}
			}
			else
				this.actualTable = tableString
		}
		
		private fun prepareQuery(selection: String?): Query {
			
			if(selection == null) {
				return Query()
			}
			val splitAnd = selection.split(" AND ")
			val splitOr = selection.split(" OR ")
			
			if(splitAnd.size != 1 && splitOr.size != 1) {
				return Query()
			}
			val connectWithAnd: Boolean
			val queryStrings: List<String>
			if(splitAnd.size != 1) {
				connectWithAnd = true
				queryStrings = splitAnd
			}
			else {
				connectWithAnd = false
				queryStrings = splitOr
			}
			
			val queries = ArrayList<QueryPart>()
			for(queryString in queryStrings) {
				val regex = queryPattern.find(queryString.filterNot { it.isWhitespace() })
				if(regex == null) {
					println("MockDatabase: Could not decipher query: $selection")
					return Query()
				}
				val (actualTable, column, operate, quotedValue) = regex.destructured
				val value = if(quotedValue.startsWith("'") || quotedValue.startsWith("\""))
					quotedValue.substring(1,quotedValue.length-1)
				else
					quotedValue
				queries.add(QueryPart(actualTable.ifEmpty { null }, column, operate, value))
			}
			
			return Query(true, connectWithAnd, queries)
		}
		
		private fun queryIsTrue(query: Query, selectionArgs: Array<String>?, lines: LineData): Boolean {
			var valueIndex = 0
			for(queryPart in query.queries) {
				var compareValue: String
				if(queryPart.value == "?") {
					if(selectionArgs == null || selectionArgs.size-1 < valueIndex)
						return false
					compareValue = selectionArgs[valueIndex]
					++valueIndex
				}
				else
					compareValue = queryPart.value
				
				
				val sourceValue = lines.getValue(queryPart.actualTable, queryPart.column) ?: (
					when(compareValue) { //this is a workaround for values that are set on db creation
						"" -> ""
						"0" -> "0"
						else -> null
					})
				
				val isTrue = when(queryPart.operator) {
					"=", "IS" -> sourceValue.toString() == compareValue
					"!=", "ISNOT" -> sourceValue.toString() != compareValue
					">" -> (sourceValue as Long) > compareValue.toLong()
					">=" -> (sourceValue as Long) >= compareValue.toLong()
					"<" -> (sourceValue as Long) < compareValue.toLong()
					"<=" -> (sourceValue as Long) <= compareValue.toLong()
					else -> false
				}
				
				if(query.connectWithAnd) {
					if(!isTrue)
						return false
				}
				else if(isTrue)
					return true
			}
			return query.connectWithAnd
		}
		
		fun getLines(line: SQLiteValues): LineData {
			if(joinTable) {
				val actualVar = (line as MockValues).getValues()[actualTableVar] ?: return LineData(actualTable, line)
				for(joinedLine in savedData[joinedTable]!!) {
					val joinedVar = (joinedLine as MockValues).getValues()[joinedTableVar] ?: return LineData(actualTable, line)
					if(joinedVar == actualVar) {
						return LineData(actualTable, line, joinedTable, joinedLine)
					}
				}
				return LineData(actualTable, line)
			}
			else
				return LineData(actualTable, line)
		}
		
		fun loop(
			tableString: String,
			selection: String?,
			selectionArgs: Array<String>?,
			callback: (i: Int, line: LineData) -> Unit
		) {
			prepareTable(tableString)
			if(!savedData.containsKey(actualTable)) {
				//println("MockDatabase: No entries in $actualTable")
				return
			}
			val lines = savedData[actualTable]!!
			
			val query = prepareQuery(selection)
			
			
			
			if(!query.canBeLooped) {
				println("MockDatabase: Returning everything from table $actualTable\n(query: $selection; $selectionArgs)")
				for((i, line) in lines.withIndex()) {
					callback(i, getLines(line))
				}
			}
			else {
				for((i, line) in lines.withIndex()) {
					val returnLines = getLines(line)
					if(queryIsTrue(query, selectionArgs, returnLines))
						callback(i, returnLines)
				}
			}
		}
	}
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
	
	var queryCallBack: ((String) -> Unit)? = null
	
	private val queryInterpreter: QueryInterpreter = QueryInterpreter(savedData)
	
	private val orderPattern = "^(?:([\\w_]+)\\.)?([\\w_]+)\\s?(ASC|DESC)?\$".toRegex()
	
	//Note: if we cant interpret the selection, we ignore it and just return everything
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
		
		//query:
		var list = ArrayList<LineData>()
		queryInterpreter.loop(table, selection, selectionArgs) { _, line ->
			list.add(line)
		}
		
		//group by
		if(groupBy != null) {
			val split = groupBy.split(".")
			val groupByColumn: String
			var groupByTable: String? = null
			if(split.size == 1)
				groupByColumn = groupBy
			else {
				groupByTable = split[0]
				groupByColumn = split[1]
			}
			val map = HashMap<String, ArrayList<LineData>>()
			
			
			//create groups
			for(line in list) {
				val key = line.getValue(groupByTable, groupByColumn).toString()
				if(!map.containsKey(key))
					map[key] = ArrayList()
				map[key]?.add(line)
			}
			
			//find value for sorting
			var isMax = false
			var sortTable: String? = null
			var sortColumn: String? = null
			for(value in values) {
				val uppercase = value.toUpperCase()
				isMax = if(uppercase.startsWith("MAX("))
					true
				else if(uppercase.startsWith("MIN("))
					false
				else
					continue
				
				val column = value.substring(4, value.length-1)
				
				val columnSplit = column.split(".")
				
				if(columnSplit.size == 1)
					sortColumn = column
				else {
					sortTable = columnSplit[0]
					sortColumn = columnSplit[1]
				}
				
				break
			}
			
			
			//create a new list and fill it with only one element per group
			list = ArrayList()
			
			for((_, groupedLines) in map) {
				if(sortColumn != null) {
					val comparator = { line: LineData ->
						line.getSortableValue(sortTable, sortColumn)
					}
					if(isMax)
						groupedLines.sortByDescending(comparator)
					else
						groupedLines.sortBy(comparator)
				}
				list.add(groupedLines[0])
			}
		}
		
		
		//order:
		if(orderBy != null) {
			val match = orderPattern.find(orderBy)
			if(match != null) {
				val (orderTable, column, type) = match.destructured
				val comparator = { line: LineData ->
					line.getSortableValue(orderTable, column)
				}
				if(type == "DESC") {
					list.sortByDescending(comparator)
				}
				else {
					list.sortBy(comparator)
				}
			}
		}
		
		return MockCursor(values, if(limit != null && list.size > limit.toInt()) list.subList(0, limit.toInt()) else list)
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
		
		
		val updates = (values as MockValues).getValues()
		queryInterpreter.loop(table, selection, selectionArgs) { _, line ->
			val content = line.getActualLines()
			for((key, value) in updates) {
				content[key] = value
			}
		}
	}
	
	override fun delete(table: String, selection: String?, selectionArgs: Array<String>?) {
		if(!deletedData.containsKey(table))
			deletedData[table] = ArrayList()
		
		deletedData[table]?.add(DeleteStatement(selection, selectionArgs))
		
		if(!savedData.containsKey(table))
			return
		
		if(savedData.containsKey(table)) {
			val tableData = savedData[table]!!
			val toRemove = ArrayList<Int>()
			queryInterpreter.loop(table, selection, selectionArgs) { i, _ ->
				toRemove.add(i)
			}
			for(i in toRemove.reversed()) {
				tableData.removeAt(i)
			}
		}
	}
	
	override fun execSQL(query: String) {
		queryCallBack?.let { it(query) }
	}
	
	override fun beginTransaction() {
		//not implemented
	}
	override fun setTransactionSuccessful() {
		//not implemented
	}
	override fun endTransaction() {
		//not implemented
	}
	
	override fun getValueBox(): SQLiteValues {
		return MockValues()
	}
	
	override fun close() {
		//do nothing
	}
	
	fun reset() {
		idCounter = 1L
		selectData.clear()
		savedData.clear()
		updateData.clear()
		deletedData.clear()
	}
}