// IRemoteService.aidl
package com.motrex.ktx.mqtt;

import com.motrex.ktx.mqtt.IRemoteServiceCallback;
import com.motrex.ktx.mqtt.MovieInfo;
// Declare any non-default types here with import statements

interface IRemoteService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    void registerCallback(IRemoteServiceCallback callback);
    void unregisterCallback(IRemoteServiceCallback callback);
    String onService(String msg);

    boolean callCrew();
    List<MovieInfo> getMovieList();
    void setTurnOn(int flag);
}
