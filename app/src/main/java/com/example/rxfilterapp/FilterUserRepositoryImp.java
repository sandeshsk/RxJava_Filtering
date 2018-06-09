package com.example.rxfilterapp;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;

public class FilterUserRepositoryImp implements FilterUserRepository {

    private final FilterUserDao filterUserDao;

    public FilterUserRepositoryImp(FilterUserDao filterUserDao){
        this.filterUserDao=filterUserDao;
    }


    @Override
    public Observable<List<FilterUser>> getFilterUser(final String filterString) {

        return Observable.fromCallable(new Callable<List<FilterUser>>() {
            @Override
            public List<FilterUser> call() throws Exception {
                return filterUserDao.getFilteredUser(filterString);
            }
        });
    }

    @Override
    public void insertUsers(List<FilterUser> filterUsers) {
        List<FilterUser> list=filterUserDao.getFilteredUsers();

        filterUserDao.insertFilterUser(filterUsers);
    }

    @Override
    public void deleteUsers() {
        filterUserDao.deleteFilterUser();
    }
}
