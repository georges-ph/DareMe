package ga.jundbits.dareme.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ga.jundbits.dareme.Adapters.NewChallengePlayersRecyclerAdapter;
import ga.jundbits.dareme.Models.ChallengeModel;
import ga.jundbits.dareme.Models.NewChallengePlayersModel;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.HelperMethods;

public class NewChallengeActivity extends AppCompatActivity implements NewChallengePlayersRecyclerAdapter.ListItemButtonClick /* implements NewChallengePlayersRecyclerAdapter.ListItemButtonClick */ {

    private ConstraintLayout newChallengeConstraintLayout;
    private Toolbar newChallengeToolbar;
    private EditText newChallengePlayerUsername, newChallengeDescription, newChallengePrize;
    private RecyclerView newChallengePlayersRecyclerView;
    private Button newChallengeAddChallengeButton;

    private List<NewChallengePlayersModel> newChallengePlayersModelList;
    private NewChallengePlayersRecyclerAdapter newChallengePlayersRecyclerAdapter;

    private String currentUserID;
    private String selectedPlayerUsername;
    private String playerUserID;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_challenge);

        initVars();
        setupToolbar();
        setupAdapter();
        loadData();
        setOnClicks();

    }

    private void initVars() {

        newChallengeConstraintLayout = findViewById(R.id.new_challenge_constraint_layout);
        newChallengeToolbar = findViewById(R.id.new_challenge_toolbar);
        newChallengePlayerUsername = findViewById(R.id.new_challenge_player_username);
        newChallengeDescription = findViewById(R.id.new_challenge_description);
        newChallengePrize = findViewById(R.id.new_challenge_prize);
        newChallengePlayersRecyclerView = findViewById(R.id.new_challenge_players_recycler_view);
        newChallengeAddChallengeButton = findViewById(R.id.new_challenge_add_challenge_button);

        progressDialog = new ProgressDialog(NewChallengeActivity.this);

        newChallengePlayersModelList = new ArrayList<>();

    }

    private void setupToolbar() {

        setSupportActionBar(newChallengeToolbar);
        getSupportActionBar().setTitle(getString(R.string.new_challenge));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void setupAdapter() {

        newChallengePlayersRecyclerAdapter = new NewChallengePlayersRecyclerAdapter(this, newChallengePlayersModelList, this);

        newChallengePlayersRecyclerView.setHasFixedSize(true);
        newChallengePlayersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newChallengePlayersRecyclerView.setAdapter(newChallengePlayersRecyclerAdapter);

    }

    private void loadData() {

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
                    filter(s.toString().toLowerCase().trim());
                }

            }
        });

    }

    private void filter(String username) {

        HelperMethods.usersCollectionRef(this)
                .whereEqualTo("type", "player")
                .get().addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (queryDocumentSnapshots.isEmpty()) {
                            newChallengePlayersRecyclerView.setVisibility(View.GONE);
                            return;
                        }
                        newChallengePlayersRecyclerView.setVisibility(View.VISIBLE);

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                            NewChallengePlayersModel newChallengePlayersModel = documentSnapshot.toObject(NewChallengePlayersModel.class);

                            if (newChallengePlayersModel.getUsername().contains(username) && !newChallengePlayersModelList.contains(newChallengePlayersModel))
                                newChallengePlayersModelList.add(newChallengePlayersModel);

                        }

                        newChallengePlayersRecyclerAdapter.updateList(newChallengePlayersModelList);

                    }
                });

    }

    private void setOnClicks() {

        newChallengeAddChallengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HelperMethods.closeKeyboard(NewChallengeActivity.this);

                String playerUsername = newChallengePlayerUsername.getText().toString().trim();
                String description = newChallengeDescription.getText().toString().trim();
                String prize = newChallengePrize.getText().toString().trim();

                if (TextUtils.isEmpty(playerUsername) || TextUtils.isEmpty(description) || TextUtils.isEmpty(prize)) {

                    if (TextUtils.isEmpty(playerUsername)) {
                        HelperMethods.showError(newChallengeConstraintLayout, getString(R.string.player_s_username_cannot_be_empty));
                    } else if (TextUtils.isEmpty(description)) {
                        HelperMethods.showError(newChallengeConstraintLayout, getString(R.string.challenge_description_cannot_be_empty));
                    } else if (TextUtils.isEmpty(prize)) {
                        HelperMethods.showError(newChallengeConstraintLayout, getString(R.string.challenge_prize_cannot_be_empty));
                    }

                    return;

                }

                if (!playerUsername.equals(selectedPlayerUsername)) {
                    HelperMethods.showError(newChallengeConstraintLayout, getString(R.string.username_not_found));
                    return;
                }

                progressDialog.setMessage(getString(R.string.please_wait));
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                String[] array = getResources().getStringArray(R.array.colors);
                String randomColor = array[new Random().nextInt(array.length)];

                ChallengeModel challengeModel = new ChallengeModel(currentUserID,
                        playerUserID,
                        randomColor,
                        description,
                        prize,
                        null,
                        null,
                        false,
                        false,
                        false);

                HelperMethods.challengesCollectionRef(NewChallengeActivity.this)
                        .add(challengeModel)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                progressDialog.dismiss();

                                Toast.makeText(NewChallengeActivity.this, "Challenge added successfully", Toast.LENGTH_SHORT).show();

                                Intent challengeIntent = new Intent(NewChallengeActivity.this, ChallengeActivity.class);
                                challengeIntent.putExtra("challenge_id", documentReference.getId());
                                startActivity(challengeIntent);
                                finish();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                progressDialog.dismiss();
                                HelperMethods.showError(newChallengeConstraintLayout, e.getMessage());

                            }
                        });

            }
        });

    }

    @Override
    public void onListItemButtonClick(String id, String username) {
        playerUserID = id;
        selectedPlayerUsername = username;
        newChallengePlayerUsername.setText(username);
        newChallengePlayersRecyclerView.setVisibility(View.GONE);
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

}
