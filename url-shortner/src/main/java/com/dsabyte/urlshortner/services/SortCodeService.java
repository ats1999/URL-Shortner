package com.dsabyte.urlshortner.services;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.KeeperException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SortCodeService {
    private static long counter = 0;
    private static long maxCounter = -1;
    private static final String BASE62_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String getSortCode() throws Exception {
        if (counter > maxCounter) {
            assignNewCounterRange();
        }

        String base62Sortcode = encodeBase62(getCounter());
        int sortCodeLength = 7;

        base62Sortcode = "0"
                .repeat(sortCodeLength - base62Sortcode.length()) + base62Sortcode;

        return base62Sortcode;
    }

    private static String encodeBase62(long number) {
        StringBuilder sb = new StringBuilder();
        int base = BASE62_CHARACTERS.length();

        while (number > 0) {
            int index = (int) (number % base);
            sb.insert(0, BASE62_CHARACTERS.charAt(index));
            number /= base;
        }

        return sb.toString();
    }

    private static synchronized long getCounter() {
        return counter++;
    }

    private static synchronized void assignNewCounterRange() throws Exception {
        // https://en.wikipedia.org/wiki/Double-checked_locking
        if (counter <= maxCounter) {
            return;
        }

        CuratorFramework client = ZooCurator.getClient();
        InterProcessMutex lock = ZooCurator.getLock("/counter_update");

        try {
            lock.acquire();
            createRangeNode(client);
            List<String> children = client.getChildren().forPath("/ranges");

            int newCounterStart = getNewCounterStart(children);
            int rangeInterval = Integer.parseInt(System.getenv("ZK_RANGE_INTERVAL"));
            int newMaxCounter = newCounterStart + rangeInterval;
            String newRangeNodePath = newCounterStart + "-" + newMaxCounter;

            // update new counter range in ZooKeeper
            client
                    .create()
                    .forPath("/ranges/" + newRangeNodePath);

            // update local counter
            counter = newCounterStart;
            maxCounter = newMaxCounter;

            deleteOldRanges(client, children);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            lock.release();
        }
    }

    private static void deleteOldRanges(CuratorFramework client, List<String> children) {
        // old ranges are no longer required
        children.forEach(path -> {
            try {
                client.delete().forPath("/ranges/" + path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static int getNewCounterStart(List<String> children) {
        return children.stream()
                // each znode in ranges will be in form of start-end
                .map(range -> range.split("-"))
                .map(range -> Integer.valueOf(range[1]))
                .max(Comparator.comparingInt(o -> o))
                // initially ranges could be empty
                .orElse(0)
                // increment last range end counter by 1, because it might have been already used by any other process
                + 1;
    }


    private static void createRangeNode(CuratorFramework client) {
        try {
            client.create().forPath("/ranges");
        } catch (KeeperException.NodeExistsException ex) {
            // noop
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
