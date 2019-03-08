package com.airmap.geofencingsdkexample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.ui.views.AirMapMapView;
import com.airmap.airmapsdk.util.AirMapConfig;
import com.airmap.geofencingsdkexample.airspace.source.AirspaceMapSource;
import com.airmap.geofencingsdkexample.view.CustomTabLayout;
import com.airmap.geofencingsdkexample.view.NotificationPagerAdapter;
import com.airmap.geofencingsdkexample.view.SimplePageChangeListener;
import com.airmap.geofencingsdkexample.view.SimpleTabSelectedListener;
import com.airmap.geofencingsdk.GeofencingService;
import com.airmap.geofencingsdk.status.GeofencingStatus;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int[] SEVERITY_COLORS = new int[] {
            R.color.light_grey,
            R.color.status_yellow,
            R.color.status_orange,
            R.color.status_red
    };

    private static final Set<String> PREFERRED_RULESET_IDS = new HashSet<>(Arrays.asList("usa_part_107", "usa_airmap_rules"));

    private static final String TAG = "MainActivity";

    private AirMapMapView mapView;
    private FloatingActionButton flyButton;
    private CustomTabLayout tabLayout;
    private ViewPager viewPager;
    private NotificationPagerAdapter notificationPagerAdapter;

    private DroneAnnotation droneAnnotation;
    private FlightSimulator simulator;

    private GeofencingService geofencingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        AirMap.init(this);
        Mapbox.getInstance(this, AirMapConfig.getMapboxApiKey());

        // setup map
        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.configure(new AirMapMapView.DynamicConfiguration(PREFERRED_RULESET_IDS, null, true ));
        mapView.getMapAsync(null);
        mapView.addOnMapLoadListener(new AirMapMapView.OnMapLoadListener() {
            @Override
            public void onMapLoaded() {
                addDroneToMap();

                startGeofencingService();
            }

            @Override
            public void onMapFailed(AirMapMapView.MapFailure reason) {
            }
        });

        // setup play button
        flyButton = findViewById(R.id.fly_button);
        flyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });

        // setup custom tabs
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new SimpleTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (viewPager == null) {
                    return;
                }

                int position = tabLayout.getSelectedTabPosition();
                viewPager.setCurrentItem(position);
            }
        });

        // setup view pager
        notificationPagerAdapter = new NotificationPagerAdapter(this);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(notificationPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new SimplePageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (!Objects.requireNonNull(tabLayout.getTabAt(position)).isSelected()) {
                    Objects.requireNonNull(tabLayout.getTabAt(position)).select();
                }
            }
        });
    }

    private void play() {
        // if playing, stop
        if (flyButton.isSelected()) {
            flyButton.setImageResource(R.drawable.ic_play_arrow);
            flyButton.setSelected(false);
            simulator.destroy();

        // if not playing, create & start flight simulator
        } else {
            flyButton.setImageResource(R.drawable.ic_stop);
            flyButton.setSelected(true);

            simulator = new FlightSimulator(this, new FlightSimulator.FlightListener() {
                @Override
                public void onPositionChanged(LatLng latLng, double bearing) {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    // update marker on map
                    droneAnnotation.updateOnMap(latLng, bearing);

                    // pass position to Geofencing Service
                    Position aircraftPosition = Position.fromCoordinates(latLng.getLongitude(), latLng.getLatitude());
                    geofencingService.onPositionChanged(aircraftPosition, 0, 0);

                    // speed is constant, but Vx & Vy change with bearing
                    double velocityX = Math.cos(Math.toRadians(bearing)) * FlightSimulator.SPEED;
                    double velocityY = Math.sin(Math.toRadians(bearing)) * FlightSimulator.SPEED;
//                    Log.v(TAG, "velocity x: " + velocityX + " y: " + velocityY + " bearing: " + bearing);

                    // pass velocities to Geofencing Service
                    geofencingService.onSpeedChanged(velocityX, velocityY, 0);

                    // move camera to follow drone
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng)
                            .build();
                    mapView.getMap().setCameraPosition(cameraPosition);
                }

                @Override
                public void onFlightFinished() {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    Toast.makeText(MainActivity.this, "Flight Ended", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startGeofencingService() {
        geofencingService = new GeofencingService(new AirspaceMapSource(mapView, mapView.getMap()));
        geofencingService.addListener(new GeofencingService.Listener() {
            @Override
            public void onStatusChanged(List<GeofencingStatus> statuses) {
                /*
                 *  Statuses with the same airspace will come back because
                 *  one airspace can be represented by multiple rendered
                 *  features on the map. Sort them by priority (intersecting >
                 *  entering > approaching, etc) and remove duplicates
                 */
                Map<String, GeofencingStatus> prioritizedStatus = new HashMap<>();
                for (GeofencingStatus status : statuses) {
                    if (status.airspace == null) {
                        continue;
                    }

                    String name = status.airspace.getName();
                    if (!prioritizedStatus.containsKey(name) || (status.priority() > prioritizedStatus.get(name).priority())) {
                        prioritizedStatus.put(name, status);
                    }
                }

                List<GeofencingStatus> statusList = new ArrayList<>(prioritizedStatus.values());
                updateTabs(statusList);
                notificationPagerAdapter.setStatuses(statusList);
            }
        });
    }

    private void addDroneToMap() {
        if (droneAnnotation != null) {
            return;
        }

        LatLng center = new LatLng(34.015027991104574, -118.49517485165802);
        droneAnnotation = new DroneAnnotation(this, center);
        droneAnnotation.addToMap(mapView.getMap());
    }

    private void updateTabs(List<GeofencingStatus> statuses) {
        // total number of airspaces intersecting, entering & approaching
        int numIntersecting = 0;
        int numEntering = 0;
        int numApproaching = 0;

        // severity (No Fly Zone, Caution, etc)
        int intersectingSeverity = 0;
        int enteringSeverity = 0;
        int approachingSeverity = 0;

        for (GeofencingStatus status : statuses) {
            switch (status.level) {
                case INTERSECTING:
                    numIntersecting++;
                    intersectingSeverity = Math.max(Utils.getSeverity(status), intersectingSeverity);
                    break;
                case ENTERING:
                    numEntering++;
                    enteringSeverity = Math.max(Utils.getSeverity(status), enteringSeverity);
                    break;
                case APPROACHING:
                    numApproaching++;
                    approachingSeverity = Math.max(Utils.getSeverity(status), approachingSeverity);
                    break;
                case SAFE:
                case UNAVAILABLE:
                default:
                    continue;
            }
        }

        // update tab number & color
        tabLayout.setIntersecting(numIntersecting, SEVERITY_COLORS[intersectingSeverity]);
        tabLayout.setEntering(numEntering, SEVERITY_COLORS[enteringSeverity]);
        tabLayout.setApproaching(numApproaching, SEVERITY_COLORS[approachingSeverity]);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();

        if (simulator != null) {
            simulator.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

        // shutdown service
        if (geofencingService != null) {
            geofencingService.destroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
