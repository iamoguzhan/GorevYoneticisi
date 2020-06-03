package com.example.gorevyoneticisi;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private DatabaseReference planRef;
    private RecyclerView planList;
    private Dialog myDialog;
    private CalendarView calendarView;
    private Button createBtn, showAllBtn;
    private String selectedDate;
    private RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        planRef = FirebaseDatabase.getInstance().getReference().child("Plans");

        calendarView = (CalendarView) findViewById(R.id.calendar_view);
        createBtn = (Button) findViewById(R.id.main_create_plan_btn);
        showAllBtn = (Button) findViewById(R.id.main_show_all_btn);
        mainLayout = (RelativeLayout) findViewById(R.id.layout_main_activity);
        planList = findViewById(R.id.plan_recycler_view);
        planList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        myDialog = new Dialog(this);

        getSelectedDate();


        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();

        createBtn.setOnClickListener(new View.OnClickListener() {       //when click button, apply dim and open create new plan popup
            @Override
            public void onClick(View v) {
                applyDim(root, 0.5f);

                Intent myIntent = new Intent(getApplicationContext(), CreateNewPlanActivity.class);
                startActivity(myIntent);
            }
        });

        mainLayout.setOnClickListener(new View.OnClickListener() {      //when click mainLayout, clear dim
            @Override
            public void onClick(View v) {
                clearDim(root);
            }
        });

        showAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDate = null;
                holder();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        holder();

    }

    private void holder() {

        FirebaseRecyclerOptions<Plan> options = null;

        options = new FirebaseRecyclerOptions.Builder<Plan>().setQuery(planRef.orderByChild("startDate").startAt(selectedDate).endAt(selectedDate + "\uf8ff"), Plan.class).build();

        FirebaseRecyclerAdapter<Plan, PlanViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Plan, PlanViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PlanViewHolder holder, final int position, @NonNull final Plan model) {

                final String listCourseId = getRef(position).getKey();      //get children of the planRef


                planRef.child(listCourseId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            String planEndDate = dataSnapshot.child("endDate").getValue().toString();

                            try {
                                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
                                Date currentDate = new Date();

                                Date currentDateFin = dateFormatter.parse(dateFormatter.format(currentDate));
                                Date planEndDateGmt = dateFormatter.parse(planEndDate);

                                if (planEndDateGmt.before(currentDateFin)) {         //if plan is finished, delete from database
                                    planRef.child(listCourseId).removeValue();
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                planRef.child(listCourseId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            holder.startDate.setText(dataSnapshot.child("startDate").getValue().toString());                //get from database and write to cardview
                            holder.endDate.setText(dataSnapshot.child("endDate").getValue().toString());
                            holder.title.setText(dataSnapshot.child("title").getValue().toString());
                            holder.note.setText(dataSnapshot.child("note").getValue().toString());
                            holder.startTime.setText(dataSnapshot.child("startTime").getValue().toString());

                            if (dataSnapshot.child("endTime").getValue().toString().isEmpty()) {        //if endTime does not exist, make endTime end hyphen visibilty gone
                                holder.endTime.setVisibility(View.GONE);
                                holder.hyphen.setVisibility(View.GONE);
                            } else {                                                                    //Otherwise, write to cardview
                                holder.endTime.setText(dataSnapshot.child("endTime").getValue().toString());
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.trash.setOnClickListener(new View.OnClickListener() {        //when press trash icon, delete plan from database
                    @Override
                    public void onClick(View v) {
                        planRef.child(listCourseId).removeValue();
                    }
                });

            }

            @NonNull
            @Override
            public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
                View view = LayoutInflater.from(p.getContext()).inflate(R.layout.plan_view_design, p, false);
                PlanViewHolder viewHolder = new PlanViewHolder(view);
                return viewHolder;
            }
        };

        planList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }

    public static class PlanViewHolder extends RecyclerView.ViewHolder {

        TextView startDate, endDate, title, note, startTime, endTime, hyphen;
        ImageView trash;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);

            startDate = itemView.findViewById(R.id.design_plan_start_date);
            endDate = itemView.findViewById(R.id.design_plan_end_date);
            title = itemView.findViewById(R.id.design_plan_title);
            note = itemView.findViewById(R.id.design_plan_note);
            startTime = itemView.findViewById(R.id.design_plan_start_time);
            endTime = itemView.findViewById(R.id.design_plan_end_time);
            hyphen = itemView.findViewById(R.id.design_hyphen_time);
            trash = itemView.findViewById(R.id.trash);
        }
    }

    private void getSelectedDate() {     //get selected date from calendar


        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

                selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year;

                onStart();

            }
        });

    }

    public static void applyDim(@NonNull ViewGroup parent, float dimAmount) {        //make screen dim
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * dimAmount));

        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    public static void clearDim(@NonNull ViewGroup parent) {        //clear screen dim
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.clear();
    }

}