package com.example.rxfilterapp;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface FilterUserDao {

    @Insert
    public void insertFilterUser(List<FilterUser> filterUsers);

    @Query("SELECT * from FilterUser where userName like :filterString")
    List<FilterUser> getFilteredUser(String filterString);


    @Query("SELECT * from Filteruser")
    List<FilterUser> getFilteredUsers();

    @Query("DELETE FROM Filteruser")
    void deleteFilterUser();
}
