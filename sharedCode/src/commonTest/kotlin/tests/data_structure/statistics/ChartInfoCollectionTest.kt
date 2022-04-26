package tests.data_structure.statistics

import BaseTest
import MockTools
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.*
import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfoCollection
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_perValue
import at.jodlidev.esmira.sharedCode.data_structure.statistics.StatisticData_timed
import tests.data_structure.BaseDataStructureTest
import kotlin.test.*

/**
 * Created by JodliDev on 31.03.2022.
 */
class ChartInfoCollectionTest : BaseDataStructureTest() {
	
	@Test
	fun constructorDb() {
		//TODO: no tests
	}
	
	@Test
	fun constructorJson() {
		val study = createStudy()
		val chartInfo = ChartInfoCollection("""{
			"dataSet":[
				{
					"storageType": 0,
					"data": {
						"1603411200":{
							"sum":14,
							"count":4
						},
						"1603497600":{
							"sum":11,
							"count":6
						},
						"1603584000":{
							"sum":31,
							"count":3
						}
					}
				},
				{
					"storageType": 1,
					"data": {
						"column1": 5,
						"column2": 6,
						"column3": 7
					}
				}
			]
		}""", study)
		
		//test StatisticData_timed
		val timed: StatisticData_timed = chartInfo.dataListContainer["dataSet0"]?.get("1603411200") as StatisticData_timed
		assertEquals(1603411200, timed.dayTimestampSec)
		assertEquals(0, timed.observableIndex)
		assertEquals("dataSet", timed.variableName)
		assertEquals(study.id, timed.studyId)
		assertEquals(14.0, timed.sum)
		assertEquals(4, timed.count)
		
		//test StatisticData_perValue
		val column1: StatisticData_perValue = chartInfo.dataListContainer["dataSet1"]?.get("column1") as StatisticData_perValue
		assertEquals("column1", column1.value)
		assertEquals(5, column1.count)
		assertEquals("dataSet", column1.variableName)
		assertEquals(1, column1.observableIndex)
		assertEquals(study.id, column1.studyId)
		
		
		
		assertEquals(1603411200, chartInfo.firstDay)
		assertEquals(1603584000, chartInfo.lastDay)
	}
	
	@Test
	fun loadDailyStatisticsFromDb() {
		//TODO: no tests
	}
	
	@Test
	fun loadFrequencyDistributionFromDb() {
		//TODO: no tests
	}
	
	@Test
	fun addPublicData() {
		//TODO: no tests
	}
}