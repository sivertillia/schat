package com.sivert.chat.Model;

public class User {
    private String id;
    private String username;
    private String ImageUrl;
    private String status;
    private String search;
    private String official;

    public User(String id, String username, String ImageUrl, String status, String search, String official) {
        this.id = id;
        this.username = username;
        this.ImageUrl = ImageUrl;
        this.status = status;
        this.search = search;
        this.official = official;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return ImageUrl;
    }

    public void setImageURL(String ImageUrl) { this.ImageUrl = ImageUrl; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getSearch() { return search; }

    public void setSearch(String search) { this.search = search; }

    public String getOfficial() { return official; }

    public void setOfficial(String official) { this.official = official; }
}
