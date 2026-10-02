package code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A node in the search tree.
 * Wraps a state together with the bookkeeping needed by the search procedure.
 */
public class TreeNode {

    private final CaveState state;           // the world configuration this node represents
    private final TreeNode parent;       // node that generated this one (null for root)
    private final Operator operator;     // action applied to the parent to reach this node
    private final int depth;             // number of actions from the root
    // path cost from the root to this node (g(n)) was seperated into two components to allow for tie-breaking
    private final int livesUsed;     // g(n), primary component
    private final int energyUsed;    // g(n), secondary component (tie-breaker)


    /** Creates a root node. */
    public TreeNode(CaveState state) {
        this(state, null, null, 0, 0, 0);
    }

    public TreeNode(
            CaveState state,
            TreeNode parent,
            Operator operator,
            int depth,
            int livesUsed,
            int energyUsed) {

        this.state = state;
        this.parent = parent;
        this.operator = operator;
        this.depth = depth;
        this.livesUsed = livesUsed;
        this.energyUsed = energyUsed;
        
    }

    public CaveState getState() {
        return state;
    }

    public TreeNode getParent() {
        return parent;
    }

    public Operator getOperator() {
        return operator;
    }

    public int getDepth() {
        return depth;
    }

    public int getLivesUsed() {
        return livesUsed;
    }

    public int getEnergyUsed() {
        return energyUsed;
    }

    // Compares path costs lexicographically:
    // minimize lives used first, then energy used.
    public static int compareCost(TreeNode a, TreeNode b) {
        if (a.livesUsed != b.livesUsed) return Integer.compare(a.livesUsed, b.livesUsed);
        return Integer.compare(a.energyUsed, b.energyUsed);
    }


    /**
     * Walks back up the parent links and returns the actions
     * from the root to this node, in order.
     */
    public List<Operator> getPlan() {
        List<Operator> plan = new ArrayList<>();

        for (TreeNode n = this; n.parent != null; n = n.parent) {
            plan.add(n.operator);
        }

        Collections.reverse(plan);
        return plan;
    }
}