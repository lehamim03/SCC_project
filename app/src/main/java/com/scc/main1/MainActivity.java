package com.scc.main1;

import android.annotation.SuppressLint;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import android.view.ViewGroup;
import com.scc.main1.local.EventData;
import com.scc.main1.local.Friend;
import com.scc.main1.local.MyEvent;
import com.scc.main1.util.ColorUtils;
import com.scc.main1.util.DayDecorator;

public class MainActivity extends AppCompatActivity {
    private List<EditText> eventEditTextList = new ArrayList<>();
    private List<EditText> friendEditTextList = new ArrayList<>();
    private final String TAG = this.getClass().getSimpleName();
    private MaterialCalendarView calendarView;
    public LinearLayout eventContainer, friendContainer;
    public Button friend_btn, get2_Btn, add_btn, del_btn, add_friend, del_friend, share_event;
    public TextView diaryTextView, textView2, textView3;
    private EditText selectedEditText;
    public FrameLayout layout_friend;
    private Button X_btn;
    private EditText myaccount;
    private EditText eventEditText;
    private ImageView proimg;
    private FirebaseFirestore db;
    private Switch debugSwitch;
    private CollectionReference userEventsRef;
    private AtomicInteger uniqueIdCounter = new AtomicInteger();

    private int debug = 0; // 디버그 모드 상태를 저장하는 변수

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (debug == 1)
            Toast.makeText(getApplicationContext(), "onCreate", Toast.LENGTH_SHORT).show(); //디버그 모드 토스트 메시지
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        debugSwitch = findViewById(R.id.debugSwitch);
        calendarView = findViewById(R.id.calendarview);
        diaryTextView = findViewById(R.id.diaryTextView);
        get2_Btn = findViewById(R.id.get2_Btn);
        friend_btn = findViewById(R.id.friend_Btn);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        layout_friend = findViewById(R.id.friend);
        X_btn = findViewById(R.id.X);
        myaccount = findViewById(R.id.myaccount);
        proimg = findViewById(R.id.proimg1);
        add_btn = findViewById(R.id.add_Btn);
        del_btn = findViewById(R.id.del_Btn);
        add_friend = findViewById(R.id.add_friend);
        del_friend = findViewById(R.id.del_friend);
        share_event = findViewById(R.id.share_event);
        eventContainer = findViewById(R.id.eventContainer);
        friendContainer = findViewById(R.id.friendContainer);
        eventEditTextList = new ArrayList<>();
        friendEditTextList = new ArrayList<>();

        layout_friend.setVisibility(View.INVISIBLE);

        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String photoUrl = user.getPhotoUrl().toString();
            Glide.with(this)
                    .load(photoUrl)
                    .into(proimg);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("user_email", "");
        String userName = sharedPreferences.getString("user_name", "");

        userEventsRef = db.collection("users").document(userEmail).collection("events");

        myaccount.append(userName + "\n"); // 구글 계정 정보 한 줄 추가
        myaccount.append(userEmail);

        myaccount.setEnabled(true); // EditText를 수정 불가능하게 설정
        myaccount.setFocusable(false); // EditText의 포커스를 받지 않도록 설정

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
                    add_btn.setVisibility(View.VISIBLE);
                    del_btn.setVisibility(View.INVISIBLE);
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

        debugSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // isChecked 값에 따라 디버그 모드를 활성화 또는 비활성화
                if (isChecked) {
                    enableDebugMode();
                } else {
                    disableDebugMode();
                }
            }
        });

        X_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_friend.setVisibility(View.INVISIBLE);
            }
        });

        share_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (friendEditTextList.isEmpty()) {
                    return;
                }

                // 한 번에 하나의 친구만 선택될 수 있다고 가정
                EditText selectedFriendEditText = friendEditTextList.get(0);
                String friendInfo = selectedFriendEditText.getText().toString().trim();

                // friendInfo를 줄 단위로 분할
                String[] lines = friendInfo.split("\n");

                // 이메일이 두 번째 줄에 있을 것으로 가정
                if (lines.length >= 2) {
                    String friendName = lines[0].trim();
                    String friendEmail = lines[1].trim();

                    // ShareActivity를 시작하기 위한 인텐트 생성
                    Intent shareIntent = new Intent(MainActivity.this, ShareActivity.class);

                    // ShareActivity로 친구의 이메일을 전달
                    shareIntent.putExtra("FRIEND_EMAIL", friendEmail);
                    shareIntent.putExtra("FRIEND_NAME", friendName);

                    // ShareActivity 시작
                    startActivity(shareIntent);
                }
            }
        });

        add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // AlertDialog를 이용하여 팝업 창 생성
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("친구 추가");

                // 팝업 창에 EditText 추가
                final EditText friendEditText = new EditText(MainActivity.this);
                friendEditText.setHint("상대방의 이메일을 입력하세요.");
                builder.setView(friendEditText);

                // 저장 버튼 추가
                builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String friendEmail = friendEditText.getText().toString().trim();
                        if (!friendEmail.isEmpty()) {
                            addFriendToFriendList(friendEmail);
                        } else {
                            Toast.makeText(MainActivity.this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // 취소 버튼 추가
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 취소 버튼을 눌렀을 때의 동작 구현
                        dialogInterface.dismiss(); // 팝업 창 닫기
                    }
                });

                // AlertDialog 보이기
                builder.show();
            }
        });

        del_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 친구EditText가 선택되었는지 확인
                if (friendEditTextList.isEmpty()) {
                    return;
                }

                // 가정: 한 번에 하나의 친구만 선택될 수 있다고 가정합니다.
                EditText selectedFriendEditText = friendEditTextList.get(0);

                // 선택된 EditText에서 친구 정보를 추출합니다.
                String friendInfo = selectedFriendEditText.getText().toString().trim();

                // friendInfo를 줄 단위로 분할합니다.
                String[] lines = friendInfo.split("\n");

                // 이메일이 두 번째 줄에 있을 것으로 가정합니다.
                if (lines.length >= 2) {
                    String friendEmail = lines[1].trim(); // 두 번째 줄에서 이메일 추출

                    // Firestore에서 친구 정보 삭제
                    deleteFriendFromFirestore(friendEmail);

                    // UI에서 선택된 친구EditText 제거
                    friendContainer.removeView(selectedFriendEditText);

                    // 리스트에서 선택된 친구EditText 제거
                    friendEditTextList.remove(selectedFriendEditText);

                    // UI 새로고침
                    friendContainer.invalidate();
                    friendContainer.requestLayout();
                }
            }
        });

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(MainActivity.this, Addevent.class);
                startActivity(intent2);
                finish();
            }
        });

        del_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Does nothing if no EditText is selected
                if (eventEditTextList.isEmpty()) {
                    return;
                }

                // Assumption: Only one EditText can be selected at a time
                EditText selectedEditText = eventEditTextList.get(0);

                // Modified part: Get the schedule information of the selected EditText and delete it from Firestore
                String eventInfo = selectedEditText.getText().toString();

                // Log the uniqueId before calling deleteEventFromFirestore
                int uniqueId = selectedEditText.getId();
                Log.d(TAG, "Deleting EditText with uniqueId: " + uniqueId);
                deleteEventFromFirestore(eventInfo, uniqueId);

                // Remove the selected EditText from the screen
                removeSelectedEventEditText(uniqueId);

                // Modified part: Change button state after deletion
                add_btn.setVisibility(View.VISIBLE);
                del_btn.setVisibility(View.INVISIBLE);
            }
        });


        get2_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (debug == 1)
                    Toast.makeText(getApplicationContext(), "get2_Btn", Toast.LENGTH_SHORT).show(); //디버그 모드 토스트 메시지
                Intent intent1 = new Intent(MainActivity.this, Getevent.class);
                startActivity(intent1);
                if (debug == 1)
                    Toast.makeText(getApplicationContext(), "Getevent 인스턴스 호출", Toast.LENGTH_SHORT).show(); //디버그 모드 토스트 메시지
            }
        });

        friend_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_friend.setVisibility(View.VISIBLE);
                loadFriendsFromFirestore();
            }
        });

    }
    private void enableDebugMode() {
        // 디버그 모드를 활성화하는 작업을 수행
        debug = 1; // 디버그 모드 활성화
        Toast.makeText(getApplicationContext(), "디버그 모드가 활성화되었습니다.", Toast.LENGTH_SHORT).show();
    }
    private void disableDebugMode() {
        // 디버그 모드를 비활성화하는 작업을 수행
        debug = 0; // 디버그 모드 비활성화
        Toast.makeText(getApplicationContext(), "디버그 모드가 비활성화되었습니다.", Toast.LENGTH_SHORT).show();
    }
    private void loadEventsForDate(LocalDate selectedDate) {
        // 전체 데이터 가져오기
        userEventsRef.get().addOnCompleteListener(task -> {
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
    private int generateUniqueId() {
        // 적절한 고유 ID 생성 로직 추가
        // 여기서는 간단히 현재 시간을 사용합니다.
        return uniqueIdCounter.incrementAndGet();
    }
    private void updateUIWithEvents(List<MyEvent> eventsForDate) {
        for (MyEvent myEvent : eventsForDate) {
            Log.d(TAG, "Updating UI with event: " + myEvent.getEventTitle());
            Log.d(TAG, "Updating UI with event: " + myEvent.getEventPlace());
            // 새로운 EditText를 동적으로 생성
            eventEditText = new EditText(MainActivity.this);
            int uniqueId = generateUniqueId();
            eventEditText.setId(uniqueId);
            Log.d(TAG, "Setting EditText ID: " + eventEditText.getId()); // Add this log

            String shareStatus = myEvent.getShare() == 1 ? "공유: OFF" : "공유: ON";
            eventEditText.setText("일정 제목: " + myEvent.getEventTitle() +
                    "\n장소: " + myEvent.getEventPlace() + "\n" + shareStatus +
                    "\n\n");

            // 필요에 따라 스타일이나 속성을 설정할 수 있습니다.

            // 생성된 EditText를 LinearLayout에 추가
            eventContainer.addView(eventEditText);
            eventEditText.setEnabled(true); // EditText를 수정 불가능하게 설정
            eventEditText.setFocusable(false); // EditText의 포커스를 받지 않도록 설정

            // eventEditText를 리스트에 추가
            eventEditTextList.add(eventEditText);

            eventEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleSelection((EditText) view);
                }
            });
        }
    }
    private void toggleSelection(EditText selectedEditText) {
        // Deselect other EditTexts
        for (EditText editText : eventEditTextList) {
            editText.setSelected(false);
        }

        // Switch the selection state of the clicked EditText
        selectedEditText.setSelected(!selectedEditText.isSelected());

        // Debug statement
        Log.d(TAG, "After toggle - EditText ID: " + selectedEditText.getId() + ", isSelected: " + selectedEditText.isSelected());

        // Update button visibility based on selection
        if (selectedEditText.isSelected()) {
            add_btn.setVisibility(View.INVISIBLE);
            del_btn.setVisibility(View.VISIBLE);
        } else {
            add_btn.setVisibility(View.VISIBLE);
            del_btn.setVisibility(View.INVISIBLE);
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

    private void deleteEventFromFirestore(String eventInfo, int uniqueId) {
        // Extract schedule information from eventInfo and delete it from Firestore
        // Example: Extract title and location from information saved in the format "일정 제목: {Title}\n장소: {Location}\n\n"
        String[] parts = eventInfo.split("\n");
        String eventTitle = parts[0].replace("일정 제목: ", "");
        String eventPlace = parts[1].replace("장소: ", "");

        // Delete that schedule from Firestore using the extracted information
        Query query = userEventsRef.whereEqualTo("eventTitle", eventTitle)
                .whereEqualTo("eventPlace", eventPlace);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    document.getReference().delete();
                }
                // Pass the unique ID for the deleted event to remove it from the UI
                removeSelectedEventEditText(uniqueId);
            } else {
                Toast.makeText(getApplicationContext(), "Error deleting event: " + task.getException(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error deleting event", task.getException());
            }
        });
    }

    private void removeSelectedEventEditText(int uniqueId) {
        // Find and remove the EditText with the specified unique ID from the list and UI
        Iterator<EditText> iterator = eventEditTextList.iterator();
        while (iterator.hasNext()) {
            EditText editText = iterator.next();
            if (editText.getId() == uniqueId) {
                // Remove selected EditText from eventContainer
                LinearLayout eventContainer = findViewById(R.id.eventContainer);
                eventContainer.removeView(editText);
                // Remove selected EditText from the list
                iterator.remove();
                break; // No need to continue searching
            }
        }
    }

    private void addFriendToFriendList(String friendEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String currentUserEmail = currentUser.getEmail();
            Log.d(TAG, "friendEmail: " + friendEmail);
            Log.d(TAG, "currentUserEmail: " + currentUserEmail);

            // Retrieve documents using friendEmail
            CollectionReference friendDocRef = db.collection("users").document(friendEmail).collection("events");
            friendDocRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "데이터 가져옴");
                            if (document.exists()) {
                                Log.d(TAG, "데이터 가져오기 성공");

                                // If the document exists using friendEmail, add the friend
                                DocumentReference currentUserDocRef = db.collection("users").document(currentUserEmail);
                                DocumentReference friendDocRef = currentUserDocRef.collection("friends").document(friendEmail);

                                // Use the update method to add new fields or update existing fields
                                Map<String, Object> friendData = new HashMap<>();
                                friendData.put("friendName", friendEmail);

                                friendDocRef.set(friendData, SetOptions.merge())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Add friend to container
                                                addFriendToContainer(friendEmail);
                                                Toast.makeText(MainActivity.this, "친구 추가 성공", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, "친구 추가 실패", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Log.d(TAG, "Data retrieval failed");
                                // If the document does not exist using friendEmail, display a message indicating that the user is not valid
                                Toast.makeText(MainActivity.this, "유호한 사용자가 아닙니다", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        // Handle errors while retrieving the documents
                        Log.e(TAG, "불러오는데 실패하였습니다: ", task.getException());
                    }
                }
            });
        }
    }

    private void addFriendToContainer(String friendEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference friendDocRef = db.collection("users").document(friendEmail).collection("profile").document("profilelist");
        friendDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 사용자 정보 가져오기 성공
                        String friendName = document.getString("userName");
                        String friendProfilePicUrl = document.getString("profileImageUrl");
                        Log.d(TAG, "사용자 정보 가져옴");

                        // 동적으로 생성된 EditText를 friendContainer에 추가
                        // friendEditText를 생성
                        LinearLayout friendLayout = new LinearLayout(MainActivity.this);
                        friendLayout.setOrientation(LinearLayout.VERTICAL);
                        Log.d(TAG, "레이아웃 추가 완료");
                        EditText friendEditText = new EditText(MainActivity.this);
                        friendEditText.setText(friendName + "\n" + friendEmail);

                        // LinearLayout의 레이아웃 파라미터 설정
                        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        friendLayout.setLayoutParams(layoutParams);

                        // EditText의 레이아웃 파라미터 설정
                        ViewGroup.LayoutParams editTextParams = new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        friendEditText.setLayoutParams(editTextParams);

                        friendLayout.addView(friendEditText);
                        Log.d(TAG, "텍스트 추가 완료");

                        if (friendProfilePicUrl != null && !friendProfilePicUrl.isEmpty()) {
                            ImageView profileImageView = new ImageView(MainActivity.this);
                            // 이미지를 로드하여 profileImageView에 설정하는 로직을 추가하세요 (Picasso, Glide 등 사용)
                            // 예: Picasso.get().load(friendProfilePicUrl).into(profileImageView);
                            // 이 부분을 사용하는 라이브러리에 맞게 수정하세요

                            // friendLayout에 ImageView 추가
                            friendLayout.addView(profileImageView);
                            Log.d(TAG, "이미지 추가 완료");
                        }
                        friendEditText.setEnabled(true); // EditText를 수정 불가능하게 설정
                        friendEditText.setFocusable(false); // EditText의 포커스를 받지 않도록 설정
                        friendLayout.setVisibility(View.VISIBLE);
                        friendContainer.setVisibility(View.VISIBLE);
                        friendContainer.addView(friendLayout);
                        Log.d(TAG, "프렌드 컨테이너 추가 완료");

                        // UI 갱신을 위해 추가
                        friendContainer.invalidate();
                        friendContainer.requestLayout();
                        friendEditTextList.add(friendEditText); //수정 해볼수있을지도
                    } else {
                        // 사용자 정보가 없는 경우 처리
                        Toast.makeText(MainActivity.this, "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 정보를 가져오는 중에 오류가 발생한 경우 처리
                    Toast.makeText(MainActivity.this, "사용자 정보를 가져오는 중 오류가 발생했습니다: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void loadFriendsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String currentUserEmail = currentUser.getEmail();
            CollectionReference friendsCollectionRef = db.collection("users").document(currentUserEmail).collection("friends");

            // Clear existing friends from UI and list
            friendContainer.removeAllViews();
            friendEditTextList.clear();

            friendsCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // 각 문서의 ID를 가져옴
                            String friendDocumentId = document.getId();

                            // ID를 사용하여 실제 친구의 이메일을 참조
                            DocumentReference friendDocRef = db.collection("users").document(currentUserEmail).collection("friends").document(friendDocumentId);
                            friendDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot friendDocument = task.getResult();
                                        if (friendDocument.exists()) {
                                            String friendEmail = friendDocument.getString("friendName");
                                            if (friendEmail != null && !friendEmail.isEmpty()) {
                                                addFriendToContainer(friendEmail);
                                            } else {
                                                Log.e(TAG, "Friend email is null or empty");
                                            }
                                        } else {
                                            Log.e(TAG, "Friend document does not exist");
                                        }
                                    } else {
                                        Log.e(TAG, "Error loading friend document", task.getException());
                                    }
                                }
                            });
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "친구 목록을 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error loading friends", task.getException());
                    }
                }
            });
        }
    }

    private void deleteFriendFromFirestore(String friendEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Reference to the friend document
            DocumentReference friendDocRef = db.collection("users")
                    .document(currentUser.getEmail())
                    .collection("friends")
                    .document(friendEmail);

            // Delete the friend document from Firestore
            friendDocRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendContainer.invalidate();
                            friendContainer.requestLayout();
                            // Deletion successful
                            Toast.makeText(MainActivity.this, "친구가 성공적으로 삭제되었습니다", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle deletion failure
                            Toast.makeText(MainActivity.this, "친구 삭제에 실패하였습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
