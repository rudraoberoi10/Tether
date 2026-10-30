package com.tether.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.tether.app.data.local.UserPreferences
import com.tether.app.data.model.Circle
import com.tether.app.data.model.Member
import com.tether.app.data.model.MemberStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

class TetherRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPrefs: UserPreferences
) {
    private val alphabet = "ABCDEFGHJKMNPQRSTUVWXYZ23456789" // 6-character PRD code alphabet
    private val random = SecureRandom()

    val currentUserId: String?
        get() = auth.currentUser?.uid ?: userPrefs.userId

    /**
     * Observes all circles the current user belongs to in real time.
     */
    fun observeUserCircles(): Flow<List<Circle>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("circles")
            .whereArrayContains("memberIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val circles = snapshot?.documents?.map { doc ->
                    Circle.fromMap(doc.id, doc.data ?: emptyMap())
                }.orEmpty()
                trySend(circles)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Observes a single circle's metadata by its ID in real time.
     */
    fun observeCircle(circleId: String): Flow<Circle?> = callbackFlow {
        val listener = firestore.collection("circles")
            .document(circleId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(Circle.fromMap(snapshot.id, snapshot.data ?: emptyMap()))
            }

        awaitClose { listener.remove() }
    }

    /**
     * Observes all members belonging to a specific circle.
     */
    fun observeCircleMembers(circleId: String): Flow<List<Member>> = callbackFlow {
        val listener = firestore.collection("circles")
            .document(circleId)
            .collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val members = snapshot?.documents?.map { doc ->
                    Member.fromMap(doc.id, doc.data ?: emptyMap())
                }.orEmpty()
                trySend(members)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Creates a new circle and adds the creator as the first member with UNKNOWN status.
     */
    suspend fun createCircle(
        name: String,
        fenceLat: Double,
        fenceLng: Double,
        fenceRadiusM: Double,
        fenceLabel: String
    ): Result<Circle> = runCatching {
        val uid = checkNotNull(currentUserId) { "User must be signed in" }
        val inviteCode = generateUniqueInviteCode()

        val circleRef = firestore.collection("circles").document()
        val circleId = circleRef.id

        val circleData = hashMapOf(
            "name" to name.trim(),
            "ownerId" to uid,
            "memberIds" to listOf(uid),
            "inviteCode" to inviteCode,
            "fenceLat" to fenceLat,
            "fenceLng" to fenceLng,
            "fenceRadiusM" to fenceRadiusM,
            "fenceLabel" to fenceLabel,
            "createdAt" to FieldValue.serverTimestamp()
        )

        val memberRef = circleRef.collection("members").document(uid)
        val memberData = hashMapOf(
            "displayName" to userPrefs.displayName.ifBlank { "Owner" },
            "avatarColor" to userPrefs.avatarColor,
            "status" to MemberStatus.UNKNOWN.name,
            "statusUpdatedAt" to FieldValue.serverTimestamp()
        )

        val batch = firestore.batch()
        batch.set(circleRef, circleData)
        batch.set(memberRef, memberData)
        batch.commit().await()

        Circle(
            id = circleId,
            name = name.trim(),
            ownerId = uid,
            memberIds = listOf(uid),
            inviteCode = inviteCode,
            fenceLat = fenceLat,
            fenceLng = fenceLng,
            fenceRadiusM = fenceRadiusM,
            fenceLabel = fenceLabel
        )
    }

    /**
     * Joins an existing circle using a 6-character code.
     */
    suspend fun joinCircle(inviteCode: String): Result<String> = runCatching {
        val uid = checkNotNull(currentUserId) { "User must be signed in" }
        val cleanCode = inviteCode.trim().uppercase()

        val query = firestore.collection("circles")
            .whereEqualTo("inviteCode", cleanCode)
            .limit(1)
            .get()
            .await()

        if (query.isEmpty) {
            throw IllegalArgumentException("That code does not match any circle. Check it and try again.")
        }

        val doc = query.documents.first()
        val circle = Circle.fromMap(doc.id, doc.data ?: emptyMap())

        if (circle.memberIds.contains(uid)) {
            return@runCatching circle.id
        }

        if (circle.memberIds.size >= 10) {
            throw IllegalStateException("This circle already has 10 members.")
        }

        val memberRef = doc.reference.collection("members").document(uid)
        val memberData = hashMapOf(
            "displayName" to userPrefs.displayName.ifBlank { "Member" },
            "avatarColor" to userPrefs.avatarColor,
            "status" to MemberStatus.UNKNOWN.name,
            "statusUpdatedAt" to FieldValue.serverTimestamp()
        )

        val batch = firestore.batch()
        batch.update(doc.reference, "memberIds", FieldValue.arrayUnion(uid))
        batch.set(memberRef, memberData)
        batch.commit().await()

        circle.id
    }

    /**
     * Removes the current user from memberIds and deletes their member status document.
     */
    suspend fun leaveCircle(circleId: String): Result<Unit> = runCatching {
        val uid = checkNotNull(currentUserId) { "User must be signed in" }
        val circleRef = firestore.collection("circles").document(circleId)
        val memberRef = circleRef.collection("members").document(uid)

        val batch = firestore.batch()
        batch.update(circleRef, "memberIds", FieldValue.arrayRemove(uid))
        batch.delete(memberRef)
        batch.commit().await()
    }

    private suspend fun generateUniqueInviteCode(): String {
        repeat(5) {
            val code = (1..6)
                .map { alphabet[random.nextInt(alphabet.length)] }
                .joinToString("")

            val existing = firestore.collection("circles")
                .whereEqualTo("inviteCode", code)
                .limit(1)
                .get()
                .await()

            if (existing.isEmpty) return code
        }
        return (100000..999999).random().toString()
    }
}