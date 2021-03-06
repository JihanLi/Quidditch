package edu.columbia.quidditch;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.SharedDrawable;

import edu.columbia.quidditch.interact.ButtonListener;
import edu.columbia.quidditch.render.screen.LoadScreen;
import edu.columbia.quidditch.render.screen.Modal;
import edu.columbia.quidditch.render.screen.PlayScreen;
import edu.columbia.quidditch.render.screen.Screen;
import edu.columbia.quidditch.render.screen.StartScreen;
import edu.columbia.quidditch.render.screen.TeamScreen;

/**
 * The main game class
 * 
 * @author Jihan Li, Yuqing Guan, Yilin Xiong
 * 
 */
public class MainGame
{

	private static final String WINDOW_TITLE = "Quidditch World Cup";

	// Default width and height
	public static final int DEFAULT_WIDTH = 960;
	public static final int DEFAULT_HEIGHT = 540;

	// Default name of screenshot
	private static final String SCREENSHOT_PATH = "screenshot";
	private static final String SCREENSHOT_NAME = "pa5_";

	// Farthest distance
	private static final float FAR = 20000.0f;

	// Statuses
	private static final int STATUS_START = 0;
	private static final int STATUS_RUNNING = 1;
	private static final int STATUS_LOADING = 2;
	private static final int STATUS_EXIT = 3;
	private static final int STATUS_TEAM = 4;

	private static final int BYTES_PER_PIXEL = 4;

	private boolean closeRequested, showModal;
	private long lastFrameTime;

	private StartScreen startScreen;
	private TeamScreen teamScreen;

	// Can be switched between windowed and fullscreen mode
	private DisplayMode windowed, fullscreen, current;

	// Models

	// Current status
	private int status;

	// Display context, used for multi-threading
	private SharedDrawable drawable;

	private FloatBuffer projBuffer;

	private Modal modal;
	private ButtonListener closeListener, cancelListener, returnListener;

	private PlayScreen playScreen;

	public MainGame()
	{
		status = STATUS_LOADING;
		closeRequested = showModal = false;
	}

	/**
	 * Create window, initialize OpenGL, load models and render
	 */
	public void run()
	{
		createWindow();

		initGL();
		load();

		while (!closeRequested)
		{
			moveAndInput();

			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glLoadIdentity();

			render3D();
			render2D();

			Display.update();
		}
	}

	/**
	 * Create an OpenGL window with default size
	 */
	private void createWindow()
	{
		try
		{
			windowed = new DisplayMode(DEFAULT_WIDTH, DEFAULT_HEIGHT);
			fullscreen = Display.getDesktopDisplayMode();

			current = windowed;

			Display.setDisplayMode(current);

			Display.setVSyncEnabled(true);
			Display.setTitle(WINDOW_TITLE);
			Display.setResizable(false);

			Display.create();

			drawable = new SharedDrawable(Display.getDrawable());
		}
		catch (LWJGLException e)
		{
			Sys.alert("Error", "Initialization failed!\n\n" + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Initialize OpenGL
	 */
	private void initGL()
	{
		int width = Display.getWidth();
		int height = Display.getHeight();

		glViewport(0, 0, width, height);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		projBuffer = BufferUtils.createFloatBuffer(16);
		gluPerspective(45.0f * height / DEFAULT_HEIGHT,
				((float) width / (float) height), 0.1f, FAR);
		glGetFloat(GL_PROJECTION_MATRIX, projBuffer);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		glShadeModel(GL_SMOOTH);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);

		glEnable(GL_TEXTURE_2D);

		glEnable(GL_POINT_SMOOTH);
		glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);

		glEnable(GL_LINE_SMOOTH);
		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

		glEnable(GL_LIGHT0);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glEnable(GL_COLOR_MATERIAL);

	}

	/**
	 * Use another thread to show the loading logs in the screen
	 */
	private void load()
	{
		try
		{
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					status = STATUS_EXIT;
				}
			}));

			LoadScreen loadingScreen = new LoadScreen(this);
			Thread loadingThread = new Thread(loadingScreen);
			loadingThread.start();

			createModels();
			status = STATUS_START;

			loadingThread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Calculate how many milliseconds have passed since last frame.
	 * 
	 * @return milliseconds passed since last frame
	 */
	private int getDeltaTime()
	{
		long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
		int delta = (int) (time - lastFrameTime);

		lastFrameTime = time;

		return delta;
	}

	/**
	 * Create models for the game
	 */
	private void createModels()
	{
		startScreen = new StartScreen(this);
		teamScreen = new TeamScreen(this);

		modal = Modal.create(this);

		closeListener = new ButtonListener()
		{
			@Override
			public void onClick()
			{
				closeRequested = true;
			}
		};

		cancelListener = new ButtonListener()
		{
			@Override
			public void onClick()
			{
				showModal = false;
			}

		};

		returnListener = new ButtonListener()
		{
			@Override
			public void onClick()
			{
				playScreen.resetGame();
				status = STATUS_START;
				showModal = false;
			}
		};

		playScreen = new PlayScreen(this);

		status = STATUS_START;
	}

	/**
	 * Reset OpenGL when window is toggled between windowed and fullscreen mode
	 */
	private void resetGL()
	{
		int width = Display.getWidth();
		int height = Display.getHeight();

		glViewport(0, 0, width, height);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		projBuffer.rewind();
		gluPerspective(45.0f * height / DEFAULT_HEIGHT,
				((float) width / (float) height), 0.1f, FAR);
		glGetFloat(GL_PROJECTION_MATRIX, projBuffer);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}

	public void render3D()
	{
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		glMultMatrix(projBuffer);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		if (status == STATUS_RUNNING)
		{
			playScreen.render();
		}
	}

	/**
	 * Render 2D buttons and tips
	 * 
	 * @param pick
	 */
	public void render2D()
	{
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHTING);

		gluOrtho2D(0, MainGame.DEFAULT_WIDTH, 0, MainGame.DEFAULT_HEIGHT);

		switch (status)
		{
		case STATUS_START:
			startScreen.render();
			break;
		case STATUS_TEAM:
			teamScreen.render();
		}

		if (showModal)
		{
			modal.render();
		}

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_LIGHTING);

		glMatrixMode(GL_MODELVIEW);
	}

	/**
	 * Move planes, myself and radar when playing the game Parse the inputs
	 */
	private void moveAndInput()
	{
		float delta = getDeltaTime();

		Screen screen = getActiveScreen();
		if (screen != null)
		{
			screen.checkKeyboardInput(delta);
			screen.checkMouseInput(delta);
			screen.move(delta);
		}

		if (Display.isCloseRequested())
		{
			requestClose();
		}
	}

	/**
	 * Toggle fullscreen / windowed
	 * 
	 * @return whether the screen is changed successfully.
	 */
	public boolean toggleFullscreen()
	{
		try
		{
			if (status == STATUS_RUNNING)
			{
				playScreen.getCamera().stopSwing();
			}

			if (current == windowed)
			{
				current = fullscreen;
				Display.setDisplayModeAndFullscreen(current);
			}
			else
			{
				current = windowed;
				Display.setDisplayMode(current);
				Display.setFullscreen(false);
				Display.setResizable(false);
			}

			resetGL();
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Take screenshot
	 */
	public void screenshot()
	{
		glReadBuffer(GL_FRONT);

		int width = Display.getWidth();
		int height = Display.getHeight();

		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height
				* BYTES_PER_PIXEL);
		glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; ++x)
		{
			for (int y = 0; y < height; ++y)
			{
				int i = (x + (width * y)) * BYTES_PER_PIXEL;

				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;

				image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16)
						| (g << 8) | b);
			}
		}

		File dir = new File(SCREENSHOT_PATH);
		if (dir.isFile())
		{
			dir.delete();
		}
		dir.mkdirs();

		// Find the first index where there is no corresponding image file
		int idx = 0;
		File file;
		String filePath;
		do
		{
			filePath = SCREENSHOT_PATH + File.separator + SCREENSHOT_NAME
					+ (idx++) + ".png";
			file = new File(filePath);
		}
		while (file.exists());

		try
		{
			ImageIO.write(image, "png", file);
			System.out.println("Screenshot saved to " + filePath);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Get OpenGL context for multi-threading
	 * 
	 * @return
	 */
	public Drawable getDrawable()
	{
		return drawable;
	}

	public boolean isLoading()
	{
		return status == STATUS_LOADING;
	}

	public boolean isBeginning()
	{
		return status == STATUS_START;
	}

	public boolean isRunning()
	{
		return status == STATUS_RUNNING;
	}

	public void startGame()
	{
		status = STATUS_RUNNING;
	}

	public void chooseTeam()
	{
		status = STATUS_TEAM;
	}

	public void requestClose()
	{
		modal.setListener(0, closeListener);
		modal.setListener(1, cancelListener);
		modal.setModalInStart();
		showModal = true;
	}

	public void requestReturn()
	{
		modal.setListener(0, returnListener);
		modal.setListener(1, cancelListener);
		modal.setModalInRunning();
		showModal = true;
	}

	public Screen getActiveScreen()
	{
		if (showModal)
		{
			return modal;
		}
		else
		{
			switch (status)
			{
			case STATUS_START:
				return startScreen;
			case STATUS_TEAM:
				return teamScreen;
			case STATUS_RUNNING:
				return playScreen;
			default:
				return null;
			}
		}
	}

	public PlayScreen getPlayScreen()
	{
		return playScreen;
	}

	public void setPlayScreen(PlayScreen playScreen)
	{
		this.playScreen = playScreen;
	}

	/**
	 * Return to start screen
	 */
	public void stopGame()
	{
		status = STATUS_START;
	}

	public static void main(String[] args)
	{
		new MainGame().run();
	}
}
