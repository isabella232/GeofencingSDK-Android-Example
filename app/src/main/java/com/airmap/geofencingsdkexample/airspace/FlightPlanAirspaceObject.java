package com.airmap.geofencingsdkexample.airspace;

import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.geofencingsdk.airspace.AirspaceObject;
import com.airmap.geofencingsdk.util.GeometryUtils;
import com.mapbox.services.commons.geojson.Geometry;

public class FlightPlanAirspaceObject extends AirspaceObject<Geometry,AirMapFlightPlan> {

    private String name;

    public FlightPlanAirspaceObject(Geometry flightPlanGeometry, float maxAltitude, String name) {
        super(flightPlanGeometry, 0, maxAltitude, null, Type.GEOCAGE);

        this.name = name;
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FlightPlanAirspaceObject)) {
            return false;
        }

        return ((FlightPlanAirspaceObject) o).metadata.getPlanId().equals(metadata.getPlanId()) &&
                GeometryUtils.equal(((FlightPlanAirspaceObject) o).geometry, geometry);
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
