package blockmonsters.entity.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.vecmath.Vector3f;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import CoroUtil.entity.IObjectSerializable;

import blockmonsters.BlockMonsters;
import blockmonsters.entity.ai.JobAttackRoutineBlockMonster1;
import blockmonsters.entity.ai.LeafBlockMonster;
import blockmonsters.physics.PhysicsWorld;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.linearmath.Transform;

public class StructurePiece implements IStructureUser, IObjectSerializable {

	//Missing features
	//client side getting sizeHalf data, and axisYRadius from the grid
	
	//New, hopefully cleaner design implementing jbullet objects
	
    //stuff for client side
    //abs pos
    //rotation matrix to tell nodes and blocks
	
	
	
	//Used for top piece
	public HashMap<Integer, StructurePiece> childPiecesLookupAll;
	
	//Used for all but top piece
	public double relX; //local unrotated grid coords
	public double relY;
	public double relZ;
	
	//Used for all
	public World world;
	
	public int ID;
	public IStructureUser parent;
	public StructurePiece topPiece;
	public RigidBody rigidBody = null;
    public TypedConstraint constraint = null;
	
	public List<StructureNode> nodes;
    public List<StructurePiece> childPieces;
    public HashMap<Integer, StructurePiece> childPiecesLookup;
    
    public float scale = 1F;
    public float mass = 1F;
    
	public Vector3f relPosRotated = new Vector3f();
	public AxisAlignedBB relAABB;
	//the center of the used grid space - with new jbullet implementation this might be unneeded
	public double relXCenterGrid;
	public double relYCenterGrid;
	public double relZCenterGrid;
	public Vector3f sizeHalf;
	public float axisYRadius; //capsule shape support requires grid usage along the y axis
	//public float axisYHeight; sizeHalf.y * 2
    
    public StructurePiece(int parID, IStructureUser parParent, float x, float y, float z) {
		ID = parID;
		parent = parParent;
		if (isTop()) {
			childPiecesLookupAll = new HashMap<Integer, StructurePiece>();
		} else {
			scale = ((StructurePiece) parent).scale;
		}
		//needs better fix - changed to LeafBlockMonster, still needs cleaner implementation, a new base class or linkage to another base class
		if (parent instanceof LeafBlockMonster) {
			world = ((LeafBlockMonster) parent).ent.worldObj;
		} else {
			System.out.println("!!!!!!!!!! NO WORLD OBJECT! WAT!");
		}
		relX = x;
		relY = y;
		relZ = z;
		nodes = new ArrayList<StructureNode>();
		childPieces = new ArrayList<StructurePiece>();
		childPiecesLookup = new HashMap<Integer, StructurePiece>();
	}
    
    public StructurePiece getTopPiece() {
    	return childPiecesLookupAll.get(0);
    }
	
	//should be used for both read from disk and client side creation, it handles the creation of the instances
	public static StructurePiece createFromNBTRecursive(IStructureUser parParent, NBTTagCompound parData) {
		StructurePiece sp = new StructurePiece(parData.getInteger("ID"), parParent, (float)parData.getDouble("relX"), (float)parData.getDouble("relY"), (float)parData.getDouble("relZ"));
		sp.readFromNBT(parData);
		sp.calculateCenter();
		if (parParent instanceof StructurePiece) ((StructurePiece) parParent).addChildPiece(sp);
		sp.initPhysicsObject(new CapsuleShape(sp.axisYRadius, sp.sizeHalf.y*2), /*new Vector3f((float)sp.absX, (float)sp.absY, (float)sp.absZ), */sp.mass);
		
		int childPieceCount = parData.getInteger("childPieceCount");
		for (int i = 0; i < childPieceCount; i++) {
			StructurePiece spChild = createFromNBTRecursive(sp, parData.getCompoundTag("childPiece_" + i));
			//sp.addChildPiece(spChild);
		}
		return sp;
	}
	
	//Calculate the local center of this piece
	public void calculateCenter() {
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
		for (int i = 0; i < nodes.size(); i++) {
			StructureNode sn = nodes.get(i);
			if (sn.relX < aabb.minX) aabb.minX = sn.relX;
			if (sn.relX > aabb.maxX) aabb.maxX = sn.relX;
			if (sn.relY < aabb.minY) aabb.minY = sn.relY;
			if (sn.relY > aabb.maxY) aabb.maxY = sn.relY;
			if (sn.relZ < aabb.minZ) aabb.minZ = sn.relZ;
			if (sn.relZ > aabb.maxZ) aabb.maxZ = sn.relZ;
		}
		
		float offsetX = 0;
		float offsetY = 0;
		float offsetZ = 0;
		
		if (aabb.minX < 0) offsetX = (float)-aabb.minX;
		if (aabb.minY < 0) offsetY = (float)-aabb.minY;
		if (aabb.minZ < 0) offsetZ = (float)-aabb.minZ;
		
		relXCenterGrid = ((aabb.maxX+offsetX)/2D)-offsetX;
		relYCenterGrid = ((aabb.maxY+offsetY)/2D)-offsetY;
		relZCenterGrid = ((aabb.maxZ+offsetZ)/2D)-offsetZ;
		relAABB = aabb;
		sizeHalf = new Vector3f(((float)aabb.maxX+offsetX)/2F, ((float)aabb.maxY+offsetY)/2F, ((float)aabb.maxZ+offsetZ)/2F);
		axisYRadius = 1+Math.max(((float)aabb.maxX+offsetX)/2F, ((float)aabb.maxZ+offsetZ)/2F);
	}
	
	//convinience method, sets the physics origin to its parents position + this's relative position, assumes no rotation, so might need adjusting to relPosRotated if its updated...
	public RigidBody initPhysicsObject(CollisionShape shape, float mass) {
		if (parent instanceof StructurePiece) {
			Entity ent = parent.getOwnerEntity();
			Vector3f newPos = new Vector3f((float)(ent.posX+relX), (float)(ent.posY+relY), (float)(ent.posZ+relZ));
			return initPhysicsObject(shape, newPos, mass);
		}
		return null;
	}
	
	//default to capsule
	public RigidBody initPhysicsObject() {
		calculateCenter();
		return initPhysicsObject(new CapsuleShape(axisYRadius, sizeHalf.y*2)); 
	}
	
	public RigidBody initPhysicsObject(float mass) {
		calculateCenter();
		return initPhysicsObject(new CapsuleShape(axisYRadius, Math.max(1, (sizeHalf.y*2)-2)), mass); 
	}
	
	public RigidBody initPhysicsObject(CollisionShape shape) {
		return initPhysicsObject(shape, 1F);
	}
	
	public RigidBody initPhysicsObject(CollisionShape shape, Vector3f spawnPos, float mass) {
		//if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			PhysicsWorld physWorld = BlockMonsters.physMan.getPhysicsWorld(world);
			//System.out.println(physWorld.dynamicsWorld);
			rigidBody = physWorld.addRigidBody(shape/*sizeHalf*/, spawnPos/*new Vector3f((float)absX, (float)absY, (float)absZ)*/, mass);
			rigidBody.setDamping(0.2F, 0.2F);
			//physics init of object
			physWorld.initObject(rigidBody);
		//}
		return rigidBody;
		
	}
	
	//depends on the IDs to properly update the correct pieces (incase the list order goes out of order??), this method could be edited for updating a single piece too 
	public void readFromNBTRecursive(NBTTagCompound parData) {
		readFromNBT(parData);
		int childPieceCount = parData.getInteger("childPieceCount");
		
		for (int i = 0; i < childPieceCount; i++) {
			//lookup via nbt index, convert to ID and use hashmap to get piece @_@
			NBTTagCompound childData = parData.getCompoundTag("childPiece_" + i);
			StructurePiece spChild = childPiecesLookup.get(childData.getInteger("ID"));
			if (spChild != null) {
				spChild.readFromNBTRecursive(childData);
			} else {
				System.out.println("CRITICAL: StructurePiece ID lookup fail on update nbt");
			}
		}
	}
	
	public void writeToNBTRecursive(NBTTagCompound parData) {
		writeToNBTDisk(parData);
		parData.setInteger("childPieceCount", childPieces.size());
		for (int i = 0; i < childPieces.size(); i++) {
			NBTTagCompound childData = new NBTTagCompound();
			childPieces.get(i).writeToNBTRecursive(childData);
			parData.setCompoundTag("childPiece_" + i, childData);
		}
	}
	
	//used for syncing server to client, needs a mode between a first time sync and a constant update sync
	public void readFromNBT(NBTTagCompound parData) {
		//read in data relevant to this piece only, createFromNBT recurses deeper
		if (parData.hasKey("ID")) ID = parData.getInteger("ID");
		if (parData.hasKey("scale")) scale = parData.getFloat("scale");
		if (parData.hasKey("sizeX")) {
			sizeHalf = new Vector3f(parData.getFloat("sizeX")/2, parData.getFloat("sizeY")/2, parData.getFloat("sizeZ")/2);
		}
		if (parData.hasKey("axisYRadius")) axisYRadius = parData.getFloat("axisYRadius");
		if (parData.hasKey("mass")) mass = parData.getFloat("mass");
		if (parData.hasKey("relX")) {
			relX = parData.getDouble("relX");
			relY = parData.getDouble("relY");
			relZ = parData.getDouble("relZ");
		}
		/*if (parData.hasKey("absX")) {
			absX = parData.getDouble("absX");
			absY = parData.getDouble("absY");
			absZ = parData.getDouble("absZ");
		}*/
	}
	
	public void writeToNBT(NBTTagCompound parData) {
		parData.setInteger("ID", ID);
		parData.setFloat("scale", scale);
		parData.setFloat("sizeX", sizeHalf.x*2F);
		parData.setFloat("sizeY", sizeHalf.y*2F);
		parData.setFloat("sizeZ", sizeHalf.z*2F);
		parData.setFloat("axisYRadius", axisYRadius);
		parData.setFloat("mass", mass);
		parData.setDouble("relX", relX);
		parData.setDouble("relY", relY);
		parData.setDouble("relZ", relZ);
		/*parData.setDouble("absX", absX);
		parData.setDouble("absY", absY);
		parData.setDouble("absZ", absZ);*/
	}
	
	//interface method for disk event
	@Override
	public void readFromNBTDisk(NBTTagCompound parData) {
		readFromNBT(parData);
	}
	
	//interface method for disk event
	@Override
	public void writeToNBTDisk(NBTTagCompound parData) {
		writeToNBT(parData);
	}
	
	public void addChildPiece(StructurePiece parPiece) {
		childPieces.add(parPiece);
		childPiecesLookup.put(parPiece.ID, parPiece);
		if (isTop()) {
			System.out.println("adding to childPiecesLookupAll: " + parPiece.ID);
			childPiecesLookupAll.put(parPiece.ID, parPiece);
			parPiece.topPiece = this;
		} else {
			StructurePiece tempParent = (StructurePiece)parent;
			while (tempParent.parent instanceof StructurePiece) {
				tempParent = (StructurePiece)tempParent.parent;
			}
			System.out.println("adding to childPiecesLookupAll via parent lookup: " + parPiece.ID);
			tempParent.childPiecesLookupAll.put(parPiece.ID, parPiece);
			
			//set this childs pieces top of the tree
			parPiece.topPiece = tempParent;
		}
	}
	
	public void cleanupRecursive() {
		for (int i = 0; i < childPieces.size(); i++) {
			childPieces.get(i).cleanupRecursive();
		}
		
		cleanup();
	}
	
	public void cleanup() {
		childPieces.clear();
		childPiecesLookup.clear();
		if (isTop()) childPiecesLookupAll.clear();
		
		PhysicsWorld physWorld = BlockMonsters.physMan.getPhysicsWorld(world);		
		
		if (physWorld != null) {
			if (rigidBody != null) {
				System.out.println("cleaning up physics objects");
				physWorld.dynamicsWorld.removeRigidBody(rigidBody);
				if (constraint != null) physWorld.dynamicsWorld.removeConstraint(constraint);
				rigidBody.destroy();
				
			}
		}
	}
	
	public void setScale(float parScale, boolean setForNodes, boolean setForPieces) {
		scale = parScale;
		if (setForNodes) {
			for (int i = 0; i < nodes.size(); i++) {
				StructureNode sn = nodes.get(i);
				sn.setScale(parScale);
			}
		}
		if (setForPieces) {
			for (int i = 0; i < childPieces.size(); i++) {
				StructurePiece sp = childPieces.get(i);
				sp.setScale(parScale, setForNodes, setForPieces);
			}
		}
	}
	
	public void ownerDied() {
		for (int i = 0; i < nodes.size(); i++) {
			StructureNode sn = nodes.get(i);
			sn.ownerDied();
		}
		for (int i = 0; i < childPieces.size(); i++) {
			StructurePiece sp = childPieces.get(i);
			sp.ownerDied();
		}
	}
	
	@Override
	public boolean passBackAttackFrom(IStructureUser parPasser, DamageSource par1DamageSource, float par2, Vector3f coordsSource) {
		
		//maybe still pass up if theres no handles collision flag of sorts
		//still needs a way to pass up to master that a hit happened etc, plan more
		
		//this should only happen since pieces handle their own hits now
		if (parPasser instanceof StructureNode) {
			handleHit(this, par1DamageSource, par2, coordsSource);
		}
		if (parent != null) {
			return parent.passBackAttackFrom(parPasser, par1DamageSource, par2, coordsSource);
		}
		return true;
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
	
	public void handleHit(IStructureUser parPasser, DamageSource par1DamageSource, float par2, Vector3f coordsSource) {
		Random rand = new Random();
		applyTorqueForce(new Vector3f(rand.nextFloat() * 100F, 0, 0), new Vector3f(1, 1, 1), 10F);
		applyThrustForce(new Vector3f(0, 3500, 0), new Vector3f());
	}
	
	public void applyTorqueForce(Vector3f parForce, Vector3f parRelToCenterOfMass, float parAmount) {
		parForce.z *= -1;
		Vector3f inertia = new Vector3f();
		inertia.cross(parRelToCenterOfMass, parForce);
		inertia.scale(parAmount);
		
		rigidBody.applyTorqueImpulse(inertia);
		
		//this helped!
		rigidBody.setActivationState(CollisionObject.ACTIVE_TAG);
	}
	
	public void applyThrustForce(Vector3f parForce, Vector3f parRelToCenterOfMass) {
		rigidBody.applyForce(parForce, parRelToCenterOfMass);
		
		//this helped!
		rigidBody.setActivationState(CollisionObject.ACTIVE_TAG);
	}

	//true = found spot, false = spot not found 
	public boolean addBlockRecursive(StructureBlock parBlock) {
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i).block == null) {
				nodes.get(i).setNewBlock(parBlock);
				return true;
			}
		}
		for (int i = 0; i < childPieces.size(); i++) {
			if (childPieces.get(i).addBlockRecursive(parBlock)) return true;
		}
		return false;
	}
	
	public int getMissingBlockCount() {
		int curCount = 0;
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i).block == null) {
				curCount++;
			}
		}
		return curCount;
	}
	
	//recursive methods
	public int getMissingBlockCountRecursive() {
		int curCount = getMissingBlockCount();
		for (int i = 0; i < childPieces.size(); i++) {
			curCount += childPieces.get(i).getMissingBlockCountRecursive();
		}
		return curCount;
	}
	
	public void tick() {
		Transform trns = new Transform();
		rigidBody.getWorldTransform(trns);
		
		Vector3f vec = new Vector3f((float)this.relX, (float)this.relY, (float)this.relZ);
		trns.basis.transform(vec);
		
		//update a cache of the rotated local piece coords, usable in world coords if absolute of parent is added
		relPosRotated = vec;
		
		for (int i = 0; i < nodes.size(); i++) {
			StructureNode sn = nodes.get(i);
			
			if (sn.block != null) {
				if (sn.block.isDead) {
					sn.removeBlock();
				} else {
					if (sn.block != null) {
						
						Vector3f vecNode = new Vector3f((float)sn.relX, (float)sn.relY, (float)sn.relZ);
						trns.basis.transform(vecNode);
						
						//temp change
						
						Random rand = new Random();
						
						int range = 2;
						
						/*sn.block.rotationPitchB+=rand.nextInt(range)-rand.nextInt(range);
						sn.block.rotationYawB+=rand.nextInt(range)-rand.nextInt(range);
						sn.block.rotationRoll+=rand.nextInt(range)-rand.nextInt(range);*/
						
						//one time adjustment
						if (sn.block.rotationPitchB == 0) {
							sn.block.rotationPitchB = rand.nextInt(360);
						}
						
						if (sn.block.rotationYawB == 0) {
							sn.block.rotationYawB = rand.nextInt(360);
						}
						
						if (sn.block.rotationRoll == 0) {
							sn.block.rotationRoll = rand.nextInt(360);
						}
						
						sn.block.setPositionAndRotation(trns.origin.x+vecNode.x, trns.origin.y+vecNode.y, trns.origin.z+vecNode.z, sn.block.rotationYawB, sn.block.rotationPitchB, sn.block.rotationRoll);
						//sn.block.setPositionAndRotation(trns.origin.x+vecNode.x, trns.origin.y+vecNode.y, trns.origin.z+vecNode.z, 0, 0, 0);
						
						sn.block.motionX = 0.0;
						sn.block.motionY = 0.0;
						sn.block.motionZ = 0.0;
					}
				}
			}
		}
		
		for (int i = 0; i < childPieces.size(); i++) {
			StructurePiece sp = childPieces.get(i);
			
			sp.tick();
		}
		
		//rigidBody.setFriction(1F);
	}

	public boolean isTop() {
		return !(parent instanceof StructurePiece);
	}

	@Override
	public Entity getOwnerEntity() {
		if (isTop()) {
			return parent.getOwnerEntity();
		} else {
			if (topPiece == null) {
				System.out.println("bug, crash");
				return null;
			} else {
				return topPiece.getOwnerEntity();
			}
		}
	}
}
