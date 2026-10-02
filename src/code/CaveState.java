package code;


public final class CaveState {

    private final int x, y;            // explorer position (0-indexed, (0,0) = top-left)
    private final int energy;          // energy remaining
    private final int rope;            // rope length remaining
    private final int lives;           // lives remaining
    private final boolean holdingKey;  // true if carrying a key (max one at a time)

    //the [ositions stored in the CaveExplorer class, but the state of whether they are still locked or on the map is stored here]
    //is there a better way?
    private final int doorsMask;       // bit i set -> door i still locked 

    private final int keysMask;        // bit j set -> key j still lying on the map

    public CaveState(int x, int y, int energy, int rope, int lives,
                     boolean holdingKey, int doorsMask, int keysMask) {
        this.x = x;
        this.y = y;
        this.energy = energy;
        this.rope = rope;
        this.lives = lives;
        this.holdingKey = holdingKey;
        this.doorsMask = doorsMask;
        this.keysMask = keysMask;
    }

    public int getX()             { return x; }
    public int getY()             { return y; }
    public int getEnergy()        { return energy; }
    public int getRope()          { return rope; }
    public int getLives()         { return lives; }
    public boolean isHoldingKey() { return holdingKey; }
    public int getDoorsMask()     { return doorsMask; }
    public int getKeysMask()      { return keysMask; }


    // no locked doors remain 
    public boolean isGoal() {
        return doorsMask == 0 && lives >= 1;
    }

    public boolean isDoorLocked(int doorIndex) { return (doorsMask & (1 << doorIndex)) != 0; }
    public boolean isKeyOnMap(int keyIndex)    { return (keysMask  & (1 << keyIndex))  != 0; }

    
    //shouldnt i actually implement it here? -> CaveExplorer handles the movement rules and calculations since ot has full info about grid and so
    public CaveState moveTo(int nx, int ny, int newEnergy, int newRope, int newLives) {
        return new CaveState(nx, ny, newEnergy, newRope, newLives,
                             holdingKey, doorsMask, keysMask);
    }

    
    public CaveState collectKey(int keyIndex) {
        return new CaveState(x, y, energy, rope, lives,
                             true, doorsMask, keysMask & ~(1 << keyIndex));
    }

    
    public CaveState unlockDoor(int doorIndex) {
        return new CaveState(x, y, energy, rope, lives,
                             false, doorsMask & ~(1 << doorIndex), keysMask);
    }

    // equality (used for repeated-state checking)
    // uses hashcode() for speed, then checks all fields for equality using equals() to avoid false positives  
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CaveState)) return false;
        CaveState s = (CaveState) o;
        return x == s.x && y == s.y && energy == s.energy && rope == s.rope
            && lives == s.lives && holdingKey == s.holdingKey
            && doorsMask == s.doorsMask && keysMask == s.keysMask;
    }

    @Override
    public int hashCode() {
        int h = x;
        h = 31 * h + y;
        h = 31 * h + energy;
        h = 31 * h + rope;
        h = 31 * h + lives;
        h = 31 * h + (holdingKey ? 1 : 0);
        h = 31 * h + doorsMask;
        h = 31 * h + keysMask;
        return h;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ") E=" + energy + " R=" + rope + " L=" + lives
             + " key=" + holdingKey + " doors=" + Integer.toBinaryString(doorsMask)
             + " keys=" + Integer.toBinaryString(keysMask);
    }
}