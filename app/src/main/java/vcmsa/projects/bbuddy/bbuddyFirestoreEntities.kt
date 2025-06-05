package vcmsa.projects.bbuddy

data class FirestoreUser(
    var id: Int = 0,
    var name: String = "",
    var surname: String = "",
    var income: Double = 0.0,
    var fbUid: String = ""
) {
    constructor() : this(0, "", "", 0.0, "")
}

data class FirestoreCategory(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var minAmount: Double = 0.0,
    var maxAmount: Double = 0.0,
    var userId: String = ""
) {
    constructor() : this("", "", "", 0.0, 0.0, "")
}

data class FirestoreExpense(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var amount: Double = 0.0,
    var monthYear: String = "",
    var imageUri: String = "",
    var categoryId: String = "",
    var userId: String = ""
) {
    constructor() : this("", "", "", 0.0, "", "", "", "")
}
