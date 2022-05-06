package m4gshm.benchmark.rest.ktor.graalvm

import co.touchlab.stately.isolate.IsolateState

class IsolateStateMap<K, V> : MutableMap<K, V> {
    private val state = IsolateState { mutableMapOf<K, V>() }
    override fun putAll(from: Map<out K, V>) = state.access { it.putAll(from) }
    override fun put(key: K, value: V): V? = state.access { it.put(key, value) }
    override fun get(key: K): V? = state.access { it[key] }
    override fun remove(key: K): V? = state.access { it.remove(key) }
    override fun clear() = state.access { it.clear() }
    override fun containsKey(key: K): Boolean = state.access { it.containsKey(key) }
    override fun containsValue(value: V): Boolean = state.access { it.containsValue(value) }
    override fun isEmpty() = state.access { it.isEmpty() }
    override val size get() = state.access { it.size }
    override val keys get() = state.access { it.keys }
    override val values get() = state.access { it.values }
    override val entries get() = state.access { it.entries }
}