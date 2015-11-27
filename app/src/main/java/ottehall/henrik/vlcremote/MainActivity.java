package ottehall.henrik.vlcremote;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity
{

    VLCInstance instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Connection"));
        tabLayout.addTab(tabLayout.newTab().setText("Controls"));
        tabLayout.addTab(tabLayout.newTab().setText("Playlist"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Set up the ViewPager for sliding pages
        final ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        final PageAdapter adapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        // Set adapters and listeners for page changing
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                if(instance != null)
                {
                    instance.setVisible(tab.getPosition());
                    if(tab.getPosition() == 2)
                    {
                        instance.setCommand(Commands.getPlayList);
                    }
                }
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });
    }

    public void startConnection(View view)
    {
        EditText address = (EditText)findViewById(R.id.txtAdress);
        EditText password = (EditText)findViewById(R.id.txtPassword);
        instance = new VLCInstance(this, address.getText().toString(), password.getText().toString());
        instance.start();
    }

    public void controlButtonClick(View view)
    {
        Commands command = Commands.none;

        if(view == findViewById(R.id.btnPrevious))
        {
            instance.setCommand(Commands.previous);
        }
        else if(view == findViewById(R.id.btnPlay))
        {
            instance.setCommand(Commands.play);
        }
        else if(view == findViewById(R.id.btnPause))
        {
            instance.setCommand(Commands.pause);
        }
        else if(view == findViewById(R.id.btnStop))
        {
            instance.setCommand(Commands.stop);
        }
        else if(view == findViewById(R.id.btnNext))
        {
            instance.setCommand(Commands.next);
        }

    }
}