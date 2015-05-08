package edu.columbia.quidditch.basic;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

/**
 * Store material from mtl files
 * @author Yuqing Guan
 * 
 */
public class Material
{
	private Texture texture;
	private Vector3f diffuse, ambient, specular;
	private float transparency;
	
	public Material()
	{
		diffuse = ambient = specular = new Vector3f(0.0f, 0.0f, 0.0f);
		transparency = 1.0f;
	}
	
	/** 
	 * Set diffuse color
	 * @param r
	 * @param g
	 * @param b
	 */
	public void setDiffuse(float r, float g, float b)
	{
		diffuse = new Vector3f(r, g, b);
	}
	
	/** 
	 * Set ambient color
	 * @param r
	 * @param g
	 * @param b
	 */
	public void setAmbient(float r, float g, float b)
	{
		ambient = new Vector3f(r, g, b);
	}
	
	/**
	 * Set specular color
	 * @param r
	 * @param g
	 * @param b
	 */
	public void setSpecular(float r, float g, float b)
	{
		specular = new Vector3f(r, g, b);
	}
	
	/**
	 * Set transparency
	 * @param t
	 */
	public void setTransparency(float t)
	{
		transparency = t;
	}
	
	/**
	 * Set the texture
	 * @param texture
	 */
	public void setTexture(Texture texture)
	{
		this.texture = texture;
	}
	
	/**
	 * Check whether it contains a texture 
	 * @return
	 */
	public boolean hasTexture()
	{
		return texture != null;
	}
	
	/**
	 * Set the colors, and bind texture if it exists
	 */
	public void bind()
	{
		Vector3f color;
		
		color = ambient;
		GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT);
		GL11.glColor4f(color.x, color.y, color.z, transparency);
		
		color = diffuse;
		GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE);
		GL11.glColor4f(color.x, color.y, color.z, transparency);
		
		color = specular;
		GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR);
		GL11.glColor4f(color.x, color.y, color.z, transparency);
		
		if (texture != null)
		{
			texture.bind();
		}
		else
		{
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}
	}
	
	/**
	 * Copy this material to another material
	 * Used for planes with various models
	 * In this case, I just copy this material and set different texture
	 * @return
	 */
	public Material copy()
	{
		Material other = new Material();
		
		other.setAmbient(ambient.x, ambient.y, ambient.z);
		other.setDiffuse(diffuse.x, diffuse.y, diffuse.z);
		other.setSpecular(specular.x, specular.y, specular.z);
		
		other.setTexture(texture);
		
		return other;
	}
}
