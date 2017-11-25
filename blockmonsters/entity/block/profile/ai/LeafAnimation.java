package blockmonsters.entity.block.profile.ai;

import blockmonsters.entity.block.StructurePiece;
import CoroUtil.bt.Behavior;



public class LeafAnimation extends LeafMovement {

	//base class for locked animation based movements
	
	public LeafAnimation(Behavior parParent, int max, StructurePiece parPiece) {
		super(parParent, max, parPiece);
	}
}
