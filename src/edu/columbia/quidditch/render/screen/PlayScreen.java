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
import edu.columbia.quidditch.render.collisionobject.Ball;
import edu.columbia.quidditch.render.collisionobject.Player;

/**
 * Camera class
 * 
 * @author Yuqing Guan, Jihan Li
 * 
 */

public class PlayScreen extends Screen
{
	public static final float LONG_AXIS = 1100;
	public static final float SHORT_AXIS = 420;

	public static final float TOP = 2500;
	public static final float BOTTOM = -50;

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
	private static final float DOOR_EXT_RADIUS = 19.0f;

	private FloatBuffer lightPosBuffer;

	private Camera camera;
	private boolean globalView = true;
	private boolean gameOn = true;
	private float velocity = 5;
	private float offset = 500;
	private CameraAnimator animator1;

	private Model sky, terra, stadium;
	private Player player, currentPlayer;
	private Ball ball;

	public PlayScreen(MainGame game)
	{
		super(game);

		camera = new Camera(game, this);
		camera.setPosition(camera.getGlobalPos());
		camera.setRotation(camera.getGlobalRot());

		animator1 = new CameraAnimator(1);

		sky = new Sky(game);
		terra = Terra.create(game);
		stadium = Stadium.create(game);
		player = new Player(game, this);
		
		ball = new Ball(game, this, 0);
		ball.setPos(new Vector3f(0, 300, -500));

		currentPlayer = player;

		lightPosBuffer = floats2Buffer(LIGHT_POS);

		FloatBuffer ambientBuffer = floats2Buffer(AMBIENT);
		FloatBuffer blackBuffer = floats2Buffer(BLACK);
		FloatBuffer diffuseBuffer = floats2Buffer(DIFFUSE);
		FloatBuffer specularBuffer = floats2Buffer(SPECULAR);

		glLightModel(GL_LIGHT_MODEL_AMBIENT, ambientBuffer);
		glLight(GL_LIGHT0, GL_AMBIENT, blackBuffer);
		glLight(GL_LIGHT0, GL_DIFFUSE, diffuseBuffer);
		glLight(GL_LIGHT0, GL_SPECULAR, specularBuffer);
		
		children.add(terra);
		children.add(stadium);
		children.add(player);
		children.add(ball);
	}

	public Player getCurrentPlayer()
	{
		return currentPlayer;
	}

	@Override
	public void render()
	{
		camera.applyRotation();
		sky.render();
		camera.applyTranslation();

		glLight(GL_LIGHT0, GL_POSITION, lightPosBuffer);

		super.render();

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

		
		if(gameOn) 
		{ 
			gameOn = animator1.animate(camera); 
			if(!gameOn)
				camera.setRotation(30, 0, 0);
			return true; 
		}
		 
		if(globalView) 
		{ 
			if(camera.getCameraPos().z >= -1400 && camera.getCameraPos().z <= 400) 
			{
				camera.setPosition(camera.getGlobalPos().x, camera.getGlobalPos().y, -(currentPlayer.getPos().z + offset)); 
			}
			
			if(camera.getCameraPos().z < -1400)
			{
				camera.getCameraPos().z = -1400;
			}
			else if(camera.getCameraPos().z > 400)
			{
				camera.getCameraPos().z = 400;
			}
		}
		
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
					//camera.setRotation(player.getRot());
					//camera.setPosition(player.getPos());
					break;
				case Keyboard.KEY_R:
					globalView = true;
					//camera.setRotation(camera.getGlobalRot());
					//camera.setPosition(camera.getGlobalPos());
					break;
				}
			}
		}

		if (keyReleased)
		{
			return true;
		}
		
		if (currentPlayer == null)
		{
			return false;
		}

		if (!currentPlayer.isControllable())
		{
			return true;
		}
		//int wheel = Mouse.getDWheel();

		boolean keyForward = Keyboard.isKeyDown(Keyboard.KEY_W)
				|| Keyboard.isKeyDown(Keyboard.KEY_UP)/* || wheel > 0*/;
		boolean keyBack = Keyboard.isKeyDown(Keyboard.KEY_S)
				|| Keyboard.isKeyDown(Keyboard.KEY_DOWN)/* || wheel < 0*/;

		boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_A)
				|| Keyboard.isKeyDown(Keyboard.KEY_LEFT);
		boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_D)
				|| Keyboard.isKeyDown(Keyboard.KEY_RIGHT);

		boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
		boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

		boolean keyReset = Keyboard.isKeyDown(Keyboard.KEY_R);
		
		keyReleased = keyForward || keyBack ||  keyLeft || keyRight || keyUp || keyDown || keyReset;

		// Turn left myself
		if (keyLeft)
		{
			currentPlayer.rotY(1, delta);
		}

		// Turn right myself
		if (keyRight)
		{
			currentPlayer.rotY(-1, delta);
		}

		// Turn up myself
		if (keyUp)
		{
			currentPlayer.rotX(1, delta);
		}

		// Turn down myself
		if (keyDown)
		{
			currentPlayer.rotX(-1, delta);
		}

		// Reset myself to a horizontal direction
		if (keyReset)
		{
			currentPlayer.resetRotX();
		}

		// Accelerate
		if (keyForward)
		{
			currentPlayer.accelerate();
		}

		// Decelerate
		if (keyBack)
		{
			currentPlayer.decelerate();
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

		/*// Rotate the camera by mouse
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
		}*/

		return false;
	}
	
	@Override
	public void move(float delta)
	{
		player.move(delta);
		ball.move(delta);
	}

	@Override
	protected void createList()
	{
		list = glGenLists(1);
		glNewList(list, GL_COMPILE);
		glEndList();
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

	public boolean isGlobalView() {
		return globalView;
	}

	public void setGlobalView(boolean globalView) {
		this.globalView = globalView;
	}

	public boolean isGameOn() {
		return gameOn;
	}

	public void setGameOn(boolean gameOn) {
		this.gameOn = gameOn;
	}
}
