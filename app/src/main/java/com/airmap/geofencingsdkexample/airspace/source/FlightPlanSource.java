package com.airmap.geofencingsdkexample.airspace.source;

import android.content.Context;

import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.airmapsdk.models.shapes.AirMapGeometry;
import com.airmap.airmapsdk.models.shapes.AirMapPoint;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.geofencingsdkexample.R;
import com.airmap.geofencingsdkexample.airspace.FlightPlanAirspaceObject;
import com.airmap.geofencingsdk.airspace.AirspaceObject;
import com.airmap.geofencingsdk.airspace.AirspaceSource;
import com.airmap.geofencingsdk.util.GeometryUtils;
import com.mapbox.services.commons.geojson.Geometry;

import java.util.HashSet;
import java.util.Set;

import rx.Observable;

public class FlightPlanSource implements AirspaceSource {

    private Set<AirspaceObject> airspace;

    public FlightPlanSource(Context context, AirMapFlightPlan flightPlan) {
        airspace = new HashSet<>();

        AirMapPolygon polygon = AirMapGeometry.convertPointToPolygon(new AirMapPoint(flightPlan.getTakeoffCoordinate()), flightPlan.getBuffer());
        Geometry geometry = GeometryUtils.toGeometry(AirMapGeometry.getGeoJSONFromGeometry(polygon));

        String name = context.getResources().getString(R.string.flight_plan_area);
        AirspaceObject flightPlanAirspace = new FlightPlanAirspaceObject(geometry, flightPlan.getMaxAltitude(), name);
        airspace.add(flightPlanAirspace);
    }

    @Override
    public Observable<Set<AirspaceObject>> getAirspaces() {
        return Observable.just(airspace);
    }
}
