package com.example.sketchpady.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sketchpady.data.convertToOldColor
import io.ak1.drawbox.DrawBox
import io.ak1.drawbox.rememberDrawController
import io.ak1.rangvikalp.RangVikalp
import io.ak1.rangvikalp.defaultSelectedColor

@Composable
fun SketchPadScreen(modifier: Modifier) {
    val lines = remember { mutableStateListOf<Line>() }
    val selectedLines = remember { mutableStateListOf<Int>() }
    var isErasing by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(true){
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        if (!isErasing) {
                            val line = Line(
                                start = change.position - dragAmount,
                                end = change.position
                            )
                            lines.add(line)
                        } else {
                            // Erasing logic: Remove lines near the touch position
                            val eraserRadius = 20f // Adjust as needed
                            lines.removeAll { line ->
                                (change.position - line.start).getDistance() < eraserRadius ||
                                        (change.position- line.end).getDistance() < eraserRadius
                            }
                        }
                    }
                }
                .pointerInput(true) {
                    detectTapGestures(onLongPress = { offset ->
                        if (!isErasing) {
                            val index = lines.indexOfFirst { line ->
                                (offset - line.start).getDistance() < 50f ||
                                        (offset - line.end).getDistance() < 50f
                            }
                            if (index != -1) {
                                if (index in selectedLines) {
                                    selectedLines.remove(index)
                                } else {
                                    selectedLines.add(index)
                                }
                            }
                        }
                    })
                }
        ) {
            lines.forEachIndexed { index, line ->
                val color = if (index in selectedLines) Color.Red else line.color
                drawLine(
                    color = color,
                    start = line.start,
                    end = line.end,
                    strokeWidth = line.strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        //Buttons for erasing and deleting
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            Button(
                onClick = { isErasing = !isErasing },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isErasing) Color.Red else Color.LightGray
                )
            ) {
                Text(if (isErasing) "Done Erasing" else "Erase")
            }

            Spacer(modifier = Modifier.height(8.dp)) // Add some space between buttons

            Button(
                onClick = {
                    selectedLines.sortedDescending().forEach { index ->
                        lines.removeAt(index)
                    }
                    selectedLines.clear()
                },
                enabled = !isErasing // Disable delete button while erasing
            ) {
                Text("Delete Lines")
            }
        }
    }
}

// ... (rest of the code remains the same)




data class Line(
    val start: Offset,
    val end: Offset,
    val color: Color = Color.Black,
    val strokeWidth: Dp = 1.dp
)

@Composable
fun HomeScreen(save: (Bitmap) -> Unit) {
    val undoVisibility = remember { mutableStateOf(false) }
    val redoVisibility = remember { mutableStateOf(false) }
    val colorBarVisibility = remember { mutableStateOf(false) }
    val sizeBarVisibility = remember { mutableStateOf(false) }
    val currentColor = remember { mutableStateOf(defaultSelectedColor) }
    val bg = MaterialTheme.colorScheme.background
    val currentBgColor = remember { mutableStateOf(bg) }
    val currentSize = remember { mutableStateOf(10) }
    val colorIsBg = remember { mutableStateOf(false) }
    val drawController = rememberDrawController()

    Box {
        Column {
            DrawBox(
                drawController = drawController,
                backgroundColor = currentBgColor.value,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f, fill = false),
                bitmapCallback = { imageBitmap, error ->
                    imageBitmap?.let {
                        save(it.asAndroidBitmap())
                    }
                }
            ) { undoCount, redoCount ->
                sizeBarVisibility.value = false
                colorBarVisibility.value = false
                undoVisibility.value = undoCount != 0
                redoVisibility.value = redoCount != 0
            }

            ControlsBar(
                drawController = drawController,
                {
                    drawController.saveBitmap()
                },
                {
                    colorBarVisibility.value = when (colorBarVisibility.value) {
                        false -> true
                        colorIsBg.value -> true
                        else -> false
                    }
                    colorIsBg.value = false
                    sizeBarVisibility.value = false
                },
                {
                    colorBarVisibility.value = when (colorBarVisibility.value) {
                        false -> true
                        !colorIsBg.value -> true
                        else -> false
                    }
                    colorIsBg.value = true
                    sizeBarVisibility.value = false
                },
                {
                    sizeBarVisibility.value = !sizeBarVisibility.value
                    colorBarVisibility.value = false
                },
                undoVisibility = undoVisibility,
                redoVisibility = redoVisibility,
                colorValue = currentColor,
                bgColorValue = currentBgColor,
                sizeValue = currentSize
            )
            RangVikalp(isVisible = colorBarVisibility.value, showShades = true) {

                if (colorIsBg.value) {
                    currentBgColor.value = it
                    drawController.changeBgColor(it)
                } else {
                    currentColor.value = it
                    drawController.changeColor(it)
                }
            }
            CustomSeekbar(
                isVisible = sizeBarVisibility.value,
                progress = currentSize.value,
                progressColor = MaterialTheme.colorScheme.primary.convertToOldColor(),
                thumbColor = currentColor.value.convertToOldColor()
            ) {
                currentSize.value = it
                drawController.changeStrokeWidth(it.toFloat())
            }
        }

    }
}