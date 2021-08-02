package gtec.java.unicornandroidapi;

import android.content.Context;
import android.opengl.GLSurfaceView;

import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import gtec.java.unicornandroidapi.MainActivity;

public class Gl_Handler extends GLSurfaceView {
    GLSurfaceView mainsurface;

    public Gl_Handler(Context context, AttributeSet attrs) {
        super(context, attrs);
        mainsurface = new GLSurfaceView(context);
        mainsurface.setRenderer(new Renderer_frag());
    }
    public void onResume(){
        mainsurface.onResume();
    }
    public void onPause(){
        mainsurface.onPause();
    }


}
