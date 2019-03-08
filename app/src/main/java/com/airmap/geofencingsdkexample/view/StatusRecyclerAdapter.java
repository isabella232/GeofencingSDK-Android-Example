package com.airmap.geofencingsdkexample.view;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airmap.geofencingsdkexample.R;
import com.airmap.geofencingsdkexample.Utils;
import com.airmap.geofencingsdkexample.util.DiffStatusCallback;
import com.airmap.geofencingsdk.status.GeofencingStatus;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.airmap.geofencingsdkexample.Utils.getSeverity;

public class StatusRecyclerAdapter extends RecyclerView.Adapter<StatusRecyclerAdapter.ViewHolder> {

    private static final String TAG = "StatusRecyclerAdapter";

    private List<GeofencingStatus> statusList;

    public StatusRecyclerAdapter() {}

    public void setStatuses(@Nullable Collection<GeofencingStatus> statuses) {
        // sort
        List<GeofencingStatus> newStatuses = sort(statuses);

        // calculate changes
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffStatusCallback(statusList, newStatuses));

        // notify changes
        statusList = newStatuses;
        diffResult.dispatchUpdatesTo(this);
    }

    private List<GeofencingStatus> sort(Collection<GeofencingStatus> statuses) {
        List<GeofencingStatus> statusList = new ArrayList<>(statuses);
        Collections.sort(statusList, new Comparator<GeofencingStatus>() {
            @Override
            public int compare(GeofencingStatus status1, GeofencingStatus status2) {
                if (getSeverity(status1) < getSeverity(status2)) {
                    return 1;
                } else if (getSeverity(status1) > getSeverity(status2)) {
                    return -1;
                } else {
                    return status1.airspace.getName().compareTo(status2.airspace.getName());
                }
            }
        });

        return statusList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_status, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GeofencingStatus status = getItemAt(position);

        String airspaceName = status.airspace.getName();
        holder.airspaceNameTextView.setText(airspaceName);

        String airspaceType;
        int airspaceColor;
        JsonObject properties = (JsonObject) status.airspace.metadata;
        String restrictionType = properties.get("restriction_type").getAsString();
        switch (restrictionType.toLowerCase()) {
            case "no_fly":
                airspaceType = "No Fly Zone";
                airspaceColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_red);
                break;
            case "caution":
            case "permit":
            case "notice":
                boolean authorization = properties.has("authorization") && properties.get("authorization").getAsBoolean();

                airspaceType = authorization ? "Authorization Required" : "Notice";
                airspaceColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_orange);
                break;
            default:
                airspaceType = "Caution";
                airspaceColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_yellow);
                break;
        }
        holder.airspaceTypeIndicator.getBackground().setColorFilter(airspaceColor, PorterDuff.Mode.SRC);
        holder.airspaceTypeTextView.setText(airspaceType);

        if (status.proximity != null) {
            int distanceFt = (int) Utils.metersToFeet(status.proximity.distanceTo);
            int seconds = (int) status.proximity.timeTo;

            String proximityText = distanceFt + "ft - " + seconds + " seconds";
            holder.proximityTextView.setText(proximityText);
            holder.proximityTextView.setVisibility(View.VISIBLE);
        } else {
            holder.proximityTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return statusList != null ? statusList.size() : 0;
    }

    private GeofencingStatus getItemAt(int index) {
        return statusList.get(index);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private View airspaceTypeIndicator;
        private TextView airspaceNameTextView;
        private TextView airspaceTypeTextView;
        private TextView proximityTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            airspaceTypeIndicator = itemView.findViewById(R.id.airspace_type_indicator);
            airspaceNameTextView = itemView.findViewById(R.id.airspace_name_text_view);
            airspaceTypeTextView = itemView.findViewById(R.id.airspace_type_text_view);
            proximityTextView = itemView.findViewById(R.id.proximity_text_view);
        }
    }
}
