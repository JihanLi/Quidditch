package edu.columbia.quidditch.util;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import edu.columbia.quidditch.MainGame;
import edu.columbia.quidditch.basic.Camera;

/**
 * Check the keyboard and mouse input
 * 
 * @author Yuqing Guan
 * 
 */
public class InputChecker
{
	private static final float MOUSE_SENSITIVITY = 0.05f;

	private MainGame game;

	public InputChecker(MainGame game)
	{
		game.increaseLoadCount();
		this.game = game;
	}

	/**
	 * Check keyboard event when the game is running
	 * 
	 * @param delta
	 *            time
	 */
	private void checkKeyboardForStatusRunning(float delta)
	{
		int wheel = Mouse.getDWheel();

		boolean keyForward = Keyboard.isKeyDown(Keyboard.KEY_W)
				|| Keyboard.isKeyDown(Keyboard.KEY_UP) || wheel > 0;
		boolean keyBack = Keyboard.isKeyDown(Keyboard.KEY_S)
				|| Keyboard.isKeyDown(Keyboard.KEY_DOWN) || wheel < 0;

		boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_A)
				|| Keyboard.isKeyDown(Keyboard.KEY_LEFT);
		boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_D)
				|| Keyboard.isKeyDown(Keyboard.KEY_RIGHT);

		boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
		boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

		boolean keyReset = Keyboard.isKeyDown(Keyboard.KEY_R);

		boolean keyFire = Keyboard.isKeyDown(Keyboard.KEY_RETURN);

		// TODO Myself
		/*
		 * Myself myself = game.getMyself();
		 * 
		 * // Turn left myself if (keyLeft) { myself.rotY(1, delta); }
		 * 
		 * // Turn right myself if (keyRight) { myself.rotY(-1, delta); }
		 * 
		 * // Turn up myself if (keyUp) { myself.rotX(1, delta); }
		 * 
		 * // Turn down myself if (keyDown) { myself.rotX(-1, delta); }
		 * 
		 * // Reset myself to a horizontal direction if (keyReset) {
		 * myself.resetRotX(); }
		 * 
		 * // Accelerate if (keyForward) { myself.accelerate(); }
		 * 
		 * // Decelerate if (keyBack) { myself.decelerate(); }
		 * 
		 * // Attack if (keyFire) { game.fire(); }
		 */
	}

	/**
	 * Check keyboard for all status
	 */
	private void checkKeyboardForAllStatuses()
	{
		while (Keyboard.next())
		{
			if (Keyboard.getEventKeyState())
			{
				switch (Keyboard.getEventKey())
				{
				case Keyboard.KEY_Q:
				case Keyboard.KEY_ESCAPE:
					game.requestClose();
					break;
				case Keyboard.KEY_F2: // In Minecraft, F2 can be used to take
										// screenshots
				case Keyboard.KEY_F12: // In Europa Universalis and Victoria,
										// F12 can be used to take screenshots
				case Keyboard.KEY_P:
					game.screenshot();
					break;
				case Keyboard.KEY_F11:
					game.toggleFullscreen();
					break;
				case Keyboard.KEY_RETURN:

					break;
				}
			}
		}
	}

	/**
	 * Check keyboard
	 * 
	 * @param delta
	 *            time
	 */
	public void checkKeyboard(float delta)
	{
		if (game.isRunning())
		{
			checkKeyboardForStatusRunning(delta);
		}

		checkKeyboardForAllStatuses();
	}

	/**
	 * Check mouse
	 * 
	 * @param delta
	 *            time
	 */
	public void checkMouse(float delta)
	{
		if (!game.isRunning())
		{
			while (Mouse.next())
			{
				if (!Mouse.getEventButtonState() && Mouse.getEventButton() == 0)
				{
					if (game.isShowingModal())
					{
						game.getModal().checkMouseInput();
					}
					else
					{
						if (game.isBeginning())
						{
							game.getStartScreen().checkMouseInput();
						}
					}
				}
			}

			return;
		}

		// Rotate the camera by mouse
		Camera camera = game.getCamera();

		if (Display.isActive())
		{
			if (camera.isSwinging())
			{
				float mouseDX = Mouse.getDX();
				float mouseDY = -Mouse.getDY();

				camera.rotY(mouseDX * MOUSE_SENSITIVITY * delta);
				camera.rotX(mouseDY * MOUSE_SENSITIVITY * delta);
			}
			else
			{
				Mouse.getDX();
				Mouse.getDY();

				camera.startSwing();
			}
		}
		else
		{
			camera.stopSwing();
		}
	}
}
