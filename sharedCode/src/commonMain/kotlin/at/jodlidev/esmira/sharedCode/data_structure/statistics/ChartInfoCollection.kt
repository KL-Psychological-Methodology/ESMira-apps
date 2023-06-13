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
	//TODO ChartInfoCollection() is a mess. We should redo it
	@Serializable
	class StatisticServerData (
		val storageType: Int,
		@Serializable(with = JsonToStringSerializer::class) val data: String
	)
	
	internal val dataListContainer: Map<String, Map<String, StatisticData>>
	internal var firstDay: Long = NativeLink.getNowMillis() / 1000
	internal var lastDay: Long = 0L
	val charts: List<ChartInfo>
	var hasPublicData = false
	val studyIsJoined: Boolean
	
	constructor(study: Study) {
		val container = HashMap<String, MutableMap<String, StatisticData>>()
		loadDailyStatisticsFromDb(study, container)
		loadFrequencyDistributionFromDb(study, container)
		
		this.charts = study.personalCharts
		this.dataListContainer = container
		this.studyIsJoined = study.isJoined()
	}
	
	constructor(json: String, study: Study) {
		this.charts = study.publicCharts
		this.dataListContainer = loadJson(json, study)
		this.studyIsJoined = study.isJoined()
	}
	
	private fun finishDailyStatistics() {
		if(lastDay == 0L) //happens when there is only one entry
			lastDay = firstDay
		
		val oneDaySec = 60 * 60 * 24
		firstDay = (firstDay / oneDaySec) * oneDaySec //firstDay needs to start at the beginning of day or fillTimed() will not find any results
	}
	
	
	private fun loadDailyStatisticsFromDb(
		study: Study,
		container: HashMap<String, MutableMap<String, StatisticData>>
	): HashMap<String, MutableMap<String, StatisticData>> {
		val c = NativeLink.sql.select(
			StatisticData_timed.TABLE_CONNECTED,
			StatisticData_timed.COLUMNS_CONNECTED,
			"${StatisticData_timed.TABLE}.${StatisticData_timed.KEY_STUDY_ID} = ?", arrayOf(study.id.toString()),
			null,
			null,
			null,
			null
		)
		
		while(c.moveToNext()) {
			val statistic = StatisticData_timed(c)
			val index = statistic.observableIndex
			val key = statistic.variableName + index
			var dataList: MutableMap<String, StatisticData>
			
			if(container.containsKey(key)) { //load existing list
				dataList = container[key]!!
			}
			else { //create new list
				dataList = HashMap()
				container[key] = dataList
			}
			
			if(statistic.dayTimestampSec < firstDay)
				firstDay = statistic.dayTimestampSec
			else if(statistic.dayTimestampSec > lastDay)
				lastDay = statistic.dayTimestampSec
			//add data:
			dataList[statistic.dayTimestampSec.toString()] = statistic
		}
		c.close()
		
		finishDailyStatistics()
		return container
	}
	
	private fun loadFrequencyDistributionFromDb(
		study: Study,
		container: HashMap<String, MutableMap<String, StatisticData>>
	): HashMap<String, MutableMap<String, StatisticData>> {
		val c = NativeLink.sql.select(
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
				if(container.containsKey(key)) {
					dataList = container[key]!!
				}
				else {
					dataList = HashMap()
					container[key] = dataList
				}
				dataList[statistic.value] = statistic
			} while(c.moveToNext())
		}
		c.close()
		
		return container
	}
	
	
	private fun loadJson(
		json: String,
		study: Study
	): Map<String, Map<String, StatisticData>> {
		val container = HashMap<String, Map<String, StatisticData>>()
		val studyId = study.id
		val rawData: Map<String, List<StatisticServerData>> = DbLogic.getJsonConfig().decodeFromString(json)
		
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
								currentStatisticData.completeData(dayStr.toLong(), key, index, studyId)
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
		
		finishDailyStatistics()
		return container
	}
	
	
	fun addPublicData(publicChartCollection: ChartInfoCollection) {
		hasPublicData = true
		for(chart in charts) {
			if(!studyIsJoined || !chart.hideUntilCompletion)
				chart.builder.addPublicData(publicChartCollection)
		}
	}
}