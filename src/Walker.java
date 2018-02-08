import java.util.Iterator;

/**
 * Programmed by Joey Ferguson
 * 8 February 2018
 */

// Walker takes an Iterator of Coords and simulates an individual walking along the path over the given Terrain
public class Walker {
    private Terrain t;
    private Coord prev;
    private Coord current;
    private Iterator<Coord> iterator;

    public Walker(Terrain terrain, Iterable<Coord> path) {
        t = terrain;
        iterator = path.iterator();
        prev = iterator.next();
        current = iterator.next();
    }

    // Returns current location of the walker
    public Coord getLocation() {
        return current;
    }

    // Returns true if walker has reached the end
    public boolean doneWalking() { return !iterator.hasNext(); }

    // Advances the Walker along path
    public void advance(float byTime) {
        float time = t.computeTravelCost(prev, current) / byTime;
        StdOut.println(time);
        try {
            Thread.sleep((long)time);
        }
        catch(InterruptedException e){
            /* do nothing */
        }
        prev = current;
        current = iterator.next();
    }
}
