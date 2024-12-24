package com.durganmcbroom.resources

//public class DelegatingResource(
//    override val location: String,
//    private val delegate: () -> ResourceStream
//) : Resource {
//    override fun open(): ResourceStream {
//        return delegate()
//    }
//}
//
//public fun delegateToResource(location: String, delegate: () -> ResourceStream): Resource =
//    DelegatingResource(location) {
//        delegate()
//    }