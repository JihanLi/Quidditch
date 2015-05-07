package main;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

import render.Background;
import render.Map;
import render.Model;
import control.Camera;
import menu.Button;
import menu.Menu;
import menu.ProgressBar;
import util.PropertiesManager;

public class Main {
	
	static PropertiesManager properties;
	
	private static enum State
	{
		LOADING, MAIN_MENU, GAME, PAUSE, TEAM;
	}

    String windowTitle = "Quidditch World Cup";
    private boolean fullscreen = false;
    private boolean closeRequested = false;
    private long lastFrameTime;
	private DisplayMode window, lastWindow;
	
	/** 
     * There is a game interface, a camera, background music, a sky box, terrain, and a set of models.
     */
    private State state = State.LOADING;
    private Camera camera = new Camera();
    
    
    private Menu loading = new Menu();
    private Menu mainMenu = new Menu(); 
    private Menu title = new Menu();
    private Menu game = new Menu();
    private Menu team = new Menu();
    private Menu pause = new Menu();
    
    private ProgressBar barBackground = new ProgressBar();
    private ProgressBar progressing = new ProgressBar();
    private int loadingCount = 0;
    
    private Button playGame = new Button();
    private Button museMusic = new Button();
    private Button quitGame = new Button();
    
    
    private Music gameMusic;
    private Background skybox;
    private Map terrain;
    
    private List<Model> models = new ArrayList<Model>();
    private UnicodeFont font;
    private DecimalFormat formatter = new DecimalFormat("#.##");
    private FloatBuffer perspectiveMatrix = BufferUtils.createFloatBuffer(16);
    private FloatBuffer orthographicMatrix = BufferUtils.createFloatBuffer(16);
    
    /** 
     * Run the main body of the game.
     */
    public void run() throws LWJGLException, FileNotFoundException, IOException, InterruptedException, SlickException {

        createWindow();
        getDelta();
        initGame();
        
        while (!closeRequested) {
        	gameControl();
            renderGL();

            Display.update();
            Display.sync(60);
        }
        
        cleanup();
    }
    

    /** 
     * Create the main window.
     */
    private void createWindow() throws IOException 
    {
        try {
        	Display.setFullscreen(false);
        	Display.setResizable(true);
        	window = new DisplayMode(PropertiesManager.getDefaultWidth(), PropertiesManager.getDefaultHeight());
            
            Display.setDisplayMode(window);
            Display.setTitle(windowTitle);
            Display.setVSyncEnabled(true);
            Display.create();
            Mouse.setGrabbed(false);
            
        } catch (LWJGLException e) {
            Sys.alert("Error", "Initialization failed!\n\n" + e.getMessage());
            System.exit(0);
        }
        
    }
    

    /** 
     * Calculate how many milliseconds have passed 
     * since last frame.
     * 
     * @return milliseconds passed since last frame 
     */
    public int getDelta() 
    {
        long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
        int delta = (int) (time - lastFrameTime);
        lastFrameTime = time;
        return delta;
    }
    
    /**
     * Using muti-threads to deal loading screen. */
    public class InitThread extends Thread{ 
        public InitThread() {
            super();
        } 

		public void run() {
	    	
			// load other resources in another thread.
	    	/*skybox.loadBackground("day");
	    	terrain.loadTerrain("heightMap");
	    	Model model = new Model();
	    	model.loadModel("dragon");
	    	models.add(model);
	    	for(int i = 0; i < 5; i++)
	    	{
		    	model = new Model();
		    	model.loadModel("dragon");
		    	models.add(model);
	    	}*/

	        camera.create();   

	    	
	        Font awtFont = new Font("Calibri", Font.BOLD,18);
	        font = new UnicodeFont(awtFont);
	        font.getEffects().add(new ColorEffect(Color.white));
	        font.addAsciiGlyphs();
            
	        /* TODO Real loading
	        while (loadingCount < 100)
	        {
	        	loadingCount++;
	        	try
				{
					Thread.sleep(50);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        */
        	loadingCount = 100;

			System.out.println("!!!!RUN INIT!!!!");
        }
    }
    
    private void startLoading() throws IOException, SlickException {
    	int i = 0;
    	
    	// load resources concerning with loading screen.
    	loading.loadMenu("loadingGame", 0, 0, window.getWidth(), window.getHeight());
    	barBackground.loadMenu("progress", (PropertiesManager.getDefaultWidth() - 764)/2, 405, 764, 58);
    	progressing.loadMenu("bar", 120, 440, PropertiesManager.getDefaultWidth() - 241, 8);

    	// Start rendering loading screen.
    	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    	glLoadIdentity();
    	loading.draw();
    	barBackground.draw(100);
    	progressing.draw(i);
    	Display.update();
        Display.sync(60);
        
        // load other resources concerning opengl.
        mainMenu.loadMenu("mainMenu", 0, 0, window.getWidth(), window.getHeight());
    	title.loadMenu("title", 232, 50, 496, 172);
    	
    	playGame.loadButton("button1", "button2", 355, 270, 250, 90);
    	museMusic.loadButton("button1", "button2", 355, 350, 250, 90);
    	quitGame.loadButton("button1", "button2", 355, 430, 250, 90);

//    	gameMusic = new Music("res/autumn.ogg");
//    	gameMusic.loop(1.0f, 0.1f);
    	
		// Continue rendering loading screen until resources finishing loading.
        while(loadingCount < 100) {
        	if(i < 100) i++;
        	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        	glLoadIdentity();
        	loading.draw();
        	barBackground.draw(100);
        	progressing.draw(i);
        	Display.update();
            Display.sync(60);
        }
		
	}
    
    /** 
     * Initialize all the objects of the game.
     */
    public void initGame() throws LWJGLException, SlickException, FileNotFoundException, IOException 
    {   
    	initGL();
    	
    	loadingCount = 40;
		
		// New thread for loading resources.
    	Thread init = new InitThread();
    	init.start();
    	
    	// Loading screen.
    	startLoading();
    	
    	// Load other things.
    	font.loadGlyphs();
        state = State.MAIN_MENU;
    }
   
   
    /** 
     * Initialize GL parameters.
     * @throws LWJGLException, SlickException, FileNotFoundException, IOException  
     */
    public void initGL() throws LWJGLException, SlickException, FileNotFoundException, IOException 
    {
    	
    	reinitGL();
        
        gluLookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f);
        
        initLight();
        
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClearDepth(1.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        glEnable(GL_ALPHA_TEST);
        
        
        glEnable(GL_POINT_SMOOTH);
        glEnable(GL_LINE_SMOOTH);
        
        glHint(GL_POINT_SMOOTH_HINT,GL_NICEST);
        glHint(GL_LINE_SMOOTH_HINT,GL_NICEST);
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
        
    }
    
    
    /** 
     * Initialize light parameters.
     */
    private FloatBuffer asFloatBuffer(float[] values)
    {
    	FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
    	buffer.put(values);
    	buffer.flip();
    	return buffer;
    }
    
    public void initLight()
    {
        glShadeModel(GL_SMOOTH);  
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glLight(GL_LIGHT0, GL_AMBIENT, asFloatBuffer(new float[]{0.1f, 0.1f, 0.1f, 1.0f}));
        glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(new float[]{0.0f, 2000.0f, 0.0f, 0.5f}));
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT, GL_DIFFUSE);    
        
    }
    
    
    /** 
     * Re-initialize GL parameters.
     * @throws LWJGLException 
     */
    public void reinitGL() throws LWJGLException
    {
    	int width = Display.getWidth();
        int height = Display.getHeight();
        
        glViewport(0, 0, width, height);
        
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45.0f * height / PropertiesManager.getDefaultHeight(), ((float) width / (float) height), 0.1f, 100000000.0f);
            
        glGetFloat(GL_PROJECTION_MATRIX, perspectiveMatrix);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, height, 0, 1, -1);
        glGetFloat(GL_PROJECTION_MATRIX, orthographicMatrix);
        glLoadMatrix(perspectiveMatrix);
        
        glMatrixMode(GL_TEXTURE);
        glLoadIdentity();
        
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }
    
    
    /**
     * Control the game by key and mouse event under different game state. 
     * Also check if the window is resized or inactive.
     * @throws LWJGLException, InterruptedException 
     */
    public void gameControl() throws LWJGLException, InterruptedException 
    {
    	switch(state)
    	{
    	case LOADING:
    		while (Keyboard.next()) 
	        {
	            if (Keyboard.getEventKeyState()) 
	            {
		    		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
		    		{
		    			closeRequested = true;
		    		}
		    		else if(Keyboard.isKeyDown(Keyboard.KEY_RETURN))
		    		{
		    			state = State.MAIN_MENU;
		    			camera.setFlag(false);
		    		}
		    		else if (Keyboard.getEventKey() == Keyboard.KEY_F)
	                {
		    			fullscreen = !fullscreen;
		    			camera.setFlag(false);
		    	        
		    	    	if(fullscreen)
		    	    	{
		    	    		lastWindow = new DisplayMode(Display.getWidth(), Display.getHeight());
		    	    		window = Display.getDesktopDisplayMode();
		    	    		Display.setDisplayModeAndFullscreen(window);
		    	    		Display.setResizable(false);
		    	    	}
		    	    	else
		    	    	{
		    	    		window = lastWindow;
		    	    		Display.setDisplayMode(window);
		    	    		Display.setFullscreen(false);
		    	    		Display.setResizable(true);
		    	    	}
		    	    	
		    	    	reinitGL();
	                }
	            }
	        }
    		break;
    	case MAIN_MENU:
    		while (Keyboard.next()) 
	        {
	            if (Keyboard.getEventKeyState()) 
	            {
		    		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
		    		{
		    			closeRequested = true;
		    		}
		    		else if(Keyboard.isKeyDown(Keyboard.KEY_RETURN))
		    		{
		    			state = State.GAME;
		    			camera.setFlag(false);
		    		}
		    		else if (Keyboard.getEventKey() == Keyboard.KEY_F)
	                {
		    			fullscreen = !fullscreen;
		    			camera.setFlag(false);
		    	        
		    	    	if(fullscreen)
		    	    	{
		    	    		lastWindow = new DisplayMode(Display.getWidth(), Display.getHeight());
		    	    		window = Display.getDesktopDisplayMode();
		    	    		Display.setDisplayModeAndFullscreen(window);
		    	    		Display.setResizable(false);
		    	    	}
		    	    	else
		    	    	{
		    	    		window = lastWindow;
		    	    		Display.setDisplayMode(window);
		    	    		Display.setFullscreen(false);
		    	    		Display.setResizable(true);
		    	    	}
		    	    	
		    	    	reinitGL();
	                }
	            }
	        }
    		break;
    	case GAME:
    		camera.acceptInput(getDelta());
    		while (Keyboard.next()) 
	        {
	            if (Keyboard.getEventKeyState()) 
	            {
	                if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
	                {
	                	state = State.MAIN_MENU;
	                	break;
	                }
	                else if (Keyboard.getEventKey() == Keyboard.KEY_P)
	                    snapshot();
	                else if (Keyboard.getEventKey() == Keyboard.KEY_F)
	                {
	                	fullscreen = !fullscreen;
	                	camera.setFlag(false);
	                	
	                	if(fullscreen)
		    	    	{
	                		lastWindow = new DisplayMode(Display.getWidth(), Display.getHeight());
		    	    		window = Display.getDesktopDisplayMode();
		    	    		Display.setDisplayModeAndFullscreen(window);
		    	    		Display.setResizable(false);
		    	    	}
		    	    	else
		    	    	{
		    	    		window = lastWindow;
		    	    		Display.setDisplayMode(window);
		    	    		Display.setFullscreen(false);
		    	    		Display.setResizable(true);
		    	    	}
	                	reinitGL();
	                }
	            }
	        }
	        
	        break;
		default:
			break;
    	}
        if (Display.isCloseRequested()) {
            closeRequested = true;
        }
        if (Display.wasResized()) {
        	reinitGL();
        }
        if (Display.isActive()) {
        	/*if(!gameMusic.playing())
        		gameMusic.resume();*/
        }
        else
        {
        	//gameMusic.pause();
        }
    }
    

    /**
     * Render all the objects in the game. 
     * It also changes the sky box background image due to the daytime.
     * @throws FileNotFoundException, IOException, LWJGLException
     */
    private void renderGL() throws FileNotFoundException, IOException, LWJGLException 
    {
    	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    	glLoadIdentity();
		
    	switch(state)
    	{
    	case LOADING:   			
    		loading.draw();
    		break;
    	case MAIN_MENU:   			
    		mainMenu.draw();
    		title.draw();
    		
    		playGame.draw();
    		museMusic.draw();
    		quitGame.draw();
    		if(playGame.hasClicked() == 2)
    			state = State.GAME;
    		if(quitGame.hasClicked() == 2)
    			cleanup();
    		break;
    	case GAME:
    		//reinitGL();
    		loading.draw();
    		break;
    	case TEAM:   			
    		team.draw();
    		break;
    	case PAUSE:   			
    		
    		break;
		default:
			break;
    	}
    	
    }
 
    /**
     * Take a snapshot of the game.
     */
    public void snapshot() 
    {
        System.out.println("Taking a snapshot ... snapshot.png");

        glReadBuffer(GL_FRONT);

        int width = Display.getDisplayMode().getWidth();
        int height= Display.getDisplayMode().getHeight();
        int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer );

        File file = new File("snapshot.png"); // The file to save to.
        String format = "PNG"; // Example: "PNG" or "JPG"
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
   
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                int i = (x + (width * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }
           
        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) { e.printStackTrace(); }
    }
    

    /**
     * Destroy and clean up resources.
     */
    private void cleanup() 
    {
    	/*terrain.getMapShader().destroy();
    	for(int i = 0; i < models.size(); i++)
    	{
    		models.get(i).getModelShader().destroy();
    	}*/
        Display.destroy();
        System.exit(1);
    }
    
    
    /**
     * Main function of the game.
     * @throws LWJGLException, FileNotFoundException, IOException, InterruptedException, SlickException 
     */
    public static void main(String[] args) throws LWJGLException, FileNotFoundException, IOException, InterruptedException, SlickException 
    {
    	//properties = PropertiesManager.getInstance();
        new Main().run();
    }
    
}    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

