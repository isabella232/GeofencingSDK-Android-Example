package com.airmap.geofencingsdkexample.view;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.airmap.geofencingsdk.status.GeofencingStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationPagerAdapter extends PagerAdapter {

    private RecyclerView intersectingRecyclerView;
    private RecyclerView enteringRecyclerView;
    private RecyclerView approachingRecyclerView;

    private StatusRecyclerAdapter intersectingRecyclerAdapter;
    private StatusRecyclerAdapter enteringRecyclerAdapter;
    private StatusRecyclerAdapter approachingRecyclerAdapter;

    public NotificationPagerAdapter(Context context) {
        intersectingRecyclerAdapter = new StatusRecyclerAdapter();
        enteringRecyclerAdapter = new StatusRecyclerAdapter();
        approachingRecyclerAdapter = new StatusRecyclerAdapter();

        intersectingRecyclerView = new RecyclerView(context);
        intersectingRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        intersectingRecyclerView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        intersectingRecyclerView.setAdapter(intersectingRecyclerAdapter);

        enteringRecyclerView = new RecyclerView(context);
        enteringRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        enteringRecyclerView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        enteringRecyclerView.setAdapter(enteringRecyclerAdapter);

        approachingRecyclerView = new RecyclerView(context);
        approachingRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        approachingRecyclerView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        approachingRecyclerView.setAdapter(approachingRecyclerAdapter);
    }

    public void setStatuses(List<GeofencingStatus> statuses) {
        Set<GeofencingStatus> intersecting = new HashSet<>();
        Set<GeofencingStatus> entering = new HashSet<>();
        Set<GeofencingStatus> approaching = new HashSet<>();

        for (GeofencingStatus status : statuses) {
            switch (status.level) {
                case INTERSECTING: {
                    intersecting.add(status);
                    break;
                }
                case ENTERING: {
                    entering.add(status);
                    break;
                }
                case APPROACHING: {
                    approaching.add(status);
                    break;
                }
            }
        }

        Parcelable intersectingViewState = intersectingRecyclerView.getLayoutManager().onSaveInstanceState();
        intersectingRecyclerAdapter.setStatuses(intersecting);
        intersectingRecyclerView.getLayoutManager().onRestoreInstanceState(intersectingViewState);

        Parcelable enteringViewState = enteringRecyclerView.getLayoutManager().onSaveInstanceState();
        enteringRecyclerAdapter.setStatuses(entering);
        enteringRecyclerView.getLayoutManager().onRestoreInstanceState(enteringViewState);

        Parcelable approachingViewState = approachingRecyclerView.getLayoutManager().onSaveInstanceState();
        approachingRecyclerAdapter.setStatuses(approaching);
        approachingRecyclerView.getLayoutManager().onRestoreInstanceState(approachingViewState);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view;
        switch (position) {
            case 0:
                view = intersectingRecyclerView;
                break;
            case 1:
                view = enteringRecyclerView;
                break;
            case 2:
                view = approachingRecyclerView;
                break;
            default:
                throw new RuntimeException("Invalid tab creation at: " + position);
        }

        container.addView(view);

        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return 3;
    }
}
