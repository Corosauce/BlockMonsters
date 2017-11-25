package blockmonsters;

import blockmonsters.config.ConfigMisc;
import blockmonsters.entity.BlockMonster1;
import blockmonsters.entity.block.StructureBlock;
import blockmonsters.input.ControlInputs;
import CoroUtil.entity.block.MovingBlock;
import net.minecraft.entity.EntityEggInfo;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class CommonProxy implements IGuiHandler
{
    public World mainWorld;
    //private int entityId = 0;

    public BlockMonsters mod;

    public CommonProxy()
    {
    }

    public void init(BlockMonsters pMod)
    {
        mod = pMod;
        
        String prefix = "entity.HostileWorlds.";
        String end = ".name";

        EntityRegistry.registerModEntity(BlockMonster1.class, "BlockMonster1", ConfigMisc.blockMonsterID, pMod, 128, 1, true);
        EntityRegistry.registerModEntity(StructureBlock.class, "StructureBlock", ConfigMisc.structureBlockID, pMod, 512, 1, true);
        EntityList.addMapping(BlockMonster1.class, "BlockMonster1", ConfigMisc.blockMonsterID);
        EntityList.entityEggs.put(Integer.valueOf(ConfigMisc.blockMonsterID), new EntityEggInfo(ConfigMisc.blockMonsterID, 0x00FF00, 0x000000));
        
        LanguageRegistry.instance().addStringLocalization("entity.BlockMonster1.name", "Block Monster");
    }
    
    public void preInit(FMLPreInitializationEvent event) {
    	//super.preInit(pMod, event); - dont call super to prevent a double init of some control input stuff
    	ControlInputs.initDefaultsForServer();
    }

    public World getClientWorld()
    {
        return null;
    }

    public World getServerWorld()
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() != null)
        {
            return FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
        }
        else
        {
            return null;
        }
    }

    public World getSidesWorld()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world,
            int x, int y, int z)
    {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world,
            int x, int y, int z)
    {
        return null;
    }
}
