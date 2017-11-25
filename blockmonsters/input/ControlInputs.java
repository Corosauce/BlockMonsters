package blockmonsters.input;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.Configuration;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import blockmonsters.BMPacketHandler;

import CoroUtil.util.CoroUtilFile;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ControlInputs {

	//need support for using mc keys for some, like movement, dont need to double bind those!
	//but since still we dont have that data on server side, its just a client extra watch code, that sends our command ids out
	//so we still need to register command ids, but ignore the bound keys in our system
	//support added!
	//WARNING! REWIRE KEYS MUST BE ADDED LAST! SO GUI CAN EASILY SKIP OVER THEM AND NOT SCREW UP INDEX!
	
	//Saving
	public static File saveFilePath;
	public static Configuration preInitConfig;
	
	//mappings
	public static List<KeyBindingBM> keyBindings = new ArrayList<KeyBindingBM>();
	public static List<String> keyBindingNames = new ArrayList<String>();
	public static HashMap<String, KeyBindingBM> lookupNameToKeyBind = new HashMap<String, KeyBindingBM>();
	
	//runtime data
	public static List<Boolean> keyDownState = new ArrayList<Boolean>();
	public static HashMap<String, Boolean> commandDownStateClient = new HashMap<String, Boolean>();
	//public static HashMap<String, Boolean> commandDownStateServer = new HashMap<String, Boolean>();
	
	//pseudo keys for simply registering an ID to send to server
	//public static int placeHolderKey = 0;
	public static String moveForward = "moveForward";
	public static String moveBackward = "moveBackward";
	public static String moveLeft = "moveLeft";
	public static String moveRight = "moveRight";
	public static String moveJump = "moveJump";
	
	//real mod keys
	public static String moveSpeedUp = "Walk Speed Increase";
	public static String moveSpeedDown = "Walk Speed Decrease";
	public static String legHeightBase = "Base Leg Height Toggle";
	public static String legHeightStep = "Leg Step Range Toggle";
	public static String eatTrees = "Eat Trees Toggle";
	public static String playerPhysics = "Player Physics Toggle";

	public static void initDefaults() {
		keyBindings.clear();
		keyDownState.clear();
		keyBindingNames.clear();
		commandDownStateClient.clear();
		
		//server needs string array, client needs that and the client Keys set, separate these 2 so server can just get the strings for its own stuff
		//KeyBinding class is bad to use, has internal hashmap overriding vanilla system bindings when i add mine
		//still need to split this logic up, 
		
		addBinding(moveSpeedUp, Keyboard.KEY_R);
		addBinding(moveSpeedDown, Keyboard.KEY_F);
		addBinding(legHeightBase, Keyboard.KEY_Z);
		addBinding(legHeightStep, Keyboard.KEY_X);
		addBinding(eatTrees, Keyboard.KEY_C);
		addBinding(playerPhysics, Keyboard.KEY_V);
		
		//MUST BE LAST FOR GUI
		addBindingRewire(moveForward, "key.forward");
		addBindingRewire(moveBackward, "key.back");
		addBindingRewire(moveLeft, "key.left");
		addBindingRewire(moveRight, "key.right");
		addBindingRewire(moveJump, "key.jump");
	}
	
	public static void initDefaultsForServer() {
		//mirror code above, strip lwjgl
		
		
		addBinding(moveSpeedUp);
		addBinding(moveSpeedDown);
		addBinding(legHeightBase);
		addBinding(legHeightStep);
		addBinding(eatTrees);
		addBinding(playerPhysics);
		
		//MUST BE LAST FOR GUI
		addBinding(moveForward);
		addBinding(moveBackward);
		addBinding(moveLeft);
		addBinding(moveRight);
		addBinding(moveJump);
	}
	
	public static void nbtLoad(FMLPreInitializationEvent event) {
    	initDefaults();
		saveFilePath = new File(CoroUtilFile.getSaveFolderPath() + "config" + File.separator + "BlockMonsters" + File.separator + "controls.cfg");
		nbtSaveAndReupdateFields(false);
	}
	
	public static void nbtSaveAndReupdateFields(boolean resetConfig) {
		if (saveFilePath == null) {
			System.out.println("Error saving Controls, called before initialized");
			return;
		}
		if (resetConfig && saveFilePath.exists()) saveFilePath.delete();
    	preInitConfig = new Configuration(saveFilePath);
    	preInitConfig.load();
    	
    	for (int i = 0; i < keyBindings.size(); i++) {
    		KeyBindingBM kb = keyBindings.get(i);
    		//kb.keyCode
    		keyBindings.get(i).keyCode = preInitConfig.get("Config", keyBindingNames.get(i), kb.keyCode).getInt();
    	}
    	
    	preInitConfig.save();
	}
	
	public static HashMap<String, Boolean> getNewCommandStateHashMap() {
		HashMap<String, Boolean> commandDownStateServer = new HashMap<String, Boolean>();
		
		for (int i = 0; i < keyBindingNames.size(); i++) {
			commandDownStateServer.put(keyBindingNames.get(i), false);
		}
		
		return commandDownStateServer;
	}
	
	//main client input checking method
	public static boolean getKeyDown(String commandName) {
		Boolean boolVal = commandDownStateClient.get(commandName);
		if (boolVal == null) {
			return false;
		} else {
			return boolVal;
		}
		
	}
	
	public static void addBinding(String name) {
		keyDownState.add(false);
		keyBindingNames.add(name);
		commandDownStateClient.put(name, false);
	}
	
	public static void addBinding(String name, int keyCode) {
		addBinding(name);
		KeyBindingBM kb = new KeyBindingBM(name, keyCode);
		keyBindings.add(kb);
		//needs to respect config...
		lookupNameToKeyBind.put(name, kb);
	}
	
	public static void addBindingRewire(String name, String vanillaKeyDesc) {
		addBinding(name);
		KeyBindingBM kb = new KeyBindingBM(name, vanillaKeyDesc);
		keyBindings.add(kb);
		//needs to respect config...
		lookupNameToKeyBind.put(name, kb);
	}
	
	public static String getKeyCode(String name) {
		return getKeyDisplayString(lookupNameToKeyBind.get(name).keyCode);
	}
	
	@SideOnly(Side.CLIENT)
	public static String getKeyName(int index) {
		return getKeyDisplayString(keyBindings.get(index).keyCode);
	}
	
	public static String getKeyDescription(int index) {
		return keyBindings.get(index).keyDescription;
	}
	
	public static void setKey(int index, int keyCode) {
		keyBindings.get(index).keyCode = keyCode;
		nbtSaveAndReupdateFields(true);
	}
	
	@SideOnly(Side.CLIENT)
	public static String getKeyDisplayString(int par0)
    {
        return par0 < 0 ? I18n.getStringParams("key.mouseButton", new Object[] {Integer.valueOf(par0 + 101)}): Keyboard.getKeyName(par0);
    }
	
	@SideOnly(Side.CLIENT)
	public static void tickUpdate(boolean tickEnd) {
		for (int i = 0; i < keyBindings.size(); i++) {
			KeyBindingBM keyBinding = keyBindings.get(i);
            int keyCode = keyBinding.keyCode;
            
            if (keyBinding.isRewireKey) {
            	keyCode = ((KeyBinding)KeyBinding.keybindArray.get(keyBinding.vanillaKeyIndex)).keyCode;
            	//System.out.println("rewire runtime: " + keyBinding.vanillaKeyIndex);
            }
            
            boolean state = (keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode));
            
            if (state)
            {
            	keyState(i, true, state != keyDownState.get(i));
            }
            else
            {
            	keyState(i, false, state != keyDownState.get(i));
            }
		}
	}
	
	public static void keyState(int index, boolean keyDown, boolean stateChange) {
		if (keyDown) {
			keyDown(index);
		} else {
			keyUp(index);
		}
		
		if (stateChange) {
			PacketDispatcher.sendPacketToServer(BMPacketHandler.createPacketForInputCommand(Minecraft.getMinecraft().thePlayer.username, index, keyDown));
		}
	}
	
	public static void keyDown(int index) {
		keyDownState.set(index, true);
		commandDownStateClient.put(keyBindingNames.get(index), true);
	}
	
	public static void keyUp(int index) {
		keyDownState.set(index, false);
		commandDownStateClient.put(keyBindingNames.get(index), false);
	}
}
