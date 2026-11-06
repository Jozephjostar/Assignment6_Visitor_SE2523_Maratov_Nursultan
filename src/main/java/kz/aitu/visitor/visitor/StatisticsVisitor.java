package kz.aitu.visitor.visitor;

import kz.aitu.visitor.model.DirectoryNode;
import kz.aitu.visitor.model.Element;
import kz.aitu.visitor.model.FileNode;

/**
 * Concrete Visitor that collects file count, directory count (including the visited root
 * when it is a directory), and maximum tree depth (root depth = 0).
 * Implements R5 and demonstrates OCP extensibility (R8) by being introduced without any
 * modifications to Element, FileNode, DirectoryNode, or their accept() methods.
 */
public class StatisticsVisitor implements Visitor {

    private int fileCount = 0;
    private int directoryCount = 0;
    private int maxDepth = 0;
    private int currentDepth = 0;
    private int traversalDepth = 0;

    /**
     * Resets the collected statistical counters and depth state.
     */
    public void reset() {
        this.fileCount = 0;
        this.directoryCount = 0;
        this.maxDepth = 0;
        this.currentDepth = 0;
    }

    /**
     * Returns the total number of visited FileNode elements.
     *
     * @return file count
     */
    public int getFileCount() {
        return fileCount;
    }

    /**
     * Returns the total number of visited DirectoryNode elements (including root if directory).
     *
     * @return directory count
     */
    public int getDirectoryCount() {
        return directoryCount;
    }

    /**
     * Returns the maximum depth of the visited tree (root depth = 0).
     *
     * @return maximum tree depth
     */
    public int getMaxDepth() {
        return maxDepth;
    }

    @Override
    public void visit(FileNode file) {
        boolean isRoot = (traversalDepth == 0);
        if (isRoot) {
            reset();
        }
        traversalDepth++;
        try {
            fileCount++;
            if (currentDepth > maxDepth) {
                maxDepth = currentDepth;
            }
        } finally {
            traversalDepth--;
        }
    }

    @Override
    public void visit(DirectoryNode directory) {
        boolean isRoot = (traversalDepth == 0);
        if (isRoot) {
            reset();
        }
        traversalDepth++;
        try {
            directoryCount++;
            if (currentDepth > maxDepth) {
                maxDepth = currentDepth;
            }

            currentDepth++;
            try {
                for (Element child : directory.getChildren()) {
                    child.accept(this);
                }
            } finally {
                currentDepth--;
            }
        } finally {
            traversalDepth--;
        }
    }

    @Override
    public String toString() {
        return "StatisticsVisitor{" +
                "fileCount=" + fileCount +
                ", directoryCount=" + directoryCount +
                ", maxDepth=" + maxDepth +
                '}';
    }
}
