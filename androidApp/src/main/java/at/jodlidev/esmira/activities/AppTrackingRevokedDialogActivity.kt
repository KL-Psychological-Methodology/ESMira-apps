package at.jodlidev.esmira.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.jodlidev.esmira.ESMiraSurface
import at.jodlidev.esmira.R
import at.jodlidev.esmira.views.DefaultButton
import at.jodlidev.esmira.views.ESMiraDialogContent


/**
 * Created by SelinaDev on 10.01.2024.
 */
class AppTrackingRevokedDialogActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainDialog(true)
        }
    }

    @Composable
    fun MainDialog(
        withHeader: Boolean
    ) {
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                finish()
            }

        ESMiraDialogContent(
            confirmButtonLabel = stringResource(R.string.close),
            onConfirmRequest = { finish() },
        ) {
            LazyColumn {
                if (withHeader) {
                    item {
                        Text(
                            stringResource(R.string.error_app_usage_revoked_android_header),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.error_app_usage_revoked_android_desc))
                            Spacer(modifier = Modifier.width(10.dp))
                            DefaultButton(stringResource(R.string.open_settings), onClick = {
                                launcher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            })
                        }
                    }
                }
            }
        }
    }

    @Preview
    @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
    @Composable
    fun PreviewMainDialogWithHeader() {
        ESMiraSurface {
            MainDialog(true)
        }
    }

    @Preview
    @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
    @Composable
    fun PreviewMainDialogWithoutHeader() {
        ESMiraSurface {
            MainDialog(false)
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AppTrackingRevokedDialogActivity::class.java)
            if(context !is Activity)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }
}
