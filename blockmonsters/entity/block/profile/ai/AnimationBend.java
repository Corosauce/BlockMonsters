package blockmonsters.entity.block.profile.ai;

import blockmonsters.entity.block.StructurePiece;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;


public class AnimationBend extends LeafAnimation {

	public Vec3 tweenStart;
	public Vec3 tweenEnd;
	public Vec3 tweenCurrent;
	public Vec3 tweenRate;
	public boolean xLocked = false;
	public boolean yLocked = false;
	public boolean zLocked = false;
	
	public boolean shouldReturnAnimate = false;
	public boolean onReturnAnimate = false;
	
	public AnimationBend(Behavior parParent, int max, StructurePiece parPiece, Vec3 parStart, Vec3 parEnd, Vec3 parRate, boolean parAnimateReturn) {
		this(parParent, max, parPiece, parStart, parEnd, parRate);
		shouldReturnAnimate = parAnimateReturn;
	}
	
	public AnimationBend(Behavior parParent, int max, StructurePiece parPiece, Vec3 parStart, Vec3 parEnd, Vec3 parRate) {
		super(parParent, max, parPiece);
		tweenStart = parStart;
		tweenCurrent = Vec3.createVectorHelper(tweenStart.xCoord, tweenStart.yCoord, tweenStart.zCoord);
		tweenEnd = parEnd;
		tweenRate = parRate;
		
		//ngh
		/*while (tweenEnd.xCoord < 0) tweenEnd.xCoord += 360;
		while (tweenEnd.xCoord > 360) tweenEnd.xCoord -= 360;
		while (tweenEnd.yCoord < 0) tweenEnd.yCoord += 360;
		while (tweenEnd.yCoord > 360) tweenEnd.yCoord -= 360;
		while (tweenEnd.zCoord < 0) tweenEnd.zCoord += 360;
		while (tweenEnd.zCoord > 360) tweenEnd.zCoord -= 360;*/
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		//ticks each x y z individually with rate x y z, locks each axis as it gets close enough so it doesnt go off target when other angles still animating
		
		Vec3 newRot = Vec3.createVectorHelper(tweenCurrent.xCoord, tweenCurrent.yCoord, tweenCurrent.zCoord);
		
		double amp = 1.2D;
		
		double flipRate = 1D;
		if (onReturnAnimate) flipRate = -1D;
		
		if (!xLocked) {
			newRot.xCoord += tweenRate.xCoord * flipRate;
			//while (newRot.xCoord < 0) newRot.xCoord += 360;
			//while (newRot.xCoord > 360) newRot.xCoord -= 360;
			//newRot.xCoord = MathHelper.wrapAngleTo180_double(newRot.xCoord);
			if (onReturnAnimate) {
				if (Math.abs(newRot.xCoord - tweenStart.xCoord) <= Math.abs(tweenRate.xCoord) * amp) {
					xLocked = true;
				}
			} else {
				if (Math.abs(newRot.xCoord - tweenEnd.xCoord) <= Math.abs(tweenRate.xCoord) * amp) {
					xLocked = true;
				}
			}
			
		}
		if (!yLocked) {
			newRot.yCoord += tweenRate.yCoord * flipRate;
			//while (newRot.yCoord < 0) newRot.yCoord += 360;
			//while (newRot.yCoord > 360) newRot.yCoord -= 360;
			//newRot.yCoord = MathHelper.wrapAngleTo180_double(newRot.xCoord);
			if (onReturnAnimate) {
				if (Math.abs(newRot.yCoord - tweenStart.yCoord) <= Math.abs(tweenRate.yCoord) * amp) {
					yLocked = true;
				}
			} else {
				if (Math.abs(newRot.yCoord - tweenEnd.yCoord) <= Math.abs(tweenRate.yCoord) * amp) {
					yLocked = true;
				}
			}
		}
		if (!zLocked) {
			newRot.zCoord += tweenRate.zCoord * flipRate;
			//while (newRot.zCoord < 0) newRot.zCoord += 360;
			//while (newRot.zCoord > 360) newRot.zCoord -= 360;
			//newRot.zCoord = MathHelper.wrapAngleTo180_double(newRot.xCoord);
			if (onReturnAnimate) {
				if (Math.abs(newRot.zCoord - tweenStart.zCoord) <= Math.abs(tweenRate.zCoord) * amp) {
					zLocked = true;
				}
			} else {
				if (Math.abs(newRot.zCoord - tweenEnd.zCoord) <= Math.abs(tweenRate.zCoord) * amp) {
					zLocked = true;
				}
			}
		}
		
		tweenCurrent = Vec3.createVectorHelper(newRot.xCoord, newRot.yCoord, newRot.zCoord);
		
		if (shouldReturnAnimate) {
			if (onReturnAnimate) {
				//System.out.println(tweenStart + " - " + tweenCurrent + " - " + tweenEnd + " - end: " + Math.abs(newRot.xCoord - tweenEnd.xCoord) + " - start: " + Math.abs(newRot.xCoord - tweenStart.xCoord) + " - " + tweenRate.xCoord * flipRate);
			}
		}
		
		/*this.rootPiece.rotationRoll = (float) newRot.xCoord;
		this.rootPiece.rotationYawB = (float) newRot.yCoord;
		this.rootPiece.rotationPitchB = (float) newRot.zCoord;*/
		
		if (shouldReturnAnimate) {
			if (!onReturnAnimate) {
				if (xLocked && yLocked && zLocked) {
					xLocked = false;
					yLocked = false;
					zLocked = false;
					onReturnAnimate = true;
				}
			}
		}
		
		return super.tick();
	}
	
	@Override
	public boolean isAnimationComplete() {
		//might need more accurate way to hit end, when it could come from either + or -
		return xLocked && yLocked && zLocked && (!shouldReturnAnimate || onReturnAnimate);
		//return tweenStart.distanceTo(tweenEnd) <= tweenRate.lengthVector();
	}
	
	@Override
	public void reset() {
		super.reset();
		xLocked = false;
		yLocked = false;
		zLocked = false;
		onReturnAnimate = false;
		tweenCurrent = Vec3.createVectorHelper(tweenStart.xCoord, tweenStart.yCoord, tweenStart.zCoord);
		/*this.rootPiece.rotationRoll = (float) tweenCurrent.xCoord;
		this.rootPiece.rotationYawB = (float) tweenCurrent.yCoord;
		this.rootPiece.rotationPitchB = (float) tweenCurrent.zCoord;*/
	}

}
