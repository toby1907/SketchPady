package com.example.sketchpady

import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sketchpady.data.activityChooser
import com.example.sketchpady.data.checkAndAskPermission
import com.example.sketchpady.data.saveImage
import com.example.sketchpady.ui.theme.HomeScreen
import com.example.sketchpady.ui.theme.Root
import com.example.sketchpady.ui.theme.SketchPadScreen
import com.example.sketchpady.ui.theme.SketchPadyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Root(window = window) {
                HomeScreen {
                    checkAndAskPermission {
                        CoroutineScope(Dispatchers.IO).launch {
                            val uri = saveImage(it)
                            withContext(Dispatchers.Main) {
                                startActivity(activityChooser(uri))
                            }
                        }
                    }
                }
            }
            /*SketchPadyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SketchPadScreen(modifier = Modifier.padding(innerPadding))
                }
            }*/
        }
    }
}

