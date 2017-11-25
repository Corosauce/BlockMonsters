package blockmonsters.entity.block;

import java.util.List;

import javax.vecmath.Vector3f;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import CoroUtil.entity.block.MovingBlock;
import blockmonsters.BlockMonsters;
import blockmonsters.entity.BlockMonster1;

import com.bulletphysics.linearmath.Transform;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class StructureBlock extends MovingBlock {
	
	public IStructureUser parent; //this is set to piece, not node, meant to be temporary
	public StructureNode parentNode;
	
	public int ownerEntityID = -1;
	public int parentPieceID = -1; //skipping node and going right to piece for now, to lookup rotations
	
	public Vec3 lastPos = Vec3.createVectorHelper(0, 0, 0);
	
	public StructureBlock(World var1) {
		super(var1);

		this.gravity = 0;
		this.blockifyDelay = -1;
		
		this.blockID = Block.stone.blockID;
	}
	
	public StructureBlock(World var1, int parBlockID, int parMeta, IStructureUser parParent) {
		super(var1, parBlockID, parMeta);

		this.gravity = 0;
		this.blockifyDelay = -1;
		
		rotationPitchB = var1.rand.nextInt(360);
		rotationRoll = var1.rand.nextInt(360);
		rotationYawB = var1.rand.nextInt(360);
		
		parent = parParent;
	}
	
	@Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
    	//setDead();
		ownerDied();
    	//System.out.println(par1DamageSource.damageType);
    	if (!par1DamageSource.damageType.equals("onFire") && !par1DamageSource.damageType.equalsIgnoreCase("lava")) {
    		if (parentNode != null) {
    			StructureNode sn = (StructureNode)parentNode;
    			//Vec3 vec1 = Vec3.createVectorHelper(posX-(sn.relX+sn.rotationVec.xCoord), posY-(sn.relY+sn.rotationVec.yCoord), posZ-(sn.relZ+sn.rotationVec.zCoord));
    			//Vec3 vec1 = Vec3.createVectorHelper(posX+(sn.relX-sn.rotationVec.xCoord), posY+(sn.relY-sn.rotationVec.yCoord), posZ+(sn.relZ-sn.rotationVec.zCoord));
    			
    			//parentNode.passBackAttackFrom(null, par1DamageSource, par2, vec1);
    			//parentNode.passBackAttackFrom(null, par1DamageSource, par2, Vec3.createVectorHelper(posX, posY, posZ));
    			parentNode.passBackAttackFrom(null, par1DamageSource, par2, ((StructurePiece)parent).relPosRotated);
    		}
    	}
    	return true;
    }
	
	@Override
	public boolean interactFirst(EntityPlayer par1EntityPlayer) {
		if (parentNode != null) {
			parentNode.passInteract(null, par1EntityPlayer);
			
			return true;
		}
		return super.interactFirst(par1EntityPlayer);
	}
	
	@Override
    public void onUpdate()
    {
        super.onUpdate();
        
        if (this.worldObj.isRemote) {
	        this.motionX = 0;
	        this.motionY = 0;
	        this.motionZ = 0;
        } else {
        	if (parentNode == null) {
        		setDead();
        	}
        }
        
        this.entityCollisionReduction = 1;
        
        /*this.rotationPitchB+=10;
        this.rotationYawB+=10;
        this.rotationRoll+=10;*/
        

        
    	//an attempt at smoothing out client side using relative position tracking
    	//in long run, blocks should get a copy of the client side jbullet speeds for the rigidbody
    	//if (worldObj.isRemote) {
	    	
    		//off
	    	double range = 0.9D;
	    	
	    	double posDiffX2 = (this.lastPos.xCoord - this.posX) * range;
	    	double posDiffY2 = (this.lastPos.yCoord - this.posY) * range;
			double posDiffZ2 = (this.lastPos.zCoord - this.posZ) * range;
			
			//this.motionX = -posDiffX2;
			//this.motionY = -posDiffY2;
			//this.motionZ = -posDiffZ2;
    	//}
        
		//break away obsticles, trees
		if (!worldObj.isRemote) {
			int tryX = MathHelper.floor_double(posX);
	    	int tryY = MathHelper.floor_double(posY);
	    	int tryZ = MathHelper.floor_double(posZ);
    		int idTry = worldObj.getBlockId(tryX, tryY, tryZ);
    		
    		if (idTry != 0) {
    			Material mat = Block.blocksList[idTry].blockMaterial;
    			if (mat == Material.leaves || mat == Material.wood) {
    				System.out.println("break block");
    				worldObj.setBlock(tryX, tryY, tryZ, 0);
    				System.out.println("world: " + worldObj);
    				BlockMonsters.physMan.getPhysicsWorld(worldObj).chunkManager.markChunksNeedsUpdate(new ChunkCoordinates(tryX, tryY, tryZ), 0);
    			}
    		}
		}
			
        boolean blockToPlayerWalk = true;
    	
    	if (worldObj.isRemote && blockToPlayerWalk) {
    		
        	double size = 0.0D;
	        List entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(size, size, size));
	        
	        for (int i = 0; entities != null && i < entities.size(); ++i)
	        {
	            Entity var10 = (Entity)entities.get(i);
	            
	            if (var10 != null) {
	            	if (!var10.isDead) {
	            		if (var10.ridingEntity != null) continue;
			            if (!(var10 instanceof BlockMonster1 || var10 instanceof StructureBlock/*var10 instanceof EntityPlayer*/)) {
			            	//var10.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this), 4);

			            	//if under it
			            	if (var10.boundingBox.minY < this.boundingBox.maxY) {
			            		//if (parent instanceof StructurePiece) {
			            			//System.out.println("@!##!@#");
			            			float tempRelMaxY = 0;
			            			float tempAbsY = (float) this.boundingBox.maxY;
			            			//tempRelMaxY = ((StructurePiece) parent).relSize.maxY;
			            			//tempAbsY = (((StructurePiece) parent).absY;
			            			
			            			float structureTopY = (float)tempAbsY + tempRelMaxY + 1F;
			            			//safety
			            			boolean forceTeleport = true;
			            			if (/*forceTeleport || */var10.boundingBox.minY < this.boundingBox.maxY) {
			            				
			            			}
			            			double speed = Math.sqrt(var10.motionX * var10.motionX + var10.motionY * var10.motionY + var10.motionZ * var10.motionZ);
			            			if (var10.boundingBox.minY < this.boundingBox.minY + 0.2F) {
			            				double temp = var10.motionY;
			            				if (speed < 0.2F) {
			            					moveAway(var10, this, /*(float)speed * */0.2F);
			            				}
			            				var10.motionY = temp;
			            			} else {
			            				double posDiffX = (this.lastPos.xCoord - this.posX) * 0.3D;
			            				double posDiffZ = (this.lastPos.zCoord - this.posZ) * 0.3D;
			            				//System.out.println(this.lastPos.xCoord - this.posX);
			            				var10.setPosition(var10.posX, structureTopY + 0.3F, var10.posZ);
			            				var10.motionX -= posDiffX * 0.2D;
			            				var10.motionZ -= posDiffZ * 0.2D;
			            				var10.motionX += motionX * 0.3D;
			            				var10.motionZ += motionZ * 0.3D;
			            				//var10.setPosition(var10.posX - posDiffX, structureTopY + 0.3F, var10.posZ - posDiffZ);
			            				var10.motionY = 0;
			            				/*float rate = 0.05F;
				            			if (var10.motionY < rate) {
				            				if (var10.motionY < 0) var10.motionY = 0;
				            				var10.motionY += rate;
				            			}*/
			            			}
				            		var10.onGround = true;
			            		//}
			            		
			            	} else {
			            		
			            	}
			            	
			            	double speed = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
			            	//double speed2 = Math.sqrt(var10.motionX * var10.motionX + var10.motionY * var10.motionY + var10.motionZ * var10.motionZ);
			            	//if (speed < 0.3D) {
			            		//moveAway(var10, this, /*(float)speed * */0.5F);
				            	//break; //hmmmmm
			            	//}
			            }
	            	}
	            }
	        }
    	}
    	
    	lastPos = Vec3.createVectorHelper(posX, posY, posZ);
    }
	
	//was used for when ent block collided with world, was to provide force feedback, wasnt that reliable
	/*@Override
	public void damagedBlockAtCoord(ChunkCoordinates coords, float impactSpeed) {
		
		//if they are still part of the body, cancel
		if (this.blockifyDelay != -1) return;
		
		if (true) return;
		
		//second calc was using vars from first calc, fixed but untested and also converted to jbullet
		
		if (parent instanceof StructurePiece) {
			
    		StructurePiece sp = (StructurePiece) parent;
    		
    		//rotate relative node coords using parent pieces rotation
    		Transform trns = new Transform();
    		sp.rigidBody.getWorldTransform(trns);
    		
    		//rotate local
    		Vector3f vec = new Vector3f((float)parentNode.relX, (float)parentNode.relY, (float)parentNode.relZ);
    		trns.basis.transform(vec);
    		
    		//rotate hit
    		//Vector3f vec2 = new Vector3f((float)parentNode.relX, (float)parentNode.relY, (float)parentNode.relZ);
    		//trns.basis.transform(vec2);
    		
    		double vecX = parentNode.rotationVec.xCoord - sp.rotationVecCenter.xCoord;
    		double vecY = parentNode.rotationVec.yCoord - sp.rotationVecCenter.yCoord;
    		double vecZ = parentNode.rotationVec.zCoord - sp.rotationVecCenter.zCoord;
			
			Vec3 vecDistToCenter = Vec3.createVectorHelper(vecX, vecY, vecZ).normalize();
    		
			float vecX2 = (float) (this.posX - coords.posX+0.5F);
			float vecY2 = (float) (this.posY - coords.posY+0.5F);
			float vecZ2 = (float) (this.posZ - coords.posZ+0.5F);

	    	float dist2 = (float)Math.sqrt(vecX2 * vecX2 + vecY2 * vecY2 + vecZ2 * vecZ2);
			//Vec3 vec2 = Vec3.createVectorHelper(vecX2 / dist2, vecY2 / dist2, vecZ2 / dist2);
			Vector3f vec2 = new Vector3f(vecX2 / dist2, vecY2 / dist2, vecZ2 / dist2);
			
			//vec.zCoord *= -1;
			vec2.scale(8F * impactSpeed);
			
    		//((StructurePiece) parent).applyTorqueForce(vec2, vecDistToCenter, 0.2F);
    		((StructurePiece) parent).applyThrustForce(vec2, vec);
    	}
		
	}*/
	
	@Override
	public void tickCollisionEntities() {
		double size = 0.5D;
        List entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(size, size, size));
        
        //if (worldObj.isRemote) {
	        for (int i = 0; entities != null && i < entities.size(); ++i)
	        {
	            Entity var10 = (Entity)entities.get(i);
	            
	            if (var10 != null) {
	            	if (!var10.isDead) {
			            if (var10 instanceof EntityLivingBase) {
			            	if (!(var10 instanceof BlockMonster1)) {
			            		if (!(var10 instanceof EntityPlayer)) {
			            			boolean perform = true;
			            			if (var10 instanceof EntityLivingBase) {
			            				if (((EntityLivingBase) var10).deathTime > 0) {
			            					perform = false;
			            				}
			            			}
			            			if (perform) {
				            			var10.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this), 40);
				            			worldObj.playSoundEffect(this.posX, this.posY, this.posZ, BlockMonsters.modID+":melee_hit_flesh", 0.9F, 1F);
			            			}
			            		}
				            	if (parent instanceof StructurePiece) {
		
				            		StructurePiece sp = (StructurePiece) parent;
	
				            		//rotate relative node coords using parent pieces rotation
				            		Transform trns = new Transform();
				            		sp.rigidBody.getWorldTransform(trns);
				            		Vector3f vec = new Vector3f((float)parentNode.relX, (float)parentNode.relY, (float)parentNode.relZ);
				            		trns.basis.transform(vec);
				            		vec.normalize(); //needed? or are they normalized already
				            		
				            		//make vec
				            		vec.sub(sp.relPosRotated);
				            		
				            		/*double vecX = vec.x - sp.relPosRotated.x;
				            		double vecY = parentNode.rotationVec.yCoord - sp.relPosRotated.y;
				            		double vecZ = parentNode.rotationVec.zCoord - sp.relPosRotated.z;
				        			
				            		double vecX = parentNode.rotationVec.xCoord - sp.rotationVecCenter.xCoord;
				            		double vecY = parentNode.rotationVec.yCoord - sp.rotationVecCenter.yCoord;
				            		double vecZ = parentNode.rotationVec.zCoord - sp.rotationVecCenter.zCoord;*/
				            		
				        			//Vec3 vecDistToCenter = Vec3.createVectorHelper(vecX, vecY, vecZ).normalize();
				            		
				        			//Vector3f vecAway = getMoveAwayVector3f(this, var10);
				        			//vec.zCoord *= -1;
				        			
				        			//gravity! for some reason! (walk on fun)
				        			//vecAway.y -= 0.2F;
				        			
				            		//((StructurePiece) parent).applyTorqueForce(vec, vecAway, 0.1F);
				            		//((StructurePiece) parent).applyThrustForce(vec, vecAway, 1F);
				            		break;
				            	}
			            	}
			            	
			            } else {
			            	//double speed = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
			            	//double speed2 = Math.sqrt(var10.motionX * var10.motionX + var10.motionY * var10.motionY + var10.motionZ * var10.motionZ);
			            	//if (speed < 0.3D) {
			            		//moveAway(this, var10, (float)speed * 0.5F);
				            	//break; //hmmmmm
			            	//}
			            }
	            	}
	            }
	        }
        //}
        
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9)
    {
        this.setPosition(par1, par3, par5);
        this.setRotation(par7, par8);        
    }
	
	public void ownerDied() {
		this.gravity = 0.04F;
		this.blockifyDelay = 100;
		
		double amp = 0.8D;
		//this.motionY = 0.3F;
		this.motionY = (rand.nextDouble() * amp);
		this.motionX = (rand.nextDouble() * amp)-(rand.nextDouble() * amp);
		this.motionZ = (rand.nextDouble() * amp)-(rand.nextDouble() * amp);
		rotationPitchVel = rand.nextInt(300);
		rotationYawVel = rand.nextInt(300);
		
		//TEMP QUICK KILL
		//System.out.println("StructureBlock ownerDied quick kill on");
		//setDead();
	}
	
	/*@Override
	public void checkForSolidify() {
		
	}*/
	
	@Override
    public void writeSpawnData(ByteArrayDataOutput data)
    {
		super.writeSpawnData(data);
        data.writeInt(ownerEntityID);
        data.writeInt(parentPieceID);
    }

    @Override
    public void readSpawnData(ByteArrayDataInput data)
    {
    	super.readSpawnData(data);
    	ownerEntityID = data.readInt();
    	parentPieceID = data.readInt();
    }

}
