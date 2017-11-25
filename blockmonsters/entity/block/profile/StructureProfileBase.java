package blockmonsters.entity.block.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.vecmath.Vector3f;

import blockmonsters.entity.ai.LeafBlockMonster;
import blockmonsters.entity.block.IStructureUser;
import blockmonsters.entity.block.StructureNode;
import blockmonsters.entity.block.StructurePiece;
import blockmonsters.entity.block.profile.ai.SelectorAnimationSequence;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.componentAI.jobSystem.JobBase;


public class StructureProfileBase {

	public String name;
	public StructurePiece body;
	public LeafBlockMonster job;
	public List<SelectorAnimationSequence> animationSequences = new ArrayList<SelectorAnimationSequence>(); //list of concurrently run animation sets, each containing a list of sequentially run animations
	public HashMap<String, SelectorAnimationSequence> animationSequencesLookup = new HashMap<String, SelectorAnimationSequence>();
	public HashMap<SelectorAnimationSequence, String> animationSequencesLookupRev = new HashMap<SelectorAnimationSequence, String>();
	public List<StructureNode> collisionListeners = new ArrayList<StructureNode>();
	
	public StructureProfileBase(LeafBlockMonster parJob) {
		job = parJob;
	}
	
	public void populateStructureData() {
		
	}
	
	public void populateStructureDataFromNBT(NBTTagCompound parData) {
		body = StructurePiece.createFromNBTRecursive((IStructureUser)job, parData);
	}
	
	public void tickUpdate() {
		for (int i = 0; i < animationSequences.size(); i++) {
			SelectorAnimationSequence sel = animationSequences.get(i);
			EnumBehaviorState returnState = sel.tick();
			if (returnState == EnumBehaviorState.SUCCESS || returnState == EnumBehaviorState.INVALID) {
				removeAnimationSequence(sel);
				i--;
			}
		}
	}
	
	public boolean passBackAttackFrom(IStructureUser parPasser, DamageSource par1DamageSource, float par2, Vector3f coordsSource) {
		return true;
	}
	
	public void addAnimationSequence(String parName, SelectorAnimationSequence parSequence) {
		animationSequences.add(parSequence);
		animationSequencesLookup.put(parName, parSequence);
		animationSequencesLookupRev.put(parSequence, parName);
	}
	
	public void removeAnimationSequence(String parName) {
		SelectorAnimationSequence seq = animationSequencesLookup.get(parName);
		animationSequencesLookup.remove(parName);
		animationSequencesLookupRev.remove(seq);
		animationSequences.remove(seq);
	}
	
	public void removeAnimationSequence(SelectorAnimationSequence parSequence) {
		animationSequencesLookup.remove(animationSequencesLookupRev.get(parSequence));
		animationSequencesLookupRev.remove(parSequence);
		animationSequences.remove(parSequence);
	}
	
	public boolean hasAnimationSequenceRunning(String parName) {
		return animationSequencesLookup.containsKey(parName);
	}
	
	public void cleanup() {
		
		//needs cleanup calls on anim sequencers and phys performers
		
		//cleanup structurepiece references, including links to jbullet world
		body.cleanupRecursive();
		
		body = null;
		job = null;
	}
}
