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
    val intro: String = "",
    val inShelf: Boolean = false,
)

@Entity(tableName = "chapter_cache")
data class ChapterCacheEntity(
    @PrimaryKey val id: Long,
    val bookId: Long,
    val seq: Int,
    val title: String,
    val paragraphsJson: String,
    val updateTime: Long,
)

@Dao
interface BookCacheDao {
    @Query("SELECT * FROM book_cache WHERE inShelf = 1")
    fun shelfBooks(): Flow<List<BookCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(books: List<BookCacheEntity>)

    @Query("UPDATE book_cache SET inShelf = 1 WHERE id = :bookId")
    suspend fun markInShelf(bookId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM book_cache WHERE id = :bookId AND inShelf = 1)")
    suspend fun isInShelf(bookId: Long): Boolean
}

@Dao
interface ChapterCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(chapter: ChapterCacheEntity)

    @Query("SELECT * FROM chapter_cache WHERE id = :chapterId")
    suspend fun findById(chapterId: Long): ChapterCacheEntity?
}

@Database(entities = [BookCacheEntity::class, ChapterCacheEntity::class], version = 3, exportSchema = false)
abstract class ReaderDatabase : RoomDatabase() {
    abstract fun bookCacheDao(): BookCacheDao
    abstract fun chapterCacheDao(): ChapterCacheDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun database(@ApplicationContext ctx: Context): ReaderDatabase =
        Room.databaseBuilder(ctx, ReaderDatabase::class.java, "reader.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun bookCacheDao(db: ReaderDatabase): BookCacheDao = db.bookCacheDao()

    @Provides
    fun chapterCacheDao(db: ReaderDatabase): ChapterCacheDao = db.chapterCacheDao()
}
