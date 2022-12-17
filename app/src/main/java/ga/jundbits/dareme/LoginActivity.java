package ga.jundbits.dareme;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import github.nisrulz.easydeviceinfo.base.EasyNetworkMod;

public class LoginActivity extends AppCompatActivity {

    ConstraintLayout noConnectionLayout;

    ConstraintLayout loginConstraintLayout;
    Toolbar loginToolbar;
    EditText loginEmailAddress, loginPassword;
    ImageButton loginShowPassword;
    Button loginButton, loginRegisterButton;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    String userType;
    String challengeID = "null";
    String userToken;

    Vibrator vibrator;

    ProgressDialog loginProgressDialog;

    SharedPreferences loginPreferences;
    SharedPreferences.Editor editor;

    EasyNetworkMod easyNetworkMod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        noConnectionLayout = findViewById(R.id.no_connection_layout);

        loginConstraintLayout = findViewById(R.id.login_constraint_layout);
        loginToolbar = findViewById(R.id.login_toolbar);
        loginEmailAddress = findViewById(R.id.login_email_address);
        loginPassword = findViewById(R.id.login_password);
        loginShowPassword = findViewById(R.id.login_show_password);
        loginButton = findViewById(R.id.login_button);
        loginRegisterButton = findViewById(R.id.login_register_button);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        userType = getIntent().getStringExtra("user_type");

        loginProgressDialog = new ProgressDialog(this);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        easyNetworkMod = new EasyNetworkMod(this);

        setSupportActionBar(loginToolbar);
        getSupportActionBar().setTitle(getString(R.string.login));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loginShowPassword.setColorFilter(Color.RED);

        loginPreferences = getSharedPreferences("Login Preferences", MODE_PRIVATE);

        loadInputsData();
        getUserToken();

        loginShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (loginPassword.getTransformationMethod() == null) {
                    loginPassword.setTransformationMethod(new PasswordTransformationMethod());
                    loginShowPassword.setColorFilter(Color.RED);
                } else {
                    loginPassword.setTransformationMethod(null);
                    loginShowPassword.setColorFilter(Color.GREEN);
                }

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                closeKeyboard();

                final String emailAddress = loginEmailAddress.getText().toString().trim();
                final String password = loginPassword.getText().toString().trim();

                if (!TextUtils.isEmpty(emailAddress) && !TextUtils.isEmpty(password)) {

                    if (easyNetworkMod.isNetworkAvailable()) {

                        loginProgressDialog.setTitle(getString(R.string.logging_in));
                        loginProgressDialog.setMessage(getString(R.string.please_wait_do_not_close_the_app));
                        loginProgressDialog.setCanceledOnTouchOutside(false);
                        loginProgressDialog.setCancelable(false);
                        loginProgressDialog.show();

                        firebaseFirestore.collection("RegisteredEmails").whereEqualTo("email", emailAddress)
                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                        if (queryDocumentSnapshots.isEmpty()) {

                                            loginProgressDialog.dismiss();

                                            Snackbar.make(loginConstraintLayout, getString(R.string.email_is_not_registered), Snackbar.LENGTH_SHORT)
                                                    .setAction(getString(R.string.register), new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                                                            registerIntent.putExtra("user_type", userType);
                                                            startActivity(registerIntent);
                                                            finish();
                                                        }
                                                    }).show();

                                        } else {

                                            firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                                    .collection("Users").whereEqualTo("email", emailAddress)
                                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                            if (queryDocumentSnapshots.isEmpty()) {

                                                                loginProgressDialog.dismiss();

                                                                Snackbar.make(loginConstraintLayout, getString(R.string.app_is_not_registered), Snackbar.LENGTH_SHORT)
                                                                        .setAction(getString(R.string.register), new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {
                                                                                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                                                                                registerIntent.putExtra("user_type", userType);
                                                                                startActivity(registerIntent);
                                                                                finish();
                                                                            }
                                                                        }).show();

                                                            } else {

                                                                checkPasswordBeforeSignIn(emailAddress, password);

                                                            }

                                                        }
                                                    });

                                        }

                                    }
                                });

                    } else {

                        noConnectionLayout.setVisibility(View.VISIBLE);

                        noConnectionLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (easyNetworkMod.isNetworkAvailable()) {
                                    noConnectionLayout.setVisibility(View.GONE);
                                }
                            }
                        });

                    }

                } else {

                    if (TextUtils.isEmpty(emailAddress)) {
                        loginEmailAddress.requestFocus();
                        showError(getString(R.string.email_address_cannot_be_empty));
                    } else if (TextUtils.isEmpty(password)) {
                        loginPassword.requestFocus();
                        showError(getString(R.string.password_cannot_be_empty));
                    }

                }

            }

        });

        loginRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                registerIntent.putExtra("user_type", userType);
                startActivity(registerIntent);
                finish();

            }
        });

    }

    private void showError(String errorMessage) {

        vibrator.vibrate(500);
        Snackbar.make(loginConstraintLayout, errorMessage, Snackbar.LENGTH_SHORT).show();

    }

    private void closeKeyboard() {

        View closeKeyboardView = LoginActivity.this.getCurrentFocus();
        if (closeKeyboardView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(closeKeyboardView.getWindowToken(), 0);
        }

    }

    private void checkPasswordBeforeSignIn(final String emailAddress, final String password) {

        firebaseFirestore.collection("RegisteredEmails").document(emailAddress)
                .collection("Passwords").document("CurrentPassword")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {

                        final String registeredPassword = snapshot.getString("password");

                        if (password.equals(registeredPassword)) {

                            signIn(emailAddress, password);

                        } else {

                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(LoginActivity.this);
                            builder.setTitle(getString(R.string.different_password_found));
                            builder.setMessage(getString(R.string.your_email_is_already_registered_with_a_different_password_would_you_like_to_use_your_old_password_or_change_it_with_the_new_one));
                            builder.setPositiveButton(getString(R.string.login), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    signIn(emailAddress, registeredPassword);
                                }
                            });
                            builder.setNegativeButton(getString(R.string.change_it_and_login), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    signIn(emailAddress, password);
                                }
                            });
                            builder.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    loginProgressDialog.dismiss();
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();

                        }

                    }
                });

    }

    private void signIn(final String email, final String password) {

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    FirebaseUser firebaseUser = task.getResult().getUser();
                    String currentcurrentUserID = firebaseUser.getUid();
                    updateUserDatabase(currentcurrentUserID);

                } else {

                    loginProgressDialog.dismiss();

                    String errorMessage = task.getException().getMessage();
                    vibrator.vibrate(500);
                    Snackbar.make(loginConstraintLayout, errorMessage, Snackbar.LENGTH_LONG).show();

                }

            }
        });

    }

    private void updateUserDatabase(final String currentUserID) {

        final DocumentReference currentUserDocument = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);

        final String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceModel = Build.MODEL;
        final String deviceBrand = Build.BRAND;

        Timestamp timestamp = Timestamp.now();

        long timestampSeconds = timestamp.getSeconds();
        long timestampNanoSeconds = timestamp.getNanoseconds();
        long timestampSecondsToMillis = TimeUnit.SECONDS.toMillis(timestampSeconds);
        long timestampNanoSecondsToMillis = TimeUnit.NANOSECONDS.toMillis(timestampNanoSeconds);
        final long timestampTotalMillis = timestampSecondsToMillis + timestampNanoSecondsToMillis;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy, hh:mm:ss a, zz");
        final String dateTime = dateFormat.format(calendar.getTime());

        Map<String, Object> lastLoginMap = new HashMap<>();
        lastLoginMap.put("date_time", timestampTotalMillis);
        lastLoginMap.put("device_id", deviceID);

        final Map<String, Object> userMap = new HashMap<>();
        userMap.put("last_login", lastLoginMap);
        userMap.put("token_id", userToken);

        final Map<String, Object> loginHistoryMap = new HashMap<>();
        loginHistoryMap.put("date_time", dateTime);
        loginHistoryMap.put("timestamp", FieldValue.serverTimestamp());
        loginHistoryMap.put("date_time_millis", timestampTotalMillis);
        loginHistoryMap.put("device_id", deviceID);
        loginHistoryMap.put("model", deviceModel);
        loginHistoryMap.put("brand", deviceBrand);

        final Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("brand", deviceBrand);
        sessionMap.put("model", deviceModel);
        sessionMap.put("device_id", deviceID);
        sessionMap.put("date_time", dateTime);
        sessionMap.put("timestamp", FieldValue.serverTimestamp());
        sessionMap.put("date_time_millis", timestampTotalMillis);
        sessionMap.put("logged_in", true);

        currentUserDocument.update(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    currentUserDocument.collection("LogInHistory").document(String.valueOf(timestampTotalMillis)).set(loginHistoryMap);

                    currentUserDocument.collection("Sessions").whereEqualTo("device_id", deviceID)
                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    if (!queryDocumentSnapshots.isEmpty()) {

                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                            currentUserDocument.collection("Sessions").document(documentSnapshot.getId()).update(sessionMap);
                                        }

                                    } else {
                                        currentUserDocument.collection("Sessions").document(String.valueOf(timestampTotalMillis)).set(sessionMap);
                                    }

                                }
                            });

                    loginProgressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, getString(R.string.successfully_logged_in), Toast.LENGTH_SHORT).show();

                    currentUserDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {

                            String userType = snapshot.getString("type");

                            Bundle bundle = new Bundle();
                            bundle.putString("user_type", userType);
                            bundle.putString("challenge_id", challengeID);

                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.putExtras(bundle);
                            startActivity(mainIntent);
                            finish();

                        }
                    });

                } else {

                    loginProgressDialog.dismiss();
                    showError(task.getException().getMessage());

                }

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            default:
                return false;

            case android.R.id.home:
                finish();
                return true;

        }

    }

    @Override
    public void finish() {
        saveInputsData();
        closeKeyboard();
        super.finish();
    }

    private void saveInputsData() {

        editor = loginPreferences.edit();

        String email = loginEmailAddress.getText().toString();
        String password = loginPassword.getText().toString();

        editor.putString("email", email);
        editor.putString("password", password);

        editor.apply();

    }

    private void loadInputsData() {

        String email = loginPreferences.getString("email", "");
        String password = loginPreferences.getString("password", "");

        loginEmailAddress.setText(email);
        loginPassword.setText(password);

    }

    public String getUserToken() {

        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(this, new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {

                        userToken = token;

                    }
                });

        return userToken;

    }

}
