package com.sample.mdintegrationapp;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    public static final String IMPERIAL_UNITS = "imperial_units";
    public static final String ALLOW_EMPTY_BARCODE = "allow_empty_barcode";
    public static final String AUTOMATIC_DIM = "automatic_dim";
    public static final String AUTOMATIC_UPLOAD = "automatic_upload";
    public static final String REPORT_IMAGE = "report_image";
    public static final String URL = "url";
    public static final String RETRY_DELAY = "retry_delay";
    public static final String NUM_RETRIES = "num_retries";
    public static final String CONNECT_TIMEOUT = "connect_timeout";
    public static final String READ_TIMEOUT = "read_timeout";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    private static final String CONFIG_FILENAME = "config.txt";
    private static final String TAG = Config.class.getSimpleName();
    private static Properties mProperties;
    private static String mConfigDirectory;

    public static void setConfigDirectory(String configDirectory) {
        mConfigDirectory = configDirectory;
        Log.i(TAG, "Config directory: " + mConfigDirectory);
    }

    public static String getConfigDirectory() {
        return mConfigDirectory;
    }

    private static Properties loadProperties(String fileName) {
        Properties properties = new CaseInsensitiveProperties(null);

        String filePath = mConfigDirectory + fileName;
        File file = new File(filePath);
        if (file.exists() && file.canRead()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
            } catch (IOException e) {
                Log.e(TAG, "init", e);
                Log.e(TAG, "Failed to load config");
            }
        }
        return properties;
    }

    public static void init() {
        mProperties = loadProperties(CONFIG_FILENAME);
    }

    private static boolean setSetting(String key, String value, Properties properties, String configFileName) {
        if (value != null)
            properties.setProperty(key, value);
        else
            properties.remove(key);

        String filePath = mConfigDirectory + configFileName;
        File file = new File(filePath);
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                properties.store(fos, null);
                Log.d(TAG, "Wrote config " + filePath);
            }
        } catch (IOException e) {
            Log.e(TAG, "setSetting", e);
            Log.d(TAG, "Failed to write config " + filePath);
            return false;
        }
        return true;
    }

    public static boolean setSetting(String key, String value) {
        if (mProperties == null)
            init();
        return setSetting(key, value, mProperties, CONFIG_FILENAME);
    }

    public static String getSetting(String key) {
        if (null == mProperties)
            init();

        return mProperties.getProperty(key);
    }

    public static String getSetting(String key, String defaultValue) {
        if (null == mProperties)
            init();
        return mProperties.getProperty(key, defaultValue);
    }

    static class CaseInsensitiveProperties extends Properties {
        public CaseInsensitiveProperties(Properties defaultProperties) {
            super(defaultProperties);
        }

        public synchronized Object put(Object key, Object value) {
            return super.put(((String) key).toLowerCase(), value);
        }

        public String getProperty(String key) {
            return super.getProperty(key.toLowerCase());
        }

        public String getProperty(String key, String defaultValue) {
            return super.getProperty(key.toLowerCase(), defaultValue);
        }
    }
}