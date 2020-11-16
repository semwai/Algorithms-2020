package lesson4

import java.util.*


/**
 * Префиксное дерево для строк
 */
class KtTrie : AbstractMutableSet<String>(), MutableSet<String> {

    private class Node {
        val children: MutableMap<Char, Node> = linkedMapOf()
    }

    private var root = Node()

    override var size: Int = 0
        private set

    override fun clear() {
        root.children.clear()
        size = 0
    }

    private fun String.withZero() = this + 0.toChar()

    private fun findNode(element: String): Node? {
        var current = root
        for (char in element) {
            current = current.children[char] ?: return null
        }
        return current
    }

    override fun contains(element: String): Boolean =
        findNode(element.withZero()) != null

    override fun add(element: String): Boolean {
        var current = root
        var modified = false
        for (char in element.withZero()) {
            val child = current.children[char]
            if (child != null) {
                current = child
            } else {
                modified = true
                val newChild = Node()
                current.children[char] = newChild
                current = newChild
            }
        }
        if (modified) {
            size++
        }
        return modified
    }

    override fun remove(element: String): Boolean {
        val current = findNode(element) ?: return false
        if (current.children.remove(0.toChar()) != null) {
            size--
            return true
        }
        return false
    }

    /**
     * Итератор для префиксного дерева
     *
     * Спецификация: [java.util.Iterator] (Ctrl+Click по Iterator)
     *
     * Сложная
     */
    override fun iterator() = KtTrieIterator()

    inner class KtTrieIterator internal constructor() : MutableIterator<String> {


        private var current: String? = null

        /*
        Считаю эту функцию самой важной в данной реализации итератора.
        Я использовал sequence чтобы избавиться от большой нагрузки при создании итератора и "размазал"
        ее на все прохождения по итератору.
        Память - O(n) (n - кол-во node), но постепенно элементы из стека удаляются, а не лежат там все время.
        Трудоёмкость - каждый вызов next() вызывает данную функцию со своим состоянием. В общем случае O(n^2), но чем
        больше элементов было пройдено, тем быстрее начинает работать функция, т.к. пути к конечным элементам сохраняются
        пока существует итератор.
         */
        private fun find(): Sequence<String> = sequence {
            val s = Stack<Pair<Node, String>>() //node to path
            s.push(root to "")
            while (s.isNotEmpty()) {
                val (current, path) = s.pop()
                if (current.children[0.toChar()] != null && path.isNotEmpty())
                    yield(path)
                current.children.forEach { (c: Char, child: Node) ->
                    s.push(child to path + c)
                }
            }

        }

        private val it: Iterator<String> = find().iterator()

        override fun hasNext(): Boolean {
            return it.hasNext()
        }

        override fun next(): String {
            current = it.next()
            return current!!
        }

        private var deleted: String? = null
        override fun remove() {
            if (current == null)
                throw IllegalStateException()
            if (deleted != null)
                if (deleted == current!!)
                    throw IllegalStateException()
            deleted = current
            remove(current!!)
        }

    }

}