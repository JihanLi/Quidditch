package edu.columbia.quidditch.interact;


import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.basic.Camera;

/**
 * Camera Animation
 * 
 * @author Jihan Li
 * 
 */

public class CameraAnimator {
	
	private int iterator = 0;
	private int timer = 0;
	private String[] current;
	
	// s means stay, t means translate, r means rotate, f means end.
	public static String[] animation1 = {"s 0 -300 -1200 30 0 0 10", "t 0 -300 -1200 0 -300 0 400", "s 0 0 0 -10 180 0 20",
										 "r -10 180 0 0 90 0 300", "s 0 0 0 -10 0 0 10", "f 0 0 0 0 0 0 0"};
	
	public static String[] animation2 = {"t 0 -300 -1200 30 0 0 10", "f 0 0 0 0 0 0 0"};
	
	
	public CameraAnimator(int index)
	{
		switch(index)
		{
		case 1:
			current = animation1;
			break;
		case 2:
			current = animation2;
			break;
		}
	}
	
	public boolean animate(Camera camera)
	{
		String[] motion = current[iterator].split(" ");
		int last = Integer.parseInt(motion[7]);
		
		if(motion[0].equals("f"))
		{
			return false;
		}
		else if(motion[0].equals("s"))
		{
			camera.setPosition(Float.parseFloat(motion[1]), Float.parseFloat(motion[2]), Float.parseFloat(motion[3]));
			camera.setRotation(Float.parseFloat(motion[4]), Float.parseFloat(motion[5]), Float.parseFloat(motion[6]));
		}
		else if(motion[0].equals("t"))
		{
			Vector3f initial = new Vector3f(Float.parseFloat(motion[1]), Float.parseFloat(motion[2]), Float.parseFloat(motion[3]));
			Vector3f direction = new Vector3f((Float.parseFloat(motion[4]) - initial.x)/last,
											  (Float.parseFloat(motion[5]) - initial.y)/last,
											  (Float.parseFloat(motion[6]) - initial.z)/last);
			Vector3f position = new Vector3f(initial.x + direction.x * timer, initial.y + direction.y * timer, initial.z + direction.z * timer);
			camera.setPosition(position);
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
			
			/*
			Matrix3f eulerZ = new Matrix3f();
			
			eulerZ.m00 = (float) (Math.cos(phi));
			eulerZ.m10 = (float) (Math.sin(phi));
			eulerZ.m20 = 0;
			
			eulerZ.m01 = (float) (-Math.sin(phi));
			eulerZ.m11 = (float) (Math.cos(phi));
			eulerZ.m21 = 0;
			
			eulerZ.m02 = 0;
			eulerZ.m12 = 0;
			eulerZ.m22 = 1;
			
			Matrix3f.mul(eulerZ, initial, initial);
			
			
			Matrix3f eulerX = new Matrix3f();
			
			eulerX.m00 = 1;
			eulerX.m10 = 0;
			eulerX.m20 = 0;
			
			eulerX.m01 = 0;
			eulerX.m11 = (float) (Math.cos(theta));
			eulerX.m21 = (float) (Math.sin(theta));
			
			eulerX.m02 = 0;
			eulerX.m12 = (float) (-Math.sin(theta));
			eulerX.m22 = (float) (Math.cos(theta));
			
			
			Matrix3f.mul(eulerX, initial, initial);
			
			
			Matrix3f eulerY = new Matrix3f();
			
			eulerY.m00 = (float) (Math.cos(psi));
			eulerY.m10 = (float) (Math.sin(psi));
			eulerY.m20 = 0;
			
			eulerY.m01 = (float) (-Math.sin(psi));
			eulerY.m11 = (float) (Math.cos(psi));
			eulerY.m21 = 0;
			
			eulerY.m02 = 0;
			eulerY.m12 = 0;
			eulerY.m22 = 1;
			
			Matrix3f.mul(eulerY, initial, rotMatrix);*/
			
			
			
			
			
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
			camera.setRotation(rotation);
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

}
