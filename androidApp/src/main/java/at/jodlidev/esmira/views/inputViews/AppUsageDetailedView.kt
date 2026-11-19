package at.jodlidev.esmira.views.inputViews

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.FileUpload
import at.jodlidev.esmira.sharedCode.data_structure.Input
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.round

@Composable
fun AppUsageDetailedView(input: Input) {
    val context = LocalContext.current
    val appUsageCalculator = AppUsageCalculator(context)
    val now = NativeLink.getNowMillis()
    val to = NativeLink.getMidnightMillis()
    val from = to - 86400000
    val displayAppUsage = input.packageId != ""

    val yesterdayUsageTime: Long
    val yesterdayUsageCount: Int
    val yesterdayUsageProtocol: List<Pair<Long, Long>>
    val todayUsageTime: Long
    val todayUsageCount: Int
    val todayUsageProtocol: List<Pair<Long, Long>>

    if (displayAppUsage) {
        val packageUsagesYesterday = appUsageCalculator.getAllPackageUsages(from, to)
        yesterdayUsageCount = packageUsagesYesterday[input.packageId]?.count ?: -1
        yesterdayUsageTime = packageUsagesYesterday[input.packageId]?.totalTime ?: -1L
        yesterdayUsageProtocol = packageUsagesYesterday[input.packageId]?.events ?: listOf()

        val packageUsagesToday = appUsageCalculator.getAllPackageUsages(to, now)
        todayUsageCount = packageUsagesToday[input.packageId]?.count ?: -1
        todayUsageTime = packageUsagesToday[input.packageId]?.totalTime ?: -1L
        todayUsageProtocol = packageUsagesToday[input.packageId]?.events ?: listOf()
    } else { //general screen time:
        val yesterdayPair = appUsageCalculator.countTotalEvents(from, to)
        yesterdayUsageCount = yesterdayPair.count
        yesterdayUsageTime = yesterdayPair.totalTime
        yesterdayUsageProtocol = listOf() // Currently not implemented

        val todayPair = appUsageCalculator.countTotalEvents(to, now)
        todayUsageCount = todayPair.count
        todayUsageTime = todayPair.totalTime
        todayUsageProtocol = listOf() // Currently not implemented
    }

    LaunchedEffect(Unit) {
        val csv = buildString {
            appendLine("day,start,end")
            for((start, end) in yesterdayUsageProtocol) {
                val day = "yesterday"
                val s = round(start / 1000.0).toLong()
                val e = round(end / 1000.0).toLong()
                appendLine("$day,$s,$e")
            }
            for((start, end) in todayUsageProtocol) {
                val day = "today"
                val s = round(start / 1000.0).toLong()
                val e = round(end / 1000.0).toLong()
                appendLine("$day,$s,$e")
            }
        }

        val folder = File(context.filesDir, "app_usage")
        folder.mkdir()
        val file = input.getFileName()?.let { File(it) } ?: File(folder, "${System.currentTimeMillis()}.csv")

        withContext(Dispatchers.IO) {
            file.writeText(csv)
        }
        input.setFile(file.absolutePath, FileUpload.DataTypes.Data, mapOf(
            Pair("usageCountYesterday", yesterdayUsageCount.toString()),
            Pair("usageCountToday", todayUsageCount.toString()),
            Pair("usageTimeYesterday", yesterdayUsageTime.toString()),
            Pair("usageTimeToday", todayUsageTime.toString())
        ))
    }

    AppUsageTableView(
        yesterdayUsageCount,
        yesterdayUsageTime,
        yesterdayUsageProtocol,
        todayUsageCount,
        todayUsageTime,
        todayUsageProtocol,
        displayAppUsage,
        input.packageId
    )
}