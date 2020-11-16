package lesson5

/**
 * Множество(таблица) с открытой адресацией на 2^bits элементов без возможности роста.
 */
class KtOpenAddressingSet<T : Any>(private val bits: Int) : AbstractMutableSet<T>() {
    init {
        require(bits in 2..31)
    }

    private class Deleted {}

    private val capacity = 1 shl bits

    private val storage = Array<Any?>(capacity) { null }

    override var size: Int = 0

    /**
     * Индекс в таблице, начиная с которого следует искать данный элемент
     */
    private fun T.startingIndex(): Int {
        return hashCode() and (0x7FFFFFFF shr (31 - bits))
    }

    /**
     * Проверка, входит ли данный элемент в таблицу
     */
    override fun contains(element: T): Boolean {
        var index = element.startingIndex()
        var current = storage[index]
        while (current != null || current is Deleted) {
            if (current == element) {
                return true
            }
            index = (index + 1) % capacity
            current = storage[index]
        }
        return false
    }

    /**
     * Добавление элемента в таблицу.
     *
     * Не делает ничего и возвращает false, если такой же элемент уже есть в таблице.
     * В противном случае вставляет элемент в таблицу и возвращает true.
     *
     * Бросает исключение (IllegalStateException) в случае переполнения таблицы.
     * Обычно Set не предполагает ограничения на размер и подобных контрактов,
     * но в данном случае это было введено для упрощения кода.
     */
    override fun add(element: T): Boolean {
        val startingIndex = element.startingIndex()
        var index = startingIndex
        var current = storage[index]
        while (current != null) {
            if (current == element) {
                return false
            }
            if (current is Deleted)
                break
            index = (index + 1) % capacity
            check(index != startingIndex) { "Table is full" }
            current = storage[index]
        }
        storage[index] = element
        size++
        return true
    }

    /**
     * Удаление элемента из таблицы
     *
     * Если элемент есть в таблица, функция удаляет его из дерева и возвращает true.
     * В ином случае функция оставляет множество нетронутым и возвращает false.
     * Высота дерева не должна увеличиться в результате удаления.
     *
     * Спецификация: [java.util.Set.remove] (Ctrl+Click по remove)
     *
     * Средняя
     *
     * память - O(1)
     * трудоемкость - от O(1) до O(n) - где n = кол-во элементов с одинаковым индексом
     * (O(n) произойдет в случае если в коллекции большое количество элементов относительно максимального возможного)
     */
    override fun remove(element: T): Boolean {
        var index = element.startingIndex()
        var current = storage[index]
        while (current != null || current is Deleted) {
            if (current == element) {
                storage[index] = Deleted()
                size--
                return true
            }
            index = (index + 1) % capacity
            current = storage[index]
        }
        return false
    }

    /**
     * Создание итератора для обхода таблицы
     *
     * Не забываем, что итератор должен поддерживать функции next(), hasNext(),
     * и опционально функцию remove()
     *
     * Спецификация: [java.util.Iterator] (Ctrl+Click по Iterator)
     *
     * Средняя (сложная, если поддержан и remove тоже)
     *
     * При создании итератора требуется O(n^2) времени.
     */
    override fun iterator(): MutableIterator<T> {
        return AddresIterator()
    }

    inner class AddresIterator internal constructor() : MutableIterator<T> {

        /*
        Тут такая-же ситуация, как и в Trie. Раньше это была просто коллекция без asSequence, которая за n^2 времени
        находила все подходящие элементы, а дальше hasNext & next давали результат за константное время O(1).
        Теперь начальных затрат нет, но вышеописанные функции работают за линейное время.
        Память O(n)
         */
        private val it = storage.asSequence().filterNotNull().filter { it !is Deleted }.iterator()

        private var current: T? = null

        override fun hasNext(): Boolean {
            return it.hasNext()
        }

        override fun next(): T {
            if (!hasNext())
                throw IllegalStateException()
            current = it.next() as T
            return current!!
        }

        private var deleted: T? = null

        /*
       время и память - аналогично с обычным remove
        */
        override fun remove() {
            if (current == null)
                throw IllegalStateException()
            if (deleted != null) {
                if (deleted == current)
                    throw IllegalStateException()
            }
            deleted = current
            remove(current!!)
        }

    }
}