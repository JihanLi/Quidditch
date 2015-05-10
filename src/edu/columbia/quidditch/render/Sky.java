package edu.columbia.quidditch.render;

import static org.lwjgl.opengl.GL11.*;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.render.screen.LoadScreen;

/**
 * Sky
 * 
 * @author Yuqing Guan
 * 
 */
public class Sky extends Model
{
	private static final String SKY_TNAME = "res/sky/bgt.jpg";
	private static final String SKY_BONAME = "res/sky/bgbo.jpg";
	private static final String SKY_FNAME = "res/sky/bgf.jpg";
	private static final String SKY_BANAME = "res/sky/bgba.jpg";
	private static final String SKY_LNAME = "res/sky/bgl.jpg";
	private static final String SKY_RNAME = "res/sky/bgr.jpg";

	private static final int SKY_SIZE = 5000;

	private Texture[] skies;

	/**
	 * Load six textures from file and create display list
	 * 
	 * @param game
	 */
	public Sky(MainGame game)
	{
		super(game);

		skies = new Texture[6];

		skies[0] = Texture.createFromFile(SKY_TNAME);
		skies[1] = Texture.createFromFile(SKY_BONAME);
		skies[2] = Texture.createFromFile(SKY_FNAME);
		skies[3] = Texture.createFromFile(SKY_BANAME);
		skies[4] = Texture.createFromFile(SKY_LNAME);
		skies[5] = Texture.createFromFile(SKY_RNAME);

		createList();
	}

	/**
	 * Use textures to build a sky box
	 */
	@Override
	protected void createList()
	{
		LoadScreen.log("Creating display list for sky");

		list = glGenLists(1);
		glNewList(list, GL_COMPILE);
		{
			// Top
			skies[0].bind();

			glBegin(GL_QUADS);
			{
				glTexCoord2f(1.0f, 1.0f);
				glVertex3i(SKY_SIZE, SKY_SIZE, -SKY_SIZE);
				glTexCoord2f(0.0f, 1.0f);
				glVertex3i(-SKY_SIZE, SKY_SIZE, -SKY_SIZE);
				glTexCoord2f(0.0f, 0.0f);
				glVertex3i(-SKY_SIZE, SKY_SIZE, SKY_SIZE);
				glTexCoord2f(1.0f, 0.0f);
				glVertex3i(SKY_SIZE, SKY_SIZE, SKY_SIZE);
			}
			glEnd();

			// Bottom
			skies[1].bind();

			glBegin(GL_QUADS);
			{
				glTexCoord2f(1.0f, 1.0f);
				glVertex3i(SKY_SIZE, -SKY_SIZE, SKY_SIZE);
				glTexCoord2f(0.0f, 1.0f);
				glVertex3i(-SKY_SIZE, -SKY_SIZE, SKY_SIZE);
				glTexCoord2f(0.0f, 0.0f);
				glVertex3i(-SKY_SIZE, -SKY_SIZE, -SKY_SIZE);
				glTexCoord2f(1.0f, 0.0f);
				glVertex3i(SKY_SIZE, -SKY_SIZE, -SKY_SIZE);
			}
			glEnd();

			// Front
			skies[2].bind();

			glBegin(GL_QUADS);
			{
				glTexCoord2f(1.0f, 1.0f);
				glVertex3i(SKY_SIZE, -SKY_SIZE, -SKY_SIZE);
				glTexCoord2f(0.0f, 1.0f);
				glVertex3i(-SKY_SIZE, -SKY_SIZE, -SKY_SIZE);
				glTexCoord2f(0.0f, 0.0f);
				glVertex3i(-SKY_SIZE, SKY_SIZE, -SKY_SIZE);
				glTexCoord2f(1.0f, 0.0f);
				glVertex3i(SKY_SIZE, SKY_SIZE, -SKY_SIZE);
			}
			glEnd();

			// Back
			skies[3].bind();

			glBegin(GL_QUADS);
			{
				glTexCoord2f(0.0f, 1.0f);
				glVertex3i(SKY_SIZE, -SKY_SIZE, SKY_SIZE);
				glTexCoord2f(1.0f, 1.0f);
				glVertex3i(-SKY_SIZE, -SKY_SIZE, SKY_SIZE);
				glTexCoord2f(1.0f, 0.0f);
				glVertex3i(-SKY_SIZE, SKY_SIZE, SKY_SIZE);
				glTexCoord2f(0.0f, 0.0f);
				glVertex3i(SKY_SIZE, SKY_SIZE, SKY_SIZE);
			}
			glEnd();

			// Left
			skies[4].bind();

			glBegin(GL_QUADS);
			{
				glTexCoord2f(0.0f, 0.0f);
				glVertex3i(-SKY_SIZE, SKY_SIZE, SKY_SIZE);
				glTexCoord2f(1.0f, 0.0f);
				glVertex3i(-SKY_SIZE, SKY_SIZE, -SKY_SIZE);
				glTexCoord2f(1.0f, 1.0f);
				glVertex3i(-SKY_SIZE, -SKY_SIZE, -SKY_SIZE);
				glTexCoord2f(0.0f, 1.0f);
				glVertex3i(-SKY_SIZE, -SKY_SIZE, SKY_SIZE);
			}
			glEnd();

			// Right
			skies[5].bind();

			glBegin(GL_QUADS);
			{
				glTexCoord2f(0.0f, 0.0f);
				glVertex3i(SKY_SIZE, SKY_SIZE, -SKY_SIZE);
				glTexCoord2f(1.0f, 0.0f);
				glVertex3i(SKY_SIZE, SKY_SIZE, SKY_SIZE);
				glTexCoord2f(1.0f, 1.0f);
				glVertex3i(SKY_SIZE, -SKY_SIZE, SKY_SIZE);
				glTexCoord2f(0.0f, 1.0f);
				glVertex3i(SKY_SIZE, -SKY_SIZE, -SKY_SIZE);
			}
			glEnd();

			Texture.unbind();
		}
		glEndList();
	}
}
