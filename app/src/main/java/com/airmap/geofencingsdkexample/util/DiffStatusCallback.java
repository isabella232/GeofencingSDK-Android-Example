package com.airmap.geofencingsdkexample.util;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.airmap.geofencingsdk.status.GeofencingStatus;

import java.util.List;

public class DiffStatusCallback extends DiffUtil.Callback {

    @Nullable
    private List<GeofencingStatus> oldStatuses;

    @Nullable
    private List<GeofencingStatus> newStatuses;

    public DiffStatusCallback(@Nullable List<GeofencingStatus> oldStatuses, @Nullable List<GeofencingStatus> newStatuses) {
        this.oldStatuses = oldStatuses;
        this.newStatuses = newStatuses;
    }

    @Override
    public int getOldListSize() {
        return oldStatuses != null ? oldStatuses.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newStatuses != null ? newStatuses.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        if (oldStatuses == null || newStatuses == null) {
            return false;
        }

        if (oldStatuses.get(oldItemPosition).airspace == null || newStatuses.get(newItemPosition).airspace == null) {
            return false;
        }

        return oldStatuses.get(oldItemPosition).airspace.equals(newStatuses.get(newItemPosition).airspace);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        if (oldStatuses == null || newStatuses == null) {
            return false;
        }

        GeofencingStatus oldStatus = oldStatuses.get(oldItemPosition);
        GeofencingStatus newStatus = newStatuses.get(newItemPosition);

        if (oldStatus.airspace == null || newStatus.airspace == null) {
            return false;
        }

        // check airspace matches
        if (!oldStatus.airspace.equals(newStatus.airspace)) {
            return false;
        }

        // check status hasn't changed
        if (oldStatus.level != newStatus.level) {
            return false;
        }

        if (oldStatus.proximity == null && newStatus.proximity == null) {
            return true;
        }

        // check proximity hasn't changed
        return oldStatus.proximity != null && newStatus.proximity != null && oldStatus.proximity.equals(newStatus.proximity);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
