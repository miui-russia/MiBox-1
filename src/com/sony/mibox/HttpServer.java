package com.sony.mibox;

import fi.iki.elonen.NanoHTTPD;

import java.util.Map;

public class HttpServer extends NanoHTTPD {
    public static final String TAG = "HttpServer";

    public static interface OnRequestListener {
        public abstract Response onRequest(String action, Map<String, String> params);
    }

    public HttpServer(int port) {
        super(port);
    }

    public void setOnRequestListener(OnRequestListener listener) {
        mOnRequestListener = listener;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String url = session.getUri();
        String[] actions = url.split("/");

        if (actions.length > 1 && mOnRequestListener != null) {
            return mOnRequestListener.onRequest(actions[1].toLowerCase(), session.getParms());
        }
        return new Response(Response.Status.BAD_REQUEST, "text/plain", "");
    }

    private OnRequestListener mOnRequestListener;
}
