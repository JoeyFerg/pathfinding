/**
 * Coordinates is an immutable type that store the tuple (i, j)
 */

public final class Coord {
    private final int i;
    private final int j;

    public Coord(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public int getI() { return i; }
    public int getJ() { return j; }

    public boolean isInBounds(int minI, int minJ, int maxI, int maxJ) {
        return i >= minI && j >= minJ && i <= maxI && j <= maxJ;
    }

    public boolean isInBounds(Coord min, Coord max) {
        return i >= min.i && i <= max.i && j >= min.j && j <= max.j;
    }

    public Coord add(int addI, int addJ) {
        return new Coord(i + addI, j + addJ);
    }

    public Coord add(Coord that) {
        return new Coord(i + that.i, j + that.j);
    }

    public String toString() {
        return "(" + i + "," + j + ")";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o.getClass() != this.getClass()) return false;
        Coord that = (Coord) o;

        if (this.i != that.i) return false;
        if (this.j != that.j) return false;

        return true;
    }
}
