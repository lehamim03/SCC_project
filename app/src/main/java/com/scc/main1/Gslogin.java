package com.scc.main1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Gslogin extends AppCompatActivity {
    private static final String TAG = "Gslogin";
    private static final int RC_SIGN_IN = 9001;

    // 구글 API 클라이언트
    private GoogleSignInClient mGoogleSignInClient;

    // 파이어베이스 인증 객체 생성
    private FirebaseAuth mAuth;

    // 구글 로그인 버튼
    private SignInButton btnGoogleLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gslogin_activity);

        // 파이어베이스 인증 객체 선언
        mAuth = FirebaseAuth.getInstance();

        // Google 로그인을 앱에 통합
        // GoogleSignInOptions 개체를 구성할 때 requestIdToken을 호출
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // 이미 로그인되어 있는지 확인
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // 이미 로그인된 경우, MainActivity로 이동
            startMainActivity();
        } else {
            // 로그인 버튼 이벤트 처리
            btnGoogleLogin = findViewById(R.id.btn_google_sign_in);
            btnGoogleLogin.setOnClickListener(view -> signIn());
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);

            if (acct != null) {
                firebaseAuthWithGoogle(acct.getIdToken());
            }
        } catch (ApiException e) {
            Log.e(TAG, "signInResult: failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Gslogin.this, R.string.success_login, Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        Log.w(TAG, "signInWithCredential: failure", task.getException());
                        Toast.makeText(Gslogin.this, R.string.failed_login, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startMainActivity() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String profileImageUrl = currentUser.getPhotoUrl().toString();
        String userName = currentUser.getDisplayName();
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_email", mAuth.getCurrentUser().getEmail());
        editor.putString("user_name", mAuth.getCurrentUser().getDisplayName());
        editor.apply();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 현재 사용자의 이메일 가져오기
        String userEmail = mAuth.getCurrentUser().getEmail();

        // users 컬렉션에서 현재 사용자의 지메일로 된 문서 가져오기
        DocumentReference userDocRef = db.collection("users").document(userEmail);

        // profilelist 문서를 가져와서 데이터를 업데이트
        userDocRef.collection("profile").document("profilelist")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // 이미 존재하는 데이터를 가져와서 업데이트
                            Map<String, Object> profileData = documentSnapshot.getData();
                            profileData.put("profileImageUrl", profileImageUrl);
                            profileData.put("userName", userName);

                            // 업데이트된 데이터를 다시 저장
                            userDocRef.collection("profile").document("profilelist")
                                    .set(profileData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // 데이터 저장 성공 시 실행되는 코드
                                            Log.d(TAG, "프로필 데이터 저장 성공");

                                            // MainActivity로 이동
                                            Intent intent = new Intent(Gslogin.this, MainActivity.class);
                                            startActivity(intent);
                                            finish(); // Gslogin 액티비티를 종료하여 뒤로 가기 버튼을 누르면 다시 이 액티비티로 돌아오지 않도록 함
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // 데이터 저장 실패 시 실행되는 코드
                                            Log.d(TAG, "프로필 데이터 저장 실패");
                                        }
                                    });
                        } else {
                            // 문서가 존재하지 않으면 새로운 데이터를 생성
                            Map<String, Object> profileData = new HashMap<>();
                            profileData.put("profileImageUrl", profileImageUrl);
                            profileData.put("userName", userName);

                            // 데이터를 저장
                            userDocRef.collection("profile").document("profilelist")
                                    .set(profileData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // 데이터 저장 성공 시 실행되는 코드
                                            Log.d(TAG, "프로필 데이터 저장 성공");

                                            // MainActivity로 이동
                                            Intent intent = new Intent(Gslogin.this, MainActivity.class);
                                            startActivity(intent);
                                            finish(); // Gslogin 액티비티를 종료하여 뒤로 가기 버튼을 누르면 다시 이 액티비티로 돌아오지 않도록 함
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // 데이터 저장 실패 시 실행되는 코드
                                            Log.d(TAG, "프로필 데이터 저장 실패");
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 문서 가져오기 실패 시 실행되는 코드
                        Log.d(TAG, "프로필 데이터 가져오기 실패");
                    }
                });
    }
}
