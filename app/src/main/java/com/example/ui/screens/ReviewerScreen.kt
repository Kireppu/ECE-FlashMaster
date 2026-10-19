package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CalcuTechniqueBlock
import com.example.ui.components.CanonModelBadge
import com.example.ui.components.FormulaDisplayCard
import com.example.ui.components.GlassyCard
import com.example.ui.components.GlassyPillBadge
import com.example.ui.components.KatexMathView
import com.example.ui.components.MathTextView
import com.example.ui.components.bounceClick
import com.example.ui.theme.AmberSecondaryDark
import com.example.ui.theme.DisabledButtonBrush
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.PrimaryOrangeGradient
import com.example.ui.theme.RevealedButtonBrush
import com.example.ui.viewmodel.EceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReviewerScreen(
    viewModel: EceViewModel,
    modifier: Modifier = Modifier,
    onNavigateToSubjects: (() -> Unit)? = null
) {
    val subjects by viewModel.subjects.collectAsState()
    val selectedSubId by viewModel.selectedSubjectId.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isRevealed by viewModel.isSolutionRevealed.collectAsState()
    val currentFormulas by viewModel.currentQuestionFormulas.collectAsState()
    val currentTechniques by viewModel.currentQuestionTechniques.collectAsState()

    val totalQuestions = questions.size
    val currentCard = if (questions.isNotEmpty() && currentIndex in questions.indices) questions[currentIndex] else null
    val masteredCount = questions.count { it.isMastered }
    val progress = if (totalQuestions > 0) masteredCount.toFloat() / totalQuestions else 0f

    val currentSubject = subjects.find { it.id == currentCard?.subjectId }

    var shuffleRotationTarget by remember { mutableFloatStateOf(0f) }
    val animatedShuffleRotation by animateFloatAsState(
        targetValue = shuffleRotationTarget,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "shuffle_rot"
    )

    var showDeleteCardDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // TOP SUBJECT FILTER BAR WITH SHEER GLASSY CHIPS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedSubId == null,
                onClick = { viewModel.selectSubject(null) },
                label = { Text("All (${questions.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0x24FB923C),
                    selectedLabelColor = OrangePrimary,
                    containerColor = Color(0x4D16151D),
                    labelColor = Color(0xFFA1A1AA)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedSubId == null,
                    borderColor = if (selectedSubId == null) Color(0xFFFDBA74) else Color(0x24FDBA74),
                    borderWidth = if (selectedSubId == null) 1.2.dp else 1.dp
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("filter_all_subjects")
            )
            subjects.forEach { subject ->
                val isSelected = selectedSubId == subject.id
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectSubject(subject.id) },
                    label = { Text(subject.code, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0x24FB923C),
                        selectedLabelColor = OrangePrimary,
                        containerColor = Color(0x4D16151D),
                        labelColor = Color(0xFFA1A1AA)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color(0xFFFDBA74) else Color(0x24FDBA74),
                        borderWidth = if (isSelected) 1.2.dp else 1.dp
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_subject_${subject.code}")
                )
            }
            if (onNavigateToSubjects != null) {
                FilterChip(
                    selected = false,
                    onClick = { onNavigateToSubjects() },
                    label = { Text("+ Decks", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0x18FB923C),
                        labelColor = OrangePrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = false,
                        borderColor = Color(0x33FDBA74),
                        borderWidth = 1.dp
                    ),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Manage Subjects",
                            tint = OrangePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    modifier = Modifier.testTag("filter_manage_subjects")
                )
            }
        }

        // MASTERY HUD STATS TILE
        GlassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            shape = RoundedCornerShape(14.dp),
            elevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(OrangePrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (totalQuestions > 0) "Card ${currentIndex + 1} of $totalQuestions" else "No flashcards",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "$masteredCount / $totalQuestions Mastered (${(progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (progress >= 0.8f) EmeraldTertiary else OrangePrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (progress >= 0.8f) EmeraldTertiary else OrangePrimary,
                    trackColor = Color(0x2827272A)
                )
            }
        }

        // FLASHCARD CONTENT WITH SMOOTH SLIDE/SCALE TRANSITION
        if (currentCard == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    shape = RoundedCornerShape(22.dp),
                    elevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x22FB923C))
                                .border(1.dp, Color(0x44FDBA74), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "No cards",
                                tint = OrangePrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = "No Flashcards in this Deck",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Navigate to the Import tab to paste AI-generated ECE review notes or click 'Load Sample' to begin your active recall session.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFA1A1AA),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                AnimatedContent(
                    targetState = currentCard,
                    transitionSpec = {
                        (slideInHorizontally(
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
                            initialOffsetX = { fullWidth -> fullWidth / 3 }
                        ) + fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.96f))
                            .togetherWith(
                                slideOutHorizontally(
                                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
                                    targetOffsetX = { fullWidth -> -fullWidth / 3 }
                                ) + fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.96f)
                            )
                    },
                    label = "card_flip_transition"
                ) { card ->
                    val cardScrollState = rememberScrollState()
                    val coroutineScope = rememberCoroutineScope()

                    GlassyCard(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(cardScrollState)
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            // Card Header: Subject Code & Mastered status pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    GlassyPillBadge(
                                        text = currentSubject?.code ?: "ECE",
                                        color = OrangePrimary
                                    )
                                    if (card.isMastered) {
                                        GlassyPillBadge(
                                            text = "MASTERED",
                                            color = EmeraldTertiary,
                                            icon = Icons.Default.CheckCircle
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val masteredIconScale by animateFloatAsState(
                                        targetValue = if (card.isMastered) 1.2f else 1f,
                                        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
                                        label = "mastered_scale"
                                    )
                                    val masteredIconColor by animateColorAsState(
                                        targetValue = if (card.isMastered) EmeraldTertiary else Color(0xFF71717A),
                                        animationSpec = tween(200),
                                        label = "mastered_color"
                                    )

                                    IconButton(
                                        onClick = { viewModel.toggleMastered(card) },
                                        modifier = Modifier
                                            .scale(masteredIconScale)
                                            .testTag("toggle_mastered_header")
                                    ) {
                                        Icon(
                                            imageVector = if (card.isMastered) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Toggle Mastered",
                                            tint = masteredIconColor
                                        )
                                    }

                                    IconButton(
                                        onClick = { showDeleteCardDialog = true },
                                        modifier = Modifier.testTag("delete_card_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Flashcard",
                                            tint = Color(0xFFF87171)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // QUESTION STEM HEADER
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(OrangePrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "QUESTION STEM",
                                    style = MaterialTheme.typography.labelSmall,
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OrangePrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            KatexMathView(
                                mathContent = card.stem,
                                fontSizeSp = 15.5f,
                                minHeight = 48.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("question_stem_math")
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // SOFT WARM GRADIENT REVEAL BUTTON WITH ROTATING ICON
                            val revealArrowRotation by animateFloatAsState(
                                targetValue = if (isRevealed) 180f else 0f,
                                animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
                                label = "reveal_rot"
                            )
                            val hasExtraDetails = card.solution.isNotBlank() || currentTechniques.isNotEmpty() || currentFormulas.isNotEmpty()
                            val revealButtonText = if (isRevealed) {
                                if (hasExtraDetails) "Hide Solution & Details" else "Hide Answer"
                            } else {
                                if (hasExtraDetails) "Reveal Solution & Details" else "Reveal Answer"
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .bounceClick(scaleDown = 0.96f) { viewModel.toggleSolution() }
                                    .shadow(
                                        elevation = if (isRevealed) 2.dp else 6.dp,
                                        shape = RoundedCornerShape(14.dp),
                                        spotColor = Color(0x55FB923C)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isRevealed) RevealedButtonBrush
                                        else PrimaryOrangeGradient
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isRevealed) Color(0x55FDBA74) else Color(0x66FDBA74),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .testTag("reveal_solution_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = revealButtonText,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .rotate(revealArrowRotation)
                                    )
                                }
                            }

                            // EXPANDABLE SOLUTION HUD WITH SMOOTH EXPAND TRANSITION
                            AnimatedVisibility(
                                visible = isRevealed,
                                enter = fadeIn(animationSpec = tween(240)) + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                                exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 18.dp, bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    HorizontalDivider(color = Color(0x22FDBA74))

                                    // 1. FINAL ANSWER BADGE + QUICK JUMP TO SHORTCUT GUIDE
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0x22FB923C))
                                            .border(
                                                width = 1.dp,
                                                color = Color(0x55FDBA74),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .padding(14.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lightbulb,
                                                        contentDescription = null,
                                                        tint = OrangePrimary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "FINAL ANSWER",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = OrangePrimary
                                                    )
                                                }

                                                if (currentTechniques.isNotEmpty()) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(AmberSecondaryDark.copy(alpha = 0.16f))
                                                            .border(1.dp, AmberSecondaryDark.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                coroutineScope.launch {
                                                                    cardScrollState.animateScrollTo(cardScrollState.maxValue)
                                                                }
                                                            }
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Default.Calculate,
                                                                contentDescription = null,
                                                                tint = AmberSecondaryDark,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "Shortcut Guide ↓",
                                                                color = AmberSecondaryDark,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            KatexMathView(
                                                mathContent = card.answer,
                                                fontSizeSp = 15.5f,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("answer_math")
                                            )
                                        }
                                    }

                                    // 2. CANON F-789SGA CALCULATOR TECHNIQUES (Placed prominently so phone screens show the full shortcut guide!)
                                    if (currentTechniques.isNotEmpty()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(AmberSecondaryDark)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "CALCULATOR TECHNIQUES",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 1.sp,
                                                    color = AmberSecondaryDark
                                                )
                                            }

                                            currentTechniques.forEach { tech ->
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(Color(0x55121218))
                                                        .border(
                                                            width = 1.dp,
                                                            color = Color(0x28FDBA74),
                                                            shape = RoundedCornerShape(14.dp)
                                                        )
                                                        .padding(12.dp)
                                                ) {
                                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = tech.title,
                                                                color = Color.White,
                                                                style = MaterialTheme.typography.labelLarge,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .padding(end = 10.dp)
                                                            )
                                                            CanonModelBadge(modelName = tech.calculatorModel)
                                                        }

                                                        CalcuTechniqueBlock(
                                                            rawKeystrokes = tech.keystrokes,
                                                            notes = tech.notes
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 3. STEP-BY-STEP DERIVATION
                                    if (card.solution.isNotBlank()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(OrangePrimary)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "STEP-BY-STEP DERIVATION",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 1.sp,
                                                    color = OrangePrimary
                                                )
                                            }
                                            KatexMathView(
                                                mathContent = card.solution,
                                                fontSizeSp = 14.5f,
                                                minHeight = 48.dp,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("solution_math")
                                            )
                                        }
                                    }

                                    // 4. KEY FORMULAS USED CARD
                                    if (currentFormulas.isNotEmpty()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(OrangePrimary)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "KEY FORMULAS USED",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 1.sp,
                                                    color = OrangePrimary
                                                )
                                            }
                                            currentFormulas.forEach { formulaEntity ->
                                                FormulaDisplayCard(
                                                    name = formulaEntity.name,
                                                    formula = formulaEntity.formula,
                                                    variables = formulaEntity.variables
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM ACTION NAVIGATION CONTROLS (Sheer Glassy Bar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCC111116))
                .border(width = 1.dp, color = Color(0x22FDBA74))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.previousCard() },
                    enabled = totalQuestions > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OrangePrimary
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.bounceClick(scaleDown = 0.94f).testTag("prev_card_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Card",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prev", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                IconButton(
                    onClick = {
                        shuffleRotationTarget += 360f
                        viewModel.shuffleDeck()
                    },
                    enabled = totalQuestions > 1,
                    modifier = Modifier
                        .size(38.dp)
                        .bounceClick(scaleDown = 0.92f)
                        .testTag("shuffle_deck_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle Cards",
                        tint = OrangePrimary,
                        modifier = Modifier.size(20.dp).rotate(animatedShuffleRotation)
                    )
                }

                if (currentCard != null) {
                    val isMastered = currentCard.isMastered
                    val buttonBg by animateColorAsState(
                        targetValue = if (isMastered) Color(0x2822C55E) else Color(0x22FB923C),
                        animationSpec = tween(220),
                        label = "mastered_bg"
                    )
                    val buttonBorder by animateColorAsState(
                        targetValue = if (isMastered) EmeraldTertiary else OrangePrimary,
                        animationSpec = tween(220),
                        label = "mastered_border"
                    )

                    Box(
                        modifier = Modifier
                            .bounceClick(scaleDown = 0.94f) { viewModel.toggleMastered(currentCard) }
                            .clip(RoundedCornerShape(12.dp))
                            .background(buttonBg)
                            .border(
                                width = 1.dp,
                                color = buttonBorder.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("toggle_mastered_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isMastered) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isMastered) EmeraldTertiary else OrangePrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isMastered) "Mastered" else "Mark Done",
                                color = if (isMastered) EmeraldTertiary else OrangePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .bounceClick(scaleDown = 0.94f) { if (totalQuestions > 0) viewModel.nextCard() }
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp), spotColor = Color(0x44FB923C))
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (totalQuestions > 0) PrimaryOrangeGradient else DisabledButtonBrush)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("next_card_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Next", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Card",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Delete Card Confirmation Dialog
    if (showDeleteCardDialog && currentCard != null) {
        val cardToDelete = currentCard
        AlertDialog(
            onDismissRequest = { showDeleteCardDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = Color(0xFFEF4444)
                )
            },
            title = {
                Text(
                    text = "Delete This Flashcard?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "This will permanently remove this question from your deck, along with its linked formulas and calculator steps.",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteCardDialog = false
                        viewModel.deleteQuestion(cardToDelete.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCardDialog = false }) {
                    Text("Cancel", color = Color(0xFFA1A1AA))
                }
            },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
