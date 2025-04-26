package fr.ablanc.fileexchangeandroid.presentation.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


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

