package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Functions
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
import com.example.ui.components.FormulaDisplayCard
import com.example.ui.components.GlassyCard
import com.example.ui.theme.OrangePrimary
import com.example.ui.viewmodel.EceViewModel

@Composable
fun FormulaVaultScreen(
    viewModel: EceViewModel,
    modifier: Modifier = Modifier
) {
    val formulas by viewModel.formulaList.collectAsState()
    val searchQuery by viewModel.formulaSearchQuery.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val selectedSubId by viewModel.selectedSubjectId.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // GLASSY HEADER TILE
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
                        imageVector = Icons.Default.Functions,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Formula Vault",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "${formulas.size} engineering formulas with 2D Word math",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA1A1AA)
                    )
                }
            }
        }

        // GLASSY SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setFormulaSearchQuery(it) },
            placeholder = { Text("Search by name, variable, or symbol (e.g. Z₀, I_c, β)...", color = Color(0xFF71717A), fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = OrangePrimary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setFormulaSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = Color(0xFFA1A1AA))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("formula_search_field"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xCC16151E),
                unfocusedContainerColor = Color(0x99131218),
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = Color(0x40FF7A00),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        // SUBJECT FILTER CHIPS (Glowing Pills)
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
                    selectedContainerColor = Color(0x38FF6B00),
                    selectedLabelColor = OrangePrimary,
                    containerColor = Color(0xAA16151D),
                    labelColor = Color(0xFFA1A1AA)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedSubId == null,
                    borderColor = if (selectedSubId == null) Color(0xFFFF851A) else Color(0x33FF6B00),
                    borderWidth = if (selectedSubId == null) 1.5.dp else 1.dp
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
                        selectedContainerColor = Color(0x38FF6B00),
                        selectedLabelColor = OrangePrimary,
                        containerColor = Color(0xAA16151D),
                        labelColor = Color(0xFFA1A1AA)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color(0xFFFF851A) else Color(0x33FF6B00),
                        borderWidth = if (isSelected) 1.5.dp else 1.dp
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // LIST OF FORMULAS
        if (formulas.isEmpty()) {
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
                            imageVector = Icons.Default.Functions,
                            contentDescription = null,
                            tint = Color(0x66FF6B00),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No formulas matching '$searchQuery'" else "No formulas found in this deck",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(formulas, key = { it.id }) { formula ->
                    FormulaDisplayCard(
                        name = formula.name,
                        formula = formula.formula,
                        variables = formula.variables
                    )
                }
            }
        }
    }
}
