package com.bukowiecki.weevil

import org.stringtemplate.v4.ST
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.stream.Collectors

/**
 * @author Marcin Bukowiecki
 */
object TestGenerator {

    private const val suffix = "GeneratedTest"

    private const val packagee = "com.bukowiecki.weevil.generated"

    private val toGenerate = mutableMapOf<String, MutableList<File>>()

    private val template = """
        package $packagee
        
        import com.bukowiecki.weevil.GeneratedTestRunner
        import org.junit.Test
        
        /**
         * GENERATED TEST
         *
         * @author Marcin Bukowiecki
         */
        class <testCaseName> : GeneratedTestRunner() {        
            <tests :{test | <test> }>        
        }
        
    """.trimIndent()

    private val unitTestTemplate = """
        
        @Test
        fun <name>() {
            runTest("<path>")
        }        
    """.trimIndent()

    @JvmStatic
    fun main(args: Array<String>) {
        val parentFile = File("./src/test/resources/testData/generated")
        val listFiles = parentFile.listFiles()
        if (listFiles != null) {
            for (listFile in listFiles) {
                val collectSourceFiles = collectSourceFiles(listFile)
                for (collectSourceFile in collectSourceFiles) {
                    val fileName = collectSourceFile.parent.fileName.toString()
                    toGenerate[fileName]?.add(collectSourceFile.toFile()) ?: run {
                        toGenerate[fileName] = mutableListOf(collectSourceFile.toFile())
                    }
                }
            }
        }

        for (entry in toGenerate) {
            val folderName = entry.key
            val testCaseNameFileName = entry.key.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + suffix
            val contents = mutableListOf<String>()

            for (file in entry.value) {
                val st = ST(unitTestTemplate)
                st.add("name", file.nameWithoutExtension)
                st.add("path", "generated/" + folderName + "/" + file.name)
                contents.add(st.render())
            }

            val st = ST(template)
            st.add("testCaseName", testCaseNameFileName)
            st.add("tests", contents)

            val code = st.render()
            Files.writeString(Path.of("./src/test/kotlin/com/bukowiecki/weevil/generated/$testCaseNameFileName.kt"),
                code,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)
        }
    }

    private fun collectSourceFiles(parentPath: File): List<Path> {
        val walk = Files.walk(parentPath.toPath())
        return walk
            .filter { p -> p.toString().endsWith(".java") }
            .collect(Collectors.toList()).toList()
    }
}