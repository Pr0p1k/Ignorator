import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * If no .ignore found, warns and outputs all files
 * from the current directory
 */
fun main() {
    val path = Paths.get(System.getProperty("user.dir"))
    val pathFile = path.toFile()
    val ignore = findIgnore(pathFile)
    val ignoreList = getIgnoreList(ignore)
    if (!ignore.exists())
        println("\u001B[32mNo .ignore found\u001B[0m")
    // этот метод использует File.listFiles, можно было бы использовать котлиновский
    // File.walkTopDown, но он медленнее, потому что он выдаёт список файлов
    // и там нужно было бы проверять каждый файл, а здесь я прохожу каталоги рекурсивно, игнорируя ненужные
    walkTree(pathFile, path, ignoreList).forEach(::println)
}

/**
 * Returns .ignore file in the given path
 * if file doesn't exist - it's ok. Existence should be checked next
 */
fun findIgnore(path: File): File = File(path, ".ignore")

/**
 * Reads .ignore and returns set of paths to ignore
 */
fun getIgnoreList(file: File): Set<String> {
    return if (file.exists())
        file.readLines().filter {
            it.isNotEmpty() && !it.startsWith("#")
        }.map { Paths.get(it).toAbsolutePath().toString() }.toSet()
    else emptySet()
}

/**
 * Walks the file tree of [path] using [File.listFiles] and matches files in [ignoreList]
 */
fun walkTree(root: File, path: Path, ignoreList: Set<String>): Set<String> {
    val set = TreeSet<String>(::comparePaths)
    // TreeSet требует log(n) на операции вставки/получения и вставляет новые элементы в нужное место
    path.toFile().listFiles().forEach {
        if (ignoreList.contains(path.toString()) || ignoreList.contains(it.absolutePath)) return@forEach
        if (it.isDirectory) set.addAll(walkTree(root, it.toPath(), ignoreList))
        else set.add(it.absolutePath.removePrefix(root.absolutePath + "/"))
    }
    return set
}

/**
 * Compares strings with the same rule, as system uses for paths
 */
fun comparePaths(a: String, b: String): Int {
    val subA = a.split("/")
    val subB = b.split("/")
    subA.forEachIndexed { index, path ->
        if (index < subB.size) {
            if (index < subA.lastIndex)
                if (index < subB.lastIndex) { // if both are directories
                    if (path == subB[index]) return@forEachIndexed
                    return a.toLowerCase().compareTo(b.toLowerCase())
                } else // subB here is file, so path is dir and should be higher
                    return -1
        } else {
            if (subB.size == 1) return -1
            return a[index - 1].compareTo(b[index])
        }
    }
    if (subB.size > subA.size) return 1
    return a.toLowerCase().compareTo(b.toLowerCase())
}
