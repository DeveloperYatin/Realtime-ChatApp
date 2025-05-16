package com.dev.yatin.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.dev.yatin.chatapp.presentation.viewmodel.ChatViewModel
import com.dev.yatin.chatapp.presentation.ui.ChatScreen
import com.dev.yatin.chatapp.util.ConnectivityObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChatScreen(viewModel) }

        lifecycleScope.launch {
            ConnectivityObserver.observe(this@MainActivity).collectLatest { isConnected ->
                viewModel.setConnectivity(isConnected)
            }
        }
    }
}
