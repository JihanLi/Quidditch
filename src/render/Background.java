/** 
 * 3D Video Game: Journey of a Dragon.
 * 
 * Author: Jihan Li
 * 
 * This is the background class. It creates the sky box and put textures onto each face.
 * The cloud flows by rotating the sky box.
 * 
 * Reference: 
 *  
 * LWJGL Tutorials, by Oskar Veerhoek
 * https://www.youtube.com/playlist?list=PL19F2453814E0E315
 * 
 */

package render;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL12;

public class Background {
	
	private int[] texImg = new int[6];
	private float boxWidth = 3200.0f;
	private float upHeight = 3200.0f;
	private float downHeight = -3200.0f;
	private int sky, surrounding, ground;
	private float rotation = 0;
	
	ShaderProgram skyShader = new ShaderProgram();
	    
	
	public float getWidth()
	{
		return boxWidth;
	}
	
	public float getSkyHeight()
	{
		return upHeight;
	}
	
	
	/** 
     * Load the background image and partition it into 6 pieces.
	 * @throws LWJGLException 
     */
    public void loadBackground(String name) throws FileNotFoundException, IOException, LWJGLException
    {
    	BufferedImage src = ImageIO.read(new File("res/"+ name +".jpg"));
        int width = src.getWidth()/4;
        int height = src.getHeight()/3;
        
        BufferedImage part;
    	part = src.getSubimage(width, 0, width, height);
    	load(part, 0);
    	part = src.getSubimage(0, height, width, height);
    	load(part, 1);
    	part = src.getSubimage(width, height, width, height);
    	load(part, 2);
    	part = src.getSubimage(2*width, height, width, height);
    	load(part, 3);
    	part = src.getSubimage(3*width, height, width, height);
    	load(part, 4);
    	part = src.getSubimage(width, 2*height, width, height);
    	load(part, 5);
    	
    	skyShader.create("Skybox");
    	
    	init();
    }
    
    
    /** 
     * Load each image into the texture buffer.
     */
    public void load(BufferedImage img, int idx)
    {
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
		texImg[idx] = glGenTextures();
		
		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
		glBindTexture(GL_TEXTURE_2D, texImg[idx]);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    }
    
    
    /** 
     * Initialize the sky box lists of different faces.
     */
    public void init()
    {
    	sky = glGenLists(1);
		glNewList(sky, GL_COMPILE);
		{
			glBindTexture(GL_TEXTURE_2D, texImg[0]);
			glBegin(GL_QUADS);
			{
				glTexCoord2f(0, 1);
		        glVertex3f(-boxWidth, upHeight, -boxWidth);
		        glTexCoord2f(0, 0);
		        glVertex3f(boxWidth, upHeight, -boxWidth);
		        glTexCoord2f(1, 0);
		        glVertex3f(boxWidth, upHeight, boxWidth); 
		        glTexCoord2f(1, 1);
		        glVertex3f(-boxWidth, upHeight, boxWidth); 
			}
		    glEnd();
		}
	    glEndList();
	    
	    surrounding = glGenLists(1);
	    glNewList(surrounding, GL_COMPILE);
	    {
		    glBindTexture(GL_TEXTURE_2D, texImg[1]);
			glBegin(GL_QUADS);
			{
				glTexCoord2f(1, 1);
		        glVertex3f(-boxWidth, downHeight, -boxWidth);
		        glTexCoord2f(0, 1);
		        glVertex3f(boxWidth, downHeight, -boxWidth);
		        glTexCoord2f(0, 0);
		        glVertex3f(boxWidth, upHeight, -boxWidth);
		        glTexCoord2f(1, 0);
		        glVertex3f(-boxWidth, upHeight, -boxWidth);
			}
		    glEnd();
	    
		    glBindTexture(GL_TEXTURE_2D, texImg[2]);
			glBegin(GL_QUADS);
			{
		        glTexCoord2f(0, 1);
		        glVertex3f(-boxWidth, downHeight, -boxWidth);
		        glTexCoord2f(0, 0);
		        glVertex3f(-boxWidth, upHeight, -boxWidth);
		        glTexCoord2f(1, 0);
		        glVertex3f(-boxWidth, upHeight, boxWidth);
		        glTexCoord2f(1, 1);
		        glVertex3f(-boxWidth, downHeight, boxWidth);
			}
		    glEnd(); 
		    
		    glBindTexture(GL_TEXTURE_2D, texImg[3]);
			glBegin(GL_QUADS);
			{
		        glTexCoord2f(0, 1);
		        glVertex3f(-boxWidth, downHeight, boxWidth);
		        glTexCoord2f(0, 0);
		        glVertex3f(-boxWidth, upHeight, boxWidth);
		        glTexCoord2f(1, 0);
		        glVertex3f(boxWidth, upHeight, boxWidth);
		        glTexCoord2f(1, 1);
		        glVertex3f(boxWidth, downHeight, boxWidth);
			}
		    glEnd();
		    
		    glBindTexture(GL_TEXTURE_2D, texImg[4]);
			glBegin(GL_QUADS);
			{
		        glTexCoord2f(1, 1);
		        glVertex3f(boxWidth, downHeight, -boxWidth);
		        glTexCoord2f(0, 1);
		        glVertex3f(boxWidth, downHeight, boxWidth);
		        glTexCoord2f(0, 0);
		        glVertex3f(boxWidth, upHeight, boxWidth);
		        glTexCoord2f(1, 0);
		        glVertex3f(boxWidth, upHeight, -boxWidth);
			}
		    glEnd();
	    }
	    glEndList();
	    
	    
	    ground = glGenLists(1);
		glNewList(ground, GL_COMPILE);
		{
			glBindTexture(GL_TEXTURE_2D, texImg[5]);
			glBegin(GL_QUADS);
			{
				glTexCoord2f(0, 0);
		        glVertex3f(-boxWidth, downHeight, -boxWidth);
		        glTexCoord2f(1, 0);
		        glVertex3f(-boxWidth, downHeight, boxWidth);
		        glTexCoord2f(1, 1);
		        glVertex3f(boxWidth, downHeight, boxWidth); 
		        glTexCoord2f(0, 1);
		        glVertex3f(boxWidth, downHeight, -boxWidth); 
			}
		    glEnd();
		}
	    glEndList();
    }
	    
    
    /** 
     * Draw the sky box lists. And rotate it to produce the cloud flow.
     */
    public void drawBox() throws FileNotFoundException, IOException
	{	
    	rotation += 0.05f;
    	if (rotation / 360 > 1) 
        {
            rotation -= 360;
        } 
        else if (rotation / 360 < -1) 
        {
            rotation += 360;
        }
    	glRotatef(rotation, 0, 1, 0);
    	//skyShader.enableShader();
	    glCallList(sky);
	    glCallList(surrounding);
	    glCallList(ground);
	   // skyShader.disableShader();
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
	
}
