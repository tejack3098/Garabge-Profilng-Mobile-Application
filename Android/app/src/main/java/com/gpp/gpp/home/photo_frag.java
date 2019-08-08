package com.gpp.gpp.home;



import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gpp.gpp.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;


public class photo_frag extends Fragment implements LocationListener {

    //get location variables
    private TextView tv2;
    private LocationManager locationmanager;
    private double longitude;
    private  double latitude;

    public String city;

    ProgressDialog mProgress;

    //Upload image variables
    private ImageButton btnChoose, btnUpload, btnCamera;
    private ImageView imageView;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private int action=0 ;

    //Firebase variables
    FirebaseStorage storage;
    StorageReference storageReference;
    String image_name;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        mProgress = new ProgressDialog(getActivity());
        tv2 =(TextView)view.findViewById(R.id.tv2);
        locationmanager=(LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);


        // checking the permissions
        if(ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            Toast.makeText(getActivity(), "Enable location service!!!", Toast.LENGTH_SHORT).show();
            return view;

        }



        //Location class
        Location location = locationmanager.getLastKnownLocation(locationmanager.NETWORK_PROVIDER);
        //To get the Lat long coordinates of the location
        onLocationChanged(location);

        //To get the  current city name,country of the location
        get_city_funct(location);


        //upload image code
        btnCamera = (ImageButton) view.findViewById(R.id.btnCamera);
        btnChoose = (ImageButton) view.findViewById(R.id.btnChoose);
        btnUpload = (ImageButton) view.findViewById(R.id.btnUpload);
        imageView = (ImageView) view.findViewById(R.id.imgView);

        //firebase references
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        return view;
    }

    private  void openCamera(){
        action=2;



            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

    private void chooseImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {


        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            image_name=UUID.randomUUID().toString();

            StorageReference ref = storageReference.child("images/"+ image_name);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                           /* Toast.makeText(getActivity(), image_name, Toast.LENGTH_SHORT).show();*/
                            Intent intent = new Intent(getActivity(),picratings.class).putExtra("image",image_name);
                            intent.putExtra("loc",city);

                            startActivity(intent);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(action!=2) {
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                    && data != null && data.getData() != null) {
                filePath = data.getData();
                //Toast.makeText(getActivity(), filePath.toString(), Toast.LENGTH_SHORT).show();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
        else {

             mProgress.setMessage("Uploading Image...");
            mProgress.show();

        // get the camera image

            try {
                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] dataBAOS = baos.toByteArray();


                //set the image into imageview

                imageView.setImageBitmap(bitmap);


            //upload the pic to firebase

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();

            image_name=UUID.randomUUID().toString();

          //  Toast.makeText(getActivity(), city, Toast.LENGTH_SHORT).show();

            StorageReference imagesref = storageReference.child("images/"+ image_name);

            //upload image

            UploadTask uploadTask = imagesref.putBytes(dataBAOS);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "failed..", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    /*DatabaseReference mainroot= FirebaseDatabase.getInstance().getReference();
                    //DatabaseReference childref = mainroot.child("IMAGES");
                    childref.child(image_name).push().setValue(new Data_Images());*/
                    mProgress.dismiss();
                    Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(),picratings.class).putExtra("image",image_name);
                    intent.putExtra("loc",city);

                    startActivity(intent);

                }
            });

            }catch (Exception e){
                mProgress.dismiss();
                e.printStackTrace();
            }




        }

    }
    @Override
    public void onLocationChanged(Location location) {

        longitude= location.getLongitude();
        latitude= location.getLatitude();
       // Toast.makeText(getActivity(), "longitude:"+longitude+";\nlatitude:"+latitude, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void get_city_funct(Location location){


        try {
            Geocoder geocoder = new Geocoder(getActivity());
            List<Address> addresses =null;
            addresses = geocoder.getFromLocation(latitude,longitude,1);

            city = addresses.get(0).getLocality();
            String country = addresses.get(0).getCountryName();
            tv2.setText("City: "+ city+"  Country: "+country);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}

//class Data_Images {
//    String location="delhi";
//   Data_Rating rating= new Data_Rating();
//}
//class Data_Rating{
//    int dry_wet=0;
//    int plastic=0;
//}