package com.example.rxfilterapp;

import java.util.List;

import io.reactivex.Observable;

public interface FilterUserRepository {

    Observable<List<FilterUser>> getFilterUser(String filterString);

    void insertUsers(List<FilterUser> filterUsers);
    void deleteUsers();
}
