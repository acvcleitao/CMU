package pt.ulisboa.tecnico.cmov.librarist;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestHandler {
    private final String url = "http://192.92.147.100:5000";
    private final String POST = "POST";
    private final String GET = "GET";
    private final String EMPTYRESPONSE = "";
    String responseData = null;

    public RequestHandler() {
    }

    public void sendRequest(String type, String method, ArrayList<String> paramname, ArrayList<String> param) {

        /* if url is of our get request, it should not have parameters according to our implementation.
         * But our post request should have 'name' parameter. */
        String fullURL = url + method;
        Request request;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .writeTimeout(50, TimeUnit.SECONDS).build();

        /* If it is a post request, then we have to pass the parameters inside the request body*/
        if (type.equals(POST)) {
            assert param != null;
            FormBody.Builder formBody = new FormBody.Builder();
            System.out.println(paramname);
            System.out.println(param);
            for (int i = 0; i < paramname.size(); i++) {
                formBody.add(paramname.get(i), param.get(i));
            }
            RequestBody requestBody = formBody.build();

            request = new Request.Builder()
                    .url(fullURL)
                    .post(requestBody)
                    .build();
        } else if (type.equals(GET)) {
            /*If it's our get request, it doen't require parameters, hence just sending with the url*/
            request = new Request.Builder()
                    .url(fullURL)
                    .build();
        } else {
            return;
        }
        /* this is how the callback get handled */
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                // Read data on the worker thread
                assert response.body() != null;
                responseData = response.body().string();
                if(responseData.equals("{}")){
                    responseData = EMPTYRESPONSE;
                }
                System.out.println("This was the response" + responseData);
            }
        });
    }

    public String getResponseData() {
        return responseData;
    }
}