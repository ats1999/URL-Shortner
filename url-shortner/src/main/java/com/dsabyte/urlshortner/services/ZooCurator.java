package com.dsabyte.urlshortner.services;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.time.Duration;
import java.util.Base64;

public class ZooCurator {
    private static volatile CuratorFramework client;

    private static boolean isClientConnected() {
        return client != null && client.getState() == CuratorFrameworkState.STARTED;
    }

    public static CuratorFramework getClient() {
        if (isClientConnected()) {
            return client;
        }

        synchronized (ZooCurator.class) {
            // https://en.wikipedia.org/wiki/Double-checked_locking
            if (!isClientConnected()) {
                int baseSleepTimeMs = (int) Duration.ofSeconds(1).toMillis();
                int maxRetry = 3;
                String zkConnectionString = System.getenv("ZK_CONNECTION_STRING");
                RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetry);

                client = CuratorFrameworkFactory
                        .newClient(zkConnectionString, retryPolicy);

                client.start();
            }

            return client;
        }
    }

    public static InterProcessMutex getLock(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return new InterProcessMutex(getClient(), path);
    }

    public static InterProcessMutex getLock(String path, boolean encodePathToBase64) {
        if (encodePathToBase64) {
            // urls needs to be encoded in base64 due to unwanted characters which could result as invalid path
            path = Base64
                    .getEncoder()
                    .encodeToString(path.getBytes());
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return new InterProcessMutex(getClient(), path);
    }
}
