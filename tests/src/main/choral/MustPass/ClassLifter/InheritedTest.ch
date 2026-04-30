package ClassLifter.InheritedTest;

import choral.MustPass.ClassLifter.InheritedTest.*;

public class InheritedTest@(A){
    public void run(){
        Ping@A ping = new Ping@A();
        ping.toString();
    }
}
