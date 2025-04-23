package fr.ablanc.fileexchangeandroid.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreenRoot(
) {
    val baseViewModel: BaseViewModel = koinViewModel<BaseViewModel>()
    val state = baseViewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state.value)
}

@Composable
private fun HomeScreen(state: BaseState) {
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
            AlertDownloadDialog()
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

