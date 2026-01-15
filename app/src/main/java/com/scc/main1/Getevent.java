package com.scc.main1;

import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.scc.main1.local.EventData;
import com.scc.main1.local.seoul;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import android.os.Handler;

public class Getevent extends AppCompatActivity {
    EditText selectedEditText;
    private EditText resultText;
    private TextView textView1, textView2, textView3, textView4, textView5, textView6;
    private Button localS, eventA, eventL;
    private TextView count;
    private int selectedCount = 0;
    private Spinner DO, SI1, Month, Gun, Codename, Free;
    private int SLI = 0;
    private List<EditText> editTextList = new ArrayList<>();
    private List<EventData> eventList = new ArrayList<>();
    private LinearLayout linearLayout;
    private SearchView searchView;
    private Switch Push;
    private static final int JOB_ID = 1;
    private static final int ALARM_INTERVAL_SECONDS = 10;
    private static final String PREFS_KEY_FCM_TOKEN = "fcmToken";
    private Context context;
    public static final String ACTION_REFRESH_DATA = "com.scc.main1.getevent.REFRESH_DATA";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private int share = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_event);

        localS = findViewById(R.id.button_local_select);
        eventA = findViewById(R.id.button_add_event);
        eventL = findViewById(R.id.button_loading_event);

        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        textView5 = findViewById(R.id.textView5);
        textView6 = findViewById(R.id.textView6);
        resultText = findViewById(R.id.textview_Data_result);
        linearLayout = findViewById(R.id.linearlayout1);
        Push = findViewById(R.id.push_btn);
        ScrollView scrollView = findViewById(R.id.scrollView);
        context = this;

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        DO = findViewById(R.id.spn_DoList);
        ArrayAdapter<String> DoAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.Dolist)
        );
        DO.setAdapter(DoAdapter);
        SI1 = findViewById(R.id.spn_SiList);

        DoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Month = findViewById(R.id.spn_MonthList);
        ArrayAdapter<String> MonAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.MonthList)
        );
        Month.setAdapter(MonAdapter);

        Gun = findViewById(R.id.spn_GunnameList);
        ArrayAdapter<String> GunAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.GunnameList)
        );
        Gun.setAdapter(GunAdapter);

        Codename = findViewById(R.id.spn_CodenameList);
        ArrayAdapter<String> CodenameAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.CodenameList)
        );
        Codename.setAdapter(CodenameAdapter);

        Free = findViewById(R.id.spn_FreeList);
        ArrayAdapter<String> FreeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.FreeList)
        );
        Free.setAdapter(FreeAdapter);

        searchView = findViewById(R.id.search);

        DO.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedDo = DO.getSelectedItem().toString();
                textView1.setText(getResources().getStringArray(R.array.Dolist)[position]);
                if (selectedDo.equals("경기도")) {
                    ArrayAdapter<String> Si1Adapter = new ArrayAdapter<>(Getevent.this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.Silist1));
                    Si1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    SI1.setAdapter(Si1Adapter);
                } else {
                    SI1.setAdapter(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                textView1.setText("선택 : ");
            }
        });

        SI1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedDo = SI1.getSelectedItem().toString();
                textView2.setText(getResources().getStringArray(R.array.Silist1)[position]);
                localS.setOnClickListener(null);
                SLI = 0;
                if (selectedDo.equals("서울시")) {
                    localS.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SLI = 1;
                            Toast toast = Toast.makeText(getApplicationContext(), "서울시로 설정되었습니다.", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                } else {
                    SLI = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                textView2.setText("선택 : ");
            }
        });

        Month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedMonth = getResources().getStringArray(R.array.MonthList)[position];
                textView3.setText(selectedMonth);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                textView3.setText("선택 : ");
            }
        });

        Gun.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedDo = Gun.getSelectedItem().toString();
                textView4.setText(getResources().getStringArray(R.array.GunnameList)[position]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                textView4.setText("선택 : ");
            }
        });

        Codename.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedDo = Codename.getSelectedItem().toString();
                textView5.setText(getResources().getStringArray(R.array.CodenameList)[position]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                textView5.setText("선택 : ");
            }
        });

        Free.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedDo = Free.getSelectedItem().toString();
                textView6.setText(getResources().getStringArray(R.array.FreeList)[position]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                textView6.setText("선택 : ");
            }
        });

        // 검색 이벤트 리스너 설정
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 검색어를 입력하고 검색 버튼을 눌렀을 때의 동작을 정의
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 검색어가 변경될 때마다 호출되는 동작을 정의
                performSearch(newText);
                return true;
            }
        });

        Push.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Start periodic data fetching when the switch is turned on
                    startPeriodicDataFetching(context);
                } else {
                    // Stop periodic data fetching when the switch is turned off
                    stopPeriodicDataFetching();
                }
            }
        });

        eventA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedEditText != null) {
                    // Extract information and save the event data
                    EventData eventData = extractEventData(selectedEditText);

                    // Save the eventData to Firestore or perform any other necessary actions
                    saveEventDataToFirestore(eventData);
                } else {
                    // Handle the case where no EditText is selected
                    Toast.makeText(Getevent.this, "아무런 일정도 선택되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        eventL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SLI == 1) {
                    // Get the selected month
                    String selectedMonth = Month.getSelectedItem().toString();
                    String selectedGun = Gun.getSelectedItem().toString();
                    String selectedCodename = Codename.getSelectedItem().toString();
                    String selectedFree = Free.getSelectedItem().toString();
                    seoul.SeoulData(selectedMonth, new seoul.DataCallback() {
                        @Override
                        public void onDataLoaded(List<EventData> eventList) {
                            // Update saved event list
                            Getevent.this.eventList = eventList;

                            // Initialize LinearLayout
                            linearLayout.removeAllViews();
                            selectedCount = 0;

                            for (EventData eventData : eventList) {
                                if (selectedMonth.equals(selectedMonth)) {
                                    // Check if the selected values match the event data
                                    if ((selectedGun.equals("전체") || selectedGun.equals(eventData.getGuname())) &&
                                            (selectedCodename.equals("전체") || selectedCodename.equals(eventData.getCodename())) &&
                                            (selectedFree.equals("전체") || selectedFree.equals(eventData.getIs_free()))) {
                                        // Save filter settings
                                        saveFilterSettings();
                                        // Create a new EditText
                                        EditText editText = new EditText(Getevent.this);
                                        StringBuilder editTextContent = new StringBuilder();

                                        // add data
                                        editTextContent.append(eventData.getDate()).append("\n");
                                        editTextContent.append(eventData.getCodename()).append("\n");
                                        editTextContent.append(eventData.getGuname()).append("\n");
                                        editTextContent.append(eventData.getIs_free()).append("\n");
                                        editTextContent.append(eventData.getPlace()).append("\n");
                                        editTextContent.append(eventData.getTicket()).append("\n");
                                        editTextContent.append(eventData.getTitle()).append("\n");
                                        editTextContent.append(eventData.getUse_fee()).append("\n");
                                        editTextContent.append(eventData.getUse_trgt()).append("\n");

                                        // Find the part that matches the search term and highlight it
                                        String query = searchView.getQuery().toString();
                                        String content = editTextContent.toString();
                                        int index = content.toLowerCase(Locale.getDefault()).indexOf(query.toLowerCase(Locale.getDefault()));
                                        if (index != -1) {
                                            SpannableStringBuilder builder = new SpannableStringBuilder(content);
                                            ForegroundColorSpan highlightSpan = new ForegroundColorSpan(Color.BLUE);
                                            builder.setSpan(highlightSpan, index, index + query.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            editText.setText(builder);
                                        } else {
                                            editText.setText(content);
                                        }

                                        editText.setEnabled(true); // Make EditText unmodifiable
                                        editText.setFocusable(false); // Set EditText not to receive focus

                                        // Process selection/cancellation when clicking on EditText
                                        editText.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (editTextList.contains(editText)) {
                                                    editTextList.remove(editText);
                                                    editText.setBackgroundColor(Color.LTGRAY);
                                                    selectedCount++;
                                                } else {
                                                    editTextList.add(editText);
                                                    editText.setBackgroundColor(Color.TRANSPARENT);
                                                    selectedCount--;
                                                }
                                                if (selectedCount < 0) {
                                                    selectedCount = 0;
                                                }
                                                selectedEditText = editText;

                                                // Check if the clicked editText is in the list
                                                if (editTextList.contains(selectedEditText)) {
                                                    // Extract data from the selected editText
                                                    EventData extractedEventData = extractEventData(selectedEditText);
                                                }
                                            }
                                        });
                                        linearLayout.addView(editText);
                                        editTextList.add(editText);
                                    }
                                }
                            }
                        }
                    });
                } else {
                    // Remove all views from scroll view
                    linearLayout.removeAllViews();
                    selectedCount = 0;
                }
            }
        });
    }
    private EventData extractEventData(EditText editText) {
        String content = editText.getText().toString();

        EventData eventData = new EventData();

        // startDate and endDate extraction
        List<String> dateList = Arrays.asList(content.split("~"));
        if (dateList.size() >= 2) {
            String startDate = dateList.get(0).trim();

            // Extracting endDate until the first newline character
            String endDate = dateList.get(1).substring(0, dateList.get(1).indexOf("\n")).trim();

            // Configure the eventData fields
            eventData.setStartDate(startDate);
            eventData.setEndDate(endDate);

            // Set startDate's endDate's date value
            eventData.setDate(startDate + "~" + endDate);
        }

        // Extract eventTitle (adjust based on the actual content format)
        List<String> titleLines = Arrays.asList(content.split("\n"));
        if (titleLines.size() > 6) {
            eventData.setEventTitle(titleLines.get(6));
        }

        // Extract eventPlace (adjust based on the actual content format)
        List<String> placeLines = Arrays.asList(content.split("\n"));
        if (placeLines.size() > 4) {
            eventData.setEventPlace(placeLines.get(4));
        }

        return eventData;
    }

    private void saveEventDataToFirestore(EventData eventData) {
        // Firestore code to save data (replace with your actual Firestore implementation)
        CollectionReference userEventsRef = db.collection("users").document(currentUser.getEmail()).collection("events");
        List<String> dateList = eventData.getDateList();
        String startDate = dateList.get(0);
        String endDate = dateList.get(1);

        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put("startDate", startDate);
        eventDataMap.put("endDate", endDate);
        eventDataMap.put("eventTitle", eventData.getTitle());
        eventDataMap.put("eventPlace", eventData.getPlace());
        eventDataMap.put("share", share);

        // Add the data to Firestore
        userEventsRef.document()
                .set(eventDataMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 성공적으로 추가된 경우
                        // 추가적인 작업이 필요하다면 이곳에 작성
                        Toast toast = Toast.makeText(getApplicationContext(), "일정이 추가되었습니다.", Toast.LENGTH_SHORT);
                        toast.show();
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

    private void startPeriodicDataFetching(Context context) {
        // Set up the AlarmManager for periodic tasks
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE  // or PendingIntent.FLAG_MUTABLE
        );

        long intervalMillis = ALARM_INTERVAL_SECONDS * 1000; // Convert seconds to milliseconds
        long triggerAtMillis = SystemClock.elapsedRealtime() + intervalMillis;

        // Set the alarm to trigger periodically
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, intervalMillis, pendingIntent);
    }

    private void stopPeriodicDataFetching() {
        // Cancel the AlarmManager when the switch is turned off
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);
    }

    private void saveFilterSettings() {
        SharedPreferences preferences = getSharedPreferences("FilterPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("selectedMonth", Month.getSelectedItem().toString());
        editor.putString("selectedGun", Gun.getSelectedItem().toString());
        editor.putString("selectedCodename", Codename.getSelectedItem().toString());
        editor.putString("selectedFree", Free.getSelectedItem().toString());

        editor.apply();
    }

    private void loadFilterSettings() {
        SharedPreferences preferences = getSharedPreferences("FilterPrefs", MODE_PRIVATE);

        String savedMonth = preferences.getString("selectedMonth", "DefaultMonthValue");
        String savedGun = preferences.getString("selectedGun", "DefaultGunValue");
        String savedCodename = preferences.getString("selectedCodename", "DefaultCodenameValue");
        String savedFree = preferences.getString("selectedFree", "DefaultFreeValue");

        // Set the spinner selections based on the loaded values
        Month.setSelection(getIndex(Month, savedMonth));
        Gun.setSelection(getIndex(Gun, savedGun));
        Codename.setSelection(getIndex(Codename, savedCodename));
        Free.setSelection(getIndex(Free, savedFree));
    }
    private int getIndex(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        return adapter.getPosition(value);
    }
    public void loadEventData() {
        Log.d("Getevent", "Loading event data");
        fetchDataInBackground();
    }

    private void fetchDataInBackground() {
        // Get the selected month
        String selectedMonth = Month.getSelectedItem().toString();
        String selectedGun = Gun.getSelectedItem().toString();
        String selectedCodename = Codename.getSelectedItem().toString();
        String selectedFree = Free.getSelectedItem().toString();

        seoul.SeoulData(selectedMonth, new seoul.DataCallback() {
            @Override
            public void onDataLoaded(List<EventData> eventList) {
                // Update saved event list
                Getevent.this.eventList = eventList;

                // Initialize LinearLayout
                linearLayout.removeAllViews();
                selectedCount = 0;

                for (EventData eventData : eventList) {
                    if (selectedMonth.equals(selectedMonth)) {
                        // Check if the selected values match the event data
                        if ((selectedGun.equals("전체") || selectedGun.equals(eventData.getGuname())) &&
                                (selectedCodename.equals("전체") || selectedCodename.equals(eventData.getCodename())) &&
                                (selectedFree.equals("전체") || selectedFree.equals(eventData.getIs_free()))) {
                            // Save filter settings
                            saveFilterSettings();
                            Log.d("Getevent", "Filter has been saved");
                        }
                    }
                    Log.d("Getevent", "Data loading complete");
                }
            }
        });
    }
    // 검색 결과를 업데이트하고 ScrollView에 표시
    private void performSearch(String query) {
        // 검색 결과를 필터링하고 결과를 저장할 리스트를 생성
        List<EventData> filteredEventList = new ArrayList<>();
        for (EventData eventData : eventList) {
            // eventData에서 query와 일치하는 항목을 찾아서 filteredEventList에 추가
            if (eventData.getTitle().toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault())) |
                    eventData.getPlace().toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault())) |
                    eventData.getDate().toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))) {
                filteredEventList.add(eventData);
            }
        }

        // ScrollView 초기화
        linearLayout.removeAllViews();
        selectedCount = 0;

        // 검색 결과를 ScrollView에 추가
        for (EventData eventData : filteredEventList) {
            // 새로운 EditText를 생성
            EditText editText = new EditText(Getevent.this);
            StringBuilder editTextContent = new StringBuilder();

            // 데이터를 추가
            editTextContent.append(eventData.getCodename()).append("\n");
            editTextContent.append(eventData.getDate()).append("\n");
            editTextContent.append(eventData.getGuname()).append("\n");
            editTextContent.append(eventData.getIs_free()).append("\n");
            editTextContent.append(eventData.getPlace()).append("\n");
            editTextContent.append(eventData.getTicket()).append("\n");
            editTextContent.append(eventData.getTitle()).append("\n");
            editTextContent.append(eventData.getUse_fee()).append("\n");
            editTextContent.append(eventData.getUse_trgt()).append("\n");

            // 검색어와 일치하는 부분을 찾아서 하이라이트 처리
            String content = editTextContent.toString();
            int index = content.toLowerCase(Locale.getDefault()).indexOf(query.toLowerCase(Locale.getDefault()));
            if (index != -1) {
                SpannableStringBuilder builder = new SpannableStringBuilder(content);
                ForegroundColorSpan highlightSpan = new ForegroundColorSpan(Color.BLUE);
                builder.setSpan(highlightSpan, index, index + query.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                editText.setText(builder);
            } else {
                editText.setText(content);
            }

            editText.setEnabled(true); // EditText를 수정 불가능하게 설정
            editText.setFocusable(false); // EditText의 포커스를 받지 않도록 설정

            // EditText를 클릭했을 때 선택/취소 처리
            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (editTextList.contains(editText)) {
                        editTextList.remove(editText);
                        editText.setBackgroundColor(Color.LTGRAY);
                        selectedCount++;
                    } else {
                        editTextList.add(editText);
                        editText.setBackgroundColor(Color.TRANSPARENT);
                        selectedCount--;
                    }
                    if (selectedCount < 0) {
                        selectedCount = 0;
                    }
                    selectedEditText = editText;

                    // Check if the clicked editText is in the list
                    if (editTextList.contains(selectedEditText)) {
                        // Extract data from the selected editText
                        EventData extractedEventData = extractEventData(selectedEditText);
                    }
                }
            });
            // EditText를 ScrollView에 추가
            linearLayout.addView(editText);
            editTextList.add(editText);
        }
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(ACTION_REFRESH_DATA)) {
                // Handle the broadcast, call the method
                loadEventData();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_REFRESH_DATA));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}