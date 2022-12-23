package ga.jundbits.dareme.Activities;

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
import android.widget.Spinner;
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
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ga.jundbits.dareme.R;
import github.nisrulz.easydeviceinfo.base.EasyNetworkMod;

public class RegisterActivity extends AppCompatActivity {

    ConstraintLayout noConnectionLayout;

    ConstraintLayout registerConstraintLayout;
    Toolbar registerToolbar;
    EditText registerName, registerUsername, registerEmailAddress, registerPassword, registerConfirmPassword;
    ImageButton registerShowPassword;
    Spinner registerUserTypeSpinner;
    Button registerButton;
    Button registerLoginButton;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    String userType;
    String challengeID = "null";
    String userToken;

    Vibrator vibrator;

    ProgressDialog registerProgressDialog;

    SharedPreferences registerPreferences;
    SharedPreferences.Editor editor;

    EasyNetworkMod easyNetworkMod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        noConnectionLayout = findViewById(R.id.no_connection_layout);

        registerConstraintLayout = findViewById(R.id.register_constraint_layout);
        registerToolbar = findViewById(R.id.register_toolbar);
        registerName = findViewById(R.id.register_name);
        registerUsername = findViewById(R.id.register_username);
        registerEmailAddress = findViewById(R.id.register_email_address);
        registerPassword = findViewById(R.id.register_password);
        registerConfirmPassword = findViewById(R.id.register_confirm_password);
        registerShowPassword = findViewById(R.id.register_show_password);
        registerUserTypeSpinner = findViewById(R.id.register_user_type_spinner);
        registerButton = findViewById(R.id.register_button);
        registerLoginButton = findViewById(R.id.register_login_button);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        userType = getIntent().getStringExtra("user_type");

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        registerProgressDialog = new ProgressDialog(RegisterActivity.this);

        registerPreferences = getSharedPreferences("Register Preferences", MODE_PRIVATE);

        easyNetworkMod = new EasyNetworkMod(this);

        setSupportActionBar(registerToolbar);
        getSupportActionBar().setTitle(getString(R.string.register));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        registerShowPassword.setColorFilter(Color.RED);

        loadInputsData();
        getUserToken();

        if (userType.equals("player")) {
            registerUserTypeSpinner.setSelection(0);
        } else if (userType.equals("watcher")) {
            registerUserTypeSpinner.setSelection(1);
        }

        registerShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (registerPassword.getTransformationMethod() == null) {
                    registerPassword.setTransformationMethod(new PasswordTransformationMethod());
                    registerConfirmPassword.setTransformationMethod(new PasswordTransformationMethod());
                    registerShowPassword.setColorFilter(Color.RED);
                } else {
                    registerPassword.setTransformationMethod(null);
                    registerConfirmPassword.setTransformationMethod(null);
                    registerShowPassword.setColorFilter(Color.GREEN);
                }

            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                closeKeyboard();

                final String name = registerName.getText().toString().trim();
                final String username = registerUsername.getText().toString().toLowerCase().trim();
                final String emailAddress = registerEmailAddress.getText().toString().trim();
                final String password = registerPassword.getText().toString().trim();
                String confirmPassword = registerConfirmPassword.getText().toString().trim();

                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(username) && !TextUtils.isEmpty(emailAddress) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)) {

                    if (username.length() >= 3) {

                        if (!username.equals("player") && !username.equals("watcher")) {

                            if (password.equals(confirmPassword)) {

                                if (easyNetworkMod.isNetworkAvailable()) {

                                    registerProgressDialog.setTitle(getString(R.string.registering));
                                    registerProgressDialog.setMessage(getString(R.string.please_wait_do_not_close_the_app));
                                    registerProgressDialog.setCancelable(false);
                                    registerProgressDialog.setCanceledOnTouchOutside(false);
                                    registerProgressDialog.show();

                                    firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                            .collection("Users").whereEqualTo("username", username)
                                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                    if (queryDocumentSnapshots.isEmpty()) {

                                                        firebaseFirestore.collection("RegisteredEmails").whereEqualTo("email", emailAddress)
                                                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                                        if (queryDocumentSnapshots.isEmpty()) {

                                                                            firebaseAuth.createUserWithEmailAndPassword(emailAddress, password)
                                                                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                                                                            if (task.isSuccessful()) {

                                                                                                FirebaseUser firebaseUser = task.getResult().getUser();
                                                                                                String currentUserID = firebaseUser.getUid();
                                                                                                createUserDatabase(currentUserID, name, username, emailAddress, password);

                                                                                            } else {

                                                                                                registerProgressDialog.dismiss();
                                                                                                showError(task.getException().getMessage());

                                                                                            }

                                                                                        }
                                                                                    });

                                                                        } else {

                                                                            firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                                                                    .collection("Users").whereEqualTo("email", emailAddress)
                                                                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                        @Override
                                                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                                                            if (queryDocumentSnapshots.isEmpty()) {

                                                                                                // TODO: maybe i should add a forgot password feature

                                                                                                checkPasswordBeforeSignIn(name, username, emailAddress, password);

                                                                                            } else {

                                                                                                registerProgressDialog.dismiss();

                                                                                                Snackbar.make(registerConstraintLayout, getString(R.string.email_is_already_registered), Snackbar.LENGTH_SHORT)
                                                                                                        .setAction(getString(R.string.login), new View.OnClickListener() {
                                                                                                            @Override
                                                                                                            public void onClick(View v) {
                                                                                                                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                                                                                loginIntent.putExtra("user_type", userType);
                                                                                                                startActivity(loginIntent);
                                                                                                                finish();
                                                                                                            }
                                                                                                        }).show();

                                                                                            }

                                                                                        }
                                                                                    });


                                                                        }

                                                                    }
                                                                });

                                                    } else {
                                                        registerProgressDialog.dismiss();
                                                        showError(getString(R.string.username_is_not_available));
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
                                showError(getString(R.string.password_and_confirm_password_dont_match));
                            }

                        } else {
                            showError(getString(R.string.username_not_available));
                        }

                    } else {
                        showError(getString(R.string.username_too_short));
                    }

                } else {

                    if (TextUtils.isEmpty(name)) {
                        registerName.requestFocus();
                        showError(getString(R.string.name_cannot_be_empty));
                    } else if (TextUtils.isEmpty(username)) {
                        registerUsername.requestFocus();
                        showError(getString(R.string.username_cannot_be_empty));
                    } else if (TextUtils.isEmpty(emailAddress)) {
                        registerEmailAddress.requestFocus();
                        showError(getString(R.string.email_address_cannot_be_empty));
                    } else if (TextUtils.isEmpty(password)) {
                        registerPassword.requestFocus();
                        showError(getString(R.string.password_cannot_be_empty));
                    } else if (TextUtils.isEmpty(confirmPassword)) {
                        registerConfirmPassword.requestFocus();
                        showError(getString(R.string.confirm_password_cannot_be_empty));
                    }

                }

            }
        });

        registerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                loginIntent.putExtra("user_type", userType);
                startActivity(loginIntent);
                finish();

            }
        });

    }

    private void checkPasswordBeforeSignIn(final String name, final String username, final String emailAddress, final String password) {

        firebaseFirestore.collection("RegisteredEmails").document(emailAddress)
                .collection("Passwords").document("CurrentPassword")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {

                        final String registeredPassword = snapshot.getString("password");

                        if (password.equals(registeredPassword)) {

                            signIn(name, username, emailAddress, password);

                        } else {

                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                            builder.setTitle(getString(R.string.different_password_found));
                            builder.setMessage(getString(R.string.your_email_is_already_registered_with_a_different_password_would_you_like_to_use_your_old_password_or_change_it_with_the_new_one));
                            builder.setPositiveButton(getString(R.string.login), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    signIn(name, username, emailAddress, registeredPassword);
                                }
                            });
                            builder.setNegativeButton(getString(R.string.change_it_and_login), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    signIn(name, username, emailAddress, password);
                                }
                            });
                            builder.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    registerProgressDialog.dismiss();
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();

                        }

                    }
                });

    }

    private void signIn(final String name, final String username, final String emailAddress, final String password) {

        firebaseAuth.signInWithEmailAndPassword(emailAddress, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    FirebaseUser firebaseUser = task.getResult().getUser();
                    String currentUserID = firebaseUser.getUid();
                    createUserDatabase(currentUserID, name, username, emailAddress, password);

                } else {

                    registerProgressDialog.dismiss();
                    showError(task.getException().getMessage());

                }

            }
        });

    }

    private void createUserDatabase(String currentUserID, String name, String username, final String emailAddress, String password) {

        final DocumentReference currentUserDocument = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);

        String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceModel = Build.MODEL;
        String deviceBrand = Build.BRAND;

        Timestamp timestamp = Timestamp.now();

        long timestampSeconds = timestamp.getSeconds();
        long timestampNanoSeconds = timestamp.getNanoseconds();
        long timestampSecondsToMillis = TimeUnit.SECONDS.toMillis(timestampSeconds);
        long timestampNanoSecondsToMillis = TimeUnit.NANOSECONDS.toMillis(timestampNanoSeconds);
        final long timestampTotalMillis = timestampSecondsToMillis + timestampNanoSecondsToMillis;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy, hh:mm:ss a, zz");
        final String dateTime = dateFormat.format(calendar.getTime());

        Map<String, Object> emailAppsMap = new HashMap<>();
        emailAppsMap.put("name", getString(R.string.app_name_no_spaces));
        emailAppsMap.put("date_time", dateTime);

        final Map<String, Object> emailMap = new HashMap<>();
        emailMap.put("email", emailAddress);
        emailMap.put("id", currentUserID);
        emailMap.put("apps", FieldValue.arrayUnion(emailAppsMap));

        final Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("password", password);

        Map<Object, Object> dateTimeMap = new HashMap<>();
        dateTimeMap.put(dateTime, passwordMap);

        Map<String, Object> appMap = new HashMap<>();
        appMap.put(getString(R.string.app_name_no_spaces), dateTimeMap);

        final Map<String, Object> appsMap = new HashMap<>();
        appsMap.put("apps", appMap);

        Map<String, Object> lastLoginMap = new HashMap<>();
        lastLoginMap.put("date_time", timestampTotalMillis);
        lastLoginMap.put("device_id", deviceID);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", currentUserID);
        userMap.put("name", name);
        userMap.put("username", username);
        userMap.put("email", emailAddress);
        userMap.put("password", password);
        userMap.put("type", registerUserTypeSpinner.getSelectedItem().toString().toLowerCase());
        userMap.put("image", "default");
        userMap.put("description", null);
        userMap.put("last_login", lastLoginMap);
        userMap.put("can_open_app", true);
        userMap.put("message", null);
        userMap.put("message_closed", true);
        userMap.put("token_id", userToken);

        final Map<String, Object> loginHistoryMap = new HashMap<>();
        loginHistoryMap.put("device_id", deviceID);
        loginHistoryMap.put("device_model", deviceModel);
        loginHistoryMap.put("device_brand", deviceBrand);
        loginHistoryMap.put("date_time", dateTime);
        loginHistoryMap.put("date_time_millis", timestampTotalMillis);
        loginHistoryMap.put("timestamp", FieldValue.serverTimestamp());

        final Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("brand", deviceBrand);
        sessionMap.put("model", deviceModel);
        sessionMap.put("device_id", deviceID);
        sessionMap.put("date_time", dateTime);
        sessionMap.put("timestamp", FieldValue.serverTimestamp());
        sessionMap.put("date_time_millis", timestampTotalMillis);
        sessionMap.put("logged_in", true);

        currentUserDocument.set(userMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    firebaseFirestore.collection("RegisteredEmails").document(emailAddress).set(emailMap, SetOptions.merge());
                    firebaseFirestore.collection("RegisteredEmails").document(emailAddress).collection("Passwords").document("CurrentPassword").set(passwordMap);
                    firebaseFirestore.collection("RegisteredEmails").document(emailAddress).collection("Passwords").document("AllPasswords").set(appsMap);

                    currentUserDocument.collection("LogInHistory").document(String.valueOf(timestampTotalMillis)).set(loginHistoryMap);
                    currentUserDocument.collection("Sessions").document(String.valueOf(timestampTotalMillis)).set(sessionMap);

                    editor = registerPreferences.edit();
                    editor.clear();
                    editor.apply();

                    registerProgressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, getString(R.string.successfully_registered), Toast.LENGTH_SHORT).show();

                    Bundle bundle = new Bundle();
                    bundle.putString("user_type", registerUserTypeSpinner.getSelectedItem().toString().toLowerCase());
                    bundle.putString("challenge_id", challengeID);

                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                    mainIntent.putExtras(bundle);
                    startActivity(mainIntent);
                    finish();

                } else {

                    registerProgressDialog.dismiss();
                    showError(task.getException().getMessage());

                }

            }
        });

    }

    private void showError(String errorMessage) {

        vibrator.vibrate(500);
        Snackbar.make(registerConstraintLayout, errorMessage, Snackbar.LENGTH_SHORT).show();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

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
        closeKeyboard();
        saveInputsData();
        super.finish();
    }

    private void closeKeyboard() {

        View closeKeyboardView = RegisterActivity.this.getCurrentFocus();
        if (closeKeyboardView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(closeKeyboardView.getWindowToken(), 0);
        }

    }

    private void saveInputsData() {

        editor = registerPreferences.edit();

        String name = registerName.getText().toString();
        String username = registerUsername.getText().toString();
        String email = registerEmailAddress.getText().toString();
        String password = registerPassword.getText().toString();
        String confirmPassword = registerConfirmPassword.getText().toString();

        editor.putString("name", name);
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putString("confirm password", confirmPassword);

        editor.apply();

    }

    private void loadInputsData() {

        String name = registerPreferences.getString("name", "");
        String username = registerPreferences.getString("username", "");
        String email = registerPreferences.getString("email", "");
        String password = registerPreferences.getString("password", "");
        String confirmPassword = registerPreferences.getString("confirm password", "");

        registerName.setText(name);
        registerUsername.setText(username);
        registerEmailAddress.setText(email);
        registerPassword.setText(password);
        registerConfirmPassword.setText(confirmPassword);

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
