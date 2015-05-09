package edu.columbia.quidditch.render.screen;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Camera;
import edu.columbia.quidditch.render.Character;
import edu.columbia.quidditch.render.Model;
import edu.columbia.quidditch.render.Sky;
import edu.columbia.quidditch.render.Stadium;
import edu.columbia.quidditch.render.Terra;

public class PlayScreen extends Screen
{
	private static final float MOUSE_SENSITIVITY = 0.05f;
	
	// Position of light source
	private static final float[] LIGHT_POS =
	{ 3.73f, 5.0f, -1.0f, 0.0f };

	// Color of light source
	private static final float[] AMBIENT =
	{ 0.4f, 0.4f, 0.4f, 1.0f };
	private static final float[] BLACK =
	{ 0.0f, 0.0f, 0.0f, 1.0f };
	private static final float[] DIFFUSE =
	{ 1.0f, 1.0f, 1.0f, 1.0f };
	private static final float[] SPECULAR =
	{ 1.0f, 1.0f, 1.0f, 1.0f };

	private FloatBuffer lightPosBuffer;

	private Camera camera;
	private Model sky, terra, stadium;
	private Character character;

	public PlayScreen(MainGame game)
	{
		super(game);

		camera = new Camera(game);

		sky = new Sky(game);
		terra = Terra.create(game);
		stadium = Stadium.create(game);
		character = new Character(game);

		children.add(sky);
		children.add(terra);
		children.add(stadium);

		lightPosBuffer = floats2Buffer(LIGHT_POS);

		FloatBuffer ambientBuffer = floats2Buffer(AMBIENT);
		FloatBuffer blackBuffer = floats2Buffer(BLACK);
		FloatBuffer diffuseBuffer = floats2Buffer(DIFFUSE);
		FloatBuffer specularBuffer = floats2Buffer(SPECULAR);

		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, ambientBuffer);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, blackBuffer);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, diffuseBuffer);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, specularBuffer);
	}

	@Override
	public void render()
	{
		camera.applyRotation();
		sky.render();
		camera.applyTranslation();

		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPosBuffer);

		terra.render();
		stadium.render();
		character.render();
	}

	@Override
	public boolean checkKeyboardInput(float delta)
	{
		boolean keyReleased = false;
		
		while (Keyboard.next())
		{
			if (!Keyboard.getEventKeyState())
			{
				switch (Keyboard.getEventKey())
				{
				case Keyboard.KEY_Q:
				case Keyboard.KEY_ESCAPE:
					game.requestClose();
					break;
				case Keyboard.KEY_F2:
				case Keyboard.KEY_F12:
				case Keyboard.KEY_P:
					game.screenshot();
					break;
				case Keyboard.KEY_F11:
					game.toggleFullscreen();
					break;
				}
			}
		}
		
		return keyReleased;
	}

	@Override
	public boolean checkMouseInput(float delta)
	{
		if (super.checkMouseInput(delta))
		{
			return true;
		}

		// Rotate the camera by mouse
		if (Display.isActive())
		{
			if (camera.isSwinging())
			{
				float mouseDX = Mouse.getDX();
				float mouseDY = -Mouse.getDY();

				camera.rotY(mouseDX * MOUSE_SENSITIVITY * delta);
				camera.rotX(mouseDY * MOUSE_SENSITIVITY * delta);
			}
			else
			{
				Mouse.getDX();
				Mouse.getDY();

				camera.startSwing();
			}
		}
		else
		{
			camera.stopSwing();
		}

		return false;
	}

	@Override
	protected void createList()
	{

	}

	public Camera getCamera()
	{
		return camera;
	}

	/**
	 * Convert float array to buffer, mainly used for light source
	 * 
	 * @param float array
	 * @return converted float buffer
	 */
	private FloatBuffer floats2Buffer(float[] floats)
	{
		FloatBuffer buffer = BufferUtils.createFloatBuffer(floats.length);
		buffer.put(floats).flip();
		return buffer;
	}
}
