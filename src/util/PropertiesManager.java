package util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.Music;
import org.newdawn.slick.UnicodeFont;

import control.Camera;
import render.Background;
import render.Map;
import render.Model;


public class PropertiesManager {
	
	private static PropertiesManager m_instance = null;
	private static Properties properties;
	private String propertiesFileName = "/configuration.properties";
    
	public static PropertiesManager getInstance() throws IOException {
		if (m_instance == null) {
			m_instance = new PropertiesManager();
		}
		return m_instance;
	}

	private PropertiesManager() throws IOException {
		properties = new Properties();
		InputStream in = this.getClass().getResourceAsStream(propertiesFileName);
		properties.load(in);
		in.close();
	}
}
