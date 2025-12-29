package com.zanjaprogrammer.warungku.supabase.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;
import java.util.Map;

/**
 * Supabase PostgREST API interface untuk database operations
 * Dokumentasi: https://supabase.com/docs/reference/javascript/select
 */
public interface SupabasePostgrestApi {
    
    // Users table operations
    @GET("rest/v1/users")
    Call<List<Map<String, Object>>> getUsers(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Query("id") String idEq,
        @Query("warung_id") String warungIdEq,
        @Query("select") String select
    );
    
    @POST("rest/v1/users")
    Call<List<Map<String, Object>>> insertUser(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Header("Prefer") String prefer,
        @Body Map<String, Object> user
    );
    
    @PUT("rest/v1/users")
    Call<List<Map<String, Object>>> updateUser(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Header("Prefer") String prefer,
        @Query("id") String eq,
        @Body Map<String, Object> user
    );
    
    // Warungs table operations
    @GET("rest/v1/warungs")
    Call<List<Map<String, Object>>> getWarungs(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Query("id") String eq,
        @Query("select") String select
    );
    
    @POST("rest/v1/warungs")
    Call<List<Map<String, Object>>> insertWarung(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Header("Prefer") String prefer,
        @Body Map<String, Object> warung
    );
    
    // Products table operations
    @GET("rest/v1/products")
    Call<List<Map<String, Object>>> getProducts(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Query("warung_id") String eq,
        @Query("select") String select
    );
    
    @POST("rest/v1/products")
    Call<List<Map<String, Object>>> insertProduct(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Header("Prefer") String prefer,
        @Body Map<String, Object> product
    );
    
    @PUT("rest/v1/products")
    Call<List<Map<String, Object>>> updateProduct(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Header("Prefer") String prefer,
        @Query("id") String eq,
        @Body Map<String, Object> product
    );
    
    // Cash flows table operations
    @GET("rest/v1/cash_flows")
    Call<List<Map<String, Object>>> getCashFlows(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Query("warung_id") String eq,
        @Query("select") String select
    );
    
    @POST("rest/v1/cash_flows")
    Call<List<Map<String, Object>>> insertCashFlow(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Header("Prefer") String prefer,
        @Body Map<String, Object> cashFlow
    );
    
    @PUT("rest/v1/cash_flows")
    Call<List<Map<String, Object>>> updateCashFlow(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Header("Prefer") String prefer,
        @Query("id") String eq,
        @Body Map<String, Object> cashFlow
    );
    
    // Invites table operations
    @GET("rest/v1/invites")
    Call<List<Map<String, Object>>> getInvites(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Query("email") String eq,
        @Query("status") String statusEq,
        @Query("select") String select
    );
    
    @POST("rest/v1/invites")
    Call<List<Map<String, Object>>> insertInvite(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Header("Prefer") String prefer,
        @Body Map<String, Object> invite
    );
    
    @PUT("rest/v1/invites")
    Call<List<Map<String, Object>>> updateInvite(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Header("Prefer") String prefer,
        @Query("id") String eq,
        @Body Map<String, Object> invite
    );
}

