package com.scc.main1;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;
import com.scc.main1.local.MyEvent;

public class ShareActivity extends AppCompatActivity{
    private MaterialCalendarView calendarView;
    public LinearLayout eventContainer;
    private EditText eventEditText;
    private List<EditText> eventEditTextList = new ArrayList<>();
    private TextView friendname;
    private Button  X_btn;
    private FirebaseFirestore db;
    private CollectionReference friendEventsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_event);
        calendarView = findViewById(R.id.calendarview);
        X_btn = findViewById(R.id.X_Btn);
        eventContainer = findViewById(R.id.eventContainer);
        friendname = findViewById(R.id.friend_name);
        eventEditTextList = new ArrayList<>();
        String friendEmail = getIntent().getStringExtra("FRIEND_EMAIL");
        String friendName = getIntent().getStringExtra("FRIEND_NAME");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        friendEventsRef = db.collection("users").document(friendEmail).collection("events");

        friendname.setText(friendName);

        calendarView.state()
                .edit()
                .setFirstDayOfWeek(DayOfWeek.of(Calendar.SATURDAY))
                .commit();

        // 월, 요일을 한글로 보이게 설정 (MonthArrayTitleFormatter의 작동을 확인하려면 밑의 setTitleFormatter()를 지운다)
        calendarView.setTitleFormatter(new MonthArrayTitleFormatter(getResources().getTextArray(R.array.custom_months)));
        calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)));

        // 좌우 화살표 사이 연, 월의 폰트 스타일 설정
        calendarView.setHeaderTextAppearance(R.style.CalendarWidgetHeader);

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                // 선택된 날짜에 대한 동작을 정의
                if (selected) {
                    LocalDate selectedDate = date.getDate();
                    // 선택된 날짜에 해당하는 일정을 불러오는 등의 작업을 수행
                    loadEventsForDate(selectedDate);
                }
            }
        });

        // 좌우 화살표 가운데의 연/월이 보이는 방식 커스텀
        calendarView.setTitleFormatter(new TitleFormatter() {
            @Override
            public CharSequence format(CalendarDay day) {
                // CalendarDay라는 클래스는 LocalDate 클래스를 기반으로 만들어진 클래스다
                // 때문에 MaterialCalendarView에서 연/월 보여주기를 커스텀하려면 CalendarDay 객체의 getDate()로 연/월을 구한 다음 LocalDate 객체에 넣어서
                // LocalDate로 변환하는 처리가 필요하다
                LocalDate inputText = day.getDate();
                String[] calendarHeaderElements = inputText.toString().split("-");
                StringBuilder calendarHeaderBuilder = new StringBuilder();
                calendarHeaderBuilder.append(calendarHeaderElements[0])
                        .append(" ")
                        .append(calendarHeaderElements[1]);
                return calendarHeaderBuilder.toString();
            }
        });
        X_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShareActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void loadEventsForDate(LocalDate selectedDate) {
        // 전체 데이터 가져오기
        friendEventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 선택된 날짜에 해당하는 이벤트 필터링
                List<MyEvent> eventsForDate = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                    MyEvent myEvent = document.toObject(MyEvent.class);

                    // 수정된 부분: 선택된 날짜에 해당하는 이벤트만 추가
                    if (myEvent != null && isEventInRange(myEvent, selectedDate)) {
                        Log.d(TAG, "Event title: " + myEvent.getEventTitle());
                        Log.d(TAG, "Event place: " + myEvent.getEventPlace());
                        eventsForDate.add(myEvent);
                    }
                }

                // 이전에 추가된 eventEditText 제거
                removePreviousEventEditText();

                // 선택된 날짜에 해당하는 이벤트를 사용하여 UI 업데이트
                updateUIWithEvents(eventsForDate);
            } else {
                Toast.makeText(getApplicationContext(), "Error getting events: " + task.getException(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting events", task.getException());
            }
        });
    }

    private boolean isEventInRange(MyEvent myEvent, LocalDate selectedDate) {
        LocalDate startDate = LocalDate.parse(myEvent.getStartDate()); // 시작 날짜
        LocalDate endDate = LocalDate.parse(myEvent.getEndDate()); // 끝 날짜

        // 선택된 날짜가 시작 날짜와 끝 날짜 사이에 있는지 확인
        return !selectedDate.isBefore(startDate) && !selectedDate.isAfter(endDate);
    }
    private void updateUIWithEvents(List<MyEvent> eventsForDate) {
        for (MyEvent myEvent : eventsForDate) {
            Log.d(TAG, "Updating UI with event: " + myEvent.getEventTitle());
            Log.d(TAG, "Updating UI with event: " + myEvent.getEventPlace());

            // Share 값에 따라 텍스트 조건부 표시
            if (myEvent.getShare() == 1) {
                // Share 값이 1인 경우: 출력하지 않음
                continue;
            }

            // Share 값이 0인 경우: 텍스트로 표시
            // 새로운 EditText를 동적으로 생성
            eventEditText = new EditText(ShareActivity.this);
            Log.d(TAG, "Setting EditText ID: " + eventEditText.getId()); // Add this log

            eventEditText.setText("일정 제목: " + myEvent.getEventTitle() +
                    "\n장소: " + myEvent.getEventPlace() +
                    "\n\n");

            // 필요에 따라 스타일이나 속성을 설정할 수 있습니다.

            // 생성된 EditText를 LinearLayout에 추가
            eventContainer.addView(eventEditText);
            eventEditText.setEnabled(true); // EditText를 수정 불가능하게 설정
            eventEditText.setFocusable(false); // EditText의 포커스를 받지 않도록 설정

            // eventEditText를 리스트에 추가
            eventEditTextList.add(eventEditText);
        }
    }

    private void removePreviousEventEditText() {
        LinearLayout eventContainer = findViewById(R.id.eventContainer);

        for (EditText editText : eventEditTextList) {
            // eventEditText를 eventContainer에서 제거
            eventContainer.removeView(editText);
        }

        // 수정된 부분: eventEditTextList 초기화
        eventEditTextList.clear();
    }
}
