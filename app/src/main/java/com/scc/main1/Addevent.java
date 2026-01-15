package com.scc.main1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.scc.main1.local.MyEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Addevent extends AppCompatActivity {
    private EditText title, left_time, right_time, place;
    private Button cancel_btn, save_btn;
    private Switch share_switch;
    private int share = 0;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_event);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // SharedPreferences 초기화

        share_switch = findViewById(R.id.share_Switch);
        cancel_btn = findViewById(R.id.cancel_btn);
        save_btn = findViewById(R.id.save_btn);
        title = findViewById(R.id.title_text);
        left_time = findViewById(R.id.left_time);
        right_time = findViewById(R.id.right_time);
        place = findViewById(R.id.place);

        left_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(left_time);
            }
        });

        right_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(right_time);
            }
        });

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title.setText("");
            }
        });

        place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                place.setText("");
            }
        });

        share_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    share = 1;
                } else {
                    share = 0;
                }
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Addevent.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 제목, 시작날짜, 끝날짜, 장소를 사용하여 새 이벤트를 만듭니다.
                String eventTitle = title.getText().toString();
                String startEventDate = left_time.getText().toString(); // left_time이 시작날짜 EditText인 것으로 가정합니다.
                String endEventDate = right_time.getText().toString(); // right_time이 끝날짜 EditText인 것으로 가정합니다.
                String eventPlace = place.getText().toString();

                MyEvent newEvent = new MyEvent(eventTitle, startEventDate, endEventDate, eventPlace, share);

                // 이벤트를 Firestore에 저장합니다.
                saveEventToFirestore(newEvent);
            }
        });
    }

    private void showDatePickerDialog(final EditText dateEditText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                        String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth);
                        dateEditText.setText(formattedDate);
                    }
                },
                year,
                month,
                dayOfMonth
        );

        datePickerDialog.show();
    }

    private void saveEventToFirestore(MyEvent event) {
        // CollectionReference 생성 (유저별로 컬렉션을 만들기 위해 유저 이메일을 사용)
        CollectionReference userEventsRef = db.collection("users").document(currentUser.getEmail()).collection("events");

        // 일정 데이터를 Map으로 만들어 Firestore에 추가
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventTitle", event.getEventTitle());
        eventData.put("startDate", event.getStartDate());
        eventData.put("endDate", event.getEndDate());
        eventData.put("eventPlace", event.getEventPlace());
        eventData.put("share", share);

        userEventsRef.document()
                .set(eventData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 성공적으로 추가된 경우
                        // 추가적인 작업이 필요하다면 이곳에 작성
                        Intent intent3 = new Intent(Addevent.this, MainActivity.class);
                        // 새로 추가된 이벤트만을 전달
                        intent3.putExtra("newEvent", event);
                        startActivity(intent3);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 추가에 실패한 경우
                        // 실패에 대한 처리를 이곳에 작성
                    }
                });
    }
}
