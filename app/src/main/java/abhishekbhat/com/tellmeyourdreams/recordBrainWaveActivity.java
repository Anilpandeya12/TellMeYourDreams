package abhishekbhat.com.tellmeyourdreams;

import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//// Bluetooth imports for Neurosky
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;
import com.neurosky.thinkgear.TGRawMulti;

// Graph jar:
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class recordBrainWaveActivity extends AppCompatActivity {

    // UIs
    Button btnConnect_EEG;
    Button btnStartRecording;
    Button btnStopRecording;

    TextView textInfo;
    TextView textBlink;
    TextView textPoorSignal;
    Button buttonConnect;
    TextView textViewRaw;

    TextView tvBTStatus;

    //Bluetooth Interface fields
    BluetoothAdapter btAdapter = null;

    // Data for the Neuosky EEG:
    BluetoothAdapter bluetoothAdapter;
//    TextView textBlink;
    TGDevice tgDevice;
    final boolean rawEnabled = true;

    // Write FIle related:
    public String fileName = "";// "File_1.txt"; // Make sure the extension is present. Setting the name in the onCLick listener
    String data_delimiter = ",";
    public boolean booleanEnableRecording = false;

    // Graph stuff:
    public LineGraphSeries<DataPoint> series;
    public int lastX = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_brain_wave);

        btnStartRecording = (Button) findViewById(R.id.buttonStartRecording);
        btnStopRecording = (Button) findViewById(R.id.buttonStopRecording);

        textBlink = (TextView)findViewById(R.id.tvBlink);
        textBlink.setText("No Blink");

        textInfo = (TextView) findViewById(R.id.tvInfo);
        textInfo.setText("Android version: " + Integer.valueOf(android.os.Build.VERSION.SDK) + "\n");

        textPoorSignal = (TextView) findViewById(R.id.tvPoorSig);
//        textHeartRate  = (TextView) findViewById(R.id.tvHeartRate);

        textViewRaw = (TextView) findViewById(R.id.tvRaw);
        buttonConnect = (Button) findViewById(R.id.bConnect);

        // set the instance of graph:
        GraphView graph = (GraphView) findViewById(R.id.graph);

        // data
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);

        // customize:
        Viewport viewport = graph.getViewport();
        graph.setTitle("BrainWave");

        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(100);

        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(100);
        viewport.setScrollable(true);

        // Set listeners:
        btnStartRecording.setOnClickListener(listener_buttonStartRecording);
        btnStopRecording.setOnClickListener(listener_buttonStopRecording);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doStuff(buttonConnect);
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            // Alert user that Bluetooth is not available
            Toast.makeText(recordBrainWaveActivity.this, "Bluetooth not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }else {
        	/* create the TGDevice */
            tgDevice = new TGDevice(bluetoothAdapter, handler);
            Toast.makeText(recordBrainWaveActivity.this, "Starting Handler", Toast.LENGTH_LONG).show();
        }

    }

    public void addEntry(int raw_eeg_value){
        // chose to display max 10 points and scroll to the end.
        series.appendData(new DataPoint(lastX++,raw_eeg_value),true,100);

    }


    @Override
    protected void onDestroy() {

        try {
            tgDevice.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
//        mHandler.removeCallbacks(mStatusChecker);
    }


    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TGDevice.MSG_STATE_CHANGE:

                    switch (msg.arg1) {
                        case TGDevice.STATE_IDLE:
                            break;
                        case TGDevice.STATE_CONNECTING:
                            textInfo.setText("Connecting...\n");
                            break;
                        case TGDevice.STATE_CONNECTED:
                            textInfo.setText("Connected.\n");
                            tgDevice.start();
                            break;
                        case TGDevice.STATE_NOT_FOUND:
                            textInfo.setText("Can't find\n");
                            break;
                        case TGDevice.STATE_NOT_PAIRED:
                            textInfo.setText("not paired\n");
                            break;
                        case TGDevice.STATE_DISCONNECTED:
                            textInfo.setText("Disconnected \n");
                    }

                    break;
                case TGDevice.MSG_POOR_SIGNAL:
                    //signal = msg.arg1;
                    textPoorSignal.setText("PoorSignal: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_RAW_DATA:
                    //raw1 = msg.arg1;
                    //tv.append("Got raw: " + msg.arg1 + "\n");
//                    int rawValue = msg.arg1;
//                    textViewRaw.setText(String.valueOf(rawValue));

                    break;
                case TGDevice.MSG_EEG_POWER:
                    TGEegPower eegPower = (TGEegPower) msg.obj;
                    //                    Log.v("HelloEEG", "Delta: " + ep.delta);
                    //WILL NEED TO CHANGE TO PUTPUT TO SCREEN.
                    Log.d("LSD", "highAlpha: " + eegPower.highAlpha);
                    Log.d("LSD", "lowAlpha: " + eegPower.lowAlpha);
                    Log.d("LSD", "highBeta: " + eegPower.highBeta);
                    Log.d("LSD", "lowBeta: " + eegPower.lowBeta);
                    Log.d("LSD", "lowGamma: " + eegPower.lowGamma);
                    Log.d("LSD", "midGamma: " + eegPower.midGamma);
                    Log.d("LSD", "delta: " + eegPower.delta);
                    Log.d("LSD", "theta: " + eegPower.theta);
                    double ch1 = eegPower.highAlpha;
                    double ch2 = eegPower.lowAlpha;
                    double ch3 = eegPower.highBeta;
                    double ch4 = eegPower.lowBeta;
                    double ch5 = eegPower.lowGamma;
                    double ch6 = eegPower.midGamma;
                    double ch7 = eegPower.delta;
                    double ch8 = eegPower.theta;

                                    // Write the EEG data to file:
                    String strTemp = Calendar.getInstance().getTime() + data_delimiter
                        + String.valueOf(ch1) + data_delimiter
                        +String.valueOf(ch2)+ data_delimiter
                        +String.valueOf(ch3)+ data_delimiter
                        +String.valueOf(ch4)+ data_delimiter
                        +String.valueOf(ch5)+ data_delimiter
                        +String.valueOf(ch6)+ data_delimiter
                        +String.valueOf(ch7)+ data_delimiter
                        +String.valueOf(ch8)+ data_delimiter;

                    if(booleanEnableRecording){
                        writeFile(fileName,strTemp,1);
                    }

                    break;


                case TGDevice.MSG_HEART_RATE:
//                    textHeartRate.setText("Heart rate: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_ATTENTION:
                    addEntry(msg.arg1);
                    //att = msg.arg1;
//                    tv.append("Attention: " + msg.arg1 + "\n");
                    //Log.v("HelloA", "Attention: " + att + "\n");
                    break;
                case TGDevice.MSG_MEDITATION:

                    break;
                case TGDevice.MSG_BLINK:
                    textBlink.setText("Blink: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_RAW_COUNT:
                    //tv.append("Raw Count: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_LOW_BATTERY:
                    Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
                    break;
                case TGDevice.MSG_RAW_MULTI:
                    //TGRawMulti rawM = (TGRawMulti)msg.obj;
                    //tv.append("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
//                    TGRawMulti rawM = (TGRawMulti)msg.obj;

                    break;

                default:
                    break;
            }
        }
    };

    public void doStuff(View view) {
        if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
            tgDevice.connect(rawEnabled);
        Toast.makeText(recordBrainWaveActivity.this,"TG Connect",Toast.LENGTH_SHORT).show();
        //tgDevice.ena
    }

    View.OnClickListener listener_buttonConnect_EEG = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /* create the TGDevice */
            tgDevice = new TGDevice(bluetoothAdapter, handler);
            Toast.makeText(recordBrainWaveActivity.this, "Starting Handler", Toast.LENGTH_LONG).show();
            doStuff(v);
        }
    };

    View.OnClickListener listener_buttonStartRecording = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            booleanEnableRecording = true;
            fileName = "File_"+ Calendar.getInstance().getTime()+ ".txt";
            btnStartRecording.setBackgroundColor(Color.GREEN);

        }
    };
    View.OnClickListener listener_buttonStopRecording = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            booleanEnableRecording = false;
            fileName = "";
            btnStartRecording.setBackgroundColor(Color.GRAY);
        }
    };

    public void writeFile(String fileName, String inData, int mode)
    {
        File file;
        FileOutputStream outputStream;
        try {
//            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),fileName);
            file = new File(Environment.getExternalStorageDirectory()+"/TestData",fileName);

            outputStream = new FileOutputStream(file, true); // allow appending to the file.
            outputStream.write(System.getProperty("line.separator").getBytes()); // Start on new line;
            outputStream.write(inData.getBytes()); // Print the current data.
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
