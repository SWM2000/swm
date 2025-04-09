// IRemoteServiceCallback.aidl
package com.motrex.ktx.mqtt;

// Declare any non-default types here with import statements

interface IRemoteServiceCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    oneway void messageCallback(String key, String msg);
}
