import java.lang.IndexOutOfBoundsException;

/**
 * Terrain stores elevations for map and computes distance and
 * cost in traversing the terrain.
 */

public class Terrain {
    private int N;
    private int[][] heights;

    public Terrain(int N) {
        this.N = N;
        this.heights = new int[N][N];
    }

    public Terrain(int[][] heights) {
        this.N = heights.length;
        this.heights = heights;
    }

    public Terrain(String emapfile) {
        In in = new In(emapfile);
        this.N = in.readInt();
        this.heights = new int[N][N];
        for (int j = 0; j < N; j++) {
            for (int i = 0; i < N; i++) {
                this.heights[i][j] = in.readInt();
            }
        }
    }

    public int getN() {
        return N;
    }

    public void setHeight(int i, int j, int h) {
        if(i < 0 || j < 0 || i >= N || j >= N)
            throw new IndexOutOfBoundsException("i and j must be in [0,N)");
        heights[i][j] = h;
    }
    public void setHeight(int i, int j, float h) {
        setHeight(i, j, (int) h);
    }
    public void setHeight(Coord loc, int h) {
        setHeight(loc.getI(), loc.getJ(), h);
    }
    public void setHeight(Coord loc, float h) {
        setHeight(loc.getI(), loc.getJ(), h);
    }

    public int getHeight(int i, int j) {
        if(i < 0 || j < 0 || i >= N || j >= N)
            throw new IndexOutOfBoundsException("i and j must be in [0,N)");
        return heights[i][j];
    }
    public int getHeight(Coord loc) {
        return getHeight(loc.getI(), loc.getJ());
    }

    // computes distance between (i0,j0) and (i1,j1) as the crow flies
    public float computeDistance(int i0, int j0, int i1, int j1) {
        return (float)Math.sqrt((i0 - i1) * (i0 - i1) + (j0 - j1) * (j0 - j1));
    }
    public float computeDistance(Coord c0, Coord c1) {
        return computeDistance(c0.getI(), c0.getJ(), c1.getI(), c1.getJ());
    }

    public float computeTravelCost(int i0, int j0, int i1, int j1) {
        float h0 = getHeight(i0, j0), h1 = getHeight(i1, j1);
        float climb = (float)Math.pow(Math.abs(h0 - h1) * 1000.0, 1.5);
        float dist = computeDistance(i0,j0, i1,j1);
        return (1.0f + climb) * dist;
    }
    public float computeTravelCost(Coord c0, Coord c1) {
        return computeTravelCost(c0.getI(), c0.getJ(), c1.getI(), c1.getJ());
    }
    public float computeTravelCost(Iterable<Coord> cs) {
        Coord c0 = null;
        float cost = 0.0f;
        for(Coord c1 : cs) {
            if(c0 != null) cost += computeTravelCost(c0, c1);
            c0 = c1;
        }
        return cost;
    }
}
