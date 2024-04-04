package ga.jundbits.dareme.Models;

import java.util.Objects;

public class NewChallengePlayersModel {

    private String id, image, username;

    public NewChallengePlayersModel() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public NewChallengePlayersModel(String id, String image, String username) {
        this.id = id;
        this.image = image;
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewChallengePlayersModel that = (NewChallengePlayersModel) o;
        return Objects.equals(id, that.id) && Objects.equals(image, that.image) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, image, username);
    }
}
