package VNNet.VNNet.Request;

import VNNet.VNNet.Model.User;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StudentRegisterRequest {
    @JsonProperty("name")
    private String name;
    @JsonProperty("birthDate")
    private String birthDate;
    @JsonProperty("gender")
    private String gender;
    @JsonProperty("class_name")
    private String class_name;
    @JsonProperty("userId")
    private Long userId;
    @JsonProperty("address")
    private String address;
    @JsonProperty("teacherId")
    private Long teacherId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
}
