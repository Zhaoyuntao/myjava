import java.util.ArrayList;
import java.util.List;

public class Test0 implements TestInterface {
    List<String> abc = new ArrayList<>();

    @Override
    public List<String> getList() {
        return abc;
    }
}
