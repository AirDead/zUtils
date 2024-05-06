package database

enum class Result(val code: Int) {
    UNKNOWN(-1),
    SUCCESS(0),
    MISSING_ID(1),
    ALREADY_EXISTS(2),
    NOT_FOUND(3);

    companion object {
        private val codeToResultMap = entries.associateBy(Result::code)

        fun getByCode(code: Int): Result {
            return codeToResultMap[code] ?: UNKNOWN
        }
    }
}
