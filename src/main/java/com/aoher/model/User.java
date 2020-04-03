package com.aoher.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@Entity
@NamedQueries({
        @NamedQuery(name = "user.list", query = "select u from User u order by u.fullName")
})
@XmlRootElement(name = "user")
public class User implements Cloneable {

    @Id
    @GeneratedValue
    private long id;
    private String fullName;
    private String password;
    private String email;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public User clone() {
        User user = new User();
        user.setEmail(getEmail());
        user.setFullName(getFullName());
        user.setPassword(getPassword());
        user.setId(getId());
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                Objects.equals(fullName, user.fullName) &&
                Objects.equals(password, user.password) &&
                Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fullName, password, email);
    }
}
