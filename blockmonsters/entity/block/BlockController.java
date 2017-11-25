package blockmonsters.entity.block;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import CoroUtil.bt.entity.EntityAnimalBase;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockController extends EntityAnimalBase implements IEntityAdditionalSpawnData
{

	public float rotationYawB = 0;
	public float rotationPitchB = 0;
	public float rotationRoll = 0;
	public float prevRotationRoll = 0;
	
	public float gravity = 0.04F;
	public boolean blockToEntCollision = true;
	
	public int state = 0; //generic state var
	
    public BlockController(World var1)
    {
        super(var1);
    }
    
    //TESTING COLLISION STUFF
    @Override
    public AxisAlignedBB getCollisionBox(Entity par1Entity) {
    	return super.getCollisionBox(par1Entity);
    }
    
    @Override
    public AxisAlignedBB getBoundingBox() {
    	return super.getBoundingBox();
    }
    
    @Override
    public float getCollisionBorderSize() {
    	return super.getCollisionBorderSize();
    }
    
    @Override
    public void applyEntityCollision(Entity par1Entity) {
    	super.applyEntityCollision(par1Entity);
    }

    @Override
    public void writeSpawnData(ByteArrayDataOutput data)
    {
    	
    }

    @Override
    public void readSpawnData(ByteArrayDataInput data)
    {
    	
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public boolean isInRangeToRenderDist(double var1)
    {
        return true;
    }

    @Override
    public float getShadowSize()
    {
        return 0.0F;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public boolean isInRangeToRenderVec3D(Vec3 asd)
    {
        return true;
    }

    @Override
    public boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    public void entityInit() {
    	super.entityInit();
    	this.dataWatcher.addObject(2, Float.valueOf(rotationYawB));
    	this.dataWatcher.addObject(3, Float.valueOf(rotationPitchB));
    	this.dataWatcher.addObject(4, Float.valueOf(rotationRoll));
    	this.dataWatcher.addObject(5, Integer.valueOf(state));
    }

    @Override
    public boolean canBePushed()
    {
        return !this.isDead;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }

    @Override
    public void onUpdate()
    {
    	//datawatchers
    	if (worldObj.isRemote) {
    		rotationYaw = rotationYawB = dataWatcher.getWatchableObjectFloat(2);
    		rotationPitch = rotationPitchB = dataWatcher.getWatchableObjectFloat(3);
    		rotationRoll = dataWatcher.getWatchableObjectFloat(4);
    		state = dataWatcher.getWatchableObjectInt(5);
    	} else {
    		dataWatcher.updateObject(2, rotationYawB);
    		dataWatcher.updateObject(3, rotationPitchB);
    		dataWatcher.updateObject(4, rotationRoll);
    		dataWatcher.updateObject(5, state);
    	}
    	
    	//Main movement
        /*this.motionX *= (double)speedSlowing;
        this.motionY *= (double)speedSlowing;
        this.motionZ *= (double)speedSlowing;
        this.motionY -= (double)gravity;*/
        
        /*this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        
        this.setPosition(this.posX, this.posY, this.posZ);*/
        
        if (!worldObj.isRemote) {
        	
        	
        } else {
        	
        }
        
        //taken from super onUpdate()
        boolean superTick = true;
        if (superTick) {
        	super.onUpdate();
        } else {
        	this.prevDistanceWalkedModified = this.distanceWalkedModified;
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.prevRotationPitch = this.rotationPitch = this.rotationPitchB;
            this.prevRotationYaw = this.rotationYaw = this.rotationYawB;
            this.prevRotationRoll = this.rotationRoll;
        }
    }
    
    public void setPositionAndRotation(double par1, double par3, double par5, float par7, float par8, float parRoll)
    {
        this.prevPosX = this.posX = par1;
        this.prevPosY = this.posY = par3;
        this.prevPosZ = this.posZ = par5;
        this.prevRotationYaw = this.rotationYaw = this.rotationYawB = par7;
        this.prevRotationPitch = this.rotationPitch = this.rotationPitchB = par8;
        this.prevRotationRoll = this.rotationRoll = parRoll;
        this.ySize = 0.0F;
        double d3 = (double)(this.prevRotationYaw - par7);

        if (d3 < -180.0D)
        {
            this.prevRotationYaw += 360.0F;
        }

        if (d3 >= 180.0D)
        {
            this.prevRotationYaw -= 360.0F;
        }

        this.setPosition(this.posX, this.posY, this.posZ);
        this.setRotation(par7, par8, parRoll);
    }
    
    protected void setRotation(float par1, float par2, float parRoll)
    {
        this.rotationYaw = par1 % 360.0F;
        this.rotationPitch = par2 % 360.0F;
        this.rotationRoll = parRoll % 360.0F;
    }
    
    public void moveAway(Entity ent, Entity targ, float speed) {
		double vecX = ent.posX - targ.posX;
		double vecY = ent.posY - targ.posY;
		double vecZ = ent.posZ - targ.posZ;

		double dist2 = (double)Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
		ent.motionX += vecX / dist2 * speed;
		ent.motionY += vecY / dist2 * speed;
		ent.motionZ += vecZ / dist2 * speed;
	}
    
    public void moveTowards(Entity ent, Entity targ, float speed) {
		double vecX = targ.posX - ent.posX;
		double vecY = targ.posY - ent.posY;
		double vecZ = targ.posZ - ent.posZ;

		double dist2 = (double)Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
		ent.motionX += vecX / dist2 * speed;
		ent.motionY += vecY / dist2 * speed;
		ent.motionZ += vecZ / dist2 * speed;
	}
    
    //this is probably never called unless something specifically handles this block, since its not a standard living entity that can take damage
    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
    	//setDead();
    	return super.attackEntityFrom(par1DamageSource, par2);
    }
    
    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
    	/*for (int i = 0; i < 1; i++) {
    		
    		float speed = 0.1F;
    		float randPos = 8.0F;
    		float ahead = 2.5F;
    		
    		EntityMeteorTrailFX particle = new EntityMeteorTrailFX(worldObj, 
    				posX, 
    				posY, 
    				posZ, motionX, 0.25F, motionZ, 0, posX, posY, posZ);
    		
    		particle.maxScale = 3F;
    		particle.setMaxAge(100);
    		particle.motionX = (rand.nextFloat()*2-1) * speed;
    		particle.motionY = (rand.nextFloat()*2-1) * 0.1F;
    		particle.motionZ = (rand.nextFloat()*2-1) * speed;

    		particle.spawnAsWeatherEffect();
    	}*/
    }

	@Override
	public void readEntityFromNBT(NBTTagCompound data) {
		super.readEntityFromNBT(data);
		gravity = data.getFloat("gravity");
		
		//prevent falling through world by giving collisionchunkmanager time to update
		this.posY += 50;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound data) {
		super.writeEntityToNBT(data);
		data.setFloat("gravity", gravity);
	}
}
