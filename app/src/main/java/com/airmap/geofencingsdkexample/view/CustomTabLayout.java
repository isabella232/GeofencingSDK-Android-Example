package com.airmap.geofencingsdkexample.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.ColorRes;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.TextView;

import com.airmap.geofencingsdkexample.R;

import java.util.Objects;

public class CustomTabLayout extends TabLayout {

    private Tab intersectingTab;
    private Tab enteringTab;
    private Tab approachingTab;

    public CustomTabLayout(Context context) {
        super(context);

        init();
    }

    public CustomTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public CustomTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        intersectingTab = newTab().setCustomView(R.layout.view_tab);
        TextView intersectingTextView = Objects.requireNonNull(intersectingTab.getCustomView()).findViewById(R.id.title_text_view);
        intersectingTextView.setText("Intersecting");

        enteringTab = newTab().setCustomView(R.layout.view_tab);
        TextView enteringTextView = Objects.requireNonNull(enteringTab.getCustomView()).findViewById(R.id.title_text_view);
        enteringTextView.setText("Entering");

        approachingTab = newTab().setCustomView(R.layout.view_tab);
        TextView approachingTextView = Objects.requireNonNull(approachingTab.getCustomView()).findViewById(R.id.title_text_view);
        approachingTextView.setText("Approaching");

        // add tabs
        addTab(intersectingTab);
        addTab(enteringTab);
        addTab(approachingTab);
    }

    public void setIntersecting(int badgeNumber, @ColorRes int colorRes) {
        setBadgeNumber(intersectingTab, badgeNumber);
        setBadgeColor(intersectingTab, colorRes);
    }

    public void setEntering(int badgeNumber, @ColorRes int colorRes) {
        setBadgeNumber(enteringTab, badgeNumber);
        setBadgeColor(enteringTab, colorRes);
    }

    public void setApproaching(int badgeNumber, @ColorRes int colorRes) {
        setBadgeNumber(approachingTab, badgeNumber);
        setBadgeColor(approachingTab, colorRes);
    }

    private void setBadgeColor(Tab tab, @ColorRes int colorRes) {
        int color = ContextCompat.getColor(getContext(), colorRes);
        Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.badge_text_view).getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    @SuppressLint("SetTextI18n")
    private void setBadgeNumber(Tab tab, int number) {
        ((TextView) Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.badge_text_view)).setText(Integer.toString(number));
    }
}
