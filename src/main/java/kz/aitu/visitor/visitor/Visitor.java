package kz.aitu.visitor.visitor;

import kz.aitu.visitor.model.DirectoryNode;
import kz.aitu.visitor.model.FileNode;

/**
 * Visitor interface defining operations that can be performed across elements
 * in the hierarchical file system structure.
 * Supports Double Dispatch by providing overloaded visit methods for each element type.
 */
public interface Visitor {

    /**
     * Visits a concrete FileNode element.
     *
     * @param file the file node to visit
     */
    void visit(FileNode file);

    /**
     * Visits a concrete DirectoryNode element.
     *
     * @param directory the directory node to visit
     */
    void visit(DirectoryNode directory);
}
