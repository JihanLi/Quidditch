package edu.columbia.quidditch.model;

import org.lwjgl.opengl.GL11;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.interact.ButtonListener;

public class Modal extends Model
{
	private static Modal singleton;
	
	public static Modal create(MainGame game)
	{
		if (singleton == null)
		{
			singleton = new Modal(game);
		}
		
		return singleton;
	}
	
	private Texture bg;
	
	private Button[] buttons;
	
	private Modal(MainGame game)
	{
		super(game);
		
		bg = Texture.createFromFile("res/modal/modalBg.png");
		
		buttons = new Button[2];
		
		buttons[0] = new Button(game, "Wood", 380, 150, "Confirm");
		buttons[1] = new Button(game, "Wood", 500, 150, "Cancel");
	}
	
	public void setButtonText(int idx, String text)
	{
		buttons[idx].setText(text);
	}

	@Override
	protected void createList()
	{
		LoadingScreen.log("Creating display lists for modal");
		
		list = GL11.glGenLists(1);

		GL11.glNewList(list, GL11.GL_COMPILE);
		{
			GL11.glColor4f(0, 0, 0, 0.5f);
			
			GL11.glBegin(GL11.GL_QUADS);
			
			GL11.glVertex2f(0, 0);
			GL11.glVertex2f(0, 540);
			GL11.glVertex2f(960, 540);
			GL11.glVertex2f(960, 0);
			
			GL11.glEnd();
			
			bg.drawRectangle(280, 130, 400, 280);
		}
		GL11.glEndList();
	}
	
	@Override
	public void render()
	{
		super.render();
		
		for (Button button: buttons)
		{
			button.render();
		}
	}
	
	public void setListeners(ButtonListener confirmListener, ButtonListener cancelListener)
	{
		buttons[0].setListener(confirmListener);
		buttons[1].setListener(cancelListener);
	}

	@Override
	public void checkMouseInput()
	{
		for (Button button : buttons)
		{
			if (button.mouseInside())
			{
				button.click();
				break;
			}
		}
	}
}
