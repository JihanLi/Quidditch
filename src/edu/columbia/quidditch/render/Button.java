package edu.columbia.quidditch.render;

import java.util.HashMap;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.interact.ButtonListener;
import edu.columbia.quidditch.render.screen.LoadScreen;
import edu.columbia.quidditch.render.screen.Screen;

/**
 * Button
 * 
 * @author Jihan Li
 * 
 */
public class Button extends Model
{
	private static final String[] TYPES = { "Green", "Wood" };
	private static final HashMap<String, Texture> normalBg, pressedBg;

	private static final HashMap<String, Float> defaultWidths, defaultHeights;
	
	static
	{
		normalBg = new HashMap<String, Texture>();
		pressedBg = new HashMap<String, Texture>();
		
		defaultWidths = new HashMap<String, Float>();
		defaultHeights = new HashMap<String, Float>();
		
		for (String type : TYPES)
		{
			normalBg.put(type, Texture.createFromFile("res/button/normal" + type + ".png"));
			pressedBg.put(type, Texture.createFromFile("res/button/pressed" + type + ".png"));
		}
		
		defaultWidths.put("Green", 250.0f);
		defaultHeights.put("Green", 90.0f);
		
		defaultWidths.put("Wood", 100.0f);
		defaultHeights.put("Wood", 50.0f);
	}

	private String text;

	private int normalBgList, pressedBgList, textList, name;
	private float x, y, width, height;

	private boolean visible;
	private String type;
	
	private ButtonListener listener;
	private Screen screen;

	public Button(MainGame game, String type, float x, float y, String text)
	{
		this(game, type, x, y, defaultWidths.get(type), defaultHeights.get(type), text);
	}

	public Button(MainGame game, String type, float x, float y, float width, float height,
			String text)
	{
		super(game);

		this.x = x;
		this.y = y;

		this.width = width;
		this.height = height;

		this.type = type;
		this.text = text;

		normalBgList = glGenLists(1);
		pressedBgList = glGenLists(1);
		textList = glGenLists(1);
		
		screen = null;

		createList();
		
		listener = null;
		visible = true;
	}

	public void setText(String text)
	{
		this.text = text;
		createTextList();
	}

	@Override
	protected void createList()
	{
		LoadScreen.log("Creating display lists for button");
		
		createBackgroundList();
		createTextList();
	}

	/**
	 * Create display list for the square background
	 */
	private void createBackgroundList()
	{
		glNewList(normalBgList, GL_COMPILE);
		{
			normalBg.get(type).drawRectangle(x, y, width, height);
		}
		glEndList();

		glNewList(pressedBgList, GL_COMPILE);
		{
			pressedBg.get(type).drawRectangle(x, y, width, height);
		}
		glEndList();
	}

	/**
	 * Create display list for the text
	 */
	private void createTextList()
	{
		glNewList(textList, GL_COMPILE);
		{
			// TODO
		}
		glEndList();
	}
	
	public void setListener(ButtonListener listener)
	{
		this.listener = listener;
	}
	
	public void setScreen(Screen screen)
	{
		this.screen = screen;
	}

	@Override
	public void render()
	{
		if (!visible)
		{
			return;
		}

		if (!mouseInBound() || !Mouse.isButtonDown(0))
		{
			glCallList(normalBgList);
		}
		else
		{
			glCallList(pressedBgList);
		}
		
		glCallList(textList);
	}

	/**
	 * Check whether mouse is inside this button
	 * 
	 * @return
	 */
	public boolean mouseInBound()
	{
		if (!visible)
		{
			return false;
		}
		
		if (screen != null && screen != game.getActiveScreen())
		{
			return false;
		}

		float mouseX = Mouse.getX();
		float mouseY = Mouse.getY();

		mouseX = mouseX * MainGame.DEFAULT_WIDTH / Display.getWidth();
		mouseY = mouseY * MainGame.DEFAULT_HEIGHT / Display.getHeight();

		return mouseX > x && mouseY > y && mouseX < x + width
				&& mouseY < y + height;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public int getName()
	{
		return name;
	}
	
	public void click()
	{
		if (listener == null)
		{
			return;
		}
		
		listener.onClick();
	}
}
