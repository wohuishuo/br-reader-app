package com.bookrealm.reader.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.readerDataStore by preferencesDataStore("reader_session")

data class SessionSnapshot(
    val token: String = "",
    val userId: Long = 0,
    val account: String = "",
    val username: String = "",
    val fontScale: Float = 1.0f,
    val lineScale: Float = 1.0f,
    val readerPalette: String = "Paper",
    val lastBookId: Long = 0,
    val lastChapterId: Long = 0,
    val lastParagraphIndex: Int = 0,
)

@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val token = stringPreferencesKey("token")
        val userId = longPreferencesKey("user_id")
        val account = stringPreferencesKey("account")
        val username = stringPreferencesKey("username")
        val fontScale = floatPreferencesKey("font_scale")
        val lineScale = floatPreferencesKey("line_scale")
        val readerPalette = stringPreferencesKey("reader_palette")
        val lastBookId = longPreferencesKey("last_book_id")
        val lastChapterId = longPreferencesKey("last_chapter_id")
        val lastParagraphIndex = intPreferencesKey("last_paragraph_index")
    }

    val session: Flow<SessionSnapshot> = context.readerDataStore.data.map { prefs ->
        SessionSnapshot(
            token = prefs[Keys.token].orEmpty(),
            userId = prefs[Keys.userId] ?: 0,
            account = prefs[Keys.account].orEmpty(),
            username = prefs[Keys.username].orEmpty(),
            fontScale = prefs[Keys.fontScale] ?: 1.0f,
            lineScale = prefs[Keys.lineScale] ?: 1.0f,
            readerPalette = prefs[Keys.readerPalette] ?: "Paper",
            lastBookId = prefs[Keys.lastBookId] ?: 0,
            lastChapterId = prefs[Keys.lastChapterId] ?: 0,
            lastParagraphIndex = prefs[Keys.lastParagraphIndex] ?: 0,
        )
    }

    suspend fun saveLogin(token: String, userId: Long, account: String, username: String) {
        context.readerDataStore.edit {
            it[Keys.token] = token
            it[Keys.userId] = userId
            it[Keys.account] = account
            it[Keys.username] = username
        }
    }

    suspend fun clearLogin() {
        context.readerDataStore.edit {
            it.remove(Keys.token)
            it.remove(Keys.userId)
            it.remove(Keys.account)
            it.remove(Keys.username)
        }
    }

    suspend fun saveFontScale(scale: Float) {
        context.readerDataStore.edit { it[Keys.fontScale] = scale.coerceIn(0.85f, 1.3f) }
    }

    suspend fun saveLineScale(scale: Float) {
        context.readerDataStore.edit { it[Keys.lineScale] = scale.coerceIn(0.9f, 1.5f) }
    }

    suspend fun saveReaderPalette(palette: String) {
        context.readerDataStore.edit { it[Keys.readerPalette] = palette }
    }

    suspend fun saveProgress(bookId: Long, chapterId: Long, paragraphIndex: Int) {
        context.readerDataStore.edit {
            it[Keys.lastBookId] = bookId
            it[Keys.lastChapterId] = chapterId
            it[Keys.lastParagraphIndex] = paragraphIndex.coerceAtLeast(0)
        }
    }
}
