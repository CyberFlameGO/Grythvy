package net.cyberflame.grythvy.entities;

/**
 * @param <K>
 * @param <V>
 */
public record Pair<K, V>(K key, V value)
{

    public K getKey()
    {
        return key;
    }

    public V getValue()
    {
        return value;
    }
}
