package io.wookey.wallet.support.utils;

import java.io.Closeable;
import java.io.IOException;

public class LangHelper {
    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
