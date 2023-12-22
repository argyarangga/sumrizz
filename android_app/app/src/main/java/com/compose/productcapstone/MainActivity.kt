package com.compose.productcapstone

import android.content.Intent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.window.Dialog
import com.compose.productcapstone.ui.theme.ProductCapstoneTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import com.compose.productcapstone.data.response.SummarizeResponse
import com.compose.productcapstone.data.response.SummarizeResult
import com.compose.productcapstone.data.retrofit.ApiConfig
import retrofit2.Response
import java.net.SocketTimeoutException

class MainActivity : androidx.activity.ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val options1 = listOf("text", "link")
    var selectedType by remember { mutableStateOf(options1.first()) }
    var isDialogVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        MultiSelector(
            options = options1,
            selectedOption = selectedType,
            onOptionSelect = { option ->
                selectedType = option
                Log.d("AppContent", "SelectedType changed: $selectedType")
            },
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxWidth()
                .height(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            label = { Text(text = "Input Text") },
            onValueChange = {
                text = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp)
                .widthIn(min = 280.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (options1.contains(selectedType)) {
                    keyboardController?.hide()
                    isDialogVisible = true
                } else {
                    //
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send")
        }

        if (isDialogVisible) {
            SendDialog(
                onDismissRequest = { isDialogVisible = false },
                onOptionClick = { selectedLanguage ->
                    coroutineScope.launch {
                        try {
                            Log.d("SelectedType", "Selected type: $selectedType")
                            val resultResponse = callAPI(selectedType, selectedLanguage, text.text)
                            context.startActivity(Intent(context, ResultActivity::class.java).apply {
                                putExtra("result", resultResponse)
                            })
                        } catch (e: Exception) {
                            e.printStackTrace()
                            isDialogVisible = false
                            text = TextFieldValue("")
                            selectedType = ""
                        }
                    }
                }
            )
        }
    }
}

suspend fun callAPI(
    type: String,
    language: String,
    source: String
): String? {
    return try {
        val apiService = ApiConfig.getApiService()
        val response: Response<SummarizeResult> = apiService.result(SummarizeResponse(type, language, source))
        if (response.isSuccessful) {
            response.body()?.body
        } else {
            val errorMessage = response.errorBody()?.string() ?: "Unknown error"
            Log.e("API", "Error response: $errorMessage")
            null
        }
    } catch (e: SocketTimeoutException) {
        Log.e("API", "Timeout during API call", e)
        null
    } catch (e: Exception) {
        Log.e("API", "Exception during API call", e)
        null
    }
}




@Composable
fun SendDialog(
    onDismissRequest: () -> Unit,
    onOptionClick: (selectedLanguage: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text("Choose the language", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                onOptionClick("en")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text("English")
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                onOptionClick("id")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text("Indonesia")
                    }
                }
            }
        }
    }
}

@Composable
fun MultiSelector(
    options: List<String>,
    selectedOption: String,
    onOptionSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedcolor: Color = MaterialTheme.colorScheme.onSurface,
    state: MultiSelectorState = rememberMultiSelectorState(
        options = options,
        selectedOption = selectedOption,
        selectedColor = selectedColor,
        unSelectedColor = unselectedcolor,
    ),
) {
    require(options.size >= 2) { "This composable requires at least 2 options" }
    require(options.contains(selectedOption)) { "Invalid selected option [$selectedOption]" }
    LaunchedEffect(key1 = options, key2 = selectedOption) {
        state.selectOption(this, options.indexOf(selectedOption))
    }
    Layout(
        modifier = modifier
            .clip(
                shape = RoundedCornerShape(percent = 50)
            )
            .background(MaterialTheme.colorScheme.surface),
        content = {
            val colors = state.textColors
            options.forEachIndexed { index, option ->
                Box(
                    modifier = Modifier
                        .layoutId(MultiSelectorOption.Option)
                        .clickable { onOptionSelect(option) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors[index],
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .layoutId(MultiSelectorOption.Background)
                    .clip(
                        shape = RoundedCornerShape(
                            topStartPercent = state.startCornerPercent,
                            bottomStartPercent = state.startCornerPercent,
                            topEndPercent = state.endCornerPercent,
                            bottomEndPercent = state.endCornerPercent,
                        )
                    )
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    ) { measurables, constraints ->
        val optionWidth = constraints.maxWidth / options.size
        val optionConstraints = Constraints.fixed(
            width = optionWidth,
            height = constraints.maxHeight,
        )
        val optionPlaceables = measurables
            .filter { measurable -> measurable.layoutId == MultiSelectorOption.Option }
            .map { measurable -> measurable.measure(optionConstraints) }
        val backgroundPlaceable = measurables
            .first { measurable -> measurable.layoutId == MultiSelectorOption.Background }
            .measure(optionConstraints)
        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight,
        ) {
            backgroundPlaceable.placeRelative(
                x = (state.selectedIndex * optionWidth).toInt(),
                y = 0,
            )
            optionPlaceables.forEachIndexed { index, placeable ->
                placeable.placeRelative(
                    x = optionWidth * index,
                    y = 0,
                )
            }
        }
    }
}

@Stable
interface MultiSelectorState {
    val selectedIndex: Float
    val startCornerPercent: Int
    val endCornerPercent: Int
    val textColors: List<Color>

    fun selectOption(scope: CoroutineScope, index: Int)
}

@Stable
class MultiSelectorStateImpl(
    options: List<String>,
    selectedOption: String,
    private val selectedColor: Color,
    private val unselectedColor: Color,
) : MultiSelectorState {

    override val selectedIndex: Float
        get() = _selectedIndex.value
    override val startCornerPercent: Int
        get() = _startCornerPercent.value.toInt()
    override val endCornerPercent: Int
        get() = _endCornerPercent.value.toInt()

    override val textColors: List<Color>
        get() = _textColors.value

    private var _selectedIndex =
        androidx.compose.animation.core.Animatable(options.indexOf(selectedOption).toFloat())
    private var _startCornerPercent = androidx.compose.animation.core.Animatable(
        if (options.first() == selectedOption) {
            50f
        } else {
            15f
        }
    )
    private var _endCornerPercent = androidx.compose.animation.core.Animatable(
        if (options.last() == selectedOption) {
            50f
        } else {
            15f
        }
    )

    private var _textColors: State<List<Color>> = derivedStateOf {
        List(numOptions) { index ->
            lerp(
                start = unselectedColor,
                stop = selectedColor,
                fraction = 1f - (((selectedIndex - index.toFloat()).absoluteValue).coerceAtMost(1f))
            )
        }
    }


    private val numOptions = options.size
    private val animationSpec = tween<Float>(
        durationMillis = 500,
        easing = FastOutSlowInEasing,
    )

    override fun selectOption(scope: CoroutineScope, index: Int) {
        scope.launch {
            _selectedIndex.animateTo(
                targetValue = index.toFloat(),
                animationSpec = animationSpec,
            )
        }
        scope.launch {
            _startCornerPercent.animateTo(
                targetValue = if (index == 0) 50f else 15f,
                animationSpec = animationSpec,
            )
        }
        scope.launch {
            _endCornerPercent.animateTo(
                targetValue = if (index == numOptions - 1) 50f else 15f,
                animationSpec = animationSpec,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MultiSelectorStateImpl

        if (selectedColor != other.selectedColor) return false
        if (unselectedColor != other.unselectedColor) return false
        if (_selectedIndex != other._selectedIndex) return false
        if (_startCornerPercent != other._startCornerPercent) return false
        if (_endCornerPercent != other._endCornerPercent) return false
        if (numOptions != other.numOptions) return false
        if (animationSpec != other.animationSpec) return false

        return true
    }

    override fun hashCode(): Int {
        var result = selectedColor.hashCode()
        result = 31 * result + unselectedColor.hashCode()
        result = 31 * result + _selectedIndex.hashCode()
        result = 31 * result + _startCornerPercent.hashCode()
        result = 31 * result + _endCornerPercent.hashCode()
        result = 31 * result + numOptions
        result = 31 * result + animationSpec.hashCode()
        return result
    }
}
@Composable
fun rememberMultiSelectorState(
    options: List<String>,
    selectedOption: String,
    selectedColor: Color,
    unSelectedColor: Color,
) = remember {
    MultiSelectorStateImpl(
        options,
        selectedOption,
        selectedColor,
        unSelectedColor,
    )
}

enum class MultiSelectorOption {
    Option,
    Background,
}

@Preview(showBackground = true)
@Composable
fun AppContentPreview() {
    ProductCapstoneTheme {
        AppContent()
    }
}