package fr.ablanc.fileexchangeandroid.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreenRoot(
) {
    val baseViewModel: BaseViewModel = koinViewModel<BaseViewModel>()
    val state = baseViewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state.value, onAction =  baseViewModel::onAction )
}

@Composable
private fun HomeScreen(state: BaseState, onAction: (onScreenAction) -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "${state.serverConnected}")
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
                shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White), modifier = Modifier.wrapContentSize()
            ) {
                IconButton(
                    onClick = { }, modifier = Modifier.size(200.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

        }
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            MediaPickerRoot(
                onDocumentSelected = {
                    println("mediaroot: $it")
                    onAction(onScreenAction.Connect(it))
                }
            )
        }
    }
}

@Composable
fun AlertDownloadDialog() {
    ListItem(
        modifier = Modifier.padding(8.dp).border(1.dp, Color.Black, RoundedCornerShape(16.dp)),
        headlineContent = { Text(text = "Download") },
        overlineContent = { Text(text = "Download Alert") },
        trailingContent = {
            CircularProgressIndicator()
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add"
            )

        }
    )
}

@Composable
fun MediaPickerRoot(    onDocumentSelected: (Uri) -> Unit = {}
){
    Column (modifier = Modifier.fillMaxWidth()
        .fillMaxHeight()
        .padding(16.dp)){
        PickImage(onDocumentSelected = {
            println("mediaPickerR: $it")
            onDocumentSelected(it)
        })
        PickMultipleImage()
        PickVideo(onDocumentSelected = {
            println("mediaPickerR: $it")
            onDocumentSelected(it)
        })
        PickDocument(onDocumentSelected = {
            println("mediaPickerR: $it")
            onDocumentSelected(it)
        })
    }
}

@Composable
fun PickImage(
    onDocumentSelected: (Uri) -> Unit = {}

){
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
     /*   result.value?.let { image ->
            //Use Coil to display the selected image
            val painter = rememberAsyncImagePainter(
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(data = image)
                    .build()
            )
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(150.dp, 150.dp)
                    .padding(16.dp)
            )
        }*/
    }
}


@Composable
fun PickMultipleImage(){
    val result = remember { mutableStateOf<List<Uri?>?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) {
        result.value = it
    }

    Column {
        Button(onClick = {
            launcher.launch(
                PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) {
            Text(text = "Select Multiple Image")
        }

        /*result.value?.let { images ->
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
                items(images){
                    //Use Coil to display the selected image
                    val painter = rememberAsyncImagePainter(
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(data = it)
                            .build()
                    )
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.size(150.dp, 150.dp)
                    )
                }
            }
        }*/
    }
}

@Composable
fun PickVideo(onDocumentSelected: (Uri) -> Unit = {}){
    val result = remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        result.value = it
    }

    Column {
        Button(onClick = {
            launcher.launch(
                PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        }) {
            Text(text = "Select Video")
        }

        result.value?.let { image ->
            onDocumentSelected(image)
        }
    }
}

@Composable
fun PickDocument(
    onDocumentSelected: (Uri) -> Unit = {}
){
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