package agriculture.content;

import agriculture.entities.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import mindustry.content.*;

public class Plants {
    public static IntMap<PlantType> ids = new IntMap<>();
    public static IntMap<PlantType> items = new IntMap<>();
    public static int id = 0;
    public static void register(PlantType type){
        ids.put(id, type);
        items.put(type.item.id, type);
        type.id = id;
        id++;
    }

    public static PlantType

    spore = new PlantType("sporetree", Items.sporePod, 15){
        @Override
        public void draw(Plant plant) {
            int index = Mathf.randomSeed(plant.id, 0, Blocks.sporeCluster.variantRegions.length - 1);
            TextureRegion region = Blocks.sporeCluster.variantRegions[index];
            float scale = Math.max(8 * Mathf.curve(plant.growth, 0f, 100f), 1);
            Draw.rect(region, plant.x, plant.y, scale, scale);
        }
    };
}
