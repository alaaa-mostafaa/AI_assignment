package code;

import java.util.Arrays;

/**
 * The cave explorer problem. Holds everything that never changes during
 * the search (grid, door positions, key positions); CaveState holds only
 * what changes.
 * 
 * It holds the static map: grid, doorX/Y, keyX/Y, doorAt, keyAt.
   It holds the problem rules: applyOperator, stepLives, isGoal, heuristicEnergy.
 */
public class CaveExplorer extends GenericSearchProblem {

    private static final int START_LIVES = 3;

    private static final Operator[] OPERATORS = {
        Operator.LEFT, Operator.RIGHT,
        Operator.CLIMB_UP, Operator.CLIMB_DOWN,
        Operator.JUMP_DOWN,
        Operator.COLLECT, Operator.UNLOCK
    };

    private final int[][] grid;          // grid[y][x] = difficulty, 0 = wall
    private final int height, width;
    private final int[] doorX, doorY;    // door i is at (doorX[i], doorY[i])
    private final int[] keyX, keyY;      // key j is at (keyX[j], keyY[j])
    private final int[][] doorAt;        // doorAt[y][x] = door index, or -1
    private final int[][] keyAt;         // keyAt[y][x]  = key index, or -1

    private CaveExplorer(CaveState initial, int[][] grid,
                         int[] doorX, int[] doorY, int[] keyX, int[] keyY) {
        super(initial);
        this.grid = grid;
        this.height = grid.length;
        this.width = grid[0].length;
        this.doorX = doorX; this.doorY = doorY;
        this.keyX = keyX;   this.keyY = keyY;

        // lookup tables so "is there a door/key here?" is O(1)
        doorAt = new int[height][width];
        keyAt = new int[height][width];
        for (int[] row : doorAt) Arrays.fill(row, -1);
        for (int[] row : keyAt)  Arrays.fill(row, -1);
        for (int i = 0; i < doorX.length; i++) doorAt[doorY[i]][doorX[i]] = i;
        for (int j = 0; j < keyX.length; j++)  keyAt[keyY[j]][keyX[j]] = j;
    }

    /** Parses the input string described in the assignment. */
    public static CaveExplorer fromString(String s) {
        String[] p = s.split(";");

        String[] dim = p[0].split(",");
        int h = Integer.parseInt(dim[0]);
        int w = Integer.parseInt(dim[1]);

        String[] start = p[1].split(",");
        int x = Integer.parseInt(start[0]);
        int y = Integer.parseInt(start[1]);

        String[] er = p[2].split(",");
        int energy = Integer.parseInt(er[0]);
        int rope = Integer.parseInt(er[1]);

        int[][] grid = new int[h][w];
        for (int r = 0; r < h; r++) {
            String[] cells = p[3 + r].split(",");
            for (int c = 0; c < w; c++) grid[r][c] = Integer.parseInt(cells[c]);
        }

        int[] dxy = parsePairs(p[3 + h]);   // d1X,d1Y,d2X,d2Y,...
        int[] kxy = parsePairs(p[4 + h]);
        int nd = dxy.length / 2, nk = kxy.length / 2;
        int[] doorX = new int[nd], doorY = new int[nd];
        int[] keyX = new int[nk],  keyY = new int[nk];
        for (int i = 0; i < nd; i++) { doorX[i] = dxy[2 * i]; doorY[i] = dxy[2 * i + 1]; }
        for (int j = 0; j < nk; j++) { keyX[j] = kxy[2 * j];  keyY[j] = kxy[2 * j + 1]; }

        CaveState initial = new CaveState(x, y, energy, rope, START_LIVES,
                false, (1 << nd) - 1, (1 << nk) - 1);
        return new CaveExplorer(initial, grid, doorX, doorY, keyX, keyY);
    }

    private static int[] parsePairs(String line) {
        String[] t = line.split(",");
        int[] out = new int[t.length];
        for (int i = 0; i < t.length; i++) out[i] = Integer.parseInt(t[i].trim());
        return out;
    }

    // ---------- operators ----------

    @Override
    public Operator[] getOperators() { return OPERATORS; }

    /** Returns the successor state, or null if the operator is not applicable. */
    @Override
    public CaveState applyOperator(CaveState s, Operator op) {
        int x = s.getX(), y = s.getY();
        switch (op) {
            case LEFT:       return move(s, x - 1, y, false, false);
            case RIGHT:      return move(s, x + 1, y, false, false);
            case CLIMB_UP:   return move(s, x, y - 1, true, false);
            case CLIMB_DOWN: return move(s, x, y + 1, true, false);
            case JUMP_DOWN:  return move(s, x, y + 1, false, true);
            case COLLECT: {
                int k = keyAt[y][x];
                if (k < 0 || !s.isKeyOnMap(k) || s.isHoldingKey()) return null;
                return s.collectKey(k);
            }
            case UNLOCK: {
                int d = doorAt[y][x];
                if (d < 0 || !s.isDoorLocked(d) || !s.isHoldingKey()) return null;
                return s.unlockDoor(d);
            }
            default: return null;
        }
    }

    private CaveState move(CaveState s, int nx, int ny, boolean usesRope, boolean usesLife) {
        if (nx < 0 || nx >= width || ny < 0 || ny >= height) return null;
        int difficulty = grid[ny][nx];
        if (difficulty == 0) return null;                       // wall
        if (s.getEnergy() < difficulty) return null;            // not enough energy
        if (usesRope && s.getRope() < 1) return null;
        if (usesLife && s.getLives() <= 1) return null;
        return s.moveTo(nx, ny,
                s.getEnergy() - difficulty,
                s.getRope() - (usesRope ? 1 : 0),
                s.getLives() - (usesLife ? 1 : 0));
    }

    // ---------- costs: derived from what the step actually consumed ----------

    @Override
    public int stepLives(CaveState from, Operator op, CaveState to) {
        return from.getLives() - to.getLives();
    }

    @Override
    public int stepEnergy(CaveState from, Operator op, CaveState to) {
        return from.getEnergy() - to.getEnergy();
    }

    @Override
    public boolean isGoal(CaveState s) { return s.isGoal(); }

    // ---------- heuristic ----------


    //Because nothing forces the explorer to spend a life, 
    // so 0 is the only value that is guaranteed never to overestimate.
    @Override
    public int heuristicLives(CaveState s) { return 0; }

    /*
     * HEURISTIC EXPLANATION (A*):
     * To finish, every remaining locked door cell must be entered (a door
     * can only be unlocked while standing on it), and one key per remaining
     * door must be collected, which means entering that many key cells.
     * Entering a cell costs at least its difficulty. So the energy still
     * needed is at least: the difficulties of all locked doors, plus the
     * difficulties of the cheapest keys we still need to collect. A cell we
     * are standing on right now is already paid for, so it counts as 0.
     * The walking between these cells is ignored, so we never overestimate
     * the true remaining energy -> admissible. No life is provably needed
     * (jumpdown is optional), so the lives estimate is 0.
     */
    @Override
    public int heuristicEnergy(CaveState s) {
        int energy = 0;
        int doorsLeft = 0;

        for (int i = 0; i < doorX.length; i++) {
            if (!s.isDoorLocked(i)) continue;
            doorsLeft++;
            if (doorX[i] != s.getX() || doorY[i] != s.getY())
                energy += grid[doorY[i]][doorX[i]];
        }

        int keysNeeded = doorsLeft - (s.isHoldingKey() ? 1 : 0);
        if (keysNeeded > 0) {
            int[] costs = new int[keyX.length];
            int n = 0;
            for (int j = 0; j < keyX.length; j++) {
                if (!s.isKeyOnMap(j)) continue;
                boolean here = keyX[j] == s.getX() && keyY[j] == s.getY();
                costs[n++] = here ? 0 : grid[keyY[j]][keyX[j]];
            }
            Arrays.sort(costs, 0, n);
            for (int j = 0; j < keysNeeded && j < n; j++) energy += costs[j];
        }
        return energy;
    }




      public static String solve(String initString, String strategy) {
        CaveExplorer problem = fromString(initString);
        GenericSearch search = new GenericSearch(problem);
        TreeNode goal = search.search(strategy);

        if (goal == null) return "No Solution";

        StringBuilder plan = new StringBuilder();
        for (Operator op : goal.getPlan()) {
            if (plan.length() > 0) plan.append(",");
            plan.append(op);
        }
        return plan + ";" + goal.getLivesUsed() + ";" + goal.getEnergyUsed()
                + ";" + search.getNodesExpanded();
    }
}