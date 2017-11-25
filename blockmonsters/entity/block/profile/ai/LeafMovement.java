package blockmonsters.entity.block.profile.ai;

import blockmonsters.entity.block.StructurePiece;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.leaf.LeafAction;


public class LeafMovement extends LeafAction {

	int id = 0;
	int countCur = 0;
	int countMax;
	
	public StructurePiece rootPiece;
	
	public LeafMovement(Behavior parParent, int max, StructurePiece parPiece) {
		super(parParent);
		countMax = max;
		rootPiece = parPiece;
	}
	
	@Override
	public EnumBehaviorState tick() {
		//dbg("Leaf Delay Tick id: " + id + " - " + countCur + "/" + countMax);
		countCur++;
		if (isAnimationComplete()) {
			//reset(); //dont reset animation till its at the end, or new logic for transition...
			return EnumBehaviorState.SUCCESS;
		} else {
			return EnumBehaviorState.RUNNING;
		}
	}
	
	public boolean isAnimationComplete() {
		return countCur > countMax;
	}
	
	@Override
	public void reset() {
		//dbg("Leaf Delay Reset id: " + id);
		countCur = 0;
		super.reset();
	}
	
}
