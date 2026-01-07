package supplement;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class HelloWorld {// implements Serializable {
    private String message = "World";
    private int count = 0;
    private List<String> listTest;
    // private Map<String, Integer> mapTest;

    public HelloWorld() {
       this.message = "Hello";
       this.count = 0;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void incrementCount() {
       count++;
    }
}