package edu.columbia.quidditch.render.link;

import java.nio.FloatBuffer;
import java.util.LinkedList;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Link class
 * 
 * @author Yuqing Guan
 * 
 */
public class Link
{
	private Matrix4f transform;
	private FloatBuffer transBuffer;

	private Vector3f axis;

	private LinkedList<Link> children;
	private Link parent;

	private float theta, radius;

	public Link(float px, float py, float pz, float qx, float qy, float qz,
			float qw)
	{
		transform = new Matrix4f();
		Matrix4f.setIdentity(transform);

		transBuffer = BufferUtils.createFloatBuffer(16);

		children = new LinkedList<Link>();

		// Get translation part

		transform.m30 = px;
		transform.m31 = py;
		transform.m32 = pz;

		// Get rotation axis and angle

		float radTheta = (float) (Math.acos(qw) * 2);
		float sinHalfTheta = (float) Math.sin(radTheta / 2);

		theta = (float) Math.toDegrees(radTheta);
		theta = normalizeAngle(theta);

		axis = new Vector3f();

		axis.x = qx / sinHalfTheta;
		axis.y = qy / sinHalfTheta;
		axis.z = qz / sinHalfTheta;

		setTheta(theta);
	}

	public Matrix4f getTrans()
	{
		return transform;
	}

	/**
	 * Update transform matrix
	 */
	private void updateTrans()
	{
		// Compute new rotation component

		float radTheta = (float) Math.toRadians(theta);
		float radHalfTheta = radTheta / 2;

		float sinHalfTheta = (float) Math.sin(radHalfTheta);
		float cosHalfTheta = (float) Math.cos(radHalfTheta);

		float qx, qy, qz, qw;

		qx = sinHalfTheta * axis.x;
		qy = sinHalfTheta * axis.y;
		qz = sinHalfTheta * axis.z;
		qw = cosHalfTheta;

		transform.m00 = 1 - 2 * qy * qy - 2 * qz * qz;
		transform.m10 = 2 * qx * qy - 2 * qz * qw;
		transform.m20 = 2 * qx * qz + 2 * qy * qw;

		transform.m01 = 2 * qx * qy + 2 * qz * qw;
		transform.m11 = 1 - 2 * qx * qx - 2 * qz * qz;
		transform.m21 = 2 * qy * qz - 2 * qx * qw;

		transform.m02 = 2 * qx * qz - 2 * qy * qw;
		transform.m12 = 2 * qy * qz + 2 * qx * qw;
		transform.m22 = 1 - 2 * qx * qx - 2 * qy * qy;

		transform.store(transBuffer);
		transBuffer.rewind();
	}

	public void addChild(Link child)
	{
		children.add(child);
		child.parent = this;
	}

	/**
	 * Get current link's transform in the global coordinate
	 * 
	 * @return Do not consider the planner's rotation and translation
	 */
	public Matrix4f getGlobalTrans()
	{
		if (parent == null)
		{
			return transform;
		}

		Matrix4f parentTransform = parent.getGlobalTrans();
		Matrix4f globalTransform = new Matrix4f();

		Matrix4f.mul(parentTransform, transform, globalTransform);

		return globalTransform;
	}

	/**
	 * Get current link's location in the global coordinate
	 * 
	 * @return Do not consider the planner's rotation and translation
	 */
	public Vector3f getGlobalLoc()
	{
		Matrix4f globalTransform = getGlobalTrans();

		Vector3f globalLocation = new Vector3f();

		globalLocation.x = globalTransform.m30;
		globalLocation.y = globalTransform.m31;
		globalLocation.z = globalTransform.m32;

		return globalLocation;
	}

	/**
	 * Get current link's location in the world coordinate
	 * 
	 * @param pos
	 * @param rotMatrix
	 * @return Do consider the planner's rotation and translation
	 */
	public Vector3f getWorldLoc(Vector3f pos, Matrix4f rotMatrix)
	{
		Matrix4f globalTransform = getGlobalTrans();

		Matrix4f rotatedTransform = new Matrix4f();
		Matrix4f.mul(rotMatrix, globalTransform, rotatedTransform);

		Vector3f worldLocation = new Vector3f();

		worldLocation.x = rotatedTransform.m30 + pos.x;
		worldLocation.y = rotatedTransform.m31 + pos.y;
		worldLocation.z = rotatedTransform.m32 + pos.z;

		return worldLocation;
	}

	public void setTheta(float theta)
	{
		theta = normalizeAngle(theta);
		this.theta = theta;
		updateTrans();
	}

	/**
	 * Normalize an angle so that it will be between -180 and 180
	 * 
	 * @param angle
	 * @return
	 */
	private float normalizeAngle(float angle)
	{
		angle %= 360.0f;

		if (angle < -180.0f)
		{
			angle += 360.0f;
		}
		else if (angle > 180.0f)
		{
			angle -= 360.0f;
		}

		return angle;
	}

	public float getTheta()
	{
		return theta;
	}

	public float getRadius()
	{
		return radius;
	}
}
