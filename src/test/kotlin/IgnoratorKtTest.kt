import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

internal class IgnoratorKtTest {

    @ParameterizedTest
    @CsvSource("src/test/resources/.ignore1, 2",
        "src/test/resources/.ignore2, 0",
        "src/test/resources/.ignore3, 3",
        "src/test/resources/.ignore4, 6",
        "src/test/resources/.ignore5, 12")
    fun getIgnoreList(fileName: String, size: Int) {
        val file = File(fileName)
        assertEquals(getIgnoreList(file).size, size)
    }

    // этот тест тупенький потому что junit постоянно какие-то временные
    // файлы создаёт, и вывод программы меняется
    @ParameterizedTest
    @CsvSource("src/test/resources/.ignore1, 212",
        "src/test/resources/.ignore2, 222",
        "src/test/resources/.ignore3, 74",
        "src/test/resources/.ignore4, 8",
        "src/test/resources/.ignore5, 169")
    fun walkTree(fileName: String, size: Int) {
        val ignore = File(fileName)
        val set = getIgnoreList(ignore)
        val root = File(System.getProperty("user.dir"))
        assertEquals(walkTree(root, root.toPath(), set).size, size)
    }

    @ParameterizedTest
    @CsvSource("build/classes/kotlin/main/META-INF/list, build/classes/kotlin/main/list2, true",
        "path/kek, path/lol/heh, false",
        "path1/kek, path2/kek, true",
        "возьмите/на/стажировку/пожалуйста, возьмите/на/стажировку/плиз, false",
        "ятут, тестыпишу, false",
        ".ignore, build.gradle, true",
        "build.gradle, .ignore, false")
    fun comparePaths(a: String, b: String, aIsHigher: Boolean) {
        assert(if (aIsHigher) comparePaths(a,b) < 0 else comparePaths(a,b) > 0)
    }
}
