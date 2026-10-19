package com.example.util

/**
 * State machine managing Undo and Redo operations for the Text Import screen.
 * Tracks textual revisions with bounded history depth and instant step backward/forward.
 */
class TextHistoryManager(
    initialText: String = "",
    private val maxHistorySize: Int = 60
) {
    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()

    var currentText: String = initialText
        private set

    init {
        // Push initial state
        undoStack.addLast(initialText)
    }

    val canUndo: Boolean
        get() = undoStack.size > 1

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    val undoCount: Int
        get() = (undoStack.size - 1).coerceAtLeast(0)

    val redoCount: Int
        get() = redoStack.size

    /**
     * Records a new text revision into the undo history.
     * Clears the redo branch on new divergent edits.
     */
    fun push(newText: String) {
        if (newText == currentText) return

        // Push current text to undo
        undoStack.addLast(currentText)
        if (undoStack.size > maxHistorySize) {
            undoStack.removeFirst()
        }

        // Divergent edit clears redo stack
        redoStack.clear()
        currentText = newText
    }

    /**
     * Reverts to the previous text snapshot.
     * Returns the reverted text, or null if cannot undo.
     */
    fun undo(): String? {
        if (!canUndo) return null

        val previous = undoStack.removeLast()
        redoStack.addLast(currentText)
        currentText = previous
        return currentText
    }

    /**
     * Steps forward to the next undone text snapshot.
     * Returns the reapplied text, or null if cannot redo.
     */
    fun redo(): String? {
        if (!canRedo) return null

        val next = redoStack.removeLast()
        undoStack.addLast(currentText)
        currentText = next
        return currentText
    }

    /**
     * Resets the entire history with a new baseline.
     */
    fun reset(newText: String) {
        undoStack.clear()
        redoStack.clear()
        currentText = newText
        undoStack.addLast(newText)
    }
}
