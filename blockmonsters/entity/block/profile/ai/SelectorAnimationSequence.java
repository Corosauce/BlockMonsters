package blockmonsters.entity.block.profile.ai;

import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.selector.SelectorSequence;

public class SelectorAnimationSequence extends SelectorSequence {

	//-1 = unlimited looping
	public int playCountsLeft = -1;
	public int playCountsMax = -1;
	public boolean concurrent = false;
	
	public SelectorAnimationSequence(Behavior parParent, int parPlayCount) {
		super(parParent);
		playCountsLeft = parPlayCount;
		playCountsMax = parPlayCount;
	}
	
	public SelectorAnimationSequence(Behavior parParent, int parPlayCount, boolean parConcurrent) {
		this(parParent, parPlayCount);
		concurrent = parConcurrent;
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		EnumBehaviorState returnState = EnumBehaviorState.INVALID;
		
		//concurrent needs better logic on what the final return would be, success on all success for example
		if (concurrent) {
			for (int i = 0; i < children.size(); i++) {
				returnState = children.get(i).tick();
				if (returnState == EnumBehaviorState.FAILURE) return returnState; //bail
			}
			return returnState;
		} else {
			returnState = super.tick();
			if (returnState == EnumBehaviorState.SUCCESS) {
				reset();
				if (playCountsLeft != -1) {
					playCountsLeft--;
					if (playCountsLeft <= 0) {
						//reset();
						return EnumBehaviorState.SUCCESS;
					} else {
						//reset();
						return EnumBehaviorState.RUNNING;
					}
				} else {
					//reset();
					return EnumBehaviorState.RUNNING;
				}
			} else {
				return returnState;
			}
		}
		
	}
	
	@Override
	public void resetChildren() {
		for (int i = 0; i < children.size(); i++) {
			Behavior bh = children.get(i);
			//if (bh.state == EnumBehaviorState.RUNNING) {
				bh.reset();
			//}
		}
	}

}
