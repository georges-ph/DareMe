package ga.jundbits.dareme.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

import ga.jundbits.dareme.BuildConfig;
import ga.jundbits.dareme.R;

public class SplashActivity extends AppCompatActivity {

    // TODO: 17-Dec-22 https://developer.android.com/develop/ui/views/launch/splash-screen/migrate 

    private ConstraintLayout splashUpdateLayout;
    private Button splashUpdateButton;

    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDynamicLinks firebaseDynamicLinks;
    private FirebaseStorage firebaseStorage;

    private String currentUserID;

    private DocumentReference currentUserDocument;

    private String deviceID;
    private String challengeID = "null";
    private String userToken;

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
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDynamicLinks = FirebaseDynamicLinks.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60 * 60 * 2) // 2 hours
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        firebaseRemoteConfig.setDefaultsAsync(R.xml.firebase_remote_config_defaults);

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

    }

    private void loadData() {

        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(this, new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {

                        userToken = token;

                    }
                });

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

                        } else {

                            if (firebaseUser == null) {

                                Intent registerIntent = new Intent(SplashActivity.this, StartActivity.class);
                                startActivity(registerIntent);
                                finish();

                            } else {

                                String currentAppVersion = BuildConfig.VERSION_NAME;

                                if (currentAppVersion.equals(String.valueOf(version))) {

                                    startMainActivity();

                                } else {

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
                                                startMainActivity();

                                            }
                                        });
                                        appUpdateDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {

                                                dialog.dismiss();
                                                startMainActivity();

                                            }
                                        });
                                        appUpdateDialogBuilder.setCancelable(true);
                                        AlertDialog appUpdateAlertDialog = appUpdateDialogBuilder.create();
                                        appUpdateAlertDialog.show();

                                    }

                                }

                            }

                        }

                    }
                });

    }

    private void startMainActivity() {

        currentUserID = firebaseUser.getUid();
        currentUserDocument = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);

        final String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

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

        currentUserDocument.update("fcm_token", userToken);

        currentUserDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                final boolean canOpenApp = documentSnapshot.getBoolean("can_open_app");
                final String userType = documentSnapshot.getString("type");

                currentUserDocument.collection("Sessions").whereEqualTo("device_id", deviceID)
                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                if (!queryDocumentSnapshots.isEmpty()) {

                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                                        currentUserDocument.collection("Sessions").document(documentSnapshot.getId())
                                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                        boolean loggedIn = documentSnapshot.getBoolean("logged_in");

                                                        if (!loggedIn) {

                                                            firebaseAuth.signOut();
                                                            Toast.makeText(SplashActivity.this, getString(R.string.you_are_not_logged_in), Toast.LENGTH_SHORT).show();
                                                            recreate();

                                                        } else {

                                                            if (canOpenApp) {

                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("user_type", userType);
                                                                bundle.putString("challenge_id", challengeID);

                                                                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                                                                mainIntent.putExtras(bundle);
                                                                startActivity(mainIntent);
                                                                finish();

                                                            } else {
                                                                Toast.makeText(SplashActivity.this, getString(R.string.error_please_try_again_later), Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            }

                                                        }

                                                    }
                                                });

                                    }

                                }

                            }
                        });

            }
        });

    }

}
