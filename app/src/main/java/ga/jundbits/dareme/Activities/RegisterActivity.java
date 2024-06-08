package ga.jundbits.dareme.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ga.jundbits.dareme.Models.User;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.FirebaseHelper;
import ga.jundbits.dareme.Utils.HelperMethods;

public class RegisterActivity extends AppCompatActivity {

    private ConstraintLayout registerConstraintLayout;
    private ProgressBar registerProgressBar;
    private Toolbar registerToolbar;
    private EditText registerName, registerUsername, registerEmailAddress, registerPassword, registerConfirmPassword;
    private Spinner registerUserTypeSpinner;
    private Button registerButton;
    private Button registerLoginButton;

    private SharedPreferences authPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initVars();
        setupToolbar();
        loadUserType();
        loadInputsData();
        setOnClicks();

    }

    private void initVars() {

        registerConstraintLayout = findViewById(R.id.register_constraint_layout);
        registerProgressBar = findViewById(R.id.register_progress_bar);
        registerToolbar = findViewById(R.id.register_toolbar);
        registerName = findViewById(R.id.register_name);
        registerUsername = findViewById(R.id.register_username);
        registerEmailAddress = findViewById(R.id.register_email_address);
        registerPassword = findViewById(R.id.register_password);
        registerConfirmPassword = findViewById(R.id.register_confirm_password);
        registerUserTypeSpinner = findViewById(R.id.register_user_type_spinner);
        registerButton = findViewById(R.id.register_button);
        registerLoginButton = findViewById(R.id.register_login_button);

        authPreferences = getSharedPreferences("AuthPreferences", MODE_PRIVATE);

    }

    private void setupToolbar() {

        setSupportActionBar(registerToolbar);
        getSupportActionBar().setTitle(getString(R.string.register));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadUserType() {

        if (getIntent().hasExtra("user_type")) {
            String userType = getIntent().getStringExtra("user_type");
            registerUserTypeSpinner.setSelection(userType.equals("player") ? 0 : 1);
        }

    }

    private void loadInputsData() {

        String name = authPreferences.getString("name", "");
        String username = authPreferences.getString("username", "");
        String email = authPreferences.getString("email", "");
        String password = authPreferences.getString("password", "");
        String confirmPassword = authPreferences.getString("confirm_password", "");

        registerName.setText(name);
        registerUsername.setText(username);
        registerEmailAddress.setText(email);
        registerPassword.setText(password);
        registerConfirmPassword.setText(confirmPassword);

    }

    private void saveInputsData() {

        editor = authPreferences.edit();

        editor.putString("name", registerName.getText().toString());
        editor.putString("username", registerUsername.getText().toString());
        editor.putString("email", registerEmailAddress.getText().toString());
        editor.putString("password", registerPassword.getText().toString());
        editor.putString("confirm_password", registerConfirmPassword.getText().toString());

        editor.apply();

    }

    private void setOnClicks() {

        registerButton.setOnClickListener(v -> {

            HelperMethods.closeKeyboard(RegisterActivity.this);

            String name = registerName.getText().toString().trim();
            String username = registerUsername.getText().toString().toLowerCase().trim();
            String emailAddress = registerEmailAddress.getText().toString().trim();
            String password = registerPassword.getText().toString().trim();
            String confirmPassword = registerConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username) || TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {

                if (TextUtils.isEmpty(name)) {
                    registerName.requestFocus();
                    HelperMethods.showError(registerConstraintLayout, getString(R.string.name_cannot_be_empty));
                } else if (TextUtils.isEmpty(username)) {
                    registerUsername.requestFocus();
                    HelperMethods.showError(registerConstraintLayout, getString(R.string.username_cannot_be_empty));
                } else if (TextUtils.isEmpty(emailAddress)) {
                    registerEmailAddress.requestFocus();
                    HelperMethods.showError(registerConstraintLayout, getString(R.string.email_address_cannot_be_empty));
                } else if (TextUtils.isEmpty(password)) {
                    registerPassword.requestFocus();
                    HelperMethods.showError(registerConstraintLayout, getString(R.string.password_cannot_be_empty));
                } else if (TextUtils.isEmpty(confirmPassword)) {
                    registerConfirmPassword.requestFocus();
                    HelperMethods.showError(registerConstraintLayout, getString(R.string.confirm_password_cannot_be_empty));
                }

                HelperMethods.showKeyboard(RegisterActivity.this);
                return;

            }

            if (username.length() < 3) {
                registerUsername.requestFocus();
                HelperMethods.showKeyboard(RegisterActivity.this);
                HelperMethods.showError(registerConstraintLayout, getString(R.string.username_too_short));
                return;
            }

            if (username.equals("player") || username.equals("watcher")) {
                registerUsername.requestFocus();
                HelperMethods.showKeyboard(RegisterActivity.this);
                HelperMethods.showError(registerConstraintLayout, getString(R.string.username_not_available));
                return;
            }

            if (!password.equals(confirmPassword)) {
                HelperMethods.showError(registerConstraintLayout, getString(R.string.password_and_confirm_password_dont_match));
                return;
            }

            showLoading(true);

            FirebaseHelper.collectionReference("Users").whereEqualTo("username", username).count().get(AggregateSource.SERVER).addOnSuccessListener(this, aggregateQuerySnapshot -> {

                if (aggregateQuerySnapshot.getCount() != 0) {
                    showLoading(false);
                    registerUsername.requestFocus();
                    HelperMethods.showKeyboard(RegisterActivity.this);
                    HelperMethods.showError(registerConstraintLayout, getString(R.string.username_not_available));
                    return;
                }

                FirebaseFirestore.getInstance().collection("RegisteredEmails").document(emailAddress).get().addOnSuccessListener(this, documentSnapshot -> {

                    // Email registered
                    if (documentSnapshot.exists()) {

                        List<String> apps = (List<String>) documentSnapshot.get("apps");

                        // App registered
                        if (apps.contains("DareMe")) {
                            showLoading(false);
                            HelperMethods.showError(registerConstraintLayout, getString(R.string.email_is_already_registered));
                            return;
                        }

                        // App not registered
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailAddress, password)
                                .addOnFailureListener(this, e -> {
                                    showLoading(false);
                                    HelperMethods.showError(registerConstraintLayout, e.getMessage());
                                })
                                .addOnSuccessListener(this, authResult -> {

                                    FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this, token -> {

                                        User user = new User(
                                                authResult.getUser().getUid(),
                                                name,
                                                username,
                                                emailAddress,
                                                registerUserTypeSpinner.getSelectedItem().toString().toLowerCase(),
                                                null,
                                                null,
                                                token
                                        );

                                        FirebaseFirestore.getInstance().collection("RegisteredEmails").document(emailAddress)
                                                .update("apps", FieldValue.arrayUnion("DareMe"));

                                        FirebaseHelper.documentReference("Users/" + user.getId()).set(user)
                                                .addOnFailureListener(this, e -> {
                                                    showLoading(false);
                                                    HelperMethods.showError(registerConstraintLayout, e.getMessage());
                                                })
                                                .addOnSuccessListener(this, unused -> registerSuccess());

                                    });

                                });

                        return;
                    }

                    // Email not registered
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailAddress, password)
                            .addOnFailureListener(this, e -> {
                                showLoading(false);
                                HelperMethods.showError(registerConstraintLayout, e.getMessage());
                            })
                            .addOnSuccessListener(this, authResult -> {

                                Map<String, Object> map = new HashMap<>();
                                map.put("id", authResult.getUser().getUid());
                                map.put("email", emailAddress);
                                map.put("apps", FieldValue.arrayUnion("DareMe"));

                                FirebaseFirestore.getInstance().collection("RegisteredEmails").document(emailAddress)
                                        .set(map);

                                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this, token -> {

                                    User user = new User(
                                            authResult.getUser().getUid(),
                                            name,
                                            username,
                                            emailAddress,
                                            registerUserTypeSpinner.getSelectedItem().toString().toLowerCase(),
                                            null,
                                            null,
                                            token
                                    );

                                    FirebaseHelper.documentReference("Users/" + user.getId()).set(user)
                                            .addOnFailureListener(this, e -> {
                                                showLoading(false);
                                                HelperMethods.showError(registerConstraintLayout, e.getMessage());
                                            })
                                            .addOnSuccessListener(this, unused -> registerSuccess());

                                });

                            });

                });

            });

        });

        registerLoginButton.setOnClickListener(v -> {

            Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();

        });

    }

    private void showLoading(boolean loading) {

        if (loading) {
            registerProgressBar.setVisibility(View.VISIBLE);
            registerButton.setEnabled(false);
            registerLoginButton.setEnabled(false);
        } else {
            registerProgressBar.setVisibility(View.GONE);
            registerButton.setEnabled(true);
            registerLoginButton.setEnabled(true);
        }

    }

    private void registerSuccess() {

        editor = authPreferences.edit();
        editor.clear();
        editor.apply();

        showLoading(false);
        Toast.makeText(RegisterActivity.this, getString(R.string.successfully_registered), Toast.LENGTH_SHORT).show();

        Intent splashIntent = new Intent(RegisterActivity.this, SplashActivity.class);
        splashIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(splashIntent);
        finish();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;

    }

    @Override
    public void finish() {
        saveInputsData();
        super.finish();
    }

}
