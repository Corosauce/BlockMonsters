package blockmonsters.physics;

import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class PhysicsWorldManager {

	private PhysicsWorld physicsWorld; //in future, convert this to a list of worlds for each server side dimension (if we choose to go that far)
	private PhysicsWorld physicsWorldClient; //physics simulation for clients current dimension, should be reset on client side dim change???
	
	public PhysicsWorldManager() {
		// TODO Auto-generated constructor stub
	}
	
	public void reset() {
		if (physicsWorldClient != null) physicsWorldClient.reset();
		if (physicsWorld != null) physicsWorld.reset();
	}
	
	public void tickServer() {
    	//tick jbullet world only when something initialized it
    	if (physicsWorld != null) physicsWorld.tick(50000f);
	}
	
	public void tickClient() {
    	//tick jbullet world only when something initialized it
    	if (physicsWorldClient != null) physicsWorldClient.tick(50000f);
	}
	
	//single dimension support only - but supports giving different world if client/server for simulating both
	public PhysicsWorld getPhysicsWorld(World world) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			if (physicsWorldClient == null) {
				System.out.println("Initializing JBullet world for client side");
				physicsWorldClient = new PhysicsWorld(world);
				physicsWorldClient.init();
				//read in?
			}
			return physicsWorldClient;
		} else {
			if (physicsWorld == null) {
				System.out.println("Initializing dimension shared JBullet world for server side dimID: " + world.provider.dimensionId);
				physicsWorld = new PhysicsWorld(world);
				physicsWorld.init();
				//read in?
			}
			return physicsWorld;
		}
		
	}
}
