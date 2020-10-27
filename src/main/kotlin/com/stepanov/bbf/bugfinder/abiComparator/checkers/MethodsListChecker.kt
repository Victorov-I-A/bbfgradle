package com.stepanov.bbf.bugfinder.abiComparator.checkers

import com.stepanov.bbf.bugfinder.abiComparator.checkers.ClassChecker
import com.stepanov.bbf.bugfinder.abiComparator.checkers.compareLists
import com.stepanov.bbf.bugfinder.abiComparator.isPrivate
import com.stepanov.bbf.bugfinder.abiComparator.isSynthetic
import com.stepanov.bbf.bugfinder.abiComparator.listOfNotNull
import com.stepanov.bbf.bugfinder.abiComparator.methodFlags
import com.stepanov.bbf.bugfinder.abiComparator.reports.ClassReport
import com.stepanov.bbf.bugfinder.abiComparator.tasks.methodId
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

class MethodsListChecker : ClassChecker {
    override val name = "class.methods"

    private val ignoreMissingMethod1IfMethod2LooksLikeClosureConverted = true

    override fun check(class1: ClassNode, class2: ClassNode, report: ClassReport) {
        val methods1 = class1.methods.listOfNotNull<MethodNode>().associateBy { it.methodId() }
        val methods2 = class2.methods.listOfNotNull<MethodNode>().associateBy { it.methodId() }

        val relevantMethodIds = methods1.keys.union(methods2.keys)
            .filter {
                val method1 = methods1[it]
                val method2 = methods2[it]
                acceptNonSyntheticMethods(method1, method2) &&
                        !ignoreMissingClosureConvertedMethod1(method1, method2)
            }.toSet()

        val methodIds1 = methods1.keys.intersect(relevantMethodIds).sorted()
        val methodIds2 = methods2.keys.intersect(relevantMethodIds).sorted()

        val listDiff = compareLists(methodIds1, methodIds2) ?: return
        report.addMethodListDiffs(
            listDiff.diff1.map { it.toMethodWithFlags(methods1) },
            listDiff.diff2.map { it.toMethodWithFlags(methods2) }
        )
    }

    private fun acceptNonSyntheticMethods(method1: MethodNode?, method2: MethodNode?) =
        method1 != null && !method1.access.isSynthetic() ||
                method2 != null && !method2.access.isSynthetic()

    private fun ignoreMissingClosureConvertedMethod1(method1: MethodNode?, method2: MethodNode?) =
        ignoreMissingMethod1IfMethod2LooksLikeClosureConverted &&
                method1 == null && method2 != null &&
                method2.access.isPrivate() &&
                method2.name.contains('$')

    private fun String.toMethodWithFlags(methods: Map<String, MethodNode>): String {
        val method = methods[this] ?: return this
        return "$this ${method.access.methodFlags()}"
    }
}