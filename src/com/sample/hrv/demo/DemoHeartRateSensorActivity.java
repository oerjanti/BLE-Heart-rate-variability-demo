package com.sample.hrv.demo;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;

import android.content.Context;
import android.os.SystemClock;


import com.sample.hrv.R;
import com.sample.hrv.sensor.BleHeartRateSensor;
import com.sample.hrv.sensor.BleSensor;

/**
 * Created by olli on 3/28/14.
 */
public class DemoHeartRateSensorActivity extends DemoSensorActivity {
	private final static String TAG = DemoHeartRateSensorActivity.class
			.getSimpleName();

	private TextView viewText;
	private PolygonRenderer renderer;

	private GLSurfaceView view;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo_opengl);
		view = (GLSurfaceView) findViewById(R.id.gl);

		getActionBar().setTitle(R.string.title_demo_heartrate);

		viewText = (TextView) findViewById(R.id.text);

		renderer = new PolygonRenderer(this);
		view.setRenderer(renderer);
		//view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		// Render when hear rate data is updated
		view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	@Override
	public void onDataRecieved(BleSensor<?> sensor, String text) {
		if (sensor instanceof BleHeartRateSensor) {
			final BleHeartRateSensor heartSensor = (BleHeartRateSensor) sensor;
			float[] values = heartSensor.getData();
			renderer.setInterval(values);
			view.requestRender();

			viewText.setText(text);
		}
	}
	
	public abstract class AbstractRenderer implements GLSurfaceView.Renderer {
		
		public int[] getConfigSpec() {
			int[] configSpec = { EGL10.EGL_DEPTH_SIZE, 0, EGL10.EGL_NONE };
			return configSpec;
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
			gl.glDisable(GL10.GL_DITHER);
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
			gl.glClearColor(.5f, .5f, .5f, 1);
			gl.glShadeModel(GL10.GL_SMOOTH);
			gl.glEnable(GL10.GL_DEPTH_TEST);
		}

		public void onSurfaceChanged(GL10 gl, int w, int h) {
			gl.glViewport(0, 0, w, h);
			float ratio = (float) w / h;
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);
		}

		public void onDrawFrame(GL10 gl) {
			gl.glDisable(GL10.GL_DITHER);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
			GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			draw(gl);
		}

		protected abstract void draw(GL10 gl);
	}

	public class PolygonRenderer extends AbstractRenderer {
		private final String TAG = PolygonRenderer.class
				.getSimpleName();

		// Number of points or vertices we want to use
		private final static int VERTS = 4;

		// A raw native buffer to hold the point coordinates
		private FloatBuffer mFVertexBuffer;

		// A raw native buffer to hold indices
		// allowing a reuse of points.
		private ShortBuffer mIndexBuffer;

		private int numOfIndecies = 0;

		private long prevtime = SystemClock.uptimeMillis();

		private int sides = 32;

		private float[] interval = { 0, 0, 0 };
		private float previousInterval = 0;

		public void setInterval(float[] interval) {
			if (this.interval[1] >= 0 && interval[1] > 0) {
				this.previousInterval = this.interval[1];
			}
			this.interval[0] = interval[0]; // heart rate
			this.interval[1] = interval[1]; // beat to beat interval
			this.interval[2] = 0;			// empty
		}
		
		public PolygonRenderer(Context context) {
			prepareBuffers(sides, interval[1]);
		}

		private void prepareBuffers(int sides, float radius) {
			Log.d(TAG,"radius: "+radius +" previous: "+previousInterval);
			// Is it a valid value?
			if (radius < 0) {
				radius = previousInterval;
			}
			
			// Double check if the previous value was valid
			if (radius < 0) {
				radius = 700;
			}
			Log.d(TAG,"final radius: "+radius);
			
			radius = ( ( radius / 1000 ) - 0.7f ) * 2;
			
			RegularPolygon t = new RegularPolygon(0, 0, 0, radius, sides);
			this.mFVertexBuffer = t.getVertexBuffer();
			this.mIndexBuffer = t.getIndexBuffer();
			this.numOfIndecies = t.getNumberOfIndecies();
			this.mFVertexBuffer.position(0);
			this.mIndexBuffer.position(0);
		}

		// overriden method
		protected void draw(GL10 gl) {
			long curtime = SystemClock.uptimeMillis();

			this.prepareBuffers(sides, interval[1]);
			gl.glColor4f(96/255.0f, 246/255.0f, 255/255.0f, 1.0f);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer);
			gl.glDrawElements(GL10.GL_TRIANGLES, this.numOfIndecies,
					GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
		}
	}


	private static class RegularPolygon {
		private static final String TAG = RegularPolygon.class
				.getSimpleName();
		private float cx, cy, cz, r;
		private int sides;
		private float[] xarray = null;
		private float[] yarray = null;

		public RegularPolygon(float incx, float incy, float incz, // (x,y,z)
																	// center
				float inr, // radius
				int insides) // number of sides
		{
			cx = incx;
			cy = incy;
			cz = incz;
			r = inr;
			sides = insides;

			xarray = new float[sides];
			yarray = new float[sides];
			calcArrays();
		}

		private void calcArrays() {
			float[] xmarray = this.getXMultiplierArray();
			float[] ymarray = this.getYMultiplierArray();

			// calc xarray
			for (int i = 0; i < sides; i++) {
				float curm = xmarray[i];
				float xcoord = cx + r * curm;
				xarray[i] = xcoord;
			}
			//this.printArray(xarray, "xarray");

			// calc yarray
			for (int i = 0; i < sides; i++) {
				float curm = ymarray[i];
				float ycoord = cy + r * curm;
				yarray[i] = ycoord;
			}
			//this.printArray(yarray, "yarray");

		}

		public FloatBuffer getVertexBuffer() {
			int vertices = sides + 1;
			int coordinates = 3;
			int floatsize = 4;
			int spacePerVertex = coordinates * floatsize;

			ByteBuffer vbb = ByteBuffer.allocateDirect(spacePerVertex
					* vertices);
			vbb.order(ByteOrder.nativeOrder());
			FloatBuffer mFVertexBuffer = vbb.asFloatBuffer();

			// Put the first coordinate (x,y,z:0,0,0)
			mFVertexBuffer.put(0.0f); // x
			mFVertexBuffer.put(0.0f); // y
			mFVertexBuffer.put(0.0f); // z

			int totalPuts = 3;
			for (int i = 0; i < sides; i++) {
				mFVertexBuffer.put(xarray[i]); // x
				mFVertexBuffer.put(yarray[i]); // y
				mFVertexBuffer.put(0.0f); // z
				totalPuts += 3;
			}
			//Log.d(TAG, "total puts: " + Integer.toString(totalPuts));
			return mFVertexBuffer;
		}

		public ShortBuffer getIndexBuffer() {
			short[] iarray = new short[sides * 3];
			ByteBuffer ibb = ByteBuffer.allocateDirect(sides * 3 * 2);
			ibb.order(ByteOrder.nativeOrder());
			ShortBuffer mIndexBuffer = ibb.asShortBuffer();
			for (int i = 0; i < sides; i++) {
				short index1 = 0;
				short index2 = (short) (i + 1);
				short index3 = (short) (i + 2);
				if (index3 == sides + 1) {
					index3 = 1;
				}
				mIndexBuffer.put(index1);
				mIndexBuffer.put(index2);
				mIndexBuffer.put(index3);

				iarray[i * 3 + 0] = index1;
				iarray[i * 3 + 1] = index2;
				iarray[i * 3 + 2] = index3;
			}
			//this.printShortArray(iarray, "index array");
			return mIndexBuffer;
		}

		private float[] getXMultiplierArray() {
			float[] angleArray = getAngleArrays();
			float[] xmultiplierArray = new float[sides];
			for (int i = 0; i < angleArray.length; i++) {
				float curAngle = angleArray[i];
				float sinvalue = (float) Math.cos(Math.toRadians(curAngle));
				float absSinValue = Math.abs(sinvalue);
				if (isXPositiveQuadrant(curAngle)) {
					sinvalue = absSinValue;
				} else {
					sinvalue = -absSinValue;
				}
				xmultiplierArray[i] = this.getApproxValue(sinvalue);
			}
			//this.printArray(xmultiplierArray, "xmultiplierArray");
			return xmultiplierArray;
		}

		private float[] getYMultiplierArray() {
			float[] angleArray = getAngleArrays();
			float[] ymultiplierArray = new float[sides];
			for (int i = 0; i < angleArray.length; i++) {
				float curAngle = angleArray[i];
				float sinvalue = (float) Math.sin(Math.toRadians(curAngle));
				float absSinValue = Math.abs(sinvalue);
				if (isYPositiveQuadrant(curAngle)) {
					sinvalue = absSinValue;
				} else {
					sinvalue = -absSinValue;
				}
				ymultiplierArray[i] = this.getApproxValue(sinvalue);
			}
			//this.printArray(ymultiplierArray, "ymultiplierArray");
			return ymultiplierArray;
		}

		private boolean isXPositiveQuadrant(float angle) {
			if ((0 <= angle) && (angle <= 90)) {
				return true;
			}

			if ((angle < 0) && (angle >= -90)) {
				return true;
			}
			return false;
		}

		private boolean isYPositiveQuadrant(float angle) {
			if ((0 <= angle) && (angle <= 90)) {
				return true;
			}

			if ((angle < 180) && (angle >= 90)) {
				return true;
			}
			return false;
		}

		private float[] getAngleArrays() {
			float[] angleArray = new float[sides];
			float commonAngle = 360.0f / sides;
			float halfAngle = commonAngle / 2.0f;
			float firstAngle = 360.0f - (90 + halfAngle);
			angleArray[0] = firstAngle;

			float curAngle = firstAngle;
			for (int i = 1; i < sides; i++) {
				float newAngle = curAngle - commonAngle;
				angleArray[i] = newAngle;
				curAngle = newAngle;
			}
			//printArray(angleArray, "angleArray");
			return angleArray;
		}

		private float getApproxValue(float f) {
			if (Math.abs(f) < 0.001) {
				return 0;
			}
			return f;
		}

		public int getNumberOfIndecies() {
			return sides * 3;
		}

		private void printArray(float array[], String tag) {
			StringBuilder sb = new StringBuilder(tag);
			for (int i = 0; i < array.length; i++) {
				sb.append(";").append(array[i]);
			}
			Log.d(TAG, sb.toString());
		}

		private void printShortArray(short array[], String tag) {
			StringBuilder sb = new StringBuilder(tag);
			for (int i = 0; i < array.length; i++) {
				sb.append(";").append(array[i]);
			}
			Log.d(TAG, sb.toString());
		}
	}

}
