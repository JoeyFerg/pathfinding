/**
 * The following class provides some simple terrain editing functions.
 */

public class TerrainEditor {
    private static float rndRadius(float d) {
        return (float)StdRandom.uniform(-d,d);
    }

    private static float getFractalHeight(int i, int j, float dh, Terrain terrain, boolean[] isset) {
        int N = terrain.getN();
        dh = Math.max(-64, Math.min(255+64, dh));   // allow the height to go just a bit out of range
        if(!isset[i + j * N]) {
            isset[i + j * N] = true;
            terrain.setHeight(i, j, dh);
        }
        return terrain.getHeight(i,j);
    }

    private static void setFractalHeights(int i0, int j0, int i1, int j1, Terrain terrain, boolean[] isset) {
        int id = i1-i0;
        int jd = j1-j0;
        int i01 = (i0+i1)/2;
        int j01 = (j0+j1)/2;

        if(id <= 1 && jd <= 1) return;

        float h00 = getFractalHeight(i0,j0,32,terrain,isset);
        float h01 = getFractalHeight(i0,j1,32,terrain,isset);
        float h10 = getFractalHeight(i1,j0,32,terrain,isset);
        float h11 = getFractalHeight(i1,j1,32,terrain,isset);

        float d = (float)(Math.sqrt((i1-i0)*(i1-i0) + (j1-j0)*(j1-j0)) * 0.5);

        float d0001 = (h00 + h01) / 2 + rndRadius(d);
        float d0010 = (h00 + h10) / 2 + rndRadius(d);
        float d1101 = (h11 + h01) / 2 + rndRadius(d);
        float d1110 = (h11 + h10) / 2 + rndRadius(d);

        float h0001 = getFractalHeight(i0, j01, d0001, terrain,isset);
        float h0010 = getFractalHeight(i01, j0, d0010, terrain,isset);
        float h1101 = getFractalHeight(i01, j1, d1101, terrain,isset);
        float h1110 = getFractalHeight(i1, j01, d1110, terrain,isset);

        float dm = (float)((h0001 + h0010 + h1101 + h1110) / 4.0);
        float hm = getFractalHeight(i01, j01, dm, terrain,isset);

        if(id > 1 && jd > 1) {
            setFractalHeights(i0, j0, i01, j01, terrain, isset);
            setFractalHeights(i01, j0, i1, j01, terrain, isset);
            setFractalHeights(i0, j01, i01, j1, terrain, isset);
            setFractalHeights(i01, j01, i1, j1, terrain, isset);
        } else if(jd > 1) {
            setFractalHeights(i0, j0, i1, j01, terrain, isset);
            setFractalHeights(i0, j01, i1, j1, terrain, isset);
        } else {
            setFractalHeights(i0, j0, i01, j1, terrain, isset);
            setFractalHeights(i01, j0, i1, j1, terrain, isset);
        }
    }

    public static void setFractalHeights(Terrain terrain) {
        int N = terrain.getN();
        boolean[] isset = new boolean[N*N];
        setFractalHeights(0, 0, N - 1, N - 1, terrain, isset);
    }

    public static void smoothHeights(Terrain terrain) {
        int N = terrain.getN();
        int radius = (int)Math.ceil(Math.sqrt(N*N/2.0f));
        smoothHeights(N/2, N/2, radius, false, terrain);
    }

    public static void smoothHeights(int ic, int jc, int radius, boolean fallOff, Terrain terrain) {
        final int N = terrain.getN();
        final int w = 2*radius+1;
        float[] s = new float[w*w];
        for(int ia = -radius; ia <= radius; ia++) {
            int i = ic + ia;
            if(i < 0 || i >= N) continue;
            for(int ja = -radius; ja <= radius; ja++) {
                int j = jc + ja;
                if(j < 0 || j >= N) continue;
                int sidx = (ia+radius) + (ja+radius)*w;

                float m = computeEffect(i, j, ic, jc, radius, fallOff, N);
                if(m <= 0.0000001) {
                    s[sidx] = terrain.getHeight(i,j);
                    continue;
                }
                float h = 0;
                int c = 0;
                if(i > 0 && j > 0) {
                    h += terrain.getHeight(i-1,j-1);
                    c += 1;
                }
                if(i < N-1 && j > 0) {
                    h += terrain.getHeight(i+1,j-1);
                    c += 1;
                }
                if(i > 0 && j < N-1) {
                    h += terrain.getHeight(i-1,j+1);
                    c += 1;
                }
                if(i < N-1 && j < N-1) {
                    h += terrain.getHeight(i+1,j+1);
                    c += 1;
                }
                if(i > 0) {
                    h += terrain.getHeight(i-1,j);
                    c += 1;
                }
                if(j > 0) {
                    h += terrain.getHeight(i,j-1);
                    c += 1;
                }
                if(i < N-1) {
                    h += terrain.getHeight(i+1,j);
                    c += 1;
                }
                if(j < N-1) {
                    h += terrain.getHeight(i,j+1);
                    c += 1;
                }
                s[sidx] = (1-m) * terrain.getHeight(i,j) + m * h / c;
            }
        }
        for(int ia = -radius; ia <= radius; ia++) {
            int i = ic + ia;
            if(i < 0 || i >= N) continue;
            for(int ja = -radius; ja <= radius; ja++) {
                int j = jc + ja;
                if(j < 0 || j >= N) continue;
                int sidx = (ia+radius) + (ja+radius)*w;
                terrain.setHeight(i, j, s[sidx]);
            }
        }
    }

    public static void clampHeights(Terrain terrain) {
        int N = terrain.getN();
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                terrain.setHeight(i, j, Math.max(0, Math.min(255, terrain.getHeight(i, j))));
            }
        }
    }

    private static float computeEffect(int i, int j, int ic, int jc, int radius, boolean fallOff, int N) {
        if(i < 0 || j < 0 || i >= N || j >= N) return 0;
        if(!fallOff) return 1;
        int d2 = (ic-i)*(ic-i)+(jc-j)*(jc-j);
        if(d2 > radius*radius) return 0;
        return ((float)radius - (float)Math.sqrt(d2)) / (float)radius;
    }

    public static void addHeight(int i, int j, int radius, float add, Terrain terrain) {
        final int N = terrain.getN();
        for(int ja = -radius; ja <= radius; ja++) {
            int r = j + ja;
            if(r < 0 || r >= N) continue;
            for(int ia = -radius; ia <= radius; ia++) {
                int c = i + ia;
                if(c < 0 || c >= N) continue;
                float v = add * computeEffect(c, r, i, j, radius, true, N);
                float h = terrain.getHeight(c, r) + v;
                h = Math.max(0, Math.min(255, h));
                terrain.setHeight(c, r, h);
            }
        }
    }
}
