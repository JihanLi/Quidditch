package edu.columbia.quidditch.render.screen;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.interact.ButtonListener;
import edu.columbia.quidditch.render.Button;

public class Modal extends Screen
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
	
	private Modal(MainGame game)
	{
		super(game);
		
		bg = Texture.createFromFile("res/modal/modalBg.png");
		
		Button button0 = new Button(game, "Wood", 380, 150, "Confirm");
		Button button1 = new Button(game, "Wood", 500, 150, "Cancel");
		
		buttons.add(button0);
		buttons.add(button1);
		
		children.add(button0);
		children.add(button1);
	}
	
	public void setButtonText(int idx, String text)
	{
		buttons.get(idx).setText(text);
	}

	@Override
	protected void createList()
	{
		LoadScreen.log("Creating display lists for modal");
		
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
	
	public void setListeners(ButtonListener confirmListener, ButtonListener cancelListener)
	{
		buttons.get(0).setListener(confirmListener);
		buttons.get(1).setListener(cancelListener);
	}

	@Override
	public boolean checkKeyboardInput(float delta)
	{
		boolean keyReleased = false;
		
		while (Keyboard.next())
		{
			if (!Keyboard.getEventKeyState())
			{
				switch (Keyboard.getEventKey())
				{
				case Keyboard.KEY_Q:
				case Keyboard.KEY_ESCAPE:
					buttons.get(1).click();
					break;
				case Keyboard.KEY_F2:
				case Keyboard.KEY_F12:
				case Keyboard.KEY_P:
					game.screenshot();
					break;
				case Keyboard.KEY_F11:
					game.toggleFullscreen();
					break;
				case Keyboard.KEY_RETURN:
					buttons.get(0).click();
				}
			}
		}
		
		return keyReleased;
	}
}