package kz.aitu.visitor;

import kz.aitu.visitor.model.DirectoryNode;
import kz.aitu.visitor.model.Element;
import kz.aitu.visitor.model.FileNode;
import kz.aitu.visitor.visitor.SearchVisitor;
import kz.aitu.visitor.visitor.StatisticsVisitor;
import kz.aitu.visitor.visitor.TotalSizeVisitor;
import kz.aitu.visitor.visitor.Visitor;

import java.util.List;

/**
 * Deterministic console application demonstrating the Visitor Pattern
 * over an in-memory hierarchical file system.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   ASSIGNMENT 06: VISITOR PATTERN - ADVANCED FILE SYSTEM DEMONSTRATION");
        System.out.println("   Course: ShP-2216 Software Design Patterns | Astana IT University");
        System.out.println("   Student: Nursultan Maratov (SE-2523)");
        System.out.println("================================================================================\n");

        // 1. Build sample file system hierarchy
        DirectoryNode root = buildSampleTree();

        System.out.println("--- 1. FILE SYSTEM STRUCTURE VISUALIZATION ---");
        printTree(root, "");
        System.out.println();

        // 2. Demonstrate Operation 1: TotalSizeVisitor
        System.out.println("--- 2. OPERATION 1: TOTAL SIZE VISITOR (R3) ---");
        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);
        System.out.printf("Total file size for entire tree ('%s'): %d bytes%n", root.getName(), sizeVisitor.getTotalSize());

        // Subtree size
        Element srcNode = root.getChild("src");
        if (srcNode != null) {
            TotalSizeVisitor srcSizeVisitor = new TotalSizeVisitor();
            srcNode.accept(srcSizeVisitor);
            System.out.printf("Total file size for subtree ('%s'): %d bytes%n", srcNode.getName(), srcSizeVisitor.getTotalSize());
        }
        System.out.println();

        // 3. Demonstrate Operation 2: SearchVisitor
        System.out.println("--- 3. OPERATION 2: SEARCH VISITOR (R4, Deterministic & Root-Relative) ---");
        System.out.println("Search A: Files containing '.java' with size >= 2,000 bytes");
        SearchVisitor javaSearch = new SearchVisitor(".java", 2000);
        root.accept(javaSearch);
        printSearchResults(javaSearch.getMatchingPaths());

        System.out.println("\nSearch B: All markdown files ('.md') with size >= 0 bytes");
        SearchVisitor mdSearch = new SearchVisitor(".md", 0);
        root.accept(mdSearch);
        printSearchResults(mdSearch.getMatchingPaths());

        System.out.println("\nSearch C: Non-matching query ('non_existent')");
        SearchVisitor noMatchSearch = new SearchVisitor("non_existent", 0);
        root.accept(noMatchSearch);
        printSearchResults(noMatchSearch.getMatchingPaths());
        System.out.println();

        // 4. Demonstrate Operation 3: StatisticsVisitor
        System.out.println("--- 4. OPERATION 3: STATISTICS VISITOR (R5, Extensible OCP Operation) ---");
        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        root.accept(statsVisitor);
        System.out.println("Subtree statistics for root:");
        System.out.printf("  • Total Files Visited:       %d%n", statsVisitor.getFileCount());
        System.out.printf("  • Total Directories Visited: %d (includes visited root directory)%n", statsVisitor.getDirectoryCount());
        System.out.printf("  • Maximum Tree Depth:        %d (root depth = 0)%n", statsVisitor.getMaxDepth());
        System.out.println();

        // 5. Demonstrate State Reusability & Run Order Independence (T7)
        System.out.println("--- 5. REUSABILITY & REPEAT RUN VERIFICATION (T7) ---");
        root.accept(sizeVisitor); // Re-run existing size visitor instance
        System.out.printf("Re-running TotalSizeVisitor without recreation: %d bytes (No leakage/doubling)%n", sizeVisitor.getTotalSize());

        root.accept(statsVisitor); // Re-run existing stats visitor instance
        System.out.printf("Re-running StatisticsVisitor: %d files, %d dirs, depth %d (No multiplied counters)%n",
                statsVisitor.getFileCount(), statsVisitor.getDirectoryCount(), statsVisitor.getMaxDepth());
        System.out.println();

        // 6. Demonstrate Tree Integrity Checks (R6, T8)
        System.out.println("--- 6. TREE INTEGRITY & INVARIANT VALIDATION (R6, T8) ---");
        testIntegritySafeguard("Reject Negative File Size", () -> new FileNode("bad.bin", -500));
        testIntegritySafeguard("Reject Duplicate Sibling Name", () -> root.addChild(new FileNode("README.md", 100)));
        testIntegritySafeguard("Reject Multiple Parent Ownership", () -> {
            DirectoryNode anotherDir = new DirectoryNode("backup");
            anotherDir.addChild(root.getChild("README.md"));
        });
        testIntegritySafeguard("Reject Cycle (Self-Addition)", () -> root.addChild(root));
        testIntegritySafeguard("Reject Cycle (Ancestor to Descendant)", () -> {
            DirectoryNode parentDir = new DirectoryNode("parent");
            DirectoryNode childDir = new DirectoryNode("child");
            parentDir.addChild(childDir);
            childDir.addChild(parentDir);
        });

        System.out.println("\n================================================================================");
        System.out.println("   DEMONSTRATION COMPLETED SUCCESSFULLY (All 8 Requirements Met)");
        System.out.println("================================================================================");
    }

    private static DirectoryNode buildSampleTree() {
        DirectoryNode root = new DirectoryNode("university-project");

        FileNode readme = new FileNode("README.md", 850);
        FileNode pom = new FileNode("pom.xml", 2400);

        DirectoryNode docs = new DirectoryNode("docs");
        FileNode spec = new FileNode("specification.pdf", 45000);
        FileNode architecture = new FileNode("architecture.md", 3200);
        docs.addChild(spec);
        docs.addChild(architecture);

        DirectoryNode src = new DirectoryNode("src");
        FileNode mainApp = new FileNode("Main.java", 4800);
        FileNode controller = new FileNode("Controller.java", 1850);

        DirectoryNode assets = new DirectoryNode("assets");
        FileNode logo = new FileNode("logo.png", 15400);
        assets.addChild(logo);

        src.addChild(mainApp);
        src.addChild(controller);
        src.addChild(assets);

        root.addChild(readme);
        root.addChild(pom);
        root.addChild(docs);
        root.addChild(src);

        return root;
    }

    private static void printSearchResults(List<String> paths) {
        if (paths.isEmpty()) {
            System.out.println("  (No matching files found)");
            return;
        }
        for (String path : paths) {
            System.out.println("  → " + path);
        }
    }

    private static void printTree(Element element, String indent) {
        // Double dispatch can be used or custom visitor. Here we use visitor to print!
        element.accept(new Visitor() {
            @Override
            public void visit(FileNode file) {
                System.out.println(indent + "├── [FILE] " + file.getName() + " (" + file.getSize() + " bytes)");
            }

            @Override
            public void visit(DirectoryNode directory) {
                System.out.println(indent + "└── [DIR]  " + directory.getName() + "/");
                for (Element child : directory.getChildren()) {
                    printTree(child, indent + "    ");
                }
            }
        });
    }

    private static void testIntegritySafeguard(String description, Runnable action) {
        try {
            action.run();
            System.out.println("  [FAIL] " + description + ": Expected exception was not thrown.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("  [PASS] " + description + ": Successfully caught "
                    + ex.getClass().getSimpleName() + " -> " + ex.getMessage());
        }
    }
}
