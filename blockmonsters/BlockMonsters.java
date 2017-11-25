package blockmonsters;

import java.util.List;

import modconfig.ConfigMod;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import CoroUtil.IChunkLoader;
import CoroUtil.util.CoroUtilFile;
import blockmonsters.config.ConfigMisc;
import blockmonsters.physics.PhysicsWorldManager;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@NetworkMod(channels = { "InputCommandBM" }, clientSideRequired = true, serverSideRequired = true, packetHandler = BMPacketHandler.class)
@Mod(modid = "BlockMonsters", name="Block Monsters", version="1.0.1", useMetadata=false)
public class BlockMonsters {
	
	@Mod.Instance( value = "BlockMonsters" )
	public static BlockMonsters instance;
	public static String modID = "blockmonsters";
    
    @SidedProxy(clientSide = "blockmonsters.ClientProxy", serverSide = "blockmonsters.CommonProxy")
    public static CommonProxy proxy;
    
    public static boolean initProperNeededForWorld = true;
    
    public static boolean debugConsole = true;

    //this is here because client will want it too, seems cleaner than putting in servertickhandler
	public static PhysicsWorldManager physMan = new PhysicsWorldManager();
    
    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
    	ConfigMod.addConfigFile(event, "blockMonsters", new ConfigMisc(), false);
    	//config.init(event);
    	
    	//generateConfigFile();
    	//updateSaveFile();
    	//test();
    	proxy.preInit(event);
    }
    
    @Init
    public void load(FMLInitializationEvent event)
    {
    	
    	MinecraftForge.EVENT_BUS.register(new BMEventHandler());
    	NetworkRegistry.instance().registerConnectionHandler(new ConnectionHandler());
    	/*dimIDCatacombs = DimensionManager.getNextFreeDimId();
    	
    	DimensionManager.registerProviderType(dimIDCatacombs, HWWorldProvider.class, false);
		DimensionManager.registerDimension(dimIDCatacombs, dimIDCatacombs);*/
    	
    	
    	
    	proxy.init(this);
    }
    
    @PostInit
	public void postInit(FMLPostInitializationEvent event) {
		ForgeChunkManager.setForcedChunkLoadingCallback(instance, new PortalChunkloadCallback());
	}

	public class PortalChunkloadCallback implements ForgeChunkManager.OrderedLoadingCallback {
		@Override
		public void ticketsLoaded(List<Ticket> tickets, World world) {
			for (Ticket ticket : tickets) {
				Entity ent = ticket.getEntity();
				if (ent instanceof IChunkLoader) {
					dbg("world load readd miner chunkloader");
					((IChunkLoader) ent).setChunkTicket(ticket);
					((IChunkLoader) ent).forceChunkLoading(ent.chunkCoordX, ent.chunkCoordZ);
				}

			}
		}

		@Override
		public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount) {
			List<Ticket> validTickets = Lists.newArrayList();
			for (Ticket ticket : tickets) {
				Entity ent = ticket.getEntity();
				if (ent instanceof IChunkLoader) {
					validTickets.add(ticket);
				}
			}
			return validTickets;
		}

	}
    
    public BlockMonsters() {
    	int hm = 0;
    	TickRegistry.registerTickHandler(new ServerTickHandler(this), Side.SERVER);
    }
    
    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
    	//so it inits before entity nbt does
    	
    }
    
    @EventHandler
    public void serverStart(FMLServerStartedEvent event) {

    	//proper command adding
    	//((ServerCommandManager) MinecraftServer.getServer().getCommandManager()).registerCommand(new commandAddOwner());
    }
    
    @EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
    	//HWDimensionManager.unregisterDimensionsAndSave();
    	//ServerTickHandler.rts.writeToFile(true);
    	initProperNeededForWorld = true;
    }
    
    @EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
    	event.registerServerCommand(new CommandBlockMonsters());
    }
    
    public static void initTry() {
    	if (initProperNeededForWorld) {
    		initProperNeededForWorld = false;
	    	//if (ServerTickHandler.rts == null) ServerTickHandler.rts = new RtsEngine();
	    	//ServerTickHandler.rts.readFromFile();
	    	CoroUtilFile.getWorldFolderName(); //make it cache the lastWorldFolder, lucky that it was cached before, as serverStop method cant update the cache, issue arrised due to new use of FMLServerAboutToStartEvent
	    	//HWDimensionManager.loadAndRegisterDimensions();
    	}
    }
    
    /*public static String lastWorldFolder = "";
    
    public static String getWorldFolderName() {
		World world = DimensionManager.getWorld(0);
		
		if (world != null) {
			lastWorldFolder = ((WorldServer)world).getChunkSaveLocation().getName();
			return lastWorldFolder + File.separator;
		}
		
		return lastWorldFolder + File.separator;
	}
	
	public static String getSaveFolderPath() {
    	if (MinecraftServer.getServer() == null || MinecraftServer.getServer().isSinglePlayer()) {
    		return getClientSidePath() + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator;
    	}
    	
    }
	
	public static String getWorldSaveFolderPath() {
    	if (MinecraftServer.getServer() == null || MinecraftServer.getServer().isSinglePlayer()) {
    		return getClientSidePath() + File.separator + "saves" + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator;
    	}
    	
    }
    
    @SideOnly(Side.CLIENT)
	public static String getClientSidePath() {
		return FMLClientHandler.instance().getClient().mcDataDir.getPath();
	}*/
	
	public static void writeGameNBT() {
		
		if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) return;
		
		//dbg("Saving Hostile Worlds data");
		
		NBTTagCompound gameData = new NBTTagCompound();
		
    	try {
    		
    		/*HWDimensionManager.writeNBT(gameData);
    		
    		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName();
    		
    		//Write out to file
    		FileOutputStream fos = new FileOutputStream(saveFolder + "HostileWorlds.dat");
	    	CompressedStreamTools.writeCompressed(gameData, fos);
	    	fos.close();
	    	
	    	WorldDirector.writeAllPlayerNBT();*/
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
	
	public static void readGameNBT() {
		
		//dbg("Reading Hostile Worlds data");
		
		NBTTagCompound gameData = null;
		
		/*HWDimensionManager.registeredDimensions.clear();
		
		try {
			
			String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName();
			
			if ((new File(saveFolder + "HostileWorlds.dat")).exists()) {
				gameData = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "HostileWorlds.dat"));
				
				//NBTTagList var14 = gameData.getTagList("playerData");
				HWDimensionManager.readNBT(gameData);
			}
			
			//dont read in player nbt, its read in on demand
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}*/
    }
	
	public static void writeChunkCoords(String prefix, ChunkCoordinates coords, NBTTagCompound nbt) {
		nbt.setInteger(prefix + "X", coords.posX);
		nbt.setInteger(prefix + "Y", coords.posY);
		nbt.setInteger(prefix + "Z", coords.posZ);
	}

	public static ChunkCoordinates readChunkCoords(String prefix, NBTTagCompound nbt) {
		return new ChunkCoordinates(nbt.getInteger(prefix + "X"), nbt.getInteger(prefix + "Y"), nbt.getInteger(prefix + "Z"));
	}
	
	public static void dbg(Object obj) {
		if (debugConsole) {
			//MinecraftServer.getServer().getLogAgent().logInfo(String.valueOf(obj));
			System.out.println(obj);
		}
	}
}
