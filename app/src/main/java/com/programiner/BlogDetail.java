package com.programiner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.programiner.blogapp.R;
import com.programiner.blogapp.databinding.ActivityBlogDetailBinding;

import java.util.HashMap;

public class BlogDetail extends AppCompatActivity {
    ActivityBlogDetailBinding binding;
    String id;
    String  title, desc,count;
    int n_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlogDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        showdata();
    }

    private void showdata() {
        id = getIntent().getStringExtra("id");
        FirebaseFirestore.getInstance().collection("Blogs").document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Glide.with(getApplicationContext()).load(value.getString("img")).into(binding.imageView3);
                binding.textView4.setText(Html.fromHtml("<font color='B7B7B7'>By </font> <font color='#000000'>"+value.getString("author")));
                binding.textView5.setText(value.getString("tittle"));
                binding.textView6.setText(value.getString("desc"));
                title= value.getString("tittle");
                desc= value.getString("desc");
                count= value.getString("share_count");

                int i_count=Integer.parseInt(count);
                n_count=i_count+1;
            }
        });
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                String shareBody = desc;
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, title);
                intent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(intent,"Share Using"));

                HashMap<String,Object> map = new HashMap<>();
                map.put("share_count", String.valueOf(n_count));
                FirebaseFirestore.getInstance().collection("Blogs").document(id).update(map);
            }
        });
        binding.imageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}