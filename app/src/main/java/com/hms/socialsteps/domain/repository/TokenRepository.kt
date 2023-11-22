package com.hms.socialsteps.domain.repository

import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Tokens

class TokenRepository {


    fun upsertToken(token: Tokens,
                    callback: (isSuccessful: Resource<Any?>) -> Unit){
        CloudDbWrapper.upsertToken(token, callback)

    }
}