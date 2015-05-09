package edu.columbia.quidditch.render;

import org.lwjgl.opengl.GL11;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.render.screen.LoadScreen;

/**
 * Abstract model class
 * @author Yuqing Guan
 *
 */
public abstract class Model
{
	protected static final int NO_LIST = -1;
	protected static final int NO_INDEX = -1;
	
	protected int list = NO_LIST;
	protected MainGame game;
	
	public Model(MainGame game)
	{
		LoadScreen.increaseLoadCount();
		this.game = game;
	}
	
	protected abstract void createList();
	
	/**
	 * Call the display list. If no such list exists, create it.
	 */
	public void render()
	{
		if (list == NO_LIST)
		{
			createList();
		}
		
		GL11.glCallList(list);
	}
}
