package blockmonsters;

import java.util.EnumSet;

import blockmonsters.physics.PhysicsWorldManager;
import blockmonsters.playerdata.PlayerData;
import blockmonsters.playerdata.objects.Input;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.bt.Test;
import CoroUtil.playerdata.IPlayerData;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ServerTickHandler implements ITickHandler
{
	public static BlockMonsters mod;
	//public static RtsEngine rts;
	
	public static World lastWorld = null;
	
	public static Test test;
	
    public ServerTickHandler(BlockMonsters parMod) {
    	mod = parMod;
    	
	}

	@Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (type.equals(EnumSet.of(TickType.WORLDLOAD))) {
        	//System.out.println("RTSDBG: WORLDLOAD CALLED");
        	World world = (World)tickData[0];
        	if (world.provider.dimensionId == 0) {
        		//System.out.println("is remote? " + world.isRemote);
        		
        		DimensionManager.getCurrentSaveRootDirectory();
        		
        		BlockMonsters.initTry();
        	}
        }
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (type.equals(EnumSet.of(TickType.SERVER)))
        {
        	onTickInGame();
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.SERVER, TickType.WORLDLOAD);
    }

    @Override
    public String getLabel() { return null; }
    

    public void onTickInGame()
    {
    	
    	//if (wd == null) wd = new WorldDirector();
    	//if (rts == null) rts = new RtsEngine();
    	
    	if (lastWorld != DimensionManager.getWorld(0)) {
    		lastWorld = DimensionManager.getWorld(0);
    		//((ServerCommandManager)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()).registerCommand(new CommandHW());
    		//wd.resetDimData();
    	}
    	
    	//note this ticking only cares about world 0, so no looping for each world required
    	//second note, ticking it this way doesnt activate physics world until an entity that uses physics activated it
    	mod.physMan.tickServer();
    	
    	if (lastWorld != null) {
	    	//this line is here to activate the PlayerData system, apparently it needs a request first
    		for (int i = 0; i < lastWorld.playerEntities.size(); i++) {
    			Input input = (Input)PlayerData.get(((EntityPlayer)lastWorld.playerEntities.get(i)).username, "input");
    		}
	    	
	    	tickIPlayerData();
    	}
    	//rts.tickUpdate();
    	
    	/*if (test == null) test = new Test();
    	if (lastWorld.getWorldTime() % 20 == 0) test.tick();*/
    }
    
    public void tickIPlayerData() {
        int len = PlayerData.i().tickList.size();
        for(int x=0; x < len; x++) {
        	IPlayerData ipd = (IPlayerData) PlayerData.i().tickList.get(x);
        	ipd.tick();
        }
    }
}
