package blockmonsters.playerdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import blockmonsters.playerdata.objects.Input;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import CoroUtil.playerdata.IPlayerData;
import CoroUtil.playerdata.PlayerDataObject;
import CoroUtil.util.CoroUtilFile;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class PlayerData {

	//this is the main class to handle binding a PlayerData object instance to a username
	//possible creation/implementation of interfaces ISavable & ISyncable
	//IPlayerData: implement on class you want to add, has nbt read and write overrides
	//ISyncable: handles sending the needed data to client, and maybe helps setup dummy client object for info
	
	//PlayerData -> list of PlayerDataObjects -> list of IPlayerData implemented classes
		
	//the method of lookup might require some caching to prevent hashmap lookup overkill
	
	public static PlayerData instanceClient;
	public static PlayerData instanceServer;
	
	public HashMap<String, PlayerDataObject> playerData = new HashMap<String, PlayerDataObject>();
	public ArrayList tickList = new ArrayList();
	
	public PlayerData() {
		
	}
	
	public void initObjects(PlayerDataObject pdo) {
		pdo.addObject("input", new Input());
	}
	
	/* Main usage methods start */
	public static PlayerData i() {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			if (instanceClient == null) instanceClient = new PlayerData();
			return instanceClient;
		} else {
			if (instanceServer == null) instanceServer = new PlayerData();
			return instanceServer;
		}
	}
	
	public static IPlayerData get(String username, String objectName) {
		return get(username).get(objectName);
	}
	
	public static PlayerDataObject get(String username) {
		if (!i().playerData.containsKey(username)) {
			NBTTagCompound nbt = new NBTTagCompound();
			//prevents client from loading data it shouldnt technically have access to on a dedicated server
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
				nbt = i().tryLoadPlayerData(username);
			}
			PlayerDataObject pd = new PlayerDataObject(username);
			i().playerData.put(username, pd); //this was moved above load so we could lookup the PlayerInfo while loading skills
			i().initObjects(pd);
			pd.nbtLoadAll(nbt);
		}
		return i().playerData.get(username);
	}
	/* Main usage methods end */
	
	public NBTTagCompound tryLoadPlayerData(String username) {
		//init with data, if fail, init default blank
		
		NBTTagCompound playerNBT = new NBTTagCompound();
		
		boolean firstTimeInit = false;
		
		try {
			String fileURL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + File.separator + "BMPlayerData" + File.separator + username + ".dat";
			
			if ((new File(fileURL)).exists()) {
				playerNBT = CompressedStreamTools.readCompressed(new FileInputStream(fileURL));
			} else {
				dbg("no saved data found for " + username);
				firstTimeInit = true;
			}
		} catch (Exception ex) {
			dbg("error trying to load data for " + username);
			firstTimeInit = true;
		}
		
		if (firstTimeInit) playerNBT.setBoolean("newPlayer", true); //it will be up to whatever ticks this stuff to unset this
		
		return playerNBT;
	}
	
	//write out list of PlayerDataObjects
	public void writeToNBTAll() {

    	//RPGMod.dbg("Saving RPGMod player data");
		
		Iterator it = playerData.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			
			String entryUser = (String)pairs.getKey();
			PlayerDataObject pdo = (PlayerDataObject)pairs.getValue();
			
			//NBTTagCompound data = new NBTTagCompound();
			//data.setCompoundTag(entryUser, writeToNBTPlayer(entryUser, pdo));
			
			try {
				String folderURL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + File.separator + "BMPlayerData";
				if (!(new File(folderURL)).exists()) (new File(folderURL)).mkdirs();
				String fileURL = folderURL + File.separator + entryUser + ".dat";
				FileOutputStream fos = new FileOutputStream(fileURL);
		    	CompressedStreamTools.writeCompressed(writeToNBTPlayer(entryUser, pdo), fos);
		    	fos.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
	public void unload() {
		//just wipe the main hashmap, memory leaks legligable
		playerData.clear();
	}
	
	//write out each module in PlayerDataObject 
	public NBTTagCompound writeToNBTPlayer(String parUsername, PlayerDataObject parPDO) {
		NBTTagCompound data = new NBTTagCompound();
		
		Iterator it = parPDO.playerData.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			
			String entryModule = (String)pairs.getKey();
			IPlayerData pdo = (IPlayerData)pairs.getValue();
			
			data.setCompoundTag(entryModule, pdo.nbtSave());
		}
		
		return data;
	}
	
	public void dbg(String string) {
		System.out.println(string);
	}
}
