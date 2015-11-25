package ottehall.henrik.vlcremote;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Henrik on 2015-11-23.
 */
public class VLCInstance
{
    private String m_videoName;
    private int m_currentTime;
    private int m_length;
    private Commands m_command;

    private void parseResponse(JSONObject response)
    {
        try
        {
            m_videoName = response.getJSONObject("information").getJSONObject("meta").getString("filename");
            m_currentTime = response.getInt("time");
            m_length = response.getInt("length");
        }
        catch (JSONException e)
        {

        }

    }
}
