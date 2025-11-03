package com.example.prm_be.data;

import android.util.Log;

import com.example.prm_be.data.models.Booking;
import com.example.prm_be.data.models.Salon;
import com.example.prm_be.data.models.Service;
import com.example.prm_be.data.models.Stylist;
import com.example.prm_be.data.models.User;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Executor;

/**
 * Repository class quản lý tất cả các thao tác với MongoDB
 * Tương tự như FirebaseRepo nhưng sử dụng MongoDB
 * Tất cả các operations được thực hiện async để tránh block main thread
 */
public class MongoDBRepo {
    private static final String TAG = "MongoDBRepo";
    private static MongoDBRepo instance;
    
    private MongoDBConnection mongoConnection;
    private MongoDatabase database;
    private ExecutorService executorService;
    
    // Collection names
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_SALONS = "salons";
    private static final String COLLECTION_BOOKINGS = "bookings";
    private static final String COLLECTION_SERVICES = "services";
    private static final String COLLECTION_STYLISTS = "stylists";
    
    /**
     * Private constructor để đảm bảo Singleton pattern
     */
    private MongoDBRepo() {
        mongoConnection = MongoDBConnection.getInstance();
        executorService = mongoConnection.getExecutorService();
    }
    
    /**
     * Lấy instance duy nhất của MongoDBRepo
     */
    public static synchronized MongoDBRepo getInstance() {
        if (instance == null) {
            instance = new MongoDBRepo();
        }
        return instance;
    }
    
    /**
     * Khởi tạo kết nối MongoDB
     * @param connectionString Connection string của MongoDB Atlas
     * @param databaseName Tên database
     */
    public void initialize(String connectionString, String databaseName) {
        mongoConnection.connect(connectionString, databaseName);
        database = mongoConnection.getDatabase();
    }
    
    /**
     * Kiểm tra xem đã khởi tạo chưa
     */
    private void ensureInitialized() {
        if (database == null) {
            throw new IllegalStateException("MongoDBRepo not initialized. Call initialize() first.");
        }
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Convert User object to Document
     */
    private Document userToDocument(User user) {
        Document doc = new Document();
        if (user.getUid() != null) doc.append("uid", user.getUid());
        if (user.getName() != null) doc.append("name", user.getName());
        if (user.getEmail() != null) doc.append("email", user.getEmail());
        if (user.getAvatarUrl() != null) doc.append("avatarUrl", user.getAvatarUrl());
        return doc;
    }
    
    /**
     * Convert Document to User object
     */
    private User documentToUser(Document doc) {
        User user = new User();
        user.setUid(doc.getString("uid"));
        user.setName(doc.getString("name"));
        user.setEmail(doc.getString("email"));
        user.setAvatarUrl(doc.getString("avatarUrl"));
        return user;
    }
    
    /**
     * Convert Salon object to Document
     */
    private Document salonToDocument(Salon salon) {
        Document doc = new Document();
        if (salon.getName() != null) doc.append("name", salon.getName());
        if (salon.getAddress() != null) doc.append("address", salon.getAddress());
        if (salon.getImageUrl() != null) doc.append("imageUrl", salon.getImageUrl());
        return doc;
    }
    
    /**
     * Convert Document to Salon object
     */
    private Salon documentToSalon(Document doc) {
        Salon salon = new Salon();
        salon.setId(doc.getObjectId("_id").toString());
        salon.setName(doc.getString("name"));
        salon.setAddress(doc.getString("address"));
        salon.setImageUrl(doc.getString("imageUrl"));
        return salon;
    }
    
    /**
     * Convert Booking object to Document
     */
    private Document bookingToDocument(Booking booking) {
        Document doc = new Document();
        doc.append("userId", booking.getUserId());
        doc.append("salonId", booking.getSalonId());
        doc.append("serviceId", booking.getServiceId());
        if (booking.getStylistId() != null) doc.append("stylistId", booking.getStylistId());
        doc.append("timestamp", booking.getTimestamp());
        doc.append("status", booking.getStatus());
        doc.append("createdAt", booking.getCreatedAt());
        return doc;
    }
    
    /**
     * Convert Document to Booking object
     */
    private Booking documentToBooking(Document doc) {
        Booking booking = new Booking();
        booking.setId(doc.getObjectId("_id").toString());
        booking.setUserId(doc.getString("userId"));
        booking.setSalonId(doc.getString("salonId"));
        booking.setServiceId(doc.getString("serviceId"));
        booking.setStylistId(doc.getString("stylistId"));
        booking.setTimestamp(doc.getLong("timestamp"));
        booking.setStatus(doc.getString("status"));
        booking.setCreatedAt(doc.getLong("createdAt"));
        return booking;
    }
    
    /**
     * Convert Service object to Document
     */
    private Document serviceToDocument(Service service, String salonId) {
        Document doc = new Document();
        if (service.getName() != null) doc.append("name", service.getName());
        doc.append("price", service.getPrice());
        if (salonId != null) doc.append("salonId", salonId);
        return doc;
    }
    
    /**
     * Convert Document to Service object
     */
    private Service documentToService(Document doc) {
        Service service = new Service();
        service.setId(doc.getObjectId("_id").toString());
        service.setName(doc.getString("name"));
        service.setPrice(doc.getLong("price"));
        return service;
    }
    
    /**
     * Convert Stylist object to Document
     */
    private Document stylistToDocument(Stylist stylist) {
        Document doc = new Document();
        if (stylist.getName() != null) doc.append("name", stylist.getName());
        if (stylist.getSalonId() != null) doc.append("salonId", stylist.getSalonId());
        if (stylist.getImageUrl() != null) doc.append("imageUrl", stylist.getImageUrl());
        if (stylist.getSpecialization() != null) doc.append("specialization", stylist.getSpecialization());
        return doc;
    }
    
    /**
     * Convert Document to Stylist object
     */
    private Stylist documentToStylist(Document doc) {
        Stylist stylist = new Stylist();
        stylist.setId(doc.getObjectId("_id").toString());
        stylist.setName(doc.getString("name"));
        stylist.setSalonId(doc.getString("salonId"));
        stylist.setImageUrl(doc.getString("imageUrl"));
        stylist.setSpecialization(doc.getString("specialization"));
        return stylist;
    }
    
    // ========== USER METHODS ==========
    
    /**
     * Tạo User mới
     */
    public void createUser(User user, MongoDBCallback<String> callback) {
        ensureInitialized();
        executorService.execute(() -> {
            try {
                MongoCollection<Document> collection = database.getCollection(COLLECTION_USERS);
                Document doc = userToDocument(user);
                collection.insertOne(doc);
                String id = doc.getObjectId("_id").toString();
                Log.i(TAG, "User created with ID: " + id);
                callback.onSuccess(id);
            } catch (Exception e) {
                Log.e(TAG, "Error creating user", e);
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Lấy User theo UID
     */
    public void getUser(String uid, MongoDBCallback<User> callback) {
        ensureInitialized();
        executorService.execute(() -> {
            try {
                MongoCollection<Document> collection = database.getCollection(COLLECTION_USERS);
                Document doc = collection.find(Filters.eq("uid", uid)).first();
                if (doc != null) {
                    User user = documentToUser(doc);
                    callback.onSuccess(user);
                } else {
                    callback.onFailure(new Exception("User not found"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting user", e);
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Cập nhật User
     */
    public void updateUser(User user, MongoDBCallback<Void> callback) {
        ensureInitialized();
        executorService.execute(() -> {
            try {
                MongoCollection<Document> collection = database.getCollection(COLLECTION_USERS);
                List<Bson> updateList = new ArrayList<>();
                if (user.getName() != null) updateList.add(Updates.set("name", user.getName()));
                if (user.getEmail() != null) updateList.add(Updates.set("email", user.getEmail()));
                if (user.getAvatarUrl() != null) updateList.add(Updates.set("avatarUrl", user.getAvatarUrl()));
                
                if (!updateList.isEmpty()) {
                    collection.updateOne(Filters.eq("uid", user.getUid()), Updates.combine(updateList));
                }
                callback.onSuccess(null);
            } catch (Exception e) {
                Log.e(TAG, "Error updating user", e);
                callback.onFailure(e);
            }
        });
    }
    
    // ========== SALON METHODS ==========
    
    /**
     * Lấy tất cả Salon
     */
    public void getAllSalons(MongoDBCallback<List<Salon>> callback) {
        ensureInitialized();
        executorService.execute(() -> {
            try {
                MongoCollection<Document> collection = database.getCollection(COLLECTION_SALONS);
                List<Salon> salons = new ArrayList<>();
                for (Document doc : collection.find()) {
                    salons.add(documentToSalon(doc));
                }
                callback.onSuccess(salons);
            } catch (Exception e) {
                Log.e(TAG, "Error getting all salons", e);
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Lấy Salon theo ID
     */
    public void getSalonById(String salonId, MongoDBCallback<Salon> callback) {
        ensureInitialized();
        executorService.execute(() -> {
            try {
                MongoCollection<Document> collection = database.getCollection(COLLECTION_SALONS);
                Document doc = collection.find(Filters.eq("_id", new ObjectId(salonId))).first();
                if (doc != null) {
                    callback.onSuccess(documentToSalon(doc));
                } else {
                    callback.onFailure(new Exception("Salon not found"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting salon", e);
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Tạo Salon mới
     */
    public void createSalon(Salon salon, MongoDBCallback<String> callback) {
        ensureInitialized();
        executorService.execute(() -> {
            try {
                MongoCollection<Document> collection = database.getCollection(COLLECTION_SALONS);
                Document doc = salonToDocument(salon);
                collection.insertOne(doc);
                String id = doc.getObjectId("_id").toString();
                callback.onSuccess(id);
            } catch (Exception e) {
                Log.e(TAG, "Error creating salon", e);
                callback.onFailure(e);
            }
        });
    }
    
    // ========== SERVICE METHODS ==========
    
    /**
     * Lấy tất cả Service của một Salon
     */
    public void getServicesOfSalon(String salonId, MongoDBCallback<List<Service>> callback) {
        ensureInitialized();
        executorService.execute(() -> {
            try {
                MongoCollection<Document> collection = database.getCollection(COLLECTION_SERVICES);
                List<Service> services = new ArrayList<>();
                for (Document doc : collection.find(Filters.eq("salonId", salonId))) {
                    services.add(documentToService(doc));
                }
                callback.onSuccess(services);
            } catch (Exception e) {
                Log.e(TAG, "Error getting services", e);
                callback.onFailure(e);
            }
        });
    }
    
    // ========== STYLIST METHODS ==========
    
    /**
     * Lấy tất cả Stylist của một Salon
     */
    public void getStylistsOfSalon(String salonId, MongoDBCallback<List<Stylist>> callback) {
        ensureInitialized();
        executorService.execute(() -> {
            try {
                MongoCollection<Document> collection = database.getCollection(COLLECTION_STYLISTS);
                List<Stylist> stylists = new ArrayList<>();
                for (Document doc : collection.find(Filters.eq("salonId", salonId))) {
                    stylists.add(documentToStylist(doc));
                }
                callback.onSuccess(stylists);
            } catch (Exception e) {
                Log.e(TAG, "Error getting stylists", e);
                callback.onFailure(e);
            }
        });
    }
    
    // ========== BOOKING METHODS ==========
    
    /**
     * Tạo Booking mới
     */
    public void createBooking(Booking booking, MongoDBCallback<String> callback) {
        ensureInitialized();
        executorService.execute(() -> {
            try {
                MongoCollection<Document> collection = database.getCollection(COLLECTION_BOOKINGS);
                Document doc = bookingToDocument(booking);
                collection.insertOne(doc);
                String id = doc.getObjectId("_id").toString();
                callback.onSuccess(id);
            } catch (Exception e) {
                Log.e(TAG, "Error creating booking", e);
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Lấy tất cả Booking của một User
     */
    public void getUserBookings(String userId, MongoDBCallback<List<Booking>> callback) {
        ensureInitialized();
        executorService.execute(() -> {
            try {
                MongoCollection<Document> collection = database.getCollection(COLLECTION_BOOKINGS);
                List<Booking> bookings = new ArrayList<>();
                for (Document doc : collection.find(Filters.eq("userId", userId))
                        .sort(Sorts.descending("timestamp"))) {
                    bookings.add(documentToBooking(doc));
                }
                callback.onSuccess(bookings);
            } catch (Exception e) {
                Log.e(TAG, "Error getting user bookings", e);
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Lấy Booking theo Stylist và ngày (để check time slots)
     */
    public void getBookingsByStylistAndDate(
            String stylistId,
            String salonId,
            long startTimestamp,
            long endTimestamp,
            MongoDBCallback<List<Booking>> callback) {
        ensureInitialized();
        executorService.execute(() -> {
            try {
                MongoCollection<Document> collection = database.getCollection(COLLECTION_BOOKINGS);
                List<Booking> bookings = new ArrayList<>();
                
                Bson filter = Filters.and(
                    Filters.eq("salonId", salonId),
                    Filters.gte("timestamp", startTimestamp),
                    Filters.lte("timestamp", endTimestamp)
                );
                
                if (stylistId != null && !stylistId.isEmpty()) {
                    filter = Filters.and(
                        filter,
                        Filters.eq("stylistId", stylistId)
                    );
                }
                
                for (Document doc : collection.find(filter)) {
                    Booking booking = documentToBooking(doc);
                    if ("confirmed".equals(booking.getStatus()) || "pending".equals(booking.getStatus())) {
                        bookings.add(booking);
                    }
                }
                callback.onSuccess(bookings);
            } catch (Exception e) {
                Log.e(TAG, "Error getting bookings by stylist and date", e);
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Cập nhật status của Booking
     */
    public void updateBookingStatus(String bookingId, String status, MongoDBCallback<Void> callback) {
        ensureInitialized();
        executorService.execute(() -> {
            try {
                MongoCollection<Document> collection = database.getCollection(COLLECTION_BOOKINGS);
                collection.updateOne(
                    Filters.eq("_id", new ObjectId(bookingId)),
                    Updates.set("status", status)
                );
                callback.onSuccess(null);
            } catch (Exception e) {
                Log.e(TAG, "Error updating booking status", e);
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Đóng kết nối MongoDB
     */
    public void close() {
        if (mongoConnection != null) {
            mongoConnection.close();
        }
    }
    
    /**
     * Interface callback cho các thao tác MongoDB
     */
    public interface MongoDBCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}

