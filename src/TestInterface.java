import java.util.List;

public interface TestInterface {
    List<String> getList();

    default void clear() {
        List<String> a = getList();
        a = null;
    }
}