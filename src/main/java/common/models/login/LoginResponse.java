package common.models.login;

public class LoginResponse {
    private UserInfo data;
    private boolean must_change_password;
    private String token;

    public UserInfo getData() {
        return data;
    }

    public void setData(UserInfo data) {
        this.data = data;
    }

    public boolean isMust_change_password() {
        return must_change_password;
    }

    public void setMust_change_password(boolean must_change_password) {
        this.must_change_password = must_change_password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
