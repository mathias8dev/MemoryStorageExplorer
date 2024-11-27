package com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations

enum class FileOperation {
    COPY_FILE,
    COPY_DIRECTORY,
    MOVE_FILE,
    MOVE_DIRECTORY,
    DELETE_FILE,
    DELETE_DIRECTORY,
    RENAME_FILE,
    RENAME_DIRECTORY,
    CREATE_FILE,
    CREATE_DIRECTORY,
    RESOLVE_SKIPPED_OPERATION,
    UNKNOWN, ;

    fun isCopy() = this == COPY_FILE || this == COPY_DIRECTORY
    fun isMove() = this == MOVE_FILE || this == MOVE_DIRECTORY
}