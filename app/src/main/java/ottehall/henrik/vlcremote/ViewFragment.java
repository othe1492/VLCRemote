package ottehall.henrik.vlcremote;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Henrik on 2015-11-25.
 */
public class ViewFragment extends Fragment {
    public static final String ARG_PAGE = "page";
    private int mPageNumber;

    public static ViewFragment create(int pageNumber)
    {
        ViewFragment fragment = new ViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup view = null;

        switch (mPageNumber)
        {
            case 0:
                view = (ViewGroup) inflater.inflate(R.layout.connect_activity, container, false);
                break;
            case 1:
                view = (ViewGroup) inflater.inflate(R.layout.controls_activity, container, false);
                break;
            case 2:
                view = (ViewGroup) inflater.inflate(R.layout.playlist_activity, container, false);
                break;
        }

        return view;
    }

}
