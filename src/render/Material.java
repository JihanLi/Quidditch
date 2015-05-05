/** 
 * 3D Video Game: Journey of a Dragon.
 * 
 * Author: Jihan Li
 * 
 * This is the material class. It sets up attributes of materials.
 * 
 */

package render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

public class Material {
	
	/** 
     * Different attributes of materials.
     */
	private Vector3f diffuse;
	private Vector3f specular;
	private Vector3f ambient;
	private float dissolve;
	private float shininess;
	private float transparent;
	private float illuminance;
	

	//Kd, Ks, Ka, d, Ns
	private int[] texImg = {-1,-1,-1,-1,-1};
	
	public Material()
	{
		
	}
	
	/** 
     * Set attributes of materials.
     */
	public Material(Vector3f Kd, Vector3f Ks, Vector3f Ka, float d, float Ns, float tr, float illum)
	{
		this.setDiffuse(Kd);
		this.setSpecular(Ks);
		this.setAmbient(Ka);
		this.setDissolve(d);
		this.setShininess(Ns);
		this.setTransparent(tr);
		this.setIlluminance(illum);
	}
	
	/** 
     * Load the material textures.
     */
	public void loadTexture(String name, int idx) throws FileNotFoundException, IOException
    {
	System.out.println("res/"+name);
		BufferedImage img = ImageIO.read(new File("res/"+ name));
		
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
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    }
	

	public Vector3f getAmbient() {
		return ambient;
	}

	public void setAmbient(Vector3f ambient) {
		this.ambient = ambient;
	}

	public Vector3f getDiffuse() {
		return diffuse;
	}

	public void setDiffuse(Vector3f diffuse) {
		this.diffuse = diffuse;
	}

	public Vector3f getSpecular() {
		return specular;
	}

	public void setSpecular(Vector3f specular) {
		this.specular = specular;
	}

	public float getDissolve() {
		return dissolve;
	}

	public void setDissolve(float dissolve) {
		this.dissolve = dissolve;
	}

	public float getTransparent() {
		return transparent;
	}

	public void setTransparent(float transparent) {
		this.transparent = transparent;
	}

	public float getIlluminance() {
		return illuminance;
	}

	public void setIlluminance(float illuminance) {
		this.illuminance = illuminance;
	}

	public float getShininess() {
		return shininess;
	}

	public void setShininess(float shininess) {
		this.shininess = shininess;
	}

	public int[] getTexImg() {
		return texImg;
	}

	public void setTexImg(int[] texImg) {
		this.texImg = texImg;
	}
	
}
