package gtec.java.unicornandroidapi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.stream.IntStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;


import androidx.annotation.RequiresApi;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import gtec.java.unicornandroidapi.MainActivity;
import neuro.tools.unicorn.GenericFunctions;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused for OpenGL ES 2.0
 * renderers -- the static class GLES20 is used instead.
 */
public class Renderer_frag implements GLSurfaceView.Renderer
{

    private static final int NO_TEXTURE = 0;
    private AssetManager assetManager;
    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
    private float[] mProjectionMatrix = new float[16];

    /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
    private float[] mMVPMatrix = new float[16];

    /** Store our model data in a float buffer. */
    private final FloatBuffer mTriangle1Vertices;
//    private final FloatBuffer mTriangle2Vertices;
//    private final FloatBuffer mTriangle3Vertices;

    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;
    private int valuePositionHandle;

    private int mEEGDataHandle1;
    private int mEEGDataHandle2;
    private int mEEGDataHandle3;
    private int mEEGDataHandle4;
    private int mEEGDataHandle5;
    private int mEEGDataHandle6;
    private int mEEGDataHandle7;
    private int mEEGDataHandle8;
    private int mEEGDataHandlerALL;
    private final int mBytesPerFloat = 4;
    private final int mStrideBytes = 7 * mBytesPerFloat;
    private final int mPositionOffset = 0;
    private final int mPositionDataSize = 3;
    private final int mColorOffset = 3;
    private final int mColorDataSize = 4;
    public MainActivity unicornData = new MainActivity();
    public GenericFunctions genFunc = new GenericFunctions();
    public float val;
    public float[][] eeg_data;
    public float[] val_data1;
    private int vsTextureCoord;
    private int fsTexture;



    /**
     * Initialize the model data.
     */
    public Renderer_frag()
    {
        // Define points for equilateral triangles.

        // This triangle is red, green, and blue.
        final float[] triangle1VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -1.0f, 1.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                -1.0f, -1.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                1.0f, -1.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f,

                1.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f};

        // This triangle is yellow, cyan, and magenta.
//        final float[] triangle2VerticesData = {
//                // X, Y, Z,
//                // R, G, B, A
//                -0.5f, -0.25f, 0.0f,
//                1.0f, 1.0f, 0.0f, 1.0f,
//
//                0.5f, -0.25f, 0.0f,
//                0.0f, 1.0f, 1.0f, 1.0f,
//
//                0.0f, 0.559016994f, 0.0f,
//                1.0f, 0.0f, 1.0f, 1.0f};

        // This triangle is white, gray, and black.
//        final float[] triangle3VerticesData = {
//                // X, Y, Z,
//                // R, G, B, A
//                -0.5f, -0.25f, 0.0f,
//                1.0f, 1.0f, 1.0f, 1.0f,
//
//                0.5f, -0.25f, 0.0f,
//                0.5f, 0.5f, 0.5f, 1.0f,
//
//                0.0f, 0.559016994f, 0.0f,
//                0.0f, 0.0f, 0.0f, 1.0f};

        // Initialize the buffers.
        mTriangle1Vertices = ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
//        mTriangle2Vertices = ByteBuffer.allocateDirect(triangle2VerticesData.length * mBytesPerFloat)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer();
//        mTriangle3Vertices = ByteBuffer.allocateDirect(triangle3VerticesData.length * mBytesPerFloat)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        mTriangle1Vertices.put(triangle1VerticesData).position(0);
//        mTriangle2Vertices.put(triangle2VerticesData).position(0);
//        mTriangle3Vertices.put(triangle3VerticesData).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        // Set the background clear color to gray.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        //GLES20.glBindTexture(GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, toneCurveTexture[0]));
        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.01f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        String vertexS = " ";
        String fragmentS = " ";
        try {
            vertexS = Util.readFile(Util.getResourceAsStream("assets/vert.glsl"));
            fragmentS = Util.readFile(Util.getResourceAsStream("assets/frag.glsl"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final String vertexShader = vertexS;
        final String fragmentShader = fragmentS;

        // Load in the vertex shader.
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0)
        {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0)
        {
            throw new RuntimeException("Error creating vertex shader.");
        }

        // Load in the fragment shader shader.
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0)
        {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if (fragmentShaderHandle == 0)
        {
            throw new RuntimeException("Error creating fragment shader.");
        }

        // Create a program object and store the handle to it.
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0)
        {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0)
            {
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0)
        {
            throw new RuntimeException("Error creating program.");
        }

        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
        mEEGDataHandle1 = GLES20.glGetUniformLocation(programHandle, "u_EEG1");
        mEEGDataHandle2 = GLES20.glGetUniformLocation(programHandle, "u_EEG2");
        mEEGDataHandle3 = GLES20.glGetUniformLocation(programHandle, "u_EEG3");
        mEEGDataHandle4 = GLES20.glGetUniformLocation(programHandle, "u_EEG4");
        mEEGDataHandle5 = GLES20.glGetUniformLocation(programHandle, "u_EEG5");
        mEEGDataHandle6 = GLES20.glGetUniformLocation(programHandle, "u_EEG6");
        mEEGDataHandle7 = GLES20.glGetUniformLocation(programHandle, "u_EEG7");
        mEEGDataHandle8 = GLES20.glGetUniformLocation(programHandle, "u_EEG8");
        mEEGDataHandlerALL = GLES20.glGetUniformLocation(programHandle, "u_Map");
        //vsTextureCoord = GLES20.glGetAttribLocation(programHandle, "TexCoordIn");
        //get handle to shape's texture reference
        fsTexture = GLES20.glGetUniformLocation(programHandle, "uTexture");


        valuePositionHandle = GLES20.glGetUniformLocation(programHandle, "myValue");

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);

    }



    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height)
    {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDrawFrame(GL10 glUnused)
    {

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        int[] tex_output = new int[1];
        GLES20.glGenTextures(1, tex_output,0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex_output[0]);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUniform1i(fsTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex_output[0]);
        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        //val = unicornData.dataV[5][1]-unicornData.dataV[5][0];
        eeg_data = unicornData.dataV;
        //Log.d("myTag", String.valueOf(val_data[0]));
        //Log.d("myTime", String.valueOf(time));
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        // Draw the triangle facing straight on.
        Matrix.setIdentityM(mModelMatrix, 0);
        //Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        drawTriangle(mTriangle1Vertices);

        // Draw one translated a bit down and rotated to be flat on the ground.
//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.translateM(mModelMatrix, 0, 0.0f, -1.0f, 0.0f);
//        Matrix.rotateM(mModelMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);
//        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
//        drawTriangle(mTriangle2Vertices);

        // Draw one translated a bit to the right and rotated to be facing to the left.
//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.translateM(mModelMatrix, 0, 1.0f, 0.0f, 0.0f);
//        Matrix.rotateM(mModelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f);
//        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
//        drawTriangle(mTriangle3Vertices);
    }

    /**
     * Draws a triangle from the given vertex data.
     *
     * @param aTriangleBuffer The buffer containing the vertex data.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void drawTriangle(final FloatBuffer aTriangleBuffer)
    {

        // Pass in the position information
        aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);
        //GLES20.glTexImage2D();
        //GLES20.glUniform1fv(mDataHandle, unicornData.dataV[0].length, FloatBuffer.wrap(unicornData.dataV[0]));
        GLES20.glUniform1fv(mEEGDataHandle1, eeg_data[0].length, FloatBuffer.wrap(genFunc.NormArr(eeg_data[0])));
        GLES20.glUniform1fv(mEEGDataHandle2, eeg_data[0].length, FloatBuffer.wrap(genFunc.NormArr(eeg_data[1])));
        GLES20.glUniform1fv(mEEGDataHandle3, eeg_data[0].length, FloatBuffer.wrap(genFunc.NormArr(eeg_data[2])));
        GLES20.glUniform1fv(mEEGDataHandle4, eeg_data[0].length, FloatBuffer.wrap(genFunc.NormArr(eeg_data[3])));
        GLES20.glUniform1fv(mEEGDataHandle5, eeg_data[0].length, FloatBuffer.wrap(genFunc.NormArr(eeg_data[4])));
        GLES20.glUniform1fv(mEEGDataHandle6, eeg_data[0].length, FloatBuffer.wrap(genFunc.NormArr(eeg_data[5])));
        GLES20.glUniform1fv(mEEGDataHandle7, eeg_data[0].length, FloatBuffer.wrap(genFunc.NormArr(eeg_data[6])));
        GLES20.glUniform1fv(mEEGDataHandle8, eeg_data[0].length, FloatBuffer.wrap(genFunc.NormArr(eeg_data[7])));

//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        FloatBuffer texBuffer = ByteBuffer.allocateDirect(eeg_data.length * Float.SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
//        texBuffer.put(eeg_data[0]);


        //Buffer data = ByteBuffer.wrap(ByteBuffer.allocateDirect(eeg_data.length).array());

        //GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 750 /*width*/, 17 /*height*/, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data);
        //float[][] arrays = new float[] { genFunc.NormArr(eeg_data[0]), genFunc.NormArr(eeg_data[1]), genFunc.NormArr(eeg_data[2]), genFunc.NormArr(eeg_data[3]) };
        //GLES20.glUniform4fv(mEEGDataHandle8, eeg_data[0].length, arrays);


//      GLES20.glUniform1fv(mDataHandle,0, FloatBuffer.wrap(unicornData.dataR[0]));
        GLES20.glUniform1f(valuePositionHandle, val);


        GLES20.glEnableVertexAttribArray(mColorHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
    }
}