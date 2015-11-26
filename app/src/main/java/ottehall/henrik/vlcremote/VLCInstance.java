package ottehall.henrik.vlcremote;

import android.app.Activity;
import android.os.Message;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import org.json.JSONObject;

/**
 * Created by Henrik on 2015-11-23.
 */
public class VLCInstance extends Thread
{
    private boolean mVisible;
    private boolean mConnected;
    private Activity mMainActivity;
    private HTTPInterface mConnection;
    private String mVideoName;
    private int mCurrentTime;
    private int mLength;
    private int mTiming;
    private Commands mCommand;

    public VLCInstance(Activity activity, String address, String password)
    {
        mMainActivity = activity;
        mConnection = new HTTPInterface(address, password);
        mCommand = Commands.update;
        mCurrentTime = 0;
        mLength = 0;
        mVideoName = "";
        mVisible = true; // TODO: Updated dynamically
        mConnected = true; // TODO: Updated dynamically
    }

    public void setCommand(Commands command)
    {
        mCommand = command;
    }

    public void run()
    {
        Log.d("DEBUG", "Started VLCInstance");
        mTiming = 60;
        mTimeHandler.sendMessageDelayed(Message.obtain(mTimeHandler), 1000);
        Log.d("DEBUG", "Started timeHandler");
        runLoop();
    }

    private void runLoop()
    {
        while(true)
        {
            switch (mCommand)
            {
                case update:
                    parseResponse(mConnection.SendCommand(""));
                    UpdateUI();
                    mCommand = Commands.none;
                    break;
            }
        }
    }

    private Handler mTimeHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mVisible && mConnected) {
                if(mTiming >= 60) {
                    mCommand = Commands.update;
                    mTiming = 0;
                }
                else {
                    ++mCurrentTime;
                    ++mTiming;
                    UpdateUI();
                }
                sendMessageDelayed(Message.obtain(this), 1000);
            }
        }
    };

    private void UpdateUI()
    {
        View view = mMainActivity.findViewById(R.id.controls);
        final String timeString = timeStringFromSeconds(mCurrentTime) + "/" + timeStringFromSeconds(mLength);

        view.post(new Runnable() {
            @Override
            public void run()
            {
                TextView title = (TextView)mMainActivity.findViewById(R.id.txtTitle);
                title.setText(mVideoName);

                TextView time = (TextView)mMainActivity.findViewById(R.id.txtTimer);
                time.setText(timeString);
            }
        });
    }

    private String timeStringFromSeconds(int seconds)
    {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        if(hours > 0)
        {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        else if(minutes > 0)
        {
            return String.format("%02d:%02d", minutes, seconds);
        }
        else
        {
            return String.format("%02d", seconds);
        }
    }

    private void parseResponse(JSONObject response)
    {
        try
        {
            mVideoName = response.getJSONObject("information").getJSONObject("category").getJSONObject("meta").getString("filename");
            mCurrentTime = response.getInt("time");
            mLength = response.getInt("length");
        }
        catch (Exception e)
        {
            Log.d("EXCEPTION", "Cause is " + e.toString());
        }
    }
}
