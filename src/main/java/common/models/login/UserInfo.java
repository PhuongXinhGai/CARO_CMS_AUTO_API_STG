package common.models.login;

import java.util.List;

public class UserInfo {
    private String cms_user_uid;
    private CourseInfo course_info;
    private String course_uid;
    private CustomizeBookingUI customize_booking_ui;
    private String full_name;
    private String partner_uid;
    private List<String> permissions;
    private String phone;
    private int role_agency_id;
    private int role_id;
    private String role_name;
    private String user_name;

    // Getter + Setter
    public String getCms_user_uid() {
        return cms_user_uid;
    }

    public void setCms_user_uid(String cms_user_uid) {
        this.cms_user_uid = cms_user_uid;
    }

    public CourseInfo getCourse_info() {
        return course_info;
    }

    public void setCourse_info(CourseInfo course_info) {
        this.course_info = course_info;
    }

    public String getCourse_uid() {
        return course_uid;
    }

    public void setCourse_uid(String course_uid) {
        this.course_uid = course_uid;
    }

    public CustomizeBookingUI getCustomize_booking_ui() {
        return customize_booking_ui;
    }

    public void setCustomize_booking_ui(CustomizeBookingUI customize_booking_ui) {
        this.customize_booking_ui = customize_booking_ui;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getPartner_uid() {
        return partner_uid;
    }

    public void setPartner_uid(String partner_uid) {
        this.partner_uid = partner_uid;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getRole_agency_id() {
        return role_agency_id;
    }

    public void setRole_agency_id(int role_agency_id) {
        this.role_agency_id = role_agency_id;
    }

    public int getRole_id() {
        return role_id;
    }

    public void setRole_id(int role_id) {
        this.role_id = role_id;
    }

    public String getRole_name() {
        return role_name;
    }

    public void setRole_name(String role_name) {
        this.role_name = role_name;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }
}
