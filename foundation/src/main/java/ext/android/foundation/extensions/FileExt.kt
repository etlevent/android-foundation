package ext.android.foundation.extensions

import java.io.File
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


fun File.asZipFile() = ZipFile(this)

fun ZipFile.unzip(dstDir: File) {
    val enumeration: Enumeration<out ZipEntry> = entries()
    enumeration.iterator().forEach { zipEntry ->
        if (zipEntry.isDirectory) {
            val dir = File(dstDir, zipEntry.name)
            if (!dir.exists())
                dir.mkdirs()
        } else {
            val file = File(dstDir, zipEntry.name)
            file.parentFile?.let { parent ->
                if (!parent.exists()) parent.mkdirs()
            }
            getInputStream(zipEntry).use { `is` ->
                file.outputStream().use { os ->
                    os.write(`is`.readBytes())
                }
            }
        }
    }
}

fun File.zip(dst: File) {
    ZipOutputStream(dst.outputStream())
        .use { zipOut ->
            zipFile(zipOut, this, "")
        }
}

fun Iterable<File>.zip(dst: File) {
    ZipOutputStream(dst.outputStream())
        .use { zipOut ->
            forEach { child ->
                zipFile(zipOut, child, "")
            }
        }
}

private fun zipFile(zipOut: ZipOutputStream, src: File, path: String) {
    val actualPath =
        if (path.isNotEmpty() && !path.endsWith(File.separator)) "$path${File.separator}" else path
    if (src.isDirectory) {
        src.listFiles()?.forEach { child ->
            zipFile(zipOut, child, "$actualPath${src.name}")
        }
    } else {
        src.inputStream().use { `is` ->
            zipOut.putNextEntry(ZipEntry("$actualPath${src.name}"))
            zipOut.write(`is`.readBytes())
            zipOut.closeEntry()
        }
    }
}