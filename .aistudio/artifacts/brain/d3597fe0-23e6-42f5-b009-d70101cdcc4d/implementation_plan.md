# ECE FlashMaster: Hybrid KaTeX Math & Import Management Architecture

Integrate offline KaTeX hybrid WebViews for rendering 2D equations alongside Jetpack Compose, introduce granular import deletion (by batch and individual question), and add an interactive Import History management view with raw note previews.

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural decisions below were confirmed through the interactive Phase 1 clarification:
>
> - **Math Rendering Strategy**: **Offline KaTeX WebViews alongside Compose**. Mathematical expressions (LaTeX delimiters `$$...$$`, `$...$`, `\[...\]`, `\(...\)`, and legacy `[EQ]`/`[IEQ]` tags) will be rendered into high-fidelity 2D typography via local bundled KaTeX assets (`file:///android_asset/math/index.html`) embedded inside a composable `KatexMathView`, ensuring zero network dependency.
> - **Deletion Granularity**: **Both Batch Deletion and Individual Item Removal**. Users will have the ability to delete an entire import batch (and all cascading flashcards, formulas, and calculator steps generated from it) or individually delete specific questions/flashcards directly from the reviewer and formula vault.
> - **Import Note Management**: **Dedicated Import History View with Raw Note Previews**. An Import History tab/drawer item allows students to inspect past imported note transcripts, review timestamped stats (items parsed, formulas extracted), re-copy or inspect the original text, and delete previous import sessions without wiping the whole subject.

---

## 1. Overview & Core Concept

- **What It Does**: ECE FlashMaster enables electrical engineering board exam candidates to import lecture notes and question banks, parse them deterministically into categorized cards, and review them with publication-grade 2D formulas rendered using offline KaTeX WebViews. A comprehensive Import History manager tracks each imported session and allows users to inspect or roll back specific imports without touching other notes in the subject.
- **Target Audience / Persona**: ECE board exam examinees, engineering students, and educators studying electronics, communications, mathematics, and GEAS who require accurate 2D math layout and fine-grained control over imported test banks.
- **Key Value**: Eliminates equation rendering glitches (such as broken symbols or misaligned fractions) through industry-standard KaTeX rendering, and solves note management frustration by offering one-tap batch rollback and single-card removal.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Viewing 2D Equations in Reviewer & Formula Vault**:
   - The user opens a subject (e.g., Electronics Engineering).
   - In the Reviewer flashcard, the question stem, choices, and step-by-step solutions render inline formulas and display blocks via `KatexMathView`.
   - Complex fractions ($\frac{V_{CC} - V_{CE}}{R_C}$), square roots ($\sqrt{2\pi f L}$), integrals, and matrices render cleanly in 2D with Cambria Math / KaTeX typography matching the app's dark/light theme dynamically.
2. **Importing Review Text with Batch Tracking**:
   - In the Import Screen, user pastes raw review notes or uses the standardized AI prompt.
   - Upon pressing "Parse & Append Flashcards", the app generates an `ImportBatchEntity` storing the session timestamp, note excerpt, and counts.
   - All newly generated questions and formulas receive the `batchId` foreign key.
   - A success banner displays: *"Imported 12 Questions, 4 Formulas (Batch #3)"* with a direct link to view the batch in Import History.
3. **Managing & Deleting Previous Imports in Import History**:
   - User navigates to the **Import History** tab (accessible from top app bar in Import Screen or Subject detail).
   - A list of chronological import sessions is displayed with timestamps, source note snippet, question count, and formula count.
   - Expanding a batch item reveals the full raw note preview in a copyable text area.
   - Tapping "Delete Batch" triggers a confirmation dialog explaining that all associated questions and formulas from this specific input will be purged, leaving earlier and later imports intact.
4. **Single-Card Deletion During Study**:
   - While reviewing a card in `ReviewerScreen`, the user taps an overflow/trash icon to delete just that individual question, updating the subject statistics immediately.

### Visual Identity & Theme

- **Aesthetic Direction**: High-density engineering workbench aesthetic. Deep navy/slate dark surfaces (`#0F172A`, `#1E293B`) paired with sharp technical accents (Amber `#F59E0B`, Emerald `#10B981`, and Cobalt `#3B82F6`).
- **Equation Typography**: KaTeX configured with `Cambria Math` fallback, rendered with transparent backgrounds and synchronized system theme font colors (`#E2E8F0` on dark mode, `#0F172A` on light mode).
- **Import History Cards**: Outlined cards with Monospace timestamp chips, preview snippets, badge counts, and destructive action affordances with confirmation guards.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Hybrid KaTeX WebView Composable vs Pure Native Canvas/Text
- **Chosen Approach**: Use an Android WebView hosting offline bundled KaTeX assets (`assets/math/katex.min.js`, `katex.min.css`, `index.html`) packaged into an auto-resizing Jetpack Compose component (`KatexMathView`).
- **Why**: AI models (ChatGPT, Claude, Gemini) naturally emit standard LaTeX notation (`$$...$$`, `$...$`, `\frac`, `\sqrt`). KaTeX is the exact battle-tested engine powering web AI interfaces; it provides flawless 2D mathematical typography without network requests.
- **Alternatives Considered**: Native Canvas or text-based sub/superscripts were limited in handling complex nested fractions, multi-line systems, and matrices.

### Decision 2: Batch Tracking via Relational Schema
- **Chosen Approach**: Introduce `ImportBatchEntity` in Room and associate `QuestionEntity.batchId` with `ForeignKey.CASCADE`.
- **Why**: Enables clean single-transaction rollbacks of entire user imports without affecting earlier notes or requiring manual re-imports.
- **Alternatives Considered**: String tag filtering without database foreign keys (leads to orphaned formulas and inaccurate count caches).

### Decision 3: Zero-Latency WebView Recycling & Auto-Sizing
- **Chosen Approach**: Provide a `KatexMathView` that communicates height changes via JavascriptInterface (`AndroidInterface.onHeightChanged`) and disables WebView scrollbars, rendering seamlessly as an inline Compose block.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                         Presentation Layer                             │
│                                                                        │
│  ┌─────────────────────────┐  ┌─────────────────────────────────────┐  │
│  │     ReviewerScreen      │  │        ImportHistoryScreen          │  │
│  │  - KatexMathView (stem) │  │  - Batch cards with timestamps      │  │
│  │  - Single item delete   │  │  - Raw note preview sheet           │  │
│  │  - Solution 2D formulas │  │  - "Rollback / Delete Batch" CTA    │  │
│  └────────────┬────────────┘  └──────────────────┬──────────────────┘  │
│               │                                  │                     │
│  ┌────────────┴────────────┐  ┌──────────────────┴──────────────────┐  │
│  │      KatexMathView      │  │             ImportScreen            │  │
│  │  (Hybrid Compose-WebView│  │  - AI Prompt Helper                 │  │
│  │   Offline KaTeX Engine) │  │  - Realtime Import to Batch         │  │
│  └─────────────────────────┘  └─────────────────────────────────────┘  │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────▼───────────────────────────────────┐
│                      State & Business Logic Layer                      │
│                                                                        │
│                      EceViewModel / StateFlow                          │
│     - importBatches: StateFlow<List<ImportBatchWithStats>>             │
│     - deleteImportBatch(batchId: Long)                                 │
│     - deleteQuestion(questionId: Long)                                 │
│     - importReviewText(subjectId, text) -> creates batch & questions   │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────▼───────────────────────────────────┐
│                       Data & Persistence Layer                         │
│                                                                        │
│                           AppDatabase (Room)                           │
│  ┌──────────────────────┐              ┌───────────────────────────┐   │
│  │  ImportBatchEntity   │◄────CASCADE──┤      QuestionEntity       │   │
│  │  (id, rawText, time) │              │  (id, batchId, stem...)   │   │
│  └──────────────────────┘              └─────────────┬─────────────┘   │
│                                                      │ CASCADE         │
│                                        ┌─────────────▼─────────────┐   │
│                                        │   Formula / CalcuTechnique│   │
│                                        │   (questionId, formula...)│   │
│                                        └───────────────────────────┘   │
│                                                                        │
│  Local Assets: file:///android_asset/math/ (katex.min.js, index.html) │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Model & Schema Updates

1. **`ImportBatchEntity`**:
   ```kotlin
   @Entity(
       tableName = "import_batches",
       foreignKeys = [
           ForeignKey(
               entity = SubjectEntity::class,
               parentColumns = ["id"],
               childColumns = ["subjectId"],
               onDelete = ForeignKey.CASCADE
           )
       ],
       indices = [Index(value = ["subjectId"])]
   )
   data class ImportBatchEntity(
       @PrimaryKey(autoGenerate = true) val id: Long = 0,
       val subjectId: Long,
       val rawText: String,
       val questionCount: Int,
       val formulaCount: Int,
       val calcuCount: Int,
       val createdAt: Long = System.currentTimeMillis()
   )
   ```
2. **`QuestionEntity` Update**:
   - Add `val batchId: Long? = null` with foreign key referencing `ImportBatchEntity(id)` with `onDelete = ForeignKey.CASCADE`.
   - Index on `["batchId"]`.
3. **Database Migration / Version Bump**:
   - Update `AppDatabase` version from 1 to 2 with non-destructive fallback or schema table creation for `import_batches` and column addition for `batchId`.

### Interactive Component & State Mapping

- **`KatexMathView.kt`**:
  - Encapsulates `AndroidView(factory = { WebView(...) })`.
  - Injects raw content (supporting `$$...$$`, `$...$`, `[EQ]...[/EQ]`, and bare LaTeX) into `javascript:renderMath(...)`.
  - Responds dynamically to system theme toggles (dark/light) without page reloads.
  - Automatically sizes to `document.body.scrollHeight`.
- **`ImportHistoryScreen.kt`**:
  - Displays list of `ImportBatchEntity` ordered by `createdAt DESC`.
  - Search/filter across raw text snippets.
  - Expandable drawer or modal to preview the original full text.
  - "Delete Batch" button with alert confirmation to prevent accidental loss.
- **`ReviewerScreen.kt` & Vault Screens**:
  - Replace or enhance `WordMathView` / `MathTextView` with `KatexMathView` for crisp 2D formula rendering.
  - Add single-question delete button in `ReviewerScreen` top bar / action row.
