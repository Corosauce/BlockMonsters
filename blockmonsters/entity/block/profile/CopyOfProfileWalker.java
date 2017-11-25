package blockmonsters.entity.block.profile;

import java.util.Random;

import javax.vecmath.Vector3f;

import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;

import blockmonsters.BlockMonsters;
import blockmonsters.entity.ai.LeafBlockMonster;
import blockmonsters.entity.block.IStructureUser;
import blockmonsters.entity.block.StructureNode;
import blockmonsters.entity.block.StructurePiece;
import blockmonsters.entity.block.profile.ai.AnimationBend;
import blockmonsters.entity.block.profile.ai.PhysicsMuscle;
import blockmonsters.entity.block.profile.ai.SelectorAnimationSequence;
import blockmonsters.physics.PhysicsWorld;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysics.linearmath.Transform;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class CopyOfProfileWalker extends StructureProfileBase {

	public CopyOfProfileWalker(LeafBlockMonster parJob) {
		super(parJob);
	}
	
	@Override
	public void populateStructureData() {
		super.populateStructureData();
		
		//you're using box collision on legs, fix
		//also, keep carefull watch on leg orientation to jbullet orientation, relative grid factoring etc
		
		int pieceIndex = 0;
    	StructurePiece mainBody = new StructurePiece(pieceIndex++, (IStructureUser)job, 0, 0, 0);
    	body = mainBody;
    	
    	int sizeX = 12;
    	int sizeY = 3;
    	int sizeZ = 5;
    	
    	int radius = 1+Math.max(sizeX, sizeZ);
    	
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
    	
    	float dampLinear = 0.2F;
    	float dampAngular = 0.2F;
    	
    	mainBody.calculateCenter();
    	//RigidBody rb = mainBody.initPhysicsObject(new CapsuleShape(mainBody.axisYRadius, mainBody.sizeHalf.y*2), new Vector3f((float)job.ent.posX, (float)job.ent.posY, (float)job.ent.posZ), 5F);
    	RigidBody rb = mainBody.initPhysicsObject(new BoxShape(new Vector3f(sizeX, sizeY, sizeZ)), new Vector3f((float)job.ent.posX, (float)job.ent.posY, (float)job.ent.posZ), 1F);
    	rb.setDamping(0, 0);
    	
    	//if (true) return;
    	
    	int armLength = 16;
    	
    	int limbOffsetX = 4;//(sizeX/2)+1;
    	int limbOffsetZ = (sizeZ/2)+1;
    	
    	StructurePiece limb = new StructurePiece(pieceIndex++, mainBody, limbOffsetX, 0, limbOffsetZ);
    	for (int length = 0; length < armLength; length++) {
    		limb.nodes.add(new StructureNode(limb, 0, -length, 0));
    		//limb.nodes.add(new StructureNode(limb, -1, -length, 0));
    		//limb.nodes.add(new StructureNode(limb, 1, -length, 0));
    	}
    	rb = limb.initPhysicsObject(1F);
    	rb.setDamping(dampLinear, dampAngular);
    	mainBody.addChildPiece(limb);
    	
    	StructurePiece limb2 = new StructurePiece(pieceIndex++, mainBody, -limbOffsetX, 0, limbOffsetZ);
    	for (int length = 0; length < armLength; length++) {
    		limb2.nodes.add(new StructureNode(limb2, 0, -length, 0));
    		//limb2.nodes.add(new StructureNode(limb2, -1, -length, 0));
    		//limb2.nodes.add(new StructureNode(limb2, 1, -length, 0));
    	}
    	rb = limb2.initPhysicsObject(1F);
    	rb.setDamping(dampLinear, dampAngular);
    	mainBody.addChildPiece(limb2);
    	
    	StructurePiece limb3 = new StructurePiece(pieceIndex++, mainBody, limbOffsetX, 0, -limbOffsetZ);
    	for (int length = 0; length < armLength; length++) {
    		limb3.nodes.add(new StructureNode(limb3, 0, -length, 0));
    		//limb3.nodes.add(new StructureNode(limb3, -1, -length, 0));
    		//limb3.nodes.add(new StructureNode(limb3, 1, -length, 0));
    	}
    	rb = limb3.initPhysicsObject(1F);
    	rb.setDamping(dampLinear, dampAngular);
    	mainBody.addChildPiece(limb3);
    	
    	StructurePiece limb4 = new StructurePiece(pieceIndex++, mainBody, -limbOffsetX, 0, -limbOffsetZ);
    	for (int length = 0; length < armLength; length++) {
    		limb4.nodes.add(new StructureNode(limb4, 0, -length, 0));
    		//limb4.nodes.add(new StructureNode(limb4, -1, -length, 0));
    		//limb4.nodes.add(new StructureNode(limb4, 1, -length, 0));
    	}
    	rb = limb4.initPhysicsObject(1F);
    	rb.setDamping(dampLinear, dampAngular);
    	mainBody.addChildPiece(limb4);

		PhysicsWorld physWorld = BlockMonsters.physMan.getPhysicsWorld(job.ent.worldObj);
    	
		//limb has issues reaching out to a 90 degree angle with this < 1, gets stuck just past 45 degrees upwards
		//issues with 1 too
		limbOffsetX += 2;
		limbOffsetZ += 2;
		
		//the constraint orientations need rework since capsule usage
		
		
    	if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			
			float scale = 1F;
			
			//doesnt seem to change center of mass or gravity effect (as in joints always have gravity off?)
			boolean disableCollisionsBetweenLinkedBodies = true;
			
			float limiterSide = 1F;
			float limiterMovement = 1F;
			float legOffset2 = 60;
			float legOffset3 = 90;
			legOffset2 = 0;
			legOffset3 = 0;
			
			//setting this to false solved spazzout issues when constraint was constraining
			boolean useLinearReferenceFrameA = false;
			{
				Vector3f tmp = new Vector3f();
		    	Generic6DofConstraint joint6DOF;
				Transform localA = new Transform(), localB = new Transform();
				
				localA.setIdentity();
				localB.setIdentity();

				localB.origin.set(0f, 0F, 0f);
				//MatrixUtil.setEulerZYX(localB.basis, 0, 0, BulletGlobals.SIMD_HALF_PI/2F);
				localA.origin.set(-limbOffsetX, 0.0F, -limbOffsetZ);

				joint6DOF = new Generic6DofConstraint(mainBody.rigidBody, limb.rigidBody, localA, localB, useLinearReferenceFrameA);
				mainBody.constraint = joint6DOF;
				limb.constraint = joint6DOF;
				
				tmp.set(0, 0, 0);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(0, 0, 0);
				joint6DOF.setAngularUpperLimit(tmp);
				
				physWorld.dynamicsWorld.addConstraint(joint6DOF, disableCollisionsBetweenLinkedBodies);
			}
			
			{
				Vector3f tmp = new Vector3f();
		    	Generic6DofConstraint joint6DOF;
				Transform localA = new Transform(), localB = new Transform();
				
				localA.setIdentity();
				localB.setIdentity();

				localB.origin.set(0f, 0F, 0f);
				localA.origin.set(limbOffsetX, 0, -limbOffsetZ);

				joint6DOF = new Generic6DofConstraint(mainBody.rigidBody, limb2.rigidBody, localA, localB, useLinearReferenceFrameA);
				mainBody.constraint = joint6DOF;
				limb2.constraint = joint6DOF;
				
				tmp.set(0, 0, 0);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(0, 0, 0);
				joint6DOF.setAngularUpperLimit(tmp);
				
				physWorld.dynamicsWorld.addConstraint(joint6DOF, disableCollisionsBetweenLinkedBodies);
			}
			
			{
				Vector3f tmp = new Vector3f();
		    	Generic6DofConstraint joint6DOF;
				Transform localA = new Transform(), localB = new Transform();
				
				localA.setIdentity();
				localB.setIdentity();

				localB.origin.set(0f, 0F, 0f);
				localA.origin.set(-limbOffsetX, 0, limbOffsetZ);

				joint6DOF = new Generic6DofConstraint(mainBody.rigidBody, limb3.rigidBody, localA, localB, useLinearReferenceFrameA);
				mainBody.constraint = joint6DOF;
				limb3.constraint = joint6DOF;
				
				tmp.set(0, 0, 0);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(0, 0, 0);
				joint6DOF.setAngularUpperLimit(tmp);
				
				physWorld.dynamicsWorld.addConstraint(joint6DOF, disableCollisionsBetweenLinkedBodies);
			}
			
			{
				Vector3f tmp = new Vector3f();
		    	Generic6DofConstraint joint6DOF;
				Transform localA = new Transform(), localB = new Transform();
				
				localA.setIdentity();
				localB.setIdentity();

				localB.origin.set(0f, 0F, 0f);
				localA.origin.set(limbOffsetX, 0, limbOffsetZ);

				joint6DOF = new Generic6DofConstraint(mainBody.rigidBody, limb4.rigidBody, localA, localB, useLinearReferenceFrameA);
				mainBody.constraint = joint6DOF;
				limb4.constraint = joint6DOF;
				
				tmp.set(0, 0, 0);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(0, 0, 0);
				joint6DOF.setAngularUpperLimit(tmp);
				
				physWorld.dynamicsWorld.addConstraint(joint6DOF, disableCollisionsBetweenLinkedBodies);
			}
    	}
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
				
				
				
				body.applyThrustForce(new Vector3f(0, -200, 0), new Vector3f(0, 0, 0));
			}
			
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
			float maxSpeed = 55;
			
			if (vel.length() > maxSpeed) {
				vel.scale(maxSpeed / vel.length());
				body.rigidBody.setLinearVelocity(vel);
			}
			
			removeAnimationSequence("pivotBody");
			
			if (!hasAnimationSequenceRunning("pivotBody")) {
				SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body, Vec3.createVectorHelper(0, 0, 1F), 0));
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body, Vec3.createVectorHelper(1F, 0, 0), 0));
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body, Vec3.createVectorHelper(0, 1F, 0), 0));
				//addAnimationSequence("pivotBody", seqA);
			}
			
			
			removeAnimationSequence("pivotThing");
			removeAnimationSequence("pivotThingB");
			removeAnimationSequence("pivotThingC");
			removeAnimationSequence("pivotThingD");
			
			float offset = 30;
			float rateAnim = 0.08F;
			float ampRate = 60F;
			float legSpread = 40;
			//front back alternating
			/*float amp = 0+((float)Math.sin(Math.PI+(job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			float amp2 = 0+((float)Math.sin((job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			float amp3 = 0+((float)Math.sin(Math.PI+(job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			float amp4 = 0+((float)Math.sin(Math.PI+(job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);*/
			
			//other
			float amp = 0+((float)Math.sin((job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			float amp2 = 0+((float)Math.sin((job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			float amp3 = 0+((float)Math.sin((job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			float amp4 = 0+((float)Math.sin(/*Math.PI+*/(job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			
			//amp3 = 0+((float)Math.sin((job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			//amp4 = 0+((float)Math.sin((job.ent.worldObj.getTotalWorldTime()) * rateAnim) * ampRate);
			
			//the bad way to currently enforce angle, fix constraints!
			if (!hasAnimationSequenceRunning("pivotThing")) {
				SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), -offset + amp * 2F));
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(1, 0, 0), -amp2));
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(1, 0, 0), 0));
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(0, 1, 0), 0));
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), offset+amp2));
				seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(1, 0, 0), -legSpread));
				seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(0, 1, 0), 0));
				
				
				
				seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), offset+amp));
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), 90));
				addAnimationSequence("pivotThing", seqA);
			}
			
			if (!hasAnimationSequenceRunning("pivotThingB")) {
				SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
				seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1), Vec3.createVectorHelper(0, 0, 1), offset+amp2));
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), 90));
				seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1), Vec3.createVectorHelper(1, 0, 0), -legSpread));
				seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1), Vec3.createVectorHelper(0, 1, 0), 0));
				addAnimationSequence("pivotThingB", seqA);
			}
			
			if (!hasAnimationSequenceRunning("pivotThingC")) {
				SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
				seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(2), Vec3.createVectorHelper(1, 0, 0), legSpread));
				seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(2), Vec3.createVectorHelper(0, 0, 1), offset+amp3));
				seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(2), Vec3.createVectorHelper(0, 1, 0), 0));
				addAnimationSequence("pivotThingC", seqA);
			}
			
			if (!hasAnimationSequenceRunning("pivotThingD")) {
				SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
				seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(3), Vec3.createVectorHelper(1, 0, 0), legSpread));
				seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(3), Vec3.createVectorHelper(0, 0, 1), offset+amp4));
				seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(3), Vec3.createVectorHelper(0, 1, 0), 0));
				addAnimationSequence("pivotThingD", seqA);
			}
			
			
			
			if (!hasAnimationSequenceRunning("pivotThingB")) {
				SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1), Vec3.createVectorHelper(0, 0, 1), -45+00));
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1), Vec3.createVectorHelper(1, 0, 0), -90));
				//seqA.add(new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1), Vec3.createVectorHelper(0, 1, 0), -90));
				//addAnimationSequence("pivotThingB", seqA);
			}
			
			//atm this 'animation' never technically ends, sequence set to unlimited and no true isFinished for PhysicsMuscle
			/*if (!hasAnimationSequenceRunning("bendA")) {
				
				body.childPieces.get(0).applyGravity = false;
				body.childPieces.get(0).childPieces.get(0).applyGravity = false;
				body.childPieces.get(0).childPieces.get(0).childPieces.get(0).applyGravity = false;
				body.childPieces.get(0).childPieces.get(0).childPieces.get(0).childPieces.get(0).applyGravity = false;
				
				SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
				PhysicsMuscle bendA = new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), ang);
				PhysicsMuscle bendB = new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0).childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), ang);
				PhysicsMuscle bendC = new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0).childPieces.get(0).childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), ang);
				PhysicsMuscle bendD = new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(0).childPieces.get(0).childPieces.get(0).childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), ang);
				seqA.add(bendA);
				seqA.add(bendB);
				seqA.add(bendC);
				seqA.add(bendD);
				addAnimationSequence("bendA", seqA);
			}
			
			if (!hasAnimationSequenceRunning("bendB")) {
				
				SelectorAnimationSequence seqA = new SelectorAnimationSequence(null, -1, true);
				PhysicsMuscle bendA = new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1), Vec3.createVectorHelper(0, 0, 1), -ang);
				PhysicsMuscle bendB = new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1).childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), -ang);
				PhysicsMuscle bendC = new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1).childPieces.get(0).childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), -ang);
				PhysicsMuscle bendD = new PhysicsMuscle(job.ent.worldObj, seqA, body.childPieces.get(1).childPieces.get(0).childPieces.get(0).childPieces.get(0), Vec3.createVectorHelper(0, 0, 1), -ang);
				seqA.add(bendA);
				seqA.add(bendB);
				seqA.add(bendC);
				seqA.add(bendD);
				addAnimationSequence("bendB", seqA);
			}*/
			
			
			
			
			/*body.childPieces.get(0).applyGravity = false;
			body.childPieces.get(0).childPieces.get(0).applyGravity = false;
			body.childPieces.get(0).childPieces.get(0).childPieces.get(0).applyGravity = false;
			body.childPieces.get(0).childPieces.get(0).childPieces.get(0).childPieces.get(0).applyGravity = false;*/
			
			//removeAnimationSequence("bendA");
			
			Random rand = new Random();
			
			body.tick();
			
			
			//carefull with this code
			//if (job.ent.worldObj.getTotalWorldTime() % 100 == 0) {
				Transform trns = new Transform();
				body.rigidBody.getWorldTransform(trns);
				job.ent.setPosition(trns.origin.x, trns.origin.y + 20, trns.origin.z);
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
