package ottehall.henrik.vlcremote;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SliderActivity slider;
        Intent sliderIntent = new Intent(this, SliderActivity.class);

        startActivity(sliderIntent);
    }
}