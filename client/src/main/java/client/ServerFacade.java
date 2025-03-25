package client;

import com.google.gson.Gson;
import model.AuthData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ServerFacade {

    private final String urlBase;

    public ServerFacade(String url) {
        this.urlBase = url;
    }

    private <T> T request(String httpMethod, String endpoint, Object request, Class<T> responseClass, AuthData authData) throws ResponseException {
        try {
            URL url = new URI(urlBase + "/" + endpoint).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(httpMethod);
            connection.setDoOutput(true);

            if (authData != null && authData.authToken() != null) {
                connection.setRequestProperty("authorization", authData.authToken());
            }

            if (request != null) {
                connection.addRequestProperty("Content-Type", "application/json");
                try (OutputStream requestBody = connection.getOutputStream()) {
                    requestBody.write((new Gson().toJson(request)).getBytes());
                }
            }

            connection.connect();
            int statusCode = connection.getResponseCode();
            if (statusCode / 100 != 2) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String line;
                StringBuilder errorBody = new StringBuilder();

                while ((line = errorReader.readLine()) != null) {
                    errorBody.append(line);
                }
                errorReader.close();

                throw new ResponseException(statusCode, errorBody.toString());
            }

            BufferedReader successReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder successBody = new StringBuilder();

            while ((line = successReader.readLine()) != null) {
                successBody.append(line);
            }
            successReader.close();

            return new Gson().fromJson(successBody.toString(), responseClass);
        } catch (Exception exception) {
            throw new ResponseException(-1, exception.getMessage());
        }
    }
}
