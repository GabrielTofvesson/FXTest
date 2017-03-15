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

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) throws Throwable{

        // Start first JavaFX thread
        Thread t = new Thread(() -> { try { resetJFXTracker(); launch(args); }catch(Exception ignored){} });
        t.setDaemon(true);
        t.start();
        Thread.sleep(2);

        Thread t1 = new Thread(() -> { resetJFXTracker(); try { launch(args); }catch(Exception ignored){} });
        t1.setDaemon(true);
        t1.start();
        Thread.sleep(2);

        Thread t2 = new Thread(() -> { resetJFXTracker(); try { launch(args); }catch(Exception ignored){} });
        t2.setDaemon(true);
        t2.start();
        Thread.sleep(2);

        resetJFXTracker();
        // Start second JavaFX thread
        try { launch(args); }catch(Exception ignored){}
    }

    public static void resetJFXTracker(){
        try {
            // Get unsafe
            final Unsafe u;
            Field f1 = Unsafe.class.getDeclaredField("theUnsafe");
            f1.setAccessible(true);
            u = (Unsafe) f1.get(null);

            // Modify some memory
            Field f = AtomicBoolean.class.getDeclaredField("value");
            Field f2 = LauncherImpl.class.getDeclaredField("launchCalled");
            f2.setAccessible(true);
            u.putIntVolatile(f2.get(null), u.objectFieldOffset(f), 0);
        }catch(Exception ignored){}
    }
}
