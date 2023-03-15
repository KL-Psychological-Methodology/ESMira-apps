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
import at.jodlidev.esmira.sharedCode.data_structure.UploadData
import at.jodlidev.esmira.views.ESMiraDialog

/**
 * Created by JodliDev on 24.02.2023.
 */

@Composable
fun UploadProtocolView(
	getUploadData: () -> List<UploadData>,
	reSyncDataSets: (() -> Unit) -> Unit,
	goBack: () -> Unit
) {
	val uploadDataToDelete = remember { mutableStateOf<UploadData?>(null) }
	val uploadDataSets = remember { mutableStateOf(getUploadData()) }
	if(uploadDataToDelete.value != null) {
		ESMiraDialog(
			onDismissRequest = { uploadDataToDelete.value = null },
			dismissButtonLabel = stringResource(R.string.cancel),
			confirmButtonLabel = stringResource(R.string.ok_),
			onConfirmRequest = {
				uploadDataToDelete.value?.delete()
				uploadDataSets.value = getUploadData()
				uploadDataToDelete.value = null
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
						uploadDataSets.value = getUploadData()
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
					Icon(imageVector = Icons.Default.Sync, contentDescription = "sync uploadData")
			}
		}
	) {
		LazyColumn(modifier = Modifier.fillMaxWidth()) {
			itemsIndexed(uploadDataSets.value, key = { _, uploadData -> uploadData.id }) { i, uploadData ->
				DataSetItemView(
					uploadData = uploadData,
					deleteUploadData = { uploadDataToDelete.value = uploadData },
					modifier = Modifier.background(color = if(i % 2 != 0) colorLineBackground1 else colorLineBackground2)
				)
			}
		}
	}
}

@Composable
fun DataSetItemView(uploadData: UploadData, deleteUploadData: () -> Unit, modifier: Modifier = Modifier) {
	Column(
		modifier = modifier
			.padding(horizontal = 30.dp, vertical = 20.dp)
			.fillMaxWidth()
	) {
		Text(
			NativeLink.formatDateTime(uploadData.timestamp),
			fontSize = MaterialTheme.typography.labelMedium.fontSize,
			modifier = Modifier.align(Alignment.End)
		)
		Row(verticalAlignment = Alignment.CenterVertically) {
			when(uploadData.synced) {
				UploadData.States.SYNCED ->
					Icon(Icons.Default.CheckCircle, "true", tint = colorGreen)
				UploadData.States.NOT_SYNCED ->
					Icon(Icons.Default.AccessTime, "true")
				UploadData.States.NOT_SYNCED_ERROR ->
					Icon(Icons.Default.Warning, "true")
				UploadData.States.NOT_SYNCED_ERROR_DELETABLE ->
					Icon(Icons.Default.Cancel, "true", tint = MaterialTheme.colorScheme.error)
				
			}
			Spacer(Modifier.size(ButtonDefaults.IconSpacing))
			Column {
				Text(uploadData.type.uppercase(), fontSize = MaterialTheme.typography.labelLarge.fontSize, fontWeight = FontWeight.Bold)
				if(uploadData.questionnaireName.isNotEmpty()) {
					Text(
						uploadData.questionnaireName,
						fontSize = MaterialTheme.typography.labelMedium.fontSize,
						modifier = Modifier.padding(start = 5.dp)
					)
				}
			}
			Spacer(Modifier.weight(1F))
			
			if(uploadData.synced == UploadData.States.NOT_SYNCED_ERROR_DELETABLE) {
				IconButton(onClick = deleteUploadData) {
					Icon(Icons.Default.Delete, "remove")
				}
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
		
		val dataSetSuccess = DataSet(DataSet.EventTypes.questionnaire, study, "Success", -1L, -1L)
		dataSetSuccess.synced = UploadData.States.SYNCED
		dataSetSuccess.id = 1
		
		val dataSetError = DataSet(DataSet.EventTypes.questionnaire, study, "Unspecified error", -1L, -1L)
		dataSetError.synced = UploadData.States.NOT_SYNCED_ERROR
		dataSetError.id = 2
		
		val dataSetServerError = DataSet(DataSet.EventTypes.questionnaire, study, "Server error", -1L, -1L)
		dataSetServerError.synced = UploadData.States.NOT_SYNCED_ERROR_DELETABLE
		dataSetServerError.id = 3
		
		val dataSetServerWaiting = DataSet(DataSet.EventTypes.questionnaire, study, "Waiting", -1L, -1L)
		dataSetServerWaiting.synced = UploadData.States.NOT_SYNCED
		dataSetServerWaiting.id = 4
		
		UploadProtocolView(
			{ listOf(
				dataSetServerError,
				dataSetServerError,
				dataSetServerError,
			) },
			{ listOf(
				dataSetSuccess,
				dataSetError,
				dataSetServerError,
				dataSetServerWaiting
			) },
			{}
		)
	}
}