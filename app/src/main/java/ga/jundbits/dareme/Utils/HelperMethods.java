package ga.jundbits.dareme.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.Random;

import ga.jundbits.dareme.Models.User;
import ga.jundbits.dareme.R;

public class HelperMethods {

    private static User currentUser;

    public static void setCurrentUser(User user) {
        HelperMethods.currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void showKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void closeKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showError(ConstraintLayout layout, String errorMessage) {
        Vibrator vibrator = (Vibrator) layout.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
        Snackbar.make(layout, errorMessage, Snackbar.LENGTH_SHORT).show();
    }

    public static String randomColor(Context context) {
        String[] array = context.getResources().getStringArray(R.array.colors);
        return array[new Random().nextInt(array.length)];
    }

    public static int getScreenWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

}
