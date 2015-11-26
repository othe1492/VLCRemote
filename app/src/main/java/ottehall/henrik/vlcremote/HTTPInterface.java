package ottehall.henrik.vlcremote;

import android.util.Base64;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Henrik on 2015-11-21.
 */
public class HTTPInterface extends Thread
{
    private String mPassword;
    private String mAddress;
    private String mError;

    HTTPInterface(String address, String password)
    {
        if(address.contains("http://"))
        {
            mAddress = address + "/requests/status.json?command=";
        }
        else
        {
            mAddress = "http://" + address + "/requests/status.json?command=";
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

        if(is != null)
        {
            while ((inputLine = reader.readLine()) != null)
            {
                responseString += inputLine + "\n";
            }
        }
        return responseString;
    }

    // Connects to a running VLC HTTP interface and sends command
    // Returns a JSONObject containing information from VLC
    public JSONObject SendCommand(String command)
    {
        InputStream responseStream = null;
        HttpURLConnection connection = null;
        JSONObject jsonResponse = null;
        try
        {
            connection = (HttpURLConnection)new URL(mAddress + command).openConnection();
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
                mError = "Unable to connect: Unauthorized";
            }
            else if(responseCode == 404)
            {
                mError = "Unable to connect: Not found";
            }
            else
            {
                mError = "Unknown connection error";
            }
        }
        catch (java.net.MalformedURLException e)
        {
            mError = "Unable to connect: Incorrect address";
            Log.d("EXCEPTION", "Cause is " + e.toString());
        }
        catch (java.net.ConnectException e)
        {
            mError = "Unable to connect: Timeout";
            Log.d("EXCEPTION", "Cause is " + e.toString());
        }
        catch (Exception e)
        {
            mError = "Unknown connection error";
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

    public void run()
    {
        //Entry point for Thread.start()
    }
}
