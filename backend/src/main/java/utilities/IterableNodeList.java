package utilities;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IterableNodeList implements Iterable<Node> {
    private final NodeList nodeList;

    private IterableNodeList(NodeList list) {
        this.nodeList = list;
    }

    public Stream<Node> stream() {
        return IntStream.range(0, nodeList.getLength())
                .mapToObj(nodeList::item);
    }

    @NotNull
    @Override
    public Iterator<Node> iterator() {
        return new Iterator<>() {
            private int iter;

            @Override
            public boolean hasNext() {
                return iter < nodeList.getLength();
            }

            @Override
            public Node next() {
                return nodeList.item(iter++);
            }
        };
    }

    public static IterableNodeList of(NodeList list) {
        return new IterableNodeList(list);
    }
}
