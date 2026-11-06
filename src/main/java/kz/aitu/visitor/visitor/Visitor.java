package kz.aitu.visitor.visitor;

import kz.aitu.visitor.model.DirectoryNode;
import kz.aitu.visitor.model.FileNode;

public interface Visitor {
    void visit(FileNode file);
    void visit(DirectoryNode directory);
}
