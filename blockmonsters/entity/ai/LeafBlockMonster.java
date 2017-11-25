package blockmonsters.entity.ai;

import java.util.List;

import javax.vecmath.Vector3f;

import blockmonsters.EntityStructureProfileMapping;
import blockmonsters.entity.block.IStructureUser;
import blockmonsters.entity.block.StructureBlock;
import blockmonsters.entity.block.profile.StructureProfileBase;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.leaf.LeafAction;
import CoroUtil.entity.EnumJobState;
import CoroUtil.entity.block.MovingBlock;
import CoroUtil.packet.PacketHelper;
import CoroUtil.pathfinding.PFQueue;

import cpw.mods.fml.common.network.PacketDispatcher;

public class LeafBlockMonster extends LeafAction implements IStructureUser {

	public EntityLiving ent = null;
	
	public long huntRange = 12;
	public boolean useMelee = false;
	public long keepDistantRange = 14;
	
	public boolean xRay = false;
	
	public int blocksMaxBody = 27;
	public int blocksMaxArm = 6;
	
	public StructureProfileBase structureProfile;
	
	public LeafBlockMonster(EntityLiving parEnt) {
		super(null);
		
		this.ent = parEnt;
		//setJobState(EnumJobState.IDLE);
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		keepDistantRange = 6;
		ent.fallDistance = 0;
		
		ent.motionX = 0;
		ent.motionY = 0;
		ent.motionZ = 0;
		
		if (!ent.worldObj.isRemote) {
			//make body while ticking, for sync safety
			if (structureProfile == null) {
				newBody();
			}
			
			//setJobState(EnumJobState.IDLE);
			
			/*EntityLivingBase protectEnt = ent;
			if (tamable.isTame()) {
				EntityPlayer entP = ent.worldObj.getPlayerEntityByName(tamable.owner);
				if (entP != null) protectEnt = entP; 
			}
			
			if ((ai.entityToAttack == null || ai.rand.nextInt(20) == 0)) {
				boolean found = false;
				Entity clEnt = null;
				float closest = 9999F;
		    	List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, protectEnt.boundingBox.expand(huntRange, huntRange/2, huntRange));
		        for(int j = 0; j < list.size(); j++)
		        {
		            Entity entity1 = (Entity)list.get(j);
		            if(isEnemy(entity1))
		            {
		            	if (xRay || ((EntityLivingBase) entity1).canEntityBeSeen(protectEnt)) {
		            		if (sanityCheck(entity1) && entity1 instanceof EntityPlayer) {
		            			float dist = protectEnt.getDistanceToEntity(entity1);
		            			if (dist < closest) {
		            				closest = dist;
		            				clEnt = entity1;
		            			}
		            		}
		            	}
		            }
		        }
		        if (clEnt != null) {
		        	if (ai.entityToAttack != clEnt) {
		        		if (ent instanceof EntityEpochBase) ((EntityEpochBase)ent).hookSetTargetPre(clEnt);
		        		ai.huntTarget(clEnt);
		        	} else {
		        		//if (ent.getNavigator().noPath()) {
		        			if (ent instanceof EntityEpochBase) ((EntityEpochBase)ent).hookSetTargetPre(clEnt);
		        			ai.huntTarget(clEnt);
		        		//}
		        	}
		        	
		        }
			} else {
				
				if (ai.entityToAttack != null) {
					if (!useMelee) {
						if (ai.entityToAttack.getDistanceToEntity(ent) < keepDistantRange) {
							ent.getNavigator().clearPathEntity();
						}
					}
					if (ent.getNavigator().noPath() && (ent.getDistanceToEntity(ai.entityToAttack) > keepDistantRange + 1 || useMelee)) {
						PFQueue.getPath(ent, ai.entityToAttack, ai.maxPFRange);
					} else if (!useMelee && !ai.fleeing) {
						if (ai.entityToAttack.getDistanceToEntity(ent) < keepDistantRange) {
							ent.getNavigator().clearPathEntity();
						}
					}
				}
				
			}
			ent.prevHealth = ent.getHealth();
			
			
			setJobState(EnumJobState.IDLE);
			
			if (this.state == EnumJobState.IDLE) {
				
				
				
			} else if (this.state == EnumJobState.W1) {
				
			} else if (this.state == EnumJobState.W2) {
				
			} else if (this.state == EnumJobState.W3) {
				
			}*/
			
			try {
				structureProfile.tickUpdate();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			//System.out.println(body.getMissingBlockCountRecursive());
			
			//the heal rate/cooldown doesnt account for that repair() isnt guaranteed successfull on every run...
			
			int healRate = 1;
			int healCooldown = 3;
			
			healCooldown = 1;
			healRate = 3;
			
			if (structureProfile.body.getMissingBlockCountRecursive() > 0) {
				if (ent.worldObj.getWorldTime() % healCooldown == 0) {
					//System.out.println(structureProfile.body.getMissingBlockCountRecursive());
					for (int i = 0; i < healRate; i++) {
						repair();
					}
				}
			}
			
			int syncRate = 4;
			
			if (ent.worldObj.getWorldTime() % syncRate == 0) {
				NBTTagCompound data = new NBTTagCompound();
				data.setInteger("entityID", ent.entityId);
				data.setInteger("updateType", 1);
				NBTTagCompound bodyData = new NBTTagCompound();
				structureProfile.body.writeToNBTRecursive(bodyData);
				data.setCompoundTag("structureBody", bodyData);
				PacketDispatcher.sendPacketToAllAround(ent.posX, ent.posY, ent.posZ, 64D, ent.worldObj.provider.dimensionId, PacketHelper.createPacketForNBTHandler("CoroAI_Ent", data));
			}
		}
		
		return super.tick();
	}
	
	public void newBody() {
		
		String name = "monster1";
		name = "walker";
		
		structureProfile = EntityStructureProfileMapping.newStructureProfile(this, name);
		structureProfile.populateStructureData();
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("entityID", ent.entityId);
		data.setInteger("updateType", 0);
		data.setString("name", name);
		NBTTagCompound bodyData = new NBTTagCompound();
		structureProfile.body.writeToNBTRecursive(bodyData);
		data.setCompoundTag("structureBody", bodyData);
		//PacketDispatcher.sendPacketToAllAround(ent.posX, ent.posY, ent.posZ, 64D, ent.worldObj.provider.dimensionId, PacketHelper.createPacketForNBTHandler("CoroAI_Ent", data));
		PacketDispatcher.sendPacketToAllInDimension(PacketHelper.createPacketForNBTHandler("CoroAI_Ent", data), ent.worldObj.provider.dimensionId);
	}

	public void repair() {
	
		int range = 80;
		
		int tryX = (int)ent.posX+ent.worldObj.rand.nextInt(range)-range/2;
	    int tryY = (int)ent.posY+ent.worldObj.rand.nextInt(range)-range/2;
	    int tryZ = (int)ent.posZ+ent.worldObj.rand.nextInt(range)-range/2;
	    
	    int id = ent.worldObj.getBlockId(tryX, tryY, tryZ);
	    int meta = ent.worldObj.getBlockMetadata(tryX, tryY, tryZ);
	    
	    if (isNaturalSurfaceBlock(id)) {
	    	if (ent.worldObj.getBlockTileEntity(tryX, tryY, tryZ) == null) {
	    		StructureBlock fb = ripBlock(/*structureProfile.body, */id, meta, tryX, tryY, tryZ);
	    		
	    		if (!structureProfile.body.addBlockRecursive(fb)) {
	    			//System.out.println("should never happen");
	    		} else {
	    			ent.worldObj.spawnEntityInWorld(fb); //moved here so data can get set before its spawned (so fml data on spawn works right)
	    			//System.out.println("wooo!");
	    		}
	    	}
	    }
	
	
	}
	
	public StructureBlock ripBlock(int id, int meta, int x, int y, int z) {
		
		//WARNING, parPiece IS BODY, NOT PARENT
		
		//ent.worldObj.setBlock(x, y, z, 0);
		
		StructureBlock fb = new StructureBlock(ent.worldObj);
		
		fb.setPosition(x+0.5D, y+1, z+0.5D);
		fb.blockID = id;
		fb.blockMeta = meta;
		fb.ownerEntityID = ent.entityId;
		
		//temp?
		//fb.blockID = Block.stone.blockID;
		
		return fb;
	}
	
	public boolean isNaturalSurfaceBlock(int id) {
		if (id == Block.snow.blockID || id == Block.grass.blockID || id == Block.dirt.blockID || id == Block.sand.blockID || id == Block.stone.blockID || id == Block.gravel.blockID || id == Block.tallGrass.blockID) {
			return true;
		}
		if (isLogOrLeafBlock(id)) return true;
		return false;
	}
	
	public boolean isLogOrLeafBlock(int id) {
		Block block = Block.blocksList[id];
		if (block == null) return false;
		if (block.blockMaterial == Material.leaves) return true;
		if (block.blockMaterial == Material.plants) return true;
		if (block.blockMaterial == Material.wood) return true;
		return false;
	}
	
	public void testControl1(MovingBlock parEnt, Vec3 center, int index) {
    	
    	//to handle a grid of blocks, reduce it to blocks that need the same rotations and do 1 calculation for the whole grid to use
    	
    	int bodyWidth = 3;
    	int bodyHeight = 3;
    	int bodyLength = 10; //considered relative Y

    	int bodyWidthPos = index % bodyWidth;
    	int bodyHeightPos = ((int)(index / bodyWidth) % (bodyHeight));
    	int bodyLengthPos = (int)(index / (bodyWidth * bodyHeight));
    	
    	//temp
    	//bodyWidthPos = index-(blocks.size()/2);
    	//bodyHeightPos = 0;
    	//bodyLengthPos = 0;
    	
    	float rotateTimeX = (ent.worldObj.getWorldTime() % 360);
    	
    	float radius = 1F;
    	float rate = 2F;
    	
    	float yaw = 0;//rotateTimeX * 1F;//rate * (float)Math.sin(rotateTimeX * 0.05);
    	float pitch = 0;//rate * (float)Math.sin(rotateTimeX * 0.05F);
    	float roll = 0;//rate * (float)Math.cos(rotateTimeX * 0.05F);
    	
    	double blockSpacing = 1D;// + 1D + Math.cos(rotateTimeX * 0.05F);
    	double newX = 0.5D+(bodyWidthPos*blockSpacing) - ((float)bodyWidth*blockSpacing/2F);
    	double newY = Math.cos(rotateTimeX * 0.05F)+(bodyLengthPos*blockSpacing) - ((float)bodyLength*blockSpacing/2F);
    	double newZ = 0.5D+(bodyHeightPos*blockSpacing) - ((float)bodyHeight*blockSpacing/2F);
    	
    	Vec3 distVec = Vec3.createVectorHelper(center.xCoord-newX, center.yCoord-newY, center.zCoord-newZ);
    	
    	Vec3 relPos = Vec3.createVectorHelper(newX, 0, newZ);
    	relPos.rotateAroundY(yaw * 0.01745329F);
    	
    	newX = relPos.xCoord/* / relPos.lengthVector()*/; //for some reason only normalizing here APPEARS to produce acceptable results, i suspect innacuracies though.... why only this axis?
    	newZ = relPos.zCoord/* / relPos.lengthVector()*/;
    	//newY /= relPos.lengthVector();
    	
    	relPos = Vec3.createVectorHelper(newX, newY, 0);
    	relPos.rotateAroundZ(pitch * 0.01745329F);
    	
    	newX = relPos.xCoord;
    	newY = relPos.yCoord;
    	
    	relPos = Vec3.createVectorHelper(0, newY, newZ);
    	relPos.rotateAroundX(roll * 0.01745329F);
    	
    	newY = relPos.yCoord;
    	newZ = relPos.zCoord;
    	
    	double finalXAbs = newX/* * radius*/;
    	double finalYAbs = newY/* * radius*/;
    	double finalZAbs = newZ/* * radius*/;
    	
    	parEnt.motionX = 0.0;
    	parEnt.motionY = 0.0;
    	parEnt.motionZ = 0.0;
    	
    	//parEnt.setPosition(center.xCoord + finalXAbs, center.yCoord + finalYAbs, center.zCoord + finalZAbs);
    	parEnt.setPositionAndRotation(center.xCoord + finalXAbs, center.yCoord + finalYAbs, center.zCoord + finalZAbs, yaw, pitch, roll);
    }
	
	@Override
	public boolean passBackAttackFrom(IStructureUser parPasser, DamageSource par1DamageSource, float par2, Vector3f coordsSource) {
		if (ent != null) {
			ent.attackEntityFrom(par1DamageSource, 1F);
			//if (par1DamageSource.getEntity() != null) ai.huntTarget(par1DamageSource.getEntity());
		}
		//TEMP, kill body and rebuild on hit for temp debug
		//body.ownerDied();
		//body = StructureTemplates.buildBlockMonster1(this);
		
		if (structureProfile != null) {
			return structureProfile.passBackAttackFrom(parPasser, par1DamageSource, par2, coordsSource);
		}
		
		return true;
	}

	@Override
	public boolean passInteract(IStructureUser parPasser,
			EntityPlayer par1EntityPlayer) {
		
		ent.interactFirst(par1EntityPlayer);
		
		return false;
	}

	@Override
	public Entity getOwnerEntity() {
		return ent;
	}

}
