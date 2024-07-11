package com.example.speedbumpdetectionapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public class Fragment_A extends Fragment implements OnMapReadyCallback {
    private MySharedViewModel viewModel;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Set<Double> notifiedSpeedBumps =  new HashSet<>();
    private Map<Double, Double> allSpeedBumpList = new HashMap<>();
    private GoogleMap googleMapObj;
    private FloatingActionButton fab;
    private static final String TAG = Fragment_A.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    //----------------------------------------------------------------------------------------------
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    public Fragment_A() {
        // Required empty public constructor
    }

    public static Fragment_A newInstance(String param1, String param2) {
        Fragment_A fragment = new Fragment_A();
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

        fillArrayWithAllBump();
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
        //Comunicating with another fragment
        viewModel.getLatLngLiveData().observe(getViewLifecycleOwner(), latLng -> {
            if (latLng != null) {
                addTempMarkerToMapLocation(latLng);
            }
        });
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment__a, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get a handle to the fragment and register the callback.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.Gmap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.d("Fragment_A", "SupportMapFragment not found!");
        }

        //--------------------------fab--------------------------------------

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        googleMapObj = googleMap;
        googleMapObj.setMapType(GoogleMap.MAP_TYPE_NORMAL); // Default map type

        // Check location permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Enable location layer
        googleMapObj.setMyLocationEnabled(true);
        // Start location updates
        startLocationUpdates();
        //Add Speed bump marker on the Map from All_speed_bump_list database
        addSpeedBumpMarkersOnMap();
    }

    //------------------------------------Fab menu popup function ---------------------------------------

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(requireContext(), fab);
        popupMenu.getMenuInflater().inflate(R.menu.fab_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.Normal) {
                    googleMapObj.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    Snackbar.make(fab, "Menu Item 1 clicked", Snackbar.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.Satellite) {
                    googleMapObj.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    Snackbar.make(fab, "Menu Item 2 clicked", Snackbar.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.Terrain) {
                    googleMapObj.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    Snackbar.make(fab, "Menu Item 3 clicked", Snackbar.LENGTH_SHORT).show();
                    return true;
                } else if (menuItem.getItemId() == R.id.Hybrid) {
                    googleMapObj.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    Snackbar.make(fab, "Menu Item 4 clicked", Snackbar.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            }
        });
        popupMenu.show();
    }

    //----------------------------------- Current location  ---------------------------------------

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
                Log.d(TAG, "Location permission denied");
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(4000); // 4 seconds interval

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateMapLocation(Location location) {
        if (googleMapObj != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMapObj.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f)); // Zoom level 15
            // Check For Nearer Speed Bump
            checkNearbySpeedBumps(location);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    //------------------------------ Add speed bump marker on Map ----------------------------------
    public void addSpeedBumpMarkersOnMap(){
        DatabaseReference allBumpListReference = database.getReference("All_speed_bump_list");
        allBumpListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Log.d("FirebaseData", "Total bumps: " + snapshot.getChildrenCount());
                for(DataSnapshot bumpRecord : snapshot.getChildren())
                {
                    Double latitude = bumpRecord.child("latitude").getValue(Double.class);
                    Double longitude = bumpRecord.child("longitude").getValue(Double.class);

                    LatLng location = new LatLng(latitude, longitude);
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(location)
                            .title("Speed Break") // Set the title of the marker
                            .snippet("size: big"); // Set the snippet (optional)

                    if (googleMapObj != null) {
                        googleMapObj.addMarker(markerOptions);
                    }
                }
                allBumpListReference.removeEventListener(this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
               // Log.d("DbError","Error while retriving DB data");
            }
        });
    }

    public void addTempMarkerToMapLocation(LatLng latLng) {
        // Your existing function implementation (add marker to map)
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Speed Break") // Set the title of the marker
                .snippet("size: big"); // Set the snippet (optional)

        googleMapObj.addMarker(markerOptions);
    }

    //---------------------------- Load All Bump in Array List--------------------------------------
    public void fillArrayWithAllBump(){

        DatabaseReference allBumpRef = database.getReference("All_speed_bump_list");
        allBumpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allSpeedBumpList.clear(); // Clear the list to avoid duplicates
                for (DataSnapshot bumpNumSnap : snapshot.getChildren()) {
                    Double lat = bumpNumSnap.child("latitude").getValue(Double.class);
                    Double longi = bumpNumSnap.child("longitude").getValue(Double.class);
                    allSpeedBumpList.put(lat, longi);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
              //  Toast.makeText(getContext(), "Error while filling Array List", Toast.LENGTH_SHORT).show();
            }
        });
    }

   //----------------------------------------- Check AND Notify ------------------------------------
    public void checkNearbySpeedBumps(Location userLocation){

                for(Map.Entry<Double, Double> entry : allSpeedBumpList.entrySet())
                {
                    Double latitude = entry.getKey();
                    Double longitude = entry.getValue();
                    Location speedBumpLocation = new Location("");
                    speedBumpLocation.setLatitude(latitude);
                    speedBumpLocation.setLongitude(longitude);
                    Double speedBumpId = latitude;  // entry.getKey(); // latitude as a key

                    float distance = userLocation.distanceTo(speedBumpLocation);
                    if (distance <= 40 && !notifiedSpeedBumps.contains(speedBumpId)) {
                        sendNotification();
                        notifiedSpeedBumps.add(speedBumpId);
                        break; // Notify for the first close speed bump and stop
                    }
                }
    }

    private void sendNotification() {
        Context context = getContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "speed_bump_alert";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Speed Bump Alert", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("Speed Bump Ahead")
                .setContentText("A speed bump is within 40 meters of your location.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(1, builder.build());
    }
}

//---------------------------------------------- end -----------------------------------------------