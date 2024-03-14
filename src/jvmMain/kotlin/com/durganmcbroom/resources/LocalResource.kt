package com.durganmcbroom.resources

import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path

public class LocalResource internal constructor(
    public val path: Path
) : Resource {
    override val location: String = path.toString()

    override fun open(): ResourceStream {
        // Check for this again just to make sure the file hasnt been deleted since this resource was created
        if (!Files.exists(path)) {
            throw ResourceNotFoundException(location, FileNotFoundException())
        }

        return path.toFile().inputStream().asResourceStream()
    }
}

@Throws(ResourceException::class)
public fun Path.toResource(): Resource {
    if (!Files.exists(this)) throw ResourceNotFoundException(toString(), FileNotFoundException())

    return LocalResource(this)
}

public class LocalResourceFileException(
    resource: Resource,
    override val cause: Throwable?
) : ResourceException("Failed to load the resource: '${resource.location}'")