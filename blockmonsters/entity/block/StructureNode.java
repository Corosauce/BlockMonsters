package blockmonsters.entity.block;

import javax.vecmath.Vector3f;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class StructureNode implements IStructureUser {

	//this is mostly a server side class, atm its unneeded overhead for client syncing
	
	//public IStructureUser parent;
	public StructurePiece parent;
	
	public double relX;
	public double relY;
	public double relZ;
	
	public int blockID;
	public int blockMeta;
	
	public boolean needBuild;
	public boolean render;
	
	public float scale = 1F;
	
	public StructureBlock block;
	//Vector3f rotationVec = new Vector3f(); //this might not be needed, could use the parent pieces rigidbody.transform.basis vec
	
	public StructureNode(StructurePiece parParent, int parX, int parY, int parZ/*int parID, int parMeta, */) {
		relX = parX;
		relY = parY;
		relZ = parZ;
		/*id = parID;
		meta = parMeta;*/
		
		render = false;
		needBuild = true;
		
		parent = parParent;
		scale = parent.scale;
		/*if (parent instanceof StructurePieceMessy) {
			scale = ((StructurePieceMessy) parent).scale;
		}*/
	}
	
	public void setScale(float parScale) {
		scale = parScale;
		if (block != null) block.scale = parScale;
	}
	
	public void setNewBlock(StructureBlock sb) {
		block = sb;
		block.scale = scale;
		//block.parent = this;
		block.parent = parent; //for now, set blocks parent to the piece instead of node, makes parent a StructurePiece
		block.parentNode = this;
		//fb.parent = parPiece;
		if (block.parent != null) {
			//System.out.println("setting parent to: " + ((StructurePiece)block.parent).ID);
			block.parentPieceID = ((StructurePiece)block.parent).ID;
		}
	}
	
	public void removeBlock() {
		block = null;
	}
	
	public void blockDied() {
		removeBlock();
	}

	@Override
	public boolean passBackAttackFrom(IStructureUser parPasser, DamageSource par1DamageSource, float par2, Vector3f coordsSource) {
		if (block != null) {
			block.ownerDied();
			removeBlock(); //release!
		}
		return parent.passBackAttackFrom(this, par1DamageSource, par2, coordsSource);
	}

	@Override
	public boolean passInteract(IStructureUser parPasser,
			EntityPlayer par1EntityPlayer) {
		
		if (parent != null) {
			parent.passInteract(parPasser, par1EntityPlayer);
			
			return true;
		}
		
		return false;
	}
	
	public Vec3 getCollisionDifference() {
		
		if (block != null) {
	        
	        int curX = MathHelper.floor_double(block.posX);
	        int curY = MathHelper.floor_double(block.posY);
	        int curZ = MathHelper.floor_double(block.posZ);
	    	
	    	int id = block.worldObj.getBlockId(curX, curY, curZ);
	    	
	    	if (id != 0) {
	    		//collide event
	    		
	    		//lazy collision vector temp!
	    		return Vec3.createVectorHelper(0, 0.03, 0);
	    	}
		}
		
		return Vec3.createVectorHelper(0, 0, 0);
	}
	
	public void ownerDied() {
		if (block != null) block.ownerDied();
	}

	@Override
	public Entity getOwnerEntity() {
		return parent.getOwnerEntity();
	}
	
}
