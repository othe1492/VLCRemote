package ottehall.henrik.vlcremote;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ResourceBundle;

/**
 * Created by Henrik on 2015-11-25.
 */
public class PageAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PageAdapter(android.support.v4.app.FragmentManager fm, int numOfTabs)
    {
        super(fm);
        this.mNumOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position)
    {
        return ViewFragment.create(position);
    }

    @Override
    public int getCount()
    {
        return mNumOfTabs;
    }
}
