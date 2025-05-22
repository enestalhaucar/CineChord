package com.example.cinechord.data.repository

import com.example.cinechord.data.models.UserDto
import com.example.cinechord.domain.models.User
import com.example.cinechord.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Authentication failed"))

            val userSnapshot = database.reference.child("users")
                .child(firebaseUser.uid)
                .get()
                .await()

            val userDto = userSnapshot.getValue(UserDto::class.java)
                ?: UserDto(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    profileImageUrl = firebaseUser.photoUrl?.toString() ?: ""
                )

            Result.success(userDto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("User creation failed"))

            // Update profile with name
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdate).await()

            // Create user in database
            val user = UserDto(
                id = firebaseUser.uid,
                name = name,
                email = email,
                profileImageUrl = "",
                joinedRoomIds = emptyList()
            )

            database.reference.child("users")
                .child(firebaseUser.uid)
                .setValue(user)
                .await()

            Result.success(user.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentFirebaseUser = auth.currentUser
                if (currentFirebaseUser == null) {
                    trySend(null)
                    return
                }

                val userDto = snapshot.getValue(UserDto::class.java)
                if (userDto != null) {
                    trySend(userDto.toDomain())
                } else {
                    // If user not in database yet, create a basic user
                    val basicUser = User(
                        id = currentFirebaseUser.uid,
                        name = currentFirebaseUser.displayName ?: "",
                        email = currentFirebaseUser.email ?: "",
                        profileImageUrl = currentFirebaseUser.photoUrl?.toString() ?: ""
                    )
                    trySend(basicUser)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            database.reference.child("users").child(currentUser.uid)
                .addValueEventListener(listener)
        } else {
            trySend(null)
        }

        // Set up listener for auth state changes
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                trySend(null)
            } else {
                // User is already being listened to with the database listener
            }
        }
        auth.addAuthStateListener(authStateListener)

        awaitClose {
            if (currentUser != null) {
                database.reference.child("users").child(currentUser.uid)
                    .removeEventListener(listener)
            }
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }
} 