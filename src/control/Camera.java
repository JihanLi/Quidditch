/** 
 * Shading Tests
 * 
 * Author: Jihan Li
 * 
 * This is the class of camera. It controls the motion of the camera.
 * You can move the camera by dragging the mouse.
 * 
 * Reference: 
 * Class start codes
 *
 */

package control;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.util.vector.Vector3f;



public class Camera {
    public float moveSpeed = 0.5f;

    private float maxLook = 85;

    private float mouseSensitivity = 0.05f;
    private boolean flag = true;

    private Vector3f pos;
    private Vector3f rotation;

    public void create() {
        pos = new Vector3f(0, 0, 0);
        rotation = new Vector3f(0, 0, 0);
    }

    public void apply() {

        if (rotation.y / 360 > 1) {
            rotation.y -= 360;
        } else if (rotation.y / 360 < -1) {
            rotation.y += 360;
        }
        
        glRotatef(rotation.x, 1, 0, 0);
        glRotatef(rotation.y, 0, 1, 0);
        glRotatef(rotation.z, 0, 0, 1);
        glTranslatef(-pos.x, -pos.y, -pos.z);
    }

    public void acceptInput(float delta) {
    	if(Display.isActive())
    	{
    		if(flag)
    		{
		        acceptInputRotate(delta);
		        acceptInputMove(delta);
    		}
    		else
    			flag = true;
    	}
    }
    
    public void acceptInputMove(float delta) 
    { 
    	
    }

    public void acceptInputRotate(float delta) {
        if (Mouse.isInsideWindow() && Mouse.isButtonDown(0)) {
            float mouseDX = Mouse.getDX();
            float mouseDY = Mouse.getDY();
            //System.out.println("DX/Y: " + mouseDX + "  " + mouseDY);
            rotation.y += mouseDX * mouseSensitivity * delta;
            rotation.x += mouseDY * mouseSensitivity * delta;
            //rotation.x = Math.max(-maxLook, Math.min(maxLook, rotation.x));
        }
    }

    public void setSpeed(float speed) {
        moveSpeed = speed;
    }

    public void setPos(Vector3f pos) {
    	this.pos = pos;
    }

    public Vector3f getPos() {
        return pos;
    }

    public void setX(float x) {
        pos.x = x;
    }

    public float getX() {
        return pos.x;
    }

    public void addToX(float x) {
        pos.x += x;
    }

    public void setY(float y) {
        pos.y = y;
    }

    public float getY() {
        return pos.y;
    }

    public void addToY(float y) {
        pos.y += y;
    }

    public void setZ(float z) {
        pos.z = z;
    }

    public float getZ() {
        return pos.z;
    }

    public void addToZ(float z) {
        pos.z += z;
    }

    public void setRotation(Vector3f rotation) {
    	this.rotation = rotation;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotationX(float x) {
        rotation.x = x;
    }

    public float getRotationX() {
        return rotation.x;
    }

    public void addToRotationX(float x) {
        rotation.x += x;
    }

    public void setRotationY(float y) {
        rotation.y = y;
    }

    public float getRotationY() {
        return rotation.y;
    }

    public void addToRotationY(float y) {
        rotation.y += y;
    }

    public void setRotationZ(float z) {
        rotation.z = z;
    }

    public float getRotationZ() {
        return rotation.z;
    }

    public void addToRotationZ(float z) {
        rotation.z += z;
    }

    public void setMaxLook(float maxLook) {
    	this.maxLook = maxLook;
    }

    public float getMaxLook() {
        return maxLook;
    }

    public void setMouseSensitivity(float mouseSensitivity) {
        this.mouseSensitivity = mouseSensitivity;
    }

    public float getMouseSensitivity() {
        return mouseSensitivity;
    }
    
    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public boolean getFlag() {
        return flag;
    }
}