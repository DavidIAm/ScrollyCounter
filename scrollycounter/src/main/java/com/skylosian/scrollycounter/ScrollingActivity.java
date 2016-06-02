package com.skylosian.scrollycounter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.math.BigInteger;

public class ScrollingActivity extends Activity {

    public ScrollPosition position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_scrolling);
        ScrollerSurface view = (ScrollerSurface)findViewById(R.id.scroller);
        view.setPosition(position = new ScrollPosition(Long.valueOf(this.getPreferences(MODE_PRIVATE).getString("mils", "0"))));

        view.invalidate();
    }

    @Override
    public void onPause() {
        SharedPreferences pref = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("mils", ((ScrollerSurface)findViewById(R.id.scroller)).getPosition().getPosition().toString());
        edit.apply();
        super.onPause();
    }

    public void setPosition(ScrollPosition newposition) {
        position = newposition;
    }
}
