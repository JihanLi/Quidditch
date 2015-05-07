package menu;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import org.lwjgl.BufferUtils;

import util.PropertiesManager;


public class Button {

    private int posX;
    private int posY;
    private int texWid;
    private int texHet;
    private int imgIdx;
    private int texIdx;
    
    private boolean isClicked = false;


    public void loadButton(String name, int x, int y, int wid, int het) throws IOException
    {
    	posX = x;
    	posY = y;
    	texWid = wid;
    	texHet = het;
    	
    	BufferedImage img = ImageIO.read(new File("res/"+ name +".png"));
    	int width = img.getWidth();
    	int height = img.getHeight();
    	int[] pixels = new int[width * height];
    	img.getRGB(0, 0, width, height, pixels, 0, width);
		
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4); 
		  
		for(int j = 0; j < height; j++)
		{
		    for(int i = 0; i < width; i++)
		    {
		        int pixel = pixels[j * width + i];
		        buffer.put((byte) ((pixel >> 16) & 0xFF));
		        buffer.put((byte) ((pixel >> 8) & 0xFF));
		        buffer.put((byte) (pixel & 0xFF)); 
		        buffer.put((byte) ((pixel >> 24) & 0xFF)); 
		    }
		}
		
		buffer.flip();
		imgIdx = glGenTextures();
		
		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
		glBindTexture(GL_TEXTURE_2D, imgIdx);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
	    
	    initialize();
    }

    public void initialize()
    {	       
    	texIdx = glGenLists(1);
		glNewList(texIdx, GL_COMPILE);
		{
			glBindTexture(GL_TEXTURE_2D, texIdx);
			glBegin(GL_QUADS);
			{
				glTexCoord2f(0, 0);
	            glVertex2i(posX, posY);
	            glTexCoord2f(0,1);
	            glVertex2i(posX, posY + texHet);
	            glTexCoord2f(1,1);
	            glVertex2i(posX + texWid, posY + texHet);
	            glTexCoord2f(1,0);
	            glVertex2i(posX + texWid, posY);
			}
		    glEnd();
		}
	    glEndList();
	    
       	
    }
    
    public void draw()
	{
    	glMatrixMode(GL_PROJECTION);
		
		glLoadIdentity();
        glOrtho(0, PropertiesManager.getDefaultWidth(), PropertiesManager.getDefaultHeight(), 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        
        glEnable(GL_TEXTURE_2D);   
		glCallList(texIdx);
		glDisable(GL_TEXTURE_2D);
	}

}