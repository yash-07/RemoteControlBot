package in.ac.siesgst.npl.remotecontrolbot;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {


    String ipAddress;
    EditText ip;
    Button submitButton;
    String curr = "";
    TextView status;
    JoystickView joystickLeft;
    Toolbar toolbar;

    SharedPreferenceManager sharedPreferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferenceManager = new SharedPreferenceManager(MainActivity.this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        joystickLeft = (JoystickView) findViewById(R.id.joystickView_left);
        status = findViewById(R.id.currentStatus);

        ip = findViewById(R.id.ip);
        submitButton = findViewById(R.id.submitButton);


        if(sharedPreferenceManager.isFirstRun()) {
            sharedPreferenceManager.firstRun();
            sharedPreferenceManager.defaultControls();
            Log.d("First","Run");
        }
        if (sharedPreferenceManager.isIpThere()) {
            submitButton.setVisibility(View.GONE);
            ip.setVisibility(View.GONE);

            ip.setEnabled(false);
            submitButton.setEnabled(false);

            status.setVisibility(View.VISIBLE);
            joystickLeft.setVisibility(View.VISIBLE);

        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ipAddress = ip.getText().toString();
                submitButton.setVisibility(View.GONE);
                ip.setVisibility(View.GONE);

                sharedPreferenceManager.createIpSession(ipAddress);

                ip.setEnabled(false);
                submitButton.setEnabled(false);


                status.setVisibility(View.VISIBLE);
                joystickLeft.setVisibility(View.VISIBLE);

            }
        });
        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {

                if(((angle<=30 && angle>=0)||(angle>=331 && angle<=359)) && strength>=85 && !curr.equals("rr")) {
                    status.setText("Hard Right");
                    new Thread(new UDPSender(sharedPreferenceManager.getRight())).start();
                    curr = "rr";
                }
                else if(angle>=31 && angle<=60 && strength>=85 && !curr.equals("rf")) {
                    status.setText("Soft Right F");
                    new Thread(new UDPSender(sharedPreferenceManager.getSoftRightF())).start();
                    curr = "rf";
                }
                else if(angle>=61 && angle<=120 && strength>=85 && !curr.equals("ff")) {
                    status.setText("Forward");
                    new Thread(new UDPSender(sharedPreferenceManager.getForward())).start();
                    curr = "ff";
                }
                else if(angle>=121 && angle<=150 && strength>=85 && !curr.equals("lf")) {
                    status.setText("Soft Left F");
                    new Thread(new UDPSender(sharedPreferenceManager.getSoftLeftF())).start();
                    curr = "lf";
                }
                else if(angle>=151 && angle<=210 && strength>=85 && !curr.equals("ll")) {
                    status.setText("Hard Left");
                    new Thread(new UDPSender(sharedPreferenceManager.getLeft())).start();
                    curr = "ll";
                }
                else if(angle>=211 && angle<=240 && strength>=85 && !curr.equals("lb")) {
                    status.setText("Soft Left B");
                    new Thread(new UDPSender(sharedPreferenceManager.getSoftLeftB())).start();
                    curr = "lb";
                }
                else if(angle>=241 && angle<=300 && strength>=85 && !curr.equals("bb")) {
                    status.setText("Backward");
                    new Thread(new UDPSender(sharedPreferenceManager.getBackward())).start();
                    curr = "bb";
                }
                else if(angle>=301 && angle<=330 && strength>=85 && !curr.equals("rb")) {
                    status.setText("Soft Right B");
                    new Thread(new UDPSender(sharedPreferenceManager.getSoftRightB())).start();
                    curr = "rb";
                }
                else if(strength<=85 && !curr.equals("reset")) {
                    status.setText("");
                    new Thread(new UDPSender(sharedPreferenceManager.getMotorReset())).start();
                    curr = "reset";
                }
            }
        });
    }
    class UDPSender implements Runnable{

        int message;

        public UDPSender(int message) {
            this.message = message;

        }

        @Override
        public void run() {
            try{
                DatagramSocket udpsocket = new DatagramSocket(1111);
                udpsocket.setReuseAddress(true);
                InetAddress serverAddress = InetAddress.getByName(sharedPreferenceManager.getIp());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PrintStream pout = new PrintStream(byteArrayOutputStream);
                pout.print(message);
                byte[] buf = byteArrayOutputStream.toByteArray();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, 1111);
                udpsocket.send(packet);
                udpsocket.close();
            } catch(SocketException e){
                Log.e("UDP", "Socket Error"+e);
            } catch(IOException e){
                Log.e("UDP Send", "IO Error"+e);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.mymenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.settings) {
            Intent i = new Intent(MainActivity.this,SettingsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("ip_set",sharedPreferenceManager.isIpThere());
            i.putExtra("ip",sharedPreferenceManager.getIp());
            startActivity(i);

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStop() {

        sharedPreferenceManager.closeIpSession();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
