package ga.jundbits.dareme.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;

import ga.jundbits.dareme.BuildConfig;
import ga.jundbits.dareme.Models.User;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.FirebaseHelper;
import ga.jundbits.dareme.Utils.HelperMethods;

public class SplashActivity extends AppCompatActivity {

    // TODO: 17-Dec-22 https://developer.android.com/develop/ui/views/launch/splash-screen/migrate 

    private ConstraintLayout splashUpdateLayout;
    private Button splashUpdateButton;

    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseDynamicLinks firebaseDynamicLinks;
    private FirebaseStorage firebaseStorage;

    private String challengeID;

    public static final String updateFromURL = "https://bit.ly/3cu9i1t";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initVars();
        loadData();
        setOnClicks();

    }

    private void initVars() {

        splashUpdateLayout = findViewById(R.id.splash_update_layout);
        splashUpdateButton = findViewById(R.id.splash_update_button);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseDynamicLinks = FirebaseDynamicLinks.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60 * 60 * 2) // 2 hours
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        firebaseRemoteConfig.setDefaultsAsync(R.xml.firebase_remote_config_defaults);

    }

    private void loadData() {

        firebaseRemoteConfig.fetchAndActivate()
                .addOnSuccessListener(this, aBoolean -> {

                    double version = firebaseRemoteConfig.getDouble("dare_me_app_version");
                    boolean deniedAppAccess = firebaseRemoteConfig.getBoolean("dare_me_denied_app_access");
                    boolean forceUpdate = firebaseRemoteConfig.getBoolean("dare_me_force_update");

                    if (deniedAppAccess) {
                        Toast.makeText(SplashActivity.this, getString(R.string.error_please_try_again_later), Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    if (FirebaseHelper.getCurrentUser() == null) {
                        Intent startIntent = new Intent(SplashActivity.this, StartActivity.class);
                        startActivity(startIntent);
                        finish();
                        return;
                    }

                    if (BuildConfig.VERSION_NAME.equals(String.valueOf(version))) {
                        prepareForMainActivity();
                        return;
                    }

                    if (forceUpdate) {
                        splashUpdateLayout.setVisibility(View.VISIBLE);
                        return;
                    }

                    splashUpdateLayout.setVisibility(View.GONE);

                    AlertDialog.Builder appUpdateDialogBuilder = new AlertDialog.Builder(SplashActivity.this);
                    appUpdateDialogBuilder.setTitle(getString(R.string.new_version_available));
                    appUpdateDialogBuilder.setMessage(getString(R.string.version) + " " + version + " " + getString(R.string.is_available_current_version_is) + " " + BuildConfig.VERSION_NAME);
                    appUpdateDialogBuilder.setPositiveButton(getString(R.string.update), (dialog, which) -> splashUpdateButton.performClick());
                    appUpdateDialogBuilder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                        dialog.dismiss();
                        prepareForMainActivity();
                    });
                    appUpdateDialogBuilder.setOnCancelListener(dialog -> {
                        dialog.dismiss();
                        prepareForMainActivity();
                    });
                    appUpdateDialogBuilder.setCancelable(true);
                    AlertDialog appUpdateAlertDialog = appUpdateDialogBuilder.create();
                    appUpdateAlertDialog.show();

                });

    }

    private void setOnClicks() {

        splashUpdateButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateFromURL));
            startActivity(intent);
        });

    }

    private void prepareForMainActivity() {

        // TODO: 08-Jun-24 change this once changed deep link service
        firebaseDynamicLinks.getDynamicLink(getIntent())
                .addOnSuccessListener(pendingDynamicLinkData -> {

                    if (pendingDynamicLinkData != null) {
                        Uri deepLink = pendingDynamicLinkData.getLink();
                        challengeID = firebaseStorage.getReferenceFromUrl(String.valueOf(deepLink)).getName();
                    }

                });

        if (getIntent().hasExtra("challenge_id")) {
            challengeID = getIntent().getStringExtra("challenge_id");
        }

        if (HelperMethods.getCurrentUser() != null) {
            startMainActivity();
            return;
        }

        FirebaseHelper.documentReference("Users/" + FirebaseHelper.getCurrentUser().getUid())
                .get().addOnSuccessListener(documentSnapshot -> {

                    User user = documentSnapshot.toObject(User.class);
                    HelperMethods.setCurrentUser(user);
                    startMainActivity();

                });

    }

    private void startMainActivity() {

        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        if (challengeID != null) mainIntent.putExtra("challenge_id", challengeID);
        startActivity(mainIntent);
        finish();

    }

}
