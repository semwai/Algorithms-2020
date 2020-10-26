package lesson3

import java.util.*
import kotlin.NoSuchElementException
import kotlin.math.max

// attention: Comparable is supported but Comparator is not
class KtBinarySearchTree<T : Comparable<T>> : AbstractMutableSet<T>(), CheckableSortedSet<T> {

    private class Node<T>(
        val value: T
    ) {
        var left: Node<T>? = null
        var right: Node<T>? = null

        fun min(): T = left?.min() ?: value

        override fun toString(): String {
            val l = if (left != null) ",'l':${left.toString()}" else ""
            val r = if (right != null) ",'r':${right.toString()}" else ""
            return "{'val':'$value' $l $r}"
        }
    }

    private var root: Node<T>? = null

    override var size = 0
        private set

    private fun find(value: T): Node<T>? =
        root?.let { find(it, value) }

    private fun find(start: Node<T>, value: T): Node<T> {
        val comparison = value.compareTo(start.value)
        return when {
            comparison == 0 -> start
            comparison < 0 -> start.left?.let { find(it, value) } ?: start
            else -> start.right?.let { find(it, value) } ?: start
        }
    }

    override operator fun contains(element: T): Boolean {
        val closest = find(element)
        return closest != null && element.compareTo(closest.value) == 0
    }

    /**
     * Добавление элемента в дерево
     *
     * Если элемента нет в множестве, функция добавляет его в дерево и возвращает true.
     * В ином случае функция оставляет множество нетронутым и возвращает false.
     *
     * Спецификация: [java.util.Set.add] (Ctrl+Click по add)
     *
     * Пример
     */
    override fun add(element: T): Boolean {
        val closest = find(element)
        val comparison = if (closest == null) -1 else element.compareTo(closest.value)
        if (comparison == 0) {
            return false
        }
        val newNode = Node(element)
        when {
            closest == null -> root = newNode
            comparison < 0 -> {
                assert(closest.left == null)
                closest.left = newNode
            }
            else -> {
                assert(closest.right == null)
                closest.right = newNode
            }
        }
        size++
        return true
    }


    /**
     * Удаление элемента из дерева
     *
     * Если элемент есть в множестве, функция удаляет его из дерева и возвращает true.
     * В ином случае функция оставляет множество нетронутым и возвращает false.
     * Высота дерева не должна увеличиться в результате удаления.
     *
     * Спецификация: [java.util.Set.remove] (Ctrl+Click по remove)
     * (в Котлине тип параметера изменён с Object на тип хранимых в дереве данных)
     *
     * Средняя
     *
     * Память    -  O(1)
     * Сложность -  O(n)
     */
    override fun remove(element: T): Boolean {
        if (!contains(element)) return false

        var current = root
        var parent: Node<T>? = null
        var left = true

        while (current!!.value != element) {
            parent = current
            when (element.compareTo(current.value)) {
                1 -> {
                    current = current.right
                    left = false
                }
                -1 -> {
                    current = current.left
                    left = true
                }
            }
        }

        val x = current.left == null
        val y = current.right == null

        if (x && y) {
            when {
                current == root -> root = null
                left -> parent?.left = null
                else -> parent?.right = null
            }
            size--
        }
        if (x && !y) {
            when {
                current == root -> root = current.right
                left -> parent?.left = current.right
                else -> parent?.right = current.right
            }
            size--
        }
        if (!x && y) {
            when {
                current == root -> root = current.left
                left -> parent?.left = current.left
                else -> parent?.right = current.left
            }
            size--
        }
        if (!x && !y) {
            val min = current.right!!.min()
            remove(min)
            when {
                current == root -> {
                    val l = root!!.left
                    val r = root!!.right
                    root = Node(min)
                    root?.left = l
                    root?.right = r
                }
                left -> {
                    val l = parent!!.left?.left
                    val r = parent.left?.right
                    parent.left = Node(min)
                    parent.left?.left = l
                    parent.left?.right = r
                }
                else -> {
                    val l = parent!!.right?.left
                    val r = parent.right?.right
                    parent.right = Node(min)
                    parent.right?.left = l
                    parent.right?.right = r
                }
            }
        }
        return true
    }


    override fun comparator(): Comparator<in T>? =
        null

    override fun iterator(): MutableIterator<T> =
        BinarySearchTreeIterator()

    inner class BinarySearchTreeIterator internal constructor() : MutableIterator<T> {

        private var current: Node<T>? = null
        private var pref: Node<T>? = null

        init {
            //находим самую маленькую ноду
            var start = root
            while (start?.left != null) {
                start = start.left
            }
            current = start
        }


        /**
         * Проверка наличия следующего элемента
         *
         * Функция возвращает true, если итерация по множеству ещё не окончена (то есть, если вызов next() вернёт
         * следующий элемент множества, а не бросит исключение); иначе возвращает false.
         *
         * Спецификация: [java.util.Iterator.hasNext] (Ctrl+Click по hasNext)
         *
         * Средняя
         *
         * Сложность    -   O(1)
         * Память       -   O(1)
         */
        override fun hasNext() = current != null

        /**
         * Получение следующего элемента
         *
         * Функция возвращает следующий элемент множества.
         * Так как BinarySearchTree реализует интерфейс SortedSet, последовательные
         * вызовы next() должны возвращать элементы в порядке возрастания.
         *
         * Бросает NoSuchElementException, если все элементы уже были возвращены.
         *
         * Спецификация: [java.util.Iterator.next] (Ctrl+Click по next)
         *
         * Средняя
         * Сложность    -   O(N)
         * Память       -   O(N)
         */
        override fun next(): T {
            if (current == null)
                throw NoSuchElementException()
            val e = mutableSetOf<Node<T>>()
            val s = Stack<Node<T>>()
            s.push(root)
            while (s.isNotEmpty()) {
                val v = s.pop()
                if (v.value > current!!.value)
                    e.add(v)
                if (v.right != null)
                    s.push(v.right)
                if (v.left != null)
                    s.push(v.left)
            }
            pref = current
            current = e.firstOrNull()
            return pref!!.value
        }


        /**
         * Удаление предыдущего элемента
         *
         * Функция удаляет из множества элемент, возвращённый крайним вызовом функции next().
         *
         * Бросает IllegalStateException, если функция была вызвана до первого вызова next() или же была вызвана
         * более одного раза после любого вызова next().
         *
         * Спецификация: [java.util.Iterator.remove] (Ctrl+Click по remove)
         *
         * Сложная
         *
         * Характеристики такие же как и у простого удаления.
         */
        private var deleted: T? = null
        override fun remove() {
            if (pref == null)
                throw IllegalStateException()

            if (deleted != null) {
                if (deleted == pref!!.value)
                    throw IllegalStateException()
            }
            deleted = pref!!.value
            remove(pref!!.value)
        }
    }

    /**
     * Подмножество всех элементов в диапазоне [fromElement, toElement)
     *
     * Функция возвращает множество, содержащее в себе все элементы дерева, которые
     * больше или равны fromElement и строго меньше toElement.
     * При равенстве fromElement и toElement возвращается пустое множество.
     * Изменения в дереве должны отображаться в полученном подмножестве, и наоборот.
     *
     * При попытке добавить в подмножество элемент за пределами указанного диапазона
     * должен быть брошен IllegalArgumentException.
     *
     * Спецификация: [java.util.SortedSet.subSet] (Ctrl+Click по subSet)
     * (настоятельно рекомендуется прочитать и понять спецификацию перед выполнением задачи)
     *
     * Очень сложная (в том случае, если спецификация реализуется в полном объёме)
     */
    override fun subSet(fromElement: T, toElement: T): SortedSet<T> {
        TODO()
    }

    /**
     * Подмножество всех элементов строго меньше заданного
     *
     * Функция возвращает множество, содержащее в себе все элементы дерева строго меньше toElement.
     * Изменения в дереве должны отображаться в полученном подмножестве, и наоборот.
     *
     * При попытке добавить в подмножество элемент за пределами указанного диапазона
     * должен быть брошен IllegalArgumentException.
     *
     * Спецификация: [java.util.SortedSet.headSet] (Ctrl+Click по headSet)
     * (настоятельно рекомендуется прочитать и понять спецификацию перед выполнением задачи)
     *
     * Сложная
     */
    override fun headSet(toElement: T): SortedSet<T> {
        TODO()
    }

    /**
     * Подмножество всех элементов нестрого больше заданного
     *
     * Функция возвращает множество, содержащее в себе все элементы дерева нестрого больше toElement.
     * Изменения в дереве должны отображаться в полученном подмножестве, и наоборот.
     *
     * При попытке добавить в подмножество элемент за пределами указанного диапазона
     * должен быть брошен IllegalArgumentException.
     *
     * Спецификация: [java.util.SortedSet.tailSet] (Ctrl+Click по tailSet)
     * (настоятельно рекомендуется прочитать и понять спецификацию перед выполнением задачи)
     *
     * Сложная
     */
    override fun tailSet(fromElement: T): SortedSet<T> {
        TODO()
    }

    override fun first(): T {
        var current: Node<T> = root ?: throw NoSuchElementException()
        while (current.left != null) {
            current = current.left!!
        }
        return current.value
    }

    override fun last(): T {
        var current: Node<T> = root ?: throw NoSuchElementException()
        while (current.right != null) {
            current = current.right!!
        }
        return current.value
    }

    override fun height(): Int =
        height(root)

    private fun height(node: Node<T>?): Int {
        if (node == null) return 0
        return 1 + max(height(node.left), height(node.right))
    }

    override fun checkInvariant(): Boolean =
        root?.let { checkInvariant(it) } ?: true

    private fun checkInvariant(node: Node<T>): Boolean {
        val left = node.left
        if (left != null && (left.value >= node.value || !checkInvariant(left))) return false
        val right = node.right
        return right == null || right.value > node.value && checkInvariant(right)
    }

}
