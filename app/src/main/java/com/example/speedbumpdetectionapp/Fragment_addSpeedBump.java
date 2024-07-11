package com.example.speedbumpdetectionapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
public class Fragment_addSpeedBump extends Fragment implements OnMapReadyCallback {
    private MySharedViewModel viewModel;
    private GoogleMap googleMapObj;
    private FloatingActionButton fabaddspeedBump;
    private static final String TAG = Fragment_addSpeedBump.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    Button addBumpbtn;
    LatLng latlagforAddBump;

    //------------------------------------ Database Object-------------------------------------------
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private String userId = auth.getUid();
    private DatabaseReference userAccReference = database.getReference("User_accounts").child(userId);
    private DatabaseReference allBumpListReference = database.getReference("All_speed_bump_list");
    private String bumpKey = null;

    //-----------------------------------------------------------------------------------------------
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    public Fragment_addSpeedBump() {
        // Required empty public constructor
    }

    public static Fragment_addSpeedBump newInstance(String param1, String param2) {
        Fragment_addSpeedBump fragment = new Fragment_addSpeedBump();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MySharedViewModel.class);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        // Initialize location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    // Update map with current location
                    updateMapLocation(location);
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_speed_bump, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get a handle to the fragment and register the callback.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.GmapAddspeedbump);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.d("Fragment_A", "SupportMapFragment not found!");
        }

        addBumpbtn = view.findViewById(R.id.btnAddBump);
        fabaddspeedBump = view.findViewById(R.id.fabaddspeedBump);
        fabaddspeedBump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                showPopupMenu();
            }
        });

        addBumpbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add Speed Bump location to the real time Database
                if(latlagforAddBump!=null){
                    addNewBumpLocationToUserAccountDB(latlagforAddBump);
                    // Add a marker at a specific location
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latlagforAddBump)
                            .title("Speed Break")
                            .snippet("size: big");

                    if (googleMapObj != null) {
                        viewModel.addMarkerFromFragment_addSpeedBump(latlagforAddBump);
                        googleMapObj.addMarker(markerOptions);
                        Snackbar.make(getView(), "Marked successfully", Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        googleMapObj = googleMap;
        googleMapObj.setMapType(GoogleMap.MAP_TYPE_NORMAL); // Default map type
        // Check location permission
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        // Enable location layer
        googleMapObj.setMyLocationEnabled(true);
        // Start location updates
        startLocationUpdates();
        //Add Your Contributed Marker on Map
        addOnlyMyBumpOnMap();

        googleMapObj.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                // Show a Toast message or dialog to confirm deletion
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Marker")
                        .setMessage("Do you want to delete this marker?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                marker.remove();
                                deleteMarkerByClicking(marker);
                            }
                        })
                        .setNegativeButton("No", null)
                        .setIcon(R.drawable.baseline_circle_notifications_24)
                        .show();
                return false;
            }
        });
    }

    //------------------------------------ Fab menu popup function ----------------------------------

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(requireContext(), fabaddspeedBump);
        popupMenu.getMenuInflater().inflate(R.menu.fab_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.Normal) {
                    googleMapObj.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    Snackbar.make(fabaddspeedBump, "Normal Map", Snackbar.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.Satellite) {
                    googleMapObj.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    Snackbar.make(fabaddspeedBump, "Satellite Map", Snackbar.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.Terrain) {
                    googleMapObj.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    Snackbar.make(fabaddspeedBump, "Terrain Map", Snackbar.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.Hybrid) {
                    googleMapObj.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    Snackbar.make(fabaddspeedBump, "Hybrid Map", Snackbar.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            }
        });
        popupMenu.show();
    }

    //----------------------------------- Current location  -----------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable location layer and start location updates
                if (googleMapObj != null) {
                    if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED && ActivityCompat
                            .checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                            .PERMISSION_GRANTED) {
                        return;
                    }
                    googleMapObj.setMyLocationEnabled(true);
                }
                startLocationUpdates();
            } else {
                // Permission denied
               // Log.d(TAG, "Location permission denied");
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000); // 3 seconds interval

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateMapLocation(Location location) {
        if (googleMapObj != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            latlagforAddBump = latLng;
            googleMapObj.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f)); // Zoom level 15
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    //-----------------------------------** Database Operation **------------------------------------
    public void addNewBumpLocationToUserAccountDB(LatLng LocForAddBump){

         DatabaseReference bumpCountReference = userAccReference.child("Total_speed_bump_added");
         bumpCountReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalBumpAdded = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                totalBumpAdded++; // Increment the count
                String bumpNumber = String.valueOf(totalBumpAdded);
                // Add the new bump location
                userAccReference.child(bumpNumber).child("latitude").setValue(LocForAddBump.latitude);
                userAccReference.child(bumpNumber).child("longitude").setValue(LocForAddBump.longitude);
                // Update the total bump count
                bumpCountReference.setValue(totalBumpAdded);
                addNewBumpLocationToAllBumpListDB(LocForAddBump,userId,bumpNumber);
                bumpCountReference.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
               // Toast.makeText(getContext(), "Operation Cancel", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addNewBumpLocationToAllBumpListDB(LatLng LocForAddBump,String uID, String bumpNumber){

        String bumpName = bumpNumber+"X"+uID;
        allBumpListReference.child(bumpName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Entry doesn't exist, proceed with insertion
                    allBumpListReference.child(bumpName).child("latitude").setValue(LocForAddBump.latitude);
                    allBumpListReference.child(bumpName).child("longitude").setValue(LocForAddBump.longitude);
                } else {
                    // Entry already exists, handle accordingly (log, update, etc.)
                   // Log.d("AddBumpErrorInGlobalArea", "Duplicate entry found for bumpName: " + bumpName);
                }
                allBumpListReference.child(bumpName).removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Bump Already present hear !!! ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addOnlyMyBumpOnMap(){
        DatabaseReference myAccRef = database.getReference("User_accounts").child(userId);
        myAccRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot bumpNum : snapshot.getChildren())
                {
                      Double latitude = bumpNum.child("latitude").getValue(Double.class);
                      Double longitude = bumpNum.child("longitude").getValue(Double.class);

                        if(latitude!=null && latitude !=null) {
                            LatLng location = new LatLng(latitude, longitude);
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(location)
                                    .title("Speed Break") // Set the title of the marker
                                    .snippet("size: big"); // Set the snippet (optional)

                            if (googleMapObj != null) {
                                googleMapObj.addMarker(markerOptions);
                            }
                        }
                }
                myAccRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    // --------------------------------------- Delete Marker ----------------------------------------
    public void deleteMarkerByClicking(Marker marker){
        LatLng position = marker.getPosition();
        double latitude = position.latitude;
        double longitude = position.longitude;

        DatabaseReference dbRefToRemoveNode = database.getReference("User_accounts").child(userId);
        dbRefToRemoveNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot BumpNumber : snapshot.getChildren())
                {
                    Double lati = BumpNumber.child("latitude").getValue(Double.class);
                    Double longi = BumpNumber.child("longitude").getValue(Double.class);

                   if(lati != null && longi != null && lati.equals(latitude) && longi.equals(longitude))
                    {
                      bumpKey = BumpNumber.getKey();
                        BumpNumber.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getContext(), "Deleted successfully !!", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(getContext(), "Problem while deleting !!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        break; // Exit the loop after finding and deleting the node
                    }
                }
                dbRefToRemoveNode.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
               // Toast.makeText(getContext(), "Problem while retrieving data !!", Toast.LENGTH_SHORT).show();
            }
        });
        // Delete bump from Global(home page) map
        DatabaseReference dbRefTORemoveGlobalBump = database.getReference("All_speed_bump_list");
        dbRefTORemoveGlobalBump.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot oneGlobalBump : snapshot.getChildren())
                {
                    String bumpName = bumpKey+"X"+userId;
                    if(bumpName.equals(oneGlobalBump.getKey()))
                    {
                        oneGlobalBump.getRef().removeValue();
                        break;
                    }
                }
                dbRefTORemoveGlobalBump.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

//---------------------------------------------- end -----------------------------------------------