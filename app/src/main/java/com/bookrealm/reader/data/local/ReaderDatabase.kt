package com.bookrealm.reader.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

/** 书架缓存:书库数据的本地镜像(P5 BC-3:BookCache)。R4 按需扩展字段。 */
@Entity(tableName = "book_cache")
data class BookCacheEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val inShelf: Boolean = false,
)

@Dao
interface BookCacheDao {
    @Query("SELECT * FROM book_cache WHERE inShelf = 1")
    fun shelfBooks(): Flow<List<BookCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(books: List<BookCacheEntity>)
}

@Database(entities = [BookCacheEntity::class], version = 1, exportSchema = false)
abstract class ReaderDatabase : RoomDatabase() {
    abstract fun bookCacheDao(): BookCacheDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun database(@ApplicationContext ctx: Context): ReaderDatabase =
        Room.databaseBuilder(ctx, ReaderDatabase::class.java, "reader.db").build()

    @Provides
    fun bookCacheDao(db: ReaderDatabase): BookCacheDao = db.bookCacheDao()
}
