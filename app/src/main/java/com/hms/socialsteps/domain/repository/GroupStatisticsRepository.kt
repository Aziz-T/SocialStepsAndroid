package com.hms.socialsteps.domain.repository

import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.GroupInformation
import com.hms.socialsteps.data.model.GroupMembers

class GroupStatisticsRepository {

    fun upsertGroupInformation(groupInformation: GroupInformation, callback: (isSuccessful: Resource<Boolean>) -> Unit) {
        CloudDbWrapper.upsertGroupInformation(groupInformation) { result ->
            callback(result)
        }
    }

    fun upsertGroupMembers(groupMembers: ArrayList<GroupMembers>, callback: (isSuccessful: Resource<Boolean>) -> Unit) {
        CloudDbWrapper.upsertGroupMembers(groupMembers) { result ->
            callback(result)
        }
    }

    fun queryGroup(groupID: String, groupInformation: ((Resource<List<GroupInformation>>) -> Unit)) {
        CloudDbWrapper.queryGroup(groupID) { result ->
            groupInformation(result)
        }
    }

    fun deleteGroupMember(groupMembers: GroupMembers, callback: ((Resource<Any>) -> Unit)){
        CloudDbWrapper.deleteGroupMember(groupMembers){ result ->
            callback(result)
        }
    }

    suspend fun queryUserGroupsById(userId: String) =
        CloudDbWrapper.queryGroupsByUserIdAsFlow(userId)

    suspend fun queryGroupsByUserId(userId: String) =
        CloudDbWrapper.getGroupsByUserIdFlow(userId)

    suspend fun deleteGroupMemberByUserId(userId: String, groupId: String) =
        CloudDbWrapper.deleteGroupMemberByUserIdFlow(userId, groupId)

}