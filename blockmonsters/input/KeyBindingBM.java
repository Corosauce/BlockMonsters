package blockmonsters.input;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.IntHashMap;

public class KeyBindingBM
{
	//DONT REFACTOR ME TO COROUTIL UNLESS YOU MOVE LIST TO ELSEWHERE!!!
	
	//Based off of vanilla KeyBinding
	//- removed client side only restriction
	//- using of this class prevents us from clashing with the static list and hashmap
	
    public static List keybindArray = new ArrayList();
    public static IntHashMap hash = new IntHashMap();
    public String keyDescription;
    public int keyCode;

    public boolean pressed;
    public int pressTime;
    
    //support for rewiring keyCode to a vanilla key, overall goal is so we can use our client key -> server command system to support vanilla keys without having to double bind the keys
    //(which would be a pain for the end user if they change vanilla keys at runtime)
    public boolean isRewireKey = false;
    public int vanillaKeyIndex = -1;
    //public String rewireDesc = ""; //will contain things found in GameSettings like 'key.forward', actually lets just provide that in constructor, and find its index (amount of vanilla keys cant change at runtime)

    public static void onTick(int par0)
    {
        KeyBindingBM keybinding = (KeyBindingBM)hash.lookup(par0);

        if (keybinding != null)
        {
            ++keybinding.pressTime;
        }
    }

    public static void setKeyBindState(int par0, boolean par1)
    {
        KeyBindingBM keybinding = (KeyBindingBM)hash.lookup(par0);

        if (keybinding != null)
        {
            keybinding.pressed = par1;
        }
    }

    public static void unPressAllKeys()
    {
        Iterator iterator = keybindArray.iterator();

        while (iterator.hasNext())
        {
            KeyBindingBM keybinding = (KeyBindingBM)iterator.next();
            keybinding.unpressKey();
        }
    }

    public static void resetKeyBindingArrayAndHash()
    {
        hash.clearMap();
        Iterator iterator = keybindArray.iterator();

        while (iterator.hasNext())
        {
            KeyBindingBM keybinding = (KeyBindingBM)iterator.next();
            hash.addKey(keybinding.keyCode, keybinding);
        }
    }

    public KeyBindingBM(String par1Str, String vanillaKeyRewire)
    {
        this.keyDescription = par1Str;
        this.keyCode = -1; //not used for rewire, if it ends up having to be used, it must be maintained and updated in some magic way when vanilla config changes at runtime
        this.isRewireKey = true;
        
        int i = 0;
        
        for (i = 0; i < KeyBinding.keybindArray.size(); i++) {
        	KeyBinding kb = (KeyBinding) KeyBinding.keybindArray.get(i);
        	
        	if (kb.keyDescription.equals(vanillaKeyRewire)) {
        		vanillaKeyIndex = i;
        		break;
        	}
        }
        
        if (i >= KeyBinding.keybindArray.size()) {
        	System.out.println("COROUTIL KEYBINDINGMOD CRITICAL ERROR! Could not find vanilla key " + vanillaKeyRewire + " to rewire to");
        }
        
        keybindArray.add(this);
        //hash.addKey(par2, this);
    }
    
    public KeyBindingBM(String par1Str, int par2)
    {
        this.keyDescription = par1Str;
        this.keyCode = par2;
        keybindArray.add(this);
        hash.addKey(par2, this);
    }

    public boolean isPressed()
    {
        if (this.pressTime == 0)
        {
            return false;
        }
        else
        {
            --this.pressTime;
            return true;
        }
    }

    private void unpressKey()
    {
        this.pressTime = 0;
        this.pressed = false;
    }
}
