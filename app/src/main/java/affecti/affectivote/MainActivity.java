package affecti.affectivote;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import pl.droidsonroids.gif.GifTextView;

public class MainActivity extends AppCompatActivity {

    ServiceBitalino mService;
    boolean mBounded;
    Intent intentServiceBitalino;

    Handler handler;
    int delay = 100; //milliseconds

    boolean flag = false;

    TextView textView_SeekBarValue;
    GifTextView coracaoNormal, coracaoLento, coracaoRapido;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initElements();
        initUIElements();

        doBindService();

        if(!flag) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    flag = true;
                    mService.startAnalog();
                }
            }, 1000);
        }


            handler.postDelayed(new Runnable() {
                public void run() {
                    textView_SeekBarValue.setText(bitalinoInformacao());
                    handler.postDelayed(this, delay);
                }
            }, delay);




    }

    public void mudaLetra(double valor_saida){
        if(valor_saida > 0 && valor_saida < 40) {
            coracaoNormal.setVisibility(View.INVISIBLE);
            coracaoLento.setVisibility(View.VISIBLE);
            coracaoRapido.setVisibility(View.INVISIBLE);

            textView_SeekBarValue.setTextSize(15);
            textView_SeekBarValue.setTextColor(Color.WHITE);
        }
        if(valor_saida > 40 && valor_saida < 80) {
            coracaoNormal.setVisibility(View.VISIBLE);
            coracaoLento.setVisibility(View.INVISIBLE);
            coracaoRapido.setVisibility(View.INVISIBLE);

            textView_SeekBarValue.setTextSize(25);
            textView_SeekBarValue.setTextColor(Color.rgb(181, 43, 119));
        }
        if(valor_saida > 80) {
            mService.trigger(1,1);
            coracaoNormal.setVisibility(View.INVISIBLE);
            coracaoLento.setVisibility(View.INVISIBLE);
            coracaoRapido.setVisibility(View.VISIBLE);

            textView_SeekBarValue.setTextSize(40);
            textView_SeekBarValue.setTextColor(Color.RED);
        }
    }

    private String bitalinoInformacao() {
        int valor = mService.infoFrame();
        double aux = Math.ceil(valor / 1024.0 *100);
        mudaLetra(aux);
        return String.valueOf(aux);
    }

    private void initUIElements() {

    }


    private void initElements() {
        intentServiceBitalino = new Intent(this, ServiceBitalino.class);
        handler = new Handler();

        coracaoNormal = (GifTextView) findViewById(R.id.imageView_coracaoNormal);
        coracaoLento = (GifTextView) findViewById(R.id.imageView_coracaoLento);
        coracaoRapido = (GifTextView) findViewById(R.id.imageView_coracaoRapido);
    }


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
