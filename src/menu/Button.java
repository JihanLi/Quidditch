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
import org.lwjgl.input.Mouse;

import util.PropertiesManager;


public class Button {

    private int posX;
    private int posY;
    private int texWid;
    private int texHet;
    private int imgIdx;
    private int texIdx;
    private int imgIdxPressed;
    private int texIdxPressed;
    
    private static boolean released = true;
    
    
    private boolean isClicked = false;
    private int hasClicked = 0;


    public void loadButton(String name, String pressed, int x, int y, int wid, int het) throws IOException
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
	    
	    loadButtonPressed(pressed);
    }
    
    public void loadButtonPressed(String name) throws IOException
    {
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
		imgIdxPressed = glGenTextures();
		
		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
		glBindTexture(GL_TEXTURE_2D, imgIdxPressed);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
	    
	    texIdxPressed = glGenLists(1);
		glNewList(texIdxPressed, GL_COMPILE);
		{
			glBindTexture(GL_TEXTURE_2D, texIdxPressed);
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

    
    public boolean mouseWithinBound()
    {
    	if(Mouse.getX() > posX && Mouse.getX() < posX + texWid)
    	{
    		int mouseY = PropertiesManager.getDefaultHeight() - Mouse.getY();
    		if(mouseY > posY && mouseY < posY + texHet)
    		{
    			return true;
    		}
    	}
    	return false;
    }
    
    public void draw()
	{	
    	glMatrixMode(GL_PROJECTION);
		
		glLoadIdentity();
        glOrtho(0, PropertiesManager.getDefaultWidth(), PropertiesManager.getDefaultHeight(), 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        
        glEnable(GL_TEXTURE_2D);
        
        if(Mouse.isButtonDown(0) && (isClicked || mouseWithinBound()))
        {
        	if(released || isClicked)
        	{
	    		isClicked = true;
	    		released = false;
	    		hasClicked = 0;
	    		glCallList(texIdxPressed);
        	}
        	else
        		glCallList(texIdx);
    		
        }
    	else
    	{
    		if(isClicked)
    		{
    			isClicked = false;
    			released = true;
    			hasClicked++;
    		}
    		else
    		{
    			if(hasClicked == 1 && mouseWithinBound())
    				hasClicked = 2;
    			else
    				hasClicked = 0;
    		}
    		glCallList(texIdx);
    	}
		
		glDisable(GL_TEXTURE_2D);
	}

	public int hasClicked() {
		return hasClicked;
	}

}