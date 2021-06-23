package at.jodlidev.esmira.sharedCode.data_structure.statistics

import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.JsonToStringSerializer
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.ObservedVariable
import at.jodlidev.esmira.sharedCode.data_structure.Study
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.Serializable

/**
 * Created by JodliDev on 30.06.2020.
 * not in db
 */
class ChartInfoCollection {
	@Serializable
	class StatisticServerData (
		val storageType: Int,
		@Serializable(with = JsonToStringSerializer::class) val data: String
	)
	
	internal var dataListContainer: Map<String, Map<String, StatisticData>>
	internal var firstDay: Long
	internal var lastDay: Long = 0L
	var charts: List<ChartInfo>
	var hasPublicData = false
	
	constructor(study: Study) {
		charts = study.personalCharts
		val dataListContainer = HashMap<String, MutableMap<String, StatisticData>>()
		//
		//daily statistics:
		//
		firstDay = NativeLink.getNowMillis() / 1000
		
		//get db-cursor:
		var c = NativeLink.sql.select(
			StatisticData_timed.TABLE_CONNECTED,
			StatisticData_timed.COLUMNS_CONNECTED,
			"${StatisticData_timed.TABLE}.${StatisticData_timed.KEY_STUDY_ID} = ?", arrayOf(study.id.toString()),
			null,
			null,
//			"${StatisticData_timed.TABLE}.${StatisticData_timed.KEY_DAY_TIMESTAMP}",
			null,
			null
		)
		
		while(c.moveToNext()) {
			val statistic = StatisticData_timed(c)
			val index = statistic.observableIndex
			val key = statistic.variableName + index
			var dataList: MutableMap<String, StatisticData>
			
			if(dataListContainer.containsKey(key)) { //load existing list
				dataList = dataListContainer[key]!!
			}
			else { //create new list
				dataList = HashMap()
				dataListContainer[key] = dataList
			}
			
			if(statistic.dayTimestampSec < firstDay)
				firstDay = statistic.dayTimestampSec
			else if(statistic.dayTimestampSec > lastDay)
				lastDay = statistic.dayTimestampSec
			//add data:
			dataList[statistic.dayTimestampSec.toString()] = statistic
		}
		c.close()
		if(lastDay == 0L) //happens when there is only one entry
			lastDay = firstDay
		
		//
		//Frequency distribution data:
		//
		c = NativeLink.sql.select(
			StatisticData_perValue.TABLE_CONNECTED,
			StatisticData_perValue.COLUMNS_CONNECTED,
			"${StatisticData_perValue.TABLE}.${StatisticData_perValue.KEY_STUDY_ID} = ?", arrayOf(study.id.toString()),
			null,
			null,
			StatisticData_perValue.KEY_VALUE,
			null
		)
		if(c.moveToFirst()) {
			do {
				val statistic = StatisticData_perValue(c)
				val key = statistic.variableName + statistic.observableIndex
				var dataList: MutableMap<String, StatisticData>
				if(dataListContainer.containsKey(key)) {
					dataList = dataListContainer[key]!!
				}
				else {
					dataList = HashMap()
					dataListContainer[key] = dataList
				}
				dataList[statistic.value] = statistic
			} while(c.moveToNext())
		}
		c.close()
		val oneDaySec = 60 * 60 * 24
		firstDay = (firstDay / oneDaySec) * oneDaySec //firstDay needs to start at the beginning of day or fillTimed() will not find any results
		
		this.dataListContainer = dataListContainer
	}
	
	constructor(json: String, study: Study) {
		charts = study.publicCharts
		
		firstDay = NativeLink.getNowMillis() / 1000
		val studyId = study.id
		val rawData: Map<String, List<StatisticServerData>> = DbLogic.getJsonConfig().decodeFromString(json)
		val container = HashMap<String, Map<String, StatisticData>>()
		
		for((key, serverDataList: List<StatisticServerData>) in rawData) {
			for((index, serverData: StatisticServerData) in serverDataList.withIndex()) {
				val statistics: MutableMap<String, StatisticData> = HashMap()
				container[key + index] = statistics
				
				when(serverData.storageType) {
					ObservedVariable.STORAGE_TYPE_TIMED -> {
						val data: Map<String, StatisticData_timed> = DbLogic.getJsonConfig().decodeFromString(serverData.data)
						
						if(data.isNotEmpty()) {
							//complete daily data:
							for((dayStr, currentStatisticData) in data) {
								currentStatisticData.addData(dayStr.toLong(), key, index, studyId)
								if(dayStr.toLong() < firstDay)
									firstDay = dayStr.toLong()
								else if(dayStr.toLong() > lastDay)
									lastDay = dayStr.toLong()
								
							}
							container[key + index] = data
						}
					}
					ObservedVariable.STORAGE_TYPE_FREQ_DISTR -> {
						val data: Map<String, Int> = DbLogic.getJsonConfig().decodeFromString(serverData.data)
						
						for((value_key, count) in data) {
							statistics[value_key] = StatisticData_perValue(value_key, key, index, count, studyId)
						}
					}
				}
			}
		}
		if(lastDay == 0L) //happens when there is only one entry
			lastDay = firstDay
		
		val oneDaySec = 60 * 60 * 24
		firstDay = (firstDay / oneDaySec) * oneDaySec //firstDay needs to start at the beginning of day or fillTimed() will not find any results
		
		this.dataListContainer = container
	}
	
	@Suppress("unused")
	fun addPublicData(publicChartCollection: ChartInfoCollection) {
		hasPublicData = true
		for(chart in charts) {
			chart.builder.addPublicData(publicChartCollection)
		}
	}
}