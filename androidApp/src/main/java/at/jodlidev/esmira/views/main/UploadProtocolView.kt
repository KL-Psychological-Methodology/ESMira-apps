package at.jodlidev.esmira.views.main

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.NativeLink
import at.jodlidev.esmira.sharedCode.data_structure.DataSet
import at.jodlidev.esmira.sharedCode.data_structure.Study
import at.jodlidev.esmira.views.ESMiraDialog

/**
 * Created by JodliDev on 24.02.2023.
 */

@Composable
fun UploadProtocolView(
	getDataSets: () -> List<DataSet>,
	reSyncDataSets: (() -> Unit) -> Unit,
	goBack: () -> Unit
) {
	val dataSetToDelete = remember { mutableStateOf<DataSet?>(null) }
	val dataSets = remember { mutableStateOf(getDataSets()) }
	if(dataSetToDelete.value != null) {
		ESMiraDialog(
			onDismissRequest = { dataSetToDelete.value = null },
			dismissButtonLabel = stringResource(R.string.cancel),
			confirmButtonLabel = stringResource(R.string.ok_),
			onConfirmRequest = {
				dataSetToDelete.value?.delete()
				dataSets.value = getDataSets()
				dataSetToDelete.value = null
			}
		) {
			Text(stringResource(R.string.are_you_sure))
		}
	}
	DefaultScaffoldView(
		title = stringResource(R.string.upload_protocol),
		goBack = goBack,
		actions = {
			val showLoader = remember { mutableStateOf(false) }
			
			IconButton(onClick = {
				if(!showLoader.value) {
					showLoader.value = true
					reSyncDataSets {
						showLoader.value = false
						dataSets.value = getDataSets()
					}
				}
			}
			) {
				if(showLoader.value) {
					CircularProgressIndicator(
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier
							.size(ButtonDefaults.IconSize)
					)
				}
				else
					Icon(imageVector = Icons.Default.Sync, contentDescription = "upload dataSets")
			}
		}
	) {
		LazyColumn(modifier = Modifier.fillMaxWidth()) {
			itemsIndexed(dataSets.value, key = { _, dataSet -> dataSet.id }) { i, dataSet ->
				DataSetItemView(
					dataSet = dataSet,
					deleteDataSet = { dataSetToDelete.value = dataSet },
					modifier = Modifier.background(color = if(i % 2 != 0) colorLineBackground1 else colorLineBackground2)
				)
			}
		}
	}
}

@Composable
fun DataSetItemView(dataSet: DataSet, deleteDataSet: () -> Unit, modifier: Modifier = Modifier) {
	Row(
		modifier = modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		Column(
			modifier = Modifier
				.padding(horizontal = 30.dp, vertical = 20.dp)
				.weight(1F)
		) {
			Text(
				NativeLink.formatDateTime(dataSet.responseTime),
				fontSize = MaterialTheme.typography.labelMedium.fontSize,
				modifier = Modifier.align(Alignment.End)
			)
			Row(verticalAlignment = Alignment.CenterVertically) {
				when(dataSet.synced) {
					DataSet.STATES.SYNCED ->
						Icon(Icons.Default.CheckCircle, "true", tint = colorGreen)
					DataSet.STATES.NOT_SYNCED ->
						Icon(Icons.Default.AccessTime, "true")
					DataSet.STATES.NOT_SYNCED_ERROR ->
						Icon(Icons.Default.Warning, "true")
					DataSet.STATES.NOT_SYNCED_SERVER_ERROR ->
						Icon(Icons.Default.Cancel, "true", tint = MaterialTheme.colorScheme.error)
					
				}
				Spacer(Modifier.size(ButtonDefaults.IconSpacing))
				Text(dataSet.eventType.uppercase(), fontSize = MaterialTheme.typography.labelLarge.fontSize, fontWeight = FontWeight.Bold)
			}
			Text(dataSet.questionnaireName, fontSize = MaterialTheme.typography.labelMedium.fontSize, modifier = Modifier.padding(start = 20.dp))
		}
		if(dataSet.synced == DataSet.STATES.NOT_SYNCED_SERVER_ERROR) {
			IconButton(onClick = deleteDataSet) {
				Icon(Icons.Default.Delete, "remove")
			}
		}
	}
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewUploadProtocolView() {
	ESMiraSurface {
		val study = DbLogic.createJsonObj<Study>("""{"id":-1}""")
		study.finishJSON("https://server.url", "accessKey")
		val dataSetSuccess = DataSet("questionnaire", study, "Success", -1L, -1L)
		dataSetSuccess.synced = DataSet.STATES.SYNCED
		
		val dataSetError = DataSet("questionnaire", study, "Unspecified error", -1L, -1L)
		dataSetError.synced = DataSet.STATES.NOT_SYNCED_ERROR
		
		val dataSetServerError = DataSet("questionnaire", study, "Server error", -1L, -1L)
		dataSetServerError.synced = DataSet.STATES.NOT_SYNCED_SERVER_ERROR
		
		val dataSetServerWaiting = DataSet("questionnaire", study, "Waiting", -1L, -1L)
		dataSetServerWaiting.synced = DataSet.STATES.NOT_SYNCED
		UploadProtocolView({ listOf(
			dataSetSuccess,
			dataSetError,
			dataSetServerError,
			dataSetServerWaiting
		) }, {}, {})
	}
}