package edu.columbia.quidditch;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.SharedDrawable;
import org.lwjgl.util.glu.GLU;

import edu.columbia.quidditch.basic.Camera;
import edu.columbia.quidditch.interact.ButtonListener;
import edu.columbia.quidditch.interact.InputChecker;
import edu.columbia.quidditch.model.LoadingScreen;
import edu.columbia.quidditch.model.Modal;
import edu.columbia.quidditch.model.Model;
import edu.columbia.quidditch.model.Sky;
import edu.columbia.quidditch.model.Stadium;
import edu.columbia.quidditch.model.StartScreen;
import edu.columbia.quidditch.model.Terra;

/**
 * The main game class
 * 
 * @author Yuqing Guan
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

	// Statuses
	private static final int STATUS_START = 0;
	private static final int STATUS_RUNNING = 1;
	private static final int STATUS_END = 2;
	private static final int STATUS_LOADING = 3;

	private static final int BYTES_PER_PIXEL = 4;

	private static final float ALL_LOADING = 200f;
	
	private static MainGame singleton;
	
	public static void log(String text)
	{
		System.out.println(text);
		
		if (singleton == null)
		{
			return;
		}
		
		singleton.increaseLoadCount();
	}

	private boolean closeRequested, showModal;
	private long lastFrameTime;

	private Camera camera;
	private InputChecker inputChecker;

	private StartScreen startScreen;

	// Can be switched between windowed and fullscreen mode
	private DisplayMode windowed, fullscreen, current;

	// Models

	private FloatBuffer lightPosBuffer;

	// Current status
	private int status;

	// Display context, used for multi-threading
	private SharedDrawable drawable;

	private FloatBuffer projBuffer;

	private int loadCount;

	private Modal modal;
	private ButtonListener closeListener, cancelListener;

	private Model sky, terra, stadium;
	
	public MainGame()
	{
		singleton = this;
		
		status = STATUS_LOADING;
		loadCount = 0;
		closeRequested = showModal = false;
	}

	/**
	 * Create window, initialize icons and OpenGL, load models and then start
	 * the loop
	 */
	public void run()
	{
		createWindow();

		initGL();
		load();

		while (!closeRequested)
		{
			moveAndInput();

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glLoadIdentity();

			render3D();
			render2D();

			Display.update();
		}
	}

	/**
	 * Create an OpenGL window, defaultly windowed
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

		GL11.glViewport(0, 0, width, height);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();

		projBuffer = BufferUtils.createFloatBuffer(16);
		GLU.gluPerspective(45.0f * height / DEFAULT_HEIGHT,
				((float) width / (float) height), 0.1f, FAR);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projBuffer);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);

		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glClearDepth(1.0f);

		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glEnable(GL11.GL_POINT_SMOOTH);
		GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_NICEST);

		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

		GL11.glEnable(GL11.GL_LIGHT0);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);

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

	/**
	 * Use another thread to show the loading logs in the screen
	 */
	private void load()
	{
		try
		{
			LoadingScreen loadingScreen = new LoadingScreen(this);
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
		camera = new Camera(this);
		inputChecker = new InputChecker(this);
		startScreen = new StartScreen(this);

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
		
		sky = new Sky(this);
		terra = Terra.create(this);
		stadium = Stadium.create(this);
		
		status = STATUS_START;
	}

	/**
	 * Reset OpenGL when window is toggled between windowed and fullscreen mode
	 */
	private void resetGL()
	{
		int width = Display.getWidth();
		int height = Display.getHeight();

		GL11.glViewport(0, 0, width, height);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();

		projBuffer.rewind();
		GLU.gluPerspective(45.0f * height / DEFAULT_HEIGHT,
				((float) width / (float) height), 0.1f, FAR);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projBuffer);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	public void render3D()
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();

		GL11.glMultMatrix(projBuffer);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		camera.applyRotation();
		
		if (status == STATUS_RUNNING)
		{
			sky.render();
		}
		
		camera.applyTranslation();

		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, lightPosBuffer);
		
		if (status == STATUS_RUNNING)
		{
			terra.render();
			stadium.render();
		}
	}

	/**
	 * Render 2D buttons and tips
	 * 
	 * @param pick
	 */
	public void render2D()
	{
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();

		GL11.glDisable(GL11.GL_DEPTH_TEST);

		GL11.glDisable(GL11.GL_LIGHT0);
		GL11.glDisable(GL11.GL_LIGHTING);

		GLU.gluOrtho2D(0, MainGame.DEFAULT_WIDTH, 0, MainGame.DEFAULT_HEIGHT);

		switch (status)
		{
		case STATUS_START:
			startScreen.render();
			break;
		}
		
		if (showModal)
		{
			modal.render();
		}

		GL11.glEnable(GL11.GL_DEPTH_TEST);

		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glEnable(GL11.GL_LIGHTING);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	/**
	 * Move planes, myself and radar when playing the game Parse the inputs
	 */
	private void moveAndInput()
	{
		float delta = getDeltaTime();

		if (status == STATUS_RUNNING)
		{

		}

		inputChecker.checkKeyboard(delta);
		inputChecker.checkMouse(delta);

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
			camera.stopSwing();

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

	/**
	 * Take screenshot
	 */
	public void screenshot()
	{
		GL11.glReadBuffer(GL11.GL_FRONT);

		int width = Display.getWidth();
		int height = Display.getHeight();

		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height
				* BYTES_PER_PIXEL);
		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, buffer);

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

	public Camera getCamera()
	{
		return camera;
	}

	public boolean isLoading()
	{
		return status == STATUS_LOADING;
	}

	public void increaseLoadCount()
	{
		++loadCount;
	}

	public float getLoadPercentage()
	{
		return Math.min(Math.max(loadCount / ALL_LOADING, 0), 1);
	}

	public boolean isBeginning()
	{
		return status == STATUS_START;
	}

	public boolean isRunning()
	{
		return status == STATUS_RUNNING;
	}

	public boolean isEnded()
	{
		return status == STATUS_END;
	}

	public void startGame()
	{
		status = STATUS_RUNNING;
	}

	public void stopGame()
	{
		status = STATUS_END;
		camera.resetRot();
	}

	public void requestClose()
	{
		if (showModal)
		{
			return;
		}
		
		modal.setListeners(closeListener, cancelListener);
		showModal = true;
	}

	public static void main(String[] args)
	{
		new MainGame().run();
	}

	public StartScreen getStartScreen()
	{
		return startScreen;
	}
	
	public Modal getModal()
	{
		return modal;
	}
	
	public boolean isShowingModal()
	{
		return showModal;
	}
}
