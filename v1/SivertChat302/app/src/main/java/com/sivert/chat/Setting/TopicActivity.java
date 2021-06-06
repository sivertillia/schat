package com.sivert.chat.Setting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sivert.chat.R;

public class TopicActivity extends AppCompatActivity {

    RadioGroup radio_topic;
    RadioButton theme_light, theme_dark;

    boolean b_radio_topic;

    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        radio_topic = findViewById(R.id.radio_topic);
        theme_light = findViewById(R.id.theme_light);
        theme_dark = findViewById(R.id.theme_dark);
        TextView selection = (TextView) findViewById(R.id.selection);

        settings = getSharedPreferences("PREFS", MODE_PRIVATE);
        b_radio_topic = settings.getBoolean("TOPIC", true);

        if (b_radio_topic) {
            theme_light.setChecked(true);
            b_radio_topic = true;
            selection.setText("Выбраная Светлая тема!");
        } else {
            theme_dark.setChecked(true);
            b_radio_topic = false;
            selection.setText("Выбраная Темная тема!");
        }


        radio_topic.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                TextView selection = (TextView) findViewById(R.id.selection);
                switch (checkedId) {
                    case R.id.theme_light:
                        light();
                        selection.setText("Выбраная Светлая тема!");
                        break;
                    case R.id.theme_dark:
                        dark();
                        selection.setText("Выбраная Темная тема!");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void saveSetting(String s, boolean b) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(s, b);
        editor.apply();
    }

    public void light() {
        b_radio_topic = true;
        saveSetting("TOPIC", b_radio_topic);
    }
    public void dark() {
        b_radio_topic = false;
        saveSetting("TOPIC", b_radio_topic);
    }

}