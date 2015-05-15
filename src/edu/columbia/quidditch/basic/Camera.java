package edu.columbia.quidditch.basic;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.render.collisionobject.Player;
import edu.columbia.quidditch.render.screen.LoadScreen;
import edu.columbia.quidditch.render.screen.PlayScreen;

/**
 * Camera class
 * 
 * @author Jihan Li, Yuqing Guan, Yilin Xiong
 * 
 */
public class Camera
{
	private static final float MAX_LOOK = 90;

	private Vector3f cameraRot;
	private Vector3f cameraPos;
	private Vector3f globalRot;
	private Vector3f globalPos;

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

	private PlayScreen screen;

	public Camera(PlayScreen screen)
	{
		LoadScreen.increaseLoadCount();

		cameraRot = new Vector3f(0, 0, 0);
		cameraPos = new Vector3f(0, 0, 0);
		globalRot = new Vector3f(30, 0, 0);
		globalPos = new Vector3f(0, -300, -1200);

		swing = false;
		matrix = new Matrix4f();

		this.screen = screen;
	}

	/**
	 * Rotate the world by both myself and the camera
	 */
	public void applyRotation()
	{
		if (screen.isGlobalView())
		{
			glRotatef(cameraRot.x, 1, 0, 0);
			glRotatef(cameraRot.y, 0, 1, 0);
		}
		else
		{
			Player player = screen.getCurrentPlayer();

			glRotatef(cameraRot.x, 1, 0, 0);
			glRotatef(cameraRot.y, 0, 1, 0);

			if (player == null)
			{
				return;
			}

			Vector3f playerRot = player.getRot();

			glRotatef(-playerRot.x - 30, 1, 0, 0);
			glRotatef(-playerRot.y, 0, 1, 0);
		}
	}

	public void rotate(float x, float y)
	{
		rotX(x);
		rotY(y);
	}

	public void rotate(Vector3f rot)
	{
		rotX(rot.x);
		rotY(rot.y);
	}

	public void setRotation(float x, float y, float z)
	{
		cameraRot.x = x;
		cameraRot.y = y;
		cameraRot.z = z;
	}

	public void setRotation(Vector3f rot)
	{
		cameraRot.x = rot.x;
		cameraRot.y = rot.y;
		cameraRot.z = rot.z;
	}

	/**
	 * Translate the world by the position of myself
	 */
	public void applyTranslation()
	{
		if (screen.isGlobalView())
		{
			glTranslatef(cameraPos.x, cameraPos.y, cameraPos.z);
		}
		else
		{
			Player player = screen.getCurrentPlayer();
			if (player == null)
			{
				glTranslatef(cameraPos.x, cameraPos.y, cameraPos.z);

				return;
			}
			Vector3f playerPos = player.getPos();

			glTranslatef(-playerPos.x, -playerPos.y, -playerPos.z);

			Vector3f playerRot = player.getRot();

			glRotatef(playerRot.y, 0, 1, 0);
			glRotatef(playerRot.x, 1, 0, 0);

			glTranslatef(0, -40, -30);

			glRotatef(-playerRot.x, 1, 0, 0);
			glRotatef(-playerRot.y, 0, 1, 0);
		}
	}

	public void translate(float x, float y, float z)
	{
		transX(x);
		transY(y);
		transZ(z);
	}

	public void translate(Vector3f pos)
	{
		transX(pos.x);
		transY(pos.y);
		transZ(pos.z);
	}

	public void setPosition(float x, float y, float z)
	{
		cameraPos.x = x;
		cameraPos.y = y;
		cameraPos.z = z;
	}

	public void setPosition(Vector3f pos)
	{
		cameraPos.x = pos.x;
		cameraPos.y = pos.y;
		cameraPos.z = pos.z;
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

	public void transX(float delta)
	{
		cameraPos.x += delta;
	}

	public void transY(float delta)
	{
		cameraPos.y += delta;
	}

	public void transZ(float delta)
	{
		cameraPos.z += delta;
	}

	/**
	 * Reset the camera to normal direction Used when the player hit a hill, we
	 * should let him/her know what he/she hit and what made him/her dead
	 */
	public void resetRot()
	{
		cameraRot.x = cameraRot.y = 0;
	}

	public void resetPos()
	{
		cameraPos.x = cameraPos.y = cameraPos.z = 0;
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

	public Vector3f getGlobalRot()
	{
		return globalRot;
	}

	public void setGlobalRot(Vector3f globalRot)
	{
		this.globalRot.x = globalRot.x;
		this.globalRot.y = globalRot.y;
		this.globalRot.z = globalRot.z;
	}

	public Vector3f getGlobalPos()
	{
		return globalPos;
	}

	public void setGlobalPos(Vector3f globalPos)
	{
		this.globalPos.x = globalPos.x;
		this.globalPos.y = globalPos.y;
		this.globalPos.z = globalPos.z;
	}

	public Vector3f getCameraRot()
	{
		return cameraRot;
	}

	public Vector3f getCameraPos()
	{
		return cameraPos;
	}

	/**
	 * Reset to the initial state when game begins
	 */
	public void reset()
	{
		cameraRot.set(0, 0, 0);
		cameraPos.set(0, 0, 0);
		globalRot.set(30, 0, 0);
		globalPos.set(0, -300, -1200);

		swing = false;
		matrix = new Matrix4f();
	}

}
