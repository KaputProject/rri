package si.um.feri.maprri.raster.utils;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util.println;

import jdk.internal.org.jline.utils.Log;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class HttpUtil {
    private String url = EnvUtil.get("BACKEND_URL");
    private String username = EnvUtil.get("BACKEND_USERNAME");
    private String password = EnvUtil.get("BACKEND_PASSWORD");

    private static final OkHttpClient client = new OkHttpClient();

    public HttpUtil() {}

    /**
     * Vrne svež JWT token za avtentikacijo
     */
    public String authenticate() {
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);

            RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                .url(url + "/users/login")
                .post(body)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject respJson = new JSONObject(responseBody);

                    String token = respJson.getString("token");
                    System.out.println("Obtained JWT token: " + token);

                    return token;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean getUsersFamily() {
        try {
            Request request = new Request.Builder()
                .url(url + "/users/family")
                .addHeader("Authorization", "Bearer " + this.authenticate())
                .get()
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    System.out.println("Users family response: " + responseBody);
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
