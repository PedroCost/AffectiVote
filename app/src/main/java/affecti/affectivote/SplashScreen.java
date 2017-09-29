package affecti.affectivote;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);



        Thread startThread = new Thread(){
            @Override
            public void run() {
                try{
                    sleep(1000);
                    Intent intent = new Intent(getApplicationContext(),Definicoes.class);
                    startActivity(intent);
                    finish();
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        };

        startThread.start();
    }
}
