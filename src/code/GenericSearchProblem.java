package code;

import java.util.ArrayList;
import java.util.List;

// GenericSearchProblem describes the problem: what the world is and what the rules are.
// initial state, which operators exist, what applying an operator does ,what a step costs ,goal test ,heuristic
// it is the input to the search with strategy

public abstract class GenericSearchProblem {

    private final CaveState initialState;

    protected GenericSearchProblem(CaveState initialState) {
        this.initialState = initialState;
    }

    public CaveState getInitialState() { return initialState; }


    // All operators of the problem; not all are applicable in every state. 
    public abstract Operator[] getOperators();

    public abstract CaveState applyOperator(CaveState state, Operator op);

    // Lives used by one step from 'from' to 'to' via 'op'. 
    public abstract int stepLives(CaveState from, Operator op, CaveState to);

    // Energy used by one step from 'from' to 'to' via 'op'. 
    public abstract int stepEnergy(CaveState from, Operator op, CaveState to);
    public abstract boolean isGoal(CaveState state);


    public int heuristicLives(CaveState state) { return 0; }

    public int heuristicEnergy(CaveState state) { return 0; }

    // ---------- generic part ----------

    /**
     * Expands a node: tries every operator and wraps each applicable
     * result in a child node. Depth and both cost components are updated
     * here, in one place, so they can never drift apart.
     */
    public List<TreeNode> expand(TreeNode node) {
        List<TreeNode> children = new ArrayList<>();
        CaveState s = node.getState();

        for (Operator op : getOperators()) {
            CaveState next = applyOperator(s, op);
            if (next == null) continue;   // operator not applicable here

            children.add(new TreeNode(
                    next, node, op,
                    node.getDepth() + 1,
                    node.getLivesUsed()  + stepLives(s, op, next),
                    node.getEnergyUsed() + stepEnergy(s, op, next)));
        }
        return children;
    }
}