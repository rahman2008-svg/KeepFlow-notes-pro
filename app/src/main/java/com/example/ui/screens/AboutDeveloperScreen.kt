package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDeveloperScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cardBgColor = if (isSystemInDarkTheme()) Color(0xFF25232A) else Color.White
    val cardBorderColor = if (isSystemInDarkTheme()) Color(0xFF49454F) else Color(0xFFE7E0EC)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("About Developer", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Hero Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Minimal decorative monogram avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(if (isSystemInDarkTheme()) Color(0xFF4F378B) else Color(0xFFEADDFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PR",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSystemInDarkTheme()) Color(0xFFEADDFF) else Color(0xFF21005D),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Prince AR Abdur Rahman",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Independent App Developer",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Core Biography card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Developer Bio",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Contacts Category
            item {
                Text(
                    text = "Contact & Social Circles",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // Contacts card (WhatsApp triggers copy actions, facebook and instagram initiate browser intent views)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Column {
                        // WhatsApp 1 Info
                        SocialChannelRow(
                            title = "WhatsApp Focus Line",
                            subtitle = "01707424006",
                            icon = Icons.Default.Phone,
                            onActionClick = {
                                copyToClipboard(context, "01707424006")
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = cardBorderColor
                        )

                        // WhatsApp 2 Info
                        SocialChannelRow(
                            title = "WhatsApp Priority Line",
                            subtitle = "01796951709",
                            icon = Icons.Default.Phone,
                            onActionClick = {
                                copyToClipboard(context, "01796951709")
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = cardBorderColor
                        )

                        // Facebook Link
                        SocialChannelRow(
                            title = "Facebook Account",
                            subtitle = "Abdur Rahman's Timeline",
                            icon = Icons.Default.Public,
                            onActionClick = {
                                triggerWebIntent(context, "https://www.facebook.com/share/1BNn32qoJo/")
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = cardBorderColor
                        )

                        // Instagram Link
                        SocialChannelRow(
                            title = "Instagram Profile",
                            subtitle = "ur___abdur____rahman__2008",
                            icon = Icons.Default.CameraAlt,
                            onActionClick = {
                                triggerWebIntent(context, "https://www.instagram.com/ur___abdur____rahman__2008")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SocialChannelRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onActionClick) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Contact Action Button",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Contact Number", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$text Copied to clipboard!", Toast.LENGTH_SHORT).show()
}

private fun triggerWebIntent(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Error launching browser link.", Toast.LENGTH_SHORT).show()
    }
}
