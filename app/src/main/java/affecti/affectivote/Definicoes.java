package affecti.affectivote;


import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import info.plux.pluxapi.Constants;
import pl.droidsonroids.gif.GifTextView;


public class Definicoes extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    final int[] digitalChannels = new int[2];

    ServiceBitalino mService;
    boolean mBounded;
    Intent intentServiceBitalino;

    GifTextView imageLoading, imageCorrect, imageIncorrect;

    EditText textConexao;

    ComponentName service = null;


    boolean getInfo = false;
    Handler handler;
    int delay = 500; //milliseconds

    Button buttonLigar;
    private Constants.States currentState = Constants.States.DISCONNECTED;
    public static boolean flag = false;
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definicoes);



        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        initElements();
        initUIElements();


        if(flag==false) {  // Primeira vez que onCreate é corrido. Começa o serviço e pede o para ligar o bluetooth
            flag=true;
            service = startService(new Intent(getBaseContext(), ServiceBitalino.class));
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        doBindService();

        handler.postDelayed(new Runnable() {
            public void run() {
                if(!getInfo) {
                    System.out.println("GETINFO");
                    bitalinoInformacao();
                    handler.postDelayed(this, delay);
                }
            }
        }, delay);


    }

    private void initUIElements() {
        buttonLigar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean formatoMAC = textConexao.getText().toString().matches("\\d\\d:\\d\\d:\\d\\d:\\d\\d:\\d\\d:\\d\\d");
                if(formatoMAC) {
                    imageIncorrect.setVisibility(View.INVISIBLE);
                    findViewById(R.id.imageView_loading).setVisibility(View.VISIBLE);
                    mService.ligar(textConexao.getText().toString());
                }
                else {
                    findViewById(R.id.imageView_loading).setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "MAC Address tem de estar no formato \"00:00:00:00:00:00\"", Toast.LENGTH_LONG).show();

                }
            }
        });

    }


    private void bitalinoInformacao() {
        String aux = mService.informacao();

        String[] informacao = aux.split("/");
        String bitalinoState = informacao[0];
        String bitalinoName = informacao[1];


        if(bitalinoState.equals("null")){

        }
        else {
            String[] bitalinoInfo = bitalinoState.split(" "); // String retornada: Device 20:15:05:29:21:77: CONNECTED
            System.out.println(bitalinoInfo[2]);
            if(bitalinoInfo[2].equals("CONNECTED")) {

                imageLoading.setVisibility(View.INVISIBLE);
                imageIncorrect.setVisibility(View.INVISIBLE);
                imageCorrect.setVisibility(View.VISIBLE);
                getInfo = true;


                handler.postDelayed(new Runnable() {
                    public void run() {
                        SharedPreferences settings = getSharedPreferences("Pref", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("MACAddress", textConexao.getText().toString());
                        editor.commit();

                        Intent intent = new Intent(Definicoes.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, delay);
            }


        }

    }

    private void initElements(){

        intentServiceBitalino = new Intent(this, ServiceBitalino.class);
        handler = new Handler();

        textConexao = (EditText) findViewById(R.id.editText_MACAdress);
        SharedPreferences settings = getSharedPreferences("Pref", 0);
        String str_mac = settings.getString("MACAddress", null);
        textConexao.setText(str_mac);

        buttonLigar = (Button) findViewById(R.id.button_ligar);
        imageCorrect = (GifTextView) findViewById(R.id.imageView_correct);
        imageIncorrect = (GifTextView) findViewById(R.id.imageView_incorrect);
        imageLoading = (GifTextView) findViewById(R.id.imageView_loading);

    }



    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("OnResume Definicoes");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy Definicoes");
    }

    @Override
    protected void onPause(){
        super.onPause();
        System.out.println("OnPause Definicoes");
        doUnbindService();
    }

    /*
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Definicoes.this, MainActivity.class);
        intent.putExtra("Back Pressed",true);
        startActivity(intent);
        finish();

    }
    */


    ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            ServiceBitalino.LocalBinder mLocalBinder = (ServiceBitalino.LocalBinder)service;
            mService = mLocalBinder.getServerInstance();
        }
    };

    public void doBindService() {
        System.out.println("!!");
        bindService(intentServiceBitalino, mConnection, BIND_AUTO_CREATE);
        mBounded = true;
    }

    public void doUnbindService() {
        if (mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    }

}