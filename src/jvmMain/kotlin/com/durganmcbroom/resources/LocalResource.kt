package com.durganmcbroom.resources

import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path

public class LocalResource private constructor(
    public val file: File
) : Resource {
    override val location: String = file.toString()

    public constructor(path: Path) : this(path.toFile()) {
        if (!Files.exists(path)) throw ResourceNotFoundException(toString(), FileNotFoundException())
    }

    override suspend fun open(): ResourceStream = flow {
        // Check for this again just to make sure the file hasnt been deleted since this resource was created
        if (!file.exists()) {
            throw ResourceNotFoundException(location, FileNotFoundException())
        }

        emitAll(file.inputStream().asResourceStream())
    }
}

@Throws(ResourceException::class)
public fun Path.toResource(): Resource {
    return LocalResource(this)
}

public class LocalResourceFileException(
    resource: Resource,
    override val cause: Throwable?
) : ResourceException("Failed to load the resource: '${resource.location}'")