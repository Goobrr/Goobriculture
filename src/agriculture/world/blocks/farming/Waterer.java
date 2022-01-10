package agriculture.world.blocks.farming;

import agriculture.content.*;
import agriculture.world.blocks.*;
import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;

public class Waterer extends AreaBlock {
    public float amount;
    public float rotateSpeed = 360f;
    public TextureRegion topRegion;

    public Waterer(String name, float range, float amount){
        super(name, range);
        this.amount = amount;

        hasLiquids = true;
        consumesPower = true;
        hasPower = true;

        radiusColor = Pal.accent;

        loopSound = Sounds.spray;

        envEnabled = Env.terrestrial;
    }

    @Override
    public void load() {
        super.load();
        topRegion = Core.atlas.find(name + "-top");
    }

    public class WatererBuild extends AreaEffectBuild {
        public float topRotation, topRotationSpeed = 0f;

        @Override
        public void updateTile() {
            super.updateTile();
            if(!valid()){
                topRotationSpeed = Mathf.lerpDelta(topRotationSpeed, 0, 0.01f);
            }
            topRotation += topRotationSpeed;
        }

        @Override
        public void whileValid() {
            if(Mathf.chanceDelta(0.8f)){
                Tmp.v3.setZero().trns(Mathf.range(0, 360), range * Mathf.random()).add(this);

                AgriFx.splash.at(Tmp.v3.x, Tmp.v3.y, Color.valueOf("7a95eaff"));
            }

            topRotationSpeed = Mathf.lerpDelta(topRotationSpeed, rotateSpeed / 60f, 0.05f);
        }

        @Override
        public boolean valid() {
            return cons.valid() && enabled;
        }

        @Override
        public boolean filter(Building b) {
            return b instanceof PlantBlock.PlantBuild;
        }

        @Override
        public void effect(Building b) {
            ((PlantBlock.PlantBuild) b).addWater(((amount / 60f) * (1 - Mathf.curve(Mathf.dst(x, y, b.x, b.y), 0, range)/3)) * Time.delta);
        }

        @Override
        public boolean shouldActiveSound() {
            return valid();
        }

        @Override
        public void draw() {
            super.draw();
            Draw.z(Layer.block + 1);
            Tmp.c1.set(Color.valueOf("7a95eaff")).a(Mathf.curve(topRotationSpeed, 0, rotateSpeed / 60f) / 2);
            Tmp.c2.set(Tmp.c1).a(0); // using Color.clear will make it fade to black, so use the same color with 0 alpha instead
            Fill.light(x, y, Lines.circleVertices(range / 2), range / 2, Tmp.c1, Tmp.c2);
            Draw.z(Layer.block + 2);
            Drawf.spinSprite(topRegion, x, y, topRotation);
        }
    }
}
