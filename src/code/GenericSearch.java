package code;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * The generic search procedure. It knows nothing about caves: it only asks
 * the GenericSearchProblem for the initial state, expansions, goal test and
 * heuristic. Strategies: "UC", "AS", "ID".
 */
public class GenericSearch {

    private final GenericSearchProblem problem;
    private int nodesExpanded = 0;     // nodes chosen for expansion
    private boolean cutoff = false;    // ID: did the depth limit hide anything?

    public GenericSearch(GenericSearchProblem problem) {
        this.problem = problem;
    }

    public int getNodesExpanded() { return nodesExpanded; }

    // Runs the given strategy. Returns the goal node, or null if no solution. 
    public TreeNode search(String strategy) {
        nodesExpanded = 0;
        switch (strategy) {
            case "UC": return bestFirst(false);
            case "AS": return bestFirst(true);
            case "ID": return iterativeDeepening();
            default: throw new IllegalArgumentException("Unknown strategy: " + strategy);
        }
    }

    // ---------------- UCS and A* (best-first search) ----------------

    /** A queue entry with an insertion number, so ties are broken deterministically (FIFO). */
    private static class Entry {
        final TreeNode node;
        final long seq;
        Entry(TreeNode node, long seq) { this.node = node; this.seq = seq; }
    }

    /** Compares by g (lives, then energy) -- used by UCS. */
    private int compareG(TreeNode a, TreeNode b) {
        return TreeNode.compareCost(a, b);
    }

    /** Compares by f = g + h (lives, then energy) -- used by A*. */
    private int compareF(TreeNode a, TreeNode b) {
        int la = a.getLivesUsed() + problem.heuristicLives(a.getState());
        int lb = b.getLivesUsed() + problem.heuristicLives(b.getState());
        if (la != lb) return Integer.compare(la, lb);
        int ea = a.getEnergyUsed() + problem.heuristicEnergy(a.getState());
        int eb = b.getEnergyUsed() + problem.heuristicEnergy(b.getState());
        return Integer.compare(ea, eb);
    }

    private TreeNode bestFirst(boolean useHeuristic) {
        Comparator<Entry> order = (x, y) -> {
            int c = useHeuristic ? compareF(x.node, y.node) : compareG(x.node, y.node);
            return c != 0 ? c : Long.compare(x.seq, y.seq);
        };
        PriorityQueue<Entry> frontier = new PriorityQueue<>(order);
        Set<CaveState> closed = new HashSet<>();
        long seq = 0;

        frontier.add(new Entry(new TreeNode(problem.getInitialState()), seq++));

        while (!frontier.isEmpty()) {
            TreeNode node = frontier.poll().node;
            CaveState s = node.getState();

            if (problem.isGoal(s)) return node;     // goal test when chosen, keeps optimality
            if (!closed.add(s)) continue;           // already expanded this state

            nodesExpanded++;
            for (TreeNode child : problem.expand(node)) {
                if (!closed.contains(child.getState()))
                    frontier.add(new Entry(child, seq++));
            }
        }
        return null;
    }

    // ---------------- Iterative deepening ----------------

    private TreeNode iterativeDeepening() {
        TreeNode root = new TreeNode(problem.getInitialState());
        for (int limit = 0; ; limit++) {
            cutoff = false;
            Map<CaveState, Integer> shallowest = new HashMap<>();
            TreeNode result = depthLimited(root, limit, shallowest);
            if (result != null) return result;
            if (!cutoff) return null;   // nothing was cut off: the whole space is exhausted
        }
    }

    private TreeNode depthLimited(TreeNode node, int limit, Map<CaveState, Integer> shallowest) {
        CaveState s = node.getState();
        if (problem.isGoal(s)) return node;

        // skip a state if we already reached it at the same or a smaller depth in this iteration
        Integer seenAt = shallowest.get(s);
        if (seenAt != null && seenAt <= node.getDepth()) return null;
        shallowest.put(s, node.getDepth());

        if (node.getDepth() == limit) {
            if (!problem.expand(node).isEmpty()) cutoff = true;   // deeper nodes exist
            return null;
        }

        nodesExpanded++;
        for (TreeNode child : problem.expand(node)) {
            TreeNode r = depthLimited(child, limit, shallowest);
            if (r != null) return r;
        }
        return null;
    }
}