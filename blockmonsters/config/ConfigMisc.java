package blockmonsters.config;

import java.io.File;

import modconfig.IConfigCategory;


public class ConfigMisc implements IConfigCategory {

	//cleanup once GUI plan takes form
	
	//misc
	public static int blockMonsterID = 45482;
	public static int structureBlockID = 45483;

	public ConfigMisc() {
		
	}
	
	@Override
	public String getConfigFileName() {
		return "BlockMonsters" + File.separator + "Misc";
	}

	@Override
	public String getCategory() {
		return "BlockMonsters: Misc";
	}

	@Override
	public void hookUpdatedValues() {
		
	}

}
