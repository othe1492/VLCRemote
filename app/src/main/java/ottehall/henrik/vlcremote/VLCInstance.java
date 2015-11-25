package ottehall.henrik.vlcremote;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Henrik on 2015-11-23.
 */
public class VLCInstance
{
    private HTTPInterface mConnection;
    private String mVideoName;
    private int mCurrentTime;
    private int mLength;
    private Commands mCommand;

    public VLCInstance(String address, String password)
    {
        mConnection = new HTTPInterface(address, password);
        mConnection.start();
    }

    private void parseResponse(JSONObject response)
    {
        try
        {
            mVideoName = response.getJSONObject("information").getJSONObject("meta").getString("filename");
            mCurrentTime = response.getInt("time");
            mLength = response.getInt("length");
        }
        catch (JSONException e)
        {

        }

    }
}
