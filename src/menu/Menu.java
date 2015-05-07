package menu;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;



public class Menu {

	private int imgIdx;
	private int texIdx;
	
	/** 
     * Load the texture of the main menu.
     * @throws IOException 
     */
	public void loadMenu(String name) throws IOException
	{
		BufferedImage img = ImageIO.read(new File("res/"+ name +".png"));
    	int width = img.getWidth();
    	int height = img.getHeight();
    	int[] pixels = new int[width*height];
    	img.getRGB(0, 0, width,height, pixels, 0, width);
		
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4); 
		  
		for(int y = 0; y < height; y++)
		{
		    for(int x = 0; x < width; x++)
		    {
		        int pixel = pixels[y * width + x];
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
				glVertex2i(0, 0);
		        glTexCoord2f(0, 1);
		        glVertex2i(0, 480);	
		        glTexCoord2f(1, 1);
		        glVertex2i(640, 480); 
		        glTexCoord2f(1, 0);
		        glVertex2i(640, 0);	
			}
		    glEnd();
		}
	    glEndList();
	}
	
	public void draw()
	{
		glMatrixMode(GL_PROJECTION);
		
		glLoadIdentity();
        glOrtho(0, 640, 480, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        
        glEnable(GL_TEXTURE_2D);   
		glCallList(texIdx);
		glDisable(GL_TEXTURE_2D);
	}
	
}
