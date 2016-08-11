package com.braintreepayments.api.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class StringUtils {

    public static String getStringFromStream(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream,
                Charset.forName("UTF-8")));
        try {
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }

            return data.toString();
        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {}
        }
    }

    public static InputStream getStreamFromString(String string)
            throws UnsupportedEncodingException {
        return new ByteArrayInputStream(string.getBytes("UTF-8"));
    }
}
