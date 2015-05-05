/** 
 * 3D Video Game: Journey of a Dragon.
 * 
 * Author: Jihan Li
 * 
 * This is the map class. It creates a terrain based on the input heightmap.
 * 
 * Reference: 
 *  
 * LWJGL Tutorials, by Oskar Veerhoek
 * https://www.youtube.com/playlist?list=PL19F2453814E0E315
 * 
 */

package render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lwjgl.LWJGLException;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.newdawn.slick.Color;

public class Map {
	
	private int width = 1600;
	private int height = 1600;
	private int srcW;
	private int srcH;
	
	private int terrain;
		

	private float[][] altitude;
	private Vector4f[][] colors;
	private Vector3f[][] normals;
	
	private float maxAltitude = 300.0f;
	
	private ShaderProgram mapShader = new ShaderProgram();
	private int diffuseLocation;
	
	
	
	public ShaderProgram getMapShader()
	{
		return mapShader;
	}
	
	
	public float[][] getAltitude()
	{
		return altitude;
	}
	
	
	public float getWidth()
	{
		return srcW;
	}
	
	public float getHeight()
	{
		return srcH;
	}
	
	
	/** 
     * Load the height map of a terrain.
	 * @throws LWJGLException 
     */
	public void loadTerrain(String heightMap) throws IOException, LWJGLException
	{
		BufferedImage src = ImageIO.read(new File("res/" + heightMap + ".png"));
        srcW = src.getWidth();
        srcH = src.getHeight();
        
        altitude = new float[srcW][srcH];
        Color color;
        float alt;
        for(int i = 0; i < srcW; i++)
        {
        	for(int j = 0; j < srcH; j++)
        	{
        		color = new Color(src.getRGB(i, j));
        		alt = color.getRed()/256.0f;
        		altitude[i][j] = alt * maxAltitude;
        	}
        }
        
        
        getNormal();
        getColor();
		
        mapShader.create("Terrain");
		
        glBindAttribLocation(mapShader.getShaderProgram(), diffuseLocation, "Kd");
		diffuseLocation = glGetAttribLocation(mapShader.getShaderProgram(), "Kd");
        
        initMap();
	}
	
	
	/** 
     * Generate the terrain list and use the shaders.
     */
	private void initMap()
	{
		terrain = glGenLists(1);
		glNewList(terrain, GL_COMPILE);
		{
			//glMaterialf(GL_FRONT, GL_SHININESS, 128.0f);
			for(int i = 0; i < srcW-1; i++)
	        {	
				glBegin(GL_TRIANGLE_STRIP);
	        	for(int j = 0; j < srcH; j++)
	        	{
	        		glNormal3f(normals[i][j].x, normals[i][j].y, normals[i][j].z);
	        		glVertexAttrib4f(diffuseLocation, colors[i][j].x, colors[i][j].y, colors[i][j].z, colors[i][j].w);
	        		glVertex3f(j*2.0f/(srcH-1)*height-height, altitude[i][j], i*2.0f/(srcW-1)*width-width);
	        		
	        		glNormal3f(normals[i+1][j].x, normals[i+1][j].y, normals[i+1][j].z);
	        		glVertexAttrib4f(diffuseLocation, colors[i][j].x, colors[i][j].y, colors[i][j].z, colors[i][j].w);
	        		glVertex3f(j*2.0f/(srcH-1)*height-height, altitude[i+1][j], (i+1)*2.0f/(srcW-1)*width-width);
	        	}
	        	glEnd();
	        }
		}
		glEndList();
	}
	
	
	/** 
     * Get normal vector of the height map.
     */
	private void getNormal()
	{
		normals = new Vector3f[srcW][srcH];
		for(int x = 0; x < srcW; x++)
		{
			for(int z = 0; z < srcH; z++)
			{	
				if(x != 0 && z != srcH-1)
				{
					float sum = (float) Math.sqrt(Math.pow(altitude[x][z]-altitude[x-1][z],2) + Math.pow(altitude[x][z]-altitude[x][z+1],2) + 1.0f);
					normals[x][z] = new Vector3f(-(altitude[x][z]-altitude[x-1][z])/sum, 1.0f/sum, -(altitude[x][z]-altitude[x][z+1])/sum);
				}
				else if(x == 0 || z == srcH-1)
				{
					normals[x][z] = new Vector3f(0, 1, 0);
				}
			}
		}
		
	}
	
	/** 
     * Get colors based on different heights.
     */
	private void getColor()
	{
		colors = new Vector4f[srcW][srcH];
		for(int x = 0; x < srcW; x++)
		{
			for(int z = 0; z < srcH; z++)
			{			
				Vector4f color;
				float pos = altitude[x][z] / (float)(maxAltitude);
		
				if(pos > 0.85f)
				{
					color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
				}
				if(pos > 0.8f)
				{
					float i = pos - 0.8f;
					color = new Vector4f((230/255f)*(1.0f-i) + (255/255f)*i, (230/255f)*(1.0f-i) + (255/255f)*i, (230/255f)*(1.0f-i) + (255/255f)*i, 1.0f);
				}
				else if(pos > 0.7f)
				{
					float i = pos - 0.8f;
					color = new Vector4f((120/255f)*(1.0f-i) + (230/255f)*i, (99/255f)*(1.0f-i) + (230/255f)*i, (33/255f)*(1.0f-i) + (230/255f)*i, 1.0f);
				}
				else if(pos > 0.45f)
				{
					float i = (pos - 0.45f)/0.25f;
					color = new Vector4f((118/255f)*(1.0f-i) + (120/255f)*i, (54/255f)*(1.0f-i) + (99/255f)*i, (35/255f)*(1.0f-i) + (33/255f)*i, 1.0f);
				}
				else if(pos > 0.38f)
				{
					float i = (pos - 0.38f)/0.1f;
					color = new Vector4f((236/255f)*(1f-i) + (118/255f)*i, (238/255f)*(1f-i) + (54/255f)*i, (182/255f)*(1f-i) + (35/255f)*i, 1.0f);
				}
				else if(pos > 0.2f)
				{
					float i = pos - 0.38f;
					color = new Vector4f((128/255f)*(1f-i) + (236/255f)*i, (128/255f)*(1f-i) + (238/255f)*i, (255/255f)*(1f-i) + (182/255f)*i, 0.7f*(1f-i) + i);
				}
				else
				{
					color = new Vector4f((128/255f), (128/255f), (255/255f), 0.7f);
				}
				
				colors[x][z] = color;
				
			}
		}
	}


	/** 
     * Draw the terrain list.
     */
	public void drawTerrain() throws IOException
	{
		mapShader.begin();
		glCallList(terrain);
		mapShader.end();
	}


	public Vector3f[][] getNormals() {
		return normals;
	}


	public void setNormals(Vector3f[][] normals) {
		this.normals = normals;
	}
}
