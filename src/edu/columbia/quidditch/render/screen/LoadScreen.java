package edu.columbia.quidditch.render.screen;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.Drawable;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;
import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Fonts;
import edu.columbia.quidditch.basic.Texture;

/**
 * Loading screen, executed in a separated new thread
 * 
 * @author Yilin Xiong
 * 
 */
public class LoadScreen extends Screen implements Runnable
{
	private static final float ALL_LOAD_COUNT = 200;
	
	private static final long INTERVAL = 100;
	
	private static LoadScreen singleton = null;
		
	public static void log(String text)
	{
		System.out.println(text);
		
		if (singleton == null)
		{
			return;
		}
		
		singleton.lastLog = text;
		++singleton.loadCount;
	}
	
	public static void increaseLoadCount()
	{
		if (singleton == null)
		{
			return;
		}
		
		++singleton.loadCount;
	}
	
	private String lastLog;
	private int loadCount;	
	
	private Texture bg, barBg, barContent, emblem;
	
	public LoadScreen(MainGame game)
	{
		super(game);
		
		loadCount = 1;
		lastLog = "";
		singleton = this;

		bg = Texture.createFromFile("res/loading/loadScreen.png");
		emblem = Texture.createFromFile("res/loading/hogwarts.png");
		barBg = Texture.createFromFile("res/loading/progress.png");
		barContent = Texture.createFromFile("res/loading/bar.png");
	}

	@Override
	protected void createList()
	{
		list = glGenLists(1);

		glNewList(list, GL_COMPILE);
		{
			bg.drawRectangle(0, 0, 960, 540);
			emblem.drawRectangle(330, 190, 300, 300);
			barBg.drawRectangle(98, 77, 764, 58);
		}
		glEndList();
	}

	/**
	 * Print logs on screen
	 */
	@Override
	public void render()
	{
		if (list == NO_LIST)
		{
			createList();
		}

		glCallList(list);

		barContent.bind();
		float percentage = loadCount / ALL_LOAD_COUNT;

		glBegin(GL_QUADS);
		{
			glTexCoord2d(0, 1);
			glVertex2d(120, 92);

			glTexCoord2d(0, 0);
			glVertex2d(120, 100);

			glTexCoord2d(percentage, 0);
			glVertex2d(120 + 720 * percentage, 100);

			glTexCoord2d(percentage, 1);
			glVertex2d(120 + 720 * percentage, 92);
		}
		glEnd();

		Texture.unbind();
		
		Fonts.draw(480, 150, lastLog, 16);
	}

	/**
	 * Initialize OpenGL like the main thread
	 * 
	 * @throws LWJGLException
	 */
	private void initGL() throws LWJGLException
	{
		Drawable drawable = game.getDrawable();
		drawable.makeCurrent();

		int width = Display.getWidth();
		int height = Display.getHeight();

		glViewport(0, 0, width, height);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		glShadeModel(GL_SMOOTH);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);

		glEnable(GL_TEXTURE_2D);

		glEnable(GL_POINT_SMOOTH);
		glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);

		glEnable(GL_LINE_SMOOTH);
		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	/**
	 * The main thread to print logs on the OpenGL window
	 */
	@Override
	public void run()
	{
		try
		{
			initGL();

			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			gluOrtho2D(0, MainGame.DEFAULT_WIDTH, 0, MainGame.DEFAULT_HEIGHT);

			// If all models are loaded, the loop will be terminated
			while (game.isLoading())
			{
				glClear(GL_COLOR_BUFFER_BIT
						| GL_DEPTH_BUFFER_BIT);

				glDisable(GL_DEPTH_TEST);
				
				render();
				
				glEnable(GL_DEPTH_TEST);

				Display.update();
			}

			Thread.sleep(INTERVAL);
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public boolean checkKeyboardInput(float delta)
	{
		return false;
	}
}
