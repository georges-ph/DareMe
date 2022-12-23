package ga.jundbits.dareme.Utils;

import android.content.Context;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import ga.jundbits.dareme.R;

public class TimeAgo {

    public String getTimeAgo(Context context, long duration) {

        Date now = new Date();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - duration);
        long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - duration);
        long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - duration);

        if (seconds < 60) {
            return context.getString(R.string.just_now);
        } else if (minutes == 1) {
            return context.getString(R.string.one_minute_ago);
        } else if (minutes > 1 && minutes < 60) {
            return minutes + " " + context.getString(R.string.minutes_ago);
        } else if (hours == 1) {
            return context.getString(R.string.one_hour_ago);
        } else if (hours > 1 && hours < 24) {
            return hours + " " + context.getString(R.string.hours_ago);
        } else if (days == 1) {
            return context.getString(R.string.one_day_ago);
        } else {
            return days + " " + context.getString(R.string.days_ago);
        }

    }

}
