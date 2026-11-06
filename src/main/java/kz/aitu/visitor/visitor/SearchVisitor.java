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

/**
 * Concrete Visitor that searches for files satisfying a configurable filename criterion
 * and an inclusive minimum-size threshold (size >= threshold).
 * Returns matching file paths relative to the chosen root, using '/' as separator,
 * sorted lexicographically for deterministic output (R4, Constraint 4).
 */
public class SearchVisitor implements Visitor {

    private final Predicate<String> nameCriterion;
    private final long minSizeThreshold;

    private final List<String> matchingPaths = new ArrayList<>();
    private final Deque<String> currentPathSegments = new ArrayDeque<>();
    private int traversalDepth = 0;

    /**
     * Constructs a SearchVisitor with a predicate criterion and minimum size threshold.
     *
     * @param nameCriterion    filter predicate tested against each file name
     * @param minSizeThreshold inclusive minimum byte size threshold
     * @throws IllegalArgumentException if nameCriterion is null or minSizeThreshold is negative
     */
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

    /**
     * Convenience constructor accepting a substring query pattern and minimum size threshold.
     * Matches files whose name contains the query string (case-sensitive). If query is null
     * or empty, matches all names.
     *
     * @param namePattern      substring pattern to search for
     * @param minSizeThreshold inclusive minimum byte size threshold
     */
    public SearchVisitor(String namePattern, long minSizeThreshold) {
        this(name -> namePattern == null || namePattern.isEmpty() || name.contains(namePattern), minSizeThreshold);
    }

    /**
     * Resets the visitor's internal match results and path segments.
     */
    public void reset() {
        this.matchingPaths.clear();
        this.currentPathSegments.clear();
    }

    /**
     * Returns the matching file paths relative to the chosen root, sorted lexicographically.
     *
     * @return unmodifiable list of deterministic root-relative paths
     */
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
            // Push directory name only for subdirectories (relative to root)
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
