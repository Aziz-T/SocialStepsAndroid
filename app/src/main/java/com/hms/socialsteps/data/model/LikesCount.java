/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 * Generated by the CloudDB ObjectType compiler.  DO NOT EDIT!
 */
package com.hms.socialsteps.data.model;

import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;
import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.Text;

import java.util.Date;

/**
 * Definition of ObjectType LikesCount.
 *
 * @since 2023-02-27
 */
@PrimaryKeys({"postId"})
public final class LikesCount extends CloudDBZoneObject {
    private String postId;

    private Integer likesCount;

    public LikesCount() {
        super(LikesCount.class);
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostId() {
        return postId;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

}
