package blockmonsters.physics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import javax.vecmath.Vector3f;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import blockmonsters.entity.block.debug.DebugOutput;

import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DebugDrawModes;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

public class PhysicsWorld {

	public World worldMC;
	public DynamicsWorld dynamicsWorld = null;
	//public ObjectArrayList<CollisionShape> collisionShapes = new ObjectArrayList<CollisionShape>(); //unneeded, can access internal dynamicsWorlds list
	
	private static final int ARRAY_SIZE_X = 5;
	private static final int ARRAY_SIZE_Y = 5;
	private static final int ARRAY_SIZE_Z = 5;

	// maximum number of objects (and allow user to shoot additional boxes)
	private static final int MAX_PROXIES = (ARRAY_SIZE_X*ARRAY_SIZE_Y*ARRAY_SIZE_Z + 1024);

	private static final int START_POS_X = -5;
	private static final int START_POS_Y = -5;
	private static final int START_POS_Z = -3;
	
	private ByteBuffer gVertices;
	private ByteBuffer gIndices;
	private BvhTriangleMeshShape trimeshShape;
	private TriangleIndexVertexArray indexVertexArrays;
	private RigidBody groundBody;
	
	public DebugOutput debugger = new DebugOutput();

	public CollisionChunkManager chunkManager;
	
	public PhysicsWorld(World parWorld) {
		worldMC = parWorld;
	}
	
	public void init() {
		initPhysicsToMC();
		clientResetScene();
		reset();
	}
	
	public void reset() {
		chunkManager = new CollisionChunkManager(this);
	}
	
	public static void main(String[] args) {
		PhysicsWorld wat = new PhysicsWorld(null);
		wat.initPhysics();
		
		//inaccurate stepping, meh
		int tickCount = 0;
		while (true) {
			float msDelay = 50;
			wat.tick(msDelay);
			System.out.println("tickCount: " + tickCount++ + " - BulletStats.gNumGjkChecks: " + BulletStats.gNumGjkChecks);
			try {
				Thread.sleep((long) msDelay);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void initPhysicsToMC() {
		DefaultCollisionConfiguration collision_config = new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher = new CollisionDispatcher(collision_config);
		Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
		Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
		BroadphaseInterface overlappingPairCache = new DbvtBroadphase();
		ConstraintSolver constraintSolver = new SequentialImpulseConstraintSolver();
		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache, constraintSolver, collision_config);
		//dynamicsWorld.setGravity(new Vector3f(0f, -20f, 0f));
		dynamicsWorld.setGravity(new Vector3f(0f, 0f, 0f));
		dynamicsWorld.setDebugDrawer(debugger);
		debugger.setDebugMode(DebugDrawModes.MAX_DEBUG_DRAW_MODE);
		
		//temp ground collision
		/*float groundLevel = 4.5F;
		float groundSize = 4000;
		CollisionShape groundShape = new BoxShape(new Vector3f(groundSize, 20f+groundLevel, groundSize));
		Transform groundTransform = new Transform();
		groundTransform.setIdentity();
		groundTransform.origin.set(-groundSize/2, -20f, -groundSize/2);
		localCreateRigidBody(0f, groundTransform, groundShape);*/
		
		//ground 2
		//int i;
		//int j;
		
		
		//createGround();
	}
	
	public void createGround() {

		//TEMP HACK!!!
		//worldMC = DimensionManager.getWorld(0);
		
		if (groundBody != null) {
			dynamicsWorld.removeRigidBody(groundBody);
			groundBody = null;
		}
		
		Vector3f tmp = new Vector3f();
		
		float TRIANGLE_SIZE=1f;
		int NUM_VERTS_X = 200;
		int NUM_VERTS_Y = 200;
		
		//NUM_VERTS_X = 10;
		//NUM_VERTS_Y = 10;
		
		int totalVerts = NUM_VERTS_X*NUM_VERTS_Y;
		float waveheight = 1F;
		float offset = 0F;
		
		
		
		//3 = dimensions
		//4 = bytes in float
		
		int vertStride = 3 * 4;
		int indexStride = 3 * 4;

		int totalTriangles = 2 * (NUM_VERTS_X - 1) * (NUM_VERTS_Y - 1);

		gVertices = ByteBuffer.allocateDirect(totalVerts * 3 * 4).order(ByteOrder.nativeOrder());
		gIndices = ByteBuffer.allocateDirect(totalTriangles * 3 * 4).order(ByteOrder.nativeOrder());

		/*for (int i = 0; i < NUM_VERTS_X; i++) {
			for (int j = 0; j < NUM_VERTS_Y; j++) {
				tmp.set(
						(i - NUM_VERTS_X * 0.5f) * TRIANGLE_SIZE,
						//0.f,
						waveheight * (float) Math.sin((float) i + offset) * (float) Math.cos((float) j + offset),
						(j - NUM_VERTS_Y * 0.5f) * TRIANGLE_SIZE);

				int index = i + j * NUM_VERTS_X;
				gVertices.putFloat((index*3 + 0) * 4, tmp.x);
				gVertices.putFloat((index*3 + 1) * 4, tmp.y);
				gVertices.putFloat((index*3 + 2) * 4, tmp.z);
			}
		}*/
		
		for (int i = 0; i < NUM_VERTS_X; i++) {
			for (int j = 0; j < NUM_VERTS_Y; j++) {
				//System.out.println(i + " - " + j + ": " + worldMC.getHeightValue(i, j));
				tmp.set(
						(i - NUM_VERTS_X * 0.5f) * TRIANGLE_SIZE,
						worldMC.getHeightValue(i-NUM_VERTS_X/2, j-NUM_VERTS_Y/2),
						//waveheight * (float) Math.sin((float) i + offset) * (float) Math.cos((float) j + offset),
						(j - NUM_VERTS_Y * 0.5f) * TRIANGLE_SIZE);

				int index = i + j * NUM_VERTS_X;
				gVertices.putFloat((index*3 + 0) * 4, tmp.x);
				gVertices.putFloat((index*3 + 1) * 4, tmp.y);
				gVertices.putFloat((index*3 + 2) * 4, tmp.z);
			}
		}
		
		gIndices.clear();
		for (int i = 0; i < NUM_VERTS_X - 1; i++) {
			for (int j = 0; j < NUM_VERTS_Y - 1; j++) {
				gIndices.putInt(j * NUM_VERTS_X + i);
				gIndices.putInt(j * NUM_VERTS_X + i + 1);
				gIndices.putInt((j + 1) * NUM_VERTS_X + i + 1);

				gIndices.putInt(j * NUM_VERTS_X + i);
				gIndices.putInt((j + 1) * NUM_VERTS_X + i + 1);
				gIndices.putInt((j + 1) * NUM_VERTS_X + i);
			}
		}
		gIndices.flip();

		indexVertexArrays = new TriangleIndexVertexArray(totalTriangles,
				gIndices,
				indexStride,
				totalVerts, gVertices, vertStride);

		boolean useQuantizedAabbCompression = true;
		
		trimeshShape = new BvhTriangleMeshShape(indexVertexArrays, useQuantizedAabbCompression);
		//collisionShapes.add(trimeshShape);

		CollisionShape groundShape = trimeshShape;
		
		Transform startTransform = new Transform();
		startTransform.setIdentity();
		startTransform.origin.set(-0.0F, 0F, -0.0F);
		groundBody = localCreateRigidBody(0f, startTransform, groundShape);

		groundBody.setCollisionFlags(groundBody.getCollisionFlags() | CollisionFlags.STATIC_OBJECT);

		// enable custom material callback
		groundBody.setCollisionFlags(groundBody.getCollisionFlags() | CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
	}
	
	public RigidBody addRigidBody(CollisionShape shape, /*Vector3f sizeHalfs, */Vector3f spawnPos, float parMass) {
		CollisionShape colShape = shape;//new BoxShape(sizeHalfs);
		Transform startTransform = new Transform();
		startTransform.setIdentity();
		startTransform.origin.set(spawnPos);
		
		// rigidbody is dynamic if and only if mass is non zero, otherwise static
		boolean isDynamic = (parMass != 0f);

		Vector3f localInertia = new Vector3f(0, 0, 0);
		if (isDynamic) {
			colShape.calculateLocalInertia(parMass, localInertia);
		}
		
		DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
		RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(parMass, myMotionState, colShape, localInertia);

		//for smoother ragdool constraint stuff
		//rbInfo.additionalDamping = true;
		
		//keep these to 0.1 to keep block monsters from "falling asleep"
		rbInfo.angularSleepingThreshold = 0.1F;
		rbInfo.linearSleepingThreshold = 0.1F;
		
		//test
		rbInfo.restitution = 0.0F;
		rbInfo.friction = 1F;
		
		RigidBody body = new RigidBody(rbInfo);
		body.setActivationState(RigidBody.ISLAND_SLEEPING);
		
		dynamicsWorld.addRigidBody(body);
		
		return body;
	}
	
	public void initPhysics() {
		
		DefaultCollisionConfiguration collision_config = new DefaultCollisionConfiguration();

		CollisionDispatcher dispatcher = new CollisionDispatcher(collision_config);
		
		Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
		Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
		BroadphaseInterface overlappingPairCache = new DbvtBroadphase();
		
		ConstraintSolver constraintSolver = new SequentialImpulseConstraintSolver();
		
		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache, constraintSolver, collision_config);

		dynamicsWorld.setGravity(new Vector3f(0f, -30f, 0f));

		//my debugger
		dynamicsWorld.setDebugDrawer(debugger);
		debugger.setDebugMode(DebugDrawModes.MAX_DEBUG_DRAW_MODE);
		
		float groundLevel = 3F;
		
		//Ground
		CollisionShape groundShape = new BoxShape(new Vector3f(200f, 15f+groundLevel, 200f));
		Transform groundTransform = new Transform();
		groundTransform.setIdentity();
		groundTransform.origin.set(0f, -15f, 0f);
		localCreateRigidBody(0f, groundTransform, groundShape);
		
		float sizeBox = 1F;
		
		//example object inits
		CollisionShape colShape = new BoxShape(new Vector3f(sizeBox/2F, sizeBox/2F, sizeBox/2F));
		//CollisionShape colShape = new SphereShape(1f);

		// Create Dynamic Objects
		Transform startTransform = new Transform();
		startTransform.setIdentity();

		float mass = 1f;

		// rigidbody is dynamic if and only if mass is non zero, otherwise static
		boolean isDynamic = (mass != 0f);

		Vector3f localInertia = new Vector3f(0, 0, 0);
		if (isDynamic) {
			colShape.calculateLocalInertia(mass, localInertia);
		}

		Random rand = new Random();
		
		float start_x = START_POS_X - ARRAY_SIZE_X / 2;
		float start_y = /*START_POS_Y + */groundLevel;
		float start_z = START_POS_Z - ARRAY_SIZE_Z / 2;

		for (int k = 0; k < ARRAY_SIZE_Y * 10; k++) {
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					startTransform.origin.set(
							1f * i,
							0.2f + k + start_y,
							1f * j);

					// using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
					DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
					RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, colShape, localInertia);
					RigidBody body = new RigidBody(rbInfo);
					body.setActivationState(RigidBody.ISLAND_SLEEPING);

					dynamicsWorld.addRigidBody(body);
					//body.setActivationState(RigidBody.ISLAND_SLEEPING);
				}
			}
		}
		
		clientResetScene();
	}
	
	public void tick(float partialTicksMS) {
		//System.out.println(dynamicsWorld);
		// simple dynamics world doesn't handle fixed-time-stepping
		//mc game tick rate, 50ms a tick, 20 ticks a second
		float ms = partialTicksMS * 1000000F;//50000;//getDeltaTimeMicroseconds();
		/*float minFPS = 1000000f / 60f;
		if (ms > minFPS) {
			ms = minFPS;
		}*/

		chunkManager.tick();
		
		if (dynamicsWorld != null) {
			dynamicsWorld.stepSimulation(ms / 1000000.f);
			// optional but useful: debug drawing
			dynamicsWorld.debugDrawWorld();
		}
		
		
	}
	
	public RigidBody localCreateRigidBody(float mass, Transform startTransform, CollisionShape shape) {
		return localCreateRigidBody(mass, startTransform, shape, 0.5F, 0.9F);
	}
	
	public RigidBody localCreateRigidBody(float mass, Transform startTransform, CollisionShape shape, float parRestitution, float parFriction) {
		// rigidbody is dynamic if and only if mass is non zero, otherwise static
		boolean isDynamic = (mass != 0f);

		Vector3f localInertia = new Vector3f(0f, 0f, 0f);
		if (isDynamic) {
			shape.calculateLocalInertia(mass, localInertia);
		}

		// using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
		DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
		
		RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(mass, myMotionState, shape, localInertia);
		
		cInfo.restitution = parRestitution;
		cInfo.friction = parFriction;
		
		RigidBody body = new RigidBody(cInfo);
		
		dynamicsWorld.addRigidBody(body);

		return body;
	}
	
	public DynamicsWorld getDynamicsWorld() {
		return dynamicsWorld;
	}
	
	public void clientResetScene() {
		//#ifdef SHOW_NUM_DEEP_PENETRATIONS
		BulletStats.gNumDeepPenetrationChecks = 0;
		BulletStats.gNumGjkChecks = 0;
		//#endif //SHOW_NUM_DEEP_PENETRATIONS

		//perform 1 tick for some reason, to get active objects i guess
		int numObjects = 0;
		if (dynamicsWorld != null) {
			dynamicsWorld.stepSimulation(1f / 20f, 0);
			numObjects = dynamicsWorld.getNumCollisionObjects();
		}

		for (int i = 0; i < numObjects; i++) {
			CollisionObject colObj = dynamicsWorld.getCollisionObjectArray().getQuick(i);
			
			initObject(colObj);
			/*
			//quickly search some issue at a certain simulation frame, pressing space to reset
			int fixed=18;
			for (int i=0;i<fixed;i++)
			{
			getDynamicsWorld()->stepSimulation(1./60.f,1);
			}
			*/
		}
	}
	
	public void initObject(CollisionObject parObj) {
		//RigidBody body = RigidBody.upcast(parObj);
		
		RigidBody body = RigidBody.upcast(parObj);
		if (body != null) {
			if (body.getMotionState() != null) {
				DefaultMotionState myMotionState = (DefaultMotionState) body.getMotionState();
				myMotionState.graphicsWorldTrans.set(myMotionState.startWorldTrans);
				parObj.setWorldTransform(myMotionState.graphicsWorldTrans);
				parObj.setInterpolationWorldTransform(myMotionState.startWorldTrans);
				parObj.activate();
			}
			// removed cached contact points
			dynamicsWorld.getBroadphase().getOverlappingPairCache().cleanProxyFromPairs(parObj.getBroadphaseHandle(), getDynamicsWorld().getDispatcher());

			body = RigidBody.upcast(parObj);
			if (body != null && !body.isStaticObject()) {
				RigidBody.upcast(parObj).setLinearVelocity(new Vector3f(0f, 0f, 0f));
				RigidBody.upcast(parObj).setAngularVelocity(new Vector3f(0f, 0f, 0f));
			}
		}
	}
}
