package menu;

import static org.lwjgl.opengl.GL11.GL_COMPILE;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_REPLACE;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV_MODE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexEnvi;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glVertex2i;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;


public class Menu {
	
	private int menuImg;
	private int mainMenu;

	/** 
     * Load the texture of the main menu.
     * @throws IOException 
     */
	public void loadInterface(String name) throws IOException
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
		menuImg = glGenTextures();
		
		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
		glBindTexture(GL_TEXTURE_2D, menuImg);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
	    
	    mainMenu = glGenLists(1);
		glNewList(mainMenu, GL_COMPILE);
		{
			glBindTexture(GL_TEXTURE_2D, menuImg);
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
	
}
