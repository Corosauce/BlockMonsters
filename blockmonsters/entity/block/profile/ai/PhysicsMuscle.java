package blockmonsters.entity.block.profile.ai;

import java.util.Random;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;

import blockmonsters.entity.block.StructurePiece;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysics.dynamics.constraintsolver.RotationalLimitMotor;
import com.bulletphysics.linearmath.Transform;

public class PhysicsMuscle extends LeafPhysics {

	//probably going to use jbullet rotational motors(constraints)
	//if not, check out Generic6DofConstraint.getCalculatedTransformA and getCalculatedTransformB
	//those should be local transforms for each ... orientation...point?
	
	//needs to only use local rotation, not world transform translation
	//axis aligned issues exist atm
	
	//all angles and rotational forces are defined as degrees here
	
	//Has 2 modes
	//1. maintain angle muscle with tenseness increase as forces push it away from desired angle more
	//2. muscle force in a direction
	
	public World world;
	
	public Vec3 axisAngle; //used in quat for, ie 0, 1, 0 for yaw, at first used to mark what axis it will rotate on, later used in quaternions or converted to one
	public float angleCurrent; //might become a quat
	
	public boolean muscleUseModeActive = false;
	
	//resting angle fields
	public float angleTensingRate = 0.3F; //used to measure how much the joint should fight back as its pulled away from resting angle, might need to become a method/algo for more dynamic response from dist etc
	public float angleDesired; //resting angle for maintain mode
	public float angleDesiredDiffTolerance = 5; //resting angle diff tollerance, in degrees
	public float baseMuscleForce = 3;
	
	//muscle forcing fields
	public float muscleForce; //also direction of rotation, as in can be negative
	
	
	
	
	public PhysicsMuscle(World parWorld, Behavior parParent, StructurePiece parPiece, Vec3 parAxis, float parRestingAngle) {
		super(parParent, parPiece);
		angleDesired = parRestingAngle;
		angleCurrent = angleDesired;
		axisAngle = parAxis;
		world = parWorld;
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		Random rand = new Random();
		
		float snapToMinimum = 2;
		
		//temp debug
		baseMuscleForce = 3F/* + rand.nextFloat()*0.1F*/;
		//angleDesired = 0;// + (float) (Math.sin(System.currentTimeMillis()*0.0003D) * 90D);
		angleTensingRate = 0.0F;//0.03F + rand.nextFloat()*0.05F;
		angleDesiredDiffTolerance = 0;//+(float) (Math.sin(System.currentTimeMillis()*0.001D) * 20D);
		
		//now that its quats, i guess calculate the angle now along our respective axis to check, atan2 for all!
		
		//parentless piece uses the world for its alignment axis i guess
		//or more accurately, it uses a classic yaw for the initial offset if the AI chooses to use a yaw for aiming at stuff
		
		//System.out.println(Math.toDegrees(rootPiece.rot.y));
		
		/*Quat4f rot = new Quat4f();
		QuaternionUtil.setRotation(rot, cameraUp, razi);*/
		
		Transform trns = new Transform();
		rootPiece.rigidBody.getWorldTransform(trns);
		Quat4f quat = new Quat4f();
		quat.set(trns.basis);
		//trns.basis.set(quat);
		//System.out.println(quat);
		
		//temp weird vec to euler rotation code until proper quat usage
		float pieceAngleCurrent = 0;
		if (axisAngle.xCoord == 1) {
			pieceAngleCurrent = (float) Math.toDegrees(quat.x);//this.rootPiece.rotationRoll;
		} else if (axisAngle.yCoord == 1) {
			pieceAngleCurrent = (float) Math.toDegrees(quat.y);//this.rootPiece.rotationYawB;
		} else if (axisAngle.zCoord == 1) {
			pieceAngleCurrent = (float) Math.toDegrees(quat.z);
		}
		
		float finalForce = 0F;
		
		muscleUseModeActive = true;
		
		if (muscleUseModeActive) {
			if (world != null && rootPiece != null && rootPiece.constraint != null) {
				
				//Transform transA = new Transform();
				//((Generic6DofConstraint)rootPiece.constraint).getCalculatedTransformA(transA);
				//Quat4f quat2 = new Quat4f();
				//quat2.set(transA.basis);
				//transA.basis.set(quat2);
				
				RotationalLimitMotor motorX = ((Generic6DofConstraint)rootPiece.constraint).getRotationalLimitMotor(0);
				RotationalLimitMotor motorY = ((Generic6DofConstraint)rootPiece.constraint).getRotationalLimitMotor(1);
				RotationalLimitMotor motorZ = ((Generic6DofConstraint)rootPiece.constraint).getRotationalLimitMotor(2);
				
				float test = 0F;
				
				//class defaults
				float bounce = 0F;
				float limitSoftness = 0.5F;
				float damping = 1F;
				float maxLimitForce = 300F;
				
				bounce = 0F; //shouldnt want any bounce.... we'll see
				limitSoftness = 1F;
				damping = 1f; //changing the motor damping doesnt seem to benefit stability, use motor force
				maxLimitForce = 900F;
				
				motorX.targetVelocity = test;
				motorY.targetVelocity = test;
				motorZ.targetVelocity = test;
				
				motorX.maxMotorForce = 100F;
				motorY.maxMotorForce = 100F;
				motorZ.maxMotorForce = 100F;
				
				motorX.maxLimitForce = maxLimitForce;
				motorY.maxLimitForce = maxLimitForce;
				motorZ.maxLimitForce = maxLimitForce;
				
				motorX.enableMotor = true;
				motorY.enableMotor = true;
				motorZ.enableMotor = true;
				
				motorX.damping = damping;
				motorY.damping = damping;
				motorZ.damping = damping;
				
				motorX.bounce = bounce;
				motorX.limitSoftness = limitSoftness;
				motorY.bounce = bounce;
				motorY.limitSoftness = limitSoftness;
				motorZ.bounce = bounce;
				motorZ.limitSoftness = limitSoftness;
				
				float wat = 0.1F;
				float moveRange = 0.01F;
				float moveRangeOffset = (float) (Math.sin(world.getTotalWorldTime() * 0.05F) * BulletGlobals.SIMD_PI/8);
				
				Vector3f lower = new Vector3f();
				lower.set(0, -BulletGlobals.SIMD_PI * wat, (-BulletGlobals.SIMD_PI * moveRange)+moveRangeOffset);
				Vector3f upper = new Vector3f();
				upper.set(0, BulletGlobals.SIMD_PI * wat, (BulletGlobals.SIMD_PI * moveRange)+moveRangeOffset);
				
				float newLimitLow = (float)(Math.toRadians(angleDesired) - Math.toRadians(angleDesiredDiffTolerance / 2));
				float newLimitHigh = (float)(Math.toRadians(angleDesired) + Math.toRadians(angleDesiredDiffTolerance / 2));
				if (axisAngle.xCoord >= 1) {
					lower.set(newLimitLow, motorY.loLimit, motorZ.loLimit);
					upper.set(newLimitHigh, motorY.hiLimit, motorZ.hiLimit);
				} else if (axisAngle.yCoord >= 1) {
					lower.set(motorX.loLimit, newLimitLow, motorZ.loLimit);
					upper.set(motorX.hiLimit, newLimitHigh, motorZ.hiLimit);
				} else if (axisAngle.zCoord >= 1) {
					lower.set(motorX.loLimit, motorY.loLimit, newLimitLow);
					upper.set(motorX.hiLimit, motorY.hiLimit, newLimitHigh);
				}
				//temp disable v2 motor method for testing
				((Generic6DofConstraint)rootPiece.constraint).setAngularLowerLimit(lower);
				((Generic6DofConstraint)rootPiece.constraint).setAngularUpperLimit(upper);
			}
		} else {
			float vecAngle = angleDesired - pieceAngleCurrent;
			float dist = (float) Math.sqrt(vecAngle * vecAngle);

			//snapping needs parent rotation fix
			
			boolean snapLock = true;
			if (snapLock && dist < snapToMinimum) {
				Vector3f angVel = new Vector3f();
				rootPiece.rigidBody.getAngularVelocity(angVel);
				Transform trns2 = new Transform(trns);
				
				if (axisAngle.xCoord >= 1) {
					rootPiece.rigidBody.setAngularVelocity(new Vector3f(0, angVel.y, angVel.z));
					quat.x = (float) Math.toRadians(angleDesired);
				} else if (axisAngle.yCoord >= 1) {
					rootPiece.rigidBody.setAngularVelocity(new Vector3f(angVel.x, 0, angVel.z));
					quat.y = (float) Math.toRadians(angleDesired);
				} else if (axisAngle.zCoord >= 1) {
					quat.z = (float) Math.toRadians(angleDesired);
					rootPiece.rigidBody.setAngularVelocity(new Vector3f(angVel.x, angVel.y, 0));
				}
				//rootPiece.rigidBody.applyTorqueImpulse(new Vector3f(0, 0, 0));
				
				trns2.setRotation(quat);
				rootPiece.rigidBody.setWorldTransform(trns2);
				
				
				rootPiece.rigidBody.setActivationState(CollisionObject.ACTIVE_TAG);
			} else if (dist > angleDesiredDiffTolerance) {
				finalForce = ((baseMuscleForce + (angleTensingRate * dist)) * (vecAngle / dist));
				
				//System.out.println("resisting, angleDesired: " + angleDesired + ", dist: " + dist);
				rootPiece.rigidBody.setActivationState(CollisionObject.ACTIVE_TAG);
				
				rootPiece.rigidBody.applyTorqueImpulse(new Vector3f((float)(finalForce * axisAngle.xCoord), (float)(finalForce * axisAngle.yCoord), (float)(finalForce * axisAngle.zCoord)));
			} else {
				
			}
		}
		
		//finalForce *= 0.95F;
		
		//temp weird vec to euler rotation code until proper quat usage
		/*this.rootPiece.velRotation.xCoord += finalForce * axisAngle.xCoord;
		this.rootPiece.velRotation.yCoord += finalForce * axisAngle.yCoord;
		this.rootPiece.velRotation.zCoord += finalForce * axisAngle.zCoord;*/

		/*this.rootPiece.velRotation.xCoord *= 0.99F;
		this.rootPiece.velRotation.yCoord *= 0.99F;
		this.rootPiece.velRotation.zCoord *= 0.99F;*/
		
		//rootPiece.rigidBody.clearForces();
		
		
		
		return super.tick();
	}
	
	@Override
	public boolean isAnimationComplete() {
		return false;
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		super.reset();
	}
	
	public void setMuscleUseModeActive(boolean muscleUseModeActive) {
		this.muscleUseModeActive = muscleUseModeActive;
	}

}
