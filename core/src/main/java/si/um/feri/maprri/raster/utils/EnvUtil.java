package si.um.feri.maprri.raster.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvUtil {
    private static Dotenv dotenv;

    public static String get(String key) {
        String val = System.getenv(key);
        if (val == null) {
            if (dotenv == null) dotenv = Dotenv.load();
            val = dotenv.get(key);
        }
        return val;
    }
}
