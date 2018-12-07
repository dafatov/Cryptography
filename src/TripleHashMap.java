import java.util.ArrayList;

class  TripleHashMap<K, V, P> {
    private ArrayList<K> key;
    private ArrayList<V> value1;
    private ArrayList<P> value2;
    private int size;

    TripleHashMap() {
        key = new ArrayList<>();
        value1 = new ArrayList<>();
        value2 = new ArrayList<>();
        size = 0;
    }

    TripleHashMap(String[] values) {
        for (String valueF : values) {
            String[] value = valueF.split(":");
            assert key != null;
            key.add((K) value[0]);
            assert value1 != null;
            value1.add((V) value[1]);
            assert value2 != null;
            value2.add((P) value[2]);
            size++;
        }
    }

    void put(K key, V obj1, P obj2) {
        if (!this.key.contains(key)) {
            this.key.add(key);
            value1.add(obj1);
            value2.add(obj2);
            size++;
        }
    }

    DoubleSideMap<V, P> get(K key) {
        for (int i = 0; i < this.key.size(); i++) {
            if (this.key.get(i).equals(key)) {
                DoubleSideMap<V, P> tmp = new DoubleSideMap<>();
                tmp.put(value1.get(i), value2.get(i));
                return tmp;
            }
        }
        return null;
    }

    void clear() {
        key.clear();
        value1.clear();
        value2.clear();
        size = 0;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < size; i++) {
            stringBuilder.append(key.get(i));
            stringBuilder.append(":");
            stringBuilder.append(value1.get(i));
            stringBuilder.append(":");
            stringBuilder.append(value2.get(i));
            if (i!=size-1) stringBuilder.append("\n");
         }
         return stringBuilder.toString();
    }

    boolean contains(K key) {
        return this.key.contains(key);
    }
}
