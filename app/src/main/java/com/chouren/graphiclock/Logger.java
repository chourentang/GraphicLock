package com.chouren.graphiclock;

import android.util.Log;

/**
 * 日志打印 用于日志的打印，便于日志的统�?���?
 * @author TangLong
 */
public class Logger {
	public static enum DebugLevel {
		ERROR {
			public int getValue() {
				return 0;
			}
		},
		WARN {
			public int getValue() {
				return 1;
			}
		},
		INFO {
			public int getValue() {
				return 2;
			}
		},
		DEBUG {
			public int getValue() {
				return 3;
			}
		};

		public abstract int getValue();
	};

	// 日志的等级，�?��改变日志等级时，修改此行即可
	private static DebugLevel debugLevel = DebugLevel.DEBUG;
	// 默认的日志标�?
	private static String TAG = "tag_default";

	public static void error(String tag, String msg) {
		if (DebugLevel.ERROR.getValue() <= debugLevel.getValue()) {
			if (msg == null) {
				return;
			}
			Log.e(tag == null ? TAG : tag, msg);
		}
	}

	public static void warn(String tag, String msg) {
		if (DebugLevel.WARN.getValue() <= debugLevel.getValue()) {
			if (msg == null) {
				return;
			}
			Log.w(tag == null ? TAG : tag, msg);
		}
	}

	public static void info(String tag, String msg) {
		if (DebugLevel.INFO.getValue() <= debugLevel.getValue()) {
			if (msg == null) {
				return;
			}
			Log.i(tag == null ? TAG : tag, msg);
		}
	}

	public static void debug(String tag, String msg) {
		if (DebugLevel.DEBUG.getValue() <= debugLevel.getValue()) {
			if (msg == null) {
				return;
			}
			Log.d(tag == null ? TAG : tag, msg);
		}
	}

}
