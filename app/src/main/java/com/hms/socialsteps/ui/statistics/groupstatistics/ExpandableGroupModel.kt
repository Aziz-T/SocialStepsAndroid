package com.hms.socialsteps.ui.statistics.groupstatistics

import com.hms.socialsteps.data.model.Group
import com.hms.socialsteps.data.model.Users

class ExpandableGroupModel {
    companion object{
        const val PARENT = 1
        const val CHILD = 2
        /* const val BUTTONLAYOUT = 3
         const val REMOVELIST = 4
         const val EMPTYLAYOUT = 5*/
    }
    lateinit var groupParent: Group
    var type : Int
    lateinit var groupChild : Users
    var isExpanded : Boolean
    // var isLocked : Boolean
    var childRank = 0

    constructor( type : Int, groupParent: Group, isExpanded : Boolean = true){
        this.type = type
        this.groupParent = groupParent
        this.isExpanded = isExpanded

    }


    constructor(type : Int, groupChild : Users, isExpanded : Boolean = true, childRank: Int, groupParent: Group){
        this.type = type
        this.groupChild = groupChild
        this.isExpanded = isExpanded
        this.childRank = childRank
        this.groupParent = groupParent
    }
}