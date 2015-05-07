package menu;

import static org.lwjgl.opengl.GL11.*;

import util.PropertiesManager;

public class ProgressBar extends Menu {
	
	public void draw(int count)
	{
		glMatrixMode(GL_PROJECTION);
		
		glLoadIdentity();
		glOrtho(0, PropertiesManager.getDefaultWidth(), PropertiesManager.getDefaultHeight(), 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        
        glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, texIdx);
		glBegin(GL_QUADS);
		{
			glTexCoord2f(0.0f, 0.0f);
            glVertex2i(posX, posY);
            glTexCoord2f(0.0f, 1.0f);
            glVertex2i(posX, posY + texHet);
            glTexCoord2f(count/100.0f, 1.0f);
            glVertex2i(posX + texWid*count/100, posY + texHet);
            glTexCoord2f(count/100.0f, 0.0f);
            glVertex2i(posX + texWid*count/100, posY);
		}
	    glEnd();
		glDisable(GL_TEXTURE_2D);
	}
	
}
