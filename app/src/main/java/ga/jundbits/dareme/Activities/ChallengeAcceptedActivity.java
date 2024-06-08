package ga.jundbits.dareme.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import ga.jundbits.dareme.Models.Challenge;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.FirebaseHelper;
import ga.jundbits.dareme.Utils.HelperMethods;

public class ChallengeAcceptedActivity extends AppCompatActivity {

    private Toolbar challengeAcceptedToolbar;
    private ProgressBar challengeAcceptedProgressBar;
    private ConstraintLayout challengeAcceptedChallengeTextLayout;
    private TextView challengeAcceptedChallengeText;
    private VideoView challengeAcceptedVideoView;
    private Button challengeAcceptedUploadButton;

    private String challengeID;
    private Challenge challenge;

    private DocumentReference challengeDocument;
    private StorageReference challengeStorageReference;

    private boolean clickedFirstTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_accepted);

        initVars();
        setupToolbar();
        loadData();
        setOnClicks();

    }

    private void initVars() {

        challengeAcceptedToolbar = findViewById(R.id.challenge_accepted_toolbar);
        challengeAcceptedProgressBar = findViewById(R.id.challenge_accepted_progress_bar);
        challengeAcceptedChallengeTextLayout = findViewById(R.id.challenge_accepted_challenge_text_layout);
        challengeAcceptedChallengeText = findViewById(R.id.challenge_accepted_challenge_text);
        challengeAcceptedVideoView = findViewById(R.id.challenge_accepted_video_view);
        challengeAcceptedUploadButton = findViewById(R.id.challenge_accepted_upload_button);

        challengeID = getIntent().getExtras().getString("challenge_id");
        challenge = new Gson().fromJson(getIntent().getExtras().getString("challenge"), Challenge.class);

        challengeDocument = FirebaseHelper.documentReference("Challenges/" + challengeID);
        challengeStorageReference = FirebaseHelper.storageReference("Challenges/" + challengeID + ".mp4");

    }

    private void setupToolbar() {

        setSupportActionBar(challengeAcceptedToolbar);
        getSupportActionBar().setTitle(getString(R.string.challenge_accepted));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadData() {

        challengeAcceptedChallengeText.setText(challenge.getDescription());
        challengeAcceptedChallengeTextLayout.setBackgroundColor(Color.parseColor(challenge.getColor()));

    }

    private void setOnClicks() {

        challengeAcceptedUploadButton.setOnClickListener(view -> {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                if (clickedFirstTime) uploadVideo();
                else chooseVideoToUpload();

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionDeniedDialog(false);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

        });

    }

    private void showPermissionDeniedDialog(boolean isPermanent) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ChallengeAcceptedActivity.this);
        builder.setTitle(getString(!isPermanent ? R.string.permission_denied : R.string.permission_permanently_denied));
        builder.setMessage(getString(R.string.permission_needed_in_order_to_choose_video));
        builder.setCancelable(false);

        builder.setPositiveButton(getString(!isPermanent ? R.string.ok : R.string.go_to_settings), (dialog, which) -> {

            if (!isPermanent) {
                dialog.dismiss();
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                return;
            }

            Intent permissionSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", getPackageName(), null));
            startActivity(permissionSettingsIntent);

        });

        if (isPermanent) {
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void chooseVideoToUpload() {

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityLauncher.launch(Intent.createChooser(intent, "Select Video"));

    }

    private void uploadVideo() {

        showLoading(true);

        Uri videoUri = Uri.parse(String.valueOf(challengeAcceptedVideoView.getTag()));
        UploadTask uploadTaskVideo = challengeStorageReference.putFile(videoUri);

        // Get Download Url
        uploadTaskVideo.continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return challengeStorageReference.getDownloadUrl();
                })
                .addOnFailureListener(this, e -> {
                    showLoading(false);
                    Toast.makeText(ChallengeAcceptedActivity.this, getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(this, uri -> challengeDocument.update("video_url", uri.toString())
                        .addOnFailureListener(e -> {
                            showLoading(false);
                            Toast.makeText(ChallengeAcceptedActivity.this, getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
                        })
                        .addOnSuccessListener(unused -> {

                            challenge.setVideo_url(uri.toString());

                            showLoading(false);
                            Toast.makeText(ChallengeAcceptedActivity.this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show();

                            Bundle bundle = new Bundle();
                            bundle.putString("challenge", new Gson().toJson(challenge));
                            bundle.putString("challenge_id", challengeID);

                            Intent challengeIntent = new Intent(ChallengeAcceptedActivity.this, ChallengeActivity.class);
                            challengeIntent.putExtras(bundle);
                            startActivity(challengeIntent);
                            finish();

                        }));

    }

    private void showLoading(boolean loading) {

        if (loading) {
            challengeAcceptedProgressBar.setVisibility(View.VISIBLE);
            challengeAcceptedUploadButton.setEnabled(false);
        } else {
            challengeAcceptedProgressBar.setVisibility(View.GONE);
            challengeAcceptedUploadButton.setEnabled(true);
        }

    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {

        if (granted) {
            if (clickedFirstTime) uploadVideo();
            else chooseVideoToUpload();
        } else if (!ActivityCompat.shouldShowRequestPermissionRationale(ChallengeAcceptedActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showPermissionDeniedDialog(true);
        }

    });

    private final ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            final MediaController mediaController = new MediaController(ChallengeAcceptedActivity.this);

            if (result.getResultCode() != RESULT_OK) return;

            challengeAcceptedVideoView.setVisibility(View.VISIBLE);
            challengeAcceptedVideoView.setTag(result.getData().getData());

            challengeAcceptedVideoView.setOnPreparedListener(mp -> {

                int videoDuration = mp.getDuration() / 1000;

                if (videoDuration <= 60) {
                    clickedFirstTime = true;
                } else {
                    clickedFirstTime = false;
                    Toast.makeText(ChallengeAcceptedActivity.this, getString(R.string.video_is_too_long), Toast.LENGTH_SHORT).show();
                }

                mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);

                ViewGroup.LayoutParams params = challengeAcceptedVideoView.getLayoutParams();
                params.width = HelperMethods.getScreenWidth(ChallengeAcceptedActivity.this);
                params.height = HelperMethods.getScreenWidth(ChallengeAcceptedActivity.this);
                challengeAcceptedVideoView.setLayoutParams(params);

            });

            mediaController.setAnchorView(challengeAcceptedVideoView);
            challengeAcceptedVideoView.setMediaController(mediaController);
            challengeAcceptedVideoView.setVideoURI(result.getData().getData());
            challengeAcceptedVideoView.seekTo(300);
            challengeAcceptedVideoView.setOnCompletionListener(mp -> mediaController.show());

        }
    });

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;

    }

}
