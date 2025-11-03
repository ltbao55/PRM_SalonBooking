package com.example.prm_be.data;

import android.util.Log;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Class quản lý kết nối MongoDB Cluster
 * Singleton pattern để đảm bảo chỉ có một instance kết nối
 */
public class MongoDBConnection {
    private static final String TAG = "MongoDBConnection";
    private static MongoDBConnection instance;
    
    private MongoClient mongoClient;
    private MongoDatabase database;
    private ExecutorService executorService;
    
    // Database configuration - Cần cập nhật với thông tin MongoDB cluster thực tế
    private static final String CONNECTION_STRING = "mongodb+srv://lythucbao2004_db_user:z35xz4LXb0MYFm0N@cluster0.kvuehpp.mongodb.net/?appName=Cluster0";
    private static final String DATABASE_NAME = "prm_salon_booking";
    
    /**
     * Private constructor để đảm bảo Singleton pattern
     */
    private MongoDBConnection() {
        executorService = Executors.newFixedThreadPool(4);
    }
    
    /**
     * Lấy instance duy nhất của MongoDBConnection
     */
    public static synchronized MongoDBConnection getInstance() {
        if (instance == null) {
            instance = new MongoDBConnection();
        }
        return instance;
    }
    
    /**
     * Khởi tạo kết nối đến MongoDB Cluster
     * @param connectionString Connection string của MongoDB Atlas
     * @param databaseName Tên database
     */
    public void connect(String connectionString, String databaseName) {
        try {
            if (mongoClient != null) {
                Log.w(TAG, "MongoDB connection already exists. Closing previous connection.");
                close();
            }
            
            ConnectionString connString = new ConnectionString(connectionString);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connString)
                    .build();
            
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(databaseName);
            
            Log.i(TAG, "Successfully connected to MongoDB: " + databaseName);
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to MongoDB", e);
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }
    
    /**
     * Khởi tạo kết nối với connection string và database name mặc định
     */
    public void connect() {
        connect(CONNECTION_STRING, DATABASE_NAME);
    }
    
    /**
     * Lấy MongoDB database instance
     * @return MongoDatabase instance
     */
    public MongoDatabase getDatabase() {
        if (database == null) {
            throw new IllegalStateException("MongoDB not connected. Call connect() first.");
        }
        return database;
    }
    
    /**
     * Lấy MongoClient instance
     * @return MongoClient instance
     */
    public MongoClient getMongoClient() {
        if (mongoClient == null) {
            throw new IllegalStateException("MongoDB not connected. Call connect() first.");
        }
        return mongoClient;
    }
    
    /**
     * Kiểm tra xem đã kết nối chưa
     * @return true nếu đã kết nối
     */
    public boolean isConnected() {
        return mongoClient != null && database != null;
    }
    
    /**
     * Đóng kết nối MongoDB
     */
    public void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                Log.i(TAG, "MongoDB connection closed");
            } catch (Exception e) {
                Log.e(TAG, "Error closing MongoDB connection", e);
            } finally {
                mongoClient = null;
                database = null;
            }
        }
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    /**
     * Lấy ExecutorService để thực hiện các thao tác database async
     * @return ExecutorService instance
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }
    
    /**
     * Set connection string mặc định
     * @param connectionString Connection string mới
     */
    public static void setDefaultConnectionString(String connectionString) {
        // Có thể lưu vào SharedPreferences hoặc config file
        Log.i(TAG, "Default connection string updated");
    }
    
    /**
     * Set database name mặc định
     * @param databaseName Tên database mới
     */
    public static void setDefaultDatabaseName(String databaseName) {
        // Có thể lưu vào SharedPreferences hoặc config file
        Log.i(TAG, "Default database name updated");
    }
}


