import java.awt.Color;
import java.awt.Font;

/**
 * PathfinderVisualizer visualizes the terrain and found path.
 */

public class PathfinderVisualizer {

    // color for different elevations.  must be in ascending order!
    private final static int[][] elevationColors = {
            // h    r    g    b   where h=height, r=red, g=green, b=blue
            {  0,   0,   0,   0},
            {  2,   0,   0, 160},
            {  4,   0, 224, 224},
            { 16, 192, 192,   0},
            { 64,  16, 255,  16},
            {128,  32, 224,  32},
            {192, 128, 128,   8},
            {224, 164, 164, 164},
            {255, 255, 255, 255}
    };

    private final static Color cStart    = new Color(128, 128, 255);
    private final static Color cEnd      = new Color(255, 128, 128);
    private final static Color cSolution = new Color(255, 128, 255);
    private final static Color cSearched = new Color(128,   0, 128);

    // linearly interpolating between the corresponding rgb values
    private static Color colorLERP(float v1, int r0, int g0, int b0, int r1, int g1, int b1) {
        v1 = Math.max(0.0f, Math.min(1.0f, v1));
        float v0 = 1.0f - v1;
        int r = (int)(v0 * r0 + v1 * r1);
        int g = (int)(v0 * g0 + v1 * g1);
        int b = (int)(v0 * b0 + v1 * b1);
        return new Color(r, g, b);
    }
    private static Color colorLERP(float v1, Color c0, Color c1) {
        v1 = Math.max(0.0f, Math.min(1.0f, v1));
        float v0 = 1.0f - v1;
        int r = (int)(v0 * c0.getRed()   + v1 * c1.getRed());
        int g = (int)(v0 * c0.getGreen() + v1 * c1.getGreen());
        int b = (int)(v0 * c0.getBlue()  + v1 * c1.getBlue());
        int a = (int)(v0 * c0.getAlpha() + v1 * c1.getAlpha());
        return new Color(r, g, b, a);
    }

    // returns color for given height
    private static Color height2Color(int h) {
        h = Math.max(0, Math.min(255, h));

        int[] ec0 = elevationColors[0];  // default lower bound
        int[] ec1 = elevationColors[1];  // default upper bound

        // find upper bound while updating lower bound
        for(int i = 1; i < elevationColors.length; i++) {
            ec1 = elevationColors[i];
            if(h >= ec0[0] && h <= ec1[0]) break;
            ec0 = ec1;
        }

        int h0=ec0[0], r0=ec0[1], g0=ec0[2], b0=ec0[3];
        int h1=ec1[0], r1=ec1[1], g1=ec1[2], b1=ec1[3];
        float v1 = ((float)(h - h0)) / ((float)(h1 - h0));

        return colorLERP(v1, r0, g0, b0, r1, g1, b1);
    }

    private static void drawCircle(int i, int j, int N, double rad) {
        int c = (int)((i+0.5f) * 464.0f / N);
        int r = (int)((j+0.5f) * 464.0f / N);
        StdDraw.circle(c + 24, 464 - r + 36, rad);
    }

    private static void drawCircle(Coord loc, int N, double rad) {
        drawCircle(loc.getI(), loc.getJ(), N, rad);
    }

    private static void drawPath(Iterable<Coord> path, int N) {
        boolean first = true;
        int c0 = 0, r0 = 0;
        for (Coord loc : path) {
            int c1 = (int)((loc.getI()+0.5f) * 464.0f / N);
            int r1 = (int)((loc.getJ()+0.5f) * 464.0f / N);
            if(!first) {
                StdDraw.line(c0 + 24, 464 - r0 + 36, c1 + 24, 464 - r1 + 36);
            }
            first = false;
            c0 = c1; r0 = r1;
        }
    }

    // draw N-by-N pathfinding map
    public static void draw(Terrain terrain, Pathfinder pf, Walker walker, int N) {
        StdDraw.clear();
        StdDraw.setXscale(0, 512);
        StdDraw.setYscale(0, 512);
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.filledSquare(256, 256 + 12, 464 / 2);

        // draw terrain
        for(int row = 0; row < 464; row++) {
            int j = (int)((float)row/464.0f * N);
            for(int col = 0; col < 464; col++) {
                int i = (int)((float)col/464.0f * N);
                Coord loc = new Coord(i,j);
                Color c = height2Color(terrain.getHeight(loc));
                if(pf.wasSearched(loc)) {
                    c = colorLERP(0.50f, c, cSearched);
                }
                StdDraw.setPenColor(c);
                StdDraw.filledSquare(col + 24, 464 - row + 36, 1);
            }
        }

        // draw path start
        if(pf.getPathStart() != null) {
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setPenRadius(0.006);
            drawCircle(pf.getPathStart(), N, 4.0);

            StdDraw.setPenColor(cStart);
            StdDraw.setPenRadius(0.002);
            drawCircle(pf.getPathStart(), N, 4.0);
        }

        // draw path end
        if(pf.getPathEnd() != null) {
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setPenRadius(0.006);
            drawCircle(pf.getPathEnd(), N, 4.0);

            StdDraw.setPenColor(cEnd);
            StdDraw.setPenRadius(0.002);
            drawCircle(pf.getPathEnd(), N, 4.0);
        }

        // draw found path
        if(pf.foundPath()) {
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setPenRadius(0.006);
            drawPath(pf.getPathSolution(), N);

            StdDraw.setPenColor(cSolution);
            StdDraw.setPenRadius(0.002);
            drawPath(pf.getPathSolution(), N);
        }

        // draw walker
        if(walker != null) {
            Coord loc = walker.getLocation();
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setPenRadius(0.006);
            PathfinderVisualizer.drawCircle(loc, N, 2.0);

            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.setPenRadius(0.002);
            PathfinderVisualizer.drawCircle(loc, N, 2.0);
        }

        // write status text
        StdDraw.setFont(new Font("SansSerif", Font.PLAIN, 12));
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.text(96, 12, "heuristic: " + pf.getHeuristic());
        if(pf.foundPath()) StdDraw.text(256, 12, "path: " + pf.getPathCost());
        else               StdDraw.text(256, 12, "no path found");
        StdDraw.text(512-96, 12, "searched: " + pf.getSearchSize() + " (" + (int)(100.0f * pf.getSearchSize() / (N*N)) + "%)");
    }
}
