package edu.columbia.quidditch.model;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Texture;

/**
 * Loading screen, executed in a separated new thread
 * 
 * @author Yuqing Guan
 * 
 */
public class LoadingScreen extends Model implements Runnable
{
	private static final long INTERVAL = 100;
	private Texture bg, barBg, barContent;
	private Texture emblem;

	public LoadingScreen(MainGame game)
	{
		super(game);

		bg = Texture.createFromFile("res/loading/loadScreen.png");
		emblem = Texture.createFromFile("res/hogwarts1.png");
		barBg = Texture.createFromFile("res/loading/progress.png");
		barContent = Texture.createFromFile("res/loading/bar.png");
	}

	@Override
	protected void createList()
	{
		list = GL11.glGenLists(1);

		GL11.glNewList(list, GL11.GL_COMPILE);
		{
			bg.drawRectangle(0, 0, 960, 540);
			emblem.drawRectangle(330, 190, 300, 300);
			barBg.drawRectangle(98, 77, 764, 58);
		}
		GL11.glEndList();
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

		GL11.glCallList(list);

		barContent.bind();
		float percentage = game.getLoadPercentage();

		GL11.glBegin(GL11.GL_QUADS);
		{
			GL11.glTexCoord2d(0, 1);
			GL11.glVertex2d(120, 92);

			GL11.glTexCoord2d(0, 0);
			GL11.glVertex2d(120, 100);

			GL11.glTexCoord2d(percentage, 0);
			GL11.glVertex2d(120 + 720 * percentage, 100);

			GL11.glTexCoord2d(percentage, 1);
			GL11.glVertex2d(120 + 720 * percentage, 92);
		}
		GL11.glEnd();

		Texture.unbind();
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

		GL11.glViewport(0, 0, width, height);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glClearDepth(1.0f);

		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glEnable(GL11.GL_POINT_SMOOTH);
		GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_NICEST);

		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
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

			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GLU.gluOrtho2D(0, MainGame.DEFAULT_WIDTH, 0, MainGame.DEFAULT_HEIGHT);

			// If all models are loaded, the loop will be terminated
			while (game.isLoading())
			{
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT
						| GL11.GL_DEPTH_BUFFER_BIT);

				GL11.glDisable(GL11.GL_DEPTH_TEST);
				
				render();
				
				GL11.glEnable(GL11.GL_DEPTH_TEST);

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
}
