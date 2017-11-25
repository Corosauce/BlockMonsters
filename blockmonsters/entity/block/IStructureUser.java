package blockmonsters.entity.block;

import javax.vecmath.Vector3f;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public interface IStructureUser {

	public boolean passBackAttackFrom(IStructureUser parPasser, DamageSource par1DamageSource, float par2, Vector3f coordsSource);
	public boolean passInteract(IStructureUser parPasser, EntityPlayer par1EntityPlayer);
	
	public Entity getOwnerEntity();
	
}
