package com.durganmcbroom.resources

public open class ResourceException(
    override val message: String,
    override val cause: Throwable?,
) : Exception() {
    public constructor(message: String) : this(message, null)
}