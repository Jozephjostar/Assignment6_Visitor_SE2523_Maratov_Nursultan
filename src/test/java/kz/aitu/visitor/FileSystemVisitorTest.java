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

        Element fileElement = new FileNode("sample.txt", 100);
        fileElement.accept(recordingVisitor);

        Element dirElement = new DirectoryNode("sampleDir");
        dirElement.accept(recordingVisitor);

        assertEquals(2, visitedOrder.size());
        assertEquals("FILE:sample.txt", visitedOrder.get(0));
        assertEquals("DIR:sampleDir", visitedOrder.get(1));
    }

    @Test
    @DisplayName("T2: Size accumulation across multiple nesting levels is correct")
    void testTotalSizeVisitorNesting() {
        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);

        long expectedTotal = 500 + 12000 + 3500 + 1500 + 8000;
        assertEquals(expectedTotal, sizeVisitor.getTotalSize());

        TotalSizeVisitor subtreeVisitor = new TotalSizeVisitor();
        src.accept(subtreeVisitor);
        assertEquals(13000, subtreeVisitor.getTotalSize());

        TotalSizeVisitor singleFileVisitor = new TotalSizeVisitor();
        specPdf.accept(singleFileVisitor);
        assertEquals(12000, singleFileVisitor.getTotalSize());
    }

    @Test
    @DisplayName("T3: Search correctly combines both configurable filter conditions (name AND size >= threshold)")
    void testSearchCombinesConditions() {
        SearchVisitor visitor = new SearchVisitor(".java", 2000);
        root.accept(visitor);

        List<String> results = visitor.getMatchingPaths();
        assertEquals(1, results.size());
        assertEquals("src/App.java", results.get(0));

        SearchVisitor pngVisitor = new SearchVisitor(name -> name.endsWith(".png"), 5000);
        root.accept(pngVisitor);
        assertEquals(List.of("src/images/logo.png"), pngVisitor.getMatchingPaths());

        SearchVisitor bothJavaVisitor = new SearchVisitor(".java", 1000);
        root.accept(bothJavaVisitor);
        assertEquals(List.of("src/App.java", "src/Utils.java"), bothJavaVisitor.getMatchingPaths());
    }

    @Test
    @DisplayName("T4: Search produces correct, deterministic, root-relative paths sorted lexicographically")
    void testSearchDeterministicRootRelativePaths() {
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

        for (String path : results) {
            assertFalse(path.contains("\\"), "Path must use '/' as separator: " + path);
            assertFalse(path.startsWith("/"), "Path must be root-relative without leading slash: " + path);
        }

        SearchVisitor searchSrc = new SearchVisitor("", 0);
        src.accept(searchSrc);
        List<String> srcResults = searchSrc.getMatchingPaths();
        assertEquals(List.of("App.java", "Utils.java", "images/logo.png"), srcResults);
    }

    @Test
    @DisplayName("T5: Statistics are correct for nesting, counting, and maximum depth (root depth = 0)")
    void testStatisticsVisitorAccuracy() {
        StatisticsVisitor stats = new StatisticsVisitor();
        root.accept(stats);

        assertEquals(4, stats.getDirectoryCount());
        assertEquals(5, stats.getFileCount());
        assertEquals(3, stats.getMaxDepth());

        StatisticsVisitor docsStats = new StatisticsVisitor();
        docs.accept(docsStats);
        assertEquals(1, docsStats.getDirectoryCount());
        assertEquals(1, docsStats.getFileCount());
        assertEquals(1, docsStats.getMaxDepth());

        StatisticsVisitor singleFileStats = new StatisticsVisitor();
        readme.accept(singleFileStats);
        assertEquals(0, singleFileStats.getDirectoryCount());
        assertEquals(1, singleFileStats.getFileCount());
        assertEquals(0, singleFileStats.getMaxDepth());
    }

    @Test
    @DisplayName("T6: Empty structures and no-match search cases are handled gracefully")
    void testEmptyStructuresAndNoMatchCases() {
        DirectoryNode emptyDir = new DirectoryNode("empty");

        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        emptyDir.accept(sizeVisitor);
        assertEquals(0, sizeVisitor.getTotalSize());

        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        emptyDir.accept(statsVisitor);
        assertEquals(0, statsVisitor.getFileCount());
        assertEquals(1, statsVisitor.getDirectoryCount());
        assertEquals(0, statsVisitor.getMaxDepth());

        SearchVisitor searchEmpty = new SearchVisitor("", 0);
        emptyDir.accept(searchEmpty);
        assertTrue(searchEmpty.getMatchingPaths().isEmpty());

        SearchVisitor noMatch = new SearchVisitor("nonexistent_file_name_12345", 0);
        root.accept(noMatch);
        assertTrue(noMatch.getMatchingPaths().isEmpty());

        SearchVisitor highThreshold = new SearchVisitor("", 100_000);
        root.accept(highThreshold);
        assertTrue(highThreshold.getMatchingPaths().isEmpty());
    }

    @Test
    @DisplayName("T7: Repeat executions do not leak or multiply accumulated state")
    void testControlledStateAndRepeatedExecutions() {
        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);
        long firstRunSize = sizeVisitor.getTotalSize();

        root.accept(sizeVisitor);
        assertEquals(firstRunSize, sizeVisitor.getTotalSize());

        sizeVisitor.reset();
        root.accept(sizeVisitor);
        assertEquals(firstRunSize, sizeVisitor.getTotalSize());

        docs.accept(sizeVisitor);
        assertEquals(12000, sizeVisitor.getTotalSize());

        SearchVisitor searchVisitor = new SearchVisitor(".java", 0);
        root.accept(searchVisitor);
        List<String> firstSearch = searchVisitor.getMatchingPaths();

        root.accept(searchVisitor);
        List<String> secondSearch = searchVisitor.getMatchingPaths();
        assertEquals(firstSearch, secondSearch);

        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        root.accept(statsVisitor);
        assertEquals(5, statsVisitor.getFileCount());
        assertEquals(4, statsVisitor.getDirectoryCount());
        assertEquals(3, statsVisitor.getMaxDepth());

        root.accept(statsVisitor);
        assertEquals(5, statsVisitor.getFileCount());
        assertEquals(4, statsVisitor.getDirectoryCount());
        assertEquals(3, statsVisitor.getMaxDepth());
    }

    @Test
    @DisplayName("T8: Invalid tree mutations are rejected predictably while valid nesting remains supported")
    void testTreeIntegrityConstraints() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("invalid.dat", -1));

        assertThrows(IllegalArgumentException.class, () -> new FileNode("", 100));
        assertThrows(IllegalArgumentException.class, () -> new FileNode("   ", 100));
        assertThrows(IllegalArgumentException.class, () -> new FileNode(null, 100));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(""));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(null));

        assertThrows(IllegalArgumentException.class, () -> new FileNode("a/b.txt", 100));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode("sub/dir"));

        DirectoryNode testDir = new DirectoryNode("testDir");
        testDir.addChild(new FileNode("item.txt", 100));
        assertThrows(IllegalArgumentException.class, () -> testDir.addChild(new FileNode("item.txt", 200)));
        assertThrows(IllegalArgumentException.class, () -> testDir.addChild(new DirectoryNode("item.txt")));

        DirectoryNode otherDir = new DirectoryNode("otherDir");
        FileNode sharedFile = new FileNode("shared.txt", 50);
        testDir.addChild(sharedFile);
        assertThrows(IllegalStateException.class, () -> otherDir.addChild(sharedFile));

        assertTrue(testDir.removeChild(sharedFile));
        assertNull(sharedFile.getParent());
        assertDoesNotThrow(() -> otherDir.addChild(sharedFile));

        assertThrows(IllegalArgumentException.class, () -> testDir.addChild(testDir));

        DirectoryNode a = new DirectoryNode("A");
        DirectoryNode b = new DirectoryNode("B");
        DirectoryNode c = new DirectoryNode("C");
        a.addChild(b);
        b.addChild(c);

        assertThrows(IllegalArgumentException.class, () -> c.addChild(a));
        assertThrows(IllegalArgumentException.class, () -> c.addChild(b));

        List<Element> children = testDir.getChildren();
        assertThrows(UnsupportedOperationException.class, () -> children.add(new FileNode("hack.txt", 1)));
    }
}
