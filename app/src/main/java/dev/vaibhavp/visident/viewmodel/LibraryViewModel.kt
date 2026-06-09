package dev.vaibhavp.visident.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vaibhavp.visident.data.model.SessionEntity
import dev.vaibhavp.visident.repo.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/** Backs the search list and session-detail screens with reactive reads and delete/edit. */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: SessionRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // The list re-queries automatically as the query changes, and re-emits on any table change.
    @OptIn(ExperimentalCoroutinesApi::class)
    val sessions: StateFlow<List<SessionEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.observeAllSessions()
            else repository.searchSessions(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedSession = MutableStateFlow<SessionEntity?>(null)
    val selectedSession: StateFlow<SessionEntity?> = _selectedSession.asStateFlow()

    private val _selectedSessionImages = MutableStateFlow<List<File>>(emptyList())
    val selectedSessionImages: StateFlow<List<File>> = _selectedSessionImages.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            _selectedSession.value = repository.getSession(sessionId)
            _selectedSessionImages.value = repository.getImagesForSession(sessionId)
        }
    }

    fun deleteSession(session: SessionEntity, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteSession(session)
            onDeleted()
        }
    }

    fun updateSession(session: SessionEntity, onUpdated: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateSession(session)
            _selectedSession.value = session
            onUpdated()
        }
    }
}
