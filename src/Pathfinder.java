import java.lang.IndexOutOfBoundsException;
import java.lang.IllegalArgumentException;
import java.util.LinkedList;

/**
 * Programmed by Joey Ferguson
 * 8 February 2018
 */

// Pathfinder uses A* search to find a near optimal path between to locations with given terrain
public class Pathfinder {
    private Coord startLoc, endLoc;         // Start and end locations
    private float heuristic;                // Heuristic values of the path
    private int N;                          // Size of the board
    private int searchSize;                 // How much of the map it searches
    private boolean found;                  // Whether the path has been found
    private boolean[][] usedMatrix;         // Boolean matrix of whether locations are used
    private PFNode currentNode;             // Current node
    private PFNode endNode;                 // End node
    private Terrain t;                      // Terrain instance


    // PFNode will be the key for MinPQ (used in computePath())
    public class PFNode implements Comparable<PFNode> {
        private Coord location;             // Location coordinate
        private PFNode prev;                // Previous node
        private float cost;                 // Cost
        private float heuristicCost;        // Heuristic cost

        private PFNode(Coord loc, PFNode fromNode) {
            location = loc;
            prev = fromNode;

            if ( loc == startLoc ) {
                cost = 0.0f;
                heuristicCost = getHeuristic();
            }
            else {
                cost = t.computeTravelCost(this.prev.location, this.location) + this.prev.cost;
                heuristicCost = cost + getHeuristic() * t.computeTravelCost(this.location, endLoc);
            }
        }

        // Compares this with that, used to find minimum cost PFNode
        public int compareTo(PFNode that) {
            return Float.compare(this.heuristicCost, that.heuristicCost);
        }

        // Returns the cost of travel
        public float getCost(float heuristic) {
            return this.heuristicCost;
        }

        // Returns if the PFNode has been used
        private boolean isUsed() {
            return usedMatrix[this.location.getI()][this.location.getJ()];
        }

        // Flags the PFNode as used
        private void use() {
            usedMatrix[this.location.getI()][this.location.getJ()] = true;
        }

        // Returns an Iterable of PFNodes that surround this
        private Iterable<PFNode> neighbors() {
            LinkedList<PFNode> LL = new LinkedList<>();

            // Stores i and j for easier reference
            int i = location.getI();
            int j = location.getJ();

            // Initialize coordinates
            Coord right; Coord left; Coord up; Coord down;

            // Right
            right = new Coord(i+1, j);
            if ( right.isInBounds(0, 0, N-1, N-1) ) {
                if ( !usedMatrix[i+1][j] ) {
                    LL.add(new PFNode(right, this));
                    searchSize++;
                }
            }

            // Left
            left = new Coord(i-1, j);
            if ( left.isInBounds(0, 0, N-1, N-1) ) {
                if ( !usedMatrix[i-1][j] ) {
                    LL.add(new PFNode(left, this));
                    searchSize++;
                }
            }

            // Up
            up = new Coord(i, j-1);
            if ( up.isInBounds(0, 0, N-1, N-1) ) {
                if ( !usedMatrix[i][j-1] ) {
                    LL.add(new PFNode(up, this));
                    searchSize++;
                }
            }

            // Down
            down = new Coord(i, j+1);
            if ( down.isInBounds(0, 0, N-1, N-1) ) {
                if ( !usedMatrix[i][j+1] ) {
                    LL.add(new PFNode(down, this));
                    searchSize++;
                }
            }
            return LL;
        }
    }

    // Initializes terrain and searchSize
    public Pathfinder(Terrain terrain) {
        t = terrain;
        N = terrain.getN();
        searchSize = 0;

        // Initializes matrix of PFNodes
        usedMatrix = new boolean[N][N];
    }

    // Set the start location of the path
    public void setPathStart(Coord loc) {
        if ( loc == null ) throw new IllegalArgumentException("Cannot start at null location.");
        if ( !loc.isInBounds(0, 0, t.getN() - 1, t.getN() - 1) ) throw new IndexOutOfBoundsException("Cannot start out of bounds.");

        startLoc = new Coord(loc.getI(), loc.getJ());
    }

    // Returns the start location of the path
    public Coord getPathStart() {
        return startLoc;
    }

    // Sets the end location of the path
    public void setPathEnd(Coord loc) {
        if ( loc == null ) throw new IllegalArgumentException("Cannot end at null location.");
        if ( !loc.isInBounds(0, 0, t.getN() - 1, t.getN() - 1) ) throw new IndexOutOfBoundsException("Cannot end out of bounds.");

        endLoc = new Coord(loc.getI(), loc.getJ());
    }

    // Returns the end location of the path
    public Coord getPathEnd() {
        return endLoc;
    }

    // Sets the heuristic value
    public void setHeuristic(float v) {
        heuristic = v;
    }

    // Returns the heuristic value
    public float getHeuristic() {
        return heuristic;
    }

    // Resets the path when it recalculates
    public void resetPath() {
        usedMatrix = new boolean[N][N];
        searchSize = 0;
    }

    // Figures out the path of least resistance from start to end
    public void computePath() {
        if ( startLoc == null || endLoc == null ) throw new IllegalArgumentException("Start and end location cannot be null.");

        MinPQ<PFNode> mpq = new MinPQ<>();
        PFNode startNode = new PFNode(this.startLoc, null);
        mpq.insert(startNode);

        found = false;

        // Checks all the rest of the neighbors and adds onto MinPQ if not used yet
        while ( !found ) {
            currentNode = mpq.delMin();
            if (currentNode.location.equals(endLoc)) {
                found = true;
                endNode = currentNode;
                return;
            }
            for ( PFNode N : currentNode.neighbors() ) {
                if ( !N.isUsed() ) {
                    mpq.insert(N);
                    N.use();
                }
            }
        }
    }

    // Returns if the path has been found yet
    public boolean foundPath() {
        return found;
    }

    // Gets the cost of taking the path it generates
    public float getPathCost() {
        return endNode.cost;
    }

    // Returns how many locations it searched on the board
    public int getSearchSize() {
        return searchSize;
    }

    // Returns an iterable stack of the path solution
    public Iterable<Coord> getPathSolution() {
        Stack<Coord> solution = new Stack<>();
        currentNode = endNode;

        while ( currentNode.prev != null) {
            solution.push(currentNode.location);
            currentNode = currentNode.prev;
        }
        return solution;
    }

    // Returns how many locations were searched on the board
    public boolean wasSearched(Coord loc) { return usedMatrix[loc.getI()][loc.getJ()]; }
}
