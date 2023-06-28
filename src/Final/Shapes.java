package Final;

import com.jogamp.opengl.GL2;

public class Shapes {
    public static void draw(GL2 gl, float shape, float size, float rotation, float tX, float tY, float r, float g, float b, float fill){
        float radius = size / 2;
        //gl.glLoadIdentity();  // Reset The View
        gl.glPushMatrix();
        gl.glTranslated(tX, tY, 0.0f);
        gl.glRotatef( rotation, 0.0f, 0.0f, 1.0f );
        //gl.glScalef(scale, scale, scale);

        gl.glColor3f((int)r,(int)g,(int)b);

        double d = (Math.sqrt(2)*radius)/6;//6

        switch ((int)shape){
            case 0:
                break;
            case 1://triangle
                gl.glBegin((int)fill); // TRIANGLE
                //gl.glVertex2d(radius,0.0f);
                //gl.glVertex2d((radius*0.5f)-radius, -radius);
                //gl.glVertex2d((radius*0.5f)-radius,  radius);
                gl.glVertex2d(-radius-d,-radius+d);
                gl.glVertex2d(radius-d,-radius+d);
                gl.glVertex2d(radius-d,radius+d);
                break;
            case 2://cube
                gl.glBegin((int)fill); //POLYGON
                gl.glVertex2d(-radius,radius);
                gl.glVertex2d(-radius,-radius);
                gl.glVertex2d(radius,-radius);
                gl.glVertex2d(radius,radius);
                break;
            case 3:// paralelogram 1
                float m[] = {
                        1.0f,0.0f,0.0f,0.0f,
                        1.0f,1.0f,0.0f,0.0f,
                        0.0f,0.0f,1.0f,0.0f,
                        0.0f,0.0f,0.0f,1.0f
                };

                gl.glPushMatrix();
                gl.glMultMatrixf(m,0);
                gl.glBegin((int)fill);

                gl.glVertex2d(-radius,radius);
                gl.glVertex2d(-radius,-radius);
                gl.glVertex2d(radius,-radius);
                gl.glVertex2d(radius,radius);
                break;
            case 4:// paralelogram 2
                float m2[] = {
                        1.0f,0.0f,0.0f,0.0f,
                        -1.0f,1.0f,0.0f,0.0f,
                        0.0f,0.0f,1.0f,0.0f,
                        0.0f,0.0f,0.0f,1.0f
                };

                gl.glPushMatrix();
                gl.glMultMatrixf(m2,0);
                gl.glBegin((int)fill);

                gl.glVertex2d(-radius,radius);
                gl.glVertex2d(-radius,-radius);
                gl.glVertex2d(radius,-radius);
                gl.glVertex2d(radius,radius);
                break;

        }

        gl.glEnd();
        gl.glPopMatrix();
        gl.glFlush();
    }

}


//        gl.glVertex2d(0.0f, 0.2f);       // Top
//        gl.glVertex2d(0.3f, -0.2f);    // Bottom Left
//        gl.glVertex2d(-0.3f, -0.2f);      // Bottom Right
//        gl.glVertex2d(-radius,-radius);
//        gl.glVertex2d(radius,-radius);
//        gl.glVertex2d(radius,radius);
