package code;

// actions the cave explorer can perform. 
public enum Operator {
    LEFT("left"),
    RIGHT("right"),
    CLIMB_UP("climbup"),
    CLIMB_DOWN("climbdown"),
    JUMP_DOWN("jumpdown"),
    COLLECT("collect"),
    UNLOCK("unlock");

    private final String label;

    Operator(String label) {
        this.label = label;
    }

   
    @Override
    public String toString() {
        return label;
    }
}