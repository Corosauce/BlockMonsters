package blockmonsters.entity;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import CoroUtil.entity.IEntityPacket;
import blockmonsters.BlockMonsters;
import blockmonsters.EntityStructureProfileMapping;
import blockmonsters.entity.ai.LeafBlockMonster;
import blockmonsters.entity.block.BlockController;
import blockmonsters.entity.block.StructureBlock;
import blockmonsters.physics.PhysicsWorld;

public class BlockMonster1 extends BlockController implements IEntityPacket {

	
	
	public LeafBlockMonster priJob;
	
	public boolean eatTrees = false;
	public float walkRate = 0.2F; //range: 0.1 - 0.5
	public int legHeightStepRange = 45; //range: 25 - 65;
	public int legHeightBase = -90; //range: -60 - -120?
	
	public BlockMonster1(World var1) {
		super(var1);
		
		agent.btAI.add(priJob = new LeafBlockMonster(this));
		
		//FIX
		//agent.jobMan.addPrimaryJob(priJob = new JobAttackRoutineBlockMonster1(agent.jobMan));
		//agent.useCustomMovement = true;
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound data) {
		// TODO Auto-generated method stub
		super.readEntityFromNBT(data);
		
		//might cause duplicates
		if (!worldObj.isRemote) {
			PhysicsWorld physWorld = BlockMonsters.physMan.getPhysicsWorld(worldObj);
			physWorld.chunkManager.addChunkloader(this);
		}
		
		//temp until proper cleanup is done
		//setDead();
	}
	
	@Override
    public void onUpdate()
    {
		super.onUpdate();
		if (!worldObj.isRemote) {
			if (posY <= 0) {
				this.setDead();
			}
			//agent.useCustomMovement = true;
			
			
			if (eatTrees) {
				//remove trees in a silly way
				int size = 10;
				Random rand = new Random();
				
				for (int i = 0; i < 30; i++) {
					int tryX = MathHelper.floor_double(posX + rand.nextInt(size) - rand.nextInt(size));
			    	int tryZ = MathHelper.floor_double(posZ + rand.nextInt(size) - rand.nextInt(size));
			    	
	
			    	int tryY = worldObj.getHeightValue(tryX, tryZ)-1;//MathHelper.floor_double(posY + rand.nextInt(size) - rand.nextInt(size));
			    	
		    		int idTry = worldObj.getBlockId(tryX, tryY, tryZ);
		    		
		    		if (idTry != 0) {
		    			Material mat = Block.blocksList[idTry].blockMaterial;
		    			if (mat == Material.leaves || mat == Material.wood) {
		    				//System.out.println("break block");
		    				worldObj.setBlock(tryX, tryY, tryZ, 0);
		    				//System.out.println("world: " + worldObj);
		    				BlockMonsters.physMan.getPhysicsWorld(worldObj).chunkManager.markChunksNeedsUpdate(new ChunkCoordinates(tryX, tryY, tryZ), 0);
		    			}
		    		}
				}
			}
			this.motionY = 0;
			
			if (this.riddenByEntity != null) {
				//System.out.println("this.riddenByEntity: " + this.riddenByEntity.rotationYaw);
			}
		} else {
			this.motionY = 0;
			//System.out.println("posY: " + posY);
		}
    }
	
	@Override
	protected boolean canDespawn() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void setDead() {
		System.out.println("block monster died");
		super.setDead();
		if (priJob.structureProfile != null) {
			priJob.structureProfile.body.ownerDied();
		}
	}
	
	@Override
	public void initRPGStats() {
		//super.initRPGStats();
		
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(80/*getHPBonus()*/);
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
		System.out.println(par1DamageSource.damageType);
		if (par1DamageSource.getEntity() instanceof StructureBlock) return true;
		if (par1DamageSource.damageType.equalsIgnoreCase("inwall")) return true;
		if (par1DamageSource.damageType.equalsIgnoreCase("lava")) {
			System.out.println(this.posY);
		}
		return super.attackEntityFrom(par1DamageSource, par2);
	}
	
	@Override
	public boolean interact(EntityPlayer par1EntityPlayer)
    {
		System.out.println("sdfsdfsdf");
		
        if (super.interact(par1EntityPlayer))
        {
            return true;
        }
        else if (!this.worldObj.isRemote && (this.riddenByEntity == null || this.riddenByEntity == par1EntityPlayer))
        {
            par1EntityPlayer.mountEntity(this);
            return true;
        }
        else
        {
            return false;
        }
    }
	
	@Override
	public boolean isEntityInsideOpaqueBlock() {
		return false;
	}

	@Override
	public void handleNBTFromClient(NBTTagCompound par1nbtTagCompound) {
		
	}

	@Override
	public void handleNBTFromServer(NBTTagCompound par1nbtTagCompound) {
		if (par1nbtTagCompound.hasKey("updateType")) {
			int updateType = par1nbtTagCompound.getInteger("updateType");
			//0 = create, 1 = update
			NBTTagCompound bodyData = par1nbtTagCompound.getCompoundTag("structureBody");
			if (updateType == 0) {
				if (priJob.structureProfile != null) {
					priJob.structureProfile.body.ownerDied();
				}
				priJob.structureProfile = EntityStructureProfileMapping.newStructureProfile(priJob, par1nbtTagCompound.getString("name"));
				priJob.structureProfile.populateStructureDataFromNBT(bodyData);
			} else if (updateType == 1) {
				if (priJob.structureProfile != null) {
					priJob.structureProfile.body.readFromNBTRecursive(bodyData);
				}
			}
		} else {
		}
		
		super.handleNBTFromServer(par1nbtTagCompound);
	}
	
	@Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        //these need a design rethink, these numbers are based off of 0.45D speed declared in the speedBoostFlee field in AIAgent
        //agent.setSpeedFleeAdditive(0.1F); //adds on top of the base speed set below
        agent.setSpeedNormalBase(0.65F); //percent of 0.45D speed used (note: retarded)
        agent.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(20D);
    }

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}
	
	@Override
	public EntityLivingData onSpawnWithEgg(EntityLivingData par1EntityLivingData) {
		
		this.posY += 10;
		
		if (!worldObj.isRemote) {
			PhysicsWorld physWorld = BlockMonsters.physMan.getPhysicsWorld(worldObj);
			physWorld.chunkManager.addChunkloader(this);
		}
		
		return super.onSpawnWithEgg(par1EntityLivingData);
	}
	
}
