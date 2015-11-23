package ottehall.henrik.vlcremote;

import android.util.Base64;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Henrik on 2015-11-21.
 */
public class HTTPInterface extends Thread
{
    private String m_password;
    private String m_address;
    private String m_videoName;
    private int m_currentTime;
    private int m_length;
    private String m_error;
    private Commands m_command;

    HTTPInterface(String address, String password)
    {
        m_address = address;
        m_password = password;
        m_error = "";
    }

    public String getVideoName()
    {
        return m_videoName;
    }

    public int getVideoLength()
    {
        return m_length;
    }

    public int getCurrentTime()
    {
        return m_currentTime;
    }

    public String getError()
    {
        return m_error;
    }

    public void setCommand(Commands command)
    {
        m_command = command;

    }

    private void toHTTP(String command)
    {
        String url = "http://"  + m_address + "/requests/status.json?command=" + command;
        HttpURLConnection connection;

        try
        {
            connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("GET");

            String authInfo = ":" + m_password;
            authInfo = "Basic " + new String(Base64.encode(authInfo.getBytes(), Base64.DEFAULT));
            connection.setRequestProperty("Authorization", authInfo);

            int responseCode = connection.getResponseCode();
            Log.d("RESPONSE", "Response code: " + Integer.toString(responseCode));

            if(responseCode == 200)
            {
                m_error = "";
                parseResponse(connection.getInputStream());
            }
            else if(responseCode == 401)
            {
                m_error = "Unable to connect: Unauthorized";
            }
            else if(responseCode == 404)
            {
                m_error = "Unable to connect: Not found";
            }
            else
            {
                m_error = "Unknown connection error";
            }
        }
        catch (java.net.MalformedURLException e)
        {
            m_error = "Unable to connect: Incorrect address";
            Log.d("EXCEPTION", "Cause is " + e.toString());
        }
        catch (java.net.ConnectException e)
        {
            m_error = "Unable to connect: Timeout";
            Log.d("EXCEPTION", "Cause is " + e.toString());
        }
        catch (Exception e)
        {
            m_error = "Unknown connection error";
            Log.d("EXCEPTION", "Cause is " + e.toString());
        }
    }

    private void parseResponse(InputStream response)
    {
        if(response != null)
        {
            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                String responseString = "", inputLine;

                while((inputLine = reader.readLine()) != null)
                {
                    responseString += inputLine + "\n";
                }

                JSONObject jsonResponse = new JSONObject(responseString);
                m_videoName = jsonResponse.getJSONObject("information").getJSONObject("category").getJSONObject("meta").getString("filename");
                m_currentTime = jsonResponse.getInt("time");
                m_length = jsonResponse.getInt("length");
            }
            catch(Exception e)
            {
                Log.d("EXCEPTION", "Cause: " + e.toString());
            }
        }
    }

    public void run()
    {
        switch (m_command) {
            case play:
                toHTTP("pl_forceresume");
                break;
            case pause:
                toHTTP("pl_forcepause");
                break;
            case stop:
                toHTTP("pl_stop");
                break;
            case next:
                toHTTP("pl_next");
                break;
            case previous:
                toHTTP("pl_previous");
                break;
            case toggleFullscreen:
                toHTTP("fullscreen");
                break;
            case update:
                toHTTP("");
                break;
        }
    }
}
