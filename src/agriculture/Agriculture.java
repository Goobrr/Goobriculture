package agriculture;

import agriculture.content.*;
import agriculture.logic.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.mod.*;

public class Agriculture extends Mod{

    public Agriculture(){

    }

    @Override
    public void loadContent(){
        Plants.load();
        AgricultureBlocks.load();

        // logic
        LAssembler.customParsers.put("plantsensor", args -> new PlantSensorStatement(args[1], args[2], args[3]));
        LogicIO.allStatements.add(PlantSensorStatement::new);
    }

}
