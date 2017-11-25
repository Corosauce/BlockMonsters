package blockmonsters.entity.block.profile;

import java.util.Random;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.objectweb.asm.tree.JumpInsnNode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import blockmonsters.BlockMonsters;
import blockmonsters.entity.BlockMonster1;
import blockmonsters.entity.ai.LeafBlockMonster;
import blockmonsters.entity.block.IStructureUser;
import blockmonsters.entity.block.StructureNode;
import blockmonsters.entity.block.StructurePiece;
import blockmonsters.entity.block.profile.ai.AnimationBend;
import blockmonsters.entity.block.profile.ai.PhysicsMuscle;
import blockmonsters.entity.block.profile.ai.SelectorAnimationSequence;
import blockmonsters.input.ControlInputs;
import blockmonsters.physics.PhysicsWorld;
import blockmonsters.playerdata.PlayerData;
import blockmonsters.playerdata.objects.Input;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;

public class ProfileWalker extends StructureProfileBase {

	public Vector3f lastPosTracked = new Vector3f();
	public int ticksToJump = 0;
	
	public Vector3f lastPosTrackedForBalence = new Vector3f();
	public int ticksToBalence = 0;
	
	public boolean wasKeyDown = false;
	
	public ProfileWalker(LeafBlockMonster parJob) {
		super(parJob);
	}
	
	@Override
	public void populateStructureData() {
		super.populateStructureData();
		
		//you're using box collision on legs, fix
		//also, keep carefull watch on leg orientation to jbullet orientation, relative grid factoring etc
		
		int spawnYOffset = 20;
		spawnYOffset = 0;
		
		int pieceIndex = 0;
    	StructurePiece mainBody = new StructurePiece(pieceIndex++, (IStructureUser)job, 0, 0, 0);
    	body = mainBody;
    	
    	//ive adjusted the shapes to account for the +1
    	//also changed < to <= for legs

    	int armLength = 1;
    	int armLengthLower = 2;
    	
    	//radius, 1 + this
    	int radX = 1;
    	int radY = 0;
    	int radZ = 1;
    	
    	//tweaking
    	/*armLength = 1;
    	armLengthLower = 3;
    	radX = 1;
    	radY = 0;
    	radZ = 1;*/
    	
    	int sizeX = radX*2;
    	int sizeY = radY*2;
    	int sizeZ = radZ*2;
    	
    	
    	int radius = Math.max(1, Math.max(sizeX, sizeZ));
    	
    	//we cant generate the predicted capsule radius area here, because adding to the grid will extend that height we predicted, changing it
    	
    	//body
    	for (int x = -sizeX/2; x <= sizeX/2; x++) {
    		for (int z = -sizeZ/2; z <= sizeZ/2; z++) {
    			for (int y = -sizeY/2; y <= sizeY/2; y++) {    			
    				mainBody.nodes.add(new StructureNode(mainBody, x, y, z));
    				if (x == -sizeX/2 || x == sizeX/2 || z == -sizeZ/2 || z == sizeZ/2) {
    					mainBody.nodes.add(new StructureNode(mainBody, x, y+1, z));
    				}
    			}
    		}
    	}
    	
    	float weightBody = 5;
    	float weightLimbs = 5;
    	
    	float dampLinear = 0.2F;
    	float dampAngular = 0.2F;
    	
    	mainBody.calculateCenter();
    	//RigidBody rb = mainBody.initPhysicsObject(new CapsuleShape(mainBody.axisYRadius, mainBody.sizeHalf.y*2), new Vector3f((float)job.ent.posX, (float)job.ent.posY, (float)job.ent.posZ), 5F);
    	RigidBody rb = mainBody.initPhysicsObject(new BoxShape(new Vector3f(sizeX+1, sizeY+1, sizeZ+1)), new Vector3f((float)job.ent.posX, (float)job.ent.posY+spawnYOffset, (float)job.ent.posZ), weightBody);
    	rb.setDamping(0, 0);
    	
    	//if (true) return;
    	
    	
    	float spaceBetweenArms = 0.5F;
    	
    	float limbOffsetX = (sizeX/2)+1;
    	float limbOffsetZ = (sizeZ/2);
    	
    	//legA upper
    	StructurePiece limb1 = new StructurePiece(pieceIndex++, mainBody, limbOffsetX, 0, -limbOffsetZ);
    	for (int length = 0; length <= armLength; length++) {
    		limb1.nodes.add(new StructureNode(limb1, 0, -length, 0));
    	}
    	mainBody.addChildPiece(limb1);
    	rb = limb1.initPhysicsObject(weightLimbs);
    	rb.setDamping(dampLinear, dampAngular);
    	
    	//legA lower
    	StructurePiece limb11 = new StructurePiece(pieceIndex++, limb1, 0, -armLength-spaceBetweenArms, 0);
    	for (int length = 0; length <= armLengthLower; length++) {
    		limb11.nodes.add(new StructureNode(limb11, 0, -length, 0));
    	}
    	limb1.addChildPiece(limb11);
    	rb = limb11.initPhysicsObject(weightLimbs);
    	rb.setDamping(dampLinear, dampAngular);
    	
    	//legB upper
    	StructurePiece limb2 = new StructurePiece(pieceIndex++, mainBody, 0, 0, -limbOffsetZ);
    	for (int length = 0; length <= armLength; length++) {
    		limb2.nodes.add(new StructureNode(limb2, 0, -length, 0));
    	}
    	mainBody.addChildPiece(limb2);
    	rb = limb2.initPhysicsObject(weightLimbs);
    	rb.setDamping(dampLinear, dampAngular);
    	
    	//legB lower
    	StructurePiece limb22 = new StructurePiece(pieceIndex++, limb2, 0, -armLength-spaceBetweenArms, 0);
    	for (int length = 0; length <= armLengthLower; length++) {
    		limb22.nodes.add(new StructureNode(limb22, 0, -length, 0));
    	}
    	limb2.addChildPiece(limb22);
    	rb = limb22.initPhysicsObject(weightLimbs);
    	rb.setDamping(dampLinear, dampAngular);
    	
    	//legC upper
    	StructurePiece limb3 = new StructurePiece(pieceIndex++, mainBody, -limbOffsetX, 0, -limbOffsetZ);
    	for (int length = 0; length <= armLength; length++) {
    		limb3.nodes.add(new StructureNode(limb3, 0, -length, 0));
    	}
    	mainBody.addChildPiece(limb3);
    	rb = limb3.initPhysicsObject(weightLimbs);
    	rb.setDamping(dampLinear, dampAngular);
    	
    	//legC lower
    	StructurePiece limb33 = new StructurePiece(pieceIndex++, limb3, 0, -armLength-spaceBetweenArms, 0);
    	for (int length = 0; length <= armLengthLower; length++) {
    		limb33.nodes.add(new StructureNode(limb33, 0, -length, 0));
    	}
    	limb3.addChildPiece(limb33);
    	rb = limb33.initPhysicsObject(weightLimbs);
    	rb.setDamping(dampLinear, dampAngular);
    	
    	//legD upper
    	StructurePiece limb4 = new StructurePiece(pieceIndex++, mainBody, limbOffsetX, 0, limbOffsetZ);
    	for (int length = 0; length <= armLength; length++) {
    		limb4.nodes.add(new StructureNode(limb4, 0, -length, 0));
    	}
    	mainBody.addChildPiece(limb4);
    	rb = limb4.initPhysicsObject(weightLimbs);
    	rb.setDamping(dampLinear, dampAngular);
    	
    	//legD lower
    	StructurePiece limb44 = new StructurePiece(pieceIndex++, limb4, 0, -armLength-spaceBetweenArms, 0);
    	for (int length = 0; length <= armLengthLower; length++) {
    		limb44.nodes.add(new StructureNode(limb44, 0, -length, 0));
    	}
    	limb4.addChildPiece(limb44);
    	rb = limb44.initPhysicsObject(weightLimbs);
    	rb.setDamping(dampLinear, dampAngular);

    	//legE upper
    	StructurePiece limb5 = new StructurePiece(pieceIndex++, mainBody, 0, 0, limbOffsetZ);
    	for (int length = 0; length <= armLength; length++) {
    		limb5.nodes.add(new StructureNode(limb5, 0, -length, 0));
    	}
    	mainBody.addChildPiece(limb5);
    	rb = limb5.initPhysicsObject(weightLimbs);
    	rb.setDamping(dampLinear, dampAngular);
    	
    	//legE lower
    	StructurePiece limb55 = new StructurePiece(pieceIndex++, limb5, 0, -armLength-spaceBetweenArms, 0);
    	for (int length = 0; length <= armLengthLower; length++) {
    		limb55.nodes.add(new StructureNode(limb55, 0, -length, 0));
    	}
    	limb5.addChildPiece(limb55);
    	rb = limb55.initPhysicsObject(weightLimbs);
    	rb.setDamping(dampLinear, dampAngular);
    	
    	//legF upper
    	StructurePiece limb6 = new StructurePiece(pieceIndex++, mainBody, -limbOffsetX, 0, limbOffsetZ);
    	for (int length = 0; length <= armLength; length++) {
    		limb6.nodes.add(new StructureNode(limb6, 0, -length, 0));
    	}
    	mainBody.addChildPiece(limb6);
    	rb = limb6.initPhysicsObject(weightLimbs);
    	rb.setDamping(dampLinear, dampAngular);
    	
    	//legF lower
    	StructurePiece limb66 = new StructurePiece(pieceIndex++, limb6, 0, -armLength-spaceBetweenArms, 0);
    	for (int length = 0; length <= armLengthLower; length++) {
    		limb66.nodes.add(new StructureNode(limb66, 0, -length, 0));
    	}
    	limb6.addChildPiece(limb66);
    	rb = limb66.initPhysicsObject(weightLimbs);
    	rb.setDamping(dampLinear, dampAngular);
		
		//everything needed for constraint setup:
		//- bodyA
		//- bodyB
		//- local connection point for piece A
		//- local connection point for piece B
		//- might not need upper and lower angular limits, currently set to 0,0,0 then runtime muscle adjusters take control, everythings initialized downwards y
		//- 
		
		
		
    	
		//limb has issues reaching out to a 90 degree angle with this < 1, gets stuck just past 45 degrees upwards
		//issues with 1 too
    	spaceBetweenArms += 0.5F;
		limbOffsetX += spaceBetweenArms;
		limbOffsetZ += spaceBetweenArms;
		
		//notice: relative coords are inverted between structure piece inits and constraints (bug)
		
		setupConstraint(mainBody, limb1, new Vector3f(limbOffsetX, -1.0F, -limbOffsetZ), new Vector3f());
		setupConstraint(limb1, limb11, new Vector3f(0, -armLength-spaceBetweenArms, 0), new Vector3f());
		
		setupConstraint(mainBody, limb2, new Vector3f(0, -1.0F, -limbOffsetZ), new Vector3f());
		setupConstraint(limb2, limb22, new Vector3f(0, -armLength-spaceBetweenArms, 0), new Vector3f());
		
		setupConstraint(mainBody, limb3, new Vector3f(-limbOffsetX, -1.0F, -limbOffsetZ), new Vector3f());
		setupConstraint(limb3, limb33, new Vector3f(0, -armLength-spaceBetweenArms, 0), new Vector3f());
		
		setupConstraint(mainBody, limb4, new Vector3f(limbOffsetX, -1.0F, limbOffsetZ), new Vector3f());
		setupConstraint(limb4, limb44, new Vector3f(0, -armLength-spaceBetweenArms, 0), new Vector3f());
		
		setupConstraint(mainBody, limb5, new Vector3f(0, -1.0F, limbOffsetZ), new Vector3f());
		setupConstraint(limb5, limb55, new Vector3f(0, -armLength-spaceBetweenArms, 0), new Vector3f());
		
		setupConstraint(mainBody, limb6, new Vector3f(-limbOffsetX, -1.0F, limbOffsetZ), new Vector3f());
		setupConstraint(limb6, limb66, new Vector3f(0, -armLength-spaceBetweenArms, 0), new Vector3f());
		
	}
	
	public void setupConstraint(StructurePiece bodyA, StructurePiece bodyB, Vector3f localPointA, Vector3f localPointB) {
		Vector3f tmp = new Vector3f();
		Generic6DofConstraint joint6DOF;
		Transform localA = new Transform(), localB = new Transform();
		
		localA.setIdentity();
		localB.setIdentity();

		localA.origin.set(localPointA);
		localB.origin.set(localPointB);

		joint6DOF = new Generic6DofConstraint(bodyA.rigidBody, bodyB.rigidBody, localA, localB, false);
		//bodyA.constraint = joint6DOF;
		bodyB.constraint = joint6DOF;
		
		tmp.set(0, 0, 0);
		joint6DOF.setAngularLowerLimit(tmp);
		tmp.set(0, 0, 0);
		joint6DOF.setAngularUpperLimit(tmp);
		
		PhysicsWorld physWorld = BlockMonsters.physMan.getPhysicsWorld(job.ent.worldObj);
		physWorld.dynamicsWorld.addConstraint(joint6DOF, true);
	}
	
	@Override
	public boolean passBackAttackFrom(IStructureUser parPasser, DamageSource par1DamageSource, float par2, Vector3f coordsSource) {
		if (job != null && job.ent != null) {
			//job.ent.attackEntityFrom(par1DamageSource, par2);
			System.out.println("new health: " + job.ent.getHealth());
		}
		return true;
	}
	
	/*public void faceCoord(int x, int y, int z, float f, float f1)
    {
        double d = x+0.5F - ent.posX;
        double d2 = z+0.5F - ent.posZ;
        double d1;
        d1 = y+0.5F - (ent.posY + (double)ent.getEyeHeight());
        
        double d3 = MathHelper.sqrt_double(d * d + d2 * d2);
        float f2 = (float)((Math.atan2(d2, d) * 180D) / 3.1415927410125732D) - 90F;
        float f3 = (float)(-((Math.atan2(d1, d3) * 180D) / 3.1415927410125732D));
        ent.rotationPitch = -updateRotation(ent.rotationPitch, f3, f1);
        ent.rotationYaw = updateRotation(ent.rotationYaw, f2, f);
    }*/
	
	@Override
	public void tickUpdate() {
		super.tickUpdate();
		
		try {

			
			
			//main controls
			boolean activateForwardMomentum = true; //needs move foward
			boolean activateBalence = true;
			boolean activateAimAtPlayer = false;

			boolean activateWalk = false;
			boolean activateMoveFwd = false;
			
			boolean activateTurnLeft = false;
			boolean activateTurnRight = false;
			boolean activateMoveBack = false;
			
			boolean activateHelpJump = false;
			boolean activateHelpBalence = false;
			
			boolean activateGravity = true;
			

			float dampBalence = 0.5F;
			
			float turnDir = 0;
			float fwd = 0F;
			
			
			EntityPlayer entPRider = null;
			if (job.ent.riddenByEntity instanceof EntityPlayer) {
				entPRider = (EntityPlayer) job.ent.riddenByEntity;
				
				Input input = (Input)PlayerData.get(entPRider.username, "input");
				
				if (input.getKeyDown(ControlInputs.moveForward)) {
					activateMoveFwd = true;
				} else if (input.getKeyDown(ControlInputs.moveBackward)) {
					activateMoveBack = true;
				} else if (input.getKeyDown(ControlInputs.moveLeft)) {
					activateTurnLeft = true;
				} else if (input.getKeyDown(ControlInputs.moveRight)) {
					activateTurnRight = true;
				}
				
				if (input.getKeyDown(ControlInputs.moveJump)) {
					if (ticksToJump <= 0) {
						this.ticksToJump = 10;
					}
				}
				
				if (job.ent instanceof BlockMonster1) {
				
					BlockMonster1 bm = (BlockMonster1) job.ent;
					
					if (input.getKeyDown(ControlInputs.eatTrees)) {
						if (!wasKeyDown) {
							bm.eatTrees = !bm.eatTrees;
							entPRider.sendChatToPlayer(new ChatMessageComponent().addText("eat trees: " + bm.eatTrees));
						}
						
						wasKeyDown = true;
					} else if (input.getKeyDown(ControlInputs.moveSpeedUp)) {
						if (!wasKeyDown) {
							bm.walkRate += 0.1F;
							if (bm.walkRate > 0.5F) {
								bm.walkRate = 0.5F;
							}
							entPRider.sendChatToPlayer(new ChatMessageComponent().addText("walk rate: " + bm.walkRate));
						}
						wasKeyDown = true;
					} else if (input.getKeyDown(ControlInputs.moveSpeedDown)) {
						if (!wasKeyDown) {
							bm.walkRate -= 0.1F;
							if (bm.walkRate < 0.1F) {
								bm.walkRate = 0.1F;
							}
							entPRider.sendChatToPlayer(new ChatMessageComponent().addText("walk rate: " + bm.walkRate));
						}
						wasKeyDown = true;
					} else if (input.getKeyDown(ControlInputs.legHeightBase)) {
						if (!wasKeyDown) {
							bm.legHeightBase += 10F;
							if (bm.legHeightBase > -60) {
								bm.legHeightBase = -120;
							}
							entPRider.sendChatToPlayer(new ChatMessageComponent().addText("leg height base: " + bm.legHeightBase));
						}
						wasKeyDown = true;
					} else if (input.getKeyDown(ControlInputs.legHeightStep)) {
						if (!wasKeyDown) {
							bm.legHeightStepRange += 10F;
							if (bm.legHeightStepRange > 65) {
								bm.legHeightStepRange = 25;
							}
							entPRider.sendChatToPlayer(new ChatMessageComponent().addText("leg height range: " + bm.legHeightStepRange));
						}
						wasKeyDown = true;
					} else {
						wasKeyDown = false;
					}
				}
				
			}
			
			activateWalk = activateMoveFwd || activateMoveBack || activateTurnLeft || activateTurnRight;
			activateAimAtPlayer = activateWalk;
			activateForwardMomentum = activateWalk;
			
			if (activateTurnLeft) {
				turnDir = -1;
			} else if (activateTurnRight) {
				turnDir = 1;
			}
			
			if (activateMoveFwd) {
				fwd = 1F;
			} else if (activateMoveBack) {
				fwd = -1F;
			}
			
			float rateAnim = 0.2F * fwd;
			
			float moveForceAbs = 50;
			
			if (ticksToJump > 0) {
				moveForceAbs = 100;
			}

			float balenceRate = 10.6F;
			
			balenceRate = 20F;
			float gravity = -120F;
			
			float rangeYaw = 30;
			float rangePitch = 45;
			float startAngle = -100;
			//tweaking
			rangeYaw = 30;
			rangePitch = 55;
			startAngle = -100;
			
			if (job.ent instanceof BlockMonster1) {
				rateAnim = ((BlockMonster1)job.ent).walkRate * fwd;
				rangePitch = ((BlockMonster1)job.ent).legHeightStepRange;
				startAngle = ((BlockMonster1)job.ent).legHeightBase;
			}
			
			if (activateWalk) {
				if (job.ent.worldObj.getTotalWorldTime() % (int)(8 / (0.001F + (Math.abs(rateAnim)+0.5F))) == 0) {
					job.ent.worldObj.playSoundEffect(job.ent.posX, job.ent.posY, job.ent.posZ, BlockMonsters.modID+":stomp", 300F, 0.7F + (job.ent.worldObj.rand.nextFloat() * 0.3F));
				}
			}
			
			if (ticksToJump > 0) {
				balenceRate = 0;
			}
			
			//body.absX = job.ent.posX;
			//body.absY = job.ent.posY + 7.5D + (float) (Math.sin(System.currentTimeMillis()*0.0005D) * 3.5D);
			//body.absZ = job.ent.posZ;
			
			//body.setScale(1F, true, true);
			
			//body.applyGravity = true;
			
			//body.rotationYawB = 0;//40;
			//body.rotationPitchB = 0;//70;
			//body.rotationRoll = 0;//150;
			
			float speedFix = 3F;
			
			/*if (body.rotationPitchB > 0) body.rotationPitchB-=speedFix;
			if (body.rotationPitchB < 0) body.rotationPitchB+=speedFix;
			if (body.rotationYawB > 0) body.rotationYawB-=speedFix;
			if (body.rotationYawB < 0) body.rotationYawB+=speedFix;
			if (body.rotationRoll > 0) body.rotationRoll-=speedFix;
			if (body.rotationRoll < 0) body.rotationRoll+=speedFix;*/
			
			//body.rotationYawB = job.ent.rotationYawHead - 90;
			
			//not sure if ill want to use this with jbullet, might use its instead?
			for (int i = 0; i < collisionListeners.size(); i++) {
				StructureNode sn = collisionListeners.get(i);
				Vec3 collisionReturnForce = sn.getCollisionDifference();
				
				if (collisionReturnForce.lengthVector() > 0) {
					//System.out.println("collisions!");
					
					StructurePiece sp = (StructurePiece)sn.parent;
					
					/*double vecX = sn.block.posX - (sp.absX+sp.rotationVecCenter.xCoord);
					double vecY = sn.block.posY - (sp.absY+sp.rotationVecCenter.yCoord);
					double vecZ = sn.block.posZ - (sp.absZ+sp.rotationVecCenter.zCoord);
					Vec3 vecDistToCenter = Vec3.createVectorHelper(vecX, vecY, vecZ).normalize();
					sp.applyTorqueForce(collisionReturnForce, vecDistToCenter, 1F);
					
					if (sp.parent instanceof StructurePiece) {
						StructurePiece sp2 = (StructurePiece)sp.parent;
						while (true) {
							vecX = sn.block.posX - (sp2.absX+sp2.rotationVecCenter.xCoord);
							vecY = sn.block.posY - (sp2.absY+sp2.rotationVecCenter.yCoord);
							vecZ = sn.block.posZ - (sp2.absZ+sp2.rotationVecCenter.zCoord);
							vecDistToCenter = Vec3.createVectorHelper(vecX, vecY, vecZ).normalize();
							sp2.applyTorqueForce(collisionReturnForce, vecDistToCenter, 1F);
							
							if (!(sp2.parent instanceof StructurePiece)) {
								sp2.applyThrustForce(collisionReturnForce, vecDistToCenter, 1F);
								break;
							} else {
								
							}
							sp2 = (StructurePiece)sp2.parent;
						}
					}*/
				}
			}
			
			//body.rigidBody.setDamping(0.2F, 0.2F);
			//body.childPieces.get(0).rigidBody.setDamping(0.2F, 0.2F);
			
			//body.rigidBody.setAngularVelocity(new Vector3f(15.5F, 0.5F, 15F));
			
			float what = 5F;
			float what2 = 30F;
			Transform trns2 = new Transform();
			body.rigidBody.getWorldTransform(trns2);
			if (trns2.origin.y < 30) {
				/*body.applyThrustForce(new Vector3f(10F(float)Math.sin(System.currentTimeMillis()*0.0003D) * what2,
						(float) (815F - (trns2.origin.y*2) + ((float)(job.ent.worldObj.getTotalWorldTime() * 0.000003F) * what)),
						(float)7-Math.cos(System.currentTimeMillis()*0.0003D) * what2), new Vector3f(0, 0, 0));*/
				
				/*body.applyThrustForce(new Vector3f((float)Math.sin(job.ent.worldObj.getTotalWorldTime() * 0.03F) * what2,0 ,
						(float)Math.cos(job.ent.worldObj.getTotalWorldTime() * 0.03F) * what2), new Vector3f(0, 0, 0));*/
			
				//body.applyThrustForce(new Vector3f(0, 3200 + 150 - 30 - trns2.origin.y*4/* + (float)Math.sin(job.ent.worldObj.getTotalWorldTime() * 0.09F) * 300F*/, 0), new Vector3f());
				//body.applyThrustForce(new Vector3f(0, 0, 0), new Vector3f());
			}
			
			
			
			for (int i = 0; i < 4; i++) {
				float wat = 0.1F;
				float moveRange = 0.01F;
				float moveRangeOffset = (float) (Math.sin(job.ent.worldObj.getTotalWorldTime() * 0.05F) * BulletGlobals.SIMD_PI/8);
				
				/*Vector3f tmp = new Vector3f();
				tmp.set(0, -BulletGlobals.SIMD_PI * wat, (-BulletGlobals.SIMD_PI * moveRange)+moveRangeOffset);
				((Generic6DofConstraint)body.childPieces.get(i).constraint).setAngularLowerLimit(tmp);
				tmp.set(0, BulletGlobals.SIMD_PI * wat, (BulletGlobals.SIMD_PI * moveRange)+moveRangeOffset);
				((Generic6DofConstraint)body.childPieces.get(i).constraint).setAngularUpperLimit(tmp);*/
				
				
				
				
			}
			
			Random rand = new Random();
			
			//MAIN CONTROLLERS
			
			
			float turnRate = 150;
			
			
			
			
			//off
			//rangeYaw = 0;
			//rangePitch = 0;
			
			float rangeRoll = 0;
			//float startAngle = -100;// + ((float)Math.sin((job.ent.worldObj.getTotalWorldTime()) * 0.002F) * 20F);
			float legSpread2 = 90;
			float endLimbLead = 30;
			
			
			if (!activateWalk) {
				rangeYaw = 0;
				rangePitch = 0;
			}


			
			
			
			//position tracking
			if (ticksToJump <= 0) {
				Transform pos = new Transform();
				body.rigidBody.getWorldTransform(pos);
				if (job.ent.worldObj.getTotalWorldTime() % (20*25) == 0) {
					//compare X Z
					Vec3 posCur = Vec3.createVectorHelper(pos.origin.x, lastPosTracked.y, pos.origin.z);
					Vec3 posPrev = Vec3.createVectorHelper(lastPosTracked.x, lastPosTracked.y, lastPosTracked.z);
					
					//jump, might need active ticking cause of vel max
					if (activateHelpJump && posPrev.distanceTo(posCur) < 7) {
						System.out.println("trigger jump to help");
						ticksToJump = 15;
						
						int warpUp = 10;
						body.rigidBody.translate(new Vector3f(0, warpUp, 0));
						
						for (int i = 0; i < body.childPieces.size(); i++) {
							body.childPieces.get(i).rigidBody.translate(new Vector3f(0, warpUp, 0));
							
							for (int j = 0; j < body.childPieces.get(i).childPieces.size(); j++) {
								body.childPieces.get(i).childPieces.get(j).rigidBody.translate(new Vector3f(0, warpUp, 0));
							}
						}
					}
					
					lastPosTracked = new Vector3f(pos.origin);
				}
			}
			
			if (ticksToJump > 0) {
				ticksToJump--;
				body.rigidBody.applyCentralImpulse(new Vector3f(0, 400, 0));
			}
			
			//balence enabling
			if (ticksToBalence <= 0) {
				Transform pos = new Transform();
				body.rigidBody.getWorldTransform(pos);
				if (job.ent.worldObj.getTotalWorldTime() % (20*10) == 0) {
					//compare X Z
					Vec3 posCur = Vec3.createVectorHelper(pos.origin.x, lastPosTrackedForBalence.y, pos.origin.z);
					Vec3 posPrev = Vec3.createVectorHelper(lastPosTrackedForBalence.x, lastPosTrackedForBalence.y, lastPosTrackedForBalence.z);
					
					//jump, might need active ticking cause of vel max
					if (activateHelpBalence && posPrev.distanceTo(posCur) < 7) {
						System.out.println("trigger balence to help");
						ticksToBalence = 20*10;
					}
					
					lastPosTrackedForBalence = new Vector3f(pos.origin);
				}
			}
			
			if (ticksToBalence > 0) {
				ticksToBalence--;
				balenceRate = 20;
				//moveForceAbs = 0;
				//body.rigidBody.applyCentralImpulse(new Vector3f(0, 400, 0));
			}
			
			
			float moveForce = -moveForceAbs * fwd;
			
			//turn!
			body.rigidBody.applyTorqueImpulse(new Vector3f(0, turnRate * turnDir, 0));
			
			//gravity
			if (activateGravity) {
				//body.applyThrustForce(new Vector3f(0, -2000, 0), new Vector3f(0, 0, 0));
				body.rigidBody.applyCentralImpulse(new Vector3f(0, gravity, 0));
				//body.rigidBody.applyCentralImpulse(new Vector3f(0, -80, 0));
				//body.rigidBody.applyCentralImpulse(new Vector3f(0, -20, 0));
			}
			
			//jump!
			if (job.ent.worldObj.getTotalWorldTime() % 400 < 20) {
				//body.rigidBody.applyCentralImpulse(new Vector3f(0, 400, 0));
			}
			
			//balencing
			Transform trns = new Transform();
			body.rigidBody.getWorldTransform(trns);
			//Vector3f rotation = new Vector3f();
			Quat4f quat = new Quat4f(0, 1, 0, 1);
			//quat.set(trns.basis);
			//trns.getRotation(quat);
			
			Vector3f derp = new Vector3f(1, 0, 0);
			trns.basis.transform(derp);
			Vector3f derp2 = new Vector3f();
			
			float vecX = (float) (derp.x);
	    	float vecY = (float) (trns.origin.y - derp.y);
	    	float vecZ = (float) (derp.z);
	    	derp2.sub(trns.origin, derp);
	    	derp2.normalize();
	        float rotationYaw;// = (float)(Math.atan2(derp2.z, -derp2.x));
	        rotationYaw = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI) - 180F;
	        
	        EntityPlayer entP = job.ent.worldObj.getClosestPlayerToEntity(job.ent, -1);

	        
			
			//System.out.println("derp: " + derp);
			//System.out.println("vecY: " + vecY);
			
	        boolean moveForward = true;
	        

	        /*if (entP.getDistanceToEntity(this.job.ent) > 12) {
	        	moveForward = true;
	        }*/
	        
			if (activateAimAtPlayer) {
		        
		        if (moveForward && entP != null) {
			        vecX = (float) (entP.posX - trns.origin.x);
			    	vecZ = (float) (entP.posZ - trns.origin.z);
			        //float rotationYawTarget = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI) - 180F;
			        
			        //double d1 = vec3.xCoord - this.posX;
	                //double d2 = vec3.zCoord - this.posZ;
	                //double d3 = vec3.yCoord - (double)i;
	                float f2 = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI) - 0.0F;
	                
	                if (job.ent.riddenByEntity != null) {
	                	f2 = job.ent.riddenByEntity.rotationYaw + 90;
	    	        }
	                
	                float rotationYawTarget = MathHelper.wrapAngleTo180_float(f2 - rotationYaw);
			        
			        //System.out.println("rotationYawTarget: " + rotationYawTarget);
			        //System.out.println("rotationYaw: " + rotationYaw);
			        
			        float resultRot = rotationYawTarget;//Math.abs(rotationYawTarget) - Math.abs(rotationYaw);
			        //System.out.println("resultRot: " + resultRot);
			        
			        float force = 400F;
			        float range = 2;
			        
			        //body.rigidBody.applyTorqueImpulse(new Vector3f(0, force, 0));
			        
			        if (resultRot > range) {
			        	//System.out.println("1");
			        	body.rigidBody.applyTorqueImpulse(new Vector3f(0, force, 0));
			        } else if (resultRot < range) {
			        	//System.out.println("2");
			        	body.rigidBody.applyTorqueImpulse(new Vector3f(0, -force, 0));
			        }
		        }
			}
	        
	    	//float dist2 = (float)Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
			//return new Vector3f(vecX / dist2, vecY / dist2, vecZ / dist2);
			
			//this works, how?! we should probably make the quat only get the yaw...
			//has a weird issue when it gets near 230, flips to ~100
			MatrixUtil.getRotation(trns.basis, quat);
			quat.normalize();
			float wat = QuaternionUtil.getAngle(quat);
			//Vector3f rot = new Vector3f(0, 0, 0);
			//trns.basis.getColumn(1, rot);
			
			//wat -= Math.PI/2;
			
			//System.out.println(Math.toDegrees(wat));
			//System.out.println(rotationYaw);
			//System.out.println(derp);
			

			float slowWalk = 0.5F;
			
			rotationYaw -= 90F;
			
			//extra help, directional influence
			if (moveForward) {
				//self uprighting
				
			} else {
				moveForce *= slowWalk;
			}
			
			if (activateBalence) {
				body.rigidBody.applyTorqueImpulse(new Vector3f((float)Math.toDegrees(-quat.x * balenceRate), 0, (float)Math.toDegrees(-quat.z * balenceRate)));
				
				if (dampBalence != 1F) {
					Vector3f angVec = new Vector3f();
					body.rigidBody.getAngularVelocity(angVec);
					body.rigidBody.setAngularVelocity(new Vector3f(angVec.x * dampBalence, angVec.y * dampBalence, angVec.z * dampBalence));
				}
				//body.rigidBody.applyDamping(500F);//applyTorqueImpulse(new Vector3f((float)Math.toDegrees(-quat.x * balenceRate), 0, (float)Math.toDegrees(-quat.z * balenceRate)));
			}
			
			if (!activateWalk || ticksToJump > 0) {
				dampBalence = 0.1F;
				if (dampBalence != 1F) {
					Vector3f angVec = new Vector3f();
					body.rigidBody.getAngularVelocity(angVec);
					body.rigidBody.setAngularVelocity(new Vector3f(angVec.x * dampBalence, angVec.y * dampBalence, angVec.z * dampBalence));
				}
			}
			
			//forward momentum help
			if (activateForwardMomentum) {
				body.rigidBody.applyCentralImpulse(new Vector3f((float)Math.sin(Math.toRadians(-rotationYaw)) * moveForce, 0, (float)Math.cos(Math.toRadians(-rotationYaw)) * moveForce));
			}
			
			
			//System.out.println("quat.x: " + Math.toDegrees(quat.x));
			
			/*body.childPieces.get(0).rigidBody.setActivationState(CollisionObject.ACTIVE_TAG);
			body.childPieces.get(1).rigidBody.setActivationState(CollisionObject.ACTIVE_TAG);
			body.childPieces.get(2).rigidBody.setActivationState(CollisionObject.ACTIVE_TAG);
			body.childPieces.get(3).rigidBody.setActivationState(CollisionObject.ACTIVE_TAG);*/

			float rate = 10.00F;
			//body.applyTorqueForce(new Vector3f(0, 0, rate), new Vector3f(10, 0, 0), 1F);
			
			float ang = 0;
			
			Vector3f vel = new Vector3f();
			body.rigidBody.getLinearVelocity(vel);
			//System.out.println(vel.length());
			
			//60 makes you not fall through, but you get a little stuck into the object - when using sphere collision radius size of 0.8
			float maxSpeed = 35;
			
			//speed cap
			if (vel.length() > maxSpeed) {
				vel.scale(maxSpeed / vel.length());
				body.rigidBody.setLinearVelocity(vel);
				
			}
			
			//body.rigidBody.setAngularVelocity(new Vector3f());
			
			removeAnimationSequence("pivotBody");
			
			if (!hasAnimationSequenceRunning("pivotBody")) {
				SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body, Vec3.createVectorHelper(0, 0, 1F), 0));
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body, Vec3.createVectorHelper(1F, 0, 0), 0));
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body, Vec3.createVectorHelper(0, 1F, 0), 0));
				//addAnimationSequence("pivotBody", seqA);
			}
			
			//if (activateWalk) {
				removeAnimationSequence("pivotThingA");
				removeAnimationSequence("pivotThingB");
				removeAnimationSequence("pivotThingC");
				removeAnimationSequence("pivotThingD");
				removeAnimationSequence("pivotThingE");
				removeAnimationSequence("pivotThingF");
				removeAnimationSequence("pivotThingAA");
				removeAnimationSequence("pivotThingBB");
				removeAnimationSequence("pivotThingCC");
				removeAnimationSequence("pivotThingDD");
				removeAnimationSequence("pivotThingEE");
				removeAnimationSequence("pivotThingFF");
			//}
			
			float offset = 0;
			
			
			
			float offsetATime = 0;
			float offsetBTime = 0;
			
			float ampRate = 0F;
			
			float legSpread = 50+10;//-((float)Math.sin(offsetATime+(job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			//-((float)Math.sin(offsetATime+(job.ent.worldObj.getTotalWorldTime()) * 0.2F) * 30F);//110;
			
			if (job.ent.worldObj.getTotalWorldTime() % 100 > 50) {
				//legSpread2 = 60;//-((float)Math.sin(offsetATime+(job.ent.worldObj.getTotalWorldTime() % 100) * 0.1F) * 60F);
			}
			
			float offsetA = 10;
			float offsetB = -10;
			
			//front back alternating
			/*float amp = 0+((float)Math.sin(Math.PI+(job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			float amp2 = 0+((float)Math.sin((job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			float amp3 = 0+((float)Math.sin(Math.PI+(job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			float amp4 = 0+((float)Math.sin(Math.PI+(job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);*/
			
			//other
			float amp = offsetA+((float)Math.sin(offsetATime+(job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			float amp2 = offsetB+((float)Math.sin(offsetBTime+(job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			float amp3 = offsetA+((float)Math.sin(offsetATime+(job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			float amp4 = offsetB+((float)Math.sin(offsetBTime+(job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			
			if (!moveForward) {
				rateAnim *= slowWalk;
			}
			
			
			//need better way to transition between large adjustments in low and high of constraints
			
			//for spawn fixing
			
			
			//this adjusts angle of grap nav, less causes front to point up, more causes it to point down
			float extraBackLegPitch = 1.0F;
			
			
			//future methods should wait for first tripod to land firmly, before second one lifts
			//sin and cos method causes mirrored crossover, but doesnt help with what we want said above
			
			
			
			float rateA = -((offsetATime+job.ent.worldObj.getTotalWorldTime()) * rateAnim);
			float rateB = -((offsetBTime+job.ent.worldObj.getTotalWorldTime()) * rateAnim) + (float)Math.PI;
			float rateC = -((offsetATime+job.ent.worldObj.getTotalWorldTime()) * rateAnim);
			float rateD = -((offsetBTime+job.ent.worldObj.getTotalWorldTime()) * rateAnim) + (float)Math.PI;
			float rateE = -((offsetATime+job.ent.worldObj.getTotalWorldTime()) * rateAnim);
			float rateF = -((offsetBTime+job.ent.worldObj.getTotalWorldTime()) * rateAnim) + (float)Math.PI;
			
			//temp testing of rigid movement
			//rateA = (float)Math.PI;
			//rateD = (float)Math.PI;
			
			/*if (!moveForward) {
				rateA = rateB = rateC = rateD = rateE = rateF *= 0.02F;//(float) Math.PI / 2F;
			}*/
			
			if (true) {
				if (!hasAnimationSequenceRunning("pivotThingA")) {
					SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(1, 0, 0), startAngle+(float)Math.cos(rateA) * rangePitch));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(0, 1, 0), endLimbLead+(float)Math.sin(rateA) * rangeYaw));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), 0));
					addAnimationSequence("pivotThingA", seqA);
				}
				
				if (!hasAnimationSequenceRunning("pivotThingAA")) {
					SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0).childPieces.get(0), Vec3.createVectorHelper(1, 0, 0), legSpread2));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0).childPieces.get(0), Vec3.createVectorHelper(0, 1, 0), -rangeRoll));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0).childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), 0));
					addAnimationSequence("pivotThingAA", seqA);
				}
				
				if (!hasAnimationSequenceRunning("pivotThingB")) {
					SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1), Vec3.createVectorHelper(1, 0, 0), startAngle+(float)Math.cos(rateB) * rangePitch));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1), Vec3.createVectorHelper(0, 1, 0), (float)Math.sin(rateB) * rangeYaw));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1), Vec3.createVectorHelper(0, 0, 1), 0));
					addAnimationSequence("pivotThingB", seqA);
				}
				
				if (!hasAnimationSequenceRunning("pivotThingBB")) {
					SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1).childPieces.get(0), Vec3.createVectorHelper(1, 0, 0), legSpread2));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1).childPieces.get(0), Vec3.createVectorHelper(0, 1, 0), 0));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1).childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), 0));
					addAnimationSequence("pivotThingBB", seqA);
				}
				
				if (!hasAnimationSequenceRunning("pivotThingC")) {
					SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(2), Vec3.createVectorHelper(1, 0, 0), startAngle+(float)Math.cos(rateC) * rangePitch * extraBackLegPitch));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(2), Vec3.createVectorHelper(0, 1, 0), -endLimbLead+(float)Math.sin(rateC) * rangeYaw));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(2), Vec3.createVectorHelper(0, 0, 1), 0));
					addAnimationSequence("pivotThingC", seqA);
				}
				
				if (!hasAnimationSequenceRunning("pivotThingCC")) {
					SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(2).childPieces.get(0), Vec3.createVectorHelper(1, 0, 0), legSpread2));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(2).childPieces.get(0), Vec3.createVectorHelper(0, 1, 0), rangeRoll));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(2).childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), 0));
					addAnimationSequence("pivotThingCC", seqA);
				}
				
				if (!hasAnimationSequenceRunning("pivotThingD")) {
					SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(3), Vec3.createVectorHelper(1, 0, 0), -startAngle-(float)Math.cos(rateD) * rangePitch));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(3), Vec3.createVectorHelper(0, 1, 0), -endLimbLead-(float)Math.sin(rateD) * rangeYaw));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(3), Vec3.createVectorHelper(0, 0, 1), 0));
					addAnimationSequence("pivotThingD", seqA);
				}
				
				if (!hasAnimationSequenceRunning("pivotThingDD")) {
					SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(3).childPieces.get(0), Vec3.createVectorHelper(1, 0, 0), -legSpread2));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(3).childPieces.get(0), Vec3.createVectorHelper(0, 1, 0), rangeRoll));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(3).childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), 0));
					addAnimationSequence("pivotThingDD", seqA);
				}
				
				if (!hasAnimationSequenceRunning("pivotThingE")) {
					SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(4), Vec3.createVectorHelper(1, 0, 0), -startAngle-(float)Math.cos(rateE) * rangePitch));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(4), Vec3.createVectorHelper(0, 1, 0), -(float)Math.sin(rateE) * rangeYaw));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(4), Vec3.createVectorHelper(0, 0, 1), 0));
					addAnimationSequence("pivotThingE", seqA);
				}
				
				if (!hasAnimationSequenceRunning("pivotThingEE")) {
					SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(4).childPieces.get(0), Vec3.createVectorHelper(1, 0, 0), -legSpread2));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(4).childPieces.get(0), Vec3.createVectorHelper(0, 1, 0), 0));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(4).childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), 0));
					addAnimationSequence("pivotThingEE", seqA);
				}
				
				if (!hasAnimationSequenceRunning("pivotThingF")) {
					SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(5), Vec3.createVectorHelper(1, 0, 0), -startAngle-(float)Math.cos(rateF) * rangePitch * extraBackLegPitch));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(5), Vec3.createVectorHelper(0, 1, 0), endLimbLead-(float)Math.sin(rateF) * rangeYaw));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(5), Vec3.createVectorHelper(0, 0, 1), 0));
					addAnimationSequence("pivotThingF", seqA);
				}
				
				if (!hasAnimationSequenceRunning("pivotThingFF")) {
					SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(5).childPieces.get(0), Vec3.createVectorHelper(1, 0, 0), -legSpread2));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(5).childPieces.get(0), Vec3.createVectorHelper(0, 1, 0), -rangeRoll));
					seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(5).childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), 0));
					addAnimationSequence("pivotThingFF", seqA);
				}
			}
			
			//Random rand = new Random();
			
			body.tick();
			
			
			//carefull with this code
			//if (job.ent.worldObj.getTotalWorldTime() % 100 == 0) {
				Transform trnss = new Transform();
				body.rigidBody.getWorldTransform(trnss);
				job.ent.setPosition(trnss.origin.x, trnss.origin.y + 1.5F, trnss.origin.z);
				//System.out.println("hacking ent pos to block monster - " + trns.origin);
			//}
				
			/*if (body.absX != 0) {
				
			}*/
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public SelectorAnimationSequence performWalkAnimation(StructurePiece parPiece, double parFlip) {
		double speed = 8D;
		SelectorAnimationSequence seq = new SelectorAnimationSequence(null, 1, true);
		
		double angleA = 30;
		double angleB = 110;
		double angleC = 15;
		SelectorAnimationSequence seqA = new SelectorAnimationSequence(seq, 1, true);
		AnimationBend bendA = new AnimationBend(seqA, 0, parPiece, Vec3.createVectorHelper(angleA*parFlip, -angleC*parFlip, 0), Vec3.createVectorHelper(-angleA*parFlip, angleC*parFlip, 0), Vec3.createVectorHelper(-speed*parFlip, speed*parFlip, 0), true);
		AnimationBend bendB = new AnimationBend(seqA, 0, parPiece.childPieces.get(0), Vec3.createVectorHelper(0, -angleC*parFlip, 0), Vec3.createVectorHelper(angleB*parFlip, angleC*parFlip, 0), Vec3.createVectorHelper(speed*2*parFlip, speed*2*parFlip, 0), true);
		seqA.add(bendA);
		seqA.add(bendB);
		seq.add(seqA);
		return seq;
	}

}
