package agriculture.graphics;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;

public class AgriDrawf{

    // stolen from WavingProp
    public static void drawWave(TextureRegion region, float x, float y, float w, float h, long seed){
        float rotmag = 3f, rotscl = 0.5f,
        rot = Mathf.randomSeedRange(seed, 20f) - 45 + Mathf.sin(Time.time + x, 50f * rotscl, 0.5f * rotmag) + Mathf.sin(Time.time - y, 65f * rotscl, 0.9f* rotmag) + Mathf.sin(Time.time + y - x, 85f * rotscl, 0.9f* rotmag),
        scl = 30f, mag = 0.3f;

        Draw.rectv(region, x, y, w, h, rot, vec -> vec.add(
        Mathf.sin(vec.y*3 + Time.time, scl, mag) + Mathf.sin(vec.x*3 - Time.time, 70, 0.8f),
        Mathf.cos(vec.x*3 + Time.time + 8, scl + 6f, mag * 1.1f) + Mathf.sin(vec.y*3 - Time.time, 50, 0.2f)
        ));
    }
}
