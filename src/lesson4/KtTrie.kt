package lesson4

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

        private val data = mutableSetOf<String>()
        private val it: MutableIterator<String> by lazy { data.iterator() }
        private var current: String? = null

        init {
            find(root)
        }

        private fun find(current: Node, acc: String = "") {
            if (current.children.isEmpty()) {
                if (acc.isNotEmpty())
                    data.add(acc.dropLast(1))
            } else {
                current.children.forEach { (t, u) ->
                    find(u, acc + t)
                }
            }
        }

        override fun hasNext(): Boolean {
            return it.hasNext()
        }

        override fun next(): String {
            current = it.next()
            return current!!
        }

        override fun remove() {
            TODO("Not yet implemented")
        }

    }

}