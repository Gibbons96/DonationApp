package com.donationtracker.app.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.profileImageDataStore by preferencesDataStore(name = "profile_image_prefs")

object ProfileImageDataStore {
    private fun getProfileImageUriKey(userId: String) = stringPreferencesKey("profile_image_uri_$userId")

    fun getProfileImageUri(context: Context, userId: String): Flow<String?> =
        context.profileImageDataStore.data.map { prefs ->
            prefs[getProfileImageUriKey(userId)]
        }

    suspend fun setProfileImageUri(context: Context, userId: String, uri: String) {
        context.profileImageDataStore.edit { prefs ->
            prefs[getProfileImageUriKey(userId)] = uri
        }
    }
} 