package edu.columbia.quidditch.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.render.screen.LoadScreen;
import edu.columbia.quidditch.render.screen.PlayScreen;

/**
 * Radar with positions of all planes
 * 
 * @author Yuqing Guan
 * 
 */
public class Radar extends Model
{
	private static final Vector3f[] TEAM_COLORS =
	{ new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1),
			new Vector3f(1, 1, 0) };
	private static final Vector3f BALL_COLOR = new Vector3f(1, 1, 1);

	private static final int CIRCLE_SEG = 1000;

	private PlayScreen screen;

	public Radar(MainGame game, PlayScreen screen)
	{
		super(game);
		this.screen = screen;
	}

	/**
	 * Create two display lists
	 */
	@Override
	protected void createList()
	{
		LoadScreen.log("Creating display list for radar");

		list = glGenLists(1);
		glNewList(list, GL_COMPILE);
		{
			glColor4f(0, 0, 0, 0.5f);

			glBegin(GL_QUADS);
			{
				glVertex2d(800, 131.25f);
				glVertex2d(800, 408.75f);
				glVertex2d(905, 408.75f);
				glVertex2d(905, 131.25f);
			}
			glEnd();

			glColor3f(1, 1, 1);

			glBegin(GL_LINE_LOOP);
			{
				glVertex2d(800, 131.25f);
				glVertex2d(800, 408.75f);
				glVertex2d(905, 408.75f);
				glVertex2d(905, 131.25f);
			}
			glEnd();

			glBegin(GL_LINES);
			{
				glVertex2d(800, 270);
				glVertex2d(905, 270);
			}
			glEnd();

			glBegin(GL_LINE_LOOP);
			{
				for (int i = 0; i < CIRCLE_SEG; ++i)
				{
					float degree = (float) (Math.PI / CIRCLE_SEG * 2 * i);
					float sin = (float) Math.sin(degree);
					float cos = (float) Math.cos(degree);

					glVertex2f(26.25f * cos + 852.5f, 26.25f * sin + 270);
				}
			}
			glEnd();
		}
		glEndList();
	}

	@Override
	public void render()
	{
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();

		glLoadIdentity();

		glMatrixMode(GL_PROJECTION);
		glPushMatrix();

		glLoadIdentity();

		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHTING);

		gluOrtho2D(0, MainGame.DEFAULT_WIDTH, 0, MainGame.DEFAULT_HEIGHT);

		super.render();

		Vector3f[] computerPositions = screen.getComputerPositions();
		for (Vector3f pos : computerPositions)
		{
			pos.x = pos.x / 8 + 852.5f;
			pos.z = 270 - pos.z / 8;
		}
		
		Vector3f[] userPositions = screen.getUserPositions();
		for (Vector3f pos : userPositions)
		{
			pos.x = pos.x / 8 + 852.5f;
			pos.z = 270 - pos.z / 8;
		}
		
		glPointSize(12.0f);
		glColor3f(0, 0, 0);
		
		glBegin(GL_POINTS);
		{
			for (Vector3f pos : computerPositions)
			{
				glVertex2f(pos.x, pos.z);
			}
		}
		glEnd();

		glPointSize(10.0f);

		Vector3f color = TEAM_COLORS[screen.getTeamComputer()];
		glColor3f(color.x, color.y, color.z);

		glBegin(GL_POINTS);
		{
			for (Vector3f pos : computerPositions)
			{
				glVertex2f(pos.x, pos.z);
			}
		}
		glEnd();

		glPointSize(12.0f);
		glColor3f(0, 0, 0);
		
		glBegin(GL_POINTS);
		{
			for (Vector3f pos : userPositions)
			{
				glVertex2f(pos.x, pos.z);
			}
		}
		glEnd();

		glPointSize(10.0f);

		color = TEAM_COLORS[screen.getTeamUser()];
		glColor3f(color.x, color.y, color.z);

		glBegin(GL_POINTS);
		{
			for (Vector3f pos : userPositions)
			{
				glVertex2f(pos.x, pos.z);
			}
		}
		glEnd();

		Vector3f pos = screen.getBallPosition();
		
		pos.x = pos.x / 8 + 852.5f;
		pos.z = 270 - pos.z / 8;
		
		glPointSize(10.0f);
		glColor3f(0, 0, 0);

		glBegin(GL_POINTS);
		{
			glVertex2f(pos.x, pos.z);
		}
		glEnd();

		glPointSize(8.0f);
		
		color = BALL_COLOR;
		glColor3f(color.x, color.y, color.z);

		glBegin(GL_POINTS);
		{
			glVertex2f(pos.x, pos.z);
		}
		glEnd();

		glPointSize(1.0f);

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_LIGHTING);

		glPopMatrix();

		glMatrixMode(GL_MODELVIEW);

		glPopMatrix();
	}
}
