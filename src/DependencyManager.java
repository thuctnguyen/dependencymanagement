
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

/**
 * This class presents a generic dependency manager.
 * At the core of the implementation is a dependency graph.
 * Sample usage:
 * <pre>
 * {@code
 *      DependencyManager<Character> manager = new DependencyManager<>();
 *      manager.addDependency('A', 'B');
 *      manager.addDependency('A', 'C');
 *      manager.addDependency('B', 'C');
 *      List<Character> dependencies = manager.getDependencies();
 * }
 * </pre>
 * In the above example, the dependencies are returned in topological order.
 *
 * @param <T>
 */
public class DependencyManager<T> {
    /**
     * Each node in the graph keeps track of the nodes that depend on it,
     * as well as the nodes on which it depends.
     * So if A depends on B and A depends on C, both B and C are in A's dependsOn list.
     * And A is in B's dependedUpon list as well as in C's.
     *
     * @param <T>
     */
    private static class DependencyNode<T> {
        private final T data;
        private final Set<DependencyNode<T>> dependsOn;
        private final Set<DependencyNode<T>> dependedUpon;
        public DependencyNode(T data) {
            this.data = data;
            this.dependsOn = new HashSet<>();
            this.dependedUpon = new HashSet<>();
        }

        public void dependsOn(final DependencyNode<T> to) {
            if (to.dependsOn.contains(this)) {
                throw new IllegalArgumentException("Circular dependency");
            }
            this.dependsOn.add(to);
            to.dependedUpon(this);
        }

        public void dependedUpon(final DependencyNode<T> from) {
            this.dependedUpon.add(from);
        }

        public int getNumDependencies() {
            return this.dependsOn.size();
        }

        public Set<DependencyNode<T>> getDependedUponNodes() {
            Set<DependencyNode<T>> clone = new HashSet<>();
            clone.addAll(this.dependedUpon);
            return clone;
        }

        public T getData() {
            return this.data;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof DependencyNode)) {
                return false;
            }
            DependencyNode other = (DependencyNode) o;
            return this.data.equals(other.data);
        }

        @Override
        public int hashCode() {
            return this.data.hashCode();
        }
    }

    /**
     * This class is the core of the dependency manager.
     *
     * @param <T>
     */
    private static class DependencyGraph<T> {
        private final Map<T, DependencyNode<T>> nodes;
        public DependencyGraph() {
            this.nodes = new HashMap<>();
        }

        /**
         * Add a dependency from one node to another. The nodes are created if they don't already exist.
         *
         * @param from source of the dependency
         * @param to target of the dependency
         */
        public void addDependency(final T from, final T to) {
            if (from == null) {
                throw new IllegalArgumentException();
            }
            DependencyNode<T> fromNode = this.nodes.getOrDefault(from, null);
            if (fromNode == null) {
                fromNode = new DependencyNode<>(from);
                this.nodes.put(from, fromNode);
            }
            if (to != null) {
                DependencyNode<T> toNode = this.nodes.getOrDefault(to, null);
                if (toNode == null) {
                    toNode = new DependencyNode<>(to);
                    this.nodes.put(to, toNode);
                }
                fromNode.dependsOn(toNode);
            }
        }

        public Collection<DependencyNode<T>> getNodes() {
            List<DependencyNode<T>> clone = new ArrayList<>();
            clone.addAll(this.nodes.values());
            return clone;
        }
    }

    /**
     * This iterator returns the nodes of the graph in topological order.
     * As soon as a node's dependencies are satisfied, it's put in a queue to be processed.
     * Once a node is processed, those that depend on it have their number of dependencies
     * decremented by 1.
     */
    private static class TopologicalOrderIterator<T> implements Iterator<T> {

        // Nodes with no dependencies are put in this queue to be processed.
        private final Queue<DependencyNode<T>> queue;
        // This map keeps track on the number of dependencies for each node.
        private final Map<DependencyNode<T>, Integer> outDegreeMap;

        public TopologicalOrderIterator(DependencyGraph<T> graph) {
            this.queue = new LinkedList<>();
            this.outDegreeMap = new HashMap<>();
            init(graph);
        }

        private void init(DependencyGraph<T> graph) {
            if (graph == null) {
                return;
            }
            Collection<DependencyNode<T>> nodes = graph.getNodes();
            for (DependencyNode<T> node : nodes) {
                int numDependencies = node.getNumDependencies();
                outDegreeMap.put(node, numDependencies);
                if (numDependencies == 0) {
                    queue.offer(node);
                }
            }
        }

        @Override
        public boolean hasNext() {
            return !this.queue.isEmpty();
        }

        /**
         * Once a node is processed, the ones that depend on it have their dependencies
         * decremented by 1.
         *
         * @param toNode the node that just got processed.
         */
        private void satisfyDependencies(DependencyNode<T> toNode) {
            Set<DependencyNode<T>> dependedUponNodes = toNode.getDependedUponNodes();
            for(DependencyNode<T> fromNode : dependedUponNodes) {
                Integer numDependencies = this.outDegreeMap.get(fromNode);
                if (numDependencies > 0) {
                    numDependencies--;
                    if (numDependencies == 0) {
                        queue.offer(fromNode);
                    }
                    this.outDegreeMap.put(fromNode, numDependencies);
                }
            }
        }

        @Override
        public T next() {
            if (this.hasNext()) {
                DependencyNode<T> next = this.queue.remove();
                satisfyDependencies(next);
                return next.getData();
            }

            throw new NoSuchElementException();
        }
    }

    private final DependencyGraph<T> graph;

    public DependencyManager() {
        this.graph = new DependencyGraph<>();
    }

    public void addDependency(final T from, final T to) {
        this.graph.addDependency(from, to);
    }

    public void addDependency(final T from) {
        this.addDependency(from, null);
    }

    public void buildDependencyGraph(final Map<T, List<T>> dependencies) {
        if (dependencies == null) {
            return;
        }
        for(Map.Entry<T, List<T>> e : dependencies.entrySet()) {
            T to = e.getKey();
            List<T> fromList = e.getValue();
            for(T from : fromList) {
                this.addDependency(from, to);
            }
        }
    }

    public Iterator<T> iterator() {
        return new TopologicalOrderIterator<T>(this.graph);
    }

    public List<T> getDependencies() {
        List<T> result = new ArrayList<>();
        Iterator<T> iter = this.iterator();
        while(iter.hasNext()) {
            result.add(iter.next());
        }
        return result;
    }

    public static void printDependencies(final Map<Character, List<Character>> dependencies) {
        DependencyManager<Character> manager = new DependencyManager<>();
        manager.buildDependencyGraph(dependencies);
        List<Character> result = manager.getDependencies();
        for(Character c : result) {
            System.out.print(c + " ");
        }
    }

    public static void main(String[] args) {
        Map<Character, List<Character>> input = new HashMap<>();
        input.put('A', Arrays.asList('B', 'C'));
        input.put('C', Collections.singletonList('B'));
        printDependencies(input);
    }
}
