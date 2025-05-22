package com.example.cinechord.presentation.screens.watchroom.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cinechord.domain.models.Message
import com.example.cinechord.domain.models.User
import kotlinx.coroutines.delay

/**
 * Component for handling chat in landscape mode
 * Shows a floating notification for incoming messages
 * and controls for toggling the chat panel
 */
@Composable
fun LandscapeChat(
    messages: List<Message>,
    currentUser: User?,
    isMessageLoading: Boolean = false,
    showChatPanel: Boolean = false,
    onToggleChatPanel: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Right side controls for chat
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Toggle chat panel button
            FloatingActionButton(
                onClick = onToggleChatPanel,
                modifier = Modifier.size(50.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (showChatPanel) Icons.Default.Close else Icons.Default.KeyboardArrowLeft,
                    contentDescription = if (showChatPanel) "Close Chat" else "Open Chat"
                )
            }

            // Toggle quick message input button
            var showQuickMessageInput by remember { mutableStateOf(false) }
            FloatingActionButton(
                onClick = { showQuickMessageInput = !showQuickMessageInput },
                modifier = Modifier.size(50.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Quick Message"
                )
            }

            // Quick message input
            AnimatedVisibility(
                visible = showQuickMessageInput,
                enter = fadeIn() + slideInHorizontally { it },
                exit = fadeOut() + slideOutHorizontally { it }
            ) {
                var messageText by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Type...") },
                        modifier = Modifier.width(200.dp),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(messageText)
                                messageText = ""
                                showQuickMessageInput = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }

        // Chat panel (right side overlay)
        AnimatedVisibility(
            visible = showChatPanel,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .align(Alignment.CenterEnd),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 8.dp
            ) {
                ChatSection(
                    messages = messages,
                    currentUser = currentUser,
                    isMessageLoading = isMessageLoading,
                    onSendMessage = onSendMessage
                )
            }
        }

        // Floating message notification
        val latestMessage = messages.lastOrNull()
        var showMessageNotification by remember { mutableStateOf(false) }
        var lastMessageId by remember { mutableStateOf("") }
        
        // Show notification for new messages when not in chat panel
        LaunchedEffect(latestMessage) {
            if (latestMessage != null && 
                latestMessage.id != lastMessageId && 
                latestMessage.senderId != currentUser?.id && 
                !showChatPanel) {
                
                lastMessageId = latestMessage.id
                showMessageNotification = true
                delay(3000) // Show for 3 seconds
                showMessageNotification = false
            }
        }
        
        AnimatedVisibility(
            visible = showMessageNotification,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
            exit = fadeOut()
        ) {
            latestMessage?.let { message ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 80.dp)
                        .width(250.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.senderProfileImage.isNotBlank()) {
                            AsyncImage(
                                model = message.senderProfileImage,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = message.senderName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = message.content,
                                fontSize = 14.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
} 