package net.cryptic.app;
import android.util.Log;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

/**
 * Storage is used to store necessary client information, including
 * but not limited to: client session-cookie
 */

public class Storage {

    static CookieManager cookieManager = new CookieManager();
    private static int responseCode;


    public static void setCookies(Map<String, List<String>> headerFields){

        List<String> cookiesHeader = headerFields.get("Set-Cookie");

        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
            }
        }
        Log.i("OUTPUT", "Cookie stored: " + cookieManager.getCookieStore().getCookies());
    }
}


