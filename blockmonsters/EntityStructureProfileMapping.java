package blockmonsters;

import java.util.HashMap;

import blockmonsters.entity.ai.LeafBlockMonster;
import blockmonsters.entity.block.profile.ProfilePhysics;
import blockmonsters.entity.block.profile.ProfileWalker;
import blockmonsters.entity.block.profile.StructureProfileBase;

import CoroUtil.componentAI.jobSystem.JobBase;


public class EntityStructureProfileMapping {

	public static HashMap<String, Class> lookupNameToUnit = new HashMap<String, Class>();
	//public static HashMap<BuildingBase, String> lookupBuildingToName = new HashMap<BuildingBase, String>();
	
	static {
		initData();
	}
	
	public static void initData() {
		//lookupBuildingToName.clear();
		lookupNameToUnit.clear();
		
		//addMapping("monster1", ProfileBlockMonster1.class);
		//addMapping("scorpion", ProfileScorpion.class);
		addMapping("physics", ProfilePhysics.class);
		addMapping("walker", ProfileWalker.class);
	}
	
	public static void addMapping(String name, Class building) {
		lookupNameToUnit.put(name, building);
	}
	
	public static StructureProfileBase newStructureProfile(LeafBlockMonster parJob, String name) {
		System.out.println("trying to make a new block monster type: " + name);
		StructureProfileBase ub = null;
		try {
			ub = (StructureProfileBase)lookupNameToUnit.get(name).getConstructor(new Class[] {LeafBlockMonster.class}).newInstance(new Object[] {(LeafBlockMonster)parJob});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (ub != null) {
			ub.name = name;
			return ub;
		} else {
			System.out.println("critical error creating new unit instance");
		}
		return null;
	}
}
