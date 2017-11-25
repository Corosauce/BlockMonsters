package blockmonsters;

import java.util.ArrayList;
import java.util.List;

import combat.RPGMod;

import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOre;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import CoroUtil.IChunkLoader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BMEventHandler {
	
	public static List<ChunkCoordinates> listConnectablePointsVisualDebug = new ArrayList<ChunkCoordinates>();
	
	@ForgeSubscribe
	public void breakSpeed(BreakSpeed event) {
		//ZAUtil.blockEvent(event, 20);
	}
	
	@ForgeSubscribe
	public void harvest(HarvestCheck event) {
		//ZAUtil.blockEvent(event, 1);
	}
	
	@ForgeSubscribe
	public void entityEnteredChunk(EntityEvent.EnteringChunk event) {
		Entity entity = event.entity;
	    if ((entity instanceof IChunkLoader)) {
	    	if (!entity.worldObj.isRemote) {
	    		//System.out.println("update miner loaded chunks");
	    		((IChunkLoader)entity).forceChunkLoading(event.newChunkX, event.newChunkZ);
	    	}
	    }
	    
	}
	
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event) {
		/*if (Minecraft.getMinecraft().theWorld.getTotalWorldTime() % (20*3) == 0) {
			listConnectablePointsVisualDebug.clear();
		}*/
		for (int i = 0; i < listConnectablePointsVisualDebug.size(); i++) {
			ChunkCoordinates cc = listConnectablePointsVisualDebug.get(i);
			if (cc != null) {
				try {
					//Overlays.renderLineFromToBlockCenter(cc.posX, cc.posY, cc.posZ, cc.posX, cc.posY + 3, cc.posZ, 0xFFFFFF/*i * 1000*/);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	@ForgeSubscribe
	public void breakBlockHarvest(HarvestDropsEvent event) {
		if (event.harvester != null) {
			if (event.world.playerEntities.contains(event.harvester)) {
				
			}
		}
	}
}
