package ga.jundbits.dareme.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Challenge {

    private String watcher_id, watcher_username, watcher_image, player_id, player_username, player_image;
    private String color, description, rewards, video_url;
    @ServerTimestamp
    private Date timestamp;
    private boolean completed, failed;
    private List<String> likes;
    private int share_count;

    public Challenge() {

    }

    public Challenge(String watcher_id, String watcher_username, String watcher_image, String player_id, String player_username, String player_image, String color, String description, String rewards, String video_url, boolean completed, boolean failed, List<String> likes, int share_count) {
        this.watcher_id = watcher_id;
        this.watcher_username = watcher_username;
        this.watcher_image = watcher_image;
        this.player_id = player_id;
        this.player_username = player_username;
        this.player_image = player_image;
        this.color = color;
        this.description = description;
        this.rewards = rewards;
        this.video_url = video_url;
        this.completed = completed;
        this.failed = failed;
        this.likes = likes;
        this.share_count = share_count;
    }

    public String getWatcher_id() {
        return watcher_id;
    }

    public void setWatcher_id(String watcher_id) {
        this.watcher_id = watcher_id;
    }

    public String getWatcher_username() {
        return watcher_username;
    }

    public void setWatcher_username(String watcher_username) {
        this.watcher_username = watcher_username;
    }

    public String getWatcher_image() {
        return watcher_image;
    }

    public void setWatcher_image(String watcher_image) {
        this.watcher_image = watcher_image;
    }

    public String getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(String player_id) {
        this.player_id = player_id;
    }

    public String getPlayer_username() {
        return player_username;
    }

    public void setPlayer_username(String player_username) {
        this.player_username = player_username;
    }

    public String getPlayer_image() {
        return player_image;
    }

    public void setPlayer_image(String player_image) {
        this.player_image = player_image;
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

    public String getRewards() {
        return rewards;
    }

    public void setRewards(String rewards) {
        this.rewards = rewards;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    public int getShare_count() {
        return share_count;
    }

    public void setShare_count(int share_count) {
        this.share_count = share_count;
    }

}
