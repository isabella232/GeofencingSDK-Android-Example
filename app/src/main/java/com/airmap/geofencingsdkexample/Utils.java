package com.airmap.geofencingsdkexample;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.shapes.AirMapGeometry;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.geofencingsdk.status.GeofencingStatus;
import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.services.api.utils.turf.TurfMeasurement;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.models.Position;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Utils extends com.airmap.airmapsdk.util.Utils {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    private static Bitmap getBitmap(VectorDrawableCompat vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmap(Context context, @DrawableRes int drawableResId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawableCompat) {
            return getBitmap((VectorDrawableCompat) drawable);
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("Unsupported drawable type");
        }
    }

    public static AirMapPolygon toBounds(String geometry, float bufferRatio) {
        try {
            // parse geojson
            AirMapPolygon polygon = (AirMapPolygon) AirMapGeometry.getGeometryFromGeoJSON(new JSONObject(geometry));

            // create lat lng bounds
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (Coordinate coordinate : polygon.getCoordinates()) {
                boundsBuilder.include(coordinate.toMapboxLatLng());
            }
            LatLngBounds bounds = boundsBuilder.build();

            // add buffer to bounds
            Point nw = Point.fromCoordinates(Position.fromLngLat(bounds.getLonWest(), bounds.getLatNorth()));
            Point se = Point.fromCoordinates(Position.fromLngLat(bounds.getLonEast(), bounds.getLatSouth()));
            double cornerToCornerKm = TurfMeasurement.distance(nw, se);
            double bearing = TurfMeasurement.bearing(nw, se);
            Point newNw = TurfMeasurement.destination(nw, cornerToCornerKm * bufferRatio, (bearing + 180) % 360, "kilometers");
            Point newSe = TurfMeasurement.destination(se, cornerToCornerKm * bufferRatio, bearing, "kilometers");

            bounds = LatLngBounds.from(newNw.getCoordinates().getLatitude(), newSe.getCoordinates().getLongitude(), newSe.getCoordinates().getLatitude(), newNw.getCoordinates().getLongitude());

            List<Coordinate> coordinates = new ArrayList<>();
            coordinates.add(new Coordinate(bounds.getLatNorth(), bounds.getLonWest()));
            coordinates.add(new Coordinate(bounds.getLatNorth(), bounds.getLonEast()));
            coordinates.add(new Coordinate(bounds.getLatSouth(), bounds.getLonEast()));
            coordinates.add(new Coordinate(bounds.getLatSouth(), bounds.getLonWest()));
            coordinates.add(new Coordinate(bounds.getLatNorth(), bounds.getLonWest()));
            return new AirMapPolygon(coordinates);
        } catch (JSONException e) {
            Log.e("Utils", "Unable to parse geometry", e);
        }

        return null;
    }

    public static int getSeverity(GeofencingStatus status) {
        JsonObject properties = (JsonObject) status.airspace.metadata;
        String restrictionType = properties.get("restriction_type").getAsString();
        switch (restrictionType.toLowerCase()) {
            case "no_fly":
                return 3;
            case "caution":
            case "permit":
            case "notice":
                return 2;
            default:
                return 1;
        }
    }
}
