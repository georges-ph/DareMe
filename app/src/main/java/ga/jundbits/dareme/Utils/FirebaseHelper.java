package ga.jundbits.dareme.Utils;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import ga.jundbits.dareme.Models.Challenge;

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

    public static Task<ShortDynamicLink> createShareUrl(String challengeID, Challenge challenge) {

        final String APP_IMAGE_URL = "https://i.ibb.co/5Ttz167/Dare-Me.png";

        return FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://dareme.page.link?id=" + challengeID))
                .setDomainUriPrefix("https://dareme.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        // TODO: 09-May-24 change bitly links
                        // TODO: 10-May-24 even better! change deep link method
                        //  as firebase dynamic link will shut down on August 2025
                        .setFallbackUrl(Uri.parse("https://bit.ly/3cu9i1t"))
                        .build())
                .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                        .setTitle(challenge.getWatcher_username() + " has challenged " + challenge.getPlayer_username())
                        .setDescription("Check it out!")
                        .setImageUrl(Uri.parse(APP_IMAGE_URL))
                        .build()
                )
                .buildShortDynamicLink();

    }

}

