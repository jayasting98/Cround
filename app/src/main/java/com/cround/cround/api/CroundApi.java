package com.cround.cround.api;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CroundApi {
//    public static final String BASE_URL = "http://10.0.2.2:5001/cround-319708/asia-northeast1/beta_0_1_0/";
    public static final String BASE_URL = "https://asia-northeast1-cround-319708.cloudfunctions.net/beta_0_1_0/";

    @POST("users")
    Call<ResponseBody> registerAccount(@Body CroundApiRequest<SignUpCredentials> signUpCredentialsRequest);

    @POST("users/customToken/username")
    Call<ResponseBody> validateUsernameCredentials(@Body CroundApiRequest<UsernameCredentials> usernameCredentialsRequest);

    @POST("users/customToken/email")
    Call<ResponseBody> validateEmailCredentials(@Body CroundApiRequest<EmailCredentials> emailCredentials);

    @GET("users/{uid}")
    Call<ResponseBody> getUserDetails(@Path("uid") String uid, @HeaderMap Map<String, String> headers);

    @PUT("users/{uid}")
    Call<ResponseBody> updateUserDetails(@Path("uid") String uid, @HeaderMap Map<String, String> headers);

    @DELETE("users/{uid}")
    Call<ResponseBody> deleteUser(@Path("uid") String uid, @HeaderMap Map<String, String> headers);
}
