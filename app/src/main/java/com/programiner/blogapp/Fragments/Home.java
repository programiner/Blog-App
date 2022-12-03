package com.programiner.blogapp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.programiner.blogapp.Adapter;
import com.programiner.blogapp.Model;
import com.programiner.blogapp.R;
import com.programiner.blogapp.databinding.FragmentHomeBinding;

import java.util.ArrayList;


public class Home extends Fragment {


    FragmentHomeBinding binding;
    ArrayList<Model> list;
    Adapter adapter;
    Model model;

    public Home() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupRv();
        setusearchview();
        super.onViewCreated(view, savedInstanceState);
    }

    private void setusearchview() {
        binding.searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
    }

    private void filter(String newText) {
        ArrayList<Model> filtered_list = new ArrayList<>();
        for(Model item:list){
            if (item.getTittle().toString().toLowerCase().contains(newText)){
                filtered_list.add(item);
            }
        }
        if (filtered_list.isEmpty()){
            //
        }
        else{
            adapter.filter_list(filtered_list);
        }
    }

    private void setupRv() {
        list = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Blogs").orderBy("timestamp").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                list.clear();
                for (DocumentSnapshot snapshot:value.getDocuments()){
                    model = snapshot.toObject(Model.class);
                    model.setId(snapshot.getId());
                    list.add(model);
                }
                adapter.notifyDataSetChanged();
            }
        });
        adapter = new Adapter(list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        binding.rvBlogs.setLayoutManager(linearLayoutManager);
        binding.rvBlogs.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding=null;
    }
}