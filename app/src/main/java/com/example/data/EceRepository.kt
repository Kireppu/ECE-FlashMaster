package com.example.data

import android.content.Context
import com.example.data.dao.CalcuTechniqueDao
import com.example.data.dao.FormulaDao
import com.example.data.dao.ImportBatchDao
import com.example.data.dao.QuestionDao
import com.example.data.dao.SubjectDao
import com.example.data.model.CalcuTechniqueEntity
import com.example.data.model.FormulaEntity
import com.example.data.model.ImportBatchEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.SubjectEntity
import com.example.util.ParsedFlashcard
import com.example.util.TextImportParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class ImportSummary(
    val insertedQuestions: Int,
    val duplicateQuestionsSkipped: Int,
    val insertedFormulas: Int,
    val insertedTechniques: Int,
    val batchId: Long? = null
)

data class DemoStats(
    val demoQuestionsCount: Int = 0,
    val demoFormulasCount: Int = 0,
    val demoSubjectsCount: Int = 0,
    val demoTechniquesCount: Int = 0
) {
    val hasAnyDemo: Boolean
        get() = demoQuestionsCount > 0 || demoFormulasCount > 0 || demoSubjectsCount > 0
}

data class ClearDemoResult(
    val questionsDeleted: Int,
    val formulasDeleted: Int,
    val subjectsDeleted: Int,
    val techniquesDeleted: Int
)

class EceRepository(
    private val subjectDao: SubjectDao,
    private val questionDao: QuestionDao,
    private val formulaDao: FormulaDao,
    private val calcuTechniqueDao: CalcuTechniqueDao,
    private val importBatchDao: ImportBatchDao,
    private val context: Context? = null
) {
    val allSubjects: Flow<List<SubjectEntity>> = subjectDao.getAllSubjects()
    val allQuestions: Flow<List<QuestionEntity>> = questionDao.getAllQuestions()
    val allFormulas: Flow<List<FormulaEntity>> = formulaDao.getAllFormulas()
    val allTechniques: Flow<List<CalcuTechniqueEntity>> = calcuTechniqueDao.getAllTechniques()
    val allImportBatches: Flow<List<ImportBatchEntity>> = importBatchDao.getAllBatches()
    val totalMasteredCount: Flow<Int> = questionDao.getTotalMasteredCount()
    val totalQuestionCount: Flow<Int> = questionDao.getTotalQuestionCount()

    fun getQuestionsBySubject(subjectId: Long): Flow<List<QuestionEntity>> {
        return questionDao.getQuestionsBySubject(subjectId)
    }

    fun getImportBatchesBySubject(subjectId: Long): Flow<List<ImportBatchEntity>> {
        return importBatchDao.getBatchesBySubject(subjectId)
    }

    fun getFormulasBySubject(subjectId: Long): Flow<List<FormulaEntity>> {
        return formulaDao.getFormulasBySubject(subjectId)
    }

    fun getFormulasForQuestion(questionId: Long): Flow<List<FormulaEntity>> {
        return formulaDao.getFormulasForQuestion(questionId)
    }

    fun getTechniquesForQuestion(questionId: Long): Flow<List<CalcuTechniqueEntity>> {
        return calcuTechniqueDao.getTechniquesForQuestion(questionId)
    }

    fun getTechniquesBySubject(subjectId: Long): Flow<List<CalcuTechniqueEntity>> {
        return calcuTechniqueDao.getTechniquesBySubject(subjectId)
    }

    fun searchFormulas(query: String): Flow<List<FormulaEntity>> {
        return formulaDao.searchFormulas(query)
    }

    fun searchTechniques(query: String): Flow<List<CalcuTechniqueEntity>> {
        return calcuTechniqueDao.searchTechniques(query)
    }

    suspend fun createSubject(name: String, code: String, description: String, colorHex: String): Long {
        return withContext(Dispatchers.IO) {
            val subject = SubjectEntity(
                name = name,
                code = code,
                description = description,
                colorHex = colorHex
            )
            subjectDao.insertSubject(subject)
        }
    }

    suspend fun insertSubject(name: String, code: String, description: String, colorHex: String): Long {
        return createSubject(name, code, description, colorHex)
    }

    suspend fun updateSubject(subject: SubjectEntity) {
        withContext(Dispatchers.IO) {
            subjectDao.updateSubject(subject)
        }
    }

    suspend fun deleteSubject(subject: SubjectEntity) {
        withContext(Dispatchers.IO) {
            subjectDao.deleteSubject(subject)
        }
    }

    suspend fun deleteSubjectById(subjectId: Long) {
        withContext(Dispatchers.IO) {
            subjectDao.deleteSubjectById(subjectId)
        }
    }

    suspend fun toggleQuestionMastered(question: QuestionEntity) {
        withContext(Dispatchers.IO) {
            questionDao.setMasteredStatus(question.id, !question.isMastered)
        }
    }

    suspend fun setQuestionMastered(questionId: Long, isMastered: Boolean) {
        withContext(Dispatchers.IO) {
            questionDao.setMasteredStatus(questionId, isMastered)
        }
    }

    suspend fun deleteQuestion(questionId: Long) {
        withContext(Dispatchers.IO) {
            questionDao.deleteQuestionById(questionId)
        }
    }

    suspend fun deleteImportBatch(batchId: Long) {
        withContext(Dispatchers.IO) {
            importBatchDao.deleteBatchById(batchId)
        }
    }

    suspend fun getDemoStats(): DemoStats = withContext(Dispatchers.IO) {
        val qCount = questionDao.getDemoQuestionCount()
        val initialFormulaHashes = SampleData.getInitialFormulas().map { it.hash }
        val fCount = formulaDao.getDemoFormulaCount(initialFormulaHashes)
        val sCount = subjectDao.getDemoSubjectCount()
        val initialTechHashes = SampleData.getInitialTechniques().map { it.hash }
        val tCount = calcuTechniqueDao.getDemoTechniqueCount(initialTechHashes)
        DemoStats(
            demoQuestionsCount = qCount,
            demoFormulasCount = fCount,
            demoSubjectsCount = sCount,
            demoTechniquesCount = tCount
        )
    }

    suspend fun deleteDemoReviewerQuestions(): Int = withContext(Dispatchers.IO) {
        val count = questionDao.deleteDemoQuestions()
        markDemoDataCleared()
        count
    }

    suspend fun deleteDemoFormulas(): Int = withContext(Dispatchers.IO) {
        val initialFormulaHashes = SampleData.getInitialFormulas().map { it.hash }
        val count = formulaDao.deleteDemoFormulas(initialFormulaHashes)
        count
    }

    suspend fun deleteDemoSubjects(): Int = withContext(Dispatchers.IO) {
        val count = subjectDao.deleteDemoSubjects()
        markDemoDataCleared()
        count
    }

    suspend fun clearAllDemoData(): ClearDemoResult = withContext(Dispatchers.IO) {
        val initialFormulaHashes = SampleData.getInitialFormulas().map { it.hash }
        val initialTechHashes = SampleData.getInitialTechniques().map { it.hash }

        val fDeleted = formulaDao.deleteDemoFormulas(initialFormulaHashes)
        val tDeleted = calcuTechniqueDao.deleteDemoTechniques(initialTechHashes)
        val qDeleted = questionDao.deleteDemoQuestions()
        val sDeleted = subjectDao.deleteDemoSubjects()

        markDemoDataCleared()
        ClearDemoResult(
            questionsDeleted = qDeleted,
            formulasDeleted = fDeleted,
            subjectsDeleted = sDeleted,
            techniquesDeleted = tDeleted
        )
    }

    suspend fun resetAllData(): Unit = withContext(Dispatchers.IO) {
        importBatchDao.deleteAllBatches()
        calcuTechniqueDao.deleteAllTechniques()
        formulaDao.deleteAllFormulas()
        questionDao.deleteAllQuestions()
        subjectDao.deleteAllSubjects()
        markDemoDataCleared()
    }

    suspend fun restoreDemoData(): Unit = withContext(Dispatchers.IO) {
        unmarkDemoDataCleared()
        seedInitialDataIfNeeded(force = true)
    }

    private fun markDemoDataCleared() {
        context?.getSharedPreferences("ece_prefs", Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean("demo_data_cleared", true)
            ?.apply()
    }

    private fun unmarkDemoDataCleared() {
        context?.getSharedPreferences("ece_prefs", Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean("demo_data_cleared", false)
            ?.apply()
    }

    suspend fun importFlashcards(
        subjectId: Long,
        flashcards: List<ParsedFlashcard>,
        rawText: String = ""
    ): ImportSummary {
        return withContext(Dispatchers.IO) {
            var insertedQ = 0
            var skippedQ = 0
            var insertedF = 0
            var insertedT = 0

            var assignedBatchId: Long? = null
            if (rawText.isNotBlank()) {
                val batch = ImportBatchEntity(
                    subjectId = subjectId,
                    rawText = rawText,
                    questionCount = flashcards.size,
                    formulaCount = flashcards.sumOf { it.formulas.size },
                    calcuCount = flashcards.sumOf { it.calcuTechniques.size }
                )
                assignedBatchId = importBatchDao.insertBatch(batch)
            }

            for (card in flashcards) {
                val existingCount = questionDao.countDuplicate(subjectId, card.stemHash)
                if (existingCount > 0) {
                    skippedQ++
                    continue
                }

                val questionEntity = QuestionEntity(
                    subjectId = subjectId,
                    batchId = assignedBatchId,
                    stem = card.stem,
                    stemHash = card.stemHash,
                    answer = card.answer,
                    solution = card.solution,
                    isMastered = false
                )
                val newId = questionDao.insertQuestion(questionEntity)

                if (newId > 0) {
                    insertedQ++

                    val formulasToInsert = card.formulas.map { f ->
                        FormulaEntity(
                            subjectId = subjectId,
                            questionId = newId,
                            name = f.name,
                            formula = f.formula,
                            variables = f.variables,
                            hash = TextImportParser.sha256("${f.name}|${f.formula}")
                        )
                    }
                    val fIds = formulaDao.insertFormulas(formulasToInsert)
                    insertedF += fIds.count { it > 0 }

                    val techniquesToInsert = card.calcuTechniques.map { t ->
                        CalcuTechniqueEntity(
                            subjectId = subjectId,
                            questionId = newId,
                            title = t.title,
                            keystrokes = t.keystrokes,
                            calculatorModel = t.model,
                            notes = t.notes,
                            hash = TextImportParser.sha256("${t.title}|${t.keystrokes}|${t.notes}")
                        )
                    }
                    val tIds = calcuTechniqueDao.insertTechniques(techniquesToInsert)
                    insertedT += tIds.count { it > 0 }
                } else {
                    skippedQ++
                }
            }

            ImportSummary(
                insertedQuestions = insertedQ,
                duplicateQuestionsSkipped = skippedQ,
                insertedFormulas = insertedF,
                insertedTechniques = insertedT,
                batchId = assignedBatchId
            )
        }
    }

    suspend fun seedInitialDataIfNeeded(force: Boolean = false) {
        withContext(Dispatchers.IO) {
            val prefs = context?.getSharedPreferences("ece_prefs", Context.MODE_PRIVATE)
            val isDemoCleared = prefs?.getBoolean("demo_data_cleared", false) ?: false
            if (isDemoCleared && !force) return@withContext

            val count = subjectDao.getSubjectCount()
            if (count == 0) {
                for (sub in SampleData.INITIAL_SUBJECTS) {
                    subjectDao.insertSubject(sub)
                }
                questionDao.insertQuestions(SampleData.getInitialQuestions())
                formulaDao.insertFormulas(SampleData.getInitialFormulas())
                calcuTechniqueDao.upsertTechniques(SampleData.getInitialTechniques())
            } else if (!isDemoCleared || force) {
                questionDao.upsertQuestions(SampleData.getInitialQuestions())
                calcuTechniqueDao.upsertTechniques(SampleData.getInitialTechniques())
            }
        }
    }
}
