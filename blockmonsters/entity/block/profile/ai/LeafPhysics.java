package blockmonsters.entity.block.profile.ai;

import blockmonsters.entity.block.StructurePiece;
import CoroUtil.bt.Behavior;



public class LeafPhysics extends LeafMovement {

	//base class for rigidbody using physics movement
	
	public LeafPhysics(Behavior parParent, StructurePiece parPiece) {
		super(parParent, 0, parPiece);
	}
}
