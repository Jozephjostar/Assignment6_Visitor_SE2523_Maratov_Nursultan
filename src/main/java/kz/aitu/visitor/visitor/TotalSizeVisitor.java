package kz.aitu.visitor.visitor;

import kz.aitu.visitor.model.DirectoryNode;
import kz.aitu.visitor.model.Element;
import kz.aitu.visitor.model.FileNode;

public class TotalSizeVisitor implements Visitor {

    private long totalSize = 0;
    private int traversalDepth = 0;

    public void reset() {
        this.totalSize = 0;
    }

    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public void visit(FileNode file) {
        boolean isRoot = (traversalDepth == 0);
        if (isRoot) {
            reset();
        }
        traversalDepth++;
        try {
            totalSize += file.getSize();
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
            for (Element child : directory.getChildren()) {
                child.accept(this);
            }
        } finally {
            traversalDepth--;
        }
    }
}
