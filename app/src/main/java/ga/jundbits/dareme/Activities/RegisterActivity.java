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
import android.widget.Spinner;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import ga.jundbits.dareme.Models.UserModel;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.HelperMethods;

public class RegisterActivity extends AppCompatActivity {

    private ConstraintLayout registerConstraintLayout;
    private Toolbar registerToolbar;
    private EditText registerName, registerUsername, registerEmailAddress, registerPassword, registerConfirmPassword;
    private ImageButton registerShowPassword;
    private Spinner registerUserTypeSpinner;
    private Button registerButton;
    private Button registerLoginButton;

    private FirebaseAuth firebaseAuth;

    private Vibrator vibrator;

    private ProgressDialog registerProgressDialog;

    private SharedPreferences registerPreferences;
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

        firebaseAuth = FirebaseAuth.getInstance();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        registerProgressDialog = new ProgressDialog(RegisterActivity.this);

        registerPreferences = getSharedPreferences("RegisterPreferences", MODE_PRIVATE);

        registerShowPassword.setColorFilter(Color.RED);

    }

    private void setupToolbar() {

        // TODO: 10-Jan-23 try to use the original toolbar everywhere

        setSupportActionBar(registerToolbar);
        getSupportActionBar().setTitle(getString(R.string.register));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadUserType() {

        if (getIntent().hasExtra("user_type")) {

            String userType = getIntent().getStringExtra("user_type");

            if (userType.equals("player")) {
                registerUserTypeSpinner.setSelection(0);
            } else if (userType.equals("watcher")) {
                registerUserTypeSpinner.setSelection(1);
            }

        }

    }

    private void loadInputsData() {

        String name = registerPreferences.getString("name", "");
        String username = registerPreferences.getString("username", "");
        String email = registerPreferences.getString("email", "");
        String password = registerPreferences.getString("password", "");
        String confirmPassword = registerPreferences.getString("confirm_password", "");

        registerName.setText(name);
        registerUsername.setText(username);
        registerEmailAddress.setText(email);
        registerPassword.setText(password);
        registerConfirmPassword.setText(confirmPassword);

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
        editor.putString("confirm_password", confirmPassword);

        editor.apply();

    }

    private void setOnClicks() {

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

                if (registerPassword.hasFocus()) {
                    registerPassword.setSelection(registerPassword.getText().length());
                } else if (registerConfirmPassword.hasFocus()) {
                    registerConfirmPassword.setSelection(registerConfirmPassword.getText().length());
                }

            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HelperMethods.closeKeyboard(RegisterActivity.this);

                String name = registerName.getText().toString().trim();
                String username = registerUsername.getText().toString().toLowerCase().trim();
                String emailAddress = registerEmailAddress.getText().toString().trim();
                String password = registerPassword.getText().toString().trim();
                String confirmPassword = registerConfirmPassword.getText().toString().trim();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username) || TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {

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

                    HelperMethods.showKeyboard(RegisterActivity.this);
                    return;

                }

                if (username.length() < 3) {
                    registerUsername.requestFocus();
                    HelperMethods.showKeyboard(RegisterActivity.this);
                    showError(getString(R.string.username_too_short));
                    return;
                }

                if (username.equals("player") || username.equals("watcher")) {
                    registerUsername.requestFocus();
                    HelperMethods.showKeyboard(RegisterActivity.this);
                    showError(getString(R.string.username_not_available));
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    showError(getString(R.string.password_and_confirm_password_dont_match));
                    return;
                }

                registerProgressDialog.setTitle(getString(R.string.registering));
                registerProgressDialog.setMessage(getString(R.string.please_wait_do_not_close_the_app));
                registerProgressDialog.setCancelable(false);
                registerProgressDialog.setCanceledOnTouchOutside(false);
                registerProgressDialog.show();

                firebaseAuth.createUserWithEmailAndPassword(emailAddress, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                FirebaseUser firebaseUser = authResult.getUser();

                                HelperMethods.usersCollectionRef(getApplicationContext())
                                        .whereEqualTo("username", username)
                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                if (!queryDocumentSnapshots.isEmpty()) {

                                                    registerProgressDialog.dismiss();

                                                    registerUsername.requestFocus();
                                                    HelperMethods.showKeyboard(RegisterActivity.this);

                                                    showError(getString(R.string.username_is_not_available));

                                                    firebaseAuth.signOut();
                                                    firebaseUser.delete();

                                                    return;

                                                }

                                                FirebaseMessaging.getInstance()
                                                        .getToken()
                                                        .addOnSuccessListener(new OnSuccessListener<String>() {
                                                            @Override
                                                            public void onSuccess(String token) {

                                                                UserModel userModel = new UserModel(
                                                                        firebaseUser.getUid(),
                                                                        name,
                                                                        username,
                                                                        emailAddress,
                                                                        registerUserTypeSpinner.getSelectedItem().toString().toLowerCase(),
                                                                        null,
                                                                        null,
                                                                        token
                                                                );

                                                                createUserDatabase(userModel);

                                                            }
                                                        });

                                            }
                                        });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                registerProgressDialog.dismiss();
                                showError(e.getMessage());

                            }
                        });

            }
        });

        registerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();

            }
        });

    }

    private void createUserDatabase(UserModel userModel) {

        HelperMethods.setCurrentUserModel(userModel);
        DocumentReference currentUserDocument = HelperMethods.usersCollectionRef(this).document(userModel.getId());

        currentUserDocument.set(userModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        editor = registerPreferences.edit();
                        editor.clear();
                        editor.apply();

                        registerProgressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, getString(R.string.successfully_registered), Toast.LENGTH_SHORT).show();

                        Intent splashIntent = new Intent(RegisterActivity.this, SplashActivity.class);
                        startActivity(splashIntent);
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        registerProgressDialog.dismiss();
                        showError(e.getMessage());

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
