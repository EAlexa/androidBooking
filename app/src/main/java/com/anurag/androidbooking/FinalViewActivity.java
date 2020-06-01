package com.anurag.androidbooking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class FinalViewActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    Button book;
    TextView alert;

    long maxLimit;
    long limit;
    long slotNumber;
    Scheduler schedule;

    @Override
    protected void onStart() {
        super.onStart();

        db.collection("Scheduler")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (final QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Get Scheduler", document.getId() + " => " + document.getData());
                                maxLimit = (long) document.get("maxPeopleLimit");
                                Log.d("Get Scheduler","maxLimit = " + maxLimit);

                                db.collection("Count")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Log.d("Collection Count", document.getId() + " => " + document.getData());
                                                        limit = (long) document.get("log");
                                                    }

                                                    db.collection("TimeSlots")
                                                            .whereEqualTo("email", auth.getCurrentUser().getEmail())
                                                            .get()
                                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                                            Log.d("Get TimeSlots", document.getId() + " => " + document.getData());
                                                                            if (task.getResult().size() == 1) {
                                                                                slotNumber = (long) document.get("SlotDetail");
                                                                            } else {
                                                                                slotNumber = 0;
                                                                            }

                                                                        }
                                                                        Log.d("Get TimeSlots","Count: " + task.getResult().size());
                                                                        Log.d("Slot:", " " + slotNumber);
                                                                        Log.d("Get Scheduler","maxLimit = " + maxLimit);
                                                                        //If result size is equal to zero, there is no booking for the current user

                                                                        if (limit == maxLimit) {
                                                                            Log.d("Limit reached","booked out");
                                                                            Toast.makeText(FinalViewActivity.this, "Already booked out", Toast.LENGTH_SHORT).show();
                                                                            alert.setText("Cafeteria booked out");
                                                                            
                                                                        } else if (slotNumber != 0) {
                                                                            Log.d("Already","booked");
                                                                            Toast.makeText(FinalViewActivity.this, "You already booked", Toast.LENGTH_SHORT).show();
                                                                            alert.setText("Already booked");
                                                                            
                                                                            
                                                                        } else {
                                                                            //available 
                                                                            Toast.makeText(FinalViewActivity.this, "Booking available", Toast.LENGTH_SHORT).show();
                                                                            book.setEnabled(true);
                                                                            book.setVisibility(View.VISIBLE);
                                                                        }

                                                                    } else {
                                                                        Log.d("Get TimeSlots", "Error getting documents: ", task.getException());
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    Log.d("Collection Count", "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d("Get Scheduler", "Error getting documents: ", task.getException());
                        }
                    }
                });
        Log.d("Get Scheduler end","maxLimit = " + maxLimit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_view);
        book = findViewById(R.id.bookButton);
        alert = findViewById(R.id.alertBox);
    }

    public void logoutPressed(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(FinalViewActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void bookSlot(View view) {
        db.collection("Scheduler")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                            schedule = new Scheduler();
                            schedule.eachSlotTime = (long) snapshot.get("eachSlotTime");
                            schedule.maxPeopleLimit = (long) snapshot.get("maxPeopleLimit");
                            schedule.personPerSlot = (long) snapshot.get("personPerSlot");
                            Log.d("schedule", "value eachSlotTime: " + schedule.eachSlotTime);
                            Log.d("schedule", "value maxPeopleLimit: " + schedule.maxPeopleLimit);
                            Log.d("schedule", "value personPerSlot: " + schedule.personPerSlot);
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });

        calculate();
    }

    public void calculate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        OffsetDateTime date = LocalDateTime.parse(LocalDate.now().toString(), dtf).atOffset(ZoneOffset.UTC);

        Log.d("Date: ", " " + date);
    }
}
