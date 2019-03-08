package com.airmap.geofencingsdkexample.airspace;

import com.airmap.geofencingsdk.airspace.AirspaceObject;
import com.airmap.geofencingsdk.util.GeometryUtils;
import com.google.gson.JsonObject;
import com.mapbox.services.commons.geojson.Geometry;

public class FeatureAirspaceObject extends AirspaceObject<Geometry,JsonObject> {

    public FeatureAirspaceObject(Geometry geometry, float floor, float ceiling, JsonObject metadata, AirspaceObject.Type type) {
        super(geometry, floor, ceiling, metadata, type);
    }

    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public String getName() {
        return metadata.get("name").getAsString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FeatureAirspaceObject)) {
            return false;
        }

        return ((FeatureAirspaceObject) o).metadata.equals(metadata) && GeometryUtils.equal(((FeatureAirspaceObject) o).geometry, geometry);
    }

    @Override
    public int hashCode() {
        return metadata.hashCode();
    }
}
