package edu.columbia.quidditch.interact;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.render.collisionobject.CollisionObject;

/**
 * Model Animation
 * 
 * @author Jihan Li
 * 
 */

public class ModelAnimator {

	private int index = 1;
	private int iterator = 0;
	private int timer = 0;
	private String[] current;
	Vector3f originalPos = new Vector3f();
	private CollisionObject obj;
	
	
	public ModelAnimator(int ind, CollisionObject ori)
	{
		obj = ori;
		index = ind;
	}
	
	public void initiate()
	{
		Vector3f pos = obj.getPos();
		Vector3f rot = obj.getRot();
		
		switch(index)
		{
		case 1:
			current = new String[]{"s "+pos.x+" "+pos.y+" "+pos.z
								   +" "+rot.x+" "+rot.y+" "+rot.z+" 10", 
								   "t "+pos.x+" "+pos.y+" "+pos.z
								   +" 5 171 -975 50", "t 5 171 -975 5 171 -1000 50", "f 0 0 0 0 0 0 0"};
			break;
		case 2:
			current = new String[]{"s "+pos.x+" "+pos.y+" "+pos.z
								   +" "+rot.x+" "+rot.y+" "+rot.z+" 10", 
								   "t "+pos.x+" "+pos.y+" "+pos.z
								   +" 0 151 990 50", "t 0 151 990 0 151 1200 50", "f 0 0 0 0 0 0 0"};
			break;
		case 3:
			float up = pos.y + 10;
			float down = pos.y - 10;
			current = new String[]{"s "+pos.x+" "+up+" "+pos.z
								   +" "+rot.x+" "+rot.y+" "+rot.z+" 10", 
								   "t "+pos.x+" "+up+" "+pos.z
								   +" "+pos.x+" "+down+" "+pos.z+" 100", 
								   "t "+pos.x+" "+down+" "+pos.z
								   +" "+pos.x+" "+up+" "+pos.z+" 100", 
								   "t "+pos.x+" "+up+" "+pos.z
								   +" "+pos.x+" "+down+" "+pos.z+" 100", 
								   "t "+pos.x+" "+down+" "+pos.z
								   +" "+pos.x+" "+up+" "+pos.z+" 100", 
								   "t "+pos.x+" "+up+" "+pos.z
								   +" "+pos.x+" "+down+" "+pos.z+" 100", 
								   "t "+pos.x+" "+down+" "+pos.z
								   +" "+pos.x+" "+up+" "+pos.z+" 100", 
								   "t "+pos.x+" "+up+" "+pos.z
								   +" "+pos.x+" "+down+" "+pos.z+" 100", 
								   "t "+pos.x+" "+down+" "+pos.z
								   +" "+pos.x+" "+up+" "+pos.z+" 100",
								   "t "+pos.x+" "+up+" "+pos.z
								   +" "+pos.x+" "+down+" "+pos.z+" 100", 
								   "t "+pos.x+" "+down+" "+pos.z
								   +" "+pos.x+" "+up+" "+pos.z+" 100",
								   "t "+pos.x+" "+up+" "+pos.z
								   +" "+pos.x+" "+down+" "+pos.z+" 100", 
								   "t "+pos.x+" "+down+" "+pos.z
								   +" "+pos.x+" "+up+" "+pos.z+" 100",
								   "t "+pos.x+" "+up+" "+pos.z
								   +" "+pos.x+" "+down+" "+pos.z+" 100", 
								   "t "+pos.x+" "+down+" "+pos.z
								   +" "+pos.x+" "+up+" "+pos.z+" 100",
								   "t "+pos.x+" "+up+" "+pos.z
								   +" "+pos.x+" "+down+" "+pos.z+" 100", 
								   "t "+pos.x+" "+down+" "+pos.z
								   +" "+pos.x+" "+up+" "+pos.z+" 100","f 0 0 0 0 0 0 0"};
			break;
		}
	}
	
	public boolean animate()
	{
		String[] motion = current[iterator].split(" ");
		int last = Integer.parseInt(motion[7]);
		
		if(motion[0].equals("f"))
		{
			return false;
		}
		else if(motion[0].equals("s"))
		{
			obj.setPos(Float.parseFloat(motion[1]), Float.parseFloat(motion[2]), Float.parseFloat(motion[3]));
			obj.setRot(Float.parseFloat(motion[4]), Float.parseFloat(motion[5]), Float.parseFloat(motion[6]));
		}
		else if(motion[0].equals("t"))
		{
			Vector3f initial = new Vector3f(Float.parseFloat(motion[1]), Float.parseFloat(motion[2]), Float.parseFloat(motion[3]));
			Vector3f direction = new Vector3f((Float.parseFloat(motion[4]) - initial.x)/last,
											  (Float.parseFloat(motion[5]) - initial.y)/last,
											  (Float.parseFloat(motion[6]) - initial.z)/last);
			Vector3f position = new Vector3f(initial.x + direction.x * timer, initial.y + direction.y * timer, initial.z + direction.z * timer);
			obj.setPos(position);
		}
		else if(motion[0].equals("r"))
		{
			float theta = angle2Pi(Float.parseFloat(motion[4]))*timer/last;
			float phi = angle2Pi(Float.parseFloat(motion[5]))*timer/last;
			float psi = angle2Pi(Float.parseFloat(motion[6]))*timer/last;
			
			Matrix3f initial = new Matrix3f();
			initial.m00 = angle2Pi(Float.parseFloat(motion[1]));
			initial.m01 = angle2Pi(Float.parseFloat(motion[2]));
			initial.m02 = angle2Pi(Float.parseFloat(motion[3]));
			Matrix3f rotMatrix = new Matrix3f();
			
			Matrix3f euler = new Matrix3f();
			
			euler.m00 = (float) (Math.cos(psi)*Math.cos(theta) - Math.sin(psi)*Math.cos(phi)*Math.sin(theta));
			euler.m10 = (float) (-Math.sin(psi)*Math.cos(theta) - Math.cos(psi)*Math.cos(phi)*Math.sin(theta));
			euler.m20 = (float) (Math.sin(phi)*Math.sin(theta));
			
			euler.m01 = (float) (Math.cos(psi)*Math.sin(theta) + Math.sin(psi)*Math.cos(phi)*Math.cos(theta));
			euler.m11 = (float) (-Math.sin(psi)*Math.sin(theta) + Math.cos(psi)*Math.cos(phi)*Math.cos(theta));
			euler.m21 = (float) (-Math.sin(phi)*Math.cos(theta));
			
			euler.m02 = (float) (Math.sin(psi)*Math.sin(phi));
			euler.m12 = (float) (Math.cos(psi)*Math.sin(phi));
			euler.m22 = (float) (Math.cos(phi));
			
			
			Matrix3f.mul(euler, initial, rotMatrix);
			Vector3f rotation = new Vector3f(pi2Angle(rotMatrix.m00), pi2Angle(rotMatrix.m01), pi2Angle(rotMatrix.m02));
			obj.setRot(rotation);
		}
		timer++;

		if(timer == last)
		{
			iterator++;
			timer = 0;
		}
		
		return true;
	}
	
	public float angle2Pi(float angle)
	{
		return (float) (angle * Math.PI / 180);
	}
	
	public float pi2Angle(float pi)
	{
		return (float) (pi * 180 / Math.PI);
	}
	
	public int getIterator() {
		return iterator;
	}

	public void setIterator(int iterator) {
		this.iterator = iterator;
	}

	public String[] getCurrent() {
		return current;
	}

	public void setCurrent(String[] current) {
		this.current = current;
	}
	
	public void reset()
	{
		iterator = 0;
		timer = 0;
	}

}
