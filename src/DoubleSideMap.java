import java.util.ArrayList;

class DoubleSideMap<K, V> {
    private ArrayList<K> value1;
    private ArrayList<V> value2;
    private int size;

    DoubleSideMap() {
        value1 = new ArrayList<>();
        value2 = new ArrayList<>();
        size = 0;
    }

    void put(K obj1, V obj2) {
        if (!value1.contains(obj1) && !value2.contains(obj2)) {
            value1.add(obj1);
            value2.add(obj2);
            size++;
        }
    }

    K get1Arg(V obj2) {
        for (int i = 0; i < value2.size(); i++) {
            if (obj2.equals(value2.get(i))) {
                return value1.get(i);
            }
        }
        return null;
    }

    K get1Arg(int index) {
        return value1.get(index);
    }

    V get2Arg(K obj1) {
        for (int i = 0; i < value1.size(); i++) {
            if (obj1.equals(value1.get(i))) {
                return value2.get(i);
            }
        }
        return null;
    }

    V get2Arg(int index) {
        return value2.get(index);
    }

    void clear() {
        value1.clear();
        value2.clear();
        size = 0;
    }

    public String string() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < value1.size(); i++) {
            stringBuilder.append(value1.get(i));
            stringBuilder.append(":");
            stringBuilder.append(value2.get(i));
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }


}
