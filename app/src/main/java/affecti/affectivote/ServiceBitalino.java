package affecti.affectivote;


import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import info.plux.pluxapi.Communication;
import info.plux.pluxapi.Constants;
import info.plux.pluxapi.bitalino.BITalinoCommunication;
import info.plux.pluxapi.bitalino.BITalinoCommunicationFactory;
import info.plux.pluxapi.bitalino.BITalinoDescription;
import info.plux.pluxapi.bitalino.BITalinoException;
import info.plux.pluxapi.bitalino.BITalinoFrame;
import info.plux.pluxapi.bitalino.BITalinoState;
import info.plux.pluxapi.bitalino.bth.OnBITalinoDataAvailable;

public class ServiceBitalino extends Service {
    private final String TAG = this.getClass().getSimpleName();

    private BITalinoCommunication bitalino;
    final int[] digitalChannels = new int[2];

    IBinder mBinder = new LocalBinder();
    int mStartMode;

    String BITalinoVersion = "null";
    String BITalinoName = "null";
    int BITalinoFrame  = 0;

    private Constants.States currentState = Constants.States.DISCONNECTED;


    public class LocalBinder extends Binder {
        public ServiceBitalino getServerInstance() {
            return ServiceBitalino.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("START COMMAND SERVICE");

        bitalino = new BITalinoCommunicationFactory().getCommunication(Communication.BTH, getBaseContext(), new OnBITalinoDataAvailable() {
            @Override
            public void onBITalinoDataAvailable(BITalinoFrame bitalinoFrame) {
                // Log.d(TAG, "BITalinoFrame: " + bitalinoFrame.toString());
                BITalinoFrame = bitalinoFrame.getAnalog(0);
            }
        });

        registerReceiver(updateReceiver, updateIntentFilter());
        return mStartMode;
    }


    public void trigger(int trigger1, int trigger2) {
        digitalChannels[0] = trigger1;
        digitalChannels[1] = trigger2;
        try {
            bitalino.trigger(digitalChannels);
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }


    public void pwm(int value) {
        if(value < 0 || value > 255)
            Toast.makeText(getApplicationContext(), "Valores entre (0,255)", Toast.LENGTH_SHORT).show();
        else {
            try {
                bitalino.pwm(value);
            } catch (BITalinoException e) {
                e.printStackTrace();
            }
        }
    }

    public void ligar() {
        ligar("20:16:04:12:00:41");
    }

    public void ligar(String ip) {
        try {
            bitalino.connect(ip);
            bitalino.start(new int[]{0,1,2,3,4,5}, 10);
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }

    public void desligar() {
        digitalChannels[0] = 0;
        digitalChannels[1] = 0;
        pwm(0);
        try {
            bitalino.trigger(digitalChannels);
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
        try {
            bitalino.disconnect();
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }

    public String informacao(){
        try {
            bitalino.state();
        } catch (BITalinoException e) {
            e.printStackTrace();
        }

        if(!BITalinoVersion.equals("null")) {
            //BITalinoName= Device 20:15:05:29:21:77: CONNECTED
            String aux = BITalinoName +"/"+ BITalinoVersion;
            return aux;
        }

        String aux = BITalinoVersion + "/" + BITalinoName;
        return aux;
    }

    public int infoFrame(){
        return BITalinoFrame;
    }


    public void startAnalog(){
        try {
            bitalino.start(new int[]{0,1,2,3,4,5}, 10);
        } catch (BITalinoException e) {
            e.printStackTrace();
        }
    }


    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (Constants.ACTION_STATE_CHANGED.equals(action)) {
                String identifier = intent.getStringExtra(Constants.IDENTIFIER);
                Constants.States state = Constants.States.getStates(intent.getIntExtra(Constants.EXTRA_STATE_CHANGED,0));
                Log.i(TAG, "Device " + identifier + ": " + state.name());
                BITalinoName = ("Device " + identifier + ": " + state.name());

            } else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
                BITalinoFrame frame = intent.getParcelableExtra(Constants.EXTRA_DATA);
                Log.d(TAG, "BITalinoFrame: " + frame.toString());

            } else if (Constants.ACTION_COMMAND_REPLY.equals(action)) {
                String identifier = intent.getStringExtra(Constants.IDENTIFIER);
                Parcelable parcelable = intent.getParcelableExtra(Constants.EXTRA_COMMAND_REPLY);
                if(parcelable.getClass().equals(BITalinoState.class)){
                    Log.d(TAG, "BITalinoState: " + parcelable.toString());
                } else if(parcelable.getClass().equals(BITalinoDescription.class)){
                    Log.d(TAG, "BITalinoDescription: isBITalino2: " + ((BITalinoDescription)parcelable).isBITalino2() + "; FwVersion:" + String.valueOf(((BITalinoDescription)parcelable).getFwVersion()));
                    BITalinoVersion = ("BITalinoDescription: isBITalino2: " + ((BITalinoDescription)parcelable).isBITalino2() + "; FwVersion:" + String.valueOf(((BITalinoDescription)parcelable).getFwVersion()));
                }
            } else if (Constants.ACTION_MESSAGE_SCAN.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(Constants.EXTRA_DEVICE_SCAN);
            }
        }
    };

    protected static IntentFilter updateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_STATE_CHANGED);
        intentFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(Constants.ACTION_COMMAND_REPLY);
        intentFilter.addAction(Constants.ACTION_MESSAGE_SCAN);
        return intentFilter;
    }

}
