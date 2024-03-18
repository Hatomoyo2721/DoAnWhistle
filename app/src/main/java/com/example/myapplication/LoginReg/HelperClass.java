package com.example.myapplication.LoginReg;

public class HelperClass {
    private String name, username, email, password, confirmPass, role;

    public HelperClass(String name, String username, String email, String password, String confirmPassword, String role) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPass = confirmPassword;
        this.role = role;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPass() {
        return confirmPass;
    }

    public void setConfirmPass(String confirmPass) {
        this.confirmPass = confirmPass;
    }

    public HelperClass() {

    }
}
