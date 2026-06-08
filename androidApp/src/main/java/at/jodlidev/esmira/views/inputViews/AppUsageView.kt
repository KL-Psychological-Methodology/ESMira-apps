package at.jodlidev.esmira.views.inputViews

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.ScreenTrackingReceiver
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.Input
import at.jodlidev.esmira.views.DefaultButton
import at.jodlidev.esmira.views.ESMiraDialog
import java.lang.ref.WeakReference
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.round
import kotlin.random.Random

/**
 * Created by JodliDev on 23.01.2023.
 */


class AppUsageCalculator(context: Context) {
    val context: WeakReference<Context>

    init {
        this.context = WeakReference(context)
    }

    class UsageStatsInfo(
        val count: Int,
        val totalTime: Long,
        val events: List<Pair<Long, Long>>
    ) {
        fun getEventsString(): String {
            return "[" + events.joinToString(",") { (start, end) ->
                val s = round(start / 1000.0).toLong()
                val e = round(end / 1000.0).toLong()
                "[$s,$e]"
            } + "]"
        }
    }

    private class AppUsageCounter(
        val from: Long,
        val to: Long,
        val startEventCode: Int,
        val endEventCode: Int,
        val unexpectedEndEventCode: Int
    ) {
        private var count = 0
        private var totalTime = 0L
        private var events = mutableListOf<Pair<Long, Long>>()

        private var enableTimestamp = 0L
        private var hasEvent = false

        fun addEvent(event: UsageEvents.Event) {
            when (event.eventType) {
                startEventCode -> {
                    ++count
                    enableTimestamp = event.timeStamp
                    hasEvent = true
                }

                endEventCode -> {
                    if (enableTimestamp != 0L) {
                        totalTime += event.timeStamp - enableTimestamp
                        events.add(Pair(enableTimestamp, event.timeStamp))
                        enableTimestamp = 0L
                        hasEvent = true
                    } else if (!hasEvent) {//measures the time from filling out the last questionnaire to turning of the screen
                        totalTime += event.timeStamp - from
                        events.add(Pair(from, event.timeStamp))
                    }

                }

                UsageEvents.Event.DEVICE_SHUTDOWN, unexpectedEndEventCode -> {
                    if (enableTimestamp != 0L) {
                        totalTime += event.timeStamp - enableTimestamp
                        events.add(Pair(enableTimestamp, event.timeStamp))
                        enableTimestamp = 0L
                        hasEvent = true
                    }
                }
            }
        }

        fun getResults(): UsageStatsInfo {
            if (enableTimestamp != 0L) {
                totalTime += to - enableTimestamp
                events.add(Pair(enableTimestamp, to))
            }

            return UsageStatsInfo(count, totalTime, events.toList())
        }
    }

    private fun getUsageStatsManager(): UsageStatsManager {
        val systemService =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) Context.USAGE_STATS_SERVICE else "usagestats"
        return context.get()?.getSystemService(systemService) as UsageStatsManager
    }

    fun countTotalEvents(from: Long, to: Long): UsageStatsInfo {
        val usageStatsManager = getUsageStatsManager()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val counter = AppUsageCounter(
                from,
                to,
                UsageEvents.Event.SCREEN_INTERACTIVE,
                UsageEvents.Event.SCREEN_NON_INTERACTIVE,
                UsageEvents.Event.DEVICE_SHUTDOWN
            )
            val events = usageStatsManager.queryEvents(from, to)
            val event: UsageEvents.Event = UsageEvents.Event()

            while (events.getNextEvent(event)) {
                counter.addEvent(event)
            }

            counter.getResults()
        } else
            context.get()?.let { ScreenTrackingReceiver.getData(it) } ?: UsageStatsInfo(
                -1,
                -1,
                listOf()
            )
    }


    //usageStatsManager.queryAndAggregateUsageStats() seems to be very unreliable. Counting events manually works better
    // Thanks to: https://stackoverflow.com/a/50647945/10423612
    fun getAllPackageUsages(from: Long, to: Long): Map<String, UsageStatsInfo> {
        val usageStatsManager = getUsageStatsManager()
        val counterList = HashMap<String, AppUsageCounter>()

        // Query the list of events that has happened within that time frame
        val systemEvents = usageStatsManager.queryEvents(from, to)
        while (systemEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            systemEvents.getNextEvent(event)

            if (!counterList.containsKey(event.packageName)) {
                counterList[event.packageName] = AppUsageCounter(
                    from,
                    to,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) UsageEvents.Event.ACTIVITY_RESUMED else UsageEvents.Event.MOVE_TO_FOREGROUND,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) UsageEvents.Event.ACTIVITY_PAUSED else UsageEvents.Event.MOVE_TO_BACKGROUND,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) UsageEvents.Event.ACTIVITY_STOPPED else 23
                )
            }

            counterList[event.packageName]?.addEvent(event)
        }

        val returnList = HashMap<String, UsageStatsInfo>()
        for ((packageName, counter) in counterList) {
            returnList[packageName] = counter.getResults()
        }

        return returnList
    }
}

@Composable
fun AppUsageView(input: Input, get: () -> String, save: (String, Map<String, String>) -> Unit) {
    val appUsageCalculator = AppUsageCalculator(LocalContext.current)
    val now = NativeLink.getNowMillis()
    val to = NativeLink.getMidnightMillis()
    val from = to - 86400000
    val displayAppUsage = input.packageId != ""

    val yesterdayUsageTime: Long
    val yesterdayUsageCount: Int
    val yesterdayUsageProtocolString: String
    val yesterdayUsageProtocol: List<Pair<Long, Long>>
    val todayUsageTime: Long
    val todayUsageCount: Int
    val todayUsageProtocol: List<Pair<Long, Long>>
    val todayUsageProtocolString: String

    if (displayAppUsage) {
        val packageUsagesYesterday = appUsageCalculator.getAllPackageUsages(from, to)
        yesterdayUsageCount = packageUsagesYesterday[input.packageId]?.count ?: -1
        yesterdayUsageTime = packageUsagesYesterday[input.packageId]?.totalTime ?: -1L
        yesterdayUsageProtocol = packageUsagesYesterday[input.packageId]?.events ?: listOf()
        yesterdayUsageProtocolString =
            packageUsagesYesterday[input.packageId]?.getEventsString() ?: ""

        val packageUsagesToday = appUsageCalculator.getAllPackageUsages(to, now)
        todayUsageCount = packageUsagesToday[input.packageId]?.count ?: -1
        todayUsageTime = packageUsagesToday[input.packageId]?.totalTime ?: -1L
        todayUsageProtocol = packageUsagesToday[input.packageId]?.events ?: listOf()
        todayUsageProtocolString = packageUsagesToday[input.packageId]?.getEventsString() ?: ""
    } else { //general screen time:
        val yesterdayPair = appUsageCalculator.countTotalEvents(from, to)
        yesterdayUsageCount = yesterdayPair.count
        yesterdayUsageTime = yesterdayPair.totalTime
        yesterdayUsageProtocol = listOf() // Currently not implemented
        yesterdayUsageProtocolString = yesterdayPair.getEventsString()

        val todayPair = appUsageCalculator.countTotalEvents(to, now)
        todayUsageCount = todayPair.count
        todayUsageTime = todayPair.totalTime
        todayUsageProtocol = listOf() // Currently not implemented
        todayUsageProtocolString = todayPair.getEventsString()
    }

    save(
        if (yesterdayUsageCount > 0 && todayUsageCount > 0) "1" else "0", mapOf(
            Pair("usageCountYesterday", yesterdayUsageCount.toString()),
            Pair("usageCountToday", todayUsageCount.toString()),
            Pair("usageTimeYesterday", yesterdayUsageTime.toString()),
            Pair("usageTimeToday", todayUsageTime.toString()),
            Pair("usageProtocolYesterday", yesterdayUsageProtocolString),
            Pair("usageProtocolToday", todayUsageProtocolString)
        )
    )

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

private fun formatTime(context: Context, time: Long): String {
    return if (time == 0L || time == -1L) {
        context.getString(R.string.no_data)
    } else {
        val hours = TimeUnit.MILLISECONDS.toHours(time)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60

        context.getString(
            R.string.time_format_android,
            hours.toString().padStart(2, '0'),
            minutes.toString().padStart(2, '0'),
            seconds.toString().padStart(2, '0')
        )
    }
}

@Composable
fun AppUsageTableView(
    yesterdayUsageCount: Int,
    yesterdayUsageTime: Long,
    yesterdayUsageProtocol: List<Pair<Long, Long>>,
    todayUsageCount: Int,
    todayUsageTime: Long,
    todayUsageProtocol: List<Pair<Long, Long>>,
    displayAppUsage: Boolean,
    packageId: String
) {
    val context = LocalContext.current
    val todayUsageTimeFormatted = formatTime(context, todayUsageTime)
    val yesterdayUsageTimeFormatted = formatTime(context, yesterdayUsageTime)
    val showDetailedData = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(if (displayAppUsage) R.string.colon_app_usage else R.string.colon_total_screenTime),
            fontWeight = FontWeight.Bold
        )
        if (displayAppUsage) {
            Text(
                packageId,
                fontSize = MaterialTheme.typography.labelLarge.fontSize,
                modifier = Modifier.padding(start = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))


        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column {
                Text(" ")
                Spacer(modifier = Modifier.height(5.dp))
                Text(stringResource(R.string.colon_usageTime), fontWeight = FontWeight.Bold)
                if (yesterdayUsageCount > 0) {
                    Text(stringResource(R.string.colon_usageCount), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(15.dp))

            Column {
                Text(stringResource(R.string.yesterday), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(5.dp))
                Text(yesterdayUsageTimeFormatted)
                if (yesterdayUsageCount > 0) {
                    Text(yesterdayUsageCount.toString())
                }
            }

            Spacer(modifier = Modifier.width(15.dp))

            Column {
                Text(stringResource(R.string.today), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(5.dp))
                Text(todayUsageTimeFormatted)
                if (todayUsageCount > 0) {
                    Text(todayUsageCount.toString())
                }
            }
        }
        if (packageId != "") {
            if (showDetailedData.value) {

                ESMiraDialog(
                    content = {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item{Text(
                                stringResource(R.string.app_usage_sessions_yesterday),
                                fontWeight = FontWeight.Bold
                            )}
                            item{if (yesterdayUsageProtocol.isEmpty()) {
                                Text(stringResource(R.string.app_usage_sessions_no_data_yesterday))
                            }}
                            items(yesterdayUsageProtocol) { item ->
                                val start = NativeLink.formatTime(item.first)
                                val end = NativeLink.formatTime(item.second)
                                Column {
                                    Text("$start - $end")
                                }
                            }

                            item{Spacer(modifier = Modifier.height(15.dp))}

                            item{Text(
                                stringResource(R.string.app_usage_sessions_today),
                                fontWeight = FontWeight.Bold
                            )}
                            item{if (todayUsageProtocol.isEmpty()) {
                                Text(stringResource(R.string.app_usage_sessions_no_data_today))
                            }}
                            items(todayUsageProtocol) { item ->
                                val start = NativeLink.formatTime(item.first)
                                val end = NativeLink.formatTime(item.second)
                                Column {
                                    Text("$start - $end")
                                }
                            }
                        }
                    },
                    confirmButtonLabel = stringResource(R.string.ok_),
                    onConfirmRequest = { showDetailedData.value = false }
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()){
                DefaultButton(
                    "Show detailed Data",
                    onClick = {
                        showDetailedData.value = true
                    }
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAppUsageTableViewForAppUsage() {
    ESMiraSurface {
        AppUsageTableView(
            Random.nextInt(1, 20),
            Random.nextLong(1000L * 60 * 5, 1000L * 60 * 300),
            listOf(
                Pair(
                    Random.nextLong(1000L * 60 * 5, 1000L * 60 * 300),
                    Random.nextLong(1000L * 60 * 5, 1000L * 60 * 300)
                )
            ),
            Random.nextInt(1, 20),
            Random.nextLong(1000L * 60 * 5, 1000L * 60 * 300),
            listOf(
                Pair(
                    Random.nextLong(1000L * 60 * 5, 1000L * 60 * 300),
                    Random.nextLong(1000L * 60 * 5, 1000L * 60 * 300)
                )
            ),
            true,
            "at.jodlidev.esmira"
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAppUsageTableViewForAppUsageWithNoData() {
    ESMiraSurface {
        AppUsageTableView(
            -1,
            -1,
            listOf(),
            -1,
            -1,
            listOf(),
            true,
            "at.jodlidev.esmira"
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAppUsageTableViewForScreenTracking() {
    ESMiraSurface {
        AppUsageTableView(
            Random.nextInt(1, 20),
            Random.nextLong(1000L * 60 * 5, 1000L * 60 * 300),
            listOf(),
            Random.nextInt(1, 20),
            Random.nextLong(1000L * 60 * 5, 1000L * 60 * 300),
            listOf(),
            false,
            ""
        )
    }
}