package in.ac.siesgst.npl.remotecontrolbot;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.TextInputEditText;
import android.support.transition.TransitionManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class JoystickActivity extends AppCompatActivity {

    private String curr = "";

    private JoystickView joystickView;
    private int backgroundDrawable;
    private int buttonDrawable;
    private int joyDrawable;
    private ImageView secondaryAction;
    private String eventName;
    private TextView eventTitle;
    private ConstraintLayout rootView;
    private TextView editConnection;
    private SharedPreferenceManager sharedPreferenceManager;

    // Swap Controls
    private boolean isJoystickToRight = true;
    private ImageView swapControlButton;

    // AlertDialog
    private View dialogView;
    private TextInputEditText ipAddressText;
    private Button submitButton;
    private TextView playerActionTitle;
    private RadioGroup playerActionGroup;
    private RadioButton attackRadio;
    private RadioButton defenseRadio;
    private AlertDialog connectivityDialog;

    private void swapControls() {
        ConstraintSet rootConstraintSet = new ConstraintSet();
        rootConstraintSet.clone(rootView);

        rootConstraintSet.clear(R.id.joystick_view, ConstraintSet.END);
        rootConstraintSet.clear(R.id.joystick_view, ConstraintSet.START);

        rootConstraintSet.clear(R.id.button_view, ConstraintSet.START);
        rootConstraintSet.clear(R.id.button_view, ConstraintSet.END);

        rootConstraintSet.setMargin(R.id.button_view, ConstraintSet.START, 16);
        rootConstraintSet.setMargin(R.id.button_view, ConstraintSet.END, 16);

        if (isJoystickToRight) {
            rootConstraintSet.connect(R.id.joystick_view, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            rootConstraintSet.connect(R.id.button_view, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        } else {
            rootConstraintSet.connect(R.id.joystick_view, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            rootConstraintSet.connect(R.id.button_view, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        }

        TransitionManager.beginDelayedTransition(rootView);

        rootConstraintSet.applyTo(rootView);

        isJoystickToRight = !isJoystickToRight;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick);

        eventName = getIntent().getStringExtra("name");
        backgroundDrawable = getIntent().getIntExtra("bg", R.drawable.shellshock);
        buttonDrawable = getIntent().getIntExtra("button_bg", R.drawable.bullet);
        joyDrawable = getIntent().getIntExtra("joy_bg", R.drawable.robowars_joy);

        eventTitle = findViewById(R.id.joystick_event_title);
        eventTitle.setText(eventName);

        rootView = findViewById(R.id.joystick_root_view);
        rootView.setBackgroundResource(backgroundDrawable);

        joystickView = findViewById(R.id.joystick_view);
        joystickView.setBackgroundResource(joyDrawable);

        secondaryAction = findViewById(R.id.button_view);
        secondaryAction.setImageResource(buttonDrawable);
        secondaryAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO UDPSender impl
                Toast.makeText(JoystickActivity.this, "Secondary Action", Toast.LENGTH_SHORT).show();
            }
        });

        editConnection = findViewById(R.id.edit_connection);

        swapControlButton = findViewById(R.id.controls_swap_button);

        swapControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapControls();
            }
        });

        sharedPreferenceManager = new SharedPreferenceManager(JoystickActivity.this);

        dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_connectivity, null, false);

        ipAddressText = dialogView.findViewById(R.id.ip_address_textview);
        submitButton = dialogView.findViewById(R.id.submit_button);

        playerActionTitle = dialogView.findViewById(R.id.player_selection_title);

        playerActionGroup = dialogView.findViewById(R.id.player_selection_group);

        defenseRadio = dialogView.findViewById(R.id.strategy_defense);

        attackRadio = dialogView.findViewById(R.id.strategy_attack);

        connectivityDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (sharedPreferenceManager.isIpThere()) {
            ipAddressText.setText(sharedPreferenceManager.getIp());
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ipAddress = ipAddressText.getText().toString();

                if (ipAddress.isEmpty()) {
                    ipAddressText.setError("Please Enter a Valid IP");
                    return;
                }

                if (!eventName.equals(MainActivity.ROBO_WARS)
                        && !attackRadio.isChecked()
                        && !defenseRadio.isChecked()) {
                    Toast.makeText(JoystickActivity.this, "Please select a player strategy", Toast.LENGTH_SHORT).show();
                    return;
                }

                sharedPreferenceManager.createIpSession(ipAddress);

                connectivityDialog.dismiss();
            }
        });

        connectivityDialog.show();

        editConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!connectivityDialog.isShowing()) {
                    connectivityDialog.show();
                }
            }
        });

        if (!eventName.equals(MainActivity.ROBO_WARS)) {
            playerActionGroup.setVisibility(View.VISIBLE);
            playerActionTitle.setVisibility(View.VISIBLE);
            secondaryAction.setVisibility(View.VISIBLE);
        } else {
            playerActionGroup.setVisibility(View.GONE);
            playerActionTitle.setVisibility(View.GONE);
            secondaryAction.setVisibility(View.GONE);
        }

        if (sharedPreferenceManager.isFirstRun()) {
            sharedPreferenceManager.firstRun();
            sharedPreferenceManager.defaultControls();
            Log.d("First", "Run");
        }

        joystickView.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {

                if (((angle <= 30 && angle >= 0) || (angle >= 331 && angle <= 359)) && strength >= 85 && !curr.equals("rr")) {
//                    status.setText("Hard Right");
                    new Thread(new UDPSender(sharedPreferenceManager.getRight())).start();
                    curr = "rr";
                } else if (angle >= 31 && angle <= 60 && strength >= 85 && !curr.equals("rf")) {
//                    status.setText("Soft Right F");
                    new Thread(new UDPSender(sharedPreferenceManager.getSoftRightF())).start();
                    curr = "rf";
                } else if (angle >= 61 && angle <= 120 && strength >= 85 && !curr.equals("ff")) {
//                    status.setText("Forward");
                    new Thread(new UDPSender(sharedPreferenceManager.getForward())).start();
                    curr = "ff";
                } else if (angle >= 121 && angle <= 150 && strength >= 85 && !curr.equals("lf")) {
//                    status.setText("Soft Left F");
                    new Thread(new UDPSender(sharedPreferenceManager.getSoftLeftF())).start();
                    curr = "lf";
                } else if (angle >= 151 && angle <= 210 && strength >= 85 && !curr.equals("ll")) {
//                    status.setText("Hard Left");
                    new Thread(new UDPSender(sharedPreferenceManager.getLeft())).start();
                    curr = "ll";
                } else if (angle >= 211 && angle <= 240 && strength >= 85 && !curr.equals("lb")) {
//                    status.setText("Soft Left B");
                    new Thread(new UDPSender(sharedPreferenceManager.getSoftLeftB())).start();
                    curr = "lb";
                } else if (angle >= 241 && angle <= 300 && strength >= 85 && !curr.equals("bb")) {
//                    status.setText("Backward");
                    new Thread(new UDPSender(sharedPreferenceManager.getBackward())).start();
                    curr = "bb";
                } else if (angle >= 301 && angle <= 330 && strength >= 85 && !curr.equals("rb")) {
//                    status.setText("Soft Right B");
                    new Thread(new UDPSender(sharedPreferenceManager.getSoftRightB())).start();
                    curr = "rb";
                } else if (strength <= 85 && !curr.equals("reset")) {
//                    status.setText("");
                    new Thread(new UDPSender(sharedPreferenceManager.getMotorReset())).start();
                    curr = "reset";
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent i = new Intent(JoystickActivity.this, SettingsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("ip_set", sharedPreferenceManager.isIpThere());
            i.putExtra("ip", sharedPreferenceManager.getIp());
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

    class UDPSender implements Runnable {

        int message;

        public UDPSender(int message) {
            this.message = message;

        }

        @Override
        public void run() {
            try {
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
            } catch (SocketException e) {
                Log.e("UDP", "Socket Error" + e);
            } catch (IOException e) {
                Log.e("UDP Send", "IO Error" + e);
            }
        }
    }

}
