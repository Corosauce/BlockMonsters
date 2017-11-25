package blockmonsters.playerdata;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import CoroUtil.packet.PacketHelper;
import CoroUtil.playerdata.IPlayerData;
import CoroUtil.playerdata.PlayerDataObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;

public class SyncManager {

	public static void handleServerSentSync(String parUser, NBTTagCompound parNBT) {
		NBTTagCompound nbtData = parNBT.getCompoundTag("data");
    	PlayerDataObject pdo = PlayerData.get(parUser);
    	String command = parNBT.getString("command");
    	if (command.equals("sync")) {
    		String module = parNBT.getString("module");
    		IPlayerData ipd = pdo.get(module);
    		if (ipd != null) {
    			ipd.nbtSyncFromServer(nbtData);
    		} else {
    			System.out.println("PDO SYNC ERROR: module " + module + " not found");
    		}
    	} else if (command.equals("syncAll")) {
    		pdo.nbtSyncAll(nbtData);
    	}
    	
	}
	
	public static void handleClientSentCommand(String parUser, NBTTagCompound parNBT) {
		NBTTagCompound nbtData = parNBT.getCompoundTag("data");
		PlayerDataObject pdo = PlayerData.get(parUser);
		
		String module = parNBT.getString("module");
		IPlayerData ipd = pdo.get(module);
		if (ipd != null) {
			ipd.nbtCommandFromClient(nbtData);
		} else {
			System.out.println("PDO COMMAND ERROR: module " + module + " not found");
		}
	}
	
    public static Packet250CustomPayload createPacketForServerToClientSerialization(String parUser) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try {
			dos.writeUTF(parUser);
			PlayerDataObject pdo = PlayerData.get(parUser);
			PacketHelper.writeNBTTagCompound(PlayerData.i().writeToNBTPlayer(parUser, pdo), dos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = "PlayerData";
		pkt.data = bos.toByteArray();
		pkt.length = bos.size();
		pkt.isChunkDataPacket = false;
		return pkt;
	}
    
    /*public static Packet250CustomPayload createPacketForPDOSyncToClient(String parUser, String parModule) {
    	return createPacketForPDOSyncToClient(parUser, PlayerData.get(parUser, parModule));
    }*/
    
    public static Packet250CustomPayload createPacketForPDOSyncToClient(String parUser, String parModule, NBTTagCompound nbtSendData) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try {
			dos.writeUTF(parUser);
			//PlayerDataObject pdo = PlayerData.get(parUser);
			NBTTagCompound nbtSendBuffer = new NBTTagCompound();
			nbtSendBuffer.setString("command", "sync");
			nbtSendBuffer.setString("module", parModule);
			nbtSendBuffer.setCompoundTag("data", nbtSendData);
			PacketHelper.writeNBTTagCompound(nbtSendBuffer, dos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = "PlayerData";
		pkt.data = bos.toByteArray();
		pkt.length = bos.size();
		pkt.isChunkDataPacket = false;
		return pkt;
	}
    
    
    
    /*public static Packet250CustomPayload createPacketForPDOCommandToServer(String parUser, String parModule) {
    	return createPacketForPDOCommandToServer(parUser, PlayerData.get(parUser, parModule));
    }*/
    
    public static Packet250CustomPayload createPacketForPDOCommandToServer(String parUser, String parModule, NBTTagCompound nbtSendData) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try {
			dos.writeUTF(parUser);
			PlayerDataObject pdo = PlayerData.get(parUser);
			NBTTagCompound nbtSendBuffer = new NBTTagCompound();
			nbtSendBuffer.setString("module", parModule);
			nbtSendBuffer.setCompoundTag("data", nbtSendData);
			PacketHelper.writeNBTTagCompound(nbtSendBuffer, dos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = "PlayerData";
		pkt.data = bos.toByteArray();
		pkt.length = bos.size();
		pkt.isChunkDataPacket = false;
		return pkt;
	}
	
}
