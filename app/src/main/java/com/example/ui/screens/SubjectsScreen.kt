package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectEntity
import com.example.ui.components.GlassyCard
import com.example.ui.components.GlassyStatTile
import com.example.ui.components.bounceClick
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.PrimaryOrangeGradient
import com.example.ui.viewmodel.EceViewModel
import com.example.ui.viewmodel.SubjectUiModel

@Composable
fun SubjectsScreen(
    viewModel: EceViewModel,
    onNavigateToReviewer: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectModels by viewModel.subjectUiModels.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var subjectToDelete by remember { mutableStateOf<SubjectEntity?>(null) }

    val totalDecks = subjectModels.size
    val totalQuestions = subjectModels.sumOf { it.totalCount }
    val totalMastered = subjectModels.sumOf { it.masteredCount }
    val overallProgress = if (totalQuestions > 0) (totalMastered.toFloat() / totalQuestions * 100).toInt() else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // GLASSY HERO HEADER WITH ACTION
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x33FF6B00))
                                .border(1.dp, Color(0x66FF6B00), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Subject Decks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PRC Licensure Reviewer Notebooks",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA1A1AA)
                    )
                }

                // GLOWING ADD SUBJECT BUTTON
                Box(
                    modifier = Modifier
                        .bounceClick(scaleDown = 0.94f) { showCreateDialog = true }
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp), spotColor = Color(0x99FF6B00))
                        .clip(RoundedCornerShape(14.dp))
                        .background(PrimaryOrangeGradient)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Deck", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // 3-COLUMN GLASSY STAT TILES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassyStatTile(
                title = "Decks",
                value = "$totalDecks",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                accentColor = OrangePrimary,
                modifier = Modifier.weight(1f)
            )
            GlassyStatTile(
                title = "Total Cards",
                value = "$totalQuestions",
                icon = Icons.Default.Style,
                accentColor = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
            GlassyStatTile(
                title = "Mastery",
                value = "$overallProgress%",
                icon = Icons.Default.CheckCircle,
                accentColor = EmeraldTertiary,
                modifier = Modifier.weight(1f)
            )
        }

        // SUBJECT LIST
        if (subjectModels.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0x66FF6B00),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No Subjects Created Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create First Deck")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subjectModels, key = { it.subject.id }) { item ->
                    SubjectGlassyCard(
                        model = item,
                        onOpenReviewer = {
                            viewModel.selectSubject(item.subject.id)
                            onNavigateToReviewer(item.subject.id)
                        },
                        onDeleteRequest = {
                            subjectToDelete = item.subject
                        }
                    )
                }
            }
        }
    }

    // CREATE SUBJECT DIALOG
    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf("#FF6B00") }

        val palette = listOf("#FF6B00", "#F59E0B", "#10B981", "#7C3AED", "#EF4444", "#06B6D4")

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = Color(0xFF141318),
            title = { Text("Create Subject Notebook", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Subject Name (e.g. Signals & Systems)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("dialog_subject_name")
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Short Code (e.g. SIG-1)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("dialog_subject_code")
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description / Scope") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Accent Color:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA1A1AA)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        palette.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selectedColor == hex) 3.dp else 1.dp,
                                        color = if (selectedColor == hex) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && code.isNotBlank()) {
                            viewModel.createNewSubject(name, code, desc, selectedColor)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    enabled = name.isNotBlank() && code.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("dialog_confirm_create_subject")
                ) {
                    Text("Create Deck", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color(0xFFA1A1AA))
                }
            }
        )
    }

    // CONFIRM DELETE DIALOG
    subjectToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            containerColor = Color(0xFF141318),
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Delete '${target.name}'?", color = Color.White) },
            text = {
                Text(
                    "Are you sure you want to delete this subject notebook? All associated flashcards, step derivations, formulas, and calculator shortcuts will be permanently removed.",
                    color = Color(0xFFD4D4D8)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubject(target)
                        subjectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_delete_subject_button")
                ) {
                    Text("Delete Notebook")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) {
                    Text("Cancel", color = Color(0xFFA1A1AA))
                }
            }
        )
    }
}

@Composable
private fun SubjectGlassyCard(
    model: SubjectUiModel,
    onOpenReviewer: () -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sub = model.subject
    val accentColor = try {
        Color(android.graphics.Color.parseColor(sub.colorHex))
    } catch (e: Exception) {
        OrangePrimary
    }

    val animatedProgress by animateFloatAsState(
        targetValue = model.progress,
        animationSpec = tween(500),
        label = "sub_progress"
    )

    GlassyCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        onClick = onOpenReviewer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    // Glowing Code Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = sub.code,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = sub.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                }

                // Delete Icon
                IconButton(
                    onClick = onDeleteRequest,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("delete_subject_${sub.code}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Subject",
                        tint = Color(0xFF71717A),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (sub.description.isNotEmpty()) {
                Text(
                    text = sub.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1A1AA)
                )
            }

            // Stats & Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${model.totalCount} Flashcards",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                    Text(
                        text = "${model.masteredCount} Mastered (${(model.progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (model.progress >= 0.8f) EmeraldTertiary else Color(0xFFA1A1AA)
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (model.progress >= 0.8f) EmeraldTertiary else accentColor,
                    trackColor = Color(0x3327272A)
                )
            }

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .bounceClick(scaleDown = 0.95f) { onOpenReviewer() }
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x33FF6B00))
                        .border(1.dp, Color(0x66FF6B00), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Study Deck", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OrangePrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
