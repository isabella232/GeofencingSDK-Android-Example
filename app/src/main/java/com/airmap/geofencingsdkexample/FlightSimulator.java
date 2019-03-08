package com.airmap.geofencingsdkexample;

import android.content.Context;
import android.util.Log;

import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.shapes.AirMapGeometry;
import com.airmap.airmapsdk.models.shapes.AirMapPath;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.api.utils.turf.TurfMeasurement;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.models.Position;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

class FlightSimulator {

    private static final String TAG = "FlightSimulator";

    public static final float SPEED = 7f; // meters per second

    private static final int INTERVAL = 50; // ms

    private Context context;

    private AirMapPath flightPath;
    private LatLng currentLocation;
    private Coordinate nextCoordinate;

    private Subscription intervalSubscription;
    private AtomicBoolean stop = new AtomicBoolean(false);

    private FlightListener flightListener;

    public FlightSimulator(Context context, FlightListener flightListener) {
        this.context = context;
        this.flightListener = flightListener;

        init();
    }

    private void init() {
        if (flightPath == null) {
            try {
                createFlightPath();
            } catch (JSONException | IOException e) {
                Log.e(TAG, "Failed to create flight path", e);
            }
        }

        intervalSubscription = Observable.interval(INTERVAL, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .takeWhile(new Func1<Long, Boolean>() {
                    @Override
                    public Boolean call(Long aLong) {
                        return !stop.get();
                    }
                })
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long zero) {
                        if (currentLocation == null) {
                            currentLocation = flightPath.getCoordinates().get(0).toMapboxLatLng();
                            nextCoordinate = flightPath.getCoordinates().get(1);
                        }

                        double distanceToNextWayPoint = currentLocation.distanceTo(nextCoordinate.toMapboxLatLng());

                        double maxDistanceMeters = SPEED * ((double) INTERVAL / 1000);

                        Point origin = Point.fromCoordinates(Position.fromCoordinates(currentLocation.getLongitude(), currentLocation.getLatitude()));

                        double bearing = TurfMeasurement.bearing(Position.fromCoordinates(currentLocation.getLongitude(), currentLocation.getLatitude()),
                                Position.fromCoordinates(nextCoordinate.getLongitude(), nextCoordinate.getLatitude()));

                        if (distanceToNextWayPoint > maxDistanceMeters) {
                            Point destination = TurfMeasurement.destination(origin, maxDistanceMeters / 1000, bearing, "kilometers");

                            currentLocation = new LatLng(destination.getCoordinates().getLatitude(), destination.getCoordinates().getLongitude());

                            flightListener.onPositionChanged(currentLocation, bearing);
                        } else {
                            currentLocation = new LatLng(nextCoordinate.toMapboxLatLng());

                            flightListener.onPositionChanged(currentLocation, bearing);

                            int index = flightPath.getCoordinates().indexOf(nextCoordinate);
                            if (index < flightPath.getCoordinates().size() - 1) {
                                nextCoordinate = flightPath.getCoordinates().get(index + 1);
                            } else {
                                flightListener.onFlightFinished();
                                destroy();
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "Error in flight simulator", throwable);
                    }
                });
    }

    public void destroy() {
        stop.set(true);
        intervalSubscription.unsubscribe();
    }

    private void createFlightPath() throws JSONException, IOException {
        // read from assets/flightpath.json
        StringBuilder sb = new StringBuilder();
        InputStream json = context.getAssets().open("flightpath.json");
        BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));

        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line);
        }
        in.close();

        JSONObject geoJson = new JSONObject(sb.toString());
        flightPath = (AirMapPath) AirMapGeometry.getGeometryFromGeoJSON(geoJson);

    }

    public interface FlightListener {
        void onPositionChanged(LatLng latLng, double heading);
        void onFlightFinished();
    }
}
