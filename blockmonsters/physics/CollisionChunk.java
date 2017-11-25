package blockmonsters.physics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.vecmath.Vector3f;

import net.minecraft.world.chunk.Chunk;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;

public class CollisionChunk {

	public CollisionChunkManager manager;
	
	public int chunkX;
	public int chunkZ;
	
	public boolean needsUpdate = false;
	
	private ByteBuffer gVertices;
	private ByteBuffer gIndices;
	private BvhTriangleMeshShape trimeshShape;
	private TriangleIndexVertexArray indexVertexArrays;
	private RigidBody groundBody;
	
	public CollisionChunk(CollisionChunkManager parManager, int parChunkX, int parChunkZ) {
		chunkX = parChunkX;
		chunkZ = parChunkZ;
		manager = parManager;
		markNeedsUpdate();
	}
	
	public void init() {
		//?
	}
	
	public void markNeedsUpdate() {
		needsUpdate = true;
	}
	
	public boolean needsUpdate() {
		return needsUpdate;
	}
	
	public void updateCache(Chunk parChunk) {
		cleanup();
		needsUpdate = false;
		
		//System.out.println("updating cache for chunk: " + parChunk.xPosition + " - " + parChunk.zPosition);
		
		Vector3f tmp = new Vector3f();
		
		float TRIANGLE_SIZE=1f;
		int NUM_VERTS_X = 16+1;
		int NUM_VERTS_Y = 16+1;
		
		//NUM_VERTS_X = 10;
		//NUM_VERTS_Y = 10;
		
		int totalVerts = NUM_VERTS_X*NUM_VERTS_Y;
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
						manager.physWorld.worldMC.getHeightValue((chunkX * 16) + i, (chunkZ * 16) + j),
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
		
		startTransform.origin.set((chunkX * 16) + 8.5F, 0F, (chunkZ * 16) + 8.5F);
		
		groundBody = manager.physWorld.localCreateRigidBody(0f, startTransform, groundShape, 1F, 1F);

		groundBody.setCollisionFlags(groundBody.getCollisionFlags() | CollisionFlags.STATIC_OBJECT);

		// enable custom material callback
		groundBody.setCollisionFlags(groundBody.getCollisionFlags() | CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
	}
	
	public void cleanup() {
		if (groundBody != null) manager.physWorld.dynamicsWorld.removeRigidBody(groundBody);
	}
}
