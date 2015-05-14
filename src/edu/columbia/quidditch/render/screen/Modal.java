package edu.columbia.quidditch.render.screen;

import org.lwjgl.input.Keyboard;

import static org.lwjgl.opengl.GL11.*;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.interact.ButtonListener;
import edu.columbia.quidditch.render.Button;

/**
 * Modal
 * 
 * @author Yilin Xiong
 * 
 */

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
	
	private Texture bg, textRunning, textStart;
	private Boolean modalType; // True for modal in start. False for modal in running.
	
	private int textRunningList, textStartList;
	
	private Modal(MainGame game)
	{
		super(game);
		
		bg = Texture.createFromFile("res/modal/modalBg.png");
		textRunning = Texture.createFromFile("res/title/pause.png");
		textStart = Texture.createFromFile("res/title/quit.png");
		
		modalType = true;
		Button button0 = new Button(game, "Wood", 380, 150, "Confirm", 12);
		Button button1 = new Button(game, "Wood", 500, 150, "Cancel", 12);
		
		addButton(button0);
		addButton(button1);
	}
	
	public void setModalInRunning()
	{
		modalType = false;
	}
	
	public void setModalInStart()
	{
		modalType = true;
	}
	
	
	public void setButtonText(int idx, String text)
	{
		buttons.get(idx).setText(text);
	}
	
	public void setListener(int idx, ButtonListener listener)
	{
		buttons.get(idx).setListener(listener);
	}

	@Override
	protected void createList()
	{
		LoadScreen.log("Creating display lists for modal");
		
		list = glGenLists(1);

		glNewList(list, GL_COMPILE);
		{
			glColor4f(0, 0, 0, 0.5f);
			
			glBegin(GL_QUADS);
			
			glVertex2f(0, 0);
			glVertex2f(0, 540);
			glVertex2f(960, 540);
			glVertex2f(960, 0);
			
			glEnd();
			
			bg.drawRectangle(280, 130, 400, 280);
		}
		glEndList();
		
		textStartList = glGenLists(1);

		glNewList(textStartList, GL_COMPILE);
		{
			textStart.drawRectangle(345, 230, 270, 81);
		}
		glEndList();
		
		textRunningList = glGenLists(1);

		glNewList(textRunningList, GL_COMPILE);
		{
			textRunning.drawRectangle(345, 230, 270, 81);
		}
		glEndList();
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
					keyReleased = true;
					buttons.get(1).click();
					break;
				case Keyboard.KEY_F2:
				case Keyboard.KEY_F12:
				case Keyboard.KEY_P:
					keyReleased = true;
					game.screenshot();
					break;
				case Keyboard.KEY_F11:
					keyReleased = true;
					game.toggleFullscreen();
					break;
				case Keyboard.KEY_RETURN:
					keyReleased = true;
					buttons.get(0).click();
				}
			}
		}
		
		return keyReleased;
	}
	
	@Override
	public void render()
	{
		super.render();	
		glCallList(modalType ? textStartList : textRunningList);		
	}

}
