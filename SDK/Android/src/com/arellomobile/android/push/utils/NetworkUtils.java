//
//  NetworkUtils.java
//
// Pushwoosh Push Notifications SDK
// www.pushwoosh.com
//
// MIT Licensed
package com.arellomobile.android.push.utils;

import android.util.Log;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Date: 16.08.12
 * Time: 20:38
 *
 * @author mig35
 */
public class NetworkUtils
{
	private static final String TAG = "PushWoosh: NetworkUtils";

	public static final int MAX_TRIES = 5;

	public static final String PUSH_VERSION = "1.3";
	public static final String BASE_URL = "https://cp.pushwoosh.com/json/" + PUSH_VERSION + "/";


	public static NetworkResult makeRequest(Map<String, Object> data, String urlString) throws Exception
	{
		NetworkResult result = new NetworkResult(500, null);
		OutputStream connectionOutput = null;
		InputStream inputStream = null;
		try
		{
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

			connection.setDoOutput(true);


			JSONObject innerRequestJson = new JSONObject();

			for (String key : data.keySet())
			{
				innerRequestJson.put(key, data.get(key));
			}

			JSONObject requestJson = new JSONObject();
			requestJson.put("request", innerRequestJson);

			connection.setRequestProperty("Content-Length", String.valueOf(requestJson.toString().getBytes().length));

			connectionOutput = connection.getOutputStream();
			connectionOutput.write(requestJson.toString().getBytes());
			connectionOutput.flush();
			connectionOutput.close();

			inputStream = new BufferedInputStream(connection.getInputStream());

			ByteArrayOutputStream dataCache = new ByteArrayOutputStream();

			// Fully read data
			byte[] buff = new byte[1024];
			int len;
			while ((len = inputStream.read(buff)) >= 0)
			{
				dataCache.write(buff, 0, len);
			}

			// Close streams
			dataCache.close();

			String jsonString = new String(dataCache.toByteArray()).trim();
			Log.w(TAG, "PushWooshResult: " + jsonString);

			JSONObject resultJSON = new JSONObject(jsonString);

			result.setData(resultJSON);
			result.setCode(resultJSON.getInt("status_code"));
		}
		finally
		{
			if (null != inputStream)
			{
				inputStream.close();
			}
			if (null != connectionOutput)
			{
				connectionOutput.close();
			}
		}

		return result;
	}

	public static class NetworkResult
	{
		private int mResultCode;
		private JSONObject mResultData;

		public NetworkResult(int code, JSONObject data)
		{
			mResultCode = code;
			mResultData = data;
		}

		public void setCode(int code)
		{
			mResultCode = code;
		}

		public void setData(JSONObject data)
		{
			mResultData = data;
		}

		public int getResultCode()
		{
			return mResultCode;
		}

		public JSONObject getResultData()
		{
			return mResultData;
		}
	}
}