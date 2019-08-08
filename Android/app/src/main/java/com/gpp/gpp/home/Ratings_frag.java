package com.gpp.gpp.home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gpp.gpp.R;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class Ratings_frag extends Fragment {

    ArrayList<String> cities;
    ArrayList<Float> dry_wet;
    ArrayList<Float> plastic;

    MyAdapter adapter;

    String[] mcities= new String[]{"bombay"} ;
    Float[] mdry_wet = new Float[]{2.12f};
    Float[] mplastic =  new Float[]{2.1f};


    Cratings ct ;
    ListView listView;

    FirebaseDatabase database;
    DatabaseReference ref,ref2;


   /* String[] cname={"banana","orange","mango","mango","watermellon"};
    int[] ratings={4,5,3,2,5};*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rater, container, false);


        ct = new Cratings();
        cities = new ArrayList<>();
        dry_wet = new ArrayList<>();
        plastic = new ArrayList<>();


        database= FirebaseDatabase.getInstance();
        ref = database.getReference("LOCATIONS");
    //    ref2 = database.getReference("LOCTIONS/delhi");

       /* adapter = new MyAdapter(getActivity(),mcities,mdry_wet,mplastic);*/
        listView =(ListView)rootView.findViewById(R.id.list1);


       // listView.setAdapter(adapter);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ct = ds.getValue(Cratings.class);
                    cities.add(ds.getKey());
                    dry_wet.add(ct.getDry_wet());
                    plastic.add(ct.getPlastic());

                    mcities = new String[cities.size()];
                    mcities = cities.toArray(mcities);


                    mdry_wet = new Float[dry_wet.size()];
                    mdry_wet = dry_wet.toArray(mdry_wet);


                    mplastic = new Float[plastic.size()];
                    mplastic = plastic.toArray(mplastic);


                }
                adapter = new MyAdapter(getActivity(),mcities,mdry_wet,mplastic);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        return rootView;
    }
    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        Float[] mydry_wet;
        Float[] myplastic;
        String mycname[];

        MyAdapter(Context c,String []mycname,Float []mydry_wet,Float []myplastic){
            super(c,R.layout.row,R.id.text1,mcities);
            this.context=c;
            this.mycname=mcities;
            this.mydry_wet=mdry_wet;
            this.myplastic= mplastic;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.row,parent,false);
            RatingBar rater = (RatingBar) row.findViewById(R.id.rater);
            RatingBar rater2 = (RatingBar) row.findViewById(R.id.rater2);
            TextView city = (TextView) row.findViewById(R.id.text1);

            rater.setRating(mdry_wet[position]);
            rater2.setRating(mplastic[position]);
            city.setText(mcities[position]);
            return row;
        }
    }
}