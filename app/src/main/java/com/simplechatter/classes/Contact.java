package com.simplechatter.classes;

import java.util.Objects;

public class Contact {
    public String name,about,image,uid;

    public Contact()
    {

    }

    public Contact(String name, String about, String image) {
        this.name = name;
        this.about = about;
        this.image = image;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact contact = (Contact) o;
        return getName().equals(contact.getName()) &&
                getAbout().equals(contact.getAbout()) &&
                Objects.equals(getImage(), contact.getImage()) &&
                Objects.equals(getUid(), contact.getUid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAbout(), getImage(), getUid());
    }

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", about='" + about + '\'' +
                ", image='" + image + '\'' +
                ", uid='" + uid + '\'' +
                '}';
    }
}
