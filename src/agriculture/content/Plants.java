package agriculture.content;

import agriculture.entities.*;
import agriculture.graphics.*;
import arc.*;
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

    public static PlantType spore, moyuzo;

    public static void load(){
        spore = new PlantType(Items.sporePod, 15){
            @Override
            public void draw(Plant plant){
                int index = Mathf.randomSeed(plant.id, 0, Blocks.sporeCluster.variantRegions.length - 1);
                float scale = Math.max(8 * Mathf.curve(plant.growth, 0f, 100f), 1);
                Draw.rect(Blocks.sporeCluster.variantRegions[index], plant.x, plant.y, scale, scale);
            }
        };

        moyuzo = new PlantType(PlantItems.moyuzoFruit, 5){
            {
                growthSpeed = 0.6f;
                minGrowth = 50f;
                minWater = 45f;
                maxWater = 90f;
            }

            @Override
            public void load(){
                regions = new TextureRegion[]{
                Core.atlas.find("goobriculture-bush-1"),
                Core.atlas.find("goobriculture-bush-2"),
                Core.atlas.find("goobriculture-bush-3"),
                Core.atlas.find("goobriculture-moyuzo-overlay")
                };
            }

            @Override
            public void draw(Plant plant){
                int index = Mathf.randomSeed(plant.id, 0, regions.length - 2);
                float scale = Math.max(8 * Mathf.curve(plant.growth, 0f, 50f), 1);
                AgriDrawf.drawWave(regions[index], plant.x, plant.y, scale, scale, index);

                scale = 8 * Mathf.curve(plant.growth, 50f, 100f);
                Draw.rect(regions[3], plant.x, plant.y, scale, scale);
            }
        };
    }
}
