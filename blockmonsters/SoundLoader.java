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

public class SoundLoader {
	
	@ForgeSubscribe
    public void onSound(SoundLoadEvent event) {
		registerSound(event.manager, BlockMonsters.modID+":stomp.ogg");
		registerSound(event.manager, BlockMonsters.modID+":melee_hit_flesh_heavy.wav");
		registerSound(event.manager, BlockMonsters.modID+":melee_hit_flesh1.wav");
		registerSound(event.manager, BlockMonsters.modID+":melee_hit_flesh2.wav");
		registerSound(event.manager, BlockMonsters.modID+":melee_hit_flesh3.wav");
		registerSound(event.manager, BlockMonsters.modID+":melee_hit_metal.wav");
		
		//eg playing
		//par3World.playSoundEffect(par2EntityPlayer.posX, par2EntityPlayer.posY, par2EntityPlayer.posZ, ParticleMan.modID+":fire_grabb", 0.9F, par3World.rand.nextFloat());
		
		//TEMP!
		//registerSound(event.manager, ZombieCraftMod.modID+":zc.gun.deagle.ogg");
		
		
    }
	
	private void registerSound(SoundManager manager, String path) {
        try {
            manager.addSound(path);
        } catch (Exception ex) {
            System.out.println(String.format("Warning: unable to load sound file %s", path));
        }
    }
    
    private void registerStreaming(SoundManager manager, String path) {
        try {
            manager.soundPoolStreaming.addSound(path);
        } catch (Exception ex) {
            System.out.println(String.format("Warning: unable to load sound file %s"));
        }
    }
}
