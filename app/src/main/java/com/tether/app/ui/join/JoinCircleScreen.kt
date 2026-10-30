package com.tether.app.ui.join

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tether.app.TetherApp
import com.tether.app.ui.components.CodeBoxes
import com.tether.app.ui.components.PrimaryButton
import com.tether.app.ui.theme.TetherBg
import com.tether.app.ui.theme.TetherCoral
import com.tether.app.ui.theme.TextPrimary
import com.tether.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinCircleScreen(
    onBackClick: () -> Unit,
    onJoined: (String) -> Unit
) {
    val app = LocalContext.current.applicationContext as TetherApp
    val viewModel: JoinCircleViewModel = viewModel(
        factory = JoinCircleViewModel.provideFactory(app.container.repository)
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = TetherBg,
        topBar = {
            TopAppBar(
                title = { Text("Join circle", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TetherBg)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                PrimaryButton(
                    text = "Join circle",
                    onClick = { viewModel.joinCircle(onJoined) },
                    enabled = state.isComplete,
                    isLoading = state.isLoading
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter invite code",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 28.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ask the circle owner for their 6-character code to join their boundary.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Transparent TextField driving the 6 custom visual boxes
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CodeBoxes(
                    code = state.code,
                    isError = state.errorMessage != null
                )

                BasicTextField(
                    value = state.code,
                    onValueChange = viewModel::onCodeChange,
                    modifier = Modifier.matchParentSize(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    decorationBox = { /* Invisible input overlay */ }
                )
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.errorMessage ?: "",
                    color = TetherCoral,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}