package ga.jundbits.dareme.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

import ga.jundbits.dareme.R;
import github.nisrulz.easydeviceinfo.base.EasyAppMod;
import github.nisrulz.easydeviceinfo.base.EasyNetworkMod;
import github.nisrulz.easydeviceinfo.base.NetworkType;

public class SplashActivity extends AppCompatActivity {

    // TODO: 17-Dec-22 https://developer.android.com/develop/ui/views/launch/splash-screen/migrate 
    
    ConstraintLayout noConnectionLayout;

    ConstraintLayout splashUpdateLayout;
    Button splashUpdateButton;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDynamicLinks firebaseDynamicLinks;
    FirebaseStorage firebaseStorage;

    String currentUserID;

    DocumentReference currentUserDocument;

    EasyNetworkMod easyNetworkMod;
    EasyAppMod easyAppMod;

    String deviceID;
    String challengeID = "null";
    String userToken;

    SharedPreferences networkPreferences;
    SharedPreferences.Editor editor;

    public static final String updateFromURL = "https://bit.ly/3cu9i1t";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        noConnectionLayout = findViewById(R.id.no_connection_layout);

        splashUpdateLayout = findViewById(R.id.splash_update_layout);
        splashUpdateButton = findViewById(R.id.splash_update_button);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDynamicLinks = FirebaseDynamicLinks.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        easyNetworkMod = new EasyNetworkMod(this);
        easyAppMod = new EasyAppMod(this);

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

    }

    private void registerDevice() {

        final Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put("device_id", deviceID);
        deviceMap.put("denied", false);
        Log
                .d("msggg","id: "+deviceID);

        firebaseFirestore.collection("Devices").document(deviceID)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (!documentSnapshot.exists()) {

                            firebaseFirestore.collection("Devices").document(deviceID)
                                    .set(deviceMap);

                        }

                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (easyNetworkMod.isNetworkAvailable()) {

            noConnectionLayout.setVisibility(View.GONE);

            networkPreferences = getSharedPreferences("Network Preferences", MODE_PRIVATE);
            editor = networkPreferences.edit();

            if (easyNetworkMod.getNetworkType() == NetworkType.CELLULAR_2G || easyNetworkMod.getNetworkType() == NetworkType.CELLULAR_3G || easyNetworkMod.getNetworkType() == NetworkType.CELLULAR_4G || easyNetworkMod.getNetworkType() == NetworkType.CELLULAR_UNKNOWN || easyNetworkMod.getNetworkType() == NetworkType.CELLULAR_UNIDENTIFIED_GEN) {
                editor.putString("connected via", "mobile");
            }

            if (easyNetworkMod.getNetworkType() == NetworkType.WIFI_WIFIMAX) {
                editor.putString("connected via", "wifi");
            }

            editor.apply();

            getUserToken();

            firebaseFirestore.collection("Devices").document(deviceID)
                    .addSnapshotListener(SplashActivity.this, new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                            if (documentSnapshot!=null&& documentSnapshot.exists()) {

                                boolean denied = documentSnapshot.getBoolean("denied");

                                if (denied) {

                                    Toast.makeText(SplashActivity.this, getString(R.string.error_please_try_again_later), Toast.LENGTH_SHORT).show();
                                    finish();

                                } else {

                                    if (firebaseUser == null) {

                                        Intent registerIntent = new Intent(SplashActivity.this, StartActivity.class);
                                        startActivity(registerIntent);
                                        finish();

                                    } else {

                                        firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                                .collection("App").document("Updates")
                                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot snapshot) {

                                                        double version = snapshot.getDouble("version");
                                                        boolean forceUpdate = snapshot.getBoolean("force_update");

                                                        String currentAppVersion = easyAppMod.getAppVersion();

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
                                                });

                                    }

                                }

                            } else {
                                registerDevice();
                            }

                        }
                    });

        } else {

            noConnectionLayout.setVisibility(View.VISIBLE);

            noConnectionLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    noConnectionLayout.setVisibility(View.GONE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recreate();
                        }
                    }, 1500);

                }
            });

        }

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

        currentUserDocument.update("token_id", userToken);

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
