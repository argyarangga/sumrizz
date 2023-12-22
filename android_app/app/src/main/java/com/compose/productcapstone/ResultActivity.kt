package com.compose.productcapstone

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class ResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val resultText = intent.getStringExtra("result") ?: ""
            ResultContent(resultText)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultContent(resultText: String) {
    val context = LocalContext.current
    val result by rememberUpdatedState(newValue = resultText)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = result,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp)
                .widthIn(min = 280.dp),
            readOnly = true,
            enabled = false,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = LocalContentColor.current,
                disabledTextColor = LocalContentColor.current,
                cursorColor = Color.Transparent,
                focusedBorderColor = LocalContentColor.current.copy(alpha = 1f),
                unfocusedBorderColor = LocalContentColor.current.copy(alpha = 1f),
                disabledBorderColor = LocalContentColor.current.copy(alpha = 1f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                context.startActivity(Intent(context, MainActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}