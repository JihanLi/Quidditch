/** 
 * Shading Tests
 * 
 * Author: Jihan Li
 * 
 * This is the shader program class. It creates, enables, disables, and destroy the shader program.
 * 
 * Reference: 
 * Class start codes
 * 
 */

package render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.util.vector.Matrix4f;

/**
 * Loads GLSL vertex and fragment shader code and compiles them
 * Some of the code are borrowed online: https://searchcode.com/codesearch/view/51724444/
 * 
 */
public class ShaderProgram {

    public int program;
    public int vertex;
    public int fragment;

    protected String s;
	protected static FloatBuffer buff;    
	

	public void create(String filename) throws LWJGLException, IOException {

        vertex = createShader(filename, GL_VERTEX_SHADER);
        fragment = createShader(filename, GL_FRAGMENT_SHADER);

        program = glCreateProgram();

        glAttachShader(program, vertex);
        glAttachShader(program, fragment);

        glLinkProgram(program);

        String log = glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH)); 
        if (log != null && log.trim().length() != 0)
            s += log;

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE)
            System.err.println("Could not link shader program\n" + s);

        glDetachShader(program, vertex);
        glDetachShader(program, fragment);
        glDeleteShader(vertex);
        glDeleteShader(fragment);

    }

    /**
     * Compile the shader's source code and return its ID
     * 
     * @param source
     * @param type
     * @return
     * @throws IOException 
     */
    protected int createShader(String filename, int type) throws LWJGLException, IOException {
        int shader = glCreateShader(type);
        
        StringBuilder source = new StringBuilder();
        BufferedReader reader = null;
        
        if(type == GL_VERTEX_SHADER)
        	reader = new BufferedReader(new FileReader("shaders/" + filename + ".vert"));
        else if(type == GL_FRAGMENT_SHADER)
        	reader = new BufferedReader(new FileReader("shaders/" + filename + ".frag"));
		String line;
		while((line = reader.readLine()) != null)
		{
			source.append(line).append("\n");
		}
		reader.close();
        
        
        glShaderSource(shader, source);
        glCompileShader(shader);

        String log = glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH));
        if (log != null && log.trim().length() != 0)
            System.out.println(getName(type) + " error: " + log);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
            System.err.println(getName(type) + " did not compile\n" + log);

        return shader;
    }

    protected String getName(int shaderType) {
        if (shaderType == GL_VERTEX_SHADER)
            return "GL_VERTEX_SHADER";
        if (shaderType == GL_FRAGMENT_SHADER)
            return "GL_FRAGMENT_SHADER";
        else
            return "shader";
    }

    /**
     * Starts the shader program, call before end()
     */
    public void begin() {
        glUseProgram(program);
    }

    /**
     * Ends shader program, call after begin()
     */
    public void end() {
        glUseProgram(0);
    }

    /**
     * Destroys shader program, call after begin()
     */
    public void destroy() {
        glDeleteProgram(program);
    }

    public void setUniformi(int loc, int i) {
        if (loc == -1) return;
        glUniform1i(loc, i);
    }

    public void setUniform3f(String name, float v1, float v2, float v3) {
        setUniform3f(glGetUniformLocation(program, name), v1, v2, v3);
    }
    
    public void setUniform3f(int loc, float v1, float v2, float v3) {
        if ( loc == -1 ) return;
        glUniform3f(loc, v1, v2, v3);
    }
    
    public void setUniformMatrix(int loc, boolean transposed, Matrix4f mat) {
        if (loc == -1) return;
        if (buff == null)
            // 4 x 4 matrix = 16
            buff = BufferUtils.createFloatBuffer(16);
        
        buff.clear();
        mat.store(buff);
        buff.flip();
        glUniformMatrix4(loc, transposed, buff);
    }
    
	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}

	public static FloatBuffer getBuff() {
		return buff;
	}

	public static void setBuff(FloatBuffer buff) {
		ShaderProgram.buff = buff;
	}

	public int getShaderProgram() {
		return program;
	}

	public int getVertex() {
		return vertex;
	}

	public int getFragment() {
		return fragment;
	}

    public void setShaderProgram(int program) {
		this.program = program;
	}

	public void setVertex(int vertex) {
		this.vertex = vertex;
	}

	public void setFragment(int fragment) {
		this.fragment = fragment;
	}


}