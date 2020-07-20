package com.simplechatter.classes;

import java.util.Objects;

public class GroupContacts {
    String name,motto;

    public GroupContacts(String name, String motto) {
        this.name = name;
        this.motto = motto;
    }

    public GroupContacts() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMotto() {
        return motto;
    }

    public void setMotto(String motto) {
        this.motto = motto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupContacts)) return false;
        GroupContacts that = (GroupContacts) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getMotto(), that.getMotto());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getMotto());
    }

    @Override
    public String toString() {
        return "GroupContacts{" +
                "name='" + name + '\'' +
                ", motto='" + motto + '\'' +
                '}';
    }
}
