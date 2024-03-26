package io.nadeshiko.nadeshiko.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * @author chloe
 * @since March 11, 2024
 */
@UtilityClass
public class HTTPUtil {

	/**
	 * Launch a GET request to a given URL
	 *
	 * @param urlString The URL to request.
	 * @return A {@link Response} representing the response of the request
	 * @throws IOException If the request failed for some reason
	 */
	public Response get(@NonNull String urlString) throws IOException {
		return get(urlString, null);
	}

	/**
	 * Launch a GET request to a given URL with the provided headers
	 *
	 * @param urlString The URL to request.
	 * @param headers A {@code Map<String, String>} of headers - key and value - to include in the request
	 * @return A {@link Response} representing the response of the request
	 * @throws IOException If the request failed for some reason
	 */
	public Response get(@NonNull String urlString, Map<String, String> headers) throws IOException {

		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);

		// Add the headers to the request, if any were provided
		if (headers != null) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				connection.setRequestProperty(header.getKey(), header.getValue());
			}
		}

		// Read the status of the response and pick the appropriate stream to read from
		int status = connection.getResponseCode();
		Reader streamReader = new InputStreamReader(
			status > 299 ? connection.getErrorStream() : connection.getInputStream());

		BufferedReader bufferedReader = new BufferedReader(streamReader);

		// Read the response from the stream
		String line;
		StringBuilder content = new StringBuilder();
		while ((line = bufferedReader.readLine()) != null) {
			content.append(line);
		}

		// Close the streams and the connection
		streamReader.close();
		bufferedReader.close();
		connection.disconnect();

		return new Response(status, content.toString());
	}

	/**
	 * Launch a GET request to a given URL
	 *
	 * @param urlString The URL to request.
	 * @return A {@link RawResponse} representing the response of the request
	 * @throws IOException If the request failed for some reason
	 */
	public RawResponse getRaw(@NonNull String urlString) throws IOException {
		return getRaw(urlString, null);
	}

	/**
	 * Launch a GET request to a given URL with the provided headers
	 *
	 * @param urlString The URL to request.
	 * @param headers A {@code Map<String, String>} of headers - key and value - to include in the request
	 * @return A {@link RawResponse} representing the response of the request
	 * @throws IOException If the request failed for some reason
	 */
	public RawResponse getRaw(@NonNull String urlString, Map<String, String> headers) throws IOException {

		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);

		// Add the headers to the request, if any were provided
		if (headers != null) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				connection.setRequestProperty(header.getKey(), header.getValue());
			}
		}

		// Read the status of the response and pick the appropriate stream to read from
		int status = connection.getResponseCode();
		InputStream inputStream = status > 299 ? connection.getErrorStream() : connection.getInputStream();
		byte[] data = inputStream.readAllBytes();

		// Close the connection
		connection.disconnect();

		return new RawResponse(status, data);
	}

	/**
	 * Record representation of a request response (string)
	 * @param status The status code returned
	 * @param response The data returned
	 */
	public record Response(int status, String response) {
	}

	/**
	 * Record representation of a raw request response
	 * @param status The status code returned
	 * @param response The data returned
	 */
	public record RawResponse(int status, byte[] response) {
	}
}
