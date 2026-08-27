package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FilterChipPill
import com.example.ui.components.GlassCard
import com.example.ui.components.MercurySearchBar
import com.example.ui.theme.MercuryPink
import com.example.ui.theme.MercuryTheme
import com.example.ui.theme.MercuryViolet
import com.example.ui.viewmodel.NotesViewModel
import com.example.ui.viewmodel.SearchFilterOption

@Composable
fun SearchScreen(
    viewModel: NotesViewModel,
    onNoteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = MercuryTheme.glass
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchFilter by viewModel.searchFilter.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glass.canvasBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header & Search Bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.displayMedium,
                        color = glass.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    MercurySearchBar(
                        query = query,
                        onQueryChange = { viewModel.setSearchQuery(it) }
                    )
                }
            }

            // Filter Chips Row
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChipPill(
                            label = "All",
                            isSelected = searchFilter == SearchFilterOption.ALL,
                            onClick = { viewModel.setSearchFilter(SearchFilterOption.ALL) }
                        )
                    }

                    item {
                        FilterChipPill(
                            label = "Favorites",
                            isSelected = searchFilter == SearchFilterOption.FAVORITES,
                            icon = Icons.Default.Favorite,
                            onClick = { viewModel.setSearchFilter(SearchFilterOption.FAVORITES) }
                        )
                    }

                    item {
                        FilterChipPill(
                            label = "With Images",
                            isSelected = searchFilter == SearchFilterOption.HAS_IMAGE,
                            icon = Icons.Default.Image,
                            onClick = { viewModel.setSearchFilter(SearchFilterOption.HAS_IMAGE) }
                        )
                    }

                    item {
                        FilterChipPill(
                            label = "Checklists",
                            isSelected = searchFilter == SearchFilterOption.HAS_CHECKLIST,
                            icon = Icons.Default.Checklist,
                            onClick = { viewModel.setSearchFilter(SearchFilterOption.HAS_CHECKLIST) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Recent Searches (if search query is empty)
            if (query.isEmpty() && recentSearches.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = glass.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Recent Searches",
                                style = MaterialTheme.typography.titleSmall,
                                color = glass.textSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelSmall,
                            color = glass.primaryAccent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { viewModel.clearRecentSearches() }
                                .padding(4.dp)
                        )
                    }
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentSearches) { recent ->
                            GlassCard(
                                shape = RoundedCornerShape(16.dp),
                                backgroundColor = glass.cardBackground,
                                onClick = { viewModel.setSearchQuery(recent) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = recent,
                                        fontSize = 13.sp,
                                        color = glass.textPrimary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Search Results Count Header
            item {
                Text(
                    text = if (query.isNotBlank()) "Results (${searchResults.size})" else "All Notes",
                    style = MaterialTheme.typography.titleSmall,
                    color = glass.textSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // Results List
            if (searchResults.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = glass.textMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching notes found",
                            style = MaterialTheme.typography.titleMedium,
                            color = glass.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try searching for different keywords or clear filters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = glass.textSecondary
                        )
                    }
                }
            } else {
                items(searchResults, key = { it.id }) { note ->
                    NoteCardItem(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        onTogglePin = { viewModel.togglePin(note) },
                        onToggleFavorite = { viewModel.toggleFavorite(note) },
                        onDelete = { viewModel.moveToTrash(note.id) },
                        onDuplicate = { viewModel.duplicateNote(note) }
                    )
                }
            }
        }
    }
}
