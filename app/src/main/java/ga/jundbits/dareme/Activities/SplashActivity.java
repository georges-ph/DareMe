package ga.jundbits.dareme.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;

import ga.jundbits.dareme.BuildConfig;
import ga.jundbits.dareme.Models.UserModel;
import ga.jundbits.dareme.R;
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
                .addOnSuccessListener(this, new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {

                        double version = firebaseRemoteConfig.getDouble("dare_me_app_version");
                        boolean deniedAppAccess = firebaseRemoteConfig.getBoolean("dare_me_denied_app_access");
                        boolean forceUpdate = firebaseRemoteConfig.getBoolean("dare_me_force_update");

                        if (deniedAppAccess) {

                            Toast.makeText(SplashActivity.this, getString(R.string.error_please_try_again_later), Toast.LENGTH_SHORT).show();
                            finish();
                            return;

                        }

                        if (HelperMethods.getCurrentUser() == null) {

                            Intent registerIntent = new Intent(SplashActivity.this, StartActivity.class);
                            startActivity(registerIntent);
                            finish();
                            return;

                        }

                        String currentAppVersion = BuildConfig.VERSION_NAME;

                        if (currentAppVersion.equals(String.valueOf(version))) {

                            prepareForMainActivity();
                            return;

                        }

                        if (forceUpdate) {

                            splashUpdateLayout.setVisibility(View.VISIBLE);

                            splashUpdateButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(updateFromURL));
                                    startActivity(intent);

                                }
                            });

                        } else {

                            splashUpdateLayout.setVisibility(View.GONE);

                            AlertDialog.Builder appUpdateDialogBuilder = new AlertDialog.Builder(SplashActivity.this);
                            appUpdateDialogBuilder.setTitle(getString(R.string.new_version_available));
                            appUpdateDialogBuilder.setMessage(getString(R.string.version) + " " + version + " " + getString(R.string.is_available_current_version_is) + " " + currentAppVersion);
                            appUpdateDialogBuilder.setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(updateFromURL));
                                    startActivity(intent);

                                }
                            });
                            appUpdateDialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();
                                    prepareForMainActivity();

                                }
                            });
                            appUpdateDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {

                                    dialog.dismiss();
                                    prepareForMainActivity();

                                }
                            });
                            appUpdateDialogBuilder.setCancelable(true);
                            AlertDialog appUpdateAlertDialog = appUpdateDialogBuilder.create();
                            appUpdateAlertDialog.show();

                        }

                    }
                });

    }

    private void prepareForMainActivity() {

        firebaseDynamicLinks.getDynamicLink(getIntent())
                .addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {

                        if (pendingDynamicLinkData != null) {

                            Uri deepLink = pendingDynamicLinkData.getLink();
                            challengeID = firebaseStorage.getReferenceFromUrl(String.valueOf(deepLink)).getName();

                        }

                    }
                });

        if (getIntent().hasExtra("challenge_id")) {
            challengeID = getIntent().getStringExtra("challenge_id");
        }

        if (HelperMethods.getCurrentUserModel() != null) {
            startMainActivity();
            return;
        }

        HelperMethods.userDocumentRef(this, HelperMethods.getCurrentUserID())
                .get().addOnSuccessListener(this, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        UserModel userModel = documentSnapshot.toObject(UserModel.class);
                        HelperMethods.setCurrentUserModel(userModel);

                        startMainActivity();

                    }
                });

    }

    private void startMainActivity() {

        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        if (challengeID != null)
            mainIntent.putExtra("challenge_id", challengeID);
        startActivity(mainIntent);
        finish();

    }

}
