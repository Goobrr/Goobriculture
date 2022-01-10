package agriculture.world.blocks.farming;

import agriculture.content.*;
import agriculture.entities.*;
import agriculture.ui.elements.*;
import agriculture.ui.tables.*;
import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.logic.LAccess;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class PlantBlock extends Block {
    public float dryRate = 0.01f;
    public TextureRegion soilRegion, wetSoilRegion;

    public PlantBlock(String name){
        super(name);
        update = true;
        updateInUnits = false;

        category = Category.crafting;
        buildVisibility = BuildVisibility.shown;
        configurable = true;

        acceptsItems = true;
        hasItems = true;
        itemCapacity = 1;
        allowConfigInventory = false;
        unloadable = false;

        envEnabled = Env.terrestrial;

        config(Integer.class, (PlantBuild b, Integer i) -> {
            if(i == -1){
                b.plant.type = null;
                b.plant.reset();
                b.items.clear();
            }else if(b.plant.type == null){
                b.plant.type = Plants.ids.get(i, Plants.ids.get(1));
                b.items.clear();
                b.items.set(b.plant.type.item, 1);
                b.plant.reset();
            }
        });

        config(Float.class, (PlantBuild b, Float f) -> {
            b.waterLevel = f;
        });
    }

    @Override
    public void load() {
        super.load();
        soilRegion = Core.atlas.find(name + "-soil", "dirt1");
        wetSoilRegion = Core.atlas.find(name + "-soil-wet", "mud1");
    }

    @Override
    public void setBars() {
        super.setBars();
        bars.remove("items");
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.itemCapacity);
    }

    public class PlantBuild extends Building {
        public float waterLevel = 0;
        public Plant plant;

        @Override
        public void created() {
            super.created();
            plant = new Plant(null, x, y);
        }

        @Override
        public void updateTile() {
            super.updateTile();
            plant.update(waterLevel);

            if(Weathers.rain.isActive()){
                addWater((dryRate * 2) * Time.delta);
            }else{
                addWater((-dryRate * (plant.type == null ? 1f : plant.type.absorbSpeed)) * Time.delta);
            }
        }

        @Override
        public void display(Table table) {
            super.display(table);
            table.row();
            table.table(t -> {
                t.top().left();
                t.add(new ValueBar("Water Level", () -> Mathf.floor(waterLevel) + "%", Blocks.water.mapColor, () -> waterLevel, 100f)).top().left().growX();
                t.row();
                t.add(new PlantInfoTable(plant)).grow().padTop(5f);
            }).growX().padTop(5f);
        }
        @Override
        public void buildConfiguration(Table table) {
            super.buildConfiguration(table);
            table.button("water", () -> {
                waterLevel = 100;
                configure(waterLevel);
            });
            table.button("spore", () -> {
                setPlant(Plants.spore);
            });
            table.button("remove", this::removePlant);
            table.button("perfect", () -> {
                if(plant.type == null) return;
                waterLevel = plant.type.maxWater;
                plant.health = plant.growth = 100f;
            });
            table.button("all", () -> {
                Vars.world.tiles.each((x, y) -> {
                    Building b = Vars.world.build(x, y);
                    if(b instanceof PlantBuild f){
                        f.removePlant();
                        f.setPlant(Plants.moyuzo);
                        f.waterLevel = f.plant.type.maxWater;
                        f.plant.health = f.plant.growth = 100f;
                    }
                });
            });
        }


        @Override
        public boolean onConfigureTileTapped(Building other) {
            if(self() == other) harvest(Vars.player.unit());
            return true;
        }

        public float addWater(float water){
            float w = waterLevel;
            waterLevel = Mathf.clamp(waterLevel + water, 0f, 100f);
            return waterLevel - w;
        }

        public boolean plantValid(){
            return plant != null && plant.type != null;
        }

        public void setPlant(PlantType type){
            configure(type.id);
        }

        public void removePlant(){
            configure(-1);
        }

        public void harvest(Unit unit){
            if(plant.type != null && (unit.acceptsItem(plant.type.item) || plant.produce() + unit.stack.amount < unit.itemCapacity())){
                Call.takeItems(this, plant.type.item, plant.type.itemAmount, unit);
            }
        }

        @Override
        public int removeStack(Item item, int amount) {
            if(plant.type == null || plant.type.item != item) return 0;
            ItemStack harvest = plant.harvest();
            removePlant();
            noSleep();
            return harvest == null ? 0 : harvest.amount;
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            return plant.type == null ? Plants.items.containsKey(item.id) ? 1 : 0 : 0;
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source) {
            Log.info("handle stack");
            noSleep();
            if(plant.type != null) return;
            setPlant(Plants.items.get(item.id));
        }

        @Override
        public void draw() {
            drawSoil();
            super.draw();
            plant.draw();
        }

        public void drawSoil(){
            Draw.rect(soilRegion, x, y);
            Draw.alpha(waterLevel / 100f);
            Draw.rect(wetSoilRegion, x, y);
            Draw.alpha(1);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            if(revision >= 2){
                waterLevel = read.f();
                int id = read.i();
                if(id != -1) setPlant(Plants.ids.get(id));
            }
            if(revision >= 3){
                plant.health = read.f();
                plant.growth = read.f();
                plant.alive = read.bool();
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);

            write.f(waterLevel);
            write.i(plant.type == null ? -1 : plant.type.id);

            write.f(plant.health);
            write.f(plant.growth);
            write.bool(plant.alive);
        }

        @Override
        public byte version() {
            return 3;
        }
    }
}
