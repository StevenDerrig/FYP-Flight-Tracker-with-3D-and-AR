package com.example.skytracker;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.google.ar.core.Frame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Renders the AR background from the camera feed.
 */
public class BackgroundRenderer {
    private static final String TAG = "BackgroundRenderer";

    private static final String VERTEX_SHADER =
            "attribute vec4 a_Position;\n" +
            "attribute vec2 a_TexCoord;\n" +
            "varying vec2 v_TexCoord;\n" +
            "void main() {\n" +
            "   gl_Position = a_Position;\n" +
            "   v_TexCoord = a_TexCoord;\n" +
            "}\n";

    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 v_TexCoord;\n" +
            "uniform samplerExternalOES s_Texture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(s_Texture, v_TexCoord);\n" +
            "}\n";

    private FloatBuffer quadVertices;
    private FloatBuffer quadTexCoords;
    private FloatBuffer quadInputTexCoords;

    private int program = 0;
    private int positionAttribute;
    private int texCoordAttribute;
    private int textureUniform;

    public void createOnGlThread() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e(TAG, "Could not link program: " + GLES20.glGetProgramInfoLog(program));
            program = 0;
            return;
        }

        positionAttribute = GLES20.glGetAttribLocation(program, "a_Position");
        texCoordAttribute = GLES20.glGetAttribLocation(program, "a_TexCoord");
        textureUniform = GLES20.glGetUniformLocation(program, "s_Texture");

        // Quad covering the whole screen.
        float[] vertices = {
                -1.0f, -1.0f, 0.0f,
                -1.0f,  1.0f, 0.0f,
                 1.0f, -1.0f, 0.0f,
                 1.0f,  1.0f, 0.0f,
        };
        quadVertices = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        quadVertices.put(vertices).position(0);

        // Input texture coordinates.
        float[] inputTexCoords = {
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
        };
        quadInputTexCoords = ByteBuffer.allocateDirect(inputTexCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        quadInputTexCoords.put(inputTexCoords).position(0);

        quadTexCoords = ByteBuffer.allocateDirect(inputTexCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        quadTexCoords.put(inputTexCoords).position(0);
    }

    public void draw(Frame frame, int textureId) {
        if (program == 0) {
            return;
        }

        if (frame.hasDisplayGeometryChanged()) {
            quadInputTexCoords.position(0);
            quadTexCoords.position(0);
            frame.transformDisplayUvCoords(quadInputTexCoords, quadTexCoords);
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(false);
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        GLES20.glUseProgram(program);

        quadVertices.position(0);
        GLES20.glVertexAttribPointer(positionAttribute, 3, GLES20.GL_FLOAT, false, 0, quadVertices);
        GLES20.glEnableVertexAttribArray(positionAttribute);
        
        quadTexCoords.position(0);
        GLES20.glVertexAttribPointer(texCoordAttribute, 2, GLES20.GL_FLOAT, false, 0, quadTexCoords);
        GLES20.glEnableVertexAttribArray(texCoordAttribute);

        GLES20.glUniform1i(textureUniform, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(positionAttribute);
        GLES20.glDisableVertexAttribArray(texCoordAttribute);

        GLES20.glDepthMask(true);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    private static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String error = GLES20.glGetShaderInfoLog(shader);
            Log.e(TAG, "Could not compile shader " + type + ": " + error);
            GLES20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }
}
