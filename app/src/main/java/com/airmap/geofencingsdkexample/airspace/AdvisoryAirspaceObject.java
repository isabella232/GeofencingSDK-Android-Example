package com.airmap.geofencingsdkexample.airspace;

import com.airmap.airmapsdk.models.status.AirMapAdvisory;
import com.airmap.geofencingsdk.airspace.AirspaceObject;
import com.airmap.geofencingsdk.util.GeometryUtils;
import com.mapbox.services.commons.geojson.Geometry;

import org.json.JSONObject;

public class AdvisoryAirspaceObject extends AirspaceObject<JSONObject,AirMapAdvisory> {

    public AdvisoryAirspaceObject(JSONObject geometry, AirMapAdvisory metadata, Type type) {
        // TODO: floor & ceiling
        super(geometry, 0, 0, metadata, type);
    }

    @Override
    public Geometry getGeometry() {
        return GeometryUtils.toGeometry(geometry);
    }

    @Override
    public String getName() {
        return metadata.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AdvisoryAirspaceObject)) {
            return false;
        }

        return ((AdvisoryAirspaceObject) o).geometry.toString().equals(geometry.toString());
    }

    @Override
    public int hashCode() {
        return geometry.toString().hashCode();
    }
}
