package com.a6v.pagingadapter.sample.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.a6v.pagingadapter.sample.R;

public class SamplesActivity extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle(R.string.activity_main_label);
    setContentView(R.layout.activity_samples);
  }

  public static class SamplesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
      addPreferencesFromResource(R.xml.samples);
    }
  }
}
