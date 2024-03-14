package com.durganmcbroom.resources

public expect class VerifiedResource(
    unverifiedResource: Resource,

    algorithm: ResourceAlgorithm,
    digest: ByteArray,

    retryAttempts: Int = 2,
) : Resource {
    public val unverifiedResource: Resource
}

public class ResourceVerificationException(
    resource: Resource
) : ResourceException(
    "Failed to verify and match digests for resource: '${resource.location}'."
)
