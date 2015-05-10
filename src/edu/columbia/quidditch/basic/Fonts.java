package edu.columbia.quidditch.basic;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;


/**
 * Fonts
 * 
 * @author Jihan Li
 * 
 */

public class Fonts
{
	private static UnicodeFont font;
	private static DecimalFormat formatter = new DecimalFormat("#.##");

	@SuppressWarnings("unchecked")
	public static void draw(float x, float y, String text, int size)
	{
		Font awtFont = new Font("Castellar", Font.BOLD, size);
		
		font = new UnicodeFont(awtFont);
		font.getEffects().add(new ColorEffect(Color.yellow));
		font.addAsciiGlyphs();
		
		try
		{
			font.loadGlyphs();
		}
		catch (SlickException e)
		{
			e.printStackTrace();
		}

		glPushMatrix();
		{
			glRotatef(180, 1, 0, 0);
			font.drawString(x - font.getWidth(text) / 2,
					-y - font.getHeight(text), text);
		}
		glPopMatrix();
	}

}
