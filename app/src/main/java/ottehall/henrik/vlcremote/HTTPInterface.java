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
    private String m_password;
    private String m_address;
    private String m_error;

    HTTPInterface(String address, String password)
    {
        if(address.contains("http://"))
        {
            m_address = address + "/requests/status.xml?command=";
        }
        else
        {
            m_address = "http://" + address + "/requests/status.xml?command=";
        }
        m_password = password;
        m_error = "";
    }

    public String getError()
    {
        return m_error;
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
    private JSONObject toHTTP(String command)
    {
        InputStream responseStream = null;
        HttpURLConnection connection = null;
        JSONObject jsonResponse = null;
        try
        {
            connection = (HttpURLConnection)new URL(m_address + command).openConnection();
            connection.setRequestMethod("GET");

            String authInfo = ":" + m_password;
            authInfo = "Basic " + new String(Base64.encode(authInfo.getBytes(), Base64.DEFAULT));
            connection.setRequestProperty("Authorization", authInfo);

            int responseCode = connection.getResponseCode();
            Log.d("RESPONSE", "Response code: " + Integer.toString(responseCode));

            if(responseCode == 200)
            {
                m_error = "";
                responseStream = connection.getInputStream();
                String responseString = parseInputStreamToString(responseStream);
                jsonResponse = new JSONObject(responseString);
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
        finally
        {
            if (responseStream != null) {
                try
                {
                    responseStream.close();
                }
                catch (IOException e)
                {
                    m_error= "Error closing input from VLC";
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
        toHTTP("pl_pause");
    }
}
