package at.jodlidev.esmira.views.inputViews

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.Input

@Composable
fun AppUsageDetailedView(input: Input) {
    val appUsageCalculator = AppUsageCalculator(LocalContext.current)
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