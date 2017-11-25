package blockmonsters;

import java.util.EnumSet;

import javax.vecmath.Vector3f;

import modconfig.gui.GuiConfigEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;

import CoroUtil.playerdata.IPlayerData;
import blockmonsters.client.gui.GuiControlsPages;
import blockmonsters.input.ControlInputs;
import blockmonsters.physics.PhysicsWorld;
import blockmonsters.playerdata.PlayerData;
import blockmonsters.playerdata.objects.Input;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ClientTickHandler implements ITickHandler
{
	public static Minecraft mc = null;
	public static World worldRef = null;
	public static EntityPlayer player = null;
	public static World lastWorld;
    
    public static int timeout;
    public static String msg;
    public static int color;
    public static int defaultColor = 16777215;
    public static boolean ingui;
    
    public RigidBody rigidBodyClient = null;
    
    public static boolean playerPhysics = false;
    public static boolean playerPhysicsLastTickOn = false;
	
    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
    	if (type.equals(EnumSet.of(TickType.PLAYER))) {
        	EntityPlayer entP = Minecraft.getMinecraft().thePlayer;
        	
        }
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (type.equals(EnumSet.of(TickType.RENDER)))
        {
            onRenderTick();
        }
        else if (type.equals(EnumSet.of(TickType.CLIENT)))
        {
            GuiScreen guiscreen = Minecraft.getMinecraft().currentScreen;

            if (guiscreen != null)
            {
                onTickInGUI(guiscreen);
            }
            else
            {
            	ControlInputs.tickUpdate(true);
            }
            
            onTickInGame();
        }
    }

	@Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.RENDER, TickType.CLIENT, TickType.PLAYER);
        // In my testing only RENDER, CLIENT, & PLAYER did anything on the client side.
        // Read 'cpw.mods.fml.common.TickType.java' for a full list and description of available types
    }

    @Override
    public String getLabel()
    {
        return null;
    }

    public void onRenderTick()
    {
    	
    	if (mc == null) mc = FMLClientHandler.instance().getClient();
    	if (worldRef == null) worldRef = mc.theWorld;
        if (player == null) player = mc.thePlayer;
        
        if (worldRef == null || player == null) {
            return;
        }
        
        //super crappy temp gui open code
        if (Keyboard.isKeyDown(Keyboard.KEY_SUBTRACT)) {
        	if (!(mc.currentScreen instanceof GuiConfigEditor)) mc.displayGuiScreen(new GuiConfigEditor());
        }
        
        if(timeout > 0 && msg != null) {
            ScaledResolution var8 = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
            int var4 = var8.getScaledWidth();
            int var10 = var8.getScaledHeight();
            int var6 = mc.fontRenderer.getStringWidth(msg);
            mc.fontRenderer.drawStringWithShadow(msg, 3, 105, 16777215);
            
        }
        
    }

    public void onTickInGUI(GuiScreen guiscreen)
    {
        //TODO: Your Code Here
    }
    
    public void onTickInGame() {
		// TODO Auto-generated method stub
		

        if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }
        
        if (timeout > 0) --timeout;
        
        boolean paused = mc.isSingleplayer() && mc.currentScreen != null && mc.currentScreen.doesGuiPauseGame() && !mc.getIntegratedServer().getPublic();
        
        //this code is done this way because...
        
        //this activates the physics world right away for client side:
        
        
        if (mc.ingameGUI.getChatGUI().getSentMessages().size() > 0) {
            String msg = (String) mc.ingameGUI.getChatGUI().getSentMessages().get(mc.ingameGUI.getChatGUI().getSentMessages().size()-1);
            
            if (msg.equals("/bm config")) {
            	mc.ingameGUI.getChatGUI().getSentMessages().remove(mc.ingameGUI.getChatGUI().getSentMessages().size()-1);
            	mc.displayGuiScreen(new GuiControlsPages(null));
            }
        }
        
        //this line is here to activate the PlayerData system, apparently it needs a request first
        Input input = (Input)PlayerData.get(mc.thePlayer.username, "input");
        
        //boolean clientPhysics = true;
        
        if (playerPhysics && mc.thePlayer.ridingEntity == null) {
        	jBulletTest(mc.thePlayer);
        	
        	PhysicsWorld phys = BlockMonsters.instance.physMan.getPhysicsWorld(mc.theWorld);
            
            //this only ticks the world if above line is not commented out
            if (!paused) {
            	//BlockMonsters.instance.physMan.tickClient();//
            	phys.tick(50000f);
            }
        } else {
        	playerPhysicsLastTickOn = false;
        }
        
        tickIPlayerData();
	}
    
    public void tickIPlayerData() {
        int len = PlayerData.i().tickList.size();
        for(int x=0; x < len; x++) {
        	IPlayerData ipd = (IPlayerData) PlayerData.i().tickList.get(x);
        	ipd.tick();
        }
    }
    
    public static void displayMessage(String var0, int var1) {
        msg = var0;
        timeout = 100;
        color = var1;
    }

    public static void displayMessage(String var0) {
        displayMessage(var0, defaultColor);
    }
    
    public void jBulletTest(EntityPlayer entP) {
		// = getPlayerObjectClient();
		Minecraft mc = Minecraft.getMinecraft();
		boolean paused = mc.isSingleplayer() && mc.currentScreen != null && mc.currentScreen.doesGuiPauseGame() && !mc.getIntegratedServer().getPublic();
		
		if (!paused) {
		
			//System.out.println("player motionX: " + entP.motionX);
			//doesnt work for cancelling
			/*entP.motionX /= 0.98F;
			entP.motionY /= 0.98F;
			entP.motionZ /= 0.98F;*/
			
			PhysicsWorld physWorld = BlockMonsters.physMan.getPhysicsWorld(entP.worldObj);
			
			if (rigidBodyClient == null) {
				
				
				//radius wraps around all sides
				//CapsuleShape co = new CapsuleShape(0.4F, 0.8F);
				SphereShape co = new SphereShape(0.8F);
				co = new SphereShape(2.5F);
				RigidBody rb = physWorld.addRigidBody(co, new Vector3f((float)entP.posX, 90, (float)entP.posZ), 1F);
				rb.setDamping(0.0F, 0.0F);
				physWorld.initObject(rb);
				rigidBodyClient = rb;
			} else {
				Transform t = new Transform();
				rigidBodyClient.getWorldTransform(t);

				//TEMP!!!
				//physWorld.createGround();
				
				//WIP chunkloader adding
				if (!physWorld.chunkManager.listChunkloaders.contains(entP)) {
					physWorld.chunkManager.addChunkloader(entP);
				}
				
				//test stuff
				//rigidBodyClient.setGravity(new Vector3f(0, 0, 0));
				rigidBodyClient.setGravity(new Vector3f(0, -150, 0));
				rigidBodyClient.setDamping(0.1F, 0.1F);
				rigidBodyClient.setRestitution(0.1F);
				rigidBodyClient.setFriction(0.3F);
				
				//really test stuff
				//rigidBodyClient.setCollisionShape(new SphereShape(2.5F));
				
				float moveForce = 150F;
				
				//normalize them when multiple are pressed!!!
				if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.keyCode)) {
					rigidBodyClient.applyCentralForce(new Vector3f((float)Math.sin(Math.toRadians(-entP.rotationYaw)) * moveForce, 0, (float)Math.cos(Math.toRadians(-entP.rotationYaw)) * moveForce));
				}
				if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.keyCode)) {
					rigidBodyClient.applyCentralForce(new Vector3f((float)Math.sin(Math.toRadians(-entP.rotationYaw)) * -moveForce, 0, (float)Math.cos(Math.toRadians(-entP.rotationYaw)) * -moveForce));
				}
				if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.keyCode)) {
					rigidBodyClient.applyCentralForce(new Vector3f((float)-Math.cos(Math.toRadians(-entP.rotationYaw)) * -moveForce, 0, (float)Math.sin(Math.toRadians(-entP.rotationYaw)) * -moveForce));
				}
				if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.keyCode)) {
					rigidBodyClient.applyCentralForce(new Vector3f((float)-Math.cos(Math.toRadians(-entP.rotationYaw)) * moveForce, 0, (float)Math.sin(Math.toRadians(-entP.rotationYaw)) * moveForce));
				}
				
				
				
				//collision
				int tryX = MathHelper.floor_double(entP.posX);
				int tryY = MathHelper.floor_double(entP.boundingBox.minY);
				int tryYGrav = MathHelper.floor_double(entP.boundingBox.minY-0.3);
				int tryZ = MathHelper.floor_double(entP.posZ);
				int id = mc.theWorld.getBlockId(tryX, tryYGrav, tryZ);
				int id2 = mc.theWorld.getBlockId(tryX, tryYGrav+1, tryZ);
				
				//System.out.println(mc.theWorld.getHeightValue(tryX, tryZ));
				
				//clipping through things at high speed fix, since jbullet never got its continuous dynamics world
				Vector3f vel = new Vector3f();
				rigidBodyClient.getLinearVelocity(vel);
				//System.out.println(vel.length());
				
				//60 makes you not fall through, but you get a little stuck into the object - when using sphere collision radius size of 0.8
				float maxSpeed = 55;
				maxSpeed = 100;
				
				if (vel.length() > maxSpeed) {
					vel.scale(maxSpeed / vel.length());
					rigidBodyClient.setLinearVelocity(vel);
				}
				
				if (id != 0) {
					//System.out.println("collide!");
					/*Vector3f vel2 = new Vector3f();
					rigidBodyClient.getLinearVelocity(vel2);
					rigidBodyClient.setLinearVelocity(new Vector3f(vel2.x*0.8F, 0, vel2.z*0.8F));*/
					//rigidBodyClient.applyCentralForce(new Vector3f(0, 50, 0));
					if (id2 == 0) {
						//t.origin.y = tryY+2;
					}
				} else {
					//rigidBodyClient.applyCentralForce(new Vector3f(0, -50, 0));
					//System.out.println("!!!!");
				}
				
				if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.keyCode) || (mc.gameSettings.keyBindJump.isPressed())) {
					//System.out.println("sdfsdfsdf");
					rigidBodyClient.applyCentralForce(new Vector3f(0, 250, 0));
				} else {
					//rigidBodyClient.clearForces();
				}
				
				if (t.origin.y < 0) {
					rigidBodyClient.applyCentralForce(new Vector3f(0, 135 - (t.origin.y), 0));
					//rigidBodyClient.applyCentralForce(new Vector3f(0, 50, 0));
				} else {
					//rigidBodyClient.applyCentralForce(new Vector3f(0, -50, 0));
				}
				//t.basis.setIdentity();
				
				boolean lockToPlayerInstead = !playerPhysicsLastTickOn;
				
				//temp reset
				if (lockToPlayerInstead) {
					t.origin.x = (float)entP.posX;
					t.origin.y = (float)entP.posY;
					t.origin.z = (float)entP.posZ;
					rigidBodyClient.setLinearVelocity(new Vector3f());
					rigidBodyClient.setWorldTransform(t);
				}
				
				//MatrixUtil.setEulerZYX(t.basis, 0, (float)Math.toRadians(-entP.rotationYaw), (float)Math.toRadians(0/*-entP.rotationPitch*/));
				//
				//
				rigidBodyClient.setActivationState(CollisionObject.ACTIVE_TAG);
				
				
				
				/*entP.posX = t.origin.x;
				entP.posY = t.origin.y;
				entP.posZ = t.origin.z;*/
				//rigidBodyClient.getWorldTransform(t);
				
				//entP.prevCameraPitch = entP.rotationPitch;
				entP.fallDistance = 0;
				entP.motionY = 0;
				
				
				if (!lockToPlayerInstead) entP.setPosition(t.origin.x+0.5F, t.origin.y+1.5, t.origin.z+0.5F);
				//if (!lockToPlayerInstead) entP.setPositionAndRotation(t.origin.x+0.5F, t.origin.y+1.5, t.origin.z+0.5F, 0, 0);
				
				playerPhysicsLastTickOn = true;
			}
		}
	}
}
