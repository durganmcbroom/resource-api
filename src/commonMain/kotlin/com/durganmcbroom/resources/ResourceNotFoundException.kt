package com.durganmcbroom.resources

public class ResourceNotFoundException(
    location: String,
    override val cause: Throwable? = null
) : ResourceException("Couldnt find the resource: '${location}' because of previous errors.", cause)