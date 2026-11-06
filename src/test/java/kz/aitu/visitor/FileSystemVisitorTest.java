package kz.aitu.visitor;

import kz.aitu.visitor.model.DirectoryNode;
import kz.aitu.visitor.model.Element;
import kz.aitu.visitor.model.FileNode;
import kz.aitu.visitor.visitor.SearchVisitor;
import kz.aitu.visitor.visitor.StatisticsVisitor;
import kz.aitu.visitor.visitor.TotalSizeVisitor;
import kz.aitu.visitor.visitor.Visitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite verifying the Visitor Pattern implementation.
 * Covers all mandatory verification checkpoints T1 through T8 from the assignment specification.
 */
public class FileSystemVisitorTest {

    private DirectoryNode root;
    private DirectoryNode docs;
    private DirectoryNode src;
    private DirectoryNode images;

    private FileNode readme;
    private FileNode appJava;
    private FileNode utilsJava;
    private FileNode logoPng;
    private FileNode specPdf;

    @BeforeEach
    void setUp() {
        /*
         * Construct a realistic, multi-level file system hierarchy:
         *
         * root/ (depth 0)
         * ├── README.md (500 bytes)
         * ├── docs/ (depth 1)
         * │   └── spec.pdf (12000 bytes)
         * └── src/ (depth 1)
         *     ├── App.java (3500 bytes)
         *     ├── Utils.java (1500 bytes)
         *     └── images/ (depth 2)
         *         └── logo.png (8000 bytes)
         */
        root = new DirectoryNode("root");
        docs = new DirectoryNode("docs");
        src = new DirectoryNode("src");
        images = new DirectoryNode("images");

        readme = new FileNode("README.md", 500);
        specPdf = new FileNode("spec.pdf", 12000);
        appJava = new FileNode("App.java", 3500);
        utilsJava = new FileNode("Utils.java", 1500);
        logoPng = new FileNode("logo.png", 8000);

        src.addChild(appJava);
        src.addChild(utilsJava);
        src.addChild(images);
        images.addChild(logoPng);

        docs.addChild(specPdf);

        root.addChild(readme);
        root.addChild(docs);
        root.addChild(src);
    }

    // =========================================================================
    // T1: Both element types reach the correct overloaded visit methods.
    // =========================================================================

    @Test
    @DisplayName("T1: Both element types reach the correct overloaded visit methods via double dispatch")
    void testDoubleDispatchResolution() {
        List<String> visitedOrder = new ArrayList<>();

        Visitor recordingVisitor = new Visitor() {
            @Override
            public void visit(FileNode file) {
                visitedOrder.add("FILE:" + file.getName());
            }

            @Override
            public void visit(DirectoryNode directory) {
                visitedOrder.add("DIR:" + directory.getName());
            }
        };

        // Dispatch on FileNode
        Element fileElement = new FileNode("sample.txt", 100);
        fileElement.accept(recordingVisitor);

        // Dispatch on DirectoryNode
        Element dirElement = new DirectoryNode("sampleDir");
        dirElement.accept(recordingVisitor);

        assertEquals(2, visitedOrder.size());
        assertEquals("FILE:sample.txt", visitedOrder.get(0));
        assertEquals("DIR:sampleDir", visitedOrder.get(1));
    }

    // =========================================================================
    // T2: Size accumulation across multiple nesting levels is correct.
    // =========================================================================

    @Test
    @DisplayName("T2: Size accumulation across multiple nesting levels is correct")
    void testTotalSizeVisitorNesting() {
        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);

        // Expected total: 500 (README) + 12000 (spec) + 3500 (App) + 1500 (Utils) + 8000 (logo) = 25500
        long expectedTotal = 500 + 12000 + 3500 + 1500 + 8000;
        assertEquals(expectedTotal, sizeVisitor.getTotalSize());

        // Test subtree size (src subtree only: App + Utils + logo = 3500 + 1500 + 8000 = 13000)
        TotalSizeVisitor subtreeVisitor = new TotalSizeVisitor();
        src.accept(subtreeVisitor);
        assertEquals(13000, subtreeVisitor.getTotalSize());

        // Test single file node size
        TotalSizeVisitor singleFileVisitor = new TotalSizeVisitor();
        specPdf.accept(singleFileVisitor);
        assertEquals(12000, singleFileVisitor.getTotalSize());
    }

    // =========================================================================
    // T3: Search correctly combines both configurable filter conditions.
    // =========================================================================

    @Test
    @DisplayName("T3: Search correctly combines both configurable filter conditions (name AND size >= threshold)")
    void testSearchCombinesConditions() {
        // Condition: name contains ".java" AND size >= 2000
        // App.java: 3500 bytes (matches name AND size >= 2000) -> INCLUDED
        // Utils.java: 1500 bytes (matches name, but size < 2000) -> EXCLUDED
        // spec.pdf: 12000 bytes (size >= 2000, but name doesn't match) -> EXCLUDED
        SearchVisitor visitor = new SearchVisitor(".java", 2000);
        root.accept(visitor);

        List<String> results = visitor.getMatchingPaths();
        assertEquals(1, results.size());
        assertEquals("src/App.java", results.get(0));

        // Predicate-based criterion: end with ".png" AND size >= 5000
        SearchVisitor pngVisitor = new SearchVisitor(name -> name.endsWith(".png"), 5000);
        root.accept(pngVisitor);
        assertEquals(List.of("src/images/logo.png"), pngVisitor.getMatchingPaths());

        // Lower threshold to 1000 for .java: both App.java and Utils.java should match
        SearchVisitor bothJavaVisitor = new SearchVisitor(".java", 1000);
        root.accept(bothJavaVisitor);
        assertEquals(List.of("src/App.java", "src/Utils.java"), bothJavaVisitor.getMatchingPaths());
    }

    // =========================================================================
    // T4: Search produces correct, deterministic, root-relative paths.
    // =========================================================================

    @Test
    @DisplayName("T4: Search produces correct, deterministic, root-relative paths sorted lexicographically")
    void testSearchDeterministicRootRelativePaths() {
        // Search all files with size >= 0 (matches all 5 files in tree)
        SearchVisitor searchAll = new SearchVisitor("", 0);
        root.accept(searchAll);

        List<String> results = searchAll.getMatchingPaths();

        List<String> expectedPaths = List.of(
                "README.md",
                "docs/spec.pdf",
                "src/App.java",
                "src/Utils.java",
                "src/images/logo.png"
        );

        assertEquals(expectedPaths, results);

        // Verify '/' separator is used on all nested paths
        for (String path : results) {
            assertFalse(path.contains("\\"), "Path must use '/' as separator: " + path);
            assertFalse(path.startsWith("/"), "Path must be root-relative without leading slash: " + path);
        }

        // Subtree relative paths: search under 'src'
        SearchVisitor searchSrc = new SearchVisitor("", 0);
        src.accept(searchSrc);
        List<String> srcResults = searchSrc.getMatchingPaths();
        assertEquals(List.of("App.java", "Utils.java", "images/logo.png"), srcResults);
    }

    // =========================================================================
    // T5: Statistics are correct for nesting, counting and maximum depth.
    // =========================================================================

    @Test
    @DisplayName("T5: Statistics are correct for nesting, counting, and maximum depth (root depth = 0)")
    void testStatisticsVisitorAccuracy() {
        StatisticsVisitor stats = new StatisticsVisitor();
        root.accept(stats);

        // Root directory + docs + src + images = 4 directories
        assertEquals(4, stats.getDirectoryCount(), "Directory count must include root when root is a directory");

        // 5 files total (README, spec, App, Utils, logo)
        assertEquals(5, stats.getFileCount(), "File count must reflect all visited files");

        // Maximum depth:
        // root (0) -> src (1) -> images (2) -> logo.png (3)
        // Root depth is 0, so maximum depth is 3.
        assertEquals(3, stats.getMaxDepth(), "Maximum depth must equal 3 for logo.png");

        // Test statistics on a subtree: 'docs' directory
        StatisticsVisitor docsStats = new StatisticsVisitor();
        docs.accept(docsStats);
        assertEquals(1, docsStats.getDirectoryCount(), "docs dir is 1 directory");
        assertEquals(1, docsStats.getFileCount(), "spec.pdf is 1 file");
        assertEquals(1, docsStats.getMaxDepth(), "docs (0) -> spec.pdf (1) has max depth 1");

        // Test statistics directly on a FileNode root
        StatisticsVisitor singleFileStats = new StatisticsVisitor();
        readme.accept(singleFileStats);
        assertEquals(0, singleFileStats.getDirectoryCount());
        assertEquals(1, singleFileStats.getFileCount());
        assertEquals(0, singleFileStats.getMaxDepth(), "Single root file has depth 0");
    }

    // =========================================================================
    // T6: Empty structures and no-match search cases are handled.
    // =========================================================================

    @Test
    @DisplayName("T6: Empty structures and no-match search cases are handled gracefully")
    void testEmptyStructuresAndNoMatchCases() {
        DirectoryNode emptyDir = new DirectoryNode("empty");

        // Size of empty directory is 0
        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        emptyDir.accept(sizeVisitor);
        assertEquals(0, sizeVisitor.getTotalSize());

        // Statistics of empty directory: 0 files, 1 directory (itself), depth 0
        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        emptyDir.accept(statsVisitor);
        assertEquals(0, statsVisitor.getFileCount());
        assertEquals(1, statsVisitor.getDirectoryCount());
        assertEquals(0, statsVisitor.getMaxDepth());

        // Search in empty directory returns empty list
        SearchVisitor searchEmpty = new SearchVisitor("", 0);
        emptyDir.accept(searchEmpty);
        assertTrue(searchEmpty.getMatchingPaths().isEmpty());

        // Search with non-matching criterion in populated tree returns empty list
        SearchVisitor noMatch = new SearchVisitor("nonexistent_file_name_12345", 0);
        root.accept(noMatch);
        assertTrue(noMatch.getMatchingPaths().isEmpty());

        // Search with size threshold higher than any file returns empty list
        SearchVisitor highThreshold = new SearchVisitor("", 100_000);
        root.accept(highThreshold);
        assertTrue(highThreshold.getMatchingPaths().isEmpty());
    }

    // =========================================================================
    // T7: Repeat executions do not leak or multiply accumulated state.
    // =========================================================================

    @Test
    @DisplayName("T7: Repeat executions do not leak or multiply accumulated state")
    void testControlledStateAndRepeatedExecutions() {
        // 1. TotalSizeVisitor repeated runs
        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);
        long firstRunSize = sizeVisitor.getTotalSize();

        // Run again on same tree: should produce identical result, NOT 2x
        root.accept(sizeVisitor);
        assertEquals(firstRunSize, sizeVisitor.getTotalSize(), "Second run must not multiply size");

        // Run third time after manual reset
        sizeVisitor.reset();
        root.accept(sizeVisitor);
        assertEquals(firstRunSize, sizeVisitor.getTotalSize());

        // Run same visitor on a smaller subtree: should not retain root's state
        docs.accept(sizeVisitor);
        assertEquals(12000, sizeVisitor.getTotalSize(), "Subtree run must reflect only subtree size");

        // 2. SearchVisitor repeated runs
        SearchVisitor searchVisitor = new SearchVisitor(".java", 0);
        root.accept(searchVisitor);
        List<String> firstSearch = searchVisitor.getMatchingPaths();

        root.accept(searchVisitor);
        List<String> secondSearch = searchVisitor.getMatchingPaths();
        assertEquals(firstSearch, secondSearch, "Second search must not duplicate matching entries");

        // 3. StatisticsVisitor repeated runs
        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        root.accept(statsVisitor);
        assertEquals(5, statsVisitor.getFileCount());
        assertEquals(4, statsVisitor.getDirectoryCount());
        assertEquals(3, statsVisitor.getMaxDepth());

        root.accept(statsVisitor);
        assertEquals(5, statsVisitor.getFileCount(), "Repeated run must not accumulate file count");
        assertEquals(4, statsVisitor.getDirectoryCount(), "Repeated run must not accumulate dir count");
        assertEquals(3, statsVisitor.getMaxDepth(), "Repeated run must not accumulate depth");
    }

    // =========================================================================
    // T8: Invalid tree mutations are rejected while valid nesting remains supported.
    // =========================================================================

    @Test
    @DisplayName("T8: Invalid tree mutations are rejected predictably while valid nesting remains supported")
    void testTreeIntegrityConstraints() {
        // 1. Reject negative file sizes
        assertThrows(IllegalArgumentException.class, () -> new FileNode("invalid.dat", -1),
                "Negative file sizes must be rejected");

        // 2. Reject blank or null names
        assertThrows(IllegalArgumentException.class, () -> new FileNode("", 100));
        assertThrows(IllegalArgumentException.class, () -> new FileNode("   ", 100));
        assertThrows(IllegalArgumentException.class, () -> new FileNode(null, 100));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(""));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(null));

        // 3. Reject names containing path separator '/'
        assertThrows(IllegalArgumentException.class, () -> new FileNode("a/b.txt", 100));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode("sub/dir"));

        // 4. Reject duplicate sibling names
        DirectoryNode testDir = new DirectoryNode("testDir");
        testDir.addChild(new FileNode("item.txt", 100));
        assertThrows(IllegalArgumentException.class, () -> testDir.addChild(new FileNode("item.txt", 200)),
                "Duplicate sibling file name must be rejected");
        assertThrows(IllegalArgumentException.class, () -> testDir.addChild(new DirectoryNode("item.txt")),
                "Duplicate sibling directory name matching existing file must be rejected");

        // 5. Reject multiple parent ownership
        DirectoryNode otherDir = new DirectoryNode("otherDir");
        FileNode sharedFile = new FileNode("shared.txt", 50);
        testDir.addChild(sharedFile);
        assertThrows(IllegalStateException.class, () -> otherDir.addChild(sharedFile),
                "Adding an element already owned by another parent must throw IllegalStateException");

        // Detach element cleanly, then re-adding should succeed
        assertTrue(testDir.removeChild(sharedFile));
        assertNull(sharedFile.getParent());
        assertDoesNotThrow(() -> otherDir.addChild(sharedFile),
                "Once detached, element can be safely adopted by a new parent");

        // 6. Reject cycles: self-addition
        assertThrows(IllegalArgumentException.class, () -> testDir.addChild(testDir),
                "Adding directory to itself must be rejected as a cycle");

        // 7. Reject cycles: ancestor addition (A -> B -> C, then C.addChild(A))
        DirectoryNode a = new DirectoryNode("A");
        DirectoryNode b = new DirectoryNode("B");
        DirectoryNode c = new DirectoryNode("C");
        a.addChild(b);
        b.addChild(c);

        assertThrows(IllegalArgumentException.class, () -> c.addChild(a),
                "Adding an ancestor to a descendant must be rejected as a cycle");
        assertThrows(IllegalArgumentException.class, () -> c.addChild(b),
                "Adding direct parent to child must be rejected as a cycle");

        // 8. Encapsulation: internal collection is unmodifiable
        List<Element> children = testDir.getChildren();
        assertThrows(UnsupportedOperationException.class, () -> children.add(new FileNode("hack.txt", 1)),
                "Directory children list must be unmodifiable to protect encapsulation");
    }
}
