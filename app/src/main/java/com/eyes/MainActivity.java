package com.eyes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;


import android.content.SharedPreferences;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends Activity implements View.OnClickListener {

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String sharedPreferenceFileName = "eye_data";
        sharedPref = getSharedPreferences(sharedPreferenceFileName, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        setContentView(R.layout.activity_main);
        findViewById(R.id.buttonStart).setOnClickListener(this);
        findViewById(R.id.buttonSettings).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStart:
                startActivity(new Intent(MainActivity.this, CameraPreviewActivity.class));
                finish();
                break;
            case R.id.buttonSettings:
                onSelectSettings();
                break;
        }
    }

    private void onSelectSettings() {
        if (sharedPref != null && editor != null) {

            Dialog dialogSelectDifficulty = new Dialog(this);
            dialogSelectDifficulty.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogSelectDifficulty.getWindow().setBackgroundDrawableResource(R.color.color_white);
            dialogSelectDifficulty.getWindow().getAttributes().gravity = Gravity.BOTTOM;
            dialogSelectDifficulty.setContentView(R.layout.select_difficulty_item);
            dialogSelectDifficulty.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels, LinearLayout.LayoutParams.WRAP_CONTENT);

            RadioGroup radioGroup = (RadioGroup) dialogSelectDifficulty.findViewById(R.id.radioGroupSelectDifficulty);
            ((RadioButton) radioGroup.getChildAt(sharedPref.getInt(getString(R.string.storedSelectDifficulty), 0))).setChecked(true);

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    int childCount = radioGroup.getChildCount();
                    for (int j = 0; j < childCount; j++) {
                        if ((radioGroup.getChildAt(j)).getId() == i) {
                            editor.putInt(getString(R.string.storedSelectDifficulty), j);
                            editor.commit();
                        }
                    }
                }
            });

            dialogSelectDifficulty.show();
        }
    }
}
