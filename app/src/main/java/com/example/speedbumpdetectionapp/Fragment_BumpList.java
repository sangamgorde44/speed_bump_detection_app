package com.example.speedbumpdetectionapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
public class Fragment_BumpList extends Fragment {
    ArrayList<BumpListModel> speedBumpList = new ArrayList<>();
    RecyclerBumpAdapter adapter;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private String userId = user.getUid();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userAccReference = database.getReference("User_accounts").child(userId);
    //--------------------------------------------------------------------------------------------------------
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    public Fragment_BumpList() {
        // Required empty public constructor
    }

    public static Fragment_BumpList newInstance(String param1, String param2) {
        Fragment_BumpList fragment = new Fragment_BumpList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment__bump_list, container, false);
        userAccReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                speedBumpList.clear(); // Clear the list to avoid duplicates
                for (DataSnapshot bumpNumSnap : snapshot.getChildren()) {
                    Double lat = bumpNumSnap.child("latitude").getValue(Double.class);
                    Double longi = bumpNumSnap.child("longitude").getValue(Double.class);
                    String latitude = String.valueOf(lat);
                    String longitude = String.valueOf(longi);
                    speedBumpList.add(new BumpListModel(latitude, longitude));
                }
                // Find the RecyclerView in the inflated layout
                RecyclerView recyclerView = view.findViewById(R.id.recycleViewBumpList);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                adapter = new RecyclerBumpAdapter(getContext(),speedBumpList);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                //userAccReference.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Something went wrong !! ", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}

//---------------------------------------------- end -----------------------------------------------