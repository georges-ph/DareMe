package ga.jundbits.dareme.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ChallengeModel {

    private String watcher_id, player_id, color, description, prize, video_url, video_thumbnail;
    @ServerTimestamp
    private Date timestamp;
    private boolean accepted, completed, failed;

    public ChallengeModel() {

    }

    public ChallengeModel(String watcher_id, String player_id, String color, String description, String prize, String video_url, String video_thumbnail, boolean accepted, boolean completed, boolean failed) {
        this.watcher_id = watcher_id;
        this.player_id = player_id;
        this.color = color;
        this.description = description;
        this.prize = prize;
        this.video_url = video_url;
        this.video_thumbnail = video_thumbnail;
        this.accepted = accepted;
        this.completed = completed;
        this.failed = failed;
    }

    public String getWatcher_id() {
        return watcher_id;
    }

    public void setWatcher_id(String watcher_id) {
        this.watcher_id = watcher_id;
    }

    public String getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(String player_id) {
        this.player_id = player_id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_thumbnail() {
        return video_thumbnail;
    }

    public void setVideo_thumbnail(String video_thumbnail) {
        this.video_thumbnail = video_thumbnail;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

}
