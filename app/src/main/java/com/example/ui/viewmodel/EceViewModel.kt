package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ClearDemoResult
import com.example.data.DemoStats
import com.example.data.EceRepository
import com.example.data.ImportSummary
import com.example.data.SampleData
import com.example.data.model.CalcuTechniqueEntity
import com.example.data.model.FormulaEntity
import com.example.data.model.ImportBatchEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.SubjectEntity
import com.example.util.ParseResult
import com.example.util.TextHistoryManager
import com.example.util.TextImportParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SubjectUiModel(
    val subject: SubjectEntity,
    val totalCount: Int,
    val masteredCount: Int
) {
    val progress: Float
        get() = if (totalCount > 0) masteredCount.toFloat() / totalCount else 0f
}

class EceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EceRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = EceRepository(
            db.subjectDao(),
            db.questionDao(),
            db.formulaDao(),
            db.calcuTechniqueDao(),
            db.importBatchDao(),
            application
        )
        // Seed sample data on first launch
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            refreshDemoStats()
        }
    }

    private val _demoStats = MutableStateFlow(DemoStats())
    val demoStats: StateFlow<DemoStats> = _demoStats.asStateFlow()

    // -------------------------------------------------------------
    // SUBJECTS & FILTERING
    // -------------------------------------------------------------
    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjectUiModels: StateFlow<List<SubjectUiModel>> = combine(
        subjects,
        repository.allQuestions
    ) { subList, qList ->
        subList.map { sub ->
            val subQuestions = qList.filter { it.subjectId == sub.id }
            SubjectUiModel(
                subject = sub,
                totalCount = subQuestions.size,
                masteredCount = subQuestions.count { it.isMastered }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    val selectedSubjectId: StateFlow<Long?> = _selectedSubjectId.asStateFlow()

    fun selectSubject(subjectId: Long?) {
        _selectedSubjectId.value = subjectId
        _currentIndex.value = 0
        _isSolutionRevealed.value = false
    }

    // -------------------------------------------------------------
    // ACTIVE-RECALL REVIEWER HUD
    // -------------------------------------------------------------
    @OptIn(ExperimentalCoroutinesApi::class)
    val questions: StateFlow<List<QuestionEntity>> = _selectedSubjectId
        .flatMapLatest { id ->
            if (id == null) {
                repository.allQuestions
            } else {
                repository.getQuestionsBySubject(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isSolutionRevealed = MutableStateFlow(false)
    val isSolutionRevealed: StateFlow<Boolean> = _isSolutionRevealed.asStateFlow()

    fun toggleSolution() {
        _isSolutionRevealed.value = !_isSolutionRevealed.value
    }

    fun revealSolution() {
        _isSolutionRevealed.value = true
    }

    fun nextCard() {
        val total = questions.value.size
        if (total > 0) {
            _currentIndex.value = (_currentIndex.value + 1) % total
            _isSolutionRevealed.value = false
        }
    }

    fun previousCard() {
        val total = questions.value.size
        if (total > 0) {
            _currentIndex.value = if (_currentIndex.value - 1 < 0) total - 1 else _currentIndex.value - 1
            _isSolutionRevealed.value = false
        }
    }

    fun shuffleDeck() {
        val total = questions.value.size
        if (total > 1) {
            _currentIndex.value = (0 until total).random()
            _isSolutionRevealed.value = false
        }
    }

    fun toggleMastered(question: QuestionEntity) {
        viewModelScope.launch {
            repository.setQuestionMastered(question.id, !question.isMastered)
        }
    }

    // Associated formulas & techniques for current question
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentQuestionFormulas: StateFlow<List<FormulaEntity>> = combine(
        questions,
        _currentIndex
    ) { qList, idx ->
        if (qList.isNotEmpty() && idx in qList.indices) qList[idx].id else null
    }.flatMapLatest { qId ->
        if (qId != null) repository.getFormulasForQuestion(qId)
        else MutableStateFlow(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentQuestionTechniques: StateFlow<List<CalcuTechniqueEntity>> = combine(
        questions,
        _currentIndex
    ) { qList, idx ->
        if (qList.isNotEmpty() && idx in qList.indices) qList[idx].id else null
    }.flatMapLatest { qId ->
        if (qId != null) repository.getTechniquesForQuestion(qId)
        else MutableStateFlow(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------------------------------------
    // FORMULA VAULT
    // -------------------------------------------------------------
    private val _formulaSearchQuery = MutableStateFlow("")
    val formulaSearchQuery: StateFlow<String> = _formulaSearchQuery.asStateFlow()

    fun setFormulaSearchQuery(query: String) {
        _formulaSearchQuery.value = query
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val formulaList: StateFlow<List<FormulaEntity>> = combine(
        _formulaSearchQuery,
        _selectedSubjectId
    ) { query, subId -> Pair(query, subId) }
        .flatMapLatest { (query, subId) ->
            if (query.isNotBlank()) {
                repository.searchFormulas(query)
            } else if (subId != null) {
                repository.getFormulasBySubject(subId)
            } else {
                repository.allFormulas
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------------------------------------
    // CALCU-TECHNIQUE VAULT
    // -------------------------------------------------------------
    private val _calcuSearchQuery = MutableStateFlow("")
    val calcuSearchQuery: StateFlow<String> = _calcuSearchQuery.asStateFlow()

    fun setCalcuSearchQuery(query: String) {
        _calcuSearchQuery.value = query
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val calcuTechniqueList: StateFlow<List<CalcuTechniqueEntity>> = combine(
        _calcuSearchQuery,
        _selectedSubjectId
    ) { query, subId -> Pair(query, subId) }
        .flatMapLatest { (query, subId) ->
            if (query.isNotBlank()) {
                repository.searchTechniques(query)
            } else if (subId != null) {
                repository.getTechniquesBySubject(subId)
            } else {
                repository.allTechniques
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------------------------------------
    // IMPORT SCREEN & UNDO / REDO STATE MACHINE
    // -------------------------------------------------------------
    private val textHistoryManager = TextHistoryManager(initialText = "")

    private val _importText = MutableStateFlow("")
    val importText: StateFlow<String> = _importText.asStateFlow()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _selectedImportSubjectId = MutableStateFlow<Long?>(1L)
    val selectedImportSubjectId: StateFlow<Long?> = _selectedImportSubjectId.asStateFlow()

    private val _parsePreview = MutableStateFlow<ParseResult?>(null)
    val parsePreview: StateFlow<ParseResult?> = _parsePreview.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _lastImportSummary = MutableStateFlow<ImportSummary?>(null)
    val lastImportSummary: StateFlow<ImportSummary?> = _lastImportSummary.asStateFlow()

    fun setSelectedImportSubject(subjectId: Long) {
        _selectedImportSubjectId.value = subjectId
    }

    fun onImportTextChanged(newText: String) {
        if (newText != _importText.value) {
            textHistoryManager.push(newText)
            _importText.value = newText
            updateUndoRedoStates()
            triggerParse(newText)
        }
    }

    fun undoImportText() {
        val previous = textHistoryManager.undo()
        if (previous != null) {
            _importText.value = previous
            updateUndoRedoStates()
            triggerParse(previous)
        }
    }

    fun redoImportText() {
        val next = textHistoryManager.redo()
        if (next != null) {
            _importText.value = next
            updateUndoRedoStates()
            triggerParse(next)
        }
    }

    fun clearImportText() {
        if (_importText.value.isNotEmpty()) {
            textHistoryManager.push("")
            _importText.value = ""
            updateUndoRedoStates()
            _parsePreview.value = null
        }
    }

    fun loadSampleImportText() {
        val sample = SampleData.SAMPLE_IMPORT_TEXT
        textHistoryManager.push(sample)
        _importText.value = sample
        updateUndoRedoStates()
        triggerParse(sample)
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = textHistoryManager.canUndo
        _canRedo.value = textHistoryManager.canRedo
    }

    private fun triggerParse(text: String) {
        viewModelScope.launch(Dispatchers.Default) {
            if (text.isBlank()) {
                _parsePreview.value = null
            } else {
                val result = TextImportParser.parse(text)
                _parsePreview.value = result
            }
        }
    }

    fun importToSelectedSubject(
        onSuccess: (ImportSummary) -> Unit,
        onError: (String) -> Unit
    ) {
        val subjectId = _selectedImportSubjectId.value
        if (subjectId == null) {
            onError("Please select a subject notebook first.")
            return
        }

        val parsed = _parsePreview.value
        if (parsed == null || parsed.items.isEmpty()) {
            onError("No valid questions found to import. Check delimiters [Q] and [A].")
            return
        }

        viewModelScope.launch {
            _isImporting.value = true
            try {
                val rawText = _importText.value
                val summary = repository.importFlashcards(subjectId, parsed.items, rawText)
                _lastImportSummary.value = summary
                _isImporting.value = false
                onSuccess(summary)
            } catch (e: Exception) {
                _isImporting.value = false
                onError(e.message ?: "Failed to import flashcards.")
            }
        }
    }

    // -------------------------------------------------------------
    // IMPORT BATCHES & DELETIONS
    // -------------------------------------------------------------
    val allImportBatches: StateFlow<List<ImportBatchEntity>> = repository.allImportBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val importBatchesForSelectedSubject: StateFlow<List<ImportBatchEntity>> = _selectedSubjectId
        .flatMapLatest { id ->
            if (id == null) {
                repository.allImportBatches
            } else {
                repository.getImportBatchesBySubject(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteImportBatch(batchId: Long, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteImportBatch(batchId)
            onDone?.invoke()
        }
    }

    fun deleteQuestion(questionId: Long, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteQuestion(questionId)
            val currentList = questions.value
            if (currentList.isNotEmpty() && _currentIndex.value >= currentList.size - 1) {
                _currentIndex.value = maxOf(0, currentList.size - 2)
            }
            onDone?.invoke()
        }
    }

    fun createNewSubject(name: String, code: String, desc: String, colorHex: String) {
        viewModelScope.launch {
            val id = repository.createSubject(name, code, desc, colorHex)
            _selectedImportSubjectId.value = id
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            if (_selectedSubjectId.value == subject.id) {
                _selectedSubjectId.value = null
            }
            refreshDemoStats()
        }
    }

    // -------------------------------------------------------------
    // DEMO & TEST DATA MANAGEMENT
    // -------------------------------------------------------------
    fun refreshDemoStats() {
        viewModelScope.launch {
            _demoStats.value = repository.getDemoStats()
        }
    }

    fun deleteDemoReviewerQuestions(onDone: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val count = repository.deleteDemoReviewerQuestions()
            refreshDemoStats()
            val currentList = questions.value
            if (currentList.isNotEmpty() && _currentIndex.value >= currentList.size) {
                _currentIndex.value = maxOf(0, currentList.size - 1)
            }
            onDone?.invoke(count)
        }
    }

    fun deleteDemoFormulas(onDone: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val count = repository.deleteDemoFormulas()
            refreshDemoStats()
            onDone?.invoke(count)
        }
    }

    fun deleteDemoSubjects(onDone: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val count = repository.deleteDemoSubjects()
            refreshDemoStats()
            _selectedSubjectId.value = null
            _selectedImportSubjectId.value = null
            onDone?.invoke(count)
        }
    }

    fun clearAllDemoData(onDone: ((ClearDemoResult) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.clearAllDemoData()
            refreshDemoStats()
            _selectedSubjectId.value = null
            _selectedImportSubjectId.value = null
            _currentIndex.value = 0
            onDone?.invoke(result)
        }
    }

    fun resetAllData(onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.resetAllData()
            refreshDemoStats()
            _selectedSubjectId.value = null
            _selectedImportSubjectId.value = null
            _currentIndex.value = 0
            onDone?.invoke()
        }
    }

    fun restoreDemoData(onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.restoreDemoData()
            refreshDemoStats()
            onDone?.invoke()
        }
    }
}
