package kz.aitu.visitor.model;

import kz.aitu.visitor.visitor.Visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete Element representing a directory (composite node) in the file system tree.
 * Holds named child elements and supports arbitrary nesting.
 * Enforces tree integrity invariants: prevents duplicate siblings, cycles, and multi-parent ownership.
 */
public class DirectoryNode implements Element {

    private final String name;
    private DirectoryNode parent;
    private final Map<String, Element> children = new LinkedHashMap<>();

    /**
     * Constructs a new DirectoryNode.
     *
     * @param name the directory name (cannot be null, blank, or contain '/')
     * @throws IllegalArgumentException if name is invalid
     */
    public DirectoryNode(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Directory name cannot be null or blank.");
        }
        if (name.contains("/")) {
            throw new IllegalArgumentException("Directory name cannot contain path separator '/': " + name);
        }
        this.name = name.trim();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DirectoryNode getParent() {
        return parent;
    }

    @Override
    public void setParent(DirectoryNode parent) {
        this.parent = parent;
    }

    /**
     * Returns an unmodifiable view of this directory's children to preserve encapsulation.
     *
     * @return unmodifiable list of child elements
     */
    public List<Element> getChildren() {
        return Collections.unmodifiableList(new ArrayList<>(children.values()));
    }

    /**
     * Adds a child element to this directory while enforcing tree integrity.
     *
     * @param child the element to add
     * @throws IllegalArgumentException if child is null, duplicate sibling name exists, or cycle detected
     * @throws IllegalStateException    if child already belongs to another parent directory
     */
    public void addChild(Element child) {
        if (child == null) {
            throw new IllegalArgumentException("Child element cannot be null.");
        }

        // Integrity check: prevent cycles (cannot add self or an ancestor as child)
        Element current = this;
        while (current != null) {
            if (current == child) {
                throw new IllegalArgumentException(
                        "Cycle detected: Cannot add ancestor or self '" + child.getName()
                                + "' as a child of '" + this.name + "'.");
            }
            current = current.getParent();
        }

        // Integrity check: prevent multiple parent ownership
        if (child.getParent() != null && child.getParent() != this) {
            throw new IllegalStateException(
                    "Element '" + child.getName() + "' already belongs to parent '"
                            + child.getParent().getName() + "'. Detach before adding.");
        }

        // Integrity check: prevent duplicate sibling names
        if (children.containsKey(child.getName())) {
            throw new IllegalArgumentException(
                    "Duplicate sibling name: Element '" + child.getName()
                            + "' already exists in directory '" + this.name + "'.");
        }

        children.put(child.getName(), child);
        child.setParent(this);
    }

    /**
     * Removes a child element from this directory and clears its parent reference.
     *
     * @param child the child element to remove
     * @return true if the child was removed, false otherwise
     */
    public boolean removeChild(Element child) {
        if (child == null) {
            throw new IllegalArgumentException("Child cannot be null.");
        }
        if (children.remove(child.getName()) != null) {
            child.setParent(null);
            return true;
        }
        return false;
    }

    /**
     * Removes a child by name and clears its parent reference.
     *
     * @param childName the name of the child to remove
     * @return true if the child was removed, false otherwise
     */
    public boolean removeChild(String childName) {
        Element removed = children.remove(childName);
        if (removed != null) {
            removed.setParent(null);
            return true;
        }
        return false;
    }

    /**
     * Retrieves a direct child element by its name.
     *
     * @param childName the name of the child
     * @return the child element, or null if not found
     */
    public Element getChild(String childName) {
        return children.get(childName);
    }

    /**
     * Checks if this directory contains a child with the specified name.
     *
     * @param childName the name to check
     * @return true if child exists
     */
    public boolean hasChild(String childName) {
        return children.containsKey(childName);
    }

    /**
     * Returns the count of direct children in this directory.
     *
     * @return number of direct children
     */
    public int getChildCount() {
        return children.size();
    }

    @Override
    public void accept(Visitor visitor) {
        // Double dispatch step 1: delegates to visit(DirectoryNode)
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "DirectoryNode{name='" + name + "', childrenCount=" + children.size() + "}";
    }
}
