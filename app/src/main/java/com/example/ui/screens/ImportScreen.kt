package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassyCard
import com.example.ui.components.MathTextView
import com.example.ui.components.bounceClick
import com.example.ui.theme.AmberSecondaryDark
import com.example.ui.theme.DisabledButtonBrush
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.PrimaryOrangeGradient
import com.example.ui.viewmodel.EceViewModel
import com.example.util.AiPromptTemplate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    viewModel: EceViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val subjects by viewModel.subjects.collectAsState()
    val allBatches by viewModel.allImportBatches.collectAsState()
    val selectedImportSubId by viewModel.selectedImportSubjectId.collectAsState()
    val importText by viewModel.importText.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val parsePreview by viewModel.parsePreview.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val lastSummary by viewModel.lastImportSummary.collectAsState()

    var showNewSubjectDialog by remember { mutableStateOf(false) }
    var isPromptExpanded by remember { mutableStateOf(false) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var showImportSuccessDialog by remember { mutableStateOf(false) }
    var showClearDemoDialog by remember { mutableStateOf(false) }
    var selectedClearScope by remember { mutableStateOf("ALL") }

    val demoStats by viewModel.demoStats.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshDemoStats()
    }

    val currentImportSubject = subjects.find { it.id == selectedImportSubId } ?: subjects.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SCREEN TITLE (GLASSY HERO HEADER)
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FF6B00))
                        .border(1.dp, Color(0x66FF6B00), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Ingest Review Notes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Deterministic offline parser with undo/redo & SHA-256 deduplication",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA1A1AA)
                    )
                }
            }
        }

        // PAST IMPORTS / DELETER CALLOUT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick { onNavigateToHistory() }
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x221E293B))
                .border(1.dp, Color(0x44FB923C), RoundedCornerShape(16.dp))
                .padding(14.dp)
                .testTag("open_import_history_button")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x33FB923C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Import History & Batch Deleter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (allBatches.isNotEmpty())
                                "${allBatches.size} logged sessions • Inspect note transcripts or delete past inputs"
                            else "View past transcripts or delete previous inputs without touching whole subject",
                            fontSize = 11.sp,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open history",
                    tint = OrangePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // DEMO & TEST DATA MANAGEMENT CARD
        GlassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("demo_cleanup_card"),
            shape = RoundedCornerShape(16.dp),
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x28F87171))
                                .border(1.dp, Color(0x55F87171), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Demo & Test Data Cleaner",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                if (demoStats.hasAnyDemo) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0x33F87171))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            color = Color(0xFFF87171),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (demoStats.hasAnyDemo)
                                    "${demoStats.demoQuestionsCount} Questions • ${demoStats.demoFormulasCount} Formulas • ${demoStats.demoSubjectsCount} Subjects"
                                else
                                    "No starter demo data detected (Clean environment)",
                                fontSize = 11.sp,
                                color = if (demoStats.hasAnyDemo) Color(0xFFFCA5A5) else Color(0xFFA1A1AA)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .bounceClick(scaleDown = 0.94f) {
                                selectedClearScope = "ALL"
                                showClearDemoDialog = true
                            }
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x22F87171))
                            .border(1.dp, Color(0x66F87171), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("open_clear_demo_dialog_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clear / Delete",
                                color = Color(0xFFF87171),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Quick action row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quick Button: Reviewer Questions
                    OutlinedButton(
                        onClick = {
                            selectedClearScope = "REVIEWER"
                            showClearDemoDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .testTag("quick_delete_demo_reviewer_button"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33F87171))
                    ) {
                        Text(
                            text = "Reviewer (${demoStats.demoQuestionsCount})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    // Quick Button: Formulas
                    OutlinedButton(
                        onClick = {
                            selectedClearScope = "FORMULAS"
                            showClearDemoDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .testTag("quick_delete_demo_formulas_button"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33F87171))
                    ) {
                        Text(
                            text = "Formulas (${demoStats.demoFormulasCount})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }

                    // Quick Button: Subjects
                    OutlinedButton(
                        onClick = {
                            selectedClearScope = "SUBJECTS"
                            showClearDemoDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .testTag("quick_delete_demo_subjects_button"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33F87171))
                    ) {
                        Text(
                            text = "Subjects (${demoStats.demoSubjectsCount})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // STEP 1: TARGET SUBJECT NOTEBOOK
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = 3.dp
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(OrangePrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TARGET SUBJECT DECK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = OrangePrimary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = subjectDropdownExpanded,
                        onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = currentImportSubject?.let { "${it.code} • ${it.name}" } ?: "Select Subject",
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            maxLines = 1,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp
                            ),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("subject_dropdown"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xCC16151E),
                                unfocusedContainerColor = Color(0x99131218),
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color(0x40FF7A00),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false },
                            modifier = Modifier.background(Color(0xFF16151E))
                        ) {
                            subjects.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text("${sub.code} - ${sub.name}", color = Color.White, maxLines = 1) },
                                    onClick = {
                                        viewModel.setSelectedImportSubject(sub.id)
                                        subjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Button to create a new subject (aligned 52.dp height)
                    OutlinedButton(
                        onClick = { showNewSubjectDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .bounceClick(scaleDown = 0.94f)
                            .testTag("create_subject_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x18FB923C),
                            contentColor = OrangePrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x40FF7A00)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // STEP 2: AI PROMPT HELPER CARD (ONE-TAP COPY)
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "AI Helper Prompt",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Copy prompt button with gradient glow
                    Box(
                        modifier = Modifier
                            .bounceClick(scaleDown = 0.94f) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("ECE AI Prompt", AiPromptTemplate.PROMPT_TEXT))
                                scope.launch {
                                    snackbarHostState.showSnackbar("AI Prompt Template copied to clipboard!")
                                }
                            }
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(10.dp), spotColor = Color(0x80FF6B00))
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryOrangeGradient)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("copy_prompt_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Copy Prompt", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                    }
                }

                Text(
                    text = "Copies test bank questions 1:1 verbatim, eliminates duplicate questions, and skips solutions/formulas/shortcuts on pure concept questions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA1A1AA)
                )

                // Feature Highlights Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x22FB923C))
                            .border(1.dp, Color(0x44FB923C), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("1:1 Questions", color = OrangePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x2210B981))
                            .border(1.dp, Color(0x4410B981), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Zero Duplicates", color = EmeraldTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x228B5CF6))
                            .border(1.dp, Color(0x448B5CF6), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("No SOL on Concepts", color = Color(0xFFA78BFA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Expandable preview of the full prompt
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isPromptExpanded = !isPromptExpanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isPromptExpanded) "Hide prompt text" else "View prompt instructions",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (isPromptExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = OrangePrimary
                    )
                }

                AnimatedVisibility(visible = isPromptExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F0E13))
                            .border(1.dp, Color(0x33FF7A00), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = AiPromptTemplate.SHORT_INSTRUCTIONS,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFE4E4E7)
                        )
                    }
                }
            }
        }

        // STEP 3: TEXT IMPORT FIELD WITH UNDO / REDO CONTROLS
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Toolbar with Undo, Redo, Clear, and Load Sample
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(OrangePrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PASTE RAW REVIEW TEXT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = OrangePrimary
                        )
                    }

                    // UNDO / REDO ACTION ROW WITH TACTILE BOUNCE
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // UNDO BUTTON
                        IconButton(
                            onClick = { viewModel.undoImportText() },
                            enabled = canUndo,
                            modifier = Modifier
                                .size(36.dp)
                                .bounceClick(scaleDown = 0.9f)
                                .testTag("undo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo Text Change",
                                tint = if (canUndo) OrangePrimary else Color(0x4071717A),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // REDO BUTTON
                        IconButton(
                            onClick = { viewModel.redoImportText() },
                            enabled = canRedo,
                            modifier = Modifier
                                .size(36.dp)
                                .bounceClick(scaleDown = 0.9f)
                                .testTag("redo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo Text Change",
                                tint = if (canRedo) OrangePrimary else Color(0x4071717A),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // CLEAR BUTTON (Saves undo snapshot)
                        IconButton(
                            onClick = { viewModel.clearImportText() },
                            enabled = importText.isNotEmpty(),
                            modifier = Modifier
                                .size(36.dp)
                                .bounceClick(scaleDown = 0.9f)
                                .testTag("clear_text_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Text",
                                tint = if (importText.isNotEmpty()) MaterialTheme.colorScheme.error else Color(0x4071717A),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // LOAD SAMPLE BUTTON
                        OutlinedButton(
                            onClick = { viewModel.loadSampleImportText() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .bounceClick(scaleDown = 0.94f)
                                .testTag("load_sample_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
                        ) {
                            Text("Sample", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Text field
                OutlinedTextField(
                    value = importText,
                    onValueChange = { viewModel.onImportTextChanged(it) },
                    placeholder = {
                        Text(
                            text = "Paste text with tags:\n[Q] Question stem...\n[A] Final Answer...\n[SOL] Derivation...\n[FORMULA] Name: Equation | Vars\n[CALCU] Technique: [KEY] [KEY]",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF71717A)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .testTag("import_text_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xCC16151E),
                        unfocusedContainerColor = Color(0x99131218),
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = Color(0x40FF7A00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                )

                // History indicator text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${importText.length} characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA1A1AA)
                    )
                    Text(
                        text = if (canUndo) "Undo ready (${if (canRedo) "Redo available" else "Latest revision"})" else "No changes to undo",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (canUndo) OrangePrimary else Color(0xFF71717A)
                    )
                }
            }
        }

        // STEP 4: LIVE PARSING VALIDATION PREVIEW
        parsePreview?.let { preview ->
            GlassyCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(OrangePrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE PARSING PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = OrangePrimary
                        )
                    }

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x2BFF6B00))
                                .border(1.dp, Color(0x4DFF6B00), RoundedCornerShape(10.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${preview.items.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangePrimary
                                )
                                Text("Questions", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }

                        if (preview.duplicateCount > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AmberSecondaryDark.copy(alpha = 0.22f))
                                    .border(1.dp, AmberSecondaryDark.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${preview.duplicateCount}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberSecondaryDark
                                    )
                                    Text("Duplicates Omitted", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 9.sp)
                                }
                            }
                        }

                        val totalFormulas = preview.items.sumOf { it.formulas.size }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldTertiary.copy(alpha = 0.18f))
                                .border(1.dp, EmeraldTertiary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$totalFormulas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldTertiary
                                )
                                Text("Formulas", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }

                        val totalTechniques = preview.items.sumOf { it.calcuTechniques.size }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberSecondaryDark.copy(alpha = 0.18f))
                                .border(1.dp, AmberSecondaryDark.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$totalTechniques",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberSecondaryDark
                                )
                                Text("Calcu Keys", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }

                    // Warnings if any
                    if (preview.warnings.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberSecondaryDark.copy(alpha = 0.15f))
                                .border(1.dp, AmberSecondaryDark.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                preview.warnings.forEach { warning ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = AmberSecondaryDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = warning, style = MaterialTheme.typography.bodySmall, color = AmberSecondaryDark)
                                    }
                                }
                            }
                        }
                    }

                    // Preview of the first card parsed
                    if (preview.items.isNotEmpty()) {
                        val first = preview.items.first()
                        val isConcept = first.solution.isBlank() && first.formulas.isEmpty() && first.calcuTechniques.isEmpty()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F0E13))
                                .border(1.dp, Color(0x33FF7A00), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Previewing Item 1 of ${preview.items.size}:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangePrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isConcept) Color(0x228B5CF6) else Color(0x22FB923C))
                                            .border(1.dp, if (isConcept) Color(0x558B5CF6) else Color(0x55FB923C), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isConcept) "CONCEPT RECALL" else "PROBLEM SOLVING",
                                            color = if (isConcept) Color(0xFFA78BFA) else OrangePrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                                MathTextView(
                                    text = first.stem,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (first.answer.isNotEmpty()) {
                                    Text(
                                        text = "Answer: ${first.answer}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // STEP 5: COMMIT / IMPORT BUTTON (GLOWING GRADIENT BUTTON)
        val canCommit = !isImporting && (parsePreview?.items?.isNotEmpty() == true)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .bounceClick(scaleDown = 0.95f) {
                    if (canCommit) {
                        viewModel.importToSelectedSubject(
                            onSuccess = { summary ->
                                showImportSuccessDialog = true
                            },
                            onError = { err ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(err)
                                }
                            }
                        )
                    }
                }
                .shadow(
                    elevation = if (canCommit) 10.dp else 0.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = Color(0x99FF6B00)
                )
                .clip(RoundedCornerShape(14.dp))
                .background(if (canCommit) PrimaryOrangeGradient else DisabledButtonBrush)
                .border(
                    width = 1.dp,
                    color = if (canCommit) Color(0x80FFA24C) else Color.Transparent,
                    shape = RoundedCornerShape(14.dp)
                )
                .testTag("import_action_button"),
            contentAlignment = Alignment.Center
        ) {
            if (isImporting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Appending to Notebook...", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Append to ${currentImportSubject?.code ?: "Subject"}",
                        fontWeight = FontWeight.Bold,
                        color = if (canCommit) Color.White else Color(0xFF71717A),
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // CREATE NEW SUBJECT DIALOG
    if (showNewSubjectDialog) {
        var name by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewSubjectDialog = false },
            containerColor = Color(0xFF141318),
            title = { Text("Create Subject Notebook", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Subject Name (e.g. Signal Processing)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Short Code (e.g. SIG-1)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && code.isNotBlank()) {
                            viewModel.createNewSubject(name, code, desc, "#FF6B00")
                            showNewSubjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    enabled = name.isNotBlank() && code.isNotBlank()
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewSubjectDialog = false }) {
                    Text("Cancel", color = Color(0xFFA1A1AA))
                }
            }
        )
    }

    // SUCCESS SUMMARY DIALOG
    if (showImportSuccessDialog && lastSummary != null) {
        val s = lastSummary!!
        AlertDialog(
            onDismissRequest = { showImportSuccessDialog = false },
            containerColor = Color(0xFF141318),
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldTertiary,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = { Text("Import Completed", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Append-only ingestion finished successfully:", color = Color.White)
                    Text("• ${s.insertedQuestions} new questions added", color = OrangePrimary)
                    Text("• ${s.insertedFormulas} key formulas linked", color = Color(0xFFD4D4D8))
                    Text("• ${s.insertedTechniques} calculator techniques stored", color = Color(0xFFD4D4D8))
                    if (s.duplicateQuestionsSkipped > 0) {
                        Text(
                            "• ${s.duplicateQuestionsSkipped} duplicates skipped (SHA-256 deduplicated)",
                            color = AmberSecondaryDark
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showImportSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Review Deck", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // CLEAR DEMO & TEST DATA DIALOG
    if (showClearDemoDialog) {
        AlertDialog(
            onDismissRequest = { showClearDemoDialog = false },
            containerColor = Color(0xFF16151E),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x33F87171)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Clear Demo / Test Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Select what you want to remove. User-imported notes and logged review sessions remain untouched (unless you select Wipe Everything).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA1A1AA)
                    )

                    val options = listOf(
                        Triple(
                            "ALL",
                            "All Starter Demo Data",
                            "Deletes demo questions, formulas, techniques, and subjects (${demoStats.demoQuestionsCount} Qs, ${demoStats.demoFormulasCount} Formulas, ${demoStats.demoSubjectsCount} Subjects)"
                        ),
                        Triple(
                            "REVIEWER",
                            "Demo Reviewer Only",
                            "Clears the ${demoStats.demoQuestionsCount} starter questions from the Reviewer while keeping subjects intact"
                        ),
                        Triple(
                            "FORMULAS",
                            "Demo Formulas Only",
                            "Clears the ${demoStats.demoFormulasCount} starter formulas from Formula Vault"
                        ),
                        Triple(
                            "SUBJECTS",
                            "Demo Subjects Only",
                            "Deletes ELEC-1, COMM-2, MATH, GEAS starter subject notebooks"
                        ),
                        Triple(
                            "RESET",
                            "Wipe Everything (Clean Slate)",
                            "Deletes ALL subjects, flashcards, formulas, and history for a pristine fresh install"
                        )
                    )

                    options.forEach { (key, title, subtitle) ->
                        val isSelected = selectedClearScope == key
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0x28FB923C) else Color(0x33100F15))
                                .border(
                                    width = if (isSelected) 1.2.dp else 1.dp,
                                    color = if (isSelected) OrangePrimary else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedClearScope = key }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedClearScope = key },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = OrangePrimary,
                                        unselectedColor = Color(0xFF71717A)
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White else Color(0xFFE4E4E7)
                                    )
                                    Text(
                                        text = subtitle,
                                        fontSize = 11.sp,
                                        color = Color(0xFFA1A1AA),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    if (!demoStats.hasAnyDemo) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = {
                                viewModel.restoreDemoData {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Starter demo subjects, reviewer questions, and formulas restored!")
                                    }
                                    showClearDemoDialog = false
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = EmeraldTertiary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore Starter Demo Data", color = EmeraldTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (selectedClearScope) {
                            "ALL" -> {
                                viewModel.clearAllDemoData { result ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Cleared demo data: ${result.questionsDeleted} reviewer questions, ${result.formulasDeleted} formulas, ${result.subjectsDeleted} subjects."
                                        )
                                    }
                                    showClearDemoDialog = false
                                }
                            }
                            "REVIEWER" -> {
                                viewModel.deleteDemoReviewerQuestions { count ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Cleared $count demo reviewer questions.")
                                    }
                                    showClearDemoDialog = false
                                }
                            }
                            "FORMULAS" -> {
                                viewModel.deleteDemoFormulas { count ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Cleared $count demo formulas from Formula Vault.")
                                    }
                                    showClearDemoDialog = false
                                }
                            }
                            "SUBJECTS" -> {
                                viewModel.deleteDemoSubjects { count ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Deleted $count demo subjects.")
                                    }
                                    showClearDemoDialog = false
                                }
                            }
                            "RESET" -> {
                                viewModel.resetAllData {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("All database tables wiped. Ready for fresh imports.")
                                    }
                                    showClearDemoDialog = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirm Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDemoDialog = false }) {
                    Text("Cancel", color = Color(0xFFA1A1AA))
                }
            }
        )
    }
}
