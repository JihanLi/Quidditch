package edu.columbia.quidditch.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.basic.Material;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.render.screen.LoadScreen;

/**
 * Load obj and mtl file
 * @author Yuqing Guan
 *
 */
public class ObjLoader
{
	private ArrayList<Vector3f> verList, texList, norList;
	private ArrayList<ArrayList<ArrayList<Vector3i> > > meshList;
	
	private ArrayList<String> mtlList;
	private HashMap<String, Material> mtlMap;
	
	public static ObjLoader create(String objName)
	{
		try
		{
			LoadScreen.log("Loading model from " + objName);
			return new ObjLoader(objName);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
	/**
	 * Load obj file
	 * @param objName file name
	 * @throws IOException
	 */
	public ObjLoader(String objName) throws IOException
	{
		File objFile = new File(objName);
		
		verList = new ArrayList<Vector3f>();;
		texList = new ArrayList<Vector3f>();;
		norList = new ArrayList<Vector3f>();;
		
		meshList = new ArrayList<ArrayList<ArrayList<Vector3i> > >();
		
		mtlList = new ArrayList<String>();
		mtlMap = new HashMap<String, Material>();
		
		ArrayList<ArrayList<Vector3i> > mesh = new ArrayList<ArrayList<Vector3i> >();
		BufferedReader reader = new BufferedReader(new FileReader(objFile));
		String line;
		
		// Loop until the end of file
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			
			// Split the line
			String[] array = line.split("[ ]+");
			
			if (array.length < 1)
			{
				continue;
			}
			
			if (array[0].equals("mtllib")) // mtl file name
			{
				loadMtl(objFile.getParent() + File.separator + exceptFirst(line));
			}
			if (array[0].equals("v")) // vertices
			{
				float x = Float.parseFloat(array[1]);
				float y = Float.parseFloat(array[2]);
				float z = Float.parseFloat(array[3]);
				
				Vector3f ver = new Vector3f(x, y, z);
				verList.add(ver);
			}
			else if (array[0].equals("vt")) // UV coordinates
			{
				float x = Float.parseFloat(array[1]);
				float y = 1.0f - Float.parseFloat(array[2]);
				float z = 0.0f;
				
				Vector3f tex = new Vector3f(x, y, z);
				texList.add(tex);
			}
			else if (array[0].equals("vn")) // vertex normals
			{
				float x = Float.parseFloat(array[1]);
				float y = Float.parseFloat(array[2]);
				float z = Float.parseFloat(array[3]);
				
				Vector3f nor = new Vector3f(x, y, z);
				norList.add(nor);
			}
			else if (array[0].equals("g")) // a new mesh
			{
				if (!mesh.isEmpty())
				{
					meshList.add(mesh);
				}
				
				mesh = new ArrayList<ArrayList<Vector3i> >();
			}
			else if (array[0].equals("usemtl")) // material for current mesh
			{
				mtlList.add(exceptFirst(line));
			}
			else if (array[0].equals("f")) // face
			{
				ArrayList<Vector3i> face = new ArrayList<Vector3i>();
				
				for (int i = 1; i < array.length; ++i)
				{
					String token = array[i];
					String[] tokenArray = token.split("/");
					
					int v, vt, vn;
					
					v = Integer.parseInt(tokenArray[0]) - 1;
					vt = vn = -1;
					
					if (tokenArray.length > 1 && !tokenArray[1].trim().equals("") )
					{
						vt = Integer.parseInt(tokenArray[1]) - 1;
					}
					
					if (tokenArray.length > 2)
					{
						vn = Integer.parseInt(tokenArray[2]) - 1;
					}
					
					face.add(new Vector3i(v, vt, vn));
				}
				
				mesh.add(face);
			}
		}
		
		if (!mesh.isEmpty())
		{
			meshList.add(mesh);
		}
		
		reader.close();
	}
	
	/**
	 * Load mtl file
	 * @param mtlName file name
	 * @throws IOException
	 */
	private void loadMtl(String mtlName) throws IOException
	{
		File mtlFile = new File(mtlName);
		String texturePath = mtlFile.getParent() + File.separator + "textures" + File.separator;
		
		BufferedReader reader = new BufferedReader(new FileReader(mtlFile));
		String line;
		
		Material material = null;
		
		// Loop until the end of file
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			
			// Split the line
			String[] array = line.split("[ ]+");
			
			if (array.length < 1)
			{
				continue;
			}
			
			if (array[0].equals("newmtl")) // new material
			{
				material = new Material();
				mtlMap.put(exceptFirst(line), material);
			}
			else if (array[0].equals("map_Kd")) // image file
			{
				Texture texture = Texture.createFromFile(texturePath + exceptFirst(line));
				material.setTexture(texture);
			}
			else if (array[0].equals("Kd")) // diffuse color
			{
				float r = Float.parseFloat(array[1]);
				float g = Float.parseFloat(array[2]);
				float b = Float.parseFloat(array[3]);
				
				material.setDiffuse(r, g, b);
			}
			else if (array[0].equals("Ka")) // ambient color
			{
				float r = Float.parseFloat(array[1]);
				float g = Float.parseFloat(array[2]);
				float b = Float.parseFloat(array[3]);
				
				material.setAmbient(r, g, b);
			}
			else if (array[0].equals("Ks")) // specular color
			{
				float r = Float.parseFloat(array[1]);
				float g = Float.parseFloat(array[2]);
				float b = Float.parseFloat(array[3]);
				
				material.setSpecular(r, g, b);
			}
			else if (array[0].equals("d") || array[0].equals("Tr")) // transparency
			{
				float t = Float.parseFloat(array[1]);
				
				material.setTransparency(t);
			}
		}
		
		reader.close();
	}
	
	/**
	 * Get the string after the first continuous spaces
	 * in order to parse file names with spaces
	 * @param line
	 * @return
	 */
	private String exceptFirst(String line)
	{
		return line.substring(line.indexOf(" ") + 1);
	}
	
	public ArrayList<Vector3f> getVerList()
	{
		return verList;
	}
	
	public ArrayList<Vector3f> getTexList()
	{
		return texList;
	}
	
	public ArrayList<Vector3f> getNorList()
	{
		return norList;
	}
	
	public ArrayList<ArrayList<ArrayList<Vector3i>>> getMeshList()
	{
		return meshList;
	}
	
	public ArrayList<String> getMtlList()
	{
		return mtlList;
	}
	
	public HashMap<String, Material> getMtlMap()
	{
		return mtlMap;
	}
}
