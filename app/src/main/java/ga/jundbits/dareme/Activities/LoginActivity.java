package ga.jundbits.dareme.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.HelperMethods;

public class LoginActivity extends AppCompatActivity {

    private ConstraintLayout loginConstraintLayout;
    private Toolbar loginToolbar;
    private EditText loginEmailAddress, loginPassword;
    private ImageButton loginShowPassword;
    private Button loginButton, loginRegisterButton;

    private FirebaseAuth firebaseAuth;

    private Vibrator vibrator;

    private ProgressDialog loginProgressDialog;

    private SharedPreferences loginPreferences;
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
        loginToolbar = findViewById(R.id.login_toolbar);
        loginEmailAddress = findViewById(R.id.login_email_address);
        loginPassword = findViewById(R.id.login_password);
        loginShowPassword = findViewById(R.id.login_show_password);
        loginButton = findViewById(R.id.login_button);
        loginRegisterButton = findViewById(R.id.login_register_button);

        firebaseAuth = FirebaseAuth.getInstance();

        loginProgressDialog = new ProgressDialog(this);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        loginShowPassword.setColorFilter(Color.RED);

        loginPreferences = getSharedPreferences("LoginPreferences", MODE_PRIVATE);

    }

    private void setupToolbar() {

        setSupportActionBar(loginToolbar);
        getSupportActionBar().setTitle(getString(R.string.login));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadInputsData() {

        String email = loginPreferences.getString("email", "");
        String password = loginPreferences.getString("password", "");

        loginEmailAddress.setText(email);
        loginPassword.setText(password);

    }

    private void saveInputsData() {

        editor = loginPreferences.edit();

        String email = loginEmailAddress.getText().toString();
        String password = loginPassword.getText().toString();

        editor.putString("email", email);
        editor.putString("password", password);

        editor.apply();

    }

    private void setOnClicks() {

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

                if (loginPassword.hasFocus()) {
                    loginPassword.setSelection(loginPassword.getText().length());
                }

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HelperMethods.closeKeyboard(LoginActivity.this);

                String emailAddress = loginEmailAddress.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();

                if (TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(password)) {

                    if (TextUtils.isEmpty(emailAddress)) {
                        loginEmailAddress.requestFocus();
                        showError(getString(R.string.email_address_cannot_be_empty));
                    } else if (TextUtils.isEmpty(password)) {
                        loginPassword.requestFocus();
                        showError(getString(R.string.password_cannot_be_empty));
                    }

                    HelperMethods.showKeyboard(LoginActivity.this);
                    return;

                }

                loginProgressDialog.setTitle(getString(R.string.logging_in));
                loginProgressDialog.setMessage(getString(R.string.please_wait_do_not_close_the_app));
                loginProgressDialog.setCanceledOnTouchOutside(false);
                loginProgressDialog.setCancelable(false);
                loginProgressDialog.show();

                firebaseAuth.signInWithEmailAndPassword(emailAddress, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                FirebaseUser firebaseUser = authResult.getUser();

                                FirebaseMessaging.getInstance()
                                        .getToken()
                                        .addOnSuccessListener(new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(String token) {

                                                updateUserDatabase(firebaseUser.getUid(), token);

                                            }
                                        });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                loginProgressDialog.dismiss();
                                showError(e.getMessage());

                            }
                        });

            }

        });

        loginRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
                finish();

            }
        });

    }

    private void updateUserDatabase(String currentUserID, String token) {

        editor = loginPreferences.edit();
        editor.clear();
        editor.apply();

        HelperMethods.userDocumentRef(this, currentUserID)
                .update("fcm_token", token);

        loginProgressDialog.dismiss();
        Toast.makeText(LoginActivity.this, getString(R.string.successfully_logged_in), Toast.LENGTH_SHORT).show();

        Intent splashIntent = new Intent(LoginActivity.this, SplashActivity.class);
        startActivity(splashIntent);
        finish();

    }

    private void showError(String errorMessage) {

        vibrator.vibrate(500);
        Snackbar.make(loginConstraintLayout, errorMessage, Snackbar.LENGTH_SHORT).show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            default:
                return false;

        }

    }

    @Override
    public void finish() {
        saveInputsData();
        super.finish();
    }

}
