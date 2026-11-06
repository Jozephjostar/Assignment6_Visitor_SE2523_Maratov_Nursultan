package kz.aitu.visitor.model;

import kz.aitu.visitor.visitor.Visitor;

/**
 * Concrete Element representing a file leaf node in the file system tree.
 * Holds an immutable name and non-negative byte size.
 */
public class FileNode implements Element {

    private final String name;
    private final long size;
    private DirectoryNode parent;

    /**
     * Constructs a new FileNode.
     *
     * @param name the file name (cannot be null, blank, or contain '/')
     * @param size the non-negative size in bytes
     * @throws IllegalArgumentException if name is invalid or size is negative
     */
    public FileNode(String name, long size) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or blank.");
        }
        if (name.contains("/")) {
            throw new IllegalArgumentException("File name cannot contain path separator '/': " + name);
        }
        if (size < 0) {
            throw new IllegalArgumentException("File size cannot be negative: " + size);
        }
        this.name = name.trim();
        this.size = size;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the size of the file in bytes.
     *
     * @return the byte size
     */
    public long getSize() {
        return size;
    }

    @Override
    public DirectoryNode getParent() {
        return parent;
    }

    @Override
    public void setParent(DirectoryNode parent) {
        this.parent = parent;
    }

    @Override
    public void accept(Visitor visitor) {
        // Double dispatch step 1: delegates to visit(FileNode)
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "FileNode{name='" + name + "', size=" + size + " bytes}";
    }
}
