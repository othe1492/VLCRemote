package ottehall.henrik.vlcremote;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Henrik on 2015-11-21.
 */
public class HTTPInterface
{
    private String mPassword;
    private String mAddress;
    private String mError;

    HTTPInterface(String address, String password)
    {
        if(address.contains("http://"))
        {
            mAddress = address;
        }
        else
        {
            mAddress = "http://" + address;
        }
        password = ":" + password;
        mPassword = "Basic " + new String(Base64.encode(password.getBytes(), Base64.DEFAULT));
        mError = "";
    }

    public String getError()
    {
        return mError;
    }

    // Parses and InputStream and puts it all into a string
    private String parseInputStreamToString(InputStream is) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String responseString = "", inputLine;

        while ((inputLine = reader.readLine()) != null)
        {
            responseString += inputLine + "\n";
        }
        return responseString;
    }

    // Used to send a command to the VLC HTTP interface
    // Returns a JSONObject containing information from VLC
    public JSONObject SendCommand(String command)
    {
        return toHTTP(mAddress + "/requests/status.json?command=" + command);
    }

    // Gets the playlist from a running VLC
    public JSONObject GetPlaylist()
    {
        return toHTTP(mAddress + "/requests/playlist.json");
    }

    // Connects to a running VLC HTTP interface and sends command
    // Returns a JSONObject containing information from VLC
    private JSONObject toHTTP(String address)
    {
        InputStream responseStream = null;
        HttpURLConnection connection = null;
        JSONObject jsonResponse = null;

        try
        {
            connection = (HttpURLConnection)new URL(address).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", mPassword);

            int responseCode = connection.getResponseCode();
            Log.d("RESPONSE", "Response code: " + Integer.toString(responseCode));

            if(responseCode == 200)
            {
                mError = "";
                responseStream = connection.getInputStream();
                String responseString = parseInputStreamToString(responseStream);
                jsonResponse = new JSONObject(responseString);
            }
            else if(responseCode == 401)
            {
                mError = "Unauthorized";
            }
            else if(responseCode == 404)
            {
                mError = "Not found";
            }
            else
            {
                mError = "Unknown error";
            }
        }
        catch (MalformedURLException e)
        {
            mError = "Incorrect address";
            Log.d("EXCEPTION", "Cause is " + e.toString());
        }
        catch (ConnectException e)
        {
            mError = "Timeout";
            Log.d("EXCEPTION", "Cause is " + e.toString());
        }
        catch (Exception e)
        {
            mError = "Unknown error";
            Log.d("EXCEPTION", "Cause is " + e.toString());
        }
        finally
        {
            if (responseStream != null) {
                try
                {
                    responseStream.close();
                }
                catch (IOException e)
                {
                    mError= "Error closing input from VLC";
                }
            }
            if (connection != null)
            {
                connection.disconnect();
            }
        }

        return jsonResponse;
    }
}
