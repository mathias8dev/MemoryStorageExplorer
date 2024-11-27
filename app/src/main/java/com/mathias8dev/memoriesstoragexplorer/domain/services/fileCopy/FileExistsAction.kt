package com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy

enum class FileExistsAction {
    SKIP,
    OVERWRITE,
    RENAME,
    REPORT_IF_EXISTS
}