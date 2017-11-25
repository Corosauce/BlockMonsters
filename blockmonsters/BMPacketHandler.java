package blockmonsters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import blockmonsters.input.ControlInputs;
import blockmonsters.playerdata.PlayerData;
import blockmonsters.playerdata.objects.Input;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BMPacketHandler implements IPacketHandler
{
    public BMPacketHandler()
    {
    }

    @SideOnly(Side.CLIENT)
	public World getClientWorld() {
		return Minecraft.getMinecraft().theWorld;
	}
    
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.data));
        try {
        	
        	if (packet.channel.equals("InputCommandBM")) {
	        	String username = dis.readUTF();
	        	int commandID = dis.readInt();
	        	boolean keyDown = dis.readBoolean();
	        	//System.out.println("keyDown: " + keyDown);
	        	((Input)PlayerData.get(username, "input")).commandDownStateServer.put(ControlInputs.keyBindingNames.get(commandID), keyDown);
        	}
        	
        } catch (Exception ex) {
        	BlockMonsters.dbg("ERROR HANDLING BLOCK MONSTER PACKETS");
            ex.printStackTrace();
        }
    }
    
    public static Packet250CustomPayload createPacketForInputCommand(String parUser, int commandID, boolean keyState) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try {
			dos.writeUTF(parUser);
			dos.writeInt(commandID);
			dos.writeBoolean(keyState);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = "InputCommandBM";
		pkt.data = bos.toByteArray();
		pkt.length = bos.size();
		pkt.isChunkDataPacket = false;
		return pkt;
	}
}
