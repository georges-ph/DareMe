package ga.jundbits.dareme.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ga.jundbits.dareme.Models.NewChallengePlayersModel;
import ga.jundbits.dareme.Adapters.NewChallengePlayersRecyclerAdapter;
import ga.jundbits.dareme.R;
import github.nisrulz.easydeviceinfo.base.EasyNetworkMod;

public class NewChallengeActivity extends AppCompatActivity implements NewChallengePlayersRecyclerAdapter.ListItemButtonClick /* implements NewChallengePlayersRecyclerAdapter.ListItemButtonClick */ {

    ConstraintLayout noConnectionLayout;

    ConstraintLayout newChallengeConstraintLayout;
    Toolbar newChallengeToolbar;
    EditText newChallengePlayerUsername, newChallengeDescription, newChallengePrize;
    RecyclerView newChallengePlayersRecyclerView;
    Button newChallengeAddChallengeButton;

    List<NewChallengePlayersModel> newChallengePlayersModelList;
    NewChallengePlayersRecyclerAdapter newChallengePlayersRecyclerAdapter;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    String currentUserID;
    String currentUserImage;
    String currentUserUsername;
    String playerUserID;

    DocumentReference currentUserDocument;

    boolean playerUsernameAvailable = false;

    Vibrator vibrator;

    ProgressDialog progressDialog;

    EasyNetworkMod easyNetworkMod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_challenge);

        noConnectionLayout = findViewById(R.id.no_connection_layout);

        newChallengeConstraintLayout = findViewById(R.id.new_challenge_constraint_layout);
        newChallengeToolbar = findViewById(R.id.new_challenge_toolbar);
        newChallengePlayerUsername = findViewById(R.id.new_challenge_player_username);
        newChallengeDescription = findViewById(R.id.new_challenge_description);
        newChallengePrize = findViewById(R.id.new_challenge_prize);
        newChallengePlayersRecyclerView = findViewById(R.id.new_challenge_players_recycler_view);
        newChallengeAddChallengeButton = findViewById(R.id.new_challenge_add_challenge_button);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        currentUserID = firebaseUser.getUid();

        currentUserDocument = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        progressDialog = new ProgressDialog(NewChallengeActivity.this);

        easyNetworkMod = new EasyNetworkMod(this);

        setSupportActionBar(newChallengeToolbar);
        getSupportActionBar().setTitle(getString(R.string.new_challenge));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getCurrentUserImage(currentUserDocument);
        getCurrentUserUsername(currentUserDocument);

        newChallengePlayersModelList = new ArrayList<>();

        newChallengePlayersRecyclerAdapter = new NewChallengePlayersRecyclerAdapter(this, newChallengePlayersModelList, this);

        newChallengePlayersRecyclerView.setHasFixedSize(true);
        newChallengePlayersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newChallengePlayersRecyclerView.setAdapter(newChallengePlayersRecyclerAdapter);

        loadPlayers();

        newChallengePlayerUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().isEmpty()) {
                    newChallengePlayersRecyclerView.setVisibility(View.GONE);
                } else {
                    newChallengePlayersRecyclerView.setVisibility(View.VISIBLE);
                    filter(s.toString().toLowerCase());
                }

                checkPlayerUsername(s.toString());

            }
        });

        newChallengeAddChallengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                closeKeyboard();

                String playerUsername = newChallengePlayerUsername.getText().toString().trim();
                String description = newChallengeDescription.getText().toString().trim();
                String prize = newChallengePrize.getText().toString().trim();

                if (!TextUtils.isEmpty(playerUsername) && !TextUtils.isEmpty(description) && !TextUtils.isEmpty(prize)) {

                    if (easyNetworkMod.isNetworkAvailable()) {

                        if (playerUsernameAvailable) {

                            progressDialog.setMessage(getString(R.string.please_wait));
                            progressDialog.setCancelable(false);
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();

                            String[] array = getResources().getStringArray(R.array.colors);
                            String randomColor = array[new Random().nextInt(array.length)];

                            Timestamp timestamp = Timestamp.now();

                            long timestampSeconds = timestamp.getSeconds();
                            long timestampNanoSeconds = timestamp.getNanoseconds();
                            long timestampSecondsToMillis = TimeUnit.SECONDS.toMillis(timestampSeconds);
                            long timestampNanoSecondsToMillis = TimeUnit.NANOSECONDS.toMillis(timestampNanoSeconds);
                            long timestampTotalMillis = timestampSecondsToMillis + timestampNanoSecondsToMillis;

                            Map<String, Object> challengeMap = new HashMap<>();
                            challengeMap.put("image", currentUserImage);
                            challengeMap.put("username", currentUserUsername);
                            challengeMap.put("challenges_username", playerUsername);
                            challengeMap.put("color", randomColor);
                            challengeMap.put("text", description);
                            challengeMap.put("prize", prize);
                            challengeMap.put("user_id", currentUserID);
                            challengeMap.put("player_user_id", playerUserID);
                            challengeMap.put("timestamp", FieldValue.serverTimestamp());
                            challengeMap.put("date_time_millis", timestampTotalMillis);
                            challengeMap.put("accepted", false);
                            challengeMap.put("completed", false);
                            challengeMap.put("failed", false);
                            challengeMap.put("video_thumbnail", null);
                            challengeMap.put("video_proof", null);

                            firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections")
                                    .collection("Challenges")
                                    .add(challengeMap)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {

                                            if (task.isSuccessful()) {

                                                progressDialog.dismiss();

                                                Toast.makeText(NewChallengeActivity.this, "Challenge added successfully", Toast.LENGTH_SHORT).show();

                                                Intent challengeIntent = new Intent(NewChallengeActivity.this, ChallengeActivity.class);
                                                challengeIntent.putExtra("challenge_id", task.getResult().getId());
                                                startActivity(challengeIntent);
                                                finish();

                                            } else {
                                                progressDialog.dismiss();
                                                showError(task.getException().getMessage());
                                            }

                                        }
                                    });

                        } else {
                            showError(getString(R.string.username_not_found));
                        }

                    } else {
                        noConnectionAvailable();
                    }

                } else {

                    if (TextUtils.isEmpty(playerUsername)) {
                        showError(getString(R.string.player_s_username_cannot_be_empty));
                    } else if (TextUtils.isEmpty(description)) {
                        showError(getString(R.string.challenge_description_cannot_be_empty));
                    } else if (TextUtils.isEmpty(prize)) {
                        showError(getString(R.string.challenge_prize_cannot_be_empty));
                    }

                }

            }
        });

    }

    private void closeKeyboard() {

        View closeKeyboardView = NewChallengeActivity.this.getCurrentFocus();
        if (closeKeyboardView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(closeKeyboardView.getWindowToken(), 0);
        }

    }

    private void showError(String errorMessage) {

        vibrator.vibrate(500);
        Snackbar.make(newChallengeConstraintLayout, errorMessage, Snackbar.LENGTH_SHORT).show();

    }

    private void filter(String username) {

        List<NewChallengePlayersModel> list = new ArrayList<>();

        for (NewChallengePlayersModel newChallengePlayersModel : newChallengePlayersModelList) {

            if (newChallengePlayersModel.getUsername().toLowerCase().contains(username)) {
                newChallengePlayersRecyclerView.setVisibility(View.VISIBLE);
                list.add(newChallengePlayersModel);
            } else {
                newChallengePlayersRecyclerView.setVisibility(View.GONE);
            }

        }

        newChallengePlayersRecyclerAdapter.updateList(list);

    }

    private void loadPlayers() {

        newChallengePlayersModelList.clear();

        Query playersQuery = firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").whereEqualTo("type", "player");
        playersQuery.addSnapshotListener(NewChallengeActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                    if (documentChange.getType() == DocumentChange.Type.ADDED) {

                        NewChallengePlayersModel newChallengePlayersModel = documentChange.getDocument().toObject(NewChallengePlayersModel.class);
                        newChallengePlayersModelList.add(newChallengePlayersModel);

                        newChallengePlayersRecyclerAdapter.notifyDataSetChanged();

                    }

                }

            }
        });

    }

    @Override
    public void onListItemButtonClick(String username) {
        newChallengePlayerUsername.setText(username);
        newChallengePlayersRecyclerView.setVisibility(View.GONE);
        checkPlayerUsername(username);
    }

    private void noConnectionAvailable() {

        noConnectionLayout.setVisibility(View.VISIBLE);

        noConnectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (easyNetworkMod.isNetworkAvailable()) {
                    noConnectionLayout.setVisibility(View.GONE);
                }

            }
        });

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

    public boolean checkPlayerUsername(final String username) {

        firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").whereEqualTo("username", username)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (queryDocumentSnapshots.isEmpty()) {
                    playerUsernameAvailable = false;
                } else {
                    playerUsernameAvailable = true;
                    getPlayerUserID(username);
                }

            }
        });

        return playerUsernameAvailable;

    }

    public String getCurrentUserImage(DocumentReference currentUserDocument) {

        currentUserDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                currentUserImage = documentSnapshot.getString("image");

            }
        });

        return currentUserImage;

    }

    public String getCurrentUserUsername(DocumentReference currentUserDocument) {

        currentUserDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                currentUserUsername = documentSnapshot.getString("username");

            }
        });

        return currentUserUsername;

    }

    public String getPlayerUserID(String playerUsername) {

        firebaseFirestore.collection(getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").whereEqualTo("username", playerUsername)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                        playerUserID = documentSnapshot.getString("id");

                    }

                }

            }
        });

        return currentUserUsername;

    }

}
