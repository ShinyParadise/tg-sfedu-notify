package utils

import DOWNLOAD_CALENDAR_BASE_URL
import dto.User
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels

fun downloadFile(url: URL, outputFileName: String) {
    runCatching {
        url.openStream().use {
            Channels.newChannel(it).use { rbc ->
                FileOutputStream(outputFileName).use { fos ->
                    fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
                }
            }
        }
    }.onSuccess {
        println("Successful download")
    }.onFailure {
        println("Failed download")
    }
}


fun buildRequestURL(user: User): URL {
    return URL(DOWNLOAD_CALENDAR_BASE_URL + user.group)
}
