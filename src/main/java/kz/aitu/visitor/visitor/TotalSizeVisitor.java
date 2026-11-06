package kz.aitu.visitor.visitor;

import kz.aitu.visitor.model.DirectoryNode;
import kz.aitu.visitor.model.Element;
import kz.aitu.visitor.model.FileNode;

/**
 * Concrete Visitor that computes the cumulative byte size of all files in an Element subtree.
 * Follows R3: each eligible file contributes exactly once, directory nodes do not contribute size.
 * Controls state across repeated executions to prevent stale or multiplied results (T7).
 */
public class TotalSizeVisitor implements Visitor {

    private long totalSize = 0;
    private int traversalDepth = 0;

    /**
     * Resets the accumulated size state to zero.
     */
    public void reset() {
        this.totalSize = 0;
    }

    /**
     * Returns the total computed byte size of all visited files.
     *
     * @return the sum of file sizes in bytes
     */
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
            // Traversal owner is the Visitor: visit each reachable child node
            for (Element child : directory.getChildren()) {
                child.accept(this);
            }
        } finally {
            traversalDepth--;
        }
    }
}
