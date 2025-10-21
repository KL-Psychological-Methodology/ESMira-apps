package at.jodlidev.esmira.views.main

import android.view.View
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import at.jodlidev.esmira.HtmlHandler
import at.jodlidev.esmira.R
import at.jodlidev.esmira.androidNative.statistics.ChartTypeChooser
import at.jodlidev.esmira.sharedCode.Web
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfo
import at.jodlidev.esmira.sharedCode.data_structure.statistics.ChartInfoCollection
import kotlinx.coroutines.delay

/**
 * Created by JodliDev on 02.03.2023.
 */
@Composable
fun StatisticsView(
	getStudy: () -> Study,
	goBack: () -> Unit
) {
	val context = LocalContext.current
	val study = getStudy()
	val personalChartInfoCollection = ChartInfoCollection(study)
	val personalCharts = personalChartInfoCollection.charts
	for(chartInfo in personalCharts) {
		chartInfo.initBuilder(personalChartInfoCollection, ChartTypeChooser(context))
	}
	
	LaunchedEffect(study.id) {
		study.statisticWasViewed()
	}
	if(study.publicStatisticsNeeded) {
		val publicCharts = study.publicCharts
		val navController = rememberNavController()
		val publicStatistics = produceState<ChartInfoCollection?>(initialValue = null) {
			Web.loadStatistics(
				study,
				onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() },
				onSuccess = { json ->
					val publicChartInfoCollection = ChartInfoCollection(json, study)
					for(chartInfo in publicCharts) {
						chartInfo.initBuilder(publicChartInfoCollection, ChartTypeChooser(context))
					}
					value = publicChartInfoCollection
					personalChartInfoCollection.addPublicData(publicChartInfoCollection)
				})
		}
		
		DefaultScaffoldView(
			title = stringResource(R.string.statistics),
			goBack = goBack,
			bottomBar = { StatisticsBottomBar(navController) }
		) {
			NavHost(navController, startDestination = "personal", modifier = Modifier
				.padding(horizontal = 20.dp)
				.fillMaxWidth()
			) {
				composable("personal") {
					StatisticsContentView(personalCharts, personalChartInfoCollection)
				}
				composable("public") {
					if(publicStatistics.value == null) {
						Box(contentAlignment = Alignment.Center) {
							CircularProgressIndicator()
						}
					}
					else {
						StatisticsContentView(publicCharts, publicStatistics.value!!)
					}
				}
			}
		}
	}
	else {
		StatisticsContentView(personalCharts, personalChartInfoCollection)
	}
}

@Composable
fun StatisticsBottomBar(navController: NavController) {
	val backStackEntry = navController.currentBackStackEntryAsState()
	NavigationBar {
		NavigationBarItem(
			selected = backStackEntry.value?.destination?.route == "personal",
			onClick = {
				navController.navigate("personal") { popUpTo(0) }
			},
			label = { Text(stringResource(R.string.statistics_personal)) },
			icon = {
				Icon(
					imageVector = Icons.Default.Person,
					contentDescription = "personal statistics",
				)
			}
		)
		NavigationBarItem(
			selected = backStackEntry.value?.destination?.route == "public",
			onClick = {
				navController.navigate("public") { popUpTo(0) }
			},
			label = { Text(stringResource(R.string.statistics_public)) },
			icon = {
				Icon(
					imageVector = Icons.Default.Groups,
					contentDescription = "public statistics",
				)
			}
		)
	}
}

@Composable
fun StatisticsContentView(charts: List<ChartInfo>, chartInfoCollection: ChartInfoCollection) {
	Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
		//we cant use LazyColumn because mpAndroidChart can only correctly calculate the position of bar charts when the chart is loaded
		for(chartInfo in charts.filter { !it.hideOnClient }) {
			Spacer(modifier = Modifier.height(10.dp))
			Text(chartInfo.title, fontSize = MaterialTheme.typography.titleLarge.fontSize)
			HtmlHandler.HtmlText(html = chartInfo.chartDescription)
			
			if(chartInfo.hideUntilCompletion && chartInfoCollection.studyIsJoined) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(150.dp)
						.padding(all = 5.dp)
						.border(
							width = 1.dp,
							color = MaterialTheme.colorScheme.outline
						),
					contentAlignment = Alignment.Center
				) {
					Text(stringResource(R.string.visible_when_study_finished),
						color = MaterialTheme.colorScheme.onSurface,
						fontSize = MaterialTheme.typography.bodyLarge.fontSize,
						textAlign = TextAlign.Center,
						modifier = Modifier
							.padding(top = 5.dp, start = 5.dp, end = 5.dp)
							.heightIn(min = 30.dp)
					)
				}
			}
			else {
				val context = LocalContext.current
				Column(horizontalAlignment = Alignment.CenterHorizontally) {
					Box(modifier = Modifier.padding(start = 30.dp)) {
						Row(
							horizontalArrangement = Arrangement.Center,
							modifier = Modifier
								.width(200.dp)
								.graphicsLayer(
									transformOrigin = TransformOrigin(
										pivotFractionX = 0F,
										pivotFractionY = 0F,
									),
									rotationZ = 90F,
								)
						) {
							Text(
								chartInfo.yAxisLabel,
								fontSize = MaterialTheme.typography.labelSmall.fontSize,
								maxLines = 1,
							)
						}
						AndroidView(
							factory = {
								chartInfo.initBuilder(chartInfoCollection, ChartTypeChooser(context))
								chartInfo.builder.createChart() as View
							}, modifier = Modifier
								.height(200.dp)
								.fillMaxWidth()
						)
					}
					
					Text(
						chartInfo.xAxisLabel,
						fontSize = MaterialTheme.typography.labelSmall.fontSize
					)
				}
			}
			Spacer(modifier = Modifier.height(10.dp))
		}
	}
}