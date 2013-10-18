package com.sony.mibox;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {

    public static interface OnRequestListener {
        public abstract Response onRequest(String action);
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
            return mOnRequestListener.onRequest(actions[1].toLowerCase());
        }
        return new Response(Response.Status.BAD_REQUEST, "text/plain", "");
    }

    private OnRequestListener mOnRequestListener;
}
