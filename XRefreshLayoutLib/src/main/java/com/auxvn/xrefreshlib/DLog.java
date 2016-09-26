package com.auxvn.xrefreshlib;

import android.util.Log;

/**
 * Created by zhaoxin on 16/9/24.
 */

public class DLog {
    public static boolean isDebug = true;

    public static void v(String tag, String msg) {
        if(isDebug){
            Log.v(tag, msg);
        }
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param tag
     * @param msg
     * @param throwable
     */
    public static void v(String tag, String msg, Throwable throwable) {
        if(isDebug) {
            Log.v(tag, msg, throwable);
        }
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param tag
     * @param msg
     * @param args
     */
    public static void v(String tag, String msg, Object... args) {
        if(isDebug) {
            if (args.length > 0) {
                msg = String.format(msg, args);
            }
            Log.v(tag, msg);
        }
    }

    /**
     * Send a DEBUG log message
     *
     * @param tag
     * @param msg
     */
    public static void d(String tag, String msg) {
        if(isDebug) {
            Log.d(tag, msg);
        }
    }

    /**
     * Send a DEBUG log message
     *
     * @param tag
     * @param msg
     * @param args
     */
    public static void d(String tag, String msg, Object... args) {
        if(isDebug) {
            if (args.length > 0) {
                msg = String.format(msg, args);
            }
            Log.d(tag, msg);
        }
    }

    /**
     * Send a DEBUG log message
     *
     * @param tag
     * @param msg
     * @param throwable
     */
    public static void d(String tag, String msg, Throwable throwable) {
        if(isDebug) {
            Log.d(tag, msg, throwable);
        }
    }

    /**
     * Send an INFO log message
     *
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg) {
        if(isDebug) {
            Log.i(tag, msg);
        }
    }

    /**
     * Send an INFO log message
     *
     * @param tag
     * @param msg
     * @param args
     */
    public static void i(String tag, String msg, Object... args) {
        if(isDebug) {
            if (args.length > 0) {
                msg = String.format(msg, args);
            }
            Log.i(tag, msg);
        }
    }

    /**
     * Send an INFO log message
     *
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg, Throwable throwable) {
        if(isDebug) {
            Log.i(tag, msg, throwable);
        }
    }

    /**
     * Send a WARNING log message
     *
     * @param tag
     * @param msg
     */
    public static void w(String tag, String msg) {
        if(isDebug) {
            Log.w(tag, msg);
        }
    }

    /**
     * Send a WARNING log message
     *
     * @param tag
     * @param msg
     * @param args
     */
    public static void w(String tag, String msg, Object... args) {
        if(isDebug) {
            if (args.length > 0) {
                msg = String.format(msg, args);
            }
            Log.w(tag, msg);
        }
    }

    /**
     * Send a WARNING log message
     *
     * @param tag
     * @param msg
     * @param throwable
     */
    public static void w(String tag, String msg, Throwable throwable) {
        if(isDebug) {
            Log.w(tag, msg, throwable);
        }
    }

    /**
     * Send an ERROR log message
     *
     * @param tag
     * @param msg
     */
    public static void e(String tag, String msg) {
        if(isDebug) {
            Log.e(tag, msg);
        }
    }

    /**
     * Send an ERROR log message
     *
     * @param tag
     * @param msg
     * @param args
     */
    public static void e(String tag, String msg, Object... args) {
        if(isDebug) {
            if (args.length > 0) {
                msg = String.format(msg, args);
            }
            Log.e(tag, msg);
        }
    }

    /**
     * Send an ERROR log message
     *
     * @param tag
     * @param msg
     * @param throwable
     */
    public static void e(String tag, String msg, Throwable throwable) {
        if(isDebug) {
            Log.e(tag, msg, throwable);
        }
    }

    /**
     * Send a FATAL ERROR log message
     *
     * @param tag
     * @param msg
     */
    public static void f(String tag, String msg) {
        if(isDebug) {
            Log.wtf(tag, msg);
        }
    }

    /**
     * Send a FATAL ERROR log message
     *
     * @param tag
     * @param msg
     * @param args
     */
    public static void f(String tag, String msg, Object... args) {
        if(isDebug) {
            if (args.length > 0) {
                msg = String.format(msg, args);
            }
            Log.wtf(tag, msg);
        }
    }

    /**
     * Send a FATAL ERROR log message
     *
     * @param tag
     * @param msg
     * @param throwable
     */
    public static void f(String tag, String msg, Throwable throwable) {
        if(isDebug) {
            Log.wtf(tag, msg, throwable);
        }
    }
}
