package com.louiegit.zoolock;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.concurrent.TimeUnit;

/**
 * @author tianxiang.luo
 * @version 2017/12/6 15:02
 */
public class CuratorLock implements ZLock {

    private InterProcessLock processLock;

    private String path;

    private static final Integer RETRY_MAX = Integer.MAX_VALUE;

    private static final CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new RetryPolicy() {
        @Override
        public boolean allowRetry(int retry, long sleepTime, RetrySleeper retrySleeper) {
            if (retry > RETRY_MAX){
                return false;
            }
            try {
                retrySleeper.sleepFor(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    });

    public CuratorLock(ZLockInfo zLockInfo) {
        path = zLockInfo.getPath();
        client.start();
        processLock = new InterProcessMutex(client,path);
    }

    @Override
    public boolean lock() {
        try {
            processLock.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean lock(long timeout) {
        try {
            processLock.acquire(timeout,TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean unlock() {
        try {
            processLock.release();
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean isLock() {
        return processLock.isAcquiredInThisProcess();
    }

}
