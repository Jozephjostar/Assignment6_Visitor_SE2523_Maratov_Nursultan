package kz.aitu.visitor.visitor;

import kz.aitu.visitor.model.DirectoryNode;
import kz.aitu.visitor.model.Element;
import kz.aitu.visitor.model.FileNode;

public class StatisticsVisitor implements Visitor {

    private int fileCount = 0;
    private int directoryCount = 0;
    private int maxDepth = 0;
    private int currentDepth = 0;
    private int traversalDepth = 0;

    public void reset() {
        this.fileCount = 0;
        this.directoryCount = 0;
        this.maxDepth = 0;
        this.currentDepth = 0;
    }

    public int getFileCount() {
        return fileCount;
    }

    public int getDirectoryCount() {
        return directoryCount;
    }

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
