package fr.ablanc.fileexchangeandroid.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreenRoot(
) {
    val baseViewModel: BaseViewModel = koinViewModel<BaseViewModel>()
    val state = baseViewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state.value, onAction = baseViewModel::onAction)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {/*                Lifecycle.Event.ON_STOP -> {
                                    println("on stop")
                                    baseViewModel.socketConnect()
                                }
                                Lifecycle.Event.ON_START -> {
                                    println("on start")
                                    baseViewModel.socketConnect()
                                }*/
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
    var showImageDialog by remember { mutableStateOf(false) }
    var showDialogDocumentPicker by remember { mutableStateOf(false) }
    if (showImageDialog) {
        Dialog(
            onDismissRequest = { showImageDialog = false }, properties = DialogProperties(
                dismissOnBackPress = true, dismissOnClickOutside = false
            )
        ) {
            DocumentMenu {
                state.image?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Image",
                        modifier = Modifier.wrapContentSize()
                    )
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
                onAction(OnScreenAction.endLoading)
            }) {
            Card {
                MediaPickerRoot(onDocumentSelected = {
                    onAction(OnScreenAction.DocumentSelected(it))
                })
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(
            modifier = Modifier.size(56.dp),
            onClick = { /*TODO*/ },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Red,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "Add"
            )
        }
            Switch(
                checked = state.serverConnected,
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
        if (state.onDocumentLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                AlertDownloadDialog(
                    onButtonClick = {
                        showImageDialog = true
                    })
            }
        }
    }
}

@Composable
private fun DocumentMenu(
    content: @Composable () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .background(Color.Red)
    ) {
        content()
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
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

            DropdownMenu(
                expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("Sauvegarder") }, onClick = {
                    expanded = false

                })
                DropdownMenuItem(text = { Text("Supprimer") }, onClick = {
                    expanded = false
                })
            }
        }

    }
}

@Composable
fun AlertDownloadDialog(
    onButtonClick: () -> Unit = {}, onCloses: () -> Unit = {}
) {
    ListItem(
        modifier = Modifier
            .padding(8.dp)
            .border(1.dp, Color.Black, RoundedCornerShape(16.dp)),
        headlineContent = { Text(text = "Download") },
        overlineContent = { Text(text = "Download Alert") },
        trailingContent = {
            Button(onClick = { onButtonClick() }) {
                Text(text = "Consulter")
            }
        },
        leadingContent = {
            CircularProgressIndicator()
        })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaPickerRoot(
    onDocumentSelected: (Uri) -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Media",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        FlowRow(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PickImage(onDocumentSelected = {
                onDocumentSelected(it)
            })
            PickDocument(onDocumentSelected = {
                onDocumentSelected(it)
            })
        }
    }

}

@Composable
fun PickImage(
    onDocumentSelected: (Uri) -> Unit = {}
) {
    val result = remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        result.value = it
    }

    Column {
        Button(onClick = {
            launcher.launch(
                PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text(text = "Select Image")
        }
        result.value?.let { image ->
            onDocumentSelected(image)
        }
    }
}


@Composable
fun PickDocument(
    onDocumentSelected: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val result = remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        result.value = it
    }

    Column {
        Button(onClick = {
            launcher.launch(arrayOf("application/pdf"))
        }) {
            Text(text = "Select Document")
        }
        result.value?.let { image ->
            onDocumentSelected(image)

            context.contentResolver.openInputStream(image)?.readBytes()
        }
    }
}
