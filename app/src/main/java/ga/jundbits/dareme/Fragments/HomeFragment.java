package ga.jundbits.dareme.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ga.jundbits.dareme.Activities.ChallengeAcceptedActivity;
import ga.jundbits.dareme.Activities.ChallengeActivity;
import ga.jundbits.dareme.Adapters.MainHomeChallengesRecyclerAdapter;
import ga.jundbits.dareme.Models.MainHomeChallengesModel;
import ga.jundbits.dareme.R;
import github.nisrulz.easydeviceinfo.base.EasyNetworkMod;

public class HomeFragment extends Fragment implements MainHomeChallengesRecyclerAdapter.ListItemButtonClick {

    OpenCommentsBox openCommentsBox;
    NoConnection noConnection;

    SwipeRefreshLayout mainHomeSwipeRefreshLayout;
    RecyclerView mainHomeChallengesRecyclerView;

    MainHomeChallengesRecyclerAdapter mainHomeChallengesRecyclerAdapter;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage;
    FirebaseDynamicLinks firebaseDynamicLinks;

    String currentUserID;

    DocumentReference currentUserDocument;
    DocumentReference challengeDocument;
    StorageReference challengeStorageReference;

    ImageButton likeButton;

    ProgressDialog progressDialog;

    EasyNetworkMod easyNetworkMod;

    SharedPreferences challengePreferences;
    SharedPreferences.Editor editor;

    public static final String APP_IMAGE_URL = "https://i.ibb.co/5Ttz167/Dare-Me.png";

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainHomeSwipeRefreshLayout = view.findViewById(R.id.main_home_swipe_refresh_layout);
        mainHomeChallengesRecyclerView = view.findViewById(R.id.main_home_challenges_recycler_view);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDynamicLinks = FirebaseDynamicLinks.getInstance();

        currentUserID = firebaseUser.getUid();

        currentUserDocument = firebaseFirestore.collection(getContext().getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);

        progressDialog = new ProgressDialog(getContext());

        easyNetworkMod = new EasyNetworkMod(getContext());

        challengePreferences = getContext().getSharedPreferences("Challenge Preferences", Context.MODE_PRIVATE);

        Query query = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Challenges").orderBy("timestamp", Query.Direction.DESCENDING);

        PagingConfig config = new PagingConfig(3, 5, false, 10);

        FirestorePagingOptions<MainHomeChallengesModel> options = new FirestorePagingOptions.Builder<MainHomeChallengesModel>()
                .setLifecycleOwner(this)
                .setQuery(query, config, MainHomeChallengesModel.class)
                .build();

        mainHomeChallengesRecyclerAdapter = new MainHomeChallengesRecyclerAdapter(options, getContext(), this);

        mainHomeChallengesRecyclerView.setHasFixedSize(true);
        mainHomeChallengesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mainHomeChallengesRecyclerView.setAdapter(mainHomeChallengesRecyclerAdapter);

        mainHomeSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (easyNetworkMod.isNetworkAvailable()) {
                    mainHomeChallengesRecyclerAdapter.refresh();
                    mainHomeSwipeRefreshLayout.setRefreshing(false);
                } else {
                    noConnection.noConnection();
                }

            }
        });

    }

    @Override
    public void onListItemButtonClick(String buttonName, String username, String challengesUsername, String challengeID, int challengePosition) {

        challengeDocument = firebaseFirestore.collection(getContext().getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Challenges").document(challengeID);

        likeButton = getView().findViewById(R.id.main_home_challenge_list_item_like_button);

        switch (buttonName) {

            case "card view":
                goToChallengeActivity(challengeID, challengePosition);
                break;

            case "accept":
                acceptChallenge(challengeID, challengePosition);
                break;

            case "reject":
                rejectChallenge(challengePosition);
                break;

            case "completed":
                completedChallenge(challengePosition);
                break;

            case "failed":
                failedChallenge(challengePosition);
                break;

            case "comment":
                openCommentsBox.onCommentClicked(challengeID);
                break;

            case "share":
                shareChallenge(challengeID, username, challengesUsername);
                break;

        }

    }

    private void shareChallenge(final String challengeID, final String username, final String challengesUsername) {

        challengeDocument = firebaseFirestore.collection(getContext().getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Challenges").document(challengeID);
        challengeStorageReference = firebaseStorage.getReference().child(getString(R.string.app_name)).child("Challenges").child(challengeID).child(challengeID);

        if (easyNetworkMod.isNetworkAvailable()) {

            progressDialog.setMessage(getContext().getString(R.string.please_wait));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            firebaseFirestore.collection(getContext().getString(R.string.app_name_no_spaces)).document("AppCollections")
                    .collection("ShareableLinks").whereEqualTo("challenge_id", challengeID)
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                    if (queryDocumentSnapshots.isEmpty()) {

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] bytes = baos.toByteArray();

                        // Upload To Storage
                        UploadTask uploadTask = challengeStorageReference.putBytes(bytes);

                        // Get Download Url
                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                                            createLink(downloadUri, challengeID, username, challengesUsername);

                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(getContext(), getString(R.string.error_sharing_the_challenge), Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });

                    } else {

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                            firebaseFirestore.collection(getContext().getString(R.string.app_name_no_spaces)).document("AppCollections")
                                    .collection("ShareableLinks").document(documentSnapshot.getId())
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    String storageLink = documentSnapshot.getString("storage_link");
                                    createLink(Uri.parse(storageLink), challengeID, username, challengesUsername);

                                }
                            });

                        }

                    }

                }
            });

        } else {
            noConnection.noConnection();
        }

    }

    private void createLink(final Uri downloadUri, final String challengeID, String username, String challengesUsername) {

        DynamicLink dynamicLink = firebaseDynamicLinks.createDynamicLink()
                .setLink(downloadUri)
                .setDomainUriPrefix("https://dareme.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder()
                                .setFallbackUrl(Uri.parse("https://bit.ly/3cu9i1t"))
                                .build()
                )
                .setIosParameters(
                        new DynamicLink.IosParameters.Builder(getContext().getPackageName())
                                .setFallbackUrl(Uri.parse("https://bit.ly/3cu9i1t"))
                                .build()
                )
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(username + " has challenged " + challengesUsername)
                                .setDescription("Check it out!")
                                .setImageUrl(Uri.parse(APP_IMAGE_URL))
                                .build()
                )
                .buildDynamicLink();

        final Uri longDynamicLinkUri = dynamicLink.getUri();

        Task<ShortDynamicLink> shortDynamicLinkTask = firebaseDynamicLinks.createDynamicLink()
                .setLongLink(longDynamicLinkUri)
                .buildShortDynamicLink()
                .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {

                        if (task.isSuccessful()) {

                            final Uri shortLink = task.getResult().getShortLink();

                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy, hh:mm:ss a, zz");
                            final String dateTime = dateFormat.format(calendar.getTime());

                            Map<String, Object> shareMap = new HashMap<>();
                            shareMap.put("storage_link", downloadUri.toString());
                            shareMap.put("long_link", longDynamicLinkUri.toString());
                            shareMap.put("short_link", shortLink.toString());
                            shareMap.put("generated_by", currentUserID);
                            shareMap.put("challenge_id", challengeID);
                            shareMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                    .collection("ShareableLinks").document(dateTime)
                                    .set(shareMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Intent sendIntent = new Intent();
                                            sendIntent.setAction(Intent.ACTION_SEND);
                                            sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                                            sendIntent.setType("text/plain");

                                            progressDialog.dismiss();

                                            Intent shareIntent = Intent.createChooser(sendIntent, null);
                                            startActivity(shareIntent);

                                        }
                                    });

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), getString(R.string.error_sharing_the_challenge), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

    private void acceptChallenge(String challengeID, int challengePosition) {

        editor = challengePreferences.edit();
        editor.putInt("position", challengePosition);
        editor.apply();

        Intent challengeAcceptedIntent = new Intent(getContext(), ChallengeAcceptedActivity.class);
        challengeAcceptedIntent.putExtra("challenge_id", challengeID);
        getContext().startActivity(challengeAcceptedIntent);

    }

    private void goToChallengeActivity(String challengeID, int challengePosition) {

        editor = challengePreferences.edit();
        editor.putInt("position", challengePosition);
        editor.apply();

        Intent challengeIntent = new Intent(getContext(), ChallengeActivity.class);
        challengeIntent.putExtra("challenge_id", challengeID);
        getContext().startActivity(challengeIntent);

    }

    private void failedChallenge(final int challengePosition) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getString(R.string.confirm_challenge_failure));
        builder.setMessage(getContext().getString(R.string.do_you_want_to_confirm_that_the_challenge_is_failed));
        builder.setPositiveButton(getContext().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (easyNetworkMod.isNetworkAvailable()) {

                    challengeDocument.update("failed", true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    if (easyNetworkMod.isNetworkAvailable()) {
                                        mainHomeChallengesRecyclerAdapter.refresh();
                                    } else {
                                        noConnection.noConnection();
                                    }

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mainHomeChallengesRecyclerView.smoothScrollToPosition(challengePosition);
                                        }
                                    }, 1000);

                                }
                            });

                } else {
                    noConnection.noConnection();
                }

            }
        });
        builder.setNegativeButton(getContext().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void completedChallenge(final int challengePosition) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getString(R.string.confirm_challenge_completion));
        builder.setMessage(getContext().getString(R.string.do_you_want_to_confirm_that_the_challenge_is_completed));
        builder.setPositiveButton(getContext().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (easyNetworkMod.isNetworkAvailable()) {

                    challengeDocument.update("completed", true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    challengeDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(final DocumentSnapshot documentSnapshot) {

                                            String challengesUsername = documentSnapshot.getString("challenges_username");

                                            currentUserDocument.collection("CompletedChallenges").document(challengeDocument.getId())
                                                    .set(documentSnapshot.getData());

                                            firebaseFirestore.collection(getContext().getString(R.string.app_name_no_spaces)).document("AppCollections")
                                                    .collection("Users").whereEqualTo("username", challengesUsername)
                                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                    if (!queryDocumentSnapshots.isEmpty()) {

                                                        for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots.getDocuments()) {

                                                            firebaseFirestore.collection(getContext().getString(R.string.app_name_no_spaces)).document("AppCollections")
                                                                    .collection("Users").document(documentSnapshot1.getId())
                                                                    .collection("CompletedChallenges").document(challengeDocument.getId())
                                                                    .set(documentSnapshot.getData());

                                                        }

                                                    }

                                                }
                                            });

                                        }
                                    });

                                    if (easyNetworkMod.isNetworkAvailable()) {
                                        mainHomeChallengesRecyclerAdapter.refresh();
                                    } else {
                                        noConnection.noConnection();
                                    }

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mainHomeChallengesRecyclerView.smoothScrollToPosition(challengePosition);
                                        }
                                    }, 1000);

                                }
                            });

                } else {
                    noConnection.noConnection();
                }

            }
        });
        builder.setNegativeButton(getContext().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void rejectChallenge(final int challengePosition) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getString(R.string.reject_challenge));
        builder.setMessage(getContext().getString(R.string.rejecting_the_challenge_will_mark_this_challenge_as_failed_this_action_cannot_be_undone_do_you_want_to_proceed));
        builder.setPositiveButton(getContext().getString(R.string.reject), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (easyNetworkMod.isNetworkAvailable()) {

                    challengeDocument.update("failed", true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    if (easyNetworkMod.isNetworkAvailable()) {
                                        mainHomeChallengesRecyclerAdapter.refresh();
                                    } else {
                                        noConnection.noConnection();
                                    }

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mainHomeChallengesRecyclerView.smoothScrollToPosition(challengePosition);
                                        }
                                    }, 1000);

                                }
                            });

                } else {
                    noConnection.noConnection();
                }

            }
        });
        builder.setNegativeButton(getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public void setOnCommentsOpened(OpenCommentsBox openCommentsBox) {
        this.openCommentsBox = openCommentsBox;
    }

    public void setOnNoConnection(NoConnection noConnection) {
        this.noConnection = noConnection;
    }

    public interface OpenCommentsBox {
        void onCommentClicked(String challengeID);
    }

    public interface NoConnection {
        void noConnection();
    }

    @Override
    public void onResume() {
        super.onResume();

        final int challengePosition = challengePreferences.getInt("position", -1);

        if (challengePosition != -1) {

            mainHomeChallengesRecyclerAdapter.refresh();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainHomeChallengesRecyclerView.smoothScrollToPosition(challengePosition);
                }
            }, 1000);

            editor = challengePreferences.edit();
            editor.clear();
            editor.apply();

        }

    }

}
