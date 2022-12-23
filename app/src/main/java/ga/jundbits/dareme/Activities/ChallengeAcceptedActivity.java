package ga.jundbits.dareme.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ga.jundbits.dareme.R;
import io.github.muddz.quickshot.QuickShot;

public class ChallengeAcceptedActivity extends AppCompatActivity {

    private Toolbar challengeAcceptedToolbar;
    private ConstraintLayout challengeAcceptedChallengeTextLayout;
    private TextView challengeAcceptedChallengeText;
    private VideoView challengeAcceptedVideoView;
    private Button challengeAcceptedUploadButton;

    private String challengeID;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;

    private String currentUserID;

    private DocumentReference currentUserDocument;
    private DocumentReference challengeDocument;
    private StorageReference challengeStorageReference;
    private StorageReference challengeStorageThumbnailReference;

    private boolean clickedFirstTime = false;
    private boolean gotVideoThumbnail = false;

    public static final int VIDEO_REQUEST_CODE = 1;
    public static final int PERMISSION_SETTINGS_REQUEST_CODE = 2;

    private ProgressDialog progressDialog;

    private String thumbnailFileName;

    private File appStorageDirectory, thumbnailStorageDirectory;

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
        challengeAcceptedChallengeTextLayout = findViewById(R.id.challenge_accepted_challenge_text_layout);
        challengeAcceptedChallengeText = findViewById(R.id.challenge_accepted_challenge_text);
        challengeAcceptedVideoView = findViewById(R.id.challenge_accepted_video_view);
        challengeAcceptedUploadButton = findViewById(R.id.challenge_accepted_upload_button);

        challengeID = getIntent().getStringExtra("challenge_id");

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();

        currentUserID = firebaseUser.getUid();

        currentUserDocument = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);
        challengeDocument = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Challenges").document(challengeID);
        challengeStorageReference = firebaseStorage.getReference().child(getString(R.string.app_name)).child("Challenges").child(challengeID).child(challengeID + ".mp4");
        challengeStorageThumbnailReference = firebaseStorage.getReference().child(getString(R.string.app_name)).child("Thumbnails").child(challengeID).child(challengeID + ".jpg");

        progressDialog = new ProgressDialog(ChallengeAcceptedActivity.this);

        appStorageDirectory = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));
        thumbnailStorageDirectory = new File(appStorageDirectory + "/Thumbnails");

    }

    private void setupToolbar() {

        setSupportActionBar(challengeAcceptedToolbar);
        getSupportActionBar().setTitle(getString(R.string.challenge_accepted));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadData() {

        challengeDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String challengeText = documentSnapshot.getString("text");
                String challengeColor = documentSnapshot.getString("color");

                challengeAcceptedChallengeText.setText(challengeText);
                challengeAcceptedChallengeTextLayout.setBackgroundColor(Color.parseColor(challengeColor));

            }
        });

    }

    private void setOnClicks() {

        challengeAcceptedUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dexter.withContext(ChallengeAcceptedActivity.this)
                        .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                                if (multiplePermissionsReport.areAllPermissionsGranted()) {

                                    if (!appStorageDirectory.exists()) {
                                        appStorageDirectory.mkdirs();
                                        thumbnailStorageDirectory.mkdirs();
                                    }

                                    if (clickedFirstTime) {
                                        uploadVideo();
                                    } else {
                                        chooseVideoToUpload();
                                    }

                                } else {
                                    showPermissionDeniedDialog();
                                }

                                if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                                    showPermissionPermanentlyDeniedDialog();
                                }

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        })
                        .onSameThread()
                        .check();

            }
        });

    }

    private void showPermissionPermanentlyDeniedDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ChallengeAcceptedActivity.this);
        builder.setTitle(getString(R.string.permission_permanently_denied));
        builder.setMessage(getString(R.string.permission_needed_in_order_to_choose_video));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openPermissionsSettings();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void openPermissionsSettings() {

        Intent permissionSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        permissionSettingsIntent.setData(uri);
        startActivityForResult(permissionSettingsIntent, PERMISSION_SETTINGS_REQUEST_CODE);

    }

    private void showPermissionDeniedDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ChallengeAcceptedActivity.this);
        builder.setTitle(getString(R.string.permission_denied));
        builder.setMessage(getString(R.string.permission_needed_in_order_to_choose_video));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void uploadVideo() {

        progressDialog.setTitle(getString(R.string.uploading));
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if (gotVideoThumbnail) {

            Uri videoUri = Uri.parse(String.valueOf(challengeAcceptedVideoView.getTag()));
            File thumbnailFile = new File(thumbnailStorageDirectory + "/" + thumbnailFileName + ".jpg");

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(thumbnailFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] bytes = baos.toByteArray();

            // Upload To Storage
            UploadTask uploadTaskVideo = challengeStorageReference.putFile(videoUri);
            final UploadTask uploadTaskThumbnail = challengeStorageThumbnailReference.putBytes(bytes);

            // Get Download Url
            Task<Uri> urlTask = uploadTaskVideo.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return challengeStorageReference.getDownloadUrl();

                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            if (task.isSuccessful()) {

                                Uri downloadUri = task.getResult();

                                challengeDocument.update("video_proof", downloadUri.toString())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                // Get Download Url Thumb
                                                Task<Uri> urlTaskThumb = uploadTaskThumbnail.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                            @Override
                                                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                                                if (!task.isSuccessful()) {
                                                                    throw task.getException();
                                                                }

                                                                // Continue with the task to get the download URL
                                                                return challengeStorageThumbnailReference.getDownloadUrl();

                                                            }
                                                        })
                                                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Uri> task) {

                                                                if (task.isSuccessful()) {

                                                                    Uri downloadUri = task.getResult();

                                                                    Map<String, Object> videoMap = new HashMap<>();
                                                                    videoMap.put("video_thumbnail", downloadUri.toString());
                                                                    videoMap.put("accepted", true);

                                                                    challengeDocument.update(videoMap)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {

                                                                                    progressDialog.dismiss();

                                                                                    Toast.makeText(ChallengeAcceptedActivity.this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show();

                                                                                    Intent challengeIntent = new Intent(ChallengeAcceptedActivity.this, ChallengeActivity.class);
                                                                                    challengeIntent.putExtra("challenge_id", challengeID);
                                                                                    startActivity(challengeIntent);
                                                                                    finish();

                                                                                }
                                                                            });

                                                                } else {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(ChallengeAcceptedActivity.this, getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
                                                                }

                                                            }
                                                        });

                                            }
                                        });

                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(ChallengeAcceptedActivity.this, getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

        } else {
            progressDialog.dismiss();
            Toast.makeText(this, getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show();
            takVideoThumbnail();
        }

    }

    private void chooseVideoToUpload() {

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_REQUEST_CODE);

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {

            Uri fileUri = data.getData();

            challengeAcceptedVideoView.setVisibility(View.VISIBLE);
            challengeAcceptedVideoView.setTag(fileUri);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            final int screenWidth = displayMetrics.widthPixels;

            challengeAcceptedVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                    int videoDuration = mp.getDuration() / 1000;

                    if (videoDuration <= 60) {
                        clickedFirstTime = true;
                    } else {
                        clickedFirstTime = false;
                        Toast.makeText(ChallengeAcceptedActivity.this, getString(R.string.video_is_too_long), Toast.LENGTH_SHORT).show();
                    }

                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);

                    ViewGroup.LayoutParams params = challengeAcceptedVideoView.getLayoutParams();
                    params.width = screenWidth;
                    params.height = screenWidth;
                    challengeAcceptedVideoView.setLayoutParams(params);

                }
            });

            final MediaController mediaController = new MediaController(ChallengeAcceptedActivity.this);
            mediaController.setAnchorView(challengeAcceptedVideoView);

            challengeAcceptedVideoView.setMediaController(mediaController);
            challengeAcceptedVideoView.setVideoURI(fileUri);
            challengeAcceptedVideoView.seekTo(300);

            challengeAcceptedVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaController.show();
                }
            });

            takVideoThumbnail();

        }

        if (requestCode == PERMISSION_SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            chooseVideoToUpload();
        }

    }

    private void takVideoThumbnail() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                String dateTime = dateFormat.format(calendar.getTime());

                thumbnailFileName = "THUMB_" + dateTime;

                QuickShot
                        .of(challengeAcceptedVideoView)
                        .setPath(thumbnailStorageDirectory.getPath())
                        .setFilename(thumbnailFileName)
                        .toJPG()
                        .setResultListener(new QuickShot.QuickShotListener() {
                            @Override
                            public void onQuickShotSuccess(String path) {
                                gotVideoThumbnail = true;
                            }

                            @Override
                            public void onQuickShotFailed(String path, String errorMsg) {

                            }
                        })
                        .save();

            }
        }, 500);

    }

}
