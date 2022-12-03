package com.programiner.blogapp.Fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.programiner.blogapp.databinding.FragmentPublishBinding;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Publish extends Fragment {

    FragmentPublishBinding binding;
    Uri filepath;


    public Publish() {
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
        binding = FragmentPublishBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        selectimage();
        super.onViewCreated(view, savedInstanceState);
    }

    private void selectimage() {
        binding.view2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Your Image"), 101);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filepath = data.getData();
            binding.imgThumbnail.setVisibility(View.VISIBLE);
            binding.imgThumbnail.setImageURI(filepath);
            binding.view2.setVisibility(View.INVISIBLE);
            binding.bSelectImage.setVisibility(View.INVISIBLE);
            uploaddata(filepath);
        }
    }

    private void uploaddata(Uri filepath) {
        binding.btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(getActivity()).withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            if (binding.bTittle.getText().toString().equals("")) {
                                binding.bTittle.setError("Field is Required!!");
                            } else if (binding.bDesc.getText().toString().equals("")) {
                                binding.bDesc.setError("Field is Required!!");
                            } else if (binding.bAuthor.getText().toString().equals("")) {
                                binding.bAuthor.setError("Field is Required!!");
                            } else {
                                ProgressDialog pd = new ProgressDialog(getContext());
                                pd.setTitle("Uploading...");
                                pd.setMessage("Please wait for a while until we upload this data to our Firebase Storage and Firestore");
                                pd.setCancelable(false);
                                pd.show();

                                String title = binding.bTittle.getText().toString();
                                String desc = binding.bDesc.getText().toString();
                                String author = binding.bAuthor.getText().toString();

                                if (filepath != null) {
                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    StorageReference reference = storage.getReference().child("images/" + filepath.toString() + ".jpg");
                                    reference.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    String file_url = task.getResult().toString();
                                                    String date = (String) DateFormat.format("dd", new Date());
                                                    String month = (String) DateFormat.format("MMM", new Date());
                                                    String final_date = date + " " + month;

                                                    HashMap<String, String> map = new HashMap<>();
                                                    map.put("tittle", title);
                                                    map.put("desc", desc);
                                                    map.put("author", author);
                                                    map.put("date", final_date);
                                                    map.put("img", file_url);
                                                    map.put("timestamp", String.valueOf(System.currentTimeMillis()));
                                                    map.put("share_count", "0");

                                                    FirebaseFirestore.getInstance().collection("Blogs").document().set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                pd.dismiss();
                                                                Toast.makeText(getContext(), "Post Uploaded!!!", Toast.LENGTH_SHORT).show();

                                                                binding.imgThumbnail.setVisibility(View.INVISIBLE);
                                                                binding.view2.setVisibility(View.VISIBLE);
                                                                binding.bSelectImage.setVisibility(View.VISIBLE);
                                                                binding.bTittle.setText("");
                                                                binding.bDesc.setText("");
                                                                binding.bAuthor.setText("");
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }


                            }
                        }
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()){
                            showsettingdialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }

                }).withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError dexterError) {
                        getActivity().finish();
                    }
                }).onSameThread().check();
            }
        });
    }

    private void showsettingdialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Need Permission");
        builder.setMessage("This app needs permission to use this feature. You can grant us these permission manually by clicking on below button");
        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(),null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                getActivity().finish();
            }
        });
        builder.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}