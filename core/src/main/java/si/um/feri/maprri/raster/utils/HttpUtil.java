package si.um.feri.maprri.raster.utils;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class HttpUtil {
    private static String url = EnvUtil.get("BACKEND_URL");
    private static String username = EnvUtil.get("BACKEND_USERNAME");
    private static String password = EnvUtil.get("BACKEND_PASSWORD");
    private static String userId;
    private static final OkHttpClient client = new OkHttpClient();
    private static String familyId;
    public HttpUtil() {
    }

    /**
     * Vrne svež JWT token za avtentikacijo
     */
    public static String authenticate() {
        try {
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("password", password);

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder().url(url + "/users/login").post(body).build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject respJson = new JSONObject(responseBody);
                    userId = respJson.has("user") && respJson.getJSONObject("user").has("_id") ? respJson.getJSONObject("user").getString("_id") : respJson.optString("id", "");
                    String token = respJson.optString("token", "");
                    System.out.println("Obtained JWT token: " + token);

                    return token;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getUsersFamily() {
        try {
            Request request = new Request.Builder().url(url + "/users/family").addHeader("Authorization", "Bearer " + authenticate()).get().build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject respJson = new JSONObject(responseBody);
                return respJson.getString("family");
            } else {
                return "";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getBaseLocationData() {
        familyId = getUsersFamily();
        //familyId = null ; // TO TEST USER STATISTICS INSTEAD OF FAMILY STATISTICS
        if (familyId != null) {
            try {
                Request request = new Request.Builder().url(url + "/family/" + familyId + "/statistics").addHeader("Authorization", "Bearer " + new HttpUtil().authenticate()).get().build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                } else {
                    System.out.println("Failed to fetch family statistics, falling back to user statistics.");
                    throw new IOException("Unexpected code " + response);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Family ID is null, cannot fetch base family location data.");
            try {
                Request request = new Request.Builder().url(url + "/users/" + userId + "/statistics").addHeader("Authorization", "Bearer " + new HttpUtil().authenticate()).get().build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                } else {
                    System.out.println("Failed to fetch family statistics, falling back to user statistics.");
                    throw new IOException("Unexpected code " + response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
    public static String getFamilyId() {
        return familyId;
    }

}
