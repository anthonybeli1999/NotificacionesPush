package com.example.notificacionespush.Actividades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.notificacionespush.App.Config;
import com.example.notificacionespush.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    TextView txttitulo, txtMensaje;
    ImageView img_noticia;
    TextView txtRegID;
    BroadcastReceiver broadcastReceiver;
    private static String TOKEN;
    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this,
                new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String nuevoToken = instanceIdResult.getToken();
                        TOKEN = nuevoToken;
                        Log.e("NEWTOKEN", nuevoToken);
                    }
                });

        txtRegID = findViewById(R.id.txtRegID);
        txttitulo = findViewById(R.id.txt_titulo);
        txtMensaje = findViewById(R.id.txt_mensaje);
        img_noticia = findViewById(R.id.img_noticia);

        broadcastReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)){
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)){
                    String titulo = intent.getStringExtra("titulo");
                    String mensaje = intent.getStringExtra("mensaje");
                    String url_imagen = intent.getStringExtra("url_imagen");

                    Glide.with(getApplicationContext()).load(url_imagen).into(img_noticia);
                    txttitulo.setText(titulo);
                    txtMensaje.setText(mensaje);

                    almacenarPreferencia(TOKEN);
                    mostrarFirebaseId();

                }
            }
        };
    }

    private void almacenarPreferencia(String token){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("REGID", token);
        editor.commit();
    }

    private void mostrarFirebaseId(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = sharedPreferences.getString("REGID", null);
        Log.e(TAG, "Firebase Id: " + regId);
        if (!TextUtils.isEmpty(regId)){
            txtRegID.setText("Firebase Id: " + regId);
        } else {
            txtRegID.setText("No existe una respuesta de Firebase aun");
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Config.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));
        clearNotification();
    }


    public void clearNotification(){
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void onPause(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }
}
