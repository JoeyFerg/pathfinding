import java.awt.event.KeyEvent;

/**
 * InteractivePathfinderVisualizer visualizes the terrain and the computed
 * path and allows the user to change parameters of the Pathfinder.  Below
 * is a list of commands and what they do.  Note: some of the commands
 * modify the properties of the path finder and some modify the terrain.
 *
 * Command        Action                                      Terrain
 * C              clear path
 * space          recompute path
 * S/E            set start/end location to mouse
 * left/right     halve/double path search heuristic value
 * 0/1            set path search heuristic to 0 or 1
 * W              start a walker to show the path
 * R              randomly generate a new terrain                *
 * M              smooths terrain under mouse                    *
 * shift+M        smooths all terrain                            *
 * up/down        increase/decrease terrain under mouse          *
 */

@SuppressWarnings("InfiniteLoopStatement")
public class InteractivePathfinderVisualizer {
    private final static int DELAY = 10;

    // sets how many redraws it should take for the walker to reach its destination
    private final static float STEPS_TO_WALK = 100.0f;

    // for convenience, here are the emap files that are located under heightmaps/ folder
    // the python script in that folder converts png files to emap. \
    // you can use it to create your own maps to test your code
    private final static String[] emaps = {
            "maze32_0.png.emap",    // 0   (32x32 maze)
            "maze32_1.png.emap",    // 1   (another 32x32 maze)
            "maze232_0.png.emap",   // 2   (232x232 maze)
            "maze320_0.png.emap",   // 3   (same as maze32_0, only scaled up by 10x)
            "mazeAB.png.emap",      // 4   (a maze from a site no longer online :( )
            "mazeBrain.png.emap",   // 5   (...)
            "ramp.png.emap",        // 6   (some tests)
            "ramp2.png.emap",       // 7   (...)
            "ramp3.png.emap",       // 8   (...)
            "usa128.png.emap",      // 9   (elevation map of USA from https://bananas.openttd.org/en/heightmap/)
            "usa256.png.emap",      // 10  (same as usa128, only higher resolution)
            "usa1024.png.emap",     // 11  (same as usa128, only higher resolution
    };

    private final static String emapFilename = emaps[11];        // change index to load a different elevation map



    public static void main(String[] args) {
        Terrain terrain = new Terrain(emapFilename);
        Pathfinder pf = new Pathfinder(terrain);
        int N = terrain.getN();

        StdDraw.show(0);

        // set default starting and ending locations
        pf.setPathStart(new Coord(1, 1));
        pf.setPathEnd(new Coord(N-3, N-3));

        // find a path
        pf.computePath();

        PathfinderVisualizer.draw(terrain, pf, null, N);
        StdDraw.show(DELAY);

        // prevent repeated applications
        boolean keyboardR = false;
        boolean keyboardM = false;

        Walker walker = null;

        while(true) {
            boolean recompute = false;
            boolean redraw = false;

            // mouse location on map
            int mouseI = (int) ((StdDraw.mouseX()-24) / 464 * N);
            int mouseJ = (int) ((464 - (StdDraw.mouseY()-36)) / 464 * N);
            boolean isMouseOnMap = (mouseI >=0 && mouseI < N && mouseJ >= 0 && mouseJ < N);

            if(StdDraw.isKeyPressed(KeyEvent.VK_LEFT) || StdDraw.isKeyPressed(KeyEvent.VK_RIGHT)) {
                boolean keyboardLeft = StdDraw.isKeyPressed(KeyEvent.VK_LEFT);
                float m = keyboardLeft ? 0.5f : 2.0f;
                float h = pf.getHeuristic() * m;
                h = Math.max(1.0f/1024.0f, Math.min(1048576.0f, h));
                pf.setHeuristic(h);
                recompute = true;
            }

            if(StdDraw.isKeyPressed(KeyEvent.VK_0) || StdDraw.isKeyPressed(KeyEvent.VK_1)) {
                boolean keyboard0 = StdDraw.isKeyPressed(KeyEvent.VK_0);
                float h = keyboard0 ? 0 : 1;
                pf.setHeuristic(h);
                recompute = true;
            }

            if(StdDraw.isKeyPressed(KeyEvent.VK_C)) {
                pf.resetPath();
                walker = null;
                redraw = true;
            }

            if(StdDraw.isKeyPressed(KeyEvent.VK_R) && !keyboardR) {
                TerrainEditor.setFractalHeights(terrain);
                TerrainEditor.clampHeights(terrain);
                recompute = true;
            }
            keyboardR = StdDraw.isKeyPressed(KeyEvent.VK_R);

            if(StdDraw.isKeyPressed(KeyEvent.VK_M)) {
                if(StdDraw.isKeyPressed(KeyEvent.VK_SHIFT)) {
                    if(!keyboardM) {
                        TerrainEditor.smoothHeights(terrain);
                        recompute = true;
                    }
                } else {
                    TerrainEditor.smoothHeights(mouseI, mouseJ, N / 20, true, terrain);
                    recompute = true;
                }
            }
            keyboardM = StdDraw.isKeyPressed(KeyEvent.VK_M);

            if(StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
                recompute = true;
            }

            if(StdDraw.isKeyPressed(KeyEvent.VK_UP) || StdDraw.isKeyPressed(KeyEvent.VK_DOWN)) {
                boolean keyboardUp = StdDraw.isKeyPressed(KeyEvent.VK_UP);
                float add = keyboardUp ? 8.0f : -8.0f;
                TerrainEditor.addHeight(mouseI, mouseJ, N / 20, add, terrain);
                recompute = true;
            }

            if(isMouseOnMap && StdDraw.isKeyPressed(KeyEvent.VK_S)) {
                pf.setPathStart(new Coord(mouseI, mouseJ));
                recompute = true;
            }

            if(isMouseOnMap && StdDraw.isKeyPressed(KeyEvent.VK_E)) {
                pf.setPathEnd(new Coord(mouseI, mouseJ));
                recompute = true;
            }

            if(StdDraw.isKeyPressed(KeyEvent.VK_W)) {
                Iterable<Coord> path = pf.getPathSolution();
                if(path != null) {
                    walker = new Walker(terrain, pf.getPathSolution());
                }
            }

            if(walker != null) {
                if(walker.doneWalking()) {
                    walker = null;
                } else {
                    walker.advance(pf.getPathCost() / STEPS_TO_WALK);
                }
                redraw = true;
            }

            if(recompute) {
                walker = null;
                pf.resetPath();
                if(pf.getPathStart() != null && pf.getPathEnd() != null) {
                    pf.computePath();
                }
                redraw = true;
            }

            if(redraw) {
                PathfinderVisualizer.draw(terrain, pf, walker, N);
                StdDraw.show(DELAY);
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    /* do nothing */
                }
            }
        }
    }
}
