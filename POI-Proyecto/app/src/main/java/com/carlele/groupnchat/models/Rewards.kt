package com.carlele.groupnchat.models

class Rewards {
    var iconReward: Int? = 0
    var nameReward: String? = null
    var descriptionR: String? = null

    constructor(iconReward: Int?, nameReward: String?, descriptionR: String?) {
        this.iconReward = iconReward
        this.nameReward = nameReward
        this.descriptionR = descriptionR
    }
}