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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.FirebaseHelper;
import ga.jundbits.dareme.Utils.HelperMethods;

public class LoginActivity extends AppCompatActivity {

    private ConstraintLayout loginConstraintLayout;
    private ProgressBar loginProgressBar;
    private Toolbar loginToolbar;
    private EditText loginEmailAddress, loginPassword;
    private Button loginButton, loginRegisterButton;

    private SharedPreferences authPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initVars();
        setupToolbar();
        loadInputsData();
        setOnClicks();

    }

    private void initVars() {

        loginConstraintLayout = findViewById(R.id.login_constraint_layout);
        loginProgressBar = findViewById(R.id.login_progress_bar);
        loginToolbar = findViewById(R.id.login_toolbar);
        loginEmailAddress = findViewById(R.id.login_email_address);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        loginRegisterButton = findViewById(R.id.login_register_button);

        authPreferences = getSharedPreferences("AuthPreferences", MODE_PRIVATE);

    }

    private void setupToolbar() {

        setSupportActionBar(loginToolbar);
        getSupportActionBar().setTitle(getString(R.string.login));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadInputsData() {

        String email = authPreferences.getString("email", "");
        String password = authPreferences.getString("password", "");

        loginEmailAddress.setText(email);
        loginPassword.setText(password);

    }

    private void saveInputsData() {

        editor = authPreferences.edit();

        editor.putString("email", loginEmailAddress.getText().toString());
        editor.putString("password", loginPassword.getText().toString());

        editor.apply();

    }

    private void setOnClicks() {

        loginButton.setOnClickListener(v -> {

            HelperMethods.closeKeyboard(LoginActivity.this);

            String emailAddress = loginEmailAddress.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            if (TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(password)) {

                if (TextUtils.isEmpty(emailAddress)) {
                    loginEmailAddress.requestFocus();
                    HelperMethods.showError(loginConstraintLayout, getString(R.string.email_address_cannot_be_empty));
                } else if (TextUtils.isEmpty(password)) {
                    loginPassword.requestFocus();
                    HelperMethods.showError(loginConstraintLayout, getString(R.string.password_cannot_be_empty));
                }

                HelperMethods.showKeyboard(LoginActivity.this);
                return;

            }

            showLoading(true);

            FirebaseFirestore.getInstance().collection("RegisteredEmails").document(emailAddress).get().addOnSuccessListener(this, documentSnapshot -> {

                // Email not registered
                if (!documentSnapshot.exists()) {
                    showLoading(false);
                    HelperMethods.showError(loginConstraintLayout, getString(R.string.incorrect_email_password));
                    return;
                }

                // Email registered
                List<String> apps = (List<String>) documentSnapshot.get("apps");

                // App not registered
                if (!apps.contains("DareMe")) {
                    showLoading(false);
                    HelperMethods.showError(loginConstraintLayout, getString(R.string.app_is_not_registered));
                    return;
                }

                // App registered
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailAddress, password)
                        .addOnFailureListener(this, e -> {
                            showLoading(false);
                            HelperMethods.showError(loginConstraintLayout, e.getMessage());
                        })
                        .addOnSuccessListener(this, authResult -> {
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnSuccessListener(this, token -> FirebaseHelper.documentReference("Users/" + authResult.getUser().getUid())
                                            .update("fcm_token", token)
                                            .addOnSuccessListener(this, unused -> loginSuccess()));
                        });

            });

        });

        loginRegisterButton.setOnClickListener(v -> {

            Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
            finish();

        });

    }

    private void showLoading(boolean loading) {

        if (loading) {
            loginProgressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            loginRegisterButton.setEnabled(false);
        } else {
            loginProgressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            loginRegisterButton.setEnabled(true);
        }

    }

    private void loginSuccess() {

        editor = authPreferences.edit();
        editor.clear();
        editor.apply();

        showLoading(false);
        Toast.makeText(LoginActivity.this, getString(R.string.successfully_logged_in), Toast.LENGTH_SHORT).show();

        Intent splashIntent = new Intent(LoginActivity.this, SplashActivity.class);
        splashIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(splashIntent);
        finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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
