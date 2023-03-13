package at.jodlidev.esmira.views.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow

/**
 * Created by JodliDev on 21.02.2023.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopBar(
	title: String,
	scrollBehavior: TopAppBarScrollBehavior,
	goBack: (() -> Unit)? = null,
	actions: @Composable RowScope.() -> Unit = {}
) {
	
	TopAppBar(
		title = { Text(title, overflow = TextOverflow.Ellipsis, softWrap = false, maxLines = 1) },
		actions = actions,
		navigationIcon = {
			if(goBack != null) {
				IconButton(onClick = goBack) {
					Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
				}
			}
		},
		scrollBehavior = scrollBehavior
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultScaffoldView(
	title: String,
	goBack: (() -> Unit)?,
	actions: @Composable RowScope.() -> Unit = {},
	bottomBar: @Composable () -> Unit = {},
	content: @Composable () -> Unit
) {
	val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
	Scaffold(
		topBar = {
			DefaultTopBar(
				title = title,
				goBack = goBack,
				actions = actions,
				scrollBehavior = scrollBehavior
			)
		},
		bottomBar = bottomBar,
		modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
	) { innerPadding ->
		Box(modifier = Modifier.padding(innerPadding)) {
			content()
		}
	}
}