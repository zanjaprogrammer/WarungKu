package com.zanjaprogrammer.warungku.supabase.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Header;

/**
 * Supabase Auth API interface menggunakan Retrofit
 * Dokumentasi: https://supabase.com/docs/reference/javascript/auth-signup
 */
public interface SupabaseAuthApi {
    
    /**
     * Register new user
     * POST /auth/v1/signup
     */
    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(
        @Header("apikey") String apiKey,
        @Header("Content-Type") String contentType,
        @Body SignUpRequest request
    );
    
    /**
     * Login user
     * POST /auth/v1/token?grant_type=password
     */
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> signIn(
        @Header("apikey") String apiKey,
        @Header("Content-Type") String contentType,
        @Body SignInRequest request
    );
    
    /**
     * Get current user
     * GET /auth/v1/user
     */
    @GET("auth/v1/user")
    Call<UserResponse> getUser(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization
    );
    
    /**
     * Sign out
     * POST /auth/v1/logout
     */
    @POST("auth/v1/logout")
    Call<Void> signOut(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization
    );
    
    // Request/Response models
    class SignUpRequest {
        public String email;
        public String password;
        public SignUpData data;
        
        public SignUpRequest(String email, String password, SignUpData data) {
            this.email = email;
            this.password = password;
            this.data = data;
        }
    }
    
    class SignUpData {
        public String name;
        
        public SignUpData(String name) {
            this.name = name;
        }
    }
    
    class SignInRequest {
        public String email;
        public String password;
        
        public SignInRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
    
    class AuthResponse {
        public String access_token;
        public String token_type;
        public int expires_in;
        public String refresh_token;
        public User user;
        
        public class User {
            public String id;
            public String email;
            public String created_at;
            public String email_confirmed_at;
            public UserMetadata user_metadata;
            
            public class UserMetadata {
                public String name;
            }
        }
    }
    
    class UserResponse {
        public String id;
        public String email;
        public String created_at;
        public String email_confirmed_at;
        public UserMetadata user_metadata;
        
        public class UserMetadata {
            public String name;
        }
    }
}

