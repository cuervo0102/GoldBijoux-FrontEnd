package com.example.jewelryapp.network;

import com.example.jewelryapp.models.Category;
import com.example.jewelryapp.models.Product;
import com.example.jewelryapp.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public interface ApiService {

    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("api/register")
    Call<RegisterResponse> register(@Body RegisterRequest registerRequest);

    @GET("api/products")
    Call<ProductsResponse> getProducts();

    @GET("api/products/{id}")
    Call<ProductResponse> getProduct(@Path("id") int id);

    @POST("api/products")
    Call<ProductResponse> createProduct(@Header("Authorization") String token, @Body Product product);

    @PUT("api/products/{id}")
    Call<ProductResponse> updateProduct(@Header("Authorization") String token, @Path("id") int id, @Body Product product);

    @DELETE("api/products/{id}")
    Call<DeleteResponse> deleteProduct(@Header("Authorization") String token, @Path("id") int id);

    @Multipart
    @POST("api/products")
    Call<ProductResponse> createProductWithImage(
            @Header("Authorization") String token,
            @Part("name") RequestBody name,
            @Part("price") RequestBody price,
            @Part("description") RequestBody description,
            @Part("category_id") RequestBody categoryId,
            @Part("stock") RequestBody stock,
            @Part("requires_ring_size") RequestBody requiresRingSize,
            @Part("active") RequestBody active,
            @Part MultipartBody.Part image
    );

    @GET("api/cart")
    Call<CartResponse> getCart(@Header("Authorization") String token);

    @POST("api/cart")
    Call<CartItemResponse> addToCart(@Header("Authorization") String token, @Body AddToCartRequest request);

    @PUT("api/cart/{id}")
    Call<CartItemResponse> updateCartItem(@Header("Authorization") String token, @Path("id") int cartId, @Body UpdateCartRequest request);

    @DELETE("api/cart/{id}")
    Call<DeleteResponse> removeCartItem(@Header("Authorization") String token, @Path("id") int cartId);

    @DELETE("api/cart-clear")
    Call<DeleteResponse> clearCart(@Header("Authorization") String token);

    @GET("api/categories")
    Call<CategoriesResponse> getCategories();

    @POST("api/categories")
    Call<CategoryResponse> createCategory(@Header("Authorization") String token, @Body Category category);

    @PUT("api/categories/{id}")
    Call<CategoryResponse> updateCategory(@Header("Authorization") String token, @Path("id") int id, @Body Category category);

    @DELETE("api/categories/{id}")
    Call<DeleteResponse> deleteCategory(@Header("Authorization") String token, @Path("id") int id);

    @GET("api/favorites")
    Call<FavoritesResponse> getFavorites(@Header("Authorization") String token);

    @POST("api/favorites")
    Call<FavoriteResponse> addToFavorites(@Header("Authorization") String token, @Body AddToFavoriteRequest request);

    @DELETE("api/favorites/{productId}")
    Call<DeleteResponse> removeFromFavorites(@Header("Authorization") String token, @Path("productId") int productId);

    @GET("api/favorites/check/{productId}")
    Call<CheckFavoriteResponse> checkFavorite(@Header("Authorization") String token, @Path("productId") int productId);

    @DELETE("api/favorites-clear")
    Call<DeleteResponse> clearFavorites(@Header("Authorization") String token);


    class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    class RegisterRequest {
        public String name;
        public String email;
        public String phone;
        public String password;
        public String password_confirmation;

        public RegisterRequest(String name, String email, String phone, String password) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.password = password;
            this.password_confirmation = password;
        }
    }

    class LoginResponse {
        public boolean success;
        public String message;
        public AuthData data;

        public static class AuthData {
            public User user;
            public String token;
        }

        public User getUser() {
            return data != null ? data.user : null;
        }

        public String getToken() {
            return data != null ? data.token : null;
        }
    }

    class RegisterResponse {
        public boolean success;
        public String message;
        public AuthData data;

        public static class AuthData {
            public User user;
            public String token;
        }

        public User getUser() {
            return data != null ? data.user : null;
        }

        public String getToken() {
            return data != null ? data.token : null;
        }
    }

    class CategoriesResponse {
        public boolean success;
        public String message;
        public List<Category> data;
    }

    class CategoryResponse {
        public boolean success;
        public String message;
        public Category data;
    }

    class ProductsResponse {
        public boolean success;
        public String message;
        public ProductsData data;

        public static class ProductsData {
            public List<Product> data;
            public int current_page;
            public int last_page;
            public int total;
            public int per_page;
        }
    }

    class ProductResponse {
        public boolean success;
        public String message;
        public Product data;
    }

    class DeleteResponse {
        public boolean success;
        public String message;
    }


    class AddToCartRequest {
        public int product_id;
        public int quantity;

        public AddToCartRequest(int product_id, int quantity) {
            this.product_id = product_id;
            this.quantity = quantity;
        }
    }

    class UpdateCartRequest {
        public int quantity;

        public UpdateCartRequest(int quantity) {
            this.quantity = quantity;
        }
    }

    class CartResponse {
        public boolean success;
        public String message;
        public CartData data;

        public static class CartData {
            public List<com.example.jewelryapp.models.CartItem> items;
            public double total;
            public int count;
        }
    }

    class CartItemResponse {
        public boolean success;
        public String message;
        public com.example.jewelryapp.models.CartItem data;
    }

    class AddToFavoriteRequest {
        public int product_id;

        public AddToFavoriteRequest(int product_id) {
            this.product_id = product_id;
        }
    }

    class FavoritesResponse {
        public boolean success;
        public String message;
        public List<Product> data;
        public int count;
    }

    class FavoriteResponse {
        public boolean success;
        public String message;
        public FavoriteData data;

        public static class FavoriteData {
            public int id;
            public int user_id;
            public int product_id;
            public Product product;
        }
    }

    class CheckFavoriteResponse {
        public boolean success;
        public boolean is_favorite;
    }
}