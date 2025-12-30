package org.leolo.nrinfo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpRequestUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpRequestUtil.class);

    public static String sendSimpleRequest(String url) throws IOException {
        return sendSimpleRequest(new URL(url));
    }

    //TODO: Add in optional authentication
    public static InputStream sendSimpleRequestAsStream(String url) throws IOException {
        logger.debug("Sending GET request to {}", url.toString());
        HttpClient.Builder builder = HttpClient.newBuilder();
        builder.followRedirects(HttpClient.Redirect.ALWAYS);
        try (
                HttpClient httpClient = builder.build()
        ) {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] bytes = response.body();
            logger.debug("Response size: {}B", bytes.length);
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Response status code "+response.statusCode());
            }
            return new ByteArrayInputStream(bytes);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static String sendSimpleRequest(URL url) throws IOException {
        logger.debug("Sending GET request to {}", url.toString());
        URLConnection httpURLConnection = url.openConnection();
        long startTime = System.currentTimeMillis();
        httpURLConnection.connect();
        byte [] body = StreamUtils.copyToByteArray(httpURLConnection.getInputStream());
        long endTime = System.currentTimeMillis();
        logger.info("{} byte(s) received from {}. Time taken {} ms", body.length, url.getHost(), (endTime-startTime));
        return new String(body);
    }

    public static InputStream sendSimpleRequestAsStream(String url, String username, String password) throws IOException {
        logger.debug("Sending GET request to {}", url.toString());
        HttpClient.Builder builder = HttpClient.newBuilder();
        builder.followRedirects(HttpClient.Redirect.ALWAYS).authenticator(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        username,
                        password.toCharArray()
                );
            }
        });
        try (
                HttpClient httpClient = builder.build()
        ) {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] bytes = response.body();
            logger.debug("Response size: {}B", bytes.length);
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Response status code "+response.statusCode());
            }
            return new ByteArrayInputStream(bytes);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



}
