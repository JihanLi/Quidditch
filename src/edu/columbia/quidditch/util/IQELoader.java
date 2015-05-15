package edu.columbia.quidditch.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.util.vector.Vector3f;

import edu.columbia.quidditch.basic.Material;
import edu.columbia.quidditch.basic.Texture;
import edu.columbia.quidditch.render.screen.LoadScreen;

/**
 * Load iqe and mtl file
 * 
 * @author Yuqing Guan
 * 
 */
public class IQELoader
{
	private ArrayList<float[]> jointPQList;
	private ArrayList<Integer> jointParentList;

	private ArrayList<HashMap<Integer, Float>> weightList;

	private ArrayList<Vector3f> verList, texList, norList;
	private ArrayList<ArrayList<ArrayList<Integer>>> meshList;

	private ArrayList<String> mtlList;
	private HashMap<String, Material> mtlMap;

	private HashMap<String, Texture> textureMap;

	public static IQELoader create(String iqeName)
	{
		try
		{
			LoadScreen.log("Loading model from " + iqeName);
			return new IQELoader(iqeName);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	/**
	 * Load iqe file
	 * 
	 * @param iqeName
	 *            file name
	 * @throws IOException
	 */
	public IQELoader(String iqeName) throws IOException
	{
		File iqeFile = new File(iqeName);

		jointParentList = new ArrayList<Integer>();
		jointPQList = new ArrayList<float[]>();

		verList = new ArrayList<Vector3f>();
		texList = new ArrayList<Vector3f>();
		norList = new ArrayList<Vector3f>();

		meshList = new ArrayList<ArrayList<ArrayList<Integer>>>();

		mtlList = new ArrayList<String>();
		mtlMap = new HashMap<String, Material>();

		weightList = new ArrayList<HashMap<Integer, Float>>();

		ArrayList<ArrayList<Integer>> mesh = new ArrayList<ArrayList<Integer>>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(iqeFile), "utf-8"));
		String line, nextLine;

		String mtlName = iqeName.substring(0, iqeName.length() - 3) + "mtl";
		loadMtl(mtlName);

		int offset = 0;

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

			if (array[0].equals("joint")) // joint
			{
				int parent = Integer.parseInt(array[2]);
				jointParentList.add(parent);

				nextLine = reader.readLine().trim();
				array = nextLine.split("[ ]+");

				float[] pq = new float[7];
				jointPQList.add(pq);

				for (int i = 0; i < 7; ++i)
				{
					pq[i] = Float.parseFloat(array[i + 1]);
				}
			}
			else if (array[0].equals("mesh")) // mesh
			{
				if (!mesh.isEmpty())
				{
					meshList.add(mesh);
				}

				mesh = new ArrayList<ArrayList<Integer>>();

				offset = verList.size();
			}
			else if (array[0].equals("material")) // material for current mesh
			{
				String mtl = exceptFirst(line);
				mtl = mtl.substring(1);
				mtl = mtl.substring(0, mtl.length() - 1);

				mtlList.add(mtl);
			}
			else if (array[0].equals("vp")) // vertices
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
				float y = Float.parseFloat(array[2]);
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
			else if (array[0].equals("vb")) // bone weights
			{
				HashMap<Integer, Float> weightMap = new HashMap<Integer, Float>();
				weightList.add(weightMap);

				for (int i = 1; i < array.length; i += 2)
				{
					weightMap.put(Integer.parseInt(array[i]),
							Float.parseFloat(array[i + 1]));
				}
			}
			else if (array[0].equals("fm")) // face
			{
				ArrayList<Integer> face = new ArrayList<Integer>();

				for (int i = 1; i < array.length; ++i)
				{
					face.add(Integer.parseInt(array[i]) + offset);
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
	 * 
	 * @param mtlName
	 *            file name
	 * @throws IOException
	 */
	private void loadMtl(String mtlName) throws IOException
	{
		File mtlFile = new File(mtlName);
		String texturePath = mtlFile.getParent() + File.separator + "textures"
				+ File.separator;

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(mtlFile), "utf-8"));
		String line;

		Material material = null;

		textureMap = new HashMap<String, Texture>();

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
				String textureName = texturePath + exceptFirst(line);
				Texture texture;

				if (textureMap.containsKey(textureName))
				{
					texture = textureMap.get(textureName);
				}
				else
				{
					texture = Texture.createFromFile(textureName);
					textureMap.put(textureName, texture);
				}

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
	 * Get the string after the first continuous spaces in order to parse file
	 * names with spaces
	 * 
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

	public ArrayList<ArrayList<ArrayList<Integer>>> getMeshList()
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

	public ArrayList<float[]> getJointPQList()
	{
		return jointPQList;
	}

	public ArrayList<Integer> getJointParentList()
	{
		return jointParentList;
	}

	public ArrayList<HashMap<Integer, Float>> getWeightList()
	{
		return weightList;
	}
}
