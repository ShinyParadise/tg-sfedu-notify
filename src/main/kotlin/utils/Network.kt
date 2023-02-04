package utils

import DOWNLOAD_CALENDAR_BASE_URL
import dto.User
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels

fun downloadFile(url: URL, outputFileName: String) {
    url.openStream().use {
        Channels.newChannel(it).use { rbc ->
            FileOutputStream(outputFileName).use { fos ->
                fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
            }
        }
    }
}


fun buildRequestURL(user: User): URL {
    return URL(DOWNLOAD_CALENDAR_BASE_URL + user.group)
}
