package blockmonsters.playerdata.objects;

import java.util.HashMap;

import org.lwjgl.input.Keyboard;

import blockmonsters.ClientTickHandler;
import blockmonsters.input.ControlInputs;
import blockmonsters.playerdata.PlayerData;

import CoroUtil.playerdata.IPlayerData;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Input implements IPlayerData {
	
	public String username = "";
	
	public HashMap<String, Boolean> commandDownStateServer = new HashMap<String, Boolean>();
	
	public boolean wasDownPlayerPhysics = false; 
	
	public Input() {
		commandDownStateServer = ControlInputs.getNewCommandStateHashMap();
		PlayerData.i().tickList.add(this);
	}
	
	//main server input checking method
	public boolean getKeyDown(String commandName) {
		Boolean boolVal = commandDownStateServer.get(commandName);
		if (boolVal == null) {
			return false;
		} else {
			return boolVal;
		}
	}
	
	@Override
	public void nbtLoad(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NBTTagCompound nbtSave() {
		return new NBTTagCompound();
	}

	@Override
	public void tick() {
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (side == Side.SERVER) {
			EntityPlayer entP = MinecraftServer.getServerConfigurationManager(MinecraftServer.getServer()).getPlayerForUsername(username);
			if (entP != null) {
				//PlayerInfo pi = (PlayerInfo) PlayerData.get(username, "playerinfo");
				if (getKeyDown(ControlInputs.moveForward)) {
					//System.out.println("server side detecting move forward! success!");
				} else {
					//System.out.println("wat");
				}
			} else {
				//zc cam derp
			}
			//System.out.println(side + " - " + "user: " + username + " - " + entP);
			//System.out.println("------");
		} else {
			tickClient();
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void tickClient() {
		
		if (!username.equals(Minecraft.getMinecraft().thePlayer.username)) return;
		
		if (ControlInputs.getKeyDown(ControlInputs.playerPhysics)) {
			if (!wasDownPlayerPhysics) {
				ClientTickHandler.playerPhysics = !ClientTickHandler.playerPhysics;
			}
			wasDownPlayerPhysics = true;
		} else {
			wasDownPlayerPhysics = false;
		}
	}

	@Override
	public void init(String parUsername) {
		username = parUsername;
	}

	@Override
	public void nbtSyncFromServer(NBTTagCompound nbt) {
		
	}

	@Override
	public void nbtCommandFromClient(NBTTagCompound nbt) {
		
	}

}
