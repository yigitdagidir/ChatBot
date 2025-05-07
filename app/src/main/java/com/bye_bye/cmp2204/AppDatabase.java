package com.bye_bye.cmp2204;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Main database for the application
 */
@Database(
    entities = {ChatMessage.class, ChatSession.class}, 
    version = 2, 
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    // DAOs
    public abstract ChatMessageDao messageDao();
    public abstract ChatSessionDao sessionDao();

    // Singleton instance
    private static volatile AppDatabase INSTANCE;

    /**
     * Get singleton database instance
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class, 
                        "chat_database"
                    )
                    // For development only - allows database operations on main thread (avoid in prod)
                    .allowMainThreadQueries()
                    // Wipes and rebuilds instead of migrating if no Migration object exists
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
} 