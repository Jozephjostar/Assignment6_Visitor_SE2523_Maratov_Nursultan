package kz.aitu.visitor.model;

import kz.aitu.visitor.visitor.Visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DirectoryNode implements Element {

    private final String name;
    private DirectoryNode parent;
    private final Map<String, Element> children = new LinkedHashMap<>();

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

    public List<Element> getChildren() {
        return Collections.unmodifiableList(new ArrayList<>(children.values()));
    }

    public void addChild(Element child) {
        if (child == null) {
            throw new IllegalArgumentException("Child element cannot be null.");
        }

        Element current = this;
        while (current != null) {
            if (current == child) {
                throw new IllegalArgumentException(
                        "Cycle detected: Cannot add ancestor or self '" + child.getName()
                                + "' as a child of '" + this.name + "'.");
            }
            current = current.getParent();
        }

        if (child.getParent() != null && child.getParent() != this) {
            throw new IllegalStateException(
                    "Element '" + child.getName() + "' already belongs to parent '"
                            + child.getParent().getName() + "'. Detach before adding.");
        }

        if (children.containsKey(child.getName())) {
            throw new IllegalArgumentException(
                    "Duplicate sibling name: Element '" + child.getName()
                            + "' already exists in directory '" + this.name + "'.");
        }

        children.put(child.getName(), child);
        child.setParent(this);
    }

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

    public boolean removeChild(String childName) {
        Element removed = children.remove(childName);
        if (removed != null) {
            removed.setParent(null);
            return true;
        }
        return false;
    }

    public Element getChild(String childName) {
        return children.get(childName);
    }

    public boolean hasChild(String childName) {
        return children.containsKey(childName);
    }

    public int getChildCount() {
        return children.size();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "DirectoryNode{name='" + name + "', childrenCount=" + children.size() + "}";
    }
}
