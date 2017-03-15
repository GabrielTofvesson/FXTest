package sample;

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends Application {

    private static final Unsafe unsafe;
    private static final AtomicBoolean launchCalled;
    private static final long valueInject;

    static{
        Unsafe un = null;
        AtomicBoolean ab = null;
        long vi = 0;
        try{
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            un = (Unsafe) f.get(null);

            f = LauncherImpl.class.getDeclaredField("launchCalled");
            f.setAccessible(true);
            ab = (AtomicBoolean) f.get(null);

            f = AtomicBoolean.class.getDeclaredField("value");
            vi = un.objectFieldOffset(f);
        }catch(Exception ignored){}
        unsafe = un;
        launchCalled = ab;
        valueInject = vi;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) throws Throwable{

        // Start first JavaFX thread
        Thread t = new Thread(() -> { try { launch(args); }catch(Exception ignored){} });
        t.setDaemon(true);
        t.start();
        Thread.sleep(1); // Give thread some time to start and stuff since it doesn't have to invoke unsafe stuff.


        // The other threads should be slow (and fast) enough that they don't need Thread.sleep()
        Thread t1 = new Thread(() -> { unsafe.putIntVolatile(launchCalled, valueInject, 0); try { launch(args); }catch(Exception ignored){} });
        t1.setDaemon(true);
        t1.start();

        Thread t2 = new Thread(() -> { unsafe.putIntVolatile(launchCalled, valueInject, 0); try { launch(args); }catch(Exception ignored){} });
        t2.setDaemon(true);
        t2.start();

        Thread t3 = new Thread(() -> { unsafe.putIntVolatile(launchCalled, valueInject, 0); try { launch(args); }catch(Exception ignored){} });
        t3.setDaemon(true);
        t3.start();

        // -- || -- (Should be infinitely expandable in this fashion)

        unsafe.putIntVolatile(launchCalled, valueInject, 0);
        // Start second JavaFX thread
        try { launch(args); }catch(Exception ignored){}
    }
}
