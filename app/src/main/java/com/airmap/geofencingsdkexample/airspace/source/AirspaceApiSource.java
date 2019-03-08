package com.airmap.geofencingsdkexample.airspace.source;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.airspace.AirMapAirspace;
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.airmapsdk.models.shapes.AirMapGeometry;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.models.status.AirMapAdvisory;
import com.airmap.airmapsdk.models.status.AirMapAirspaceStatus;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.geofencingsdkexample.Utils;
import com.airmap.geofencingsdkexample.airspace.AdvisoryAirspaceObject;
import com.airmap.geofencingsdk.airspace.AirspaceObject;
import com.airmap.geofencingsdk.airspace.AirspaceSource;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class AirspaceApiSource implements AirspaceSource {

    private List<String> rulesetIds;
    private AirMapPolygon polygon;

    public AirspaceApiSource(AirMapFlightPlan flightPlan) {
        this.rulesetIds = flightPlan.getRulesetIds();

        // convert flight polygon to latlng bounds
        this.polygon = Utils.toBounds(flightPlan.getGeometry(), .5f);
    }

    @Override
    public Observable<Set<AirspaceObject>> getAirspaces() {
        return Observable.create(new Observable.OnSubscribe<Set<AirspaceObject>>() {
            @Override
            public void call(final Subscriber<? super Set<AirspaceObject>> subscriber) {
                Date start = new Date();
                Date end = new Date(start.getTime() + (4 * 60 * 60 * 1000));

                final Call statusCall = AirMap.getAirspaceStatus(polygon, rulesetIds, start, end, new AirMapCallback<AirMapAirspaceStatus>() {
                    @Override
                    public void onSuccess(final AirMapAirspaceStatus response) {
                        final Map<String,AirMapAdvisory> advisoriesMap = new HashMap<>();
                        for (AirMapAdvisory advisory : response.getAdvisories()) {
                            advisoriesMap.put(advisory.getId(), advisory);
                        }

                        final Call airspaceCall = AirMap.getAirspace(new ArrayList<>(advisoriesMap.keySet()), new AirMapCallback<List<AirMapAirspace>>() {
                            @Override
                            protected void onSuccess(List<AirMapAirspace> response) {
                                Set<AirspaceObject> airspaces = new HashSet<>();
                                for (AirMapAirspace airspace : response) {
                                    JSONObject geojson = AirMapGeometry.getGeoJSONFromGeometry(airspace.getGeometry());
                                    airspaces.add(new AdvisoryAirspaceObject(geojson, advisoriesMap.get(airspace.getAirspaceId()), AirspaceObject.Type.GEOFENCE));
                                }
                                subscriber.onNext(airspaces);
                                subscriber.onCompleted();
                            }

                            @Override
                            protected void onError(AirMapException e) {
                                subscriber.onError(e);
                            }
                        });

                        subscriber.add(Subscriptions.create(new Action0() {
                            @Override
                            public void call() {
                                airspaceCall.cancel();
                            }
                        }));
                    }

                    @Override
                    public void onError(AirMapException e) {
                        if (rulesetIds == null) {
                            subscriber.onNext(null);
                            subscriber.onCompleted();
                        } else {
                            subscriber.onError(e);
                        }
                    }
                });

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        statusCall.cancel();
                    }
                }));
            }
        });
    }
}
