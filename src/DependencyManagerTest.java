import org.junit.Test;

import java.util.*;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by tnguyen on 4/28/16.
 */
public class DependencyManagerTest {

    @Test
    public void testBuildDependencyGraph() throws Exception {
        DependencyManager<Character> manager = new DependencyManager<>();
        Map<Character, List<Character>> dependencies = new HashMap<>();
        dependencies.put('A', java.util.Arrays.asList('B', 'C'));
        dependencies.put('C', Collections.singletonList('B'));
        manager.buildDependencyGraph(dependencies);
        List<Character> result = manager.getDependencies();
        Character[] expectedOrder = new Character[] {'A', 'C', 'B'};
        assertArrayEquals(expectedOrder, result.toArray(new Character[result.size()]));
    }

    @Test
    public void testBuildDependencyGraphNoDependency() throws Exception {
        DependencyManager<Integer> manager = new DependencyManager<>();
        manager.addDependency(1);
        List<Integer> result = manager.getDependencies();
        Integer[] expectedOrder = new Integer[] {1};
        assertArrayEquals(expectedOrder, result.toArray(new Integer[result.size()]));
    }

    @Test
    public void testEmptyDependencyGraph() throws Exception {
        DependencyManager<Character> manager = new DependencyManager<>();
        List<Character> result = manager.getDependencies();
        assertTrue(result.isEmpty());
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testCircularDependency() throws Exception {
        DependencyManager<Character> manager = new DependencyManager<>();
        manager.addDependency('A', 'B');
        manager.addDependency('B', 'A');
        List<Character> result = manager.getDependencies();
        assertFalse(result.isEmpty());
    }

    @Test
    public void testCircularDependencyLoop() throws Exception {
        DependencyManager<Character> manager = new DependencyManager<>();
        manager.addDependency('A', 'B');
        manager.addDependency('B', 'C');
        manager.addDependency('C', 'A');
        List<Character> result = manager.getDependencies();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testMultipleDependencyGraphs() throws Exception {
        DependencyManager<Integer> manager = new DependencyManager<>();
        Map<Integer, List<Integer>> dependencies = new HashMap<>();
        dependencies.put(1, Collections.singletonList(2));
        dependencies.put(3, Collections.singletonList(4));
        manager.buildDependencyGraph(dependencies);
        List<Integer> result = manager.getDependencies();
        Integer[] expectedOrder = new Integer[] {1,3,2,4};
        assertArrayEquals(expectedOrder, result.toArray(new Integer[result.size()]));
    }

}