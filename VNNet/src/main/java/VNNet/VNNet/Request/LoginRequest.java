package VNNet.VNNet.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;

public class LoginRequest {
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("password")
    private String password;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
