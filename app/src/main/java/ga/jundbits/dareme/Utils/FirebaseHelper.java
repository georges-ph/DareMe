package ga.jundbits.dareme.Utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseHelper {

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    public static CollectionReference collectionReference(String path) {
        return FirebaseFirestore.getInstance().collection("DareMe/AppCollections/" + path);
    }

    public static DocumentReference documentReference(String path) {
        return FirebaseFirestore.getInstance().document("DareMe/AppCollections/" + path);
    }

    public static StorageReference storageReference(String path) {
        return FirebaseStorage.getInstance().getReference("DareMe/" + path);
    }

    public static DatabaseReference databaseReference(String path) {
        return FirebaseDatabase.getInstance().getReference("DareMe").child(path);
    }

}

