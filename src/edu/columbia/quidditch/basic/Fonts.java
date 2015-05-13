package edu.columbia.quidditch.basic;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluOrtho2D;

import java.awt.Color;
import java.awt.Font;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

import edu.columbia.quidditch.MainGame;


/**
 * Fonts
 * 
 * @author Jihan Li
 * 
 */

public class Fonts
{
	private static UnicodeFont font;

	@SuppressWarnings("unchecked")
	public static void draw(float x, float y, String text, String fontStyle, Color color, int size)
	{
		Font awtFont = new Font(fontStyle, Font.BOLD, size);
		
		font = new UnicodeFont(awtFont);
		font.getEffects().add(new ColorEffect(color));
		font.addAsciiGlyphs();
		
		try
		{
			font.loadGlyphs();
		}
		catch (SlickException e)
		{
			e.printStackTrace();
		}
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHTING);

		gluOrtho2D(0, MainGame.DEFAULT_WIDTH, 0, MainGame.DEFAULT_HEIGHT);
		

		glPushMatrix();
		{
			glRotatef(180, 1, 0, 0);
			font.drawString(x - font.getWidth(text) / 2,
					-y - font.getHeight(text), text);
		}
		glPopMatrix();
		
		
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_LIGHTING);

		glMatrixMode(GL_MODELVIEW);
	}

}
