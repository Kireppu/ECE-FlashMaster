package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CalcuTechniqueBlock
import com.example.ui.components.CanonModelBadge
import com.example.ui.components.GlassyCard
import com.example.ui.theme.AmberSecondaryDark
import com.example.ui.theme.OrangePrimary
import com.example.ui.viewmodel.EceViewModel

@Composable
fun CalcuVaultScreen(
    viewModel: EceViewModel,
    modifier: Modifier = Modifier
) {
    val techniques by viewModel.calcuTechniqueList.collectAsState()
    val searchQuery by viewModel.calcuSearchQuery.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val selectedSubId by viewModel.selectedSubjectId.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // COMPACT GLASSY HEADER TILE FOR MAXIMUM PHONE SCREEN VIEWPORT
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x22FBBF24))
                        .border(1.dp, Color(0x44FBBF24), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = AmberSecondaryDark,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Calcu-Technique Vault",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "${techniques.size} Canon F-789SGA Shortcut Guides",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA1A1AA),
                        maxLines = 1
                    )
                }
                CanonModelBadge(modelName = "Canon F-789SGA")
            }
        }

        // GLASSY SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setCalcuSearchQuery(it) },
            placeholder = {
                Text(
                    text = "Search shortcuts (CMPLX, Polar, CALC)...",
                    color = Color(0xFF71717A),
                    fontSize = 13.sp,
                    maxLines = 1
                )
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = AmberSecondaryDark)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setCalcuSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = Color(0xFFA1A1AA))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calcu_search_field"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0x5516151E),
                unfocusedContainerColor = Color(0x40131218),
                focusedBorderColor = AmberSecondaryDark,
                unfocusedBorderColor = Color(0x2CFDBA74),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        // SUBJECT FILTER CHIPS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedSubId == null,
                onClick = { viewModel.selectSubject(null) },
                label = { Text("All Subjects", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0x26FB923C),
                    selectedLabelColor = OrangePrimary,
                    containerColor = Color(0x5516151D),
                    labelColor = Color(0xFFA1A1AA)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedSubId == null,
                    borderColor = if (selectedSubId == null) Color(0xFFFDBA74) else Color(0x24FDBA74),
                    borderWidth = if (selectedSubId == null) 1.2.dp else 1.dp
                ),
                shape = RoundedCornerShape(12.dp)
            )
            subjects.forEach { subject ->
                val isSelected = selectedSubId == subject.id
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectSubject(subject.id) },
                    label = { Text(subject.code, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0x26FB923C),
                        selectedLabelColor = OrangePrimary,
                        containerColor = Color(0x5516151D),
                        labelColor = Color(0xFFA1A1AA)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color(0xFFFDBA74) else Color(0x24FDBA74),
                        borderWidth = if (isSelected) 1.2.dp else 1.dp
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // LIST OF TECHNIQUES
        if (techniques.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = Color(0x66FBBF24),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No shortcuts matching '$searchQuery'" else "No calculator techniques found in this deck",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(top = 2.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(techniques, key = { it.id }) { tech ->
                    GlassyCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        elevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Title + Constant-size 1-line Canon F-789SGA badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tech.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 10.dp)
                                )
                                CanonModelBadge(modelName = tech.calculatorModel)
                            }

                            // Structured Mode + Green Accent Bar + Numbered Steps
                            CalcuTechniqueBlock(
                                rawKeystrokes = tech.keystrokes,
                                notes = tech.notes
                            )
                        }
                    }
                }
            }
        }
    }
}
