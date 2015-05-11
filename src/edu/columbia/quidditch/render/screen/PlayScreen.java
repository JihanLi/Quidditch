package edu.columbia.quidditch.render.screen;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Camera;
import edu.columbia.quidditch.interact.CameraAnimator;
import edu.columbia.quidditch.render.Model;
import edu.columbia.quidditch.render.Sky;
import edu.columbia.quidditch.render.Stadium;
import edu.columbia.quidditch.render.Terra;
import edu.columbia.quidditch.render.collisionobject.Character;

/**
 * Camera class
 * 
 * @author Yuqing Guan, Jihan Li
 * 
 */

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

	private static final Vector3f[] HOME_DOORS =
	{ new Vector3f(5, 171, -975), new Vector3f(-71, 97, -975),
			new Vector3f(79, 132, -975) };

	private static final Vector3f[] AWAY_DOORS =
	{ new Vector3f(0, 151, 990), new Vector3f(-75, 117, 990),
			new Vector3f(73, 74, 986) };

	private static final float DOOR_RADIUS = 16.0f;

	private FloatBuffer lightPosBuffer;

	private Camera camera;
	private boolean globalView = true;
	private boolean gameOn = true;
	private float velocity = 5;
	private CameraAnimator animator1;

	private Model sky, terra, stadium;
	private Character character;

	public PlayScreen(MainGame game)
	{
		super(game);

		camera = new Camera(game);
		camera.setPosition(camera.getGlobalPos());
		camera.setRotation(camera.getGlobalRot());

		animator1 = new CameraAnimator(1);

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

		glLightModel(GL_LIGHT_MODEL_AMBIENT, ambientBuffer);
		glLight(GL_LIGHT0, GL_AMBIENT, blackBuffer);
		glLight(GL_LIGHT0, GL_DIFFUSE, diffuseBuffer);
		glLight(GL_LIGHT0, GL_SPECULAR, specularBuffer);
	}

	@Override
	public void render()
	{
		camera.applyRotation();
		sky.render();
		camera.applyTranslation();

		glLight(GL_LIGHT0, GL_POSITION, lightPosBuffer);

		terra.render();
		stadium.render();
		character.render();

		glDisable(GL_LIGHTING);

		glColor4f(0, 0, 1, 0.5f);

		for (Vector3f door : HOME_DOORS)
		{
			glBegin(GL_QUADS);
			{
				glVertex3f(door.x - DOOR_RADIUS, door.y - DOOR_RADIUS, door.z);
				glVertex3f(door.x - DOOR_RADIUS, door.y + DOOR_RADIUS, door.z);
				glVertex3f(door.x + DOOR_RADIUS, door.y + DOOR_RADIUS, door.z);
				glVertex3f(door.x + DOOR_RADIUS, door.y - DOOR_RADIUS, door.z);
			}
			glEnd();
		}

		glColor4f(1, 0, 0, 0.5f);

		for (Vector3f door : AWAY_DOORS)
		{
			glBegin(GL_QUADS);
			{
				glVertex3f(door.x - DOOR_RADIUS, door.y - DOOR_RADIUS, door.z);
				glVertex3f(door.x - DOOR_RADIUS, door.y + DOOR_RADIUS, door.z);
				glVertex3f(door.x + DOOR_RADIUS, door.y + DOOR_RADIUS, door.z);
				glVertex3f(door.x + DOOR_RADIUS, door.y - DOOR_RADIUS, door.z);
			}
			glEnd();
		}

		glEnable(GL_LIGHTING);
	}

	@Override
	public boolean checkKeyboardInput(float delta)
	{
		boolean keyReleased = false;

		/*
		 * if(gameOn) { gameOn = animator1.animate(camera); return true; }
		 * 
		 * if(globalView) { if(Keyboard.isKeyDown(Keyboard.KEY_UP)) {
		 * if(camera.getCameraPos().z < 600) camera.translate(0, 0, velocity); }
		 * if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
		 * if(camera.getCameraPos().z > -1200) camera.translate(0, 0,
		 * -velocity); } }
		 */

		while (Keyboard.next())
		{
			if (!Keyboard.getEventKeyState())
			{
				switch (Keyboard.getEventKey())
				{
				case Keyboard.KEY_Q:
				case Keyboard.KEY_ESCAPE:
					game.requestReturn();
					break;
				case Keyboard.KEY_F2:
				case Keyboard.KEY_F12:
				case Keyboard.KEY_P:
					game.screenshot();
					break;
				case Keyboard.KEY_F11:
					game.toggleFullscreen();
					break;

				case Keyboard.KEY_C:
					globalView = false;
					camera.setRotation(character.getRotation());
					camera.setPosition(character.getPosition());
					break;
				case Keyboard.KEY_R:
					globalView = true;
					camera.setRotation(camera.getGlobalRot());
					camera.setPosition(camera.getGlobalPos());
					break;
				/*
				 * case Keyboard.KEY_UP: if(globalView == true) {
				 * camera.translate(0, 0, velocity); } break; case
				 * Keyboard.KEY_DOWN: if(globalView == true) {
				 * camera.translate(0, 0, -velocity); } break;
				 */
				}
			}
		}

		return keyReleased;
	}

	@Override
	public boolean checkMouseInput(float delta)
	{
		/*
		 * if (super.checkMouseInput(delta)) { return true; }
		 * 
		 * // Rotate the camera by mouse if (Display.isActive()) {
		 * if(Mouse.isInsideWindow() && Mouse.isButtonDown(0)) { flag = true; }
		 * 
		 * if(!Mouse.isButtonDown(0) && flag) { flag = false;
		 * camera.setRotation(character.getRotation());
		 * camera.setPosition(character.getPosition()); } }
		 */

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
