package kz.aitu.visitor.model;

import kz.aitu.visitor.visitor.Visitor;

/**
 * Common Element interface for nodes in the in-memory hierarchical file system.
 * Part of the Visitor design pattern (Object Structure element contract).
 */
public interface Element {

    /**
     * Returns the name of the file system element.
     *
     * @return the element name
     */
    String getName();

    /**
     * Accepts a visitor to perform an operation on this element via double dispatch.
     *
     * @param visitor the visitor executing an operation
     */
    void accept(Visitor visitor);

    /**
     * Returns the parent directory containing this element, or null if root/unattached.
     *
     * @return the parent directory
     */
    DirectoryNode getParent();

    /**
     * Sets the parent directory reference for this element.
     *
     * @param parent the new parent directory
     */
    void setParent(DirectoryNode parent);
}
