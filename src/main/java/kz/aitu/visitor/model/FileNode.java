package kz.aitu.visitor.model;

import kz.aitu.visitor.visitor.Visitor;

public class FileNode implements Element {

    private final String name;
    private final long size;
    private DirectoryNode parent;

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
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "FileNode{name='" + name + "', size=" + size + " bytes}";
    }
}
