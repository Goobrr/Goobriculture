package agriculture.content;

import agriculture.world.blocks.farming.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.world.*;

public class AgricultureBlocks {
    public static Block farmland, sprinklerSmall, harvester, sensorSmall;

    public static void load() {
        farmland = new Farmland("farmland");
        sprinklerSmall = new Waterer("small-sprinkler", 60f, 1.5f){{
            consumes.power(0.1f);
            consumes.liquid(Liquids.water, 0.15f);
        }};
        harvester = new RailHarvester("harvester", 10){{
            consumes.power(1f);
            itemCapacity = 60;
        }};
        sensorSmall = new WaterSensor("small-water-sensor", 60f){{
            consumes.power(0.5f);
        }};
    }
}
