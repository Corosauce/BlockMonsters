package blockmonsters;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import CoroUtil.bt.IBTAgent;
import CoroUtil.componentAI.ICoroAI;

public class CommandBlockMonsters extends CommandBase {

	@Override
	public String getCommandName() {
		return "bm";
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2) {
		
		try {
			if(var1 instanceof EntityPlayerMP)
			{
				EntityPlayer player = getCommandSenderAsPlayer(var1);
				
				if (var2[0].equalsIgnoreCase("spawn")) {
					
					String prefix = "BlockMonsters.";
					String mobToSpawn = "BlockMonster1"; //only 1 thing!
					
					if (var2.length > 1) {
						mobToSpawn = var2[1];
					}
					
					int count = 1;
					
					if (var2.length > 2) {
						count = Integer.valueOf(var2[2]);
					}

					for (int i = 0; i < count; i++) {
						Entity ent = EntityList.createEntityByName(prefix + mobToSpawn, player.worldObj);
						
						if (ent == null) ent = EntityList.createEntityByName(mobToSpawn, player.worldObj);
						
						if (ent != null) {
							
							double dist = 1D;
							
							double finalX = player.posX - (Math.sin(player.rotationYaw * 0.01745329F) * dist);
							double finalZ = player.posZ + (Math.cos(player.rotationYaw * 0.01745329F) * dist);
							
							double finalY = player.posY;
							
							if (mobToSpawn.contains("BlockMonster")) {
								finalY += 5;
							}
							
							ent.setPosition(finalX, finalY, finalZ);
							
							
							
							//temp
							//ent.setPosition(69, player.worldObj.getHeightValue(69, 301), 301);
							//((JobGroupHorde)((ICoroAI) ent).getAIAgent().jobMan.priJob).attackCoord = new ChunkCoordinates(44, player.worldObj.getHeightValue(44, 301), 301);
							
							player.worldObj.spawnEntityInWorld(ent);
							if (ent instanceof EntityLiving) ((EntityLiving)ent).onSpawnWithEgg(null); //moved to after spawn, so client has an entity at least before syncs fire
							if (ent instanceof ICoroAI) ((ICoroAI) ent).getAIAgent().spawnedOrNBTReloadedInit();
							if (ent instanceof IBTAgent) ((IBTAgent)ent).getAIBTAgent().onSpawnEvent(null);
							System.out.println("Spawned: " + mobToSpawn);
						} else {
							System.out.println("failed to spawn");
							break;
						}
					}
				}
			}
		} catch (Exception ex) {
			System.out.println("Exception handling Epoch command");
			ex.printStackTrace();
		}
		
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
    {
        return par1ICommandSender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
    }

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "";
	}

}
