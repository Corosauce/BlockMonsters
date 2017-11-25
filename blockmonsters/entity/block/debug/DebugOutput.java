package blockmonsters.entity.block.debug;

import javax.vecmath.Vector3f;

import build.render.Overlays;

import com.bulletphysics.linearmath.IDebugDraw;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class DebugOutput extends IDebugDraw {

	private int debugMode;
	
	@Override
	public void drawLine(Vector3f from, Vector3f to, Vector3f color) {
		// TODO Auto-generated method stub
		//dbg("color: " + color);
		//Overlays.renderLineFromToBlockCenter(from.x, from.y + 80, from.z, to.x, to.y + 80, to.z, (int)color.x*255 << 24 | (int)color.x*255 << 16 | (int)color.x*255 << 8);
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			//drawLineClient(from, to, color);
		}
		//
		if (color.x != 0) {
			//System.out.println(Math.toDegrees(Math.atan2(from.y-to.y, from.z-to.z)));
			//GL11.glPushMatrix();
			//GL11.glTranslatef(from.x, from.y+1F, from.z);
			//GL11.glRotatef((float) Math.toDegrees(Math.atan2(from.z-to.z, from.x-to.x)), 0, 1, 0);
			//GL11.glRotatef((float) Math.toDegrees(Math.atan2(from.y-to.y, from.z-to.z)), 1, 0, 0);
			//Overlays.renderBlock(Block.lavaStill, 0, (int)from.x, (int)from.y, (int)from.z);
			//GL11.glPopMatrix();
		}
		//System.out.println("drawLine() from: " + from);
		
	}
	
	@SideOnly(Side.CLIENT)
	public void drawLineClient(Vector3f from, Vector3f to, Vector3f color) {
		//System.out.println("drawLine() from: " + from);
		Overlays.renderLineFromToBlockCenter(from.x, from.y, from.z, to.x, to.y, to.z, (int)color.z*255 << 16 | (int)color.y*255 << 8 | (int)color.x*255);
	}

	@Override
	public void drawContactPoint(Vector3f PointOnB, Vector3f normalOnB,
			float distance, int lifeTime, Vector3f color) {
		// TODO Auto-generated method stub
		dbg("drawContactPoint: " + PointOnB);
	}

	@Override
	public void reportErrorWarning(String warningString) {
		// TODO Auto-generated method stub
		dbg("reportErrorWarning: ");
	}

	@Override
	public void draw3dText(Vector3f location, String textString) {
		// TODO Auto-generated method stub
		dbg("draw3dText: ");
	}

	@Override
	public void setDebugMode(int debugMode) {
		// TODO Auto-generated method stub
		dbg("setDebugMode: " + debugMode);
		this.debugMode = debugMode;
	}

	@Override
	public int getDebugMode() {
		// TODO Auto-generated method stub
		return debugMode/*DebugDrawModes.MAX_DEBUG_DRAW_MODE*/;
	}
	
	public void dbg(Object obj) {
		System.out.println(obj);
	}

}
