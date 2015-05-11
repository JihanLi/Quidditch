package edu.columbia.quidditch.basic;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.render.screen.LoadScreen;

/**
 * Camera class
 * 
 * @author Yuqing Guan
 * 
 */
public class Camera
{
	private static final float MAX_LOOK = 360;

	private Vector3f cameraRot;

	/**
	 * Determine whether the world will be rotated by the last mouse move This
	 * variable is used to prevent mouse move makes sudden big rotation of the
	 * camera if a user switch from other program to my game
	 */
	private boolean swing;

	/**
	 * The transform matrix of current world, which is used to check which plane
	 * is hit
	 */
	private Matrix4f matrix;

	private MainGame game;

	public Camera(MainGame game)
	{
		LoadScreen.increaseLoadCount();
		this.game = game;
		
		cameraRot = new Vector3f(0, 0, 0);

		swing = false;
		matrix = new Matrix4f();
	}

	/**
	 * Rotate the world by both myself and the camera
	 */
	public void applyRotation()
	{
		// Vector3f myselfRot = myself.getRot();

		glRotatef(cameraRot.x, 1, 0, 0);
		glRotatef(cameraRot.y, 0, 1, 0);

		// glRotatef(-myselfRot.x, 1, 0, 0);
		// glRotatef(-myselfRot.y, 0, 1, 0);
	}

	/**
	 * Translate the world by the position of myself
	 */
	public void applyTranslation()
	{
		glTranslatef(0, -200, 0);
		// Vector3f pos = myself.getPos();
		// glTranslatef(-pos.x, -pos.y, -pos.z);
	}

	/**
	 * Rotate the x-axis by mouse
	 * 
	 * @param delta
	 */
	public void rotX(float delta)
	{
		cameraRot.x += delta;
		cameraRot.x = Math.max(-MAX_LOOK, Math.min(MAX_LOOK, cameraRot.x));
	}

	/**
	 * Rotate the x-axis by mouse
	 * 
	 * @param delta
	 */
	public void rotY(float delta)
	{
		cameraRot.y += delta;
		cameraRot.y = Math.max(-MAX_LOOK, Math.min(MAX_LOOK, cameraRot.y));
	}

	/**
	 * Reset the camera to normal direction Used when the player hit a hill, we
	 * should let him/her know what he/she hit and what made him/her dead
	 */
	public void resetRot()
	{
		cameraRot.x = cameraRot.y = 0;
	}

	public void startSwing()
	{
		swing = true;
	}

	public void stopSwing()
	{
		swing = false;
	}

	public boolean isSwinging()
	{
		return swing;
	}

	/**
	 * Store the transform matrix for the world
	 */
	public void readMatrix()
	{
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		glGetFloat(GL_MODELVIEW_MATRIX, buffer);
		matrix.load(buffer);
	}

	/**
	 * Get the transform matrix
	 * 
	 * @return
	 */
	public Matrix4f getMatrix()
	{
		return matrix;
	}
}
