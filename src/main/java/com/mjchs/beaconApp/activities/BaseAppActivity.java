package com.mjchs.beaconApp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.mjchs.beaconApp.R;

public abstract class BaseAppActivity extends AppCompatActivity {

  protected Toolbar toolbar;

  protected abstract int getLayoutResId();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getLayoutResId());
  }
}
