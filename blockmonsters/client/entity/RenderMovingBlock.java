package blockmonsters.client.entity;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import CoroUtil.entity.block.MovingBlock;
import blockmonsters.entity.BlockMonster1;
import blockmonsters.entity.block.StructureBlock;
import blockmonsters.entity.block.StructurePiece;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMovingBlock extends Render
{
	
	boolean itemRender = false;
	
    public RenderMovingBlock()
    {
    	
    }

	@Override

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(Entity entity) {
		return TextureMap.locationBlocksTexture;
	}

    @Override
    public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9)
    {
    	this.bindEntityTexture(var1);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_FOG);
        
        MovingBlock entBlock = ((MovingBlock)var1);
        float size = entBlock.scale;
        
        GL11.glTranslatef((float)var2, (float)var4, (float)var6);
        
        //quats wip
        /*GL11.glRotatef((float)(entBlock.rotationPitchB), 0.0F, 0.0F, 1.0F);
        GL11.glRotatef((float)(entBlock.rotationYawB), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef((float)(entBlock.rotationRoll), 1.0F, 0.0F, 0.0F);*/
        
        //euler wip
        /*GL11.glRotatef((float)(entBlock.rotationYawB), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef((float)(entBlock.rotationRoll), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((float)(entBlock.rotationPitchB), 0.0F, 0.0F, 1.0F);*/
        
        /*GL11.glRotatef((float)(entBlock.rotationYawB), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef((float)(entBlock.rotationRoll), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((float)(entBlock.rotationPitchB), 0.0F, 0.0F, 1.0F);*/
        
        //fun
        GL11.glRotatef((float)(entBlock.rotationYawB), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef((float)(entBlock.rotationRoll), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((float)(entBlock.rotationPitchB), 0.0F, 0.0F, 1.0F);
        
        //pitch roll yaw is in piece order
        
        List<Vec3> rotationList = new ArrayList<Vec3>();
        
        //we get the owner entity, to lookup the top body piece, to get the hashmap to use the lookup the parentPieceID, to finally be able to iterate up the rotations
        
        if (entBlock instanceof StructureBlock) {
        	Entity entLookup = entBlock.worldObj.getEntityByID(((StructureBlock) entBlock).ownerEntityID);
        	if (entLookup instanceof BlockMonster1) {
        		if (((BlockMonster1) entLookup).priJob.structureProfile != null && ((BlockMonster1) entLookup).priJob.structureProfile.body instanceof StructurePiece) {
        			StructurePiece piece = (StructurePiece)((BlockMonster1) entLookup).priJob.structureProfile.body;
        			
        			StructurePiece parentPiece = piece.childPiecesLookupAll.get(((StructureBlock) entBlock).parentPieceID);
        	    	
        			//System.out.println("((StructureBlock) entBlock).parentPieceID: " + ((StructureBlock) entBlock).parentPieceID);
        			
        			if (parentPiece != null && parentPiece.parent instanceof StructurePiece) {
    	        		parentPiece = (StructurePiece)parentPiece.parent;
    	        		
    	        		if (parentPiece instanceof StructurePiece) {
    	        			
    	        	    	while (true) {
    	        	    		
    	        	    		/////////////rotationList.add(Vec3.createVectorHelper(parentPiece.rotationRoll, parentPiece.rotationYawB, parentPiece.rotationPitchB));
    	        	    		
    	        	    		/*GL11.glRotatef((float)(parentPiece.rotationYawB), 0.0F, 1.0F, 0.0F);
    	        	            GL11.glRotatef((float)(-parentPiece.rotationRoll), 1.0F, 0.0F, 0.0F);
    	        	            GL11.glRotatef((float)(-parentPiece.rotationPitchB), 0.0F, 0.0F, 1.0F);*/
    	        	            
    	        	            /*
    	        	    		
    	        	    		relPos = Vec3.createVectorHelper(newX, newY, 0);
    	        	        	relPos.rotateAroundZ(parentPiece.rotationPitchB * 0.01745329F);
    	        	        	
    	        	        	newX = relPos.xCoord;
    	        	        	newY = relPos.yCoord;
    	        	        	
    	        	        	relPos = Vec3.createVectorHelper(0, newY, newZ);
    	        	        	relPos.rotateAroundX(parentPiece.rotationRoll * 0.01745329F);
    	        	        	
    	        	        	newY = relPos.yCoord;
    	        	        	newZ = relPos.zCoord;
    	        	        	
    	        	        	relPos = Vec3.createVectorHelper(newX, 0, newZ);
    	        	        	relPos.rotateAroundY(parentPiece.rotationYawB * 0.01745329F);
    	        	        	
    	        	        	newX = relPos.xCoord;
    	        	        	newZ = relPos.zCoord;*/
    	        	        	
    	        	        	if (parentPiece.parent instanceof StructurePiece) {
    	        	        		parentPiece =  (StructurePiece)parentPiece.parent;
    	        	        	} else {
    	        	        		break;
    	        	        	}
    	        	    	}
            			}
        			}
            	}
        	}
        }
        
        for (int i = rotationList.size()-1; i >= 0; i--) {
        	GL11.glRotatef((float)(rotationList.get(i).yCoord), 0.0F, 1.0F, 0.0F);
            GL11.glRotatef((float)(-rotationList.get(i).xCoord), 1.0F, 0.0F, 0.0F);
            GL11.glRotatef((float)(-rotationList.get(i).zCoord), 0.0F, 0.0F, 1.0F);
        }
        
        //was latest
        /*GL11.glRotatef((float)(entBlock.rotationYawB), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef((float)(-entBlock.rotationRoll), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((float)(-entBlock.rotationPitchB), 0.0F, 0.0F, 1.0F);*/
        
        //need quat info?
        
        boolean newWay = false;
        
        //WIP matrix based rotation
        if (newWay) {
	        /*GL11.glMatrixMode(GL11.GL_PROJECTION);
	        GL11.glLoadIdentity(); 
	        GLU.gluPerspective(45.0f, ((float) setting.displayW() / (float) setting.displayH()), 0.1f,10000.0f);*/ 
	        GL11.glMatrixMode(GL11.GL_MODELVIEW); 
	        GL11.glLoadIdentity(); 
	        GL11.glShadeModel(GL11.GL_SMOOTH);
	        GL11.glEnable(GL11.GL_DEPTH_TEST); 
	        GL11.glDepthFunc(GL11.GL_LEQUAL);
	        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST); 
	        Quat4f quat = new Quat4f(entBlock.rotationRoll, entBlock.rotationPitchB, entBlock.rotationYawB, 1);
	        Matrix3f rotation = new Matrix3f();
	        quat.set(rotation);
	        FloatBuffer fb = BufferUtils.createFloatBuffer(32);
	        writeMatrixToBuffer(rotation, fb);
	        GL11.glMultMatrix(fb);
        } else {
        	//i dont think this way works for matricies anymore
            GL11.glRotatef((float)(entBlock.rotationRoll), 1.0F, 0.0F, 0.0F);
            GL11.glRotatef((float)(entBlock.rotationPitchB), 0.0F, 0.0F, 1.0F);
            GL11.glRotatef((float)(entBlock.rotationYawB), 0.0F, 1.0F, 0.0F);
        }
        
        
        //to get the correct rotation for child pieces, you must do a recursive series of rotations just like the pieces do
        //tested to be the case 
        
        //GL11.glRotatef((float)(-45), 1.0F, 0.0F, 0.0F);
        //GL11.glRotatef((float)(30), 0.0F, 0.0F, 1.0F);
        //System.out.println(entBlock.rotationPitchB);
        
        //GL11.glRotatef((float)(entBlock.age/* * entBlock.blockNum*/ * 0.02F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
        //GL11.glRotatef((float)(entBlock.age/* * entBlock.blockNum*/ * 0.02F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
        //GL11.glRotatef((float)(entBlock.age/* * entBlock.blockNum*/ * 0.02F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
        GL11.glScalef(size, size, size);
        
        //this.loadTexture("/terrain.png");
        Block block = Block.blocksList[entBlock.blockID];
        
        //block = Block.lavaStill;
        //block = Block.ice;
        //block = Block.sand;
        //block = Block.glass;
        
        itemRender = false;
        
        if (block != null) {
	        if (itemRender) {
		        RenderBlocks rb = new RenderBlocks(var1.worldObj);
		        rb.renderBlockAsItem(block, 0, 0.8F);
	        } else {
	        	GL11.glDisable(GL11.GL_LIGHTING);
	        	this.renderFallingCube(entBlock, block, var1.worldObj, MathHelper.floor_double(var1.posX), MathHelper.floor_double(var1.posY), MathHelper.floor_double(var1.posZ), entBlock.blockMeta);
	        }
        } else {
        	System.out.println("moving block has no blockID set for render");
        }
        
        GL11.glEnable(GL11.GL_FOG);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
    
    public static FloatBuffer writeMatrixToBuffer(Matrix3f matrix, FloatBuffer buffer) {
		if (buffer == null)
		{
			buffer = BufferUtils.createFloatBuffer(9);
		}
		int oldPosition = buffer.position();
		buffer.put(matrix.m00);
		buffer.put(matrix.m10);
		buffer.put(matrix.m20);
		buffer.put(matrix.m01);
		buffer.put(matrix.m11);
		buffer.put(matrix.m21);
		buffer.put(matrix.m02);
		buffer.put(matrix.m12);
		buffer.put(matrix.m22);
		buffer.position(oldPosition);
		return buffer;
    }
    
    public void renderFallingCube(MovingBlock var1, Block var2, World var3, int var4, int var5, int var6, int var7)
    {
    	RenderBlocks a = new RenderBlocks(var1.worldObj);
    	
        float var8 = 0.5F;
        float var9 = 1.0F;
        float var10 = 0.8F;
        float var11 = 0.6F;
        Tessellator var12 = Tessellator.instance;
        var12.startDrawingQuads();
        //float var13 = var2.getBlockBrightness(var3, var4, var5, var6);
        //float var14 = var2.getBlockBrightness(var3, var4, var5 - 1, var6);
        var12.setBrightness(var2.getMixedBrightnessForBlock(var3, var4, var5 + 1, var6));

        float var13 = 0.8F;
        float var14 = 0.8F;
        
        //var13 = (float) (var13 + Math.cos((var1.worldObj.getWorldTime() * 0.3F) - (/*var1.blockRow * */0.5F)) * 0.15F);
        var14 = var13;
        
        float var15 = 1.0F;
        float var16 = 1.0F;
        float var17 = 1.0F;

        if (var2.blockID == Block.leaves.blockID)
        {
            int var18 = var2.colorMultiplier(var3, (int)var1.posX, (int)var1.posY, (int)var1.posZ);
            var15 = (float)(var18 >> 16 & 255) / 255.0F;
            var16 = (float)(var18 >> 8 & 255) / 255.0F;
            var17 = (float)(var18 & 255) / 255.0F;

            if (EntityRenderer.anaglyphEnable)
            {
                float var19 = (var15 * 30.0F + var16 * 59.0F + var17 * 11.0F) / 100.0F;
                float var20 = (var15 * 30.0F + var16 * 70.0F) / 100.0F;
                float var21 = (var15 * 30.0F + var17 * 70.0F) / 100.0F;
                var15 = var19;
                var16 = var20;
                var17 = var21;
            }
        }
        
        //NEW! - set block render size
        a.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
        //a.setRenderMinMax(0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D);

        var12.setColorOpaque_F(var15 * var8 * var14, var16 * var8 * var14, var17 * var8 * var14);
        a.renderFaceYNeg(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(0, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var9 * var14, var16 * var9 * var14, var17 * var9 * var14);
        a.renderFaceYPos(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(1, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var10 * var14, var16 * var10 * var14, var17 * var10 * var14);
        a.renderFaceZNeg(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(2, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var10 * var14, var16 * var10 * var14, var17 * var10 * var14);
        a.renderFaceZPos(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(3, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var11 * var14, var16 * var11 * var14, var17 * var11 * var14);
        a.renderFaceXNeg(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(4, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var11 * var14, var16 * var11 * var14, var17 * var11 * var14);
        a.renderFaceXPos(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(5, var7));
        var12.draw();
    }
}
