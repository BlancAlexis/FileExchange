package fr.ablanc.fileexchangeandroid.presentation.component

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.ablanc.fileexchangeandroid.domain.FrameContent
import fr.ablanc.fileexchangeandroid.presentation.BaseState
import fr.ablanc.fileexchangeandroid.presentation.BaseViewModel
import fr.ablanc.fileexchangeandroid.presentation.OnScreenAction
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreenRoot(
) {
    val context = LocalContext.current
    val baseViewModel: BaseViewModel = koinViewModel<BaseViewModel>()
    val state = baseViewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state.value, onAction = baseViewModel::onAction)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(state.value.toastMessage) {
        val message = state.value.toastMessage
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            baseViewModel.onAction(OnScreenAction.ClearToastMessage)
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    baseViewModel.socketDisconnection()
                }

                Lifecycle.Event.ON_START -> {
                    baseViewModel.socketConnect()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    baseViewModel.clearCache()
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }


    }
}

@Composable
private fun HomeScreen(state: BaseState, onAction: (OnScreenAction) -> Unit = {}) {
    var showDialogDocumentPicker by remember { mutableStateOf(false) }
    if (state.showCachedFilesDialog) {
        Dialog(onDismissRequest = { onAction(OnScreenAction.OnCloseCachedFilesDialog) }) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column {
                    if (!state.persistedResourceNames.isNullOrEmpty()) {
                        state.persistedResourceNames.forEach { name ->
                            Text(
                                text = name,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        onAction(OnScreenAction.OnCachedFileClicked(name))
                                    }
                            )

                        }
                    } else {
                        Text("Aucun fichier n'est disponible")
                    }
                }
            }
        }
    }




    if (state.showDocumentDialog) {
        Dialog(
            onDismissRequest = { onAction(OnScreenAction.OnCloseDocumentVisualization) },
            properties = DialogProperties(
                dismissOnBackPress = true, dismissOnClickOutside = false
            )
        ) {
            DocumentMenu(
                state = state,
                onAction = onAction
            ) {
                when (state.uiFile?.content) {
                    is FrameContent.Image -> state.uiFile.content.let {
                        Image(
                            bitmap = it.value.asImageBitmap(),
                            contentDescription = "Image",
                            modifier = Modifier.wrapContentSize()
                        )
                    }

                    is FrameContent.PDF -> {

                    }

                    null -> {
                        Text("erreur")
                    }
                }

            }

        }
    }
    if (showDialogDocumentPicker) {
        Dialog(
            properties = DialogProperties(
                dismissOnBackPress = true, dismissOnClickOutside = true
            ), onDismissRequest = {
                showDialogDocumentPicker = false
                onAction(OnScreenAction.OnCloseDocumentLoading)
            }) {
            Card {
                MediaPickerRoot(onDocumentSelected = {
                    showDialogDocumentPicker = false
                    onAction(OnScreenAction.OnDocumentSelected(it))
                })
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(
                modifier = Modifier.size(56.dp),
                onClick = { onAction(OnScreenAction.OnTriggerViewSaveButton) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "Add",
                )
            }
            Switch(
                checked = state.isServerConnected,
                onCheckedChange = {},
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    uncheckedThumbColor = Color.White,
                    checkedTrackColor = Color.Green,
                    uncheckedTrackColor = Color.Red
                ),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(Color.White),
                modifier = Modifier.wrapContentSize()
            ) {
                IconButton(
                    enabled = state.isServerConnected,
                    onClick = {
                        showDialogDocumentPicker = true
                    }, modifier = Modifier.size(200.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

        }
        if (state.isLoadingResource) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                AlertDownloadDialog(
                    state = state,
                    onButtonClick = {
                        onAction(OnScreenAction.OnCloseDocumentLoading)
                    })
            }
        }
    }
}

@Composable
private fun DocumentMenu(
    onAction: (OnScreenAction) -> Unit = {}, state: BaseState, content: @Composable () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.wrapContentSize()
    ) {
        content()
        Box(
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            IconButton(
                onClick = {
                    expanded = true
                }, modifier = Modifier
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Add",
                    modifier = Modifier,
                    tint = Color.White
                )
            }
            DocumentSubMenu(
                expanded = expanded,
                onClose = {
                    expanded = false
                    onAction(OnScreenAction.OnCloseDocumentVisualization)
                },
                onAction = onAction,
                showSave = !state.isViewingCachedFile
            )

        }

    }
}

@Composable
fun DocumentSubMenu(
    expanded: Boolean,
    onClose: () -> Unit = {},
    onAction: (OnScreenAction) -> Unit = {},
    showSave: Boolean = true,
) {
    DropdownMenu(
        expanded = expanded, onDismissRequest = { onClose() }) {
        if (showSave) {
            DropdownMenuItem(text = { Text("Sauvegarder") }, onClick = {
                onAction(OnScreenAction.OnTriggerSaveDocumentButton)
                onClose()
            })
        }
        DropdownMenuItem(text = { Text("Supprimer") }, onClick = {
            onAction(OnScreenAction.OnTriggerDeleteDocumentButton)
            onClose()
        })
    }
}

@Composable
fun AlertDownloadDialog(
    onButtonClick: () -> Unit = {}, state: BaseState
) {
    ListItem(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(16.dp)),
        headlineContent = { Text(text = "File") },
        overlineContent = { Text(text = "Download Alert") },
        trailingContent = {
            Button(onClick = { onButtonClick() }, enabled = state.isResourceLoaded) {
                Text(text = "Consulter")
            }
        },
        leadingContent = {
            if (!state.isResourceLoaded){
                CircularProgressIndicator()
            }
            else{
             Icon(imageVector = Icons.Filled.Check, contentDescription = "done", tint = Color.Green)
            }
        })
}

