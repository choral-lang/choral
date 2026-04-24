package supplement;

import java.io.Serializable;
import java.util.List;

interface testInterface {
  final int a = 10;

  void display();
}

interface testInterface2 extends testInterface {
  final int a = 10;

  void display();
}

enum Day {
  MONDAY,
  TUESDAY,
  WEDNESDAY
}

public class HelloWorld implements Serializable {
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
