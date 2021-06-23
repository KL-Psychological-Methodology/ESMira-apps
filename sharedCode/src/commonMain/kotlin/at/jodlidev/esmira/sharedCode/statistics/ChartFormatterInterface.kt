package at.jodlidev.esmira.sharedCode.statistics

/**
 * Created by JodliDev on 29.09.2020.
 */
interface ChartFormatterInterface {
	fun getString(value: Float): String
}