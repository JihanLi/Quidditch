package edu.columbia.quidditch.model;

import java.util.HashMap;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.interact.ButtonListener;

/**
 * Button
 * 
 * @author Yuqing Guan
 * 
 */
public class Button extends Model
{
	private static final String[] TYPES = { "Green", "Wood" };
	private static final HashMap<String, Texture> bg, activeBg;

	private static final HashMap<String, Float> defaultWidths, defaultHeights;
	
	static
	{
		bg = new HashMap<String, Texture>();
		activeBg = new HashMap<String, Texture>();
		
		defaultWidths = new HashMap<String, Float>();
		defaultHeights = new HashMap<String, Float>();
		
		for (String type : TYPES)
		{
			bg.put(type, Texture.createFromFile("res/button/normal" + type + ".png"));
			activeBg.put(type, Texture.createFromFile("res/button/active" + type + ".png"));
		}
		
		defaultWidths.put("Green", 250.0f);
		defaultHeights.put("Green", 90.0f);
		
		defaultWidths.put("Wood", 100.0f);
		defaultHeights.put("Wood", 50.0f);
	}

	private String text;

	private int bgList, activeBgList, textList, name;
	private float x, y, width, height;

	private boolean visible;
	private String type;
	
	private ButtonListener listener;

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

		bgList = GL11.glGenLists(1);
		activeBgList = GL11.glGenLists(1);
		textList = GL11.glGenLists(1);

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
		MainGame.log("Creating display lists for button");
		
		createBackgroundList();
		createTextList();
	}

	/**
	 * Create display list for the square background
	 */
	private void createBackgroundList()
	{
		GL11.glNewList(bgList, GL11.GL_COMPILE);
		{
			bg.get(type).drawRectangle(x, y, width, height);
		}
		GL11.glEndList();

		GL11.glNewList(activeBgList, GL11.GL_COMPILE);
		{
			activeBg.get(type).drawRectangle(x, y, width, height);
		}
		GL11.glEndList();
	}

	/**
	 * Create display list for the text
	 */
	private void createTextList()
	{
		GL11.glNewList(textList, GL11.GL_COMPILE);
		{
			// TODO
		}
		GL11.glEndList();
	}
	
	public void setListener(ButtonListener listener)
	{
		this.listener = listener;
	}

	@Override
	public void render()
	{
		if (!visible)
		{
			return;
		}

		if (!mouseInside() || !Mouse.isButtonDown(0))
		{
			GL11.glCallList(bgList);
		}
		else
		{
			GL11.glCallList(activeBgList);
		}
		
		GL11.glCallList(textList);
	}

	/**
	 * Check whether mouse is inside this button
	 * 
	 * @return
	 */
	public boolean mouseInside()
	{
		if (!visible)
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
