package gtec.java.unicornandroidapi;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.content.Context;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import gtec.java.unicorn.Unicorn;
import neuro.tools.unicorn.GenericFunctions;
import static java.lang.Math.addExact;
import static java.lang.Math.floor;
import edu.mines.jtk.dsp.BandPassFilter;
import edu.mines.jtk.ogl.*;
import edu.mines.jtk.util.*;
import static edu.mines.jtk.ogl.Gl.*;
import static edu.mines.jtk.util.ArrayMath.*;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private String _btnConStr = "Connect";
    private String _btnDisconStr = "Disconnect";
    private Button _btnConnect = null;
    private Spinner _spnDevices = null;
    private TextView _tvState = null;
    private Unicorn _unicorn = null;
    private GraphView _graphidx = null;
    private Thread _receiver;
    private Thread _receiveAnal;
    private Thread _receiveView;
    private boolean _receiverRunning = false;
    private boolean goAnal = false;
    private boolean goView = false;
    private Context _context = null;
    private  int _cnt = 0;
    private static float perioD = 3.0f;
    public static int S_cnT = (int) floor(perioD * (float)Unicorn.SamplingRateInHz);
    private int C_cnT = Unicorn.NumberOfAcquiredChannels;
    private float[][] dataS = new float[S_cnT][Unicorn.NumberOfAcquiredChannels]; // Source Data
    public float[][] dataR = new float[Unicorn.NumberOfAcquiredChannels][S_cnT]; // RAW Data
    public static float[][] dataV = new float[Unicorn.NumberOfAcquiredChannels][S_cnT]; // View Data
    public static float[][] dataShader = new float[Unicorn.NumberOfAcquiredChannels][S_cnT]; // View Data for single channel
    private int cnT  = 0;
    private int cnW  = 0;
    public static float totest  = (float) 0.0;
    DataPoint[] dataPoints = new DataPoint[S_cnT];
    GenericFunctions genFunc = new GenericFunctions();
    BandPassFilter bandpass = new BandPassFilter(0.0,0.45,0.1,0.01);

    // Audio
    private static final long UPDATE_LATENCY_EVERY_MILLIS = 1000;
    private static final Integer[] CHANNEL_COUNT_OPTIONS = {1, 2, 3, 4, 5, 6, 7, 8};
    // Default to Stereo (OPTIONS is zero-based array so index 1 = 2 channels)
    private static final int CHANNEL_COUNT_DEFAULT_OPTION_INDEX = 1;
    private static final int[] BUFFER_SIZE_OPTIONS = {0, 1, 2, 4, 8};
    private static final String[] AUDIO_API_OPTIONS = {"Unspecified", "OpenSL ES", "AAudio"};
    // Default all other spinners to the first option on the list
    private static final int SPINNER_DEFAULT_OPTION_INDEX = 0;
    private Spinner mAudioApiSpinner;
    private AudioDeviceSpinner mPlaybackDeviceSpinner;
    private Spinner mChannelCountSpinner;
    private Spinner mBufferSizeSpinner;
    private TextView mLatencyText;
    private Timer mLatencyUpdater;

    //public GLSurfaceView glSurfaceView;

//    void GL_go() {
//        setContentView(R.layout.activity_main);
//        //glSurfaceView.setEGLContextClientVersion(2);
//        glSurfaceView = (GLSurfaceView) findViewById(R.id.gl_layout);
//        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
//        float mmInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 30, getResources().getDisplayMetrics());
//        ViewGroup.LayoutParams layoutParams= new ViewGroup.LayoutParams((int) 300,(int) 300);
//
//        if (configurationInfo.reqGlEsVersion >= 0x20000) {
//            // Request an OpenGL ES 2.0 compatible context.
//            glSurfaceView.setEGLContextClientVersion(2);
//            glSurfaceView.setRenderer(new Renderer_frag());
//            //setContentView(glSurfaceView, layoutParams);
//        } else {
//            // This is where you could create an OpenGL ES 1.x compatible
//            // renderer if you wanted to support both ES 1 and ES 2.
//        }
//    }

   private Runnable makeSound = new Runnable() {
       @Override
       public void run() {
           Log.w("MainActivity", "Will make sound!");
           PlaybackEngine.setToneOn(true);
           Log.w("MainActivity", "Made sound!");
           try {
               Thread.sleep(300);
           }
           catch(InterruptedException e){
               Log.w("MainActivity", "Caught exception while sleeping");
           }
           PlaybackEngine.setToneOn(false);
       }
   };

    private void setupChannelCountSpinner() {
        mChannelCountSpinner = findViewById(R.id.channelCountSpinner);

        ArrayAdapter<Integer> channelCountAdapter = new ArrayAdapter<Integer>(this, R.layout.channel_counts_spinner, CHANNEL_COUNT_OPTIONS);
        mChannelCountSpinner.setAdapter(channelCountAdapter);
        mChannelCountSpinner.setSelection(CHANNEL_COUNT_DEFAULT_OPTION_INDEX);

        mChannelCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PlaybackEngine.setChannelCount(CHANNEL_COUNT_OPTIONS[mChannelCountSpinner.getSelectedItemPosition()]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private List<HashMap<String, String>> createBufferSizeOptionsList() {

        ArrayList<HashMap<String, String>> bufferSizeOptions = new ArrayList<>();

        for (int i : BUFFER_SIZE_OPTIONS) {
            HashMap<String, String> option = new HashMap<>();
            String strValue = String.valueOf(i);
            String description = (i == 0) ? getString(R.string.automatic) : strValue;
            option.put(getString(R.string.buffer_size_description_key), description);
            option.put(getString(R.string.buffer_size_value_key), strValue);

            bufferSizeOptions.add(option);
        }

        return bufferSizeOptions;
    }

    private void setupBufferSizeSpinner() {
        mBufferSizeSpinner = findViewById(R.id.bufferSizeSpinner);
        mBufferSizeSpinner.setAdapter(new SimpleAdapter(
                this,
                createBufferSizeOptionsList(), // list of buffer size options
                R.layout.buffer_sizes_spinner, // the xml layout
                new String[]{getString(R.string.buffer_size_description_key)}, // field to display
                new int[]{R.id.bufferSizeOption} // View to show field in
        ));

        mBufferSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PlaybackEngine.setBufferSizeInBursts(getBufferSizeInBursts());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private int getBufferSizeInBursts() {
        @SuppressWarnings("unchecked")
        HashMap<String, String> selectedOption = (HashMap<String, String>)
                mBufferSizeSpinner.getSelectedItem();

        String valueKey = getString(R.string.buffer_size_value_key);

        // parseInt will throw a NumberFormatException if the string doesn't contain a valid integer
        // representation. We don't need to worry about this because the values are derived from
        // the BUFFER_SIZE_OPTIONS int array.
        return Integer.parseInt(selectedOption.get(valueKey));
    }

    private void setupPlaybackDeviceSpinner() {
        mPlaybackDeviceSpinner = findViewById(R.id.playbackDevicesSpinner);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPlaybackDeviceSpinner.setDirectionType(AudioManager.GET_DEVICES_OUTPUTS);
            mPlaybackDeviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    PlaybackEngine.setAudioDeviceId(getPlaybackDeviceId());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }

    private int getPlaybackDeviceId() {
        return ((AudioDeviceListEntry) mPlaybackDeviceSpinner.getSelectedItem()).getId();
    }

    private void setupAudioApiSpinner() {
        mAudioApiSpinner = findViewById(R.id.audioApiSpinner);
        mAudioApiSpinner.setAdapter(new SimpleAdapter(
                this,
                createAudioApisOptionsList(),
                R.layout.audio_apis_spinner, // the xml layout
                new String[]{getString(R.string.audio_api_description_key)}, // field to display
                new int[]{R.id.audioApiOption} // View to show field in
        ));

        mAudioApiSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PlaybackEngine.setAudioApi(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private List<HashMap<String, String>> createAudioApisOptionsList() {

        ArrayList<HashMap<String, String>> audioApiOptions = new ArrayList<>();

        for (int i = 0; i < AUDIO_API_OPTIONS.length; i++) {
            HashMap<String, String> option = new HashMap<>();
            option.put(getString(R.string.buffer_size_description_key), AUDIO_API_OPTIONS[i]);
            option.put(getString(R.string.buffer_size_value_key), String.valueOf(i));
            audioApiOptions.add(option);
        }
        return audioApiOptions;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bandpass.setExtrapolation(BandPassFilter.Extrapolation.ZERO_SLOPE);

        setContentView(R.layout.activity_main);

        //Sound
        //mLatencyText = findViewById(R.id.latencyText);
        setupAudioApiSpinner();
        setupPlaybackDeviceSpinner();
        setupChannelCountSpinner();
        setupBufferSizeSpinner();
        //Should go to onResume!
        Log.w("MainActivity", "Will create Playback Engine!");
        PlaybackEngine.create(this);
        Log.w("MainActivity", "Created Playback Engine!");
        int result = PlaybackEngine.start();
        if (result != 0) {
            Log.w("MainActivity", "Could not start playback engine!");
        }

        //GL_go();

        try{
            _context = this.getApplicationContext();
            _spnDevices = findViewById(R.id.spnDevices);
            _btnConnect = findViewById(R.id.btnConnect);
            _tvState = findViewById(R.id.textView);
            _graphidx = findViewById(R.id.graph);

            _btnConnect.setText(_btnConStr);
            _btnConnect.setOnClickListener(this);

            //get available devices
            List<String> devices = Unicorn.GetAvailableDevices();

            //update ui
            ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,devices);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            _spnDevices.setAdapter(adapter);
        }
        catch(Exception ex)
        {
            Toast t = Toast.makeText(_context,String.format("Could not detect available devices. %s", ex.getMessage()) ,Toast.LENGTH_SHORT);
        }
    }

    private void StartReceiver() {
        _receiverRunning = true;
        _receiver = new Thread(_doReceive);
        _receiver.setPriority(Thread.MIN_PRIORITY);
        _receiver.setDaemon(false);
        _receiver.start();
    }
    private void StartReceiveAnal() {
        _receiveAnal = new Thread(_doReceiveAnal);
        _receiveAnal.setPriority(Thread.MIN_PRIORITY);
        _receiveAnal.setDaemon(false);
        _receiveAnal.start();
    }
    private void StartReceiveView() {
        _receiveView = new Thread(_doReceiveView);
        _receiveView.setPriority(Thread.MIN_PRIORITY);
        _receiveView.setDaemon(false);
        _receiveView.start();
    }
    private void StopReceiver() throws Exception {
        _receiverRunning = false;
        _receiver.join(500);
        _receiveAnal.join(500);
        _receiveView.join(500);
    }

    // Regular plot viewer (old)
    private Runnable _doReceive = new Runnable() {
        @Override
        public void run() {
            genFunc.SetZeros(dataS);
            genFunc.SetZeros(dataR);
            StartReceiveAnal();
            // StartReceiveView();

            while(_receiverRunning) {
                try {
                    goAnal = false;
                    float[] data = _unicorn.GetData(); // 8X[EEG] 3X[ACC] 3X[Gyroscope] 3x[...] 1X[timestep]
                    _cnt++;
                    dataS[cnT % S_cnT] = data;
                    cnT++;

                    dataR = genFunc.TransPose(dataS, cnT, S_cnT, C_cnT);

                    dataPoints = genFunc.ToPoints(dataR,0, S_cnT);
                    goAnal = true;
                    if(0 == 0) {
                        Handler mainHandler = new Handler( _context.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run(){
                                _graphidx.removeAllSeries();
                                LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);
                                _graphidx.addSeries(series);
                            }
                        };
                        mainHandler.post(myRunnable);
                    }
                } catch (Exception ex) {
                    Handler mainHandler = new Handler( _context.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            String message = _tvState.getText().toString();
                            message += String.format("Acquisition failed. %s\n", ex.getMessage());
                            _tvState.setText(message);
                            Disconnect();
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }
        }
    };
    // GLSL plot
    private Runnable _doReceiveAnal = new Runnable() {
        @Override
        public void run() {
            while(_receiverRunning) {
                if(goAnal){
                    try {
                        goView = false;
                        dataV = dataR;
                        Log.d("channel length", String.valueOf(dataV[0].length));

                        bandpass.apply(dataR,dataShader);
                        //float[] tes = genFunc.NormArr(dataShader[0]);
                        //Log.d("filtered signal", String.valueOf(tes[0]));
                        //Log.d("filtered signal", String.valueOf(dataR[0][0]));
                        cnW = cnT;
                        goView = true;
                        // Anal (dataR, cnT);
                        if(0 == 0) {
                            Handler mainHandler = new Handler( _context.getMainLooper());
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run(){
                                    String message = _tvState.getText().toString();
                                    message = Integer.toString(cnT);

                                    //_tvState.setText(message);
                                }
                            };
                            mainHandler.post(myRunnable);
                        }
                    } catch (Exception ex) {
                        Handler mainHandler = new Handler( _context.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                String message = _tvState.getText().toString();
                                message += String.format("Anal failed. %s\n", ex.getMessage());
                                _tvState.setText(message);
                                Disconnect();
                            }
                        };
                        mainHandler.post(myRunnable);
                    }
                }
            }
        }
    };
    private Runnable _doReceiveView = new Runnable() {
        @Override
        public void run() {
            while(_receiverRunning) {
                if(goView){
                    try {
                        if(goAnal){
                            // dataView.View(dataPoints, _graphidx);
                            // goAnal = false;
                        }

                    } catch (Exception ex) {
                        Handler mainHandler = new Handler( _context.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                String message = _tvState.getText().toString();
                                message += String.format("View failed. %s\n", ex.getMessage());
                                _tvState.setText(message);
                                Disconnect();
                            }
                        };
                        mainHandler.post(myRunnable);
                    }
                }
            }
        }
    };
    private void Connect() {
        _btnConnect.setEnabled(false);
        _spnDevices.setEnabled(false);
        String device = (String)_spnDevices.getSelectedItem();
        String message = "";
        try
        {
            //update ui message
            message += String.format("Connecting to %s...\n", device);
            _tvState.setText(message);

            //connect to device
            _unicorn = new Unicorn(device);
            _btnConnect.setText(_btnDisconStr);

            //update ui message
            message += "Connected.\n";
            message += "Starting data acquisition...\n";
            _tvState.setText(message);

            //start acquisition
            _unicorn.StartAcquisition();

            message += "Acquisition running.\n";
            _tvState.setText(message);
            //start receiving thread
            StartReceiver();
        }
        catch (Exception ex)
        {
            //close device
            _unicorn = null;
            System.gc();
            System.runFinalization();

            _btnConnect.setText(_btnConStr);
            _spnDevices.setEnabled(true);

            //update ui message
            message += String.format("Could not start acquisition. %s", ex.getMessage());
            _tvState.setText(message);
        }
        _btnConnect.setEnabled(true);
    }

    private void Disconnect()
    {
        _btnConnect.setEnabled(false);
        String device = (String)_spnDevices.getSelectedItem();
        String message = _tvState.getText().toString();
        try
        {
            //update ui message
            message += "\nStopping data acquisition...\n";
            _tvState.setText(message);

            //stop acquisition
            _unicorn.StopAcquisition();

            //stop receiving thread
            StopReceiver();

            //update ui message
            message += String.format("Disconnecting from %s...\n", device);
            _tvState.setText(message);

            //close device
            _unicorn = null;
            System.gc();
            System.runFinalization();

            message += "Disconnected";
            _tvState.setText(message);

            _btnConnect.setText(_btnConStr);
        }
        catch (Exception ex)
        {
            //close device
            _unicorn = null;
            System.gc();
            System.runFinalization();

            _btnConnect.setText(_btnConStr);

            message += String.format("Could not stop acquisition. %s", ex.getMessage());
            _tvState.setText(message);
        }
        _spnDevices.setEnabled(true);
        _btnConnect.setEnabled(true);
    }

    @Override
    public void onClick(View v)
    {
        Thread _beepThread = new Thread(makeSound);
        _beepThread.start();
        switch (v.getId())
        {
            case R.id.btnConnect:
            {
                if(_btnConnect.getText().equals(_btnConStr))
                {
                    Connect();
                    totest = (float) 1.0;

                    //GL_go();
                }
                else
                {
                    Disconnect();
                    //totest = (float) 0.0;

                }
                break;
            }
        }
    }

}