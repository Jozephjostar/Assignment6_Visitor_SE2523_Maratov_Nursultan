# Assignment 06: Visitor Pattern — Advanced File System

**Course:** ShP-2216 – Software Design Patterns  
**Programme:** 6B06102 – Software Engineering, 2nd Year  
**Institution:** Astana IT University  
**Student:** Nursultan Maratov (Group SE-2523)  
**Format:** Individual | Java 17 | Maven | JUnit 5  

---

## 📌 Project Overview

This project implements an **in-memory hierarchical file system** adhering to the **Visitor behavioral design pattern** in Java 17. The design decouples the hierarchical object structure (`Element`, `FileNode`, `DirectoryNode`) from operations performed across that structure, enabling independent operations without altering element classes (adhering to the **Open/Closed Principle**).

Three independent operations are implemented:
1. **`TotalSizeVisitor` (R3):** Computes cumulative byte sizes across subtrees; directories contribute zero bytes, files contribute exactly once.
2. **`SearchVisitor` (R4):** Evaluates a configurable filename criterion combined with an inclusive minimum byte-size threshold (`size >= threshold`), returning deterministic, lexicographically sorted, root-relative paths using `/` as separator.
3. **`StatisticsVisitor` (R5, R8):** Extensible operation added without modifying the element hierarchy, collecting total file count, directory count (including visited root), and maximum tree depth (root depth = 0).

---

## 🏗 Architecture & Project Structure

```text
Assignment6_Visitor_SE2523_Maratov_Nursultan/
├── pom.xml                                        # Maven configuration (Java 17, JUnit 5)
├── README.md                                      # Project guide and instructions
├── REPORT.md                                      # Assignment report (Markdown)
├── REPORT_Assignment6_Visitor.pdf                 # Submission-ready 3-page PDF report
└── src/
    ├── main/
    │   └── java/
    │       └── kz/aitu/visitor/
    │           ├── Main.java                      # Deterministic CLI demonstration
    │           ├── model/
    │           │   ├── Element.java               # Common element interface (Object Structure)
    │           │   ├── FileNode.java              # Concrete leaf element (name, byte size)
    │           │   └── DirectoryNode.java         # Composite element with tree integrity
    │           └── visitor/
    │               ├── Visitor.java               # Visitor interface declaring visit overloads
    │               ├── TotalSizeVisitor.java      # Operation 1: Recursive size accumulator
    │               ├── SearchVisitor.java         # Operation 2: Filtered path finder
    │               └── StatisticsVisitor.java     # Operation 3: Extensible tree metrics
    └── test/
        └── java/
            └── kz/aitu/visitor/
                └── FileSystemVisitorTest.java     # Full JUnit 5 test suite (T1 - T8)
```

---

## 🔄 Double Dispatch Mechanism (CLO 1)

Double dispatch resolves the executed method at runtime based on the dynamic types of **both** the receiver element and the passed visitor:

1. **First Dispatch (Runtime Dynamic Polymorphism):**  
   The client invokes `element.accept(visitor)`. Java resolves `accept()` dynamically on the runtime type of `element` (`FileNode` or `DirectoryNode`).
2. **Second Dispatch (Static Overload Resolution + Dynamic Visitor Polymorphism):**  
   Inside `accept()`, the element calls `visitor.visit(this)`. At compile time, Java statically resolves the overload `visit(FileNode)` or `visit(DirectoryNode)` because `this` has the concrete type of the enclosing class. At runtime, dynamic dispatch directs the call to the concrete `Visitor` implementation (`TotalSizeVisitor`, `SearchVisitor`, or `StatisticsVisitor`).

```text
[Client]
   │
   │ 1. element.accept(visitor) [Dynamic dispatch on Element]
   ▼
[FileNode / DirectoryNode]
   │
   │ 2. visitor.visit(this)     [Compile-time overload on 'this' + Dynamic dispatch on Visitor]
   ▼
[Concrete Visitor (TotalSizeVisitor / SearchVisitor / StatisticsVisitor)]
```

---

## 🧭 Traversal Ownership Justification (R7)

**Choice:** **Visitors as Traversal Owners**.

**Justification:**
1. **Context-Specific Traversal:** Each visitor maintains specific state during tree traversal. For example:
   - `SearchVisitor` requires a path stack (`Deque<String> currentPathSegments`) to construct root-relative paths (`docs/spec.pdf`), pushing directory names on entry and popping on exit.
   - `StatisticsVisitor` requires tracking nesting depth levels (`currentDepth`, `maxDepth`), incrementing when entering directories and decrementing upon exit.
   - `TotalSizeVisitor` requires a simple recursive child accumulation.
2. **Purity of Element Invariants:** Elements (`FileNode`, `DirectoryNode`) remain lightweight structural containers focused solely on tree topology and data storage. They do not dictate operational traversal algorithms.
3. **Double Dispatch Fidelity:** `DirectoryNode.accept(visitor)` purely executes `visitor.visit(this)`. The visitor decides when and how to visit children (`child.accept(this)`), visiting each reachable node exactly once per operation.

---

## 🛡 Tree Integrity & Invariant Protection (R6)

The `DirectoryNode` and `FileNode` enforce strict structural integrity invariants:
* **Negative Size Prevention:** `FileNode` rejects `size < 0` via `IllegalArgumentException`.
* **Blank/Invalid Names:** Rejects `null`, blank, or strings containing `/` via `IllegalArgumentException`.
* **Duplicate Sibling Names:** `DirectoryNode.addChild()` rejects elements sharing a name with existing children via `IllegalArgumentException`.
* **Multiple Parent Ownership:** Rejects adding an element already linked to another directory via `IllegalStateException`. (Elements must be explicitly removed/detached first).
* **Cycle Prevention:** Walks the `parent` reference chain upwards before insertion; rejects self-addition or adding any ancestor as a child via `IllegalArgumentException`.
* **Encapsulation:** `DirectoryNode.getChildren()` returns an unmodifiable list (`Collections.unmodifiableList`), preventing external modification of internal collections.

---

## 🧹 Clean Code Practices (Section 6)

1. **Meaningful Naming (`TotalSizeVisitor`, `SearchVisitor`, `StatisticsVisitor`, `FileNode`):**  
   Class and method names directly reflect ubiquitous file system concepts and design pattern roles without abbreviations or vague terminology.
2. **Single Responsibility Principle (`Element` hierarchy vs. `Visitor` hierarchy):**  
   Node classes are strictly responsible for hierarchical structure and integrity invariants; all business calculations, filtering, and reporting are encapsulated within isolated visitors.
3. **Small, Focused Methods (`accept()`, `reset()`, `buildRelativePath()`):**  
   Every method executes a single logical task; `accept()` is a single-line delegation (`visitor.visit(this);`), reducing cognitive complexity.
4. **Encapsulation (`DirectoryNode.getChildren()`):**  
   Internal mutable child maps are hidden; public accessors return unmodifiable collections, preserving internal directory invariants through the public API.
5. **Elimination of Type-Checking Chains (No `instanceof` / `getClass`):**  
   Imperative type-checking conditional ladders (`if (node instanceof FileNode) ...`) are completely eradicated through double dispatch polymorphism.

### ⚖️ Architectural Trade-off: Introducing a New Element Type
* **Strength of Visitor:** Adding **new operations** (e.g., `StatisticsVisitor`, `CompressVisitor`) is seamless (Open/Closed Principle) and requires zero modifications to existing element classes.
* **Trade-off/Weakness:** Adding a **new element type** (e.g., `SymlinkNode` or `DeviceNode`) requires modifying the `Visitor` interface (`void visit(SymlinkNode node)`) and consequently updating **every** existing concrete visitor implementation (`TotalSizeVisitor`, `SearchVisitor`, `StatisticsVisitor`), violating the Open/Closed Principle with respect to element types.

---

## 🚀 How to Build, Test, and Run

### Prerequisites
* Java 17+ (JDK 17 or higher)
* Apache Maven 3.8+

### 1. Compile the Project
```bash
mvn clean compile
```

### 2. Run Automated Verification Tests (JUnit 5)
```bash
mvn test
```

### 3. Run the Demonstration Application
```bash
mvn exec:java
```

---

## 🧪 Verification Checkpoints Mapping (Section 5)

| Checkpoint | Requirement | Automated Test in `FileSystemVisitorTest` | Status |
|---|---|---|---|
| **T1** | Overloaded visit dispatch | `testDoubleDispatchResolution()` | PASS ✅ |
| **T2** | Multi-level size accumulation | `testTotalSizeVisitorNesting()` | PASS ✅ |
| **T3** | Combined search criteria | `testSearchCombinesConditions()` | PASS ✅ |
| **T4** | Deterministic root-relative paths | `testSearchDeterministicRootRelativePaths()` | PASS ✅ |
| **T5** | Statistics counting & max depth | `testStatisticsVisitorAccuracy()` | PASS ✅ |
| **T6** | Empty structures & no-match cases | `testEmptyStructuresAndNoMatchCases()` | PASS ✅ |
| **T7** | State reuse & no leak/multiplication | `testControlledStateAndRepeatedExecutions()` | PASS ✅ |
| **T8** | Tree integrity & mutation rejection | `testTreeIntegrityConstraints()` | PASS ✅ |

---

## 📜 Git Incremental Commits History

The repository maintains an incremental commit history documenting the implementation and the subsequent extension of the third visitor (`StatisticsVisitor`) without element modifications:

```text
103b9d4 feat: implement deterministic CLI demonstration in Main
e06a1ff test: add comprehensive JUnit 5 tests covering all checkpoints (T1-T8)
cc6eaf6 feat: add StatisticsVisitor without modifying element hierarchy (OCP extension)
c40e751 feat: implement TotalSizeVisitor and SearchVisitor
09e0c89 feat: implement concrete elements FileNode and DirectoryNode with tree integrity validation
0fe3ad3 feat: define Element hierarchy and Visitor interface contracts
d80e842 chore: initialize Maven project structure with Java 17
```
