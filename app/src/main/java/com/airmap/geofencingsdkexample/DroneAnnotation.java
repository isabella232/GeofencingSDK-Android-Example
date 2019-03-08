package com.airmap.geofencingsdkexample;

import android.content.Context;
import android.graphics.Bitmap;

import com.airmap.airmapsdk.util.AnnotationsFactory;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.models.Position;

public class DroneAnnotation {

    private Context context;
    private MapboxMap map;

    private AnnotationsFactory annotationsFactory;
    private Bitmap icon;

    private LatLng center;

    private SymbolLayer droneMarker;
    private Polyline trail;

    public DroneAnnotation(Context context, LatLng center) {
        this.context = context;
        this.center = center;

        annotationsFactory = new AnnotationsFactory(context);
        icon = Utils.getBitmap(context, R.drawable.ic_flight_marker);
    }

    public void addToMap(MapboxMap map) {
        this.map = map;

        // drone icon
        Source markerSource = new GeoJsonSource("marker-source", Feature.fromGeometry(Point.fromCoordinates(Position.fromCoordinates(center.getLongitude(), center.getLatitude()))));
        map.addSource(markerSource);
        map.addImage("my-marker-image", icon);
        droneMarker = new SymbolLayer("marker-layer", "marker-source");
        map.addLayer(droneMarker.withProperties(PropertyFactory.iconImage("my-marker-image")));
    }

    public void updateOnMap(LatLng center, double bearing) {
        this.center = center;

        // update marker
        GeoJsonSource markerSource = map.getSourceAs("marker-source");
        markerSource.setGeoJson(Feature.fromGeometry(Point.fromCoordinates(Position.fromCoordinates(center.getLongitude(), center.getLatitude()))));
        map.getLayerAs("marker-layer").setProperties(PropertyFactory.iconRotate((float) bearing));
    }
}
