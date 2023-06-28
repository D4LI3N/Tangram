package Final;

import com.jogamp.opengl.*;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

public class Tangram extends GLCanvas implements GLEventListener, KeyListener, MouseListener {
    private final String helpMessage = "MAIN CONTROLS:\nQ & E = Move highlight\nW & A = Selection scale\n A & D = Selection rotate\n1 = Spawn triangle\n2 = Spawn square\n3 = Spawn Parelellogram\n4 = Spawn Flipped Parelellogram\n0 = Remove shape\n\nBONUS CONTROLS:\nArrow Keys = Allows rotation of the blueprint on X & Y axes (works only after the level is passed or during Check Mode)\nLeft Mouse Click = Prints default selected shape parameters\nRight Mouse Click = Prints selected shape parameters\n\nUI & BUTTONS\nHelp = This window\nLevel X = Start or Reload the Level X\nCheck = Checks if the shape is filled properly\nCheckboxes = Self explanatory";
    private int windowWidth = 640;
    private int windowHeight = 410;
    private static final int FPS = 60;
    private float angleA = 0.0f;
    private float angleX = 0.0f;
    private float angleY = 0.0f;
    private float angleZ = 0.0f;
    private boolean checkShape = false;
    private boolean shapeGood = false;

    private FPSAnimator animator;
    private JFrame frame;
    private GLCanvas canvas;
    private GLU glu;
    private float aspect;
    private float aspectR;
    private TextRenderer gameFinishedText;
    private TextRenderer signature;

    private JButton Level_1B;
    private JButton Level_2B;
    private JButton Level_3B;
    private JButton checkB;
    private JButton helpB;

    private JCheckBox lightOnOff;
    private JCheckBox ambientGlobalLighting;
    private JCheckBox diffuseLighting;
    private JCheckBox specularLighting;
    private JCheckBox ambientLight;

    private final String[] textureFileNames = {
            "Controls.png",
            "Level_1.png",
            "Level_2.png",
            "Level_3.png"
    };
    private Texture[] textures = new Texture[textureFileNames.length];
    private int[] tempArr = {0,0,0,0,0};

    private int selection = 0; //starting selection
    private float[][][] levels = {
            {//bird  shape, size, rotation, tX, tY,  r,g,b
                    {1, 0.26f, 0f, -0.424f, 0.133f, 255, 0, 0},
                    {1, 0.35f, 45f, -0.260f, 0.046f, 0, 255, 0},
                    {1, 0.49f, 0f, -0.011f, -0.021f, 0, 0, 255},
                    {1, 0.49f, 180f, 0.369f, 0.098f, 0, 255, 255},
                    {1, 0.24f, 90f, -0.161f, -0.305f, 255, 100, 0},
                    {2, 0.24f, 180f, 0.057f, -0.333f, 255, 0, 255},
                    {4, 0.24f, 180f, 0.180f, -0.578f, 0, 0, 0}
            },
            {//fish
                    {1, 0.32f, 0f, -0.13f, 0.15f, 255, 0, 0},
                    {1, 0.32f, 90f, -0.13f, -0.10f, 0, 255, 0},
                    {1, 0.25f, 45.f, 0.0439f, 0.119f, 0, 0, 255},
                    {4, 0.18f, 90f, 0.09f, -0.066f, 0, 255, 255},
                    {1, 0.18f, 0f, 0.299f, 0.185f, 255, 100, 0},
                    {2, 0.18f, 0f, 0.279f, 0.022f, 255, 0, 255},
                    {1, 0.18f, 90f,0.299f, -0.142f, 0, 0, 0}
            },
            {//horse
                    {1, 0.25f, 135.f, -0.138f, 0.355f, 255, 0, 0},
                    {2, 0.18f, 0f, -0.056f, 0.219f, 0, 255, 0},
                    {1, 0.18f, 135f,-0.285f, 0.021f, 0, 0, 255},
                    {1, 0.32f, 90f, -0.084f, 0.0f, 0, 255, 255},
                    {1, 0.32f, 45f, 0.097f, -0.097f, 255, 100, 0},
                    {1, 0.18f, 0f, -0.031f, -0.341f, 255, 0, 255},
                    {4, 0.14f, 120f, 0.277f, -0.262f, 0, 0, 0}
            }
    };
    private int[][] levelParalelogramDegrees = {
        {0,180},//bird
        {270,90},//fish
        {120,300} //horse
    };
    private int[] levelCubeDegrees = {0,90,180,270,360};
    private int level = 0;

    private float[][][] levels_blueprint = new float[levels.length][levels[0].length][levels[0][0].length];
    private float[][][] levels_minimap = new float[levels.length][levels[0].length][levels[0][0].length];


    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        setupAspectRatio(gl);

        if(checkShape){
            gl.glPushMatrix();
            gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
            gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
            gl.glRotatef(angleZ, 0.0f, 0.0f, 1.0f);
            shapeGood = true;

            for (int i = 0; i < levels[level].length; i++) {
                //if (levels_blueprint[level][i][0] == 2) { //cube
                //Arrays.stream(cubeDegrees).anyMatch(i -> i == levels_blueprint[level][i][2]))//cube
                Arrays.fill(tempArr, 0);
                switch ((int) levels_blueprint[level][i][0]) {//fix val for shapes with multiple correct degrees
                    case 1://triangle
                        tempArr[0] = (int)levels[level][i][2];
                        break;
                    case 2://cube
                        tempArr = levelCubeDegrees.clone();
                        break;
                    case 3://paralelogram
                        tempArr = levelParalelogramDegrees[level].clone(); //we don't have any so ništa
                        break;
                    case 4://paralelogram2
                        tempArr = levelParalelogramDegrees[level].clone();
                        break;
                }
                if (
                        levels_blueprint[level][i][0] != levels[level][i][0] || //shape
                                levels_blueprint[level][i][1] != levels[level][i][1] || //size
                                !(contains(tempArr, (int)levels_blueprint[level][i][2]))  //rotation universal check
                ) {
                    shapeGood = false;
                    break;
                }
            }
            //shapeGood = true; // flip it to test the animation
            if(shapeGood){ // show animation
                for (int i = 0; i < levels[level].length; i++) {
                    gl.glRotatef(angleA, 1.0f, 1.0f, 1.0f);
                    Shapes.draw(gl, levels[level][i][0], levels[level][i][1], levels[level][i][2], levels[level][i][3], levels[level][i][4], 0, 255, 0, GL2.GL_POLYGON);
                    if(angleA < 360)
                        angleA += 0.2f;
                }
            }else { //show which shapes are wrong
                for (int i = 0; i < levels[level].length; i++) {
                    Arrays.fill(tempArr, 0);
                    switch ((int) levels_blueprint[level][i][0]) {//fix val for shapes with multiple correct degrees
                        case 1://triangle
                            tempArr[0] = (int)levels[level][i][2];
                            break;
                        case 2://cube
                            tempArr = levelCubeDegrees.clone();
                            break;
                        case 3://paralelogram
                            tempArr = levelParalelogramDegrees[level].clone(); //we don't have any so ništa
                            break;
                        case 4://paralelogram2
                            tempArr = levelParalelogramDegrees[level].clone();
                            break;
                    }
                    if (
                            levels_blueprint[level][i][0] == levels[level][i][0] && //shape
                                    levels_blueprint[level][i][1] == levels[level][i][1] && //size
                                    //levels_blueprint[level][i][2] == levels[level][i][2]  //rotation
                                    contains(tempArr, (int)levels_blueprint[level][i][2])
                    ) {//shape, size, rotation, tX, tY,  r,g,b
                        Shapes.draw(gl, levels[level][i][0], levels[level][i][1], levels[level][i][2], levels[level][i][3], levels[level][i][4], 0, 255, 0, GL2.GL_POLYGON);
                    } else {

                        Shapes.draw(gl, levels[level][i][0], levels[level][i][1], levels[level][i][2], levels[level][i][3], levels[level][i][4], 255, 0, 0, GL2.GL_POLYGON);
                    }

                }
            }
            gl.glPopMatrix();

        }else {
            //blueprint outline
            gl.glPushMatrix();
            gl.glLineWidth(2);
            for (int i = 0; i < levels[level].length; i++) {
                if (i==selection){
                    Shapes.draw(gl, levels[level][i][0], levels[level][i][1], levels[level][i][2], levels[level][i][3], levels[level][i][4], 255, 255, 255, GL2.GL_POLYGON);
                } else {
                    Shapes.draw(gl, levels[level][i][0], levels[level][i][1], levels[level][i][2], levels[level][i][3], levels[level][i][4], levels[level][i][5], levels[level][i][6], levels[level][i][7], GL2.GL_LINE_LOOP);
                }
            }
            gl.glPopMatrix();

            //blueprint (game)
            gl.glPushMatrix();
            for (int i = 0; i < levels_blueprint[level].length; i++) {
                Shapes.draw(gl, levels_blueprint[level][i][0], levels_blueprint[level][i][1], levels_blueprint[level][i][2], levels_blueprint[level][i][3], levels_blueprint[level][i][4], levels_blueprint[level][i][5], levels_blueprint[level][i][6], levels_blueprint[level][i][7], GL2.GL_POLYGON);
            }
            gl.glPopMatrix();

            //minimap
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            //gl.glLoadIdentity();  // Reset The View
            gl.glRotatef(180, 0.0f, 0.0f, 1.0f);
            gl.glScalef(-0.3f, -0.3f, -0.3f);
            gl.glTranslated(-2.3f, 2.3f, 0.0f);
            gl.glPushMatrix();

            for (int i = 0; i < levels[level].length; i++) {
                Shapes.draw(gl, levels_minimap[level][i][0], levels_minimap[level][i][1], levels_minimap[level][i][2], levels_minimap[level][i][3], levels_minimap[level][i][4], levels_minimap[level][i][5], levels_minimap[level][i][6], levels_minimap[level][i][7], GL2.GL_POLYGON);
            }
            gl.glPopMatrix();
        }

        // add the lights
        lights(gl);

        // check if the global ambient light should be turned on or off
        float [] zeros = {0, 0, 0, 1};
        float [] globalAmbient = {0.1f, 0.1f, 0.1f, 1};
        if(ambientLight.isSelected()) {
            gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globalAmbient, 0);
        }else {
            gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, zeros, 0);
        }

        drawBackground(drawable);
        gl.glFlush();
        if(shapeGood){
            gl.glPushMatrix();
            gameFinishedText.beginRendering(windowWidth,windowHeight);
            // optionally set the color
            gameFinishedText.setColor(1.0f, 255, 255, 255);
            gameFinishedText.draw("Level Passed!", (windowWidth/2)-90,windowHeight/2);
            // ... more draw commands, color changes, etc.
            gameFinishedText.endRendering();
            gl.glPopMatrix();
        }
        gl.glPushMatrix();
        signature.beginRendering(windowWidth,windowHeight);
        // optionally set the color
        signature.setColor(1.0f, 255, 255, 255);
        signature.draw("Daniel Petrovich - 2019230364 - Final Project", (windowWidth/2)-315,7);
        // ... more draw commands, color changes, etc.
        signature.endRendering();
        gl.glPopMatrix();
    }

    private void setupLevel(int level){
        this.level = level;
        selection = 0;

        Random r = new Random();
        rotateColors(r.nextInt((12 - 1) + 1) + 1);//spin the colors by a random number

        for (int i = 0; i < levels.length; i++) {
            for (int j = 0; j < levels[i].length; j++) {

                //clone
                levels_minimap[i][j] =  levels[i][j].clone();
                levels_blueprint[i][j] =  levels[i][j].clone();

                //set12
                levels_blueprint[i][j][0] = 0.0f;//shape
                levels_blueprint[i][j][1] = 0.1f;//size
                levels_blueprint[i][j][2] = 1 + r.nextFloat() * (359 - 1);//rotation random
            }
        }
    }

    private void rotateColors(int random){
        for(int move = 0; move<random; move++){
            for (int i = 0; i < levels.length; i++) {
                float lastR,lastG,lastB;
                lastR = levels[i][0][5];
                lastG = levels[i][0][6];
                lastB = levels[i][0][7];
                for (int j = 1; j < levels[i].length; j++) {
                    levels[i][j - 1][5] = levels[i][j][5];
                    levels[i][j - 1][6] = levels[i][j][6];
                    levels[i][j - 1][7] = levels[i][j][7];
                }
                levels[i][levels[i].length-1][5] = lastR;
                levels[i][levels[i].length-1][6] = lastG;
                levels[i][levels[i].length-1][7] = lastB;
            }
        }
    }

    private boolean contains(final int[] array, final int key) {
        for (final int i : array) {
            if (i == key) {
                return true;
            }
        }
        return false;
    }

    private void setupAspectRatio(GL2 gl) {
        gl.glViewport(0, 0, windowWidth, windowHeight);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        aspectR = (float) windowHeight / ((float) windowWidth );
        gl.glOrtho((float) -10 / 2, // left vertical clipping plane
                (float) 10 / 2, // right vertical clipping plane
                (-10 * aspectR) / 2, // bottom horizontal clipping plane
                (10 * aspectR) / 2, // top horizontal clipping plane
                1, // near depth clipping plane
                100); // farther clipping plane
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(0, 0f, 3f,
                0, 0,  0,
                0, 1,  0);
        gl.glScalef(3f,3f,3f);
    }

    private float round(float x){
        return (float) (Math.round(x * 100.0) / 100.0);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        //glu = GLU.createGLU(gl);

        gl.glClearColor(0.95f, 0.95f, 1f, 0);
        gl.glEnable(GL2.GL_DEPTH_TEST); // enable the depth buffer to represent depth information
        gl.glEnable(GL2.GL_LIGHTING); // enable light calculation
        gl.glEnable(GL2.GL_LIGHT0); // initialize the values for the light (1,1,1,1) -> RGBA
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL2.GL_COLOR_MATERIAL); // to track the current color material

        gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, 1); // turn one two-sided lightning
        gl.glMateriali(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 100); // turn on lighting with shininess

        // set the values of individual light source parameters
        float[] ambient = {0.1f, 0.1f, 0.1f, 1.0f};
        float[] diffuse = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] specular = {1.0f, 1.0f, 1.0f, 1.0f};

        // get the lights from different light sources
        // parameters:
        // first -> the light source: which light to be configured
        // second -> the properties to be set (ambient, specular, and diffused)
        // third -> the number of values for each of the properties (RGBA -> 0.0f - 1.0f)
        // fourth -> intensity
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, diffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_SPECULAR, specular, 0);
        // other initializations
        gl.glClearDepth(1.0f); // set clear depth value to farthest
        gl.glEnable(GL2.GL_DEPTH_TEST); // enable depth testing
        gl.glDepthFunc(GL2.GL_LEQUAL); // set the type of depth test to do
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST); // set the best perspective correction
        gl.glShadeModel(GL2.GL_SMOOTH); // blend colors nicely and smoothes out lighting
        // specify the polygon to be applied both for the front and back facing polygon
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        glu = GLU.createGLU(gl); // get GL utilities

        for (int i = 0; i < textureFileNames.length; i++) {
            try {
                URL textureURL = getClass().getClassLoader().getResource("Resources/"+textureFileNames[i]);
                if (textureURL != null) {
                    // load the file
                    BufferedImage img = ImageIO.read(textureURL);
                    ImageUtil.flipImageVertically(img);
                    textures[i] = AWTTextureIO.newTexture(GLProfile.getDefault(), img, true);
                    textures[i].setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
                    textures[i].setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        textures[0].bind(gl);
        setupLevel(level);

        gameFinishedText = new TextRenderer(new Font("SansSerif", Font.BOLD, 30));
        signature = new TextRenderer(new Font("SansSerif", Font.BOLD, 30));
        //textMatch = new TextRenderer(new Font("SansSerif", Font.BOLD, 20));
    }

    private void drawBackground(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        // set the window scene
        gl.glViewport(0, 0, windowWidth, windowHeight);
        aspect = (float) windowHeight / ((float) windowWidth);
        // define the orthogonal view
        gl.glOrtho((float) -10 / 2, (float) 10 / 2,
                (-10 * aspect) / 2,
                (10 * aspect) / 2, 0, 100);
        gl.glMatrixMode(GL2.GL_MODELVIEW); // converts local coordinates into world space
        gl.glLoadIdentity(); // reset the value
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        gl.glPushMatrix();
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
        gl.glGenerateMipmap(GL2.GL_TEXTURE_2D);
        textures[level+1].bind(gl);
        gl.glTranslated(0,0,-100);
        gl.glScalef(1.75f, 1f, 1f);
        gl.glColor3f(1f, 1f, 1f);
        double radius = 3.25;
        // add texture to the background
        gl.glBegin(GL2.GL_POLYGON);
        gl.glNormal3f(0,0,1);
        // top left
        gl.glTexCoord2d(0,1);
        gl.glVertex2d(-radius, radius);
        // bottom left
        gl.glTexCoord2d(0,0);
        gl.glVertex2d(-radius, -radius);
        // bottom right
        gl.glTexCoord2d(1,0);
        gl.glVertex2d(radius, -radius);
        // top right
        gl.glTexCoord2d(1,1);
        gl.glVertex2d(radius, radius);
        gl.glEnd();
        gl.glDisable(GL2.GL_TEXTURE_2D);
        gl.glEnd();
        gl.glPopMatrix();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        windowWidth = width;
        windowHeight = height;
        System.out.println("[!] Window resize: "+windowWidth+"x"+windowHeight);
    }

    private void lights(GL2 gl) {
        gl.glColor3d(0.5, 0.5, 0.5);
        // initialize the parameters for the color when the light is off
        float [] zeros = {0, 0, 0, 1};
        // specifying the material parameters for the lighting model
        // param1: specify the side we want to apply it
        // param2: the type of light
        // param3: the properties of that light
        // additional property to give the starting index of the data
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, zeros, 0);
        // set global ambient lighting level that is independent of the other OpenGL light sources
        if(lightOnOff.isSelected()) {
            gl.glDisable(GL2.GL_LIGHTING);
        }else {
            gl.glEnable(GL2.GL_LIGHTING);
        }
        // initialize an array to store the value for different lights
        float [] ambient = {0.1f, 0.1f, 0.1f, 1};
        float [] diffuse = {1.0f, 1.0f, 1.0f, 1.0f};
        float [] specular = {1.0f, 1.0f, 1.0f, 1.0f};
        // check if ambient light is selected or not
        if(ambientGlobalLighting.isSelected()) {
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, ambient, 0);
            gl.glEnable(GL2.GL_LIGHT0);
        }else {
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, zeros, 0);
            gl.glDisable(GL2.GL_LIGHT0);
        }
        // check if diffused light is selected or not
        if(diffuseLighting.isSelected()) {
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, diffuse, 0);
            gl.glEnable(GL2.GL_LIGHT1);
        }else {
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, zeros, 0);
            gl.glDisable(GL2.GL_LIGHT1);
        }
        // check if the specular light is selected or not
        if(specularLighting.isSelected()) {
            float [] shininess = {1.0f};
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, specular, 0);
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess, 0);
            gl.glEnable(GL2.GL_LIGHT2);
        }else {
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, zeros, 0);
            gl.glDisable(GL2.GL_LIGHT2);
        }
    }

    public Tangram() {
        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setAlphaBits(8);
        capabilities.setDepthBits(24);
        capabilities.setDoubleBuffered(true);
        capabilities.setStencilBits(8);

        SwingUtilities.invokeLater(() -> {
            canvas = new GLCanvas();
            canvas.setPreferredSize(new Dimension(windowWidth, windowHeight));
            canvas.addGLEventListener(this);
            canvas.addMouseListener(this);
            canvas.addKeyListener(this);
            canvas.setFocusable(true); // gets the focus of the component
            canvas.requestFocus(); // allows the user to enter data via keyboard
            canvas.requestFocusInWindow(); // ensures the window gains the focus once launched

            animator = new FPSAnimator(canvas, FPS, true);

            frame = new JFrame();
            // Initialize the button and set it's preferred dimensions
            Level_1B = new JButton("Level 1");
            Level_2B = new JButton("Level 2");
            Level_3B = new JButton("Level 3");
            checkB = new JButton("Check");
            helpB = new JButton("Help");

            lightOnOff = new JCheckBox("Turn Light ON/OFF", true);
            ambientGlobalLighting = new JCheckBox("Ambient Light", false);
            specularLighting = new JCheckBox("Specular Light", false);
            diffuseLighting = new JCheckBox("Diffused Light", false);
            ambientLight = new JCheckBox("Global Ambient Light", false);

            Level_1B.setPreferredSize(new Dimension(100, 20));
            Level_2B.setPreferredSize(new Dimension(100, 20));
            Level_3B.setPreferredSize(new Dimension(100, 20));
            checkB.setPreferredSize(new Dimension(100, 20));
            helpB.setPreferredSize(new Dimension(100, 20));

            // set the components to false, so once we click on it
            // an action can be performed
            ambientLight.setFocusable(false);
            lightOnOff.setFocusable(false);
            ambientGlobalLighting.setFocusable(false);
            diffuseLighting.setFocusable(false);
            specularLighting.setFocusable(false);

            // initialize a layout for the buttons (2,2) grid
            JPanel bottom = new JPanel();
            bottom.setLayout(new GridLayout(2, 1));

            JPanel row1 = new JPanel();
            row1.add(helpB);
            row1.add(Level_1B);
            row1.add(Level_2B);
            row1.add(Level_3B);
            row1.add(checkB);
            bottom.add(row1);

            JPanel row2 = new JPanel();
            row2.add(ambientLight);
            row2.add(lightOnOff);
            row2.add(ambientGlobalLighting);
            row2.add(diffuseLighting);
            row2.add(specularLighting);
            bottom.add(row2);

            Level_1B.addActionListener(e -> {
                if (e.getSource() == Level_1B) {
                    angleA = 0f;
                    angleX = 0f;
                    angleY = 0f;
                    angleZ = 0f;
                    shapeGood = false;
                    setupLevel(0);
                }
                Level_1B.setFocusable(false);
            });
            Level_2B.addActionListener(e -> {
                if (e.getSource() == Level_2B) {
                    angleA = 0f;
                    angleX = 0f;
                    angleY = 0f;
                    angleZ = 0f;
                    shapeGood = false;
                    setupLevel(1);
                }
                Level_2B.setFocusable(false);
            });
            Level_3B.addActionListener(e -> {
                if (e.getSource() == Level_3B) {
                    angleA = 0f;
                    angleX = 0f;
                    angleY = 0f;
                    angleZ = 0f;
                    shapeGood = false;
                    setupLevel(2);
                }
                Level_3B.setFocusable(false);
            });
            checkB.addActionListener(e -> {
                if (e.getSource() == checkB) {
                    if(checkShape) {
                        angleA = 0f;
                        angleX = 0f;
                        angleY = 0f;
                        angleZ = 0f;
                        checkShape = false;
                        checkB.setText("Check");
                    }else{
                        checkShape = true;
                        checkB.setText("Continue");
                    }
                }
                checkB.setFocusable(false);
            });
            helpB.addActionListener(e -> {
                if (e.getSource() == helpB) {

                    JOptionPane.showMessageDialog(frame, helpMessage, "Help", JOptionPane.INFORMATION_MESSAGE);
                }
                helpB.setFocusable(false);
            });
            frame.getContentPane().add(canvas);

            try {
                BufferedImage wPic = ImageIO.read(getClass().getClassLoader().getResource("Resources/" + textureFileNames[0]));
                JLabel picLabel = new JLabel(new ImageIcon(wPic));
                frame.add(picLabel, BorderLayout.EAST);
            } catch (Exception e) {
                System.out.println("[-] ERR");
            }
            frame.add(bottom, BorderLayout.SOUTH);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    new Thread(() -> {
                        if (animator.isStarted()) animator.stop();
                        System.exit(0);
                    }).start();
                }
            });
            frame.setSize(frame.getContentPane().getPreferredSize());
            frame.setTitle("Tangram (Daniel Petrovich - danielthe@cyberdude.com)");
            frame.pack();
            frame.setVisible(true);
            animator.start();
        });
    }//end of classimport javax.media.opengl.GL2

    @Override
    public void mousePressed(MouseEvent e) {
        switch (e.getButton()) {
            // left button click on mouse
            case MouseEvent.BUTTON1:
                System.out.println("[!] Default: "+Arrays.toString(levels[level][selection]));
                break;
            case MouseEvent.BUTTON3:
                System.out.println("[!] Current: "+Arrays.toString(levels_blueprint[level][selection]));
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_NUMPAD0:
            case KeyEvent.VK_0:
                levels_blueprint[level][selection][0] = 0;
                break;
            case KeyEvent.VK_NUMPAD1:
            case KeyEvent.VK_1:
                levels_blueprint[level][selection][0] = 1;
                break;
            case KeyEvent.VK_NUMPAD2:
            case KeyEvent.VK_2:
                levels_blueprint[level][selection][0] = 2;
                break;
            case KeyEvent.VK_NUMPAD3:
            case KeyEvent.VK_3:
                levels_blueprint[level][selection][0] = 3;
                break;
            case KeyEvent.VK_NUMPAD4:
            case KeyEvent.VK_4:
                levels_blueprint[level][selection][0] = 4;
                break;

            case KeyEvent.VK_E:
                if (selection < (levels[level].length-1))
                    selection += 1;
                else
                    selection = 0;
                System.out.println("[!] Next shape:     " + selection + "/" + (levels[level].length-1));
                break;
            case KeyEvent.VK_Q:
                if (selection > 0)
                    selection -= 1;
                else
                    selection = (levels[level].length-1);
                System.out.println("[!] Previous shape: " +  selection + "/" + (levels[level].length-1));
                break;

            case KeyEvent.VK_W:
                //levels[level][selection][1] += 0.01f;
                //levels[level][selection][1] = round(levels[level][selection][1]);
                levels_blueprint[level][selection][1] += 0.01f;
                levels_blueprint[level][selection][1] = round(levels_blueprint[level][selection][1]);
                System.out.println("[!] Scale: Up "+levels_blueprint[level][selection][1]);
                break;
            case KeyEvent.VK_S:
                //levels[level][selection][1] -= 0.01f;
                //levels[level][selection][1] = round(levels[level][selection][1]);
                levels_blueprint[level][selection][1] -= 0.01f;
                levels_blueprint[level][selection][1] = round(levels_blueprint[level][selection][1]);
                System.out.println("[!] Scale: Down "+levels_blueprint[level][selection][1]);
                break;
            case KeyEvent.VK_A:

                if (levels_blueprint[level][selection][2] < 359){
                    levels_blueprint[level][selection][2] += 1;
                    levels_blueprint[level][selection][2] = (int)round(levels_blueprint[level][selection][2]);
                }else{
                    levels_blueprint[level][selection][2] = 0;
                    levels_blueprint[level][selection][2] = (int)round(levels_blueprint[level][selection][2]);
                }
//                if (levels[level][selection][2] < 360){
//                    levels[level][selection][2] += 1;
//                    levels[level][selection][2] = round(levels[level][selection][2]);
//                    System.out.println("[!] Rotation: Left " + levels[level][selection][2] );
//                }else{
//                    levels[level][selection][2] = 1;
//                    levels[level][selection][2] = round(levels[level][selection][2]);
//                    System.out.println("[!] Rotation: Left " + levels[level][selection][2] );
//                }
                System.out.println("[!] Rotation: Left " + levels_blueprint[level][selection][2] );
                break;
            case KeyEvent.VK_D:
                if (levels_blueprint[level][selection][2] > 0){
                    levels_blueprint[level][selection][2] -= 1;
                    levels_blueprint[level][selection][2] = (int)round(levels_blueprint[level][selection][2]);
                }else{
                    levels_blueprint[level][selection][2] = 359;
                    levels_blueprint[level][selection][2] = (int)round(levels_blueprint[level][selection][2]);
                }
//                if (levels[level][selection][2] > 0){
//                    levels[level][selection][2] -= 1;
//                    levels[level][selection][2] = round(levels[level][selection][2]);
//                    System.out.println("[!] Rotation: Right " + levels[level][selection][2] );
//                }else{
//                    levels[level][selection][2] = 359;
//                    levels[level][selection][2] = round(levels[level][selection][2]);
//                    System.out.println("[!] Rotation: Right " + levels[level][selection][2] );
//                }
                System.out.println("[!] Rotation: Right " + levels_blueprint[level][selection][2] );
                break;

            case KeyEvent.VK_UP:
                //levels[level][selection][4] += 0.001f;
                //levels[level][selection][4] = round(levels[level][selection][4]);
                angleX += 2f;
                break;
            case KeyEvent.VK_DOWN:
                //levels[level][selection][4] -= 0.001f;
                //levels[level][selection][4] = round(levels[level][selection][4]);
                angleX -= 2f;
                break;
            case KeyEvent.VK_LEFT:
                //levels[level][selection][3] -= 0.001f;
                //levels[level][selection][3] = round(levels[level][selection][3]);
                angleY -= 2f;
                break;
            case KeyEvent.VK_RIGHT:
                //levels[level][selection][3] += 0.001f;
                //levels[level][selection][3] = round(levels[level][selection][3]);
                angleY += 2f;
                break;

            case KeyEvent.VK_ESCAPE:
                animator.stop();
                System.exit(0);
                break;
        }
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {}
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {new Tangram();}
}