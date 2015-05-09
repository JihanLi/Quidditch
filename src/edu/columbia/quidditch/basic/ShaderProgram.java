package edu.columbia.quidditch.basic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import edu.columbia.quidditch.model.LoadingScreen;

/**
 * ShaderProgram class
 * @author Yuqing Guan
 * 
 */
public class ShaderProgram
{
	private int vertexId, fragmentId, programId;
	
	/**
	 * Read and compile shaders from files
	 * @param vertexName
	 * @param fragmentName
	 * @param attributes
	 * @return
	 */
	public static ShaderProgram createFromFiles(String vertexName, String fragmentName, Map<Integer, String> attributes)
	{
		try
		{
			LoadingScreen.log("Loading shaders from " + vertexName + " and " + fragmentName);
			
			String vertexSrc = readFile(vertexName);
			String fragmentSrc = readFile(fragmentName);
			
			return new ShaderProgram(vertexSrc, fragmentSrc, attributes);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
			return null;
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
	/**
	 * Read text file
	 * @param name
	 * @return
	 * @throws IOException
	 */
	private static String readFile(String name) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(name));
		
		StringBuffer buffer = new StringBuffer();
		String line;
		
		while ((line = reader.readLine()) != null)
		{
			buffer.append(line).append("\n");
		}
		
		reader.close();
		
		return buffer.toString();
	}
	
	/**
	 * Compile shaders from text
	 * @param vertexName
	 * @param fragmentName
	 * @param attributes
	 * @return
	 */
	public ShaderProgram(String vertexSrc, String fragmentSrc, Map<Integer, String> attributes) throws LWJGLException
	{
		
		// Compile vertex and fragment shader
		vertexId = compileShader(vertexSrc, GL20.GL_VERTEX_SHADER);
		fragmentId = compileShader(fragmentSrc, GL20.GL_FRAGMENT_SHADER);
		
		programId = GL20.glCreateProgram(); // Create a program
		
		// Attach shader to the program
		GL20.glAttachShader(programId, vertexId);
		GL20.glAttachShader(programId, fragmentId);
		
		// Set attributes
		if (attributes != null)
		{
			for (int key : attributes.keySet())
			{
				String val = attributes.get(key);
				GL20.glBindAttribLocation(programId, key, val);
			}
		}
		
		// Link the shader
		GL20.glLinkProgram(programId);
		
		checkError(programId, GL20.GL_LINK_STATUS, -1);
		
		// Detach and delete the linked shaders
		GL20.glDetachShader(programId, vertexId);
		GL20.glDetachShader(programId, fragmentId);
		
		GL20.glDeleteShader(vertexId);
		GL20.glDeleteShader(fragmentId);
	}
	
	/**
	 * Compile vertex or fragment shader
	 * @param source
	 * @param type
	 * @return
	 * @throws LWJGLException
	 */
	protected int compileShader(String source, int type) throws LWJGLException
	{
		int shaderId = GL20.glCreateShader(type);
		
		GL20.glShaderSource(shaderId, source);
		GL20.glCompileShader(shaderId);
		
		checkError(shaderId, GL20.GL_COMPILE_STATUS, type);
		
		return shaderId;
	}
	
	/**
	 * Display potential errors when compiling or linking
	 * @param id
	 * @param status
	 * @param compileType
	 * @throws LWJGLException
	 */
	private void checkError(int id, int status, int compileType) throws LWJGLException
	{
		String log;
				
		String statusName;
		int result;
		
		if (status == GL20.GL_COMPILE_STATUS)
		{
			statusName = "compiling ";
			log = GL20.glGetShaderInfoLog(id, GL20.glGetShaderi(id, GL20.GL_INFO_LOG_LENGTH));
			
			if (compileType == GL20.GL_VERTEX_SHADER)
			{
				statusName += "vertex shader";
			}
			else if (compileType == GL20.GL_FRAGMENT_SHADER)
			{
				statusName += "fragment shader";
			}
			
			result = GL20.glGetShaderi(id, status);
		}
		else
		{
			statusName = "linking";
			log = GL20.glGetProgramInfoLog(id, GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH));
			result = GL20.glGetProgrami(id, status);
		}
		
		if (log == null || log.trim().length() == 0)
		{
			log = statusName;
		}
		else
		{
			log = statusName + ": " + log;
		}
		
		if (result == GL11.GL_FALSE)
		{
			throw new LWJGLException("Failure in " + log);
		}
	}
	
	public void bind()
	{
		GL20.glUseProgram(programId);
	}
	
	public static void unbind()
	{
		GL20.glUseProgram(0);
	}
	
	/**
	 * Set uniform integer (or texture) for a specific variable name
	 * @param name
	 * @param val
	 */
	public void setUniformi(String name, int val)
	{
		int loc = GL20.glGetUniformLocation(programId, name);
		GL20.glUniform1i(loc, val);
	}
	
	/**
	 * Set uniform float for a specific variable name
	 * @param name
	 * @param val
	 */
	public void setUniformf(String name, float val)
	{
		int loc = GL20.glGetUniformLocation(programId, name);
		GL20.glUniform1f(loc, val);
	}
}