package com.example.a90phase.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.a90phase.data.local.room.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(entity: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE rowId = 0")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE rowId = 0")
    suspend fun getUserProfile(): UserProfileEntity?

    @Query("UPDATE user_profile SET discovery_phase_json = :discoveryPhaseJson WHERE rowId = 0")
    suspend fun updateDiscoveryPhase(discoveryPhaseJson: String?)
}
