package com.example.rxfilterapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Completable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class RxFilterActivity extends AppCompatActivity {

    @BindView(R.id.editText)
    EditText editText;

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    private FilterUserRepository filterUserRepository;

    private Unbinder unbinder;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<FilterUser> filterUserList;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_filter);
        unbinder = ButterKnife.bind(this);
        FilterUserDao filterUserDao = MyDatabase.getAppDatabase(this).getFilterUserDao();
        filterUserRepository = new FilterUserRepositoryImp(filterUserDao);
        filterUserList = new ArrayList<>();
        setAdapter();
        insertUsers();
        createFilterObservable();

    }

    private void setAdapter() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
    }

    //added observable from the rx binding
    private void createFilterObservable() {
        compositeDisposable.add(RxTextView.textChanges(editText)
                .skipInitialValue()
                .subscribeOn(Schedulers.io())
                .debounce(400, TimeUnit.MILLISECONDS)
                .switchMap(new Function<CharSequence, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(CharSequence charSequence) throws Exception {
                       /* Can call server for filtering data here
                        and can handle response and converting response object
                        to your model using map operator*/

                        if (TextUtils.isEmpty(charSequence)) {
                            return filterUserRepository.getFilterUser(charSequence.toString());
                        }
                        return filterUserRepository.getFilterUser("%" + charSequence + "%");
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver()));
    }


    //inserting in io thread- creates 1000 users in FilterUser table
    private void insertUsers() {
        compositeDisposable.add(Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {

                List<FilterUser> filterUsers = new ArrayList<>();
                Random r = new Random();
                String alphabet = "abcdefghijklmnopqrstuvwxyz";
                for (int j = 0; j < 1000; j++) {
                    final int N = 6;
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < N; i++) {
                        sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
                    }
                    String randomName = sb.toString();
                    FilterUser filterUser = new FilterUser();
                    filterUser.setUserName(randomName);
                    filterUsers.add(filterUser);
                }
                filterUserRepository.insertUsers(filterUsers);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).
                        subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                //Do nothing
                            }

                            @Override
                            public void onError(Throwable e) {
                                //Do nothing
                            }
                        }));

    }


    //adapter for recycler view
    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.filter_user_item, parent, false);

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            holder.textView.setText(filterUserList.get(position).getUserName());
        }

        @Override
        public int getItemCount() {
            return filterUserList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView textView;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.user_name);
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        //deletes the users
        filterUserRepository.deleteUsers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
        if (unbinder != null) {
            unbinder.unbind();
        }
    }


    //Observer to observe emitted data
    public DisposableObserver getObserver() {
        return new DisposableObserver<List<FilterUser>>() {
            @Override
            public void onNext(List<FilterUser> filterUsers) {
                filterUserList.clear();
                filterUserList.addAll(filterUsers);
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };

    }
}
