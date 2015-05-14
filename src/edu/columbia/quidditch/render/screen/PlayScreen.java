package edu.columbia.quidditch.render.screen;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Camera;
import edu.columbia.quidditch.basic.Fonts;
import edu.columbia.quidditch.interact.CameraAnimator;
import edu.columbia.quidditch.interact.ModelAnimator;
import edu.columbia.quidditch.render.Model;
import edu.columbia.quidditch.render.Sky;
import edu.columbia.quidditch.render.Stadium;
import edu.columbia.quidditch.render.Terra;
import edu.columbia.quidditch.render.collisionobject.Ball;
import edu.columbia.quidditch.render.collisionobject.Player;

/**
 * Playscreen class
 * 
 * @author Yuqing Guan, Jihan Li, Yilin Xiong
 * 
 */

public class PlayScreen extends Screen
{
	public static final float LONG_AXIS = 1100;
	public static final float SHORT_AXIS = 420;

	public static final float TOP = 250.0f;
	public static final float BOTTOM = -50.0f;

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
			new Vector3f(73, 74, 990) };

	private static final float DOOR_RADIUS = 16.0f;
	private static final float DOOR_EXT_RADIUS = 19.0f;

	private FloatBuffer lightPosBuffer;

	private Camera camera;
	private boolean globalView = true;
	private boolean gameOn = true;
	private boolean gameOff = false;
	private float offset = 500;
	private CameraAnimator startAnimator, winAnimator, loseAnimator, drawAnimator;
	private int score1, score2;
	private ModelAnimator shootAnimator1, shootAnimator2;
	private boolean animate1 = false, animate2 = false;
	private int count = 0;

	private Model sky, terra, stadium;
	private Player currentPlayer;
	private int currentIndex;
	
	private int numberOfMember = 3;
	private String[] teamName = {"Gryffindor", "Slytherin", "Ravenclaw", "Hufflepuff"};
	private int teamUser = 0;
	private int teamComputer = 1;
	private ArrayList<Player> playersUser = new ArrayList<Player>();
	private ArrayList<Player> playersComputer = new ArrayList<Player>();
	private ArrayList<Player> players = new ArrayList<Player>();
	
	private Ball ball;

	public PlayScreen(MainGame game)
	{
		super(game);

		camera = new Camera(game, this);
		camera.setPosition(camera.getGlobalPos());
		camera.setRotation(camera.getGlobalRot());

		startAnimator = new CameraAnimator(1);
		winAnimator = new CameraAnimator(2);
		drawAnimator = new CameraAnimator(3);
		loseAnimator = new CameraAnimator(4);
		
		setScore1(0);
		setScore2(0);

		sky = new Sky(game);
		terra = Terra.create(game);
		stadium = Stadium.create(game);
		
		
		for (int i = 0; i < numberOfMember; i++) {
			playersUser.add(new Player(game, this, teamUser, true, new Vector3f(100 - i * 100, 0, 200)));
			playersComputer.add(new Player(game, this, teamComputer, false, new Vector3f(100 - i * 100, 0, -200)));
		}
		
		players.addAll(playersUser);
		players.addAll(playersComputer);
		
		ball = new Ball(game, this, 0, new Vector3f(0, 200, 0));

		currentIndex = 0;
		currentPlayer = playersUser.get(currentIndex);
		
		shootAnimator1 = new ModelAnimator(1, ball);
		shootAnimator2 = new ModelAnimator(2, ball);

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
		children.add(ball);
		
		for (int i = 0; i < numberOfMember; i++) {
			children.add(playersUser.get(i));
			children.add(playersComputer.get(i));
		}
	}

	public Player getCurrentPlayer()
	{
		return currentPlayer;
	}

	@Override
	public void render()
	{
		if(gameOn) 
		{ 
			gameOn = startAnimator.animate(camera); 
			if(!gameOn)
				camera.setRotation(30, 0, 0);
		}
		
		if(gameOff) 
		{ 
			if(score1 > score2)
			{
				gameOff = winAnimator.animate(camera); 
			}
			else if(score1 == score2)
			{
				gameOff = drawAnimator.animate(camera); 
			}
			else
			{
				gameOff = loseAnimator.animate(camera); 
			}
			
			if(!gameOff)
			{
				resetGame();
				game.terminate();
			}
		}
		
		if(animate1)
		{
			animate1 = shootAnimator1.animate(); 
			if(!animate1)
			{
				shootAnimator1.reset();
				ball.setHolder(playersComputer.get(0));
				score1 += 10;
			}
		}
		
		if(animate2)
		{
			animate2 = shootAnimator2.animate(); 
			if(!animate2)
			{
				shootAnimator2.reset();
				ball.setHolder(playersUser.get(0));
				score2 += 10;
			}
		}
		
		camera.applyRotation();
		sky.render();
		camera.applyTranslation();

		glLight(GL_LIGHT0, GL_POSITION, lightPosBuffer);

		super.render();

		glDisable(GL_LIGHTING);
		
		if(!gameOn && !gameOff)
		{
			Fonts.draw(120 , 500, teamName[teamUser] + "(You): " + score1, "Times New Roman", Color.white, 20);
			Fonts.draw(800 , 500, teamName[teamComputer] + "(Computer): " + score2, "Times New Roman", Color.white, 20);
		}
		
		if(ball.isHold() && ball.getHolder().equals(currentPlayer))
		{
			if(ball.checkScope(new Vector3f(0, 85.5f, -975f), 400) && ball.getPos().z > -975f)
			{
				count++;
				if(count < 20)
					Fonts.draw(500 , 50, "Press Enter to Shoot the Gate!", "Castellar", Color.yellow, 20);
				if(count == 60)
					count = 0;
			}
		}
		
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
		
		if (gameOn || gameOff)
		{
			return true;
		}
		 
		checkPlayer();
		
		if (globalView) 
		{ 
			if(camera.getCameraPos().z >= -1400 && camera.getCameraPos().z <= 400) 
			{
				if(!ball.isHold())
				{
					camera.setPosition(camera.getGlobalPos().x, camera.getGlobalPos().y, -(ball.getPos().z + offset)); 
				}
				else
				{
					camera.setPosition(camera.getGlobalPos().x, camera.getGlobalPos().y, -(currentPlayer.getPos().z + offset)); 
				}
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
		
		if(animate1 || animate2)
		{
			return true;
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
					globalView = !globalView;
					break;
				case Keyboard.KEY_N:
					animate2 = true;
					ball.getHolder().handDown();
					ball.clearHolder();
					shootAnimator2.initiate();
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
		boolean keyThrow = Keyboard.isKeyDown(Keyboard.KEY_RETURN);
		
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
			currentPlayer.moveY(1, delta);
		}

		// Turn down myself
		if (keyDown)
		{
			currentPlayer.moveY(-1, delta);
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
		
		// Throw the ball
		if(keyThrow)
		{
			if(ball.isHold() && ball.getHolder().equals(currentPlayer))
			{
				currentPlayer.handDown();
				if(ball.checkScope(new Vector3f(0, 85.5f, -975f), 400) && ball.getPos().z > -975f)
				{
					animate1 = true;
					ball.clearHolder();
					shootAnimator1.initiate();
				}
				else
				{
					Vector3f vel = currentPlayer.getVelocity();
					Vector3f rot = currentPlayer.getRot();
					float mod = vel.length();
					if(vel.x == 0 && vel.y == 0 && vel.z == 0)
						ball.setVelocity(2*rot.x, 2*rot.y, 2*rot.z);
					else
						ball.setVelocity(0.8f*vel.x/mod, 0.8f*vel.y/mod, 0.8f*vel.z/mod);
					ball.clearHolder();
				}
			}
			else
			{
				currentPlayer.handUp();
			}
		}
		else
		{
			if(!ball.isHold() || !ball.getHolder().equals(currentPlayer))
			{
				currentPlayer.handDown();
			}
			else
			{
				currentPlayer.handUp();
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
	
	public void checkCollision()
	{
		for(int i = 0; i < 3; i++)
		{
			if(!ball.isHold())
			{
				if(ball.checkCollision(AWAY_DOORS[i], DOOR_RADIUS))
				{
					score1 += 10;
				}
				if(ball.checkCollision(HOME_DOORS[i], DOOR_RADIUS))
				{
					score2 += 10;
				}
			}
		}
		
		
		for(int i = 0; i < players.size(); i++)
		{
			Player tempPlayer1 = players.get(i);
			boolean currentCollision = false;
			
			if(!ball.isHold())
			{
				if(ball.checkCollision(tempPlayer1))
				{
					ball.setHolder(tempPlayer1);
				}
			}
			
			for(int j = i+1; j < players.size(); j++)
			{
				Player tempPlayer2 = players.get(j);

				if(tempPlayer1.checkCollision(tempPlayer2))
				{
					setVAfterCollision(tempPlayer1, tempPlayer2);
					if (!ball.isHold())
					{
						tempPlayer1.fall();
						tempPlayer2.fall();
						continue;
					}
					
					if(ball.getHolder().equals(tempPlayer1))
					{
						if (!ball.isHolderCollided())
						{
							ball.setHolder(tempPlayer2);
							tempPlayer2.handUp();
							tempPlayer1.handDown();
						}

						tempPlayer1.setBasedOnV();
						tempPlayer2.setBasedOnV();
						currentCollision = true;
					}
					else if(ball.getHolder().equals(tempPlayer2))
					{
						if (!ball.isHolderCollided())
						{
							ball.setHolder(tempPlayer1);
							tempPlayer1.handUp();
							tempPlayer2.handDown();
						}

						tempPlayer1.setBasedOnV();
						tempPlayer2.setBasedOnV();
						currentCollision = true;
					}
					else
					{
						tempPlayer1.fall();
						tempPlayer2.fall();
					}
				}
			}
			
			ball.setHolderCollided(currentCollision);
		}
	}
		
	
	@Override
	public void move(float delta)
	{
		if (gameOn) 
		{
			return;
		}
		
		for (int i = 0; i < numberOfMember; i++) 
		{	
			playersUser.get(i).move(delta);
			playersComputer.get(i).move(delta);
		}
		
		checkCollision();
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
	
	public boolean isGameOff() {
		return gameOff;
	}

	public void setGameOff(boolean gameOff) {
		this.gameOff = gameOff;
	}
	
	public void resetGame() {
		camera.reset();
		ball.reset();
		for (int i = 0; i < numberOfMember; i++) {
			playersUser.get(i).reset();
			playersComputer.get(i).reset();
		}
		
		camera.setPosition(camera.getGlobalPos());
		camera.setRotation(camera.getGlobalRot());

		currentIndex = 0;
		currentPlayer = playersUser.get(currentIndex);
		
		startAnimator = new CameraAnimator(1);
		winAnimator = new CameraAnimator(2);
		drawAnimator = new CameraAnimator(3);
		loseAnimator = new CameraAnimator(4);
		
		shootAnimator1 = new ModelAnimator(1, ball);
		shootAnimator2 = new ModelAnimator(2, ball);
		
		setScore1(0);
		setScore2(0);
		gameOn = true;
		gameOff = false;
		animate1 = false;
		animate2 = false;
		globalView = true;
	}
	
	private void checkPlayer() {
		
		if (ball.isHold()) 
		{
			for (int i = 0; i < numberOfMember; i++)
			{
				if (ball.getHolder().equals(playersUser.get(i)))
				{
					currentIndex = i;
					currentPlayer = playersUser.get(i);
					return;
				}
			}
		}
		
		if (currentPlayer.distance(ball) < 200)
		{
			return;
		}
		
		float max = 30000f;
		for (int i = 0; i < numberOfMember; i++)
		{
			float dis = playersUser.get(i).distance(ball);
			if (dis < max)
			{
				max = dis;
				currentIndex = i;
			}
		}
		currentPlayer = playersUser.get(currentIndex);
	}

	public int getScore1() {
		return score1;
	}

	public void setScore1(int score1) {
		this.score1 = score1;
	}

	public int getScore2() {
		return score2;
	}

	public void setScore2(int score2) {
		this.score2 = score2;
	}
	
	private void setVAfterCollision(Player player1, Player player2)
	{	
		Vector3f v1 = new Vector3f(player1.getVelocity());
		Vector3f v2 = new Vector3f(player2.getVelocity());
		float alpha = 0.5f;
		
		// Get the normal vector of the collision.
		Vector3f dv = Vector3f.sub(v2, v1, null);
		Vector3f norm = dv.normalise(null);
		
		float factor = Vector3f.dot(dv, norm);
		norm.scale(factor * alpha);
		v1 = Vector3f.add(v1, norm, v1);
		v2 = Vector3f.sub(v2, norm, v2);
		player1.setVelocity(v1);
		player2.setVelocity(v2);
	}

	public int getTeamUser() {
		return teamUser;
	}

	public void setTeamUser(int teamUser) {
		this.teamUser = teamUser;
		for (Player player : playersUser)
		{
			player.setTeam(teamUser);
		}
	}

	public int getTeamComputer() {
		return teamComputer;
	}

	public void setTeamComputer(int teamComputer) {
		this.teamComputer = teamComputer;
		for (Player player : playersComputer)
		{
			player.setTeam(teamComputer);
		}
	}
}
