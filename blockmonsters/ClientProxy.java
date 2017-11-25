package blockmonsters;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import CoroUtil.render.RenderNull;
import blockmonsters.client.entity.RenderMovingBlock;
import blockmonsters.entity.BlockMonster1;
import blockmonsters.entity.block.StructureBlock;
import blockmonsters.input.ControlInputs;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    public static Minecraft mc;

    public ClientProxy()
    {
        mc = FMLClientHandler.instance().getClient();
    }

    @Override
    public void init(BlockMonsters pMod) {
    	
        super.init(pMod);
        
    	MinecraftForge.EVENT_BUS.register(new SoundLoader());
        
        TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
        
		RenderingRegistry.registerEntityRenderingHandler(StructureBlock.class, new RenderMovingBlock());
		RenderingRegistry.registerEntityRenderingHandler(BlockMonster1.class, new RenderNull());
        
        //MinecraftForgeClient.registerItemRenderer(HostileWorlds.itemLaserBeam.itemID, new WeaponRenderer());
        
    }
    
    @Override
    public void preInit(FMLPreInitializationEvent event) {
    	//note, common proxy doesnt call super for preInit of this
    	ControlInputs.nbtLoad(event);
    }
}
