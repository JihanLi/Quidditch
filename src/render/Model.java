/** 
 * 3D Video Game: Journey of a Dragon.
 * 
 * Author: Jihan Li
 * 
 * This is the model class. It parses the .obj and .mtl files, 
 * and load all the attributes and textures into objects.
 * 
 * Reference: 
 *  
 * 3D Game Development Tutorials, by ThinMatrix
 * https://www.youtube.com/playlist?list=PLRIWtICgwaX0u7Rf9zkZhLoLuZVfUksDP
 * 
 */

package render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;


public class Model {
	
	private List<Vector3f> vertices = new ArrayList<Vector3f>();
	private List<Vector2f> textures = new ArrayList<Vector2f>();
	private List<Vector3f> normals = new ArrayList<Vector3f>(); 
	private List<Face> faces =	new ArrayList<Face>();
	private HashMap<String, Integer> matIndex = new HashMap<String, Integer>();
	private List<Material> materials =	new ArrayList<Material>();
	private int obj;
	private ShaderProgram modelShader = new ShaderProgram();
	private int diffuseLocation, ambientLocation, specularLocation, shininessLocation, illumLocation, dissolveLocation;
	
	
	/** 
     * Clean up the model.
     */
	public void cleanup()
	{
		vertices.clear();
		textures.clear();
		normals.clear();
		faces.clear();
		materials.clear();
	}
	
	
	/** 
     * Load the model by parsing .obj and .mtl files.
     */
	public void loadModel(String filename) throws IOException
	{
		FileReader fr = null;
		fr = new FileReader(new File("res/" + filename + ".obj"));
		BufferedReader reader = new BufferedReader(fr);
		String line;
		String mtllib = null;
		int matNum = -1;
		
		while((line = reader.readLine()) != null)
		{
			line = line.trim();
			String[] values = line.split("[ ]+");
			
			if(line.startsWith("mtllib "))
			{
				mtllib = values[1];
				loadMaterial(mtllib);
			}
			else if(line.startsWith("v "))
			{
				Vector3f vertex = new Vector3f(Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3]));
				vertices.add(vertex);
			}
			else if(line.startsWith("vt "))
			{
				Vector2f texture = new Vector2f(Float.parseFloat(values[1]), Float.parseFloat(values[2]));
				textures.add(texture);
			}
			else if(line.startsWith("vn "))
			{
				Vector3f normal = new Vector3f(Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3]));
				normals.add(normal);
			}
			else if(line.startsWith("usemtl "))
			{
				if(mtllib != null)
				{	
					matNum = matIndex.get(values[1]);
				}
			}
			else if(line.startsWith("f "))
			{
				String[] vertex1 = values[1].split("/");
				String[] vertex2 = values[2].split("/");
				String[] vertex3 = values[3].split("/");
				Vector3f verIdx = null, texIdx = null, nomIdx = null;
				if(!vertex1[0].equals(""))
					verIdx = new Vector3f(Integer.parseInt(vertex1[0]), Integer.parseInt(vertex2[0]), Integer.parseInt(vertex3[0]));
				if(!vertex1[1].equals(""))
					texIdx = new Vector3f(Integer.parseInt(vertex1[1]), Integer.parseInt(vertex2[1]), Integer.parseInt(vertex3[1]));
				if(!vertex1[2].equals(""))
					nomIdx = new Vector3f(Integer.parseInt(vertex1[2]), Integer.parseInt(vertex2[2]), Integer.parseInt(vertex3[2]));
				faces.add(new Face(verIdx, texIdx, nomIdx, matNum));
			}
		}

		reader.close();
	}
	
	
	/** 
     * Load the materials for the model.
     */
	public void loadMaterial(String filename) throws IOException
	{
		FileReader fr = null;
		fr = new FileReader(new File("res/" + filename));
		BufferedReader reader = new BufferedReader(fr);
		String line;
		int index = -1;
		Material mat = new Material();
		
		while((line = reader.readLine()) != null)
		{
			line = line.trim();
			String[] values = line.split("[ ]+");
			
			if(line.startsWith("newmtl "))
			{
				index++;
				matIndex.put(values[1], index);
				mat = new Material();
				materials.add(mat);
			}
			else if(line.startsWith("Ns "))
			{
				mat.setShininess(Float.parseFloat(values[1]));
			}
			else if(line.startsWith("d "))
			{
				mat.setDissolve(Float.parseFloat(values[1]));
			}
			else if(line.startsWith("Tr "))
			{
				mat.setTransparent(Float.parseFloat(values[1]));
			}
			else if(line.startsWith("illum "))
			{
				mat.setIlluminance(Float.parseFloat(values[1]));
			}
			else if(line.startsWith("Kd "))
			{
				mat.setDiffuse(new Vector3f(Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3])));
			}
			else if(line.startsWith("Ks "))
			{
				mat.setSpecular(new Vector3f(Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3])));
			}
			else if(line.startsWith("Ka "))
			{
				mat.setAmbient(new Vector3f(Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3])));
			}
			else if(line.startsWith("map_Kd "))
			{
				mat.loadTexture(values[1], 0);
			}
			else if(line.startsWith("map_Ks "))
			{
				mat.loadTexture(values[1], 1);
			}
			else if(line.startsWith("map_Ka "))
			{
				mat.loadTexture(values[1], 2);
			}
			else if(line.startsWith("map_d "))
			{
				mat.loadTexture(values[1], 3);
			}
			else if(line.startsWith("map_Ns "))
			{
				mat.loadTexture(values[1], 4);
			}
		}
		reader.close();
	}
	
	private FloatBuffer asFloatBuffer(float[] values)
	{
	   	FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
	   	buffer.put(values);
	   	buffer.flip();
	   	return buffer;
	}
	
	
	/** 
     * Generate the model list.
     */
	public void init()
	{
		glEnable(GL_TEXTURE_2D);
		obj = glGenLists(1);
		glNewList(obj, GL_COMPILE);
		{	
			glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(new float[]{20.0f, 20.0f, 5.0f, 1.0f}));
			glLight(GL_LIGHT1, GL_POSITION, asFloatBuffer(new float[]{-20.0f, -20.0f, 5.0f, 1.0f}));
			diffuseLocation = glGetUniformLocation(modelShader.getShaderProgram(), "Kd");
			specularLocation = glGetUniformLocation(modelShader.getShaderProgram(), "Ks");
			ambientLocation = glGetUniformLocation(modelShader.getShaderProgram(), "Ka");
			shininessLocation = glGetUniformLocation(modelShader.getShaderProgram(), "alpha");
			illumLocation = glGetUniformLocation(modelShader.getShaderProgram(), "illum");
			dissolveLocation = glGetUniformLocation(modelShader.getShaderProgram(), "dissolve");
			
			for(Face face : faces)
			{
				Vector3f indx = face.getIndices();
				Vector3f norm = face.getNormals();
				Vector3f tetu = face.getTextures();
				Material current = materials.get(face.getMaterials());
				
				glUniform3f(diffuseLocation, current.getDiffuse().x, current.getDiffuse().y, current.getDiffuse().z);
				glUniform3f(specularLocation, current.getSpecular().x, current.getSpecular().y, current.getSpecular().z);
				glUniform3f(ambientLocation, current.getAmbient().x, current.getAmbient().y, current.getAmbient().z);
				glUniform1f(dissolveLocation, current.getDissolve());
				glUniform1f(shininessLocation, current.getShininess());
				glUniform1f(illumLocation, current.getIlluminance());
				
				int [] textID = materials.get(face.getMaterials()).getTexImg();
				if(textID[0] >= 0)
				{
					glActiveTexture(GL_TEXTURE1); 
					glBindTexture(GL_TEXTURE_2D, textID[0]);
					glUniform1i(glGetUniformLocation(modelShader.getShaderProgram(), "texKd"), textID[0]);
							
				}
				
				glBegin(GL_TRIANGLES);
				{
					if(norm != null)
					{
						Vector3f n1 = normals.get((int) (norm.x - 1));
						glNormal3f(n1.x, n1.y, n1.z);
					}
					if(tetu != null)
					{
						Vector2f t1 = textures.get((int) (tetu.x - 1));
						glTexCoord2f(t1.x, t1.y);
					}
					if(indx != null)
					{
						Vector3f v1 = vertices.get((int) (indx.x - 1));
						glVertex3f(v1.x, v1.y, v1.z);
					}
					
					
					if(norm != null)
					{
						Vector3f n1 = normals.get((int) (norm.y - 1));
						glNormal3f(n1.x, n1.y, n1.z);
					}
					if(tetu != null)
					{
						Vector2f t1 = textures.get((int) (tetu.y - 1));
						glTexCoord2f(t1.x, t1.y);
					}
					if(indx != null)
					{
						Vector3f v1 = vertices.get((int) (indx.y - 1));
						glVertex3f(v1.x, v1.y, v1.z);
					}
					
					
					if(norm != null)
					{
						Vector3f n1 = normals.get((int) (norm.z - 1));
						glNormal3f(n1.x, n1.y, n1.z);
					}
					if(tetu != null)
					{
						Vector2f t1 = textures.get((int) (tetu.z - 1));
						glTexCoord2f(t1.x, t1.y);
					}
					if(indx != null)
					{
						Vector3f v1 = vertices.get((int) (indx.z - 1));
						glVertex3f(v1.x, v1.y, v1.z);
					}
		        }
				glEnd();
				glActiveTexture(GL_TEXTURE0); 
			}
			
		}
		glEndList();
		glDisable(GL_TEXTURE_2D);
	}
	
	
	/** 
     * Draw the model list.
     */
	public void draw()
	{
		modelShader.begin();
		glCallList(obj);
		modelShader.end();	
	}
	
	public List<Vector3f> getVertices() {
		return vertices;
	}
	public void setVertices(List<Vector3f> vertices) {
		this.vertices = vertices;
	}
	public List<Vector2f> getTextures() {
		return textures;
	}
	public void setTextures(List<Vector2f> textures) {
		this.textures = textures;
	}
	public List<Vector3f> getNormals() {
		return normals;
	}
	public void setNormals(List<Vector3f> normals) {
		this.normals = normals;
	}
	public List<Face> getFaces() {
		return faces;
	}
	public void setFaces(List<Face> faces) {
		this.faces = faces;
	}

	public ShaderProgram getModelShader() {
		return modelShader;
	}

	public void setModelShader(ShaderProgram modelShader) {
		this.modelShader = modelShader;
	}
	
}
