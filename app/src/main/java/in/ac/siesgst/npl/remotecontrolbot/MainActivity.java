package in.ac.siesgst.npl.remotecontrolbot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    public static final String ROBO_SOCCER = "Robo Soccer";
    public static final String SHELL_SHOCK = "Shell Shock";
    public static final String ROBO_WARS = "Robo Wars";
    public static final Event[] events = new Event[]{
            new Event(ROBO_SOCCER, R.drawable.robosoccer, R.drawable.kick, R.drawable.robosoccer_joy),
            new Event(SHELL_SHOCK, R.drawable.shellshock, R.drawable.bullet, R.drawable.robosoccer_joy),
            new Event(ROBO_WARS, R.drawable.robosoccer, R.drawable.bullet, R.drawable.robowars_joy)
    };
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.robo_events_list);
        recyclerView.setAdapter(new EventAdapter());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    static class Event {
        private String name;
        private int backgroundDrawable;
        private int buttonDrawable;
        private int joyDrawable;

        public Event(String name, int backgroundDrawable, int buttonDrawable, int joyDrawable) {
            this.name = name;
            this.backgroundDrawable = backgroundDrawable;
            this.buttonDrawable = buttonDrawable;
            this.joyDrawable = joyDrawable;
        }
    }

    class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.event_card, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.eventName.setText(events[position].name);
            Glide.with(MainActivity.this)
                    .load(events[position].backgroundDrawable)
                    .into(holder.eventBg);
            holder.bindViewHolder();
        }

        @Override
        public int getItemCount() {
            return events.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView eventName;
            ImageView eventBg;

            public ViewHolder(View itemView) {
                super(itemView);
                eventName = itemView.findViewById(R.id.event_title);
                eventBg = itemView.findViewById(R.id.event_bg);
            }

            void bindViewHolder() {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent joyIntent = new Intent(MainActivity.this, JoystickActivity.class);
                        joyIntent.putExtra("name", events[getAdapterPosition()].name);
                        joyIntent.putExtra("bg", events[getAdapterPosition()].backgroundDrawable);
                        joyIntent.putExtra("button_bg", events[getAdapterPosition()].buttonDrawable);
                        joyIntent.putExtra("joy_bg", events[getAdapterPosition()].joyDrawable);
                        startActivity(joyIntent);
                    }
                });
            }
        }
    }
}
