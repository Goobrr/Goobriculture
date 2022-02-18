package agriculture.world.blocks.farming;

import agriculture.world.blocks.*;
import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.logic.*;
import mindustry.world.*;

import static mindustry.Vars.*;

public class RailHarvester extends ConnectedBlock {
    public TextureRegion railRegion, headRegion;
    public RailHarvester(String name, int range){
        super(name, range);

        hasItems = true;
        hasPower = true;
        itemCapacity = 40;
    }

    @Override
    public boolean linkValid(Building b, Building other) {
        return other instanceof RailHarvesterBuild && positionsValid(b.tileX(), b.tileY(), other.tileX(), other.tileY());
    }

    public boolean positionsValid(int x1, int y1, int x2, int y2){
        if(x1 == x2){
            return Math.abs(y1 - y2) <= range;
        }else if(y1 == y2){
            return Math.abs(x1 - x2) <= range;
        }else{
            return false;
        }
    }

    @Override
    public void drawConnection(ConnectedBuild build, ConnectedBuild other) {
        float progress = Interp.pow3Out.apply(((RailHarvesterBuild) build).railProgress / 100f);
        float xo = build.x + (other.x - build.x) * progress;
        float yo = build.y + (other.y - build.y) * progress;
        Lines.stroke(8f);
        Lines.line(railRegion, build.x, build.y, xo, yo, false);
    }

    @Override
    public void load() {
        super.load();
        headRegion = Core.atlas.find(name + "-head");
        railRegion = Core.atlas.find(name + "-rail");
    }

    public class RailHarvesterBuild extends ConnectedBuild {
        public float headX, headY;
        public boolean arrived;
        public float progress, railProgress;

        @Override
        public void created() {
            super.created();
            headX = x;
            headY = y;
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if(valid()){
                if(progress > 95f) updateHead();
                progress = Mathf.clamp(railProgress + Time.delta, 0f, 100f);
            }else{
                headX = Mathf.approach(headX, x, Time.delta);
                headY = Mathf.approach(headY, y, Time.delta);
                progress = 0;
            }
            progress = Mathf.clamp(progress, 0f, 100f);

            if(link != null){
                railProgress = Mathf.clamp(railProgress + Time.delta, 0f, 100f);
            }else{
                railProgress = Mathf.clamp(railProgress - Time.delta, 0f, 100f);
            }
            dump();
        }

        @Override
        public void linkChanged() {
            railProgress = 0;
            progress = 0;
            headX = x;
            headY = y;
        }

        public void updateHead() {
            if(link != null){
                if(!arrived) {
                    if (headX != link.x || headY != link.y) {
                        headX = Mathf.approach(headX, link.x, Time.delta);
                        headY = Mathf.approach(headY, link.y, Time.delta);
                    } else {
                        arrived = true;
                    }
                }else{
                    if (headX != x || headY != y) {
                        headX = Mathf.approach(headX, x, Time.delta);
                        headY = Mathf.approach(headY, y, Time.delta);
                    } else {
                        arrived = false;
                    }
                }

                Building b = world.buildWorld(headX, headY);
                if(b instanceof PlantBlock.PlantBuild p && Mathf.within(headX, headY, b.x, b.y, 4f)){
                    if(p.isAdded() && p.plant.type != null){
                        Item item = p.plant.type.item;

                        if(items.get(item) + p.plant.produce() < block.itemCapacity){
                            int amount = p.removeStack(item, p.plant.produce());

                            float ex = p.x, ey = p.y;
                            for (int j = 0; j < Mathf.clamp(amount / 3, 1, 8); j++) {
                                Time.run(j * 3f, () -> {
                                    Fx.itemTransfer.at(ex, ey, amount, item.color, this);
                                });
                            }

                            Time.run(Fx.itemTransfer.lifetime, () -> items.add(item, amount));
                        }
                    }
                }
            }
        }

        @Override
        public void draw() {
            Draw.z(Layer.blockOver);
            if(link != null) drawConnection(this, link);
            Draw.z(Layer.blockOver + 1);
            Draw.rect(region, x, y);
            Draw.z(Layer.blockOver + 2);
            Draw.rect(headRegion, headX, headY);
        }

        @Override
        public void drawSelect(){
            if(link != null && link != this){
                Drawf.select(x, y, size * tilesize / 2f + 2f, Pal.accent);
                Drawf.select(link.x, link.y, link.block.size * tilesize / 2f + 2f, Pal.place);
            }
        }

        @Override
        public void drawConfigure(){
            Drawf.select(x, y, size * tilesize / 2f + 2f, Pal.accent);

            for(int i = 1; i <= range; i++){
                for(int j = 0; j < 4; j++){
                    Tile other = tile.nearby(Geometry.d4[j].x * i, Geometry.d4[j].y * i);
                    if(linkValid(this, other.build)){
                        boolean linked = other.build == link;

                        Drawf.select(other.drawx(), other.drawy(),
                            other.block().size * tilesize / 2f + 2f + (linked ? 0f : Mathf.absin(Time.time, 4f, 1f)), linked ? Pal.place : Pal.breakInvalid);
                    }
                }
            }
        }

        @Override
        public double sense(LAccess sensor) {
            if(sensor == LAccess.totalItems) return items.total();
            return super.sense(sensor);
        }

        public boolean valid(){
            return cons.valid() && enabled && link != null;
        }
    }
}