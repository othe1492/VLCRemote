package ottehall.henrik.vlcremote;

import android.app.Activity;
import android.os.Message;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Henrik on 2015-11-23.
 */
public class VLCInstance extends Thread
{
    // After how many seconds to get update from VLC server
    private static final int TIME_FOR_UPDATE = 60;

    private int mTabVisible;
    private boolean mConnected;
    private Activity mMainActivity;
    private HTTPInterface mConnection;
    private String mVideoName;
    private int mCurrentTime;
    private int mLength;
    private List<String> mPlaylist;
    private int mUpdateTiming;
    private Commands mCommand;

    public VLCInstance(Activity activity, String address, String password)
    {
        mMainActivity = activity;
        mConnection = new HTTPInterface(address, password);
        mCommand = Commands.update;
        mTabVisible = 0;
        mCurrentTime = 0;
        mLength = 0;
        mVideoName = "";
        mPlaylist = new ArrayList<String>();
    }

    public void setCommand(Commands command)
    {
        mCommand = command;
    }

    public void setVisible(int visible)
    {
        mTabVisible = visible;
        if(mTabVisible == 1)
        {
            mUpdateTiming = TIME_FOR_UPDATE;
            mTimeHandler.sendMessageDelayed(Message.obtain(mTimeHandler), 1000);
        }
    }

    public void run()
    {
        mUpdateTiming = TIME_FOR_UPDATE;
        mTimeHandler.sendMessageDelayed(Message.obtain(mTimeHandler), 1000);
        runLoop();
    }

    private void runLoop()
    {
        try {
            do {
                switch (mCommand) {
                    case none:
                        break;
                    case play:
                        Log.d("COMMAND", mCommand.toString());
                        sendCommandAndParse("pl_forceresume");
                        mCommand = Commands.none;
                        break;
                    case pause:
                        Log.d("COMMAND", mCommand.toString());
                        sendCommandAndParse("pl_forcepause");
                        mCommand = Commands.none;
                        break;
                    case stop:
                        Log.d("COMMAND", mCommand.toString());
                        sendCommandAndParse("pl_stop");
                        mCommand = Commands.none;
                        break;
                    case next:
                        Log.d("COMMAND", mCommand.toString());
                        sendCommandAndParse("pl_next");
                        mCommand = Commands.none;
                        break;
                    case previous:
                        Log.d("COMMAND", mCommand.toString());
                        sendCommandAndParse("pl_previous");
                        mCommand = Commands.none;
                        break;
                    case update:
                        Log.d("COMMAND", mCommand.toString());
                        sendCommandAndParse("");
                        mCommand = Commands.none;
                        break;
                    case toggleFullscreen:
                        Log.d("COMMAND", mCommand.toString());
                        sendCommandAndParse("fullscreen");
                        mCommand = Commands.none;
                        break;
                    case getPlayList:
                        Log.d("COMMAND", mCommand.toString());
                        getPlaylist();
                        mCommand = Commands.none;
                        break;
                }
            } while (mConnected);
        }
        catch (Exception e)
        {
            Log.d("EXCEPTION", "Cause is " + e.toString());
        }
    }

    // Gets the playlist and saves it in the mPlaylist array
    private void getPlaylist() throws JSONException
    {
        JSONObject playlistObject = mConnection.GetPlaylist();

        if(playlistObject != null)
        {
            JSONArray playlistArray = playlistObject.getJSONArray("children");
            playlistObject = (JSONObject) playlistArray.get(0);
            playlistArray = playlistObject.getJSONArray("children");

            for (int i = 0; i < playlistArray.length(); ++i) {
                playlistObject = (JSONObject) playlistArray.get(i);
                mPlaylist.add(playlistObject.getString("name"));
            }

            mConnected = true;
            UpdatePlaylist();
        }
        else
        {
            mConnected = false;
        }
    }

    // Send a command to the VLC HTTP interface and parse the response JSON
    private void sendCommandAndParse(String command) throws JSONException
    {
        JSONObject responseJSON = mConnection.SendCommand(command);

        if(responseJSON != null)
        {
            mVideoName = responseJSON.getJSONObject("information").getJSONObject("category").getJSONObject("meta").getString("filename");
            mCurrentTime = responseJSON.getInt("time");
            mLength = responseJSON.getInt("length");
            mConnected = true;
            UpdateControlsUI();
        }
        else
        {
            mConnected = false;
        }
    }

    // Updates the currentTime timer text every second
    private Handler mTimeHandler = new Handler() {
        public void handleMessage(Message m) {
            if (mTabVisible == 1 && mConnected) {
                if(mUpdateTiming >= TIME_FOR_UPDATE || mCurrentTime >= mLength) {
                    mCommand = Commands.update;
                    mUpdateTiming = 0;
                }
                else {
                    ++mCurrentTime;
                    ++mUpdateTiming;
                    UpdateControlsUI();
                }
                sendMessageDelayed(Message.obtain(this), 1000);
            }
        }
    };

    private void UpdatePlaylist()
    {
        if(mConnected && mTabVisible == 2)
        {
            View view = mMainActivity.findViewById(R.id.playlistLayout);

            view.post(new Runnable() {
                @Override
                public void run() {
                    ListView lv = (ListView)mMainActivity.findViewById(R.id.playlistView);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            mMainActivity,
                            android.R.layout.simple_list_item_1,
                            android.R.id.text1,
                            mPlaylist);
                    lv.setAdapter(adapter);
                }
            });
        }
    }

    private void UpdateControlsUI()
    {
        if(mConnected)
        {
            View view = mMainActivity.findViewById(R.id.controls);
            final String timeString = timeStringFromSeconds(mCurrentTime) + "/" + timeStringFromSeconds(mLength);

            view.post(new Runnable() {
                @Override
                public void run() {
                    TextView title = (TextView) mMainActivity.findViewById(R.id.txtTitle);
                    title.setText(mVideoName);

                    TextView time = (TextView) mMainActivity.findViewById(R.id.txtTimer);
                    time.setText(timeString);
                }
            });
        }
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
}
