package blockmonsters.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;

public class CollisionChunkManager {

	public PhysicsWorld physWorld;
	
	public List<CollisionChunk> listChunks = new ArrayList<CollisionChunk>();
	public HashMap<Long, CollisionChunk> lookupChunks = new HashMap<Long, CollisionChunk>();
	
	public List<Entity> listChunkloaders = new ArrayList<Entity>();
	
	public int chunkLoadRadius = 1; //chunk coords, radius of 1 should load chunk entity is in + 1 layer of surrounding chunks
	public int chunkUnloadRadius = 4;
	public int updateRate = 10;
	public int cleanRate = 80;
	
	public CollisionChunkManager(PhysicsWorld parPhysWorld) {
		physWorld = parPhysWorld;
	}
	
	public void addChunkloader(Entity parEnt) {
		listChunkloaders.add(parEnt);
	}
	
	public void removeChunkloader(Entity parEnt) {
		listChunkloaders.remove(parEnt);
	}
	
	public void addChunk(CollisionChunk parChunk) {
		listChunks.add(parChunk);
		lookupChunks.put(ChunkCoordIntPair.chunkXZ2Int(parChunk.chunkX, parChunk.chunkZ), parChunk);
	}
	
	public void removeChunk(CollisionChunk parChunk) {
		listChunks.remove(parChunk);
		lookupChunks.remove(ChunkCoordIntPair.chunkXZ2Int(parChunk.chunkX, parChunk.chunkZ));
		parChunk.cleanup();
	}
	
	//radius unimplemented
	public void markChunksNeedsUpdate(ChunkCoordinates coords, int radius) {
		Chunk chunk = physWorld.worldMC.getChunkFromChunkCoords(coords.posX / 16, coords.posZ / 16);
		long hash = ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition);
		
		//System.out.println("x: " + chunk.xPosition + " - z: " + chunk.zPosition);
		
		//System.out.println("this: " + this);
		
		CollisionChunk colChunk;
		if (lookupChunks.containsKey(hash)) {
			colChunk = lookupChunks.get(hash);
			
			//System.out.println("marking phys chunk for update");
			
			//testing showed that it was like the old collision still existed, lets remove the chunk instead!
			//testing again, issue stopped showing up.......
			//colChunk.markNeedsUpdate();
			//removeChunk(colChunk);
			//addChunk(colChunk);
			colChunk.markNeedsUpdate();
		} else {
			System.out.println("couldnt find phys chunk to update");
		}
	}
	
	public void tick() {
		
		if (physWorld.worldMC == null) return;
		
		//this shouldnt be high or done like this at all, high speed objects might beat the update rate and fall outside of world
		//find a good performance balence
		if (physWorld.worldMC.getWorldTime() % updateRate == 0) {
			//System.out.println("tickUpdateCache()");
			tickUpdateCache();
		}
		
		if (physWorld.worldMC.getWorldTime() % cleanRate == 0) {
			//System.out.println(FMLCommonHandler.instance().getEffectiveSide() + " - " + physWorld.worldMC.provider.dimensionId + " tickCleanupOldChunks()");
			tickCleanupOldChunks();
		}
	}
	
	public void tickUpdateCache() {
		
		//updateRate = 10;
		
		for (int i = 0; i < listChunkloaders.size(); i++) {
			Entity ent = listChunkloaders.get(i);
			if (ent.isDead) {
				removeChunkloader(ent);
				i--;
			} else {
				
				//System.out.println("this: " + this);
				
				//int chunkX = ent.chunkCoordX;
				
				//should i just tick each ent and prevent overlap with lookupChunks, or should i do what SpawnerAnimals does and gets a list of chunks to load first then a second iteration over those chunks?
				//could use EnteringChunk event to assist this so it cant get ahead of it (server side only assumed)		
						
				for (int x = ent.chunkCoordX-chunkLoadRadius; x <= ent.chunkCoordX+chunkLoadRadius; x++) {
					for (int z = ent.chunkCoordZ-chunkLoadRadius; z <= ent.chunkCoordZ+chunkLoadRadius; z++) {
						Chunk chunk = physWorld.worldMC.getChunkFromChunkCoords(x, z);
						long hash = ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition);
						
						CollisionChunk colChunk;
						if (lookupChunks.containsKey(hash)) {
							colChunk = lookupChunks.get(hash);
						} else {
							colChunk = new CollisionChunk(this, x, z);
							addChunk(colChunk);
							//System.out.println("adding chunk: " + chunk.xPosition + " - " + chunk.zPosition);
							//System.out.println("adding chunk: " + colChunk.chunkX + " - " + colChunk.chunkZ);
						}
						
						if (colChunk.needsUpdate()) {
							//System.out.println("updating chunk: " + chunk.xPosition + " - " + chunk.zPosition);
							//System.out.println("updating chunk: " + colChunk.chunkX + " - " + colChunk.chunkZ);
							colChunk.updateCache(chunk);
						}
					}
					
				}
			}
		}
	}
	
	public void tickCleanupOldChunks() {
		for (int i = 0; i < listChunks.size(); i++) {
			CollisionChunk chunk = listChunks.get(i);
			
			boolean shouldRemove = true;
			
			//break out of this loop if any found in range
			for (int j = 0; j < listChunkloaders.size(); j++) {
				Entity ent = listChunkloaders.get(j);
				if (ent.getDistance(chunk.chunkX*16, ent.posY, chunk.chunkZ*16) < chunkUnloadRadius*16) {
					shouldRemove = false;
					break;
				}
			}
			
			if (shouldRemove) {
				removeChunk(chunk);

				//System.out.println("removing distant chunk: " + chunk.chunkX + " - " + chunk.chunkZ);
			}
		}
	}
	
	public void reset() {
		listChunkloaders.clear();
		listChunks.clear();
	}
	
}
