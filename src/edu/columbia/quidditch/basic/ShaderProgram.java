package edu.columbia.quidditch.basic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.LWJGLException;

import edu.columbia.quidditch.render.screen.LoadScreen;

/**
 * ShaderProgram class
 * 
 * @author Yuqing Guan
 * 
 */
public class ShaderProgram
{
	private static final String DEFAULT_VERTEX_SHADER_NAME = "shaders/default.vsh";
	private static final String DEFAULT_FRAGMENT_SHADER_NAME = "shaders/default.fsh";

	private static ShaderProgram defaultShader = null;

	public static ShaderProgram getDefaultShader()
	{
		if (defaultShader == null)
		{
			defaultShader = ShaderProgram.createFromFiles(
					DEFAULT_VERTEX_SHADER_NAME, DEFAULT_FRAGMENT_SHADER_NAME,
					null);
		}

		return defaultShader;
	}

	/**
	 * Read and compile shaders from files
	 * 
	 * @param vertexName
	 * @param fragmentName
	 * @param attributes
	 * @return
	 */
	public static ShaderProgram createFromFiles(String vertexName,
			String fragmentName, Map<Integer, String> attributes)
	{
		try
		{
			LoadScreen.log("Loading shaders from " + vertexName + " and "
					+ fragmentName);

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
	 * 
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

	private int vertexId, fragmentId, programId;

	/**
	 * Compile shaders from text
	 * 
	 * @param vertexName
	 * @param fragmentName
	 * @param attributes
	 * @return
	 */
	public ShaderProgram(String vertexSrc, String fragmentSrc,
			Map<Integer, String> attributes) throws LWJGLException
	{

		// Compile vertex and fragment shader
		vertexId = compileShader(vertexSrc, GL_VERTEX_SHADER);
		fragmentId = compileShader(fragmentSrc, GL_FRAGMENT_SHADER);

		programId = glCreateProgram(); // Create a program

		// Attach shader to the program
		glAttachShader(programId, vertexId);
		glAttachShader(programId, fragmentId);

		// Set attributes
		if (attributes != null)
		{
			for (int key : attributes.keySet())
			{
				String val = attributes.get(key);
				glBindAttribLocation(programId, key, val);
			}
		}

		// Link the shader
		glLinkProgram(programId);

		checkError(programId, GL_LINK_STATUS, -1);

		// Detach and delete the linked shaders
		glDetachShader(programId, vertexId);
		glDetachShader(programId, fragmentId);

		glDeleteShader(vertexId);
		glDeleteShader(fragmentId);
	}

	/**
	 * Compile vertex or fragment shader
	 * 
	 * @param source
	 * @param type
	 * @return
	 * @throws LWJGLException
	 */
	protected int compileShader(String source, int type) throws LWJGLException
	{
		int shaderId = glCreateShader(type);

		glShaderSource(shaderId, source);
		glCompileShader(shaderId);

		checkError(shaderId, GL_COMPILE_STATUS, type);

		return shaderId;
	}

	/**
	 * Display potential errors when compiling or linking
	 * 
	 * @param id
	 * @param status
	 * @param compileType
	 * @throws LWJGLException
	 */
	private void checkError(int id, int status, int compileType)
			throws LWJGLException
	{
		String log;

		String statusName;
		int result;

		if (status == GL_COMPILE_STATUS)
		{
			statusName = "compiling ";
			log = glGetShaderInfoLog(id, glGetShaderi(id, GL_INFO_LOG_LENGTH));

			if (compileType == GL_VERTEX_SHADER)
			{
				statusName += "vertex shader";
			}
			else if (compileType == GL_FRAGMENT_SHADER)
			{
				statusName += "fragment shader";
			}

			result = glGetShaderi(id, status);
		}
		else
		{
			statusName = "linking";
			log = glGetProgramInfoLog(id, glGetProgrami(id, GL_INFO_LOG_LENGTH));
			result = glGetProgrami(id, status);
		}

		if (log == null || log.trim().length() == 0)
		{
			log = statusName;
		}
		else
		{
			log = statusName + ": " + log;
		}

		if (result == GL_FALSE)
		{
			throw new LWJGLException("Failure in " + log);
		}
	}

	public void bind()
	{
		glUseProgram(programId);
	}

	public static void unbind()
	{
		glUseProgram(0);
	}

	/**
	 * Set uniform integer (or texture) for a specific variable name
	 * 
	 * @param name
	 * @param val
	 */
	public void setUniformi(String name, int val)
	{
		int loc = glGetUniformLocation(programId, name);
		glUniform1i(loc, val);
	}

	/**
	 * Set uniform float for a specific variable name
	 * 
	 * @param name
	 * @param val
	 */
	public void setUniformf(String name, float val)
	{
		int loc = glGetUniformLocation(programId, name);
		glUniform1f(loc, val);
	}
}