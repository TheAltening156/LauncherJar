package fr.altening.launcher;

public class Auth {
	private final String username;
    private final String uuid;
    private final String accessToken;
    private final boolean isMicrosoft;

    public Auth(String username, String uuid, String accessToken, boolean isMicrosoft) {
        this.isMicrosoft = isMicrosoft;
        this.username = username;
        this.uuid = uuid;
        this.accessToken = isMicrosoft ? accessToken : "0";
    }

    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean isMicrosoftAccount() {
        return isMicrosoft;
    }
}
