package at.jodlidev.esmira.views.main

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.*
import at.jodlidev.esmira.R
import at.jodlidev.esmira.sharedCode.DbLogic
import at.jodlidev.esmira.sharedCode.data_structure.DbUser
import at.jodlidev.esmira.views.ESMiraDialog

/**
 * Created by JodliDev on 27.02.2023.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutView(goBack: () -> Unit) {
	val context = LocalContext.current
	val showPassword = remember { mutableStateOf(false) }
	val rememberAccessKey = remember { mutableStateOf("") }
	
	if(showPassword.value) {
		ESMiraDialog(
			dismissButtonLabel = stringResource(R.string.cancel),
			onDismissRequest = { showPassword.value = false },
			confirmButtonLabel = stringResource(R.string.ok_),
			onConfirmRequest = {
				if(DbUser.setDev(true, rememberAccessKey.value)) {
					Toast.makeText(context, R.string.info_dev_active, Toast.LENGTH_SHORT).show()
				}
				showPassword.value = false
			},
		
		) {
			OutlinedTextField(
				value = rememberAccessKey.value,
				onValueChange = {
					rememberAccessKey.value = it
				}
			)
		}
	}
	DefaultScaffoldView(
		title = stringResource(R.string.about_ESMira),
		goBack = goBack,
	) {
		Column(
			modifier = Modifier.padding(horizontal = 20.dp).verticalScroll(rememberScrollState())
		) {
			var clickCount = 0
			
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.padding(top = 20.dp).align(Alignment.CenterHorizontally)
			) {
				Image(
					painter = painterResource(id = R.drawable.ic_launcher_round),
					contentDescription = "",
					modifier = Modifier
						.size(50.dp)
						.clickable {
							if(++clickCount >= 10) {
								if(DbUser.isDev()) {
									DbUser.setDev(false)
									Toast.makeText(context, R.string.info_dev_inactive, Toast.LENGTH_SHORT).show()
								}
								else
									showPassword.value = true
							}
						}
				)
				
				Spacer(modifier = Modifier.width(10.dp))
				
				Column {
					Text(
						stringResource(R.string.app_name),
						fontSize = MaterialTheme.typography.headlineLarge.fontSize,
						fontWeight = FontWeight.Bold,
					)
					Text(
						stringResource(R.string.android_version, BuildConfig.VERSION_NAME, DbLogic.getVersion()),
						fontSize = MaterialTheme.typography.labelSmall.fontSize,
						modifier = Modifier.align(Alignment.CenterHorizontally)
					)
				}
			}
			Spacer(modifier = Modifier.height(20.dp))
			Text(stringResource(R.string.about_ESMira_description))
			
			Spacer(modifier = Modifier.height(10.dp))
			
			LibraryLine(stringResource(R.string.colon_homepage), "https://esmira.kl.ac.at")
			LibraryLine(stringResource(R.string.colon_github), "https://github.com/KL-Psychological-Methodology/ESMira")
			
			Divider(modifier = Modifier.padding(top = 30.dp))
			
			Text(
				stringResource(R.string.colon_used_libraries),
				fontSize = MaterialTheme.typography.titleLarge.fontSize,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
			)
			LibraryLine("Accompanist", "https://google.github.io/accompanist/")
			LibraryLine("Android-Debug-Database", "https://github.com/amitshekhariitbhu/Android-Debug-Database")
			LibraryLine("Charts", "https://github.com/danielgindi/Charts")
			LibraryLine("CodeScanner", "https://github.com/twostraws/CodeScanner")
			LibraryLine("Kotlin Multiplatform", "https://kotlinlang.org/lp/mobile/")
			LibraryLine("Kotlinx.serialization", "https://github.com/Kotlin/kotlinx.serialization")
			LibraryLine("Ktor", "https://ktor.io/")
			LibraryLine("Markwon", "https://noties.io/Markwon/")
			LibraryLine("MPAndroidChart", "https://github.com/PhilJay/MPAndroidChart")
			LibraryLine("URLImage", "https://github.com/dmytro-anokhin/url-image")
			LibraryLine("ZXing Android Embedded", "https://github.com/journeyapps/zxing-android-embedded")
		}
	}
}

@Composable
fun LibraryLine(name: String, url: String) {
	val uriHandler = LocalUriHandler.current
	Column(
		modifier = Modifier.clickable {
			uriHandler.openUri(url)
		}
	) {
		Text(name, fontWeight = FontWeight.Bold)
		Text(url,
			fontSize = MaterialTheme.typography.labelSmall.fontSize,
			modifier = Modifier
				.padding(start = 10.dp, top = 0.dp, bottom = 0.dp)
		)
	}
	Spacer(modifier = Modifier.height(20.dp))
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun PreviewAboutView() {
	ESMiraSurface {
		AboutView {}
	}
}