import os
import subprocess

html_content = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Assignment 06 Report - Nursultan Maratov SE-2523</title>
<style>
    @page {
        size: A4;
        margin: 10mm 12mm 10mm 12mm;
    }
    body {
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        color: #1e293b;
        line-height: 1.35;
        font-size: 10pt;
        margin: 0;
        padding: 0;
    }
    .header-box {
        border-bottom: 2px solid #0284c7;
        padding-bottom: 6px;
        margin-bottom: 8px;
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
    }
    .inst-title {
        font-size: 8pt;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        color: #64748b;
        font-weight: 600;
    }
    h1 {
        font-size: 15pt;
        color: #0f172a;
        margin: 2px 0;
        font-weight: 700;
    }
    .sub-title {
        font-size: 9.5pt;
        color: #0284c7;
        font-weight: 600;
    }
    .meta-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        border-radius: 5px;
        padding: 5px 10px;
        margin-bottom: 10px;
        font-size: 8.5pt;
    }
    .meta-item strong {
        display: block;
        font-size: 7.5pt;
        text-transform: uppercase;
        color: #64748b;
    }
    h2 {
        font-size: 10.5pt;
        color: #0f172a;
        border-left: 3px solid #0284c7;
        padding-left: 6px;
        margin: 10px 0 4px 0;
        text-transform: uppercase;
        letter-spacing: 0.3px;
    }
    h3 {
        font-size: 9pt;
        color: #0369a1;
        margin: 6px 0 2px 0;
    }
    p {
        margin: 0 0 5px 0;
        text-align: justify;
    }
    ul, ol {
        margin: 0 0 6px 0;
        padding-left: 18px;
    }
    li {
        margin-bottom: 2px;
    }
    code {
        font-family: "JetBrains Mono", ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
        font-size: 8.2pt;
        background: #f1f5f9;
        color: #0f172a;
        padding: 1px 3px;
        border-radius: 3px;
    }
    pre {
        font-family: "JetBrains Mono", ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
        font-size: 7.2pt;
        background: #0f172a;
        color: #f8fafc;
        padding: 6px 8px;
        border-radius: 4px;
        line-height: 1.25;
        margin: 4px 0 6px 0;
        white-space: pre;
    }
    table {
        width: 100%;
        border-collapse: collapse;
        margin: 6px 0 8px 0;
        font-size: 8.2pt;
    }
    th, td {
        border: 1px solid #cbd5e1;
        padding: 4px 6px;
        text-align: left;
    }
    th {
        background: #f1f5f9;
        color: #0f172a;
        font-weight: 600;
    }
    tr:nth-child(even) {
        background: #fcfcfc;
    }
    .badge-pass {
        color: #15803d;
        font-weight: bold;
    }
    .page-break {
        page-break-before: always;
    }
    .svg-container {
        text-align: center;
        margin: 4px 0;
    }
</style>
</head>
<body>

<!-- PAGE 1 -->
<div class="header-box">
    <div>
        <div class="inst-title">Astana IT University • Software Engineering • ShP-2216</div>
        <h1>Visitor Pattern — Advanced File System</h1>
        <div class="sub-title">Assignment 06 • Technical & Architectural Defense Report</div>
    </div>
    <div style="text-align: right; font-size: 8pt; color: #64748b;">
        <strong>Format:</strong> Individual | <strong>Language:</strong> Java 17 (Maven)<br>
        <strong>Weight:</strong> 15% RK2 | <strong>Defense:</strong> Week 10
    </div>
</div>

<div class="meta-grid">
    <div class="meta-item"><strong>Student</strong>Nursultan Maratov</div>
    <div class="meta-item"><strong>Group</strong>SE-2523</div>
    <div class="meta-item"><strong>Test Suite</strong>8 / 8 Passing (100%)</div>
    <div class="meta-item"><strong>Repository</strong>github.com/Jozephjostar/...</div>
</div>

<h2>1. Purpose & Architectural Design Choices</h2>
<p>
This project implements an extensible in-memory hierarchical file system using the <strong>Visitor behavioral design pattern</strong> in Java 17. The architecture separates the composite object structure (<code>Element</code>, <code>FileNode</code>, <code>DirectoryNode</code>) from operations executed over the tree (<code>TotalSizeVisitor</code>, <code>SearchVisitor</code>, <code>StatisticsVisitor</code>).
</p>

<h3>1.1 Double Dispatch Mechanism (CLO 1)</h3>
<p>
Standard Java resolves method invocations based only on the runtime receiver type (single dispatch). The Visitor pattern implements <strong>Double Dispatch</strong> across two coordinated steps:
</p>
<ol>
    <li><strong>First Dispatch (Dynamic Polymorphism on Receiver):</strong> The client calls <code>element.accept(visitor)</code>. Java resolves <code>accept()</code> dynamically at runtime on the concrete element type (<code>FileNode</code> or <code>DirectoryNode</code>).</li>
    <li><strong>Second Dispatch (Static Overload Resolution + Dynamic Visitor Dispatch):</strong> Inside <code>accept()</code>, the element calls <code>visitor.visit(this)</code>. The compiler statically binds <code>this</code> to the corresponding overloaded signature (<code>visit(FileNode)</code> or <code>visit(DirectoryNode)</code>), while the runtime selects the concrete visitor implementation.</li>
</ol>
<p>
This completely eliminates runtime type discrimination (<code>instanceof</code>, <code>getClass()</code> branching, reflection, or downcasting).
</p>

<h3>1.2 Traversal Ownership Justification (R7)</h3>
<p>
<strong>Choice: Visitors as Traversal Owners.</strong> Each visitor controls its own child traversal order and manages contextual state:
<code>SearchVisitor</code> maintains a <code>Deque&lt;String&gt;</code> path stack to format deterministic root-relative paths (<code>docs/spec.pdf</code>); 
<code>StatisticsVisitor</code> tracks nesting depth levels (<code>currentDepth</code>, <code>maxDepth</code>); 
<code>TotalSizeVisitor</code> performs depth-first recursive accumulation.
Element nodes remain lightweight composite containers focused solely on tree structure and integrity invariants.
</p>

<h3>1.3 Controlled State & Repeated Executions (T7)</h3>
<p>
To guarantee that repeat executions do not leak or multiply state, each visitor tracks its active <code>traversalDepth</code>. Upon root entry (<code>traversalDepth == 0</code>), previous results are automatically purged via an internal <code>reset()</code> call. Running a visitor twice on the same structure yields identical, idempotent results.
</p>

<h3>1.4 Tree Integrity Safeguards (R6)</h3>
<p>
<code>DirectoryNode</code> and <code>FileNode</code> predictably reject invalid mutations: negative file sizes and blank names (<code>IllegalArgumentException</code>), duplicate sibling names (<code>IllegalArgumentException</code>), multiple parent ownership (<code>IllegalStateException</code>), and cycle-forming links detected via parent pointer chain traversal (<code>IllegalArgumentException</code>). Encapsulation is preserved by exposing children as an unmodifiable list.
</p>

<h2>2. Clean Code Practices & Design Explanation</h2>
<ul>
    <li><strong>Meaningful Naming (<code>TotalSizeVisitor</code>, <code>SearchVisitor</code>, <code>StatisticsVisitor</code>):</strong> Names clearly convey domain roles and visitor functions without cryptic abbreviations.</li>
    <li><strong>Single Responsibility Principle (Element vs. Visitor Hierarchy):</strong> Elements exclusively manage tree topology and structural invariants; all computational algorithms reside inside discrete visitor classes.</li>
    <li><strong>Small Methods (<code>FileNode.accept()</code>, <code>DirectoryNode.accept()</code>):</strong> Each method performs a single task; <code>accept()</code> is a single-line delegation (<code>visitor.visit(this);</code>).</li>
    <li><strong>Encapsulation (<code>DirectoryNode.getChildren()</code>):</strong> Internal child collections are private; public callers receive an unmodifiable view via <code>Collections.unmodifiableList()</code>.</li>
    <li><strong>Elimination of Type-Checking Chains:</strong> Zero <code>instanceof</code> or class-casting conditional chains exist; polymorphism handles element-to-operation binding cleanly.</li>
</ul>

<h3>Architectural Trade-off: Adding New Elements vs. New Operations</h3>
<p>
<strong>Adding new operations (Strength):</strong> Perfectly respects the Open/Closed Principle. Adding <code>StatisticsVisitor</code> (R8) required zero modifications to existing element classes. <br>
<strong>Adding new element types (Trade-off):</strong> Introducing a new node (e.g. <code>SymlinkNode</code>) forces modifying the <code>Visitor</code> interface (<code>void visit(SymlinkNode)</code>), requiring updates across <em>all</em> existing concrete visitor implementations.
</p>

<div class="page-break"></div>
<!-- PAGE 2 -->

<h2>3. UML Class Diagram</h2>
<div class="svg-container">
<svg width="680" height="280" viewBox="0 0 680 280" xmlns="http://www.w3.org/2000/svg" style="font-family: -apple-system, sans-serif;">
  <defs>
    <filter id="shadow" x="-5%" y="-5%" width="110%" height="110%">
      <feDropShadow dx="1" dy="2" stdDeviation="2" flood-color="#000" flood-opacity="0.08"/>
    </filter>
  </defs>

  <!-- Element Interface -->
  <g filter="url(#shadow)">
    <rect x="20" y="10" width="220" height="65" rx="5" fill="#f0f9ff" stroke="#0284c7" stroke-width="1.5"/>
    <text x="130" y="25" text-anchor="middle" font-size="8.5pt" fill="#0369a1" font-style="italic">&lt;&lt;interface&gt;&gt;</text>
    <text x="130" y="40" text-anchor="middle" font-size="10.5pt" font-weight="bold" fill="#0f172a">Element</text>
    <line x1="20" y1="46" x2="240" y2="46" stroke="#0284c7" stroke-width="1"/>
    <text x="28" y="58" font-size="8pt" fill="#334155">+ accept(Visitor): void</text>
    <text x="28" y="69" font-size="8pt" fill="#334155">+ getName(): String</text>
  </g>

  <!-- Visitor Interface -->
  <g filter="url(#shadow)">
    <rect x="420" y="10" width="240" height="65" rx="5" fill="#f0fdf4" stroke="#16a34a" stroke-width="1.5"/>
    <text x="540" y="25" text-anchor="middle" font-size="8.5pt" fill="#15803d" font-style="italic">&lt;&lt;interface&gt;&gt;</text>
    <text x="540" y="40" text-anchor="middle" font-size="10.5pt" font-weight="bold" fill="#0f172a">Visitor</text>
    <line x1="420" y1="46" x2="660" y2="46" stroke="#16a34a" stroke-width="1"/>
    <text x="428" y="58" font-size="8pt" fill="#334155">+ visit(FileNode): void</text>
    <text x="428" y="69" font-size="8pt" fill="#334155">+ visit(DirectoryNode): void</text>
  </g>

  <!-- FileNode -->
  <g filter="url(#shadow)">
    <rect x="20" y="105" width="105" height="90" rx="5" fill="#ffffff" stroke="#64748b" stroke-width="1.2"/>
    <text x="72" y="121" text-anchor="middle" font-size="9pt" font-weight="bold" fill="#0f172a">FileNode</text>
    <line x1="20" y1="126" x2="125" y2="126" stroke="#cbd5e1" stroke-width="1"/>
    <text x="26" y="139" font-size="7.2pt" fill="#475569">- name: String</text>
    <text x="26" y="150" font-size="7.2pt" fill="#475569">- size: long</text>
    <line x1="20" y1="156" x2="125" y2="156" stroke="#cbd5e1" stroke-width="1"/>
    <text x="26" y="169" font-size="7.2pt" fill="#334155">+ getSize(): long</text>
    <text x="26" y="180" font-size="7.2pt" fill="#334155">+ accept(v): void</text>
  </g>

  <!-- DirectoryNode -->
  <g filter="url(#shadow)">
    <rect x="140" y="105" width="135" height="110" rx="5" fill="#ffffff" stroke="#64748b" stroke-width="1.2"/>
    <text x="207" y="121" text-anchor="middle" font-size="9pt" font-weight="bold" fill="#0f172a">DirectoryNode</text>
    <line x1="140" y1="126" x2="275" y2="126" stroke="#cbd5e1" stroke-width="1"/>
    <text x="146" y="139" font-size="7.2pt" fill="#475569">- name: String</text>
    <text x="146" y="150" font-size="7.2pt" fill="#475569">- children: Map</text>
    <line x1="140" y1="156" x2="275" y2="156" stroke="#cbd5e1" stroke-width="1"/>
    <text x="146" y="169" font-size="7.2pt" fill="#334155">+ addChild(Element)</text>
    <text x="146" y="180" font-size="7.2pt" fill="#334155">+ removeChild(Elem)</text>
    <text x="146" y="191" font-size="7.2pt" fill="#334155">+ getChildren(): List</text>
    <text x="146" y="202" font-size="7.2pt" fill="#334155">+ accept(v): void</text>
  </g>

  <!-- TotalSizeVisitor -->
  <g filter="url(#shadow)">
    <rect x="330" y="105" width="105" height="85" rx="5" fill="#ffffff" stroke="#64748b" stroke-width="1.2"/>
    <text x="382" y="120" text-anchor="middle" font-size="8.2pt" font-weight="bold" fill="#0f172a">TotalSizeVisitor</text>
    <line x1="330" y1="125" x2="435" y2="125" stroke="#cbd5e1" stroke-width="1"/>
    <text x="336" y="138" font-size="7.2pt" fill="#475569">- totalSize: long</text>
    <line x1="330" y1="144" x2="435" y2="144" stroke="#cbd5e1" stroke-width="1"/>
    <text x="336" y="157" font-size="7.2pt" fill="#334155">+ getTotalSize()</text>
    <text x="336" y="168" font-size="7.2pt" fill="#334155">+ visit(FileNode)</text>
    <text x="336" y="179" font-size="7.2pt" fill="#334155">+ visit(DirNode)</text>
  </g>

  <!-- SearchVisitor -->
  <g filter="url(#shadow)">
    <rect x="445" y="105" width="115" height="105" rx="5" fill="#ffffff" stroke="#64748b" stroke-width="1.2"/>
    <text x="502" y="120" text-anchor="middle" font-size="8.2pt" font-weight="bold" fill="#0f172a">SearchVisitor</text>
    <line x1="445" y1="125" x2="560" y2="125" stroke="#cbd5e1" stroke-width="1"/>
    <text x="451" y="138" font-size="7.2pt" fill="#475569">- criterion: Pred</text>
    <text x="451" y="149" font-size="7.2pt" fill="#475569">- minSize: long</text>
    <line x1="445" y1="155" x2="560" y2="155" stroke="#cbd5e1" stroke-width="1"/>
    <text x="451" y="168" font-size="7.2pt" fill="#334155">+ getMatchingPaths()</text>
    <text x="451" y="179" font-size="7.2pt" fill="#334155">+ visit(FileNode)</text>
    <text x="451" y="190" font-size="7.2pt" fill="#334155">+ visit(DirNode)</text>
  </g>

  <!-- StatisticsVisitor -->
  <g filter="url(#shadow)">
    <rect x="570" y="105" width="105" height="105" rx="5" fill="#ffffff" stroke="#64748b" stroke-width="1.2"/>
    <text x="622" y="120" text-anchor="middle" font-size="8.2pt" font-weight="bold" fill="#0f172a">StatisticsVisitor</text>
    <line x1="570" y1="125" x2="675" y2="125" stroke="#cbd5e1" stroke-width="1"/>
    <text x="576" y="138" font-size="7.2pt" fill="#475569">- fileCount: int</text>
    <text x="576" y="149" font-size="7.2pt" fill="#475569">- dirCount: int</text>
    <text x="576" y="160" font-size="7.2pt" fill="#475569">- maxDepth: int</text>
    <line x1="570" y1="165" x2="675" y2="165" stroke="#cbd5e1" stroke-width="1"/>
    <text x="576" y="178" font-size="7.2pt" fill="#334155">+ getFileCount()</text>
    <text x="576" y="189" font-size="7.2pt" fill="#334155">+ getDirCount()</text>
    <text x="576" y="200" font-size="7.2pt" fill="#334155">+ getMaxDepth()</text>
  </g>

  <!-- Connectors -->
  <line x1="72" y1="105" x2="72" y2="85" stroke="#0284c7" stroke-width="1.2" stroke-dasharray="3,3"/>
  <line x1="72" y1="85" x2="100" y2="85" stroke="#0284c7" stroke-width="1.2" stroke-dasharray="3,3"/>
  <line x1="207" y1="105" x2="207" y2="85" stroke="#0284c7" stroke-width="1.2" stroke-dasharray="3,3"/>
  <line x1="207" y1="85" x2="160" y2="85" stroke="#0284c7" stroke-width="1.2" stroke-dasharray="3,3"/>
  <line x1="130" y1="85" x2="130" y2="75" stroke="#0284c7" stroke-width="1.2"/>
  <polygon points="130,75 125,82 135,82" fill="#ffffff" stroke="#0284c7" stroke-width="1.2"/>

  <line x1="382" y1="105" x2="382" y2="85" stroke="#16a34a" stroke-width="1.2" stroke-dasharray="3,3"/>
  <line x1="502" y1="105" x2="502" y2="85" stroke="#16a34a" stroke-width="1.2" stroke-dasharray="3,3"/>
  <line x1="622" y1="105" x2="622" y2="85" stroke="#16a34a" stroke-width="1.2" stroke-dasharray="3,3"/>
  <line x1="382" y1="85" x2="622" y2="85" stroke="#16a34a" stroke-width="1.2" stroke-dasharray="3,3"/>
  <line x1="540" y1="85" x2="540" y2="75" stroke="#16a34a" stroke-width="1.2"/>
  <polygon points="540,75 535,82 545,82" fill="#ffffff" stroke="#16a34a" stroke-width="1.2"/>

  <!-- Directory child loop -->
  <path d="M 275,150 C 295,150 295,25 240,38" fill="none" stroke="#64748b" stroke-width="1.1"/>
  <polygon points="275,150 281,147 287,150 281,153" fill="#ffffff" stroke="#64748b" stroke-width="1.1"/>
  <text x="296" y="90" font-size="7pt" fill="#64748b">children *</text>

  <!-- Visitor to Element dependency -->
  <line x1="240" y1="40" x2="420" y2="40" stroke="#94a3b8" stroke-width="1.1" stroke-dasharray="3,3"/>
  <text x="330" y="35" text-anchor="middle" font-size="7.5pt" fill="#64748b">double dispatch</text>
</svg>
</div>

<h2>4. Sample Program Execution Output</h2>
<p>Executed via <code>mvn exec:java</code> against a deterministic multi-level project tree:</p>
<pre>
================================================================================
   ASSIGNMENT 06: VISITOR PATTERN - ADVANCED FILE SYSTEM DEMONSTRATION
================================================================================
--- 1. FILE SYSTEM STRUCTURE VISUALIZATION ---
└── [DIR]  university-project/
    ├── [FILE] README.md (850 bytes)
    ├── [FILE] pom.xml (2400 bytes)
    └── [DIR]  docs/
        ├── [FILE] specification.pdf (45000 bytes)
        ├── [FILE] architecture.md (3200 bytes)
    └── [DIR]  src/
        ├── [FILE] Main.java (4800 bytes)
        ├── [FILE] Controller.java (1850 bytes)
        └── [DIR]  assets/
            ├── [FILE] logo.png (15400 bytes)

--- 2. OPERATION 1: TOTAL SIZE VISITOR (R3) ---
Total file size for entire tree ('university-project'): 73500 bytes | Subtree ('src'): 22050 bytes

--- 3. OPERATION 2: SEARCH VISITOR (R4, Deterministic & Root-Relative) ---
Search A: Files containing '.java' with size >= 2,000 bytes -> src/Main.java
Search B: All markdown files ('.md') with size >= 0 bytes   -> README.md, docs/architecture.md
Search C: Non-matching query ('non_existent')              -> (No matching files found)

--- 4. OPERATION 3: STATISTICS VISITOR (R5, Extensible OCP Operation) ---
Subtree statistics for root: 7 files, 4 directories (including root), maximum tree depth = 3

--- 5. REUSABILITY & REPEAT RUN VERIFICATION (T7) ---
Re-running TotalSizeVisitor: 73500 bytes (No leakage/doubling)
Re-running StatisticsVisitor: 7 files, 4 dirs, depth 3 (No multiplied counters)

--- 6. TREE INTEGRITY & INVARIANT VALIDATION (R6, T8) ---
  [PASS] Reject Negative File Size: Successfully caught IllegalArgumentException
  [PASS] Reject Duplicate Sibling Name: Successfully caught IllegalArgumentException
  [PASS] Reject Multiple Parent Ownership: Successfully caught IllegalStateException
  [PASS] Reject Cycle (Self-Addition): Successfully caught IllegalArgumentException
  [PASS] Reject Cycle (Ancestor to Descendant): Successfully caught IllegalArgumentException
================================================================================
</pre>

<div class="page-break"></div>
<!-- PAGE 3 -->

<h2>5. Automated Test Suite Summary (JUnit 5)</h2>
<p>
The test suite in <code>kz.aitu.visitor.FileSystemVisitorTest</code> executes via <code>mvn test</code>, validating all eight mandatory checkpoints:
</p>

<table>
    <thead>
        <tr>
            <th style="width: 8%;">Check</th>
            <th style="width: 25%;">Automated Target</th>
            <th style="width: 53%;">Verification & Assertion Detail</th>
            <th style="width: 14%;">Status</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><strong>T1</strong></td>
            <td>Overloaded visit dispatch</td>
            <td>Verifies <code>FileNode</code> and <code>DirectoryNode</code> dispatch to the correct overloaded visit methods.</td>
            <td><span class="badge-pass">PASS ✅</span></td>
        </tr>
        <tr>
            <td><strong>T2</strong></td>
            <td>Size accumulation</td>
            <td>Calculates 25,500 bytes across 3 nesting levels; validates subtree (13,000 bytes) and leaf.</td>
            <td><span class="badge-pass">PASS ✅</span></td>
        </tr>
        <tr>
            <td><strong>T3</strong></td>
            <td>Combined search criteria</td>
            <td>Combines filename filter (<code>.java</code>) and threshold (<code>&gt;= 2000</code>); correctly includes/excludes.</td>
            <td><span class="badge-pass">PASS ✅</span></td>
        </tr>
        <tr>
            <td><strong>T4</strong></td>
            <td>Deterministic root-relative paths</td>
            <td>Validates <code>/</code> separator, root-relative path format, and lexicographical sorting.</td>
            <td><span class="badge-pass">PASS ✅</span></td>
        </tr>
        <tr>
            <td><strong>T5</strong></td>
            <td>Statistics metrics</td>
            <td>Confirms 5 files, 4 directories (including root), and maximum depth of 3 (root depth = 0).</td>
            <td><span class="badge-pass">PASS ✅</span></td>
        </tr>
        <tr>
            <td><strong>T6</strong></td>
            <td>Empty structures & edge cases</td>
            <td>Validates empty dir (size 0, 0 files, 1 dir, depth 0) and non-matching query (empty list).</td>
            <td><span class="badge-pass">PASS ✅</span></td>
        </tr>
        <tr>
            <td><strong>T7</strong></td>
            <td>Controlled state reusability</td>
            <td>Repeated runs on the same visitor instance do not leak, double, or corrupt accumulated state.</td>
            <td><span class="badge-pass">PASS ✅</span></td>
        </tr>
        <tr>
            <td><strong>T8</strong></td>
            <td>Tree integrity & invariants</td>
            <td>Rejects negative sizes, blank names, duplicate siblings, multi-parenting, and cycles.</td>
            <td><span class="badge-pass">PASS ✅</span></td>
        </tr>
    </tbody>
</table>

<p>
<strong>Test Runner Output:</strong> <code>Tests run: 8, Failures: 0, Errors: 0, Skipped: 0. BUILD SUCCESS (1.079 s)</code>
</p>

<h2>6. Individual Defense Preparation & Verification Checklist</h2>
<ul>
    <li><strong>Step 1 — Trace Two Dispatch Steps:</strong>
        <ol>
            <li><code>element.accept(v)</code>: dynamic dispatch in <code>FileNode.java</code> (line 52) or <code>DirectoryNode.java</code> (line 152).</li>
            <li><code>v.visit(this)</code>: static compile-time overload binding to <code>visit(FileNode)</code> or <code>visit(DirectoryNode)</code> + dynamic runtime dispatch on concrete visitor.</li>
        </ol>
    </li>
    <li><strong>Step 2 — Run Full Test Suite:</strong> Execute <code>mvn test</code> from the project root; all 8 test cases pass synchronously without IDE dependency.</li>
    <li><strong>Step 3 — Input/Search Configuration Change:</strong> Demonstrate how changing search predicate or minimum size threshold instantly filters outputs in <code>Main.java</code> or <code>SearchVisitor</code>.</li>
    <li><strong>Step 4 — Architectural Trade-off Defense:</strong> Explain that adding operations adheres to OCP (proven by Commit <code>cc6eaf6</code> adding <code>StatisticsVisitor</code> with zero element changes), while adding new element types violates OCP by forcing updates across all visitor implementations.</li>
</ul>

<h2>7. GitHub Repository Link</h2>
<p>
The complete source code, commit history showing progressive visitor addition, and tests are available at:<br>
<strong>GitHub Repository:</strong> <a href="https://github.com/Jozephjostar/Assignment6_Visitor_SE2523_Maratov_Nursultan.git">https://github.com/Jozephjostar/Assignment6_Visitor_SE2523_Maratov_Nursultan.git</a>
</p>

<div style="margin-top: 24px; padding-top: 6px; border-top: 1px solid #cbd5e1; font-size: 7.5pt; color: #64748b; text-align: center;">
Astana IT University • Department of Computer Science • Software Design Patterns (ShP-2216) • Assignment 06 Report
</div>

</body>
</html>
"""

base_dir = os.path.dirname(os.path.abspath(__file__))
html_path = os.path.join(base_dir, "report.html")
pdf_path = os.path.join(base_dir, "REPORT_Assignment6_Visitor.pdf")

with open(html_path, "w", encoding="utf-8") as f:
    f.write(html_content)

chrome_cmd = [
    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
    "--headless",
    "--disable-gpu",
    "--no-pdf-header-footer",
    f"--print-to-pdf={pdf_path}",
    html_path
]

res = subprocess.run(chrome_cmd, capture_output=True, text=True)
print("Chrome returncode:", res.returncode)
if os.path.exists(pdf_path):
    print("PDF successfully generated at:", pdf_path, "Size:", os.path.getsize(pdf_path))
