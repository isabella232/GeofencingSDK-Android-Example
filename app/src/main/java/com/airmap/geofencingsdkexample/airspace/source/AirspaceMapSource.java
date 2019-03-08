package com.airmap.geofencingsdkexample.airspace.source;

import android.graphics.RectF;

import com.airmap.airmapsdk.ui.views.AirMapMapView;
import com.airmap.geofencingsdkexample.airspace.FeatureAirspaceObject;
import com.airmap.geofencingsdk.airspace.AirspaceObject;
import com.airmap.geofencingsdk.airspace.AirspaceSource;
import com.airmap.geofencingsdk.airspace.SourceCannotProvideForBoundsException;
import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.layers.Filter;
import com.mapbox.services.commons.geojson.Feature;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AirspaceMapSource implements AirspaceSource {

    private MapView mapView;
    private MapboxMap map;

    public AirspaceMapSource(AirMapMapView mapView, MapboxMap map) {
        this.mapView = mapView;
        this.map = map;
    }

    @Override
    public Observable<Set<AirspaceObject>> getAirspaces() {
        return Observable.create(new Observable.OnSubscribe<List<Feature>>() {
            @Override
            public void call(final Subscriber<? super List<Feature>> subscriber) {
                if (map.getCameraPosition().zoom < 12.5) {
                    subscriber.onError(new SourceCannotProvideForBoundsException());
                    return;
                }

                // query rendered airspace
                RectF rectF = new RectF(0, 0, mapView.getMeasuredWidth(), mapView.getMeasuredHeight());

                Filter.Statement statement = Filter.all(Filter.has("airspace_id"), Filter.neq("restriction_type", "unrestricted"));
                List<Feature> features = map.queryRenderedFeatures(rectF, statement, null);

                subscriber.onNext(features);
                subscriber.onCompleted();
            }
        })
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(Schedulers.io())
        .map(new Func1<List<Feature>, Set<AirspaceObject>>() {
            @Override
            public Set<AirspaceObject> call(List<Feature> features) {
                // create airspace objects from geometry and metadata
                Set<AirspaceObject> airspaces = new HashSet<>();
                for (Feature feature : features) {
                    JsonObject properties = feature.getProperties();

                    float ceiling = 0;
                    float floor = 0;
                    // TODO:
//                    if (properties.has("floor_ft")) {
//                        float floorFt = properties.get("floor_ft").getAsFloat();
//                        ceiling = (float) Utils.feetToMeters(floorFt);
//                    }

                    airspaces.add(new FeatureAirspaceObject(feature.getGeometry(), floor, ceiling, properties, AirspaceObject.Type.GEOFENCE));
                }
                return airspaces;
            }
        });

    }
}
