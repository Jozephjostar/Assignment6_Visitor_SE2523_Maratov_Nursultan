package kz.aitu.visitor.visitor;

import kz.aitu.visitor.model.DirectoryNode;
import kz.aitu.visitor.model.Element;
import kz.aitu.visitor.model.FileNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

public class SearchVisitor implements Visitor {

    private final Predicate<String> nameCriterion;
    private final long minSizeThreshold;

    private final List<String> matchingPaths = new ArrayList<>();
    private final Deque<String> currentPathSegments = new ArrayDeque<>();
    private int traversalDepth = 0;

    public SearchVisitor(Predicate<String> nameCriterion, long minSizeThreshold) {
        if (nameCriterion == null) {
            throw new IllegalArgumentException("Filename criterion predicate cannot be null.");
        }
        if (minSizeThreshold < 0) {
            throw new IllegalArgumentException("Minimum size threshold cannot be negative: " + minSizeThreshold);
        }
        this.nameCriterion = nameCriterion;
        this.minSizeThreshold = minSizeThreshold;
    }

    public SearchVisitor(String namePattern, long minSizeThreshold) {
        this(name -> namePattern == null || namePattern.isEmpty() || name.contains(namePattern), minSizeThreshold);
    }

    public void reset() {
        this.matchingPaths.clear();
        this.currentPathSegments.clear();
    }

    public List<String> getMatchingPaths() {
        List<String> sorted = new ArrayList<>(matchingPaths);
        Collections.sort(sorted);
        return Collections.unmodifiableList(sorted);
    }

    @Override
    public void visit(FileNode file) {
        boolean isRoot = (traversalDepth == 0);
        if (isRoot) {
            reset();
        }
        traversalDepth++;
        try {
            if (nameCriterion.test(file.getName()) && file.getSize() >= minSizeThreshold) {
                matchingPaths.add(buildRelativePath(file.getName()));
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
            if (!isRoot) {
                currentPathSegments.addLast(directory.getName());
            }

            for (Element child : directory.getChildren()) {
                child.accept(this);
            }

            if (!isRoot) {
                currentPathSegments.removeLast();
            }
        } finally {
            traversalDepth--;
        }
    }

    private String buildRelativePath(String fileName) {
        if (currentPathSegments.isEmpty()) {
            return fileName;
        }
        return String.join("/", currentPathSegments) + "/" + fileName;
    }
}
