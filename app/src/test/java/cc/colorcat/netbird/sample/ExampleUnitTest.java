package cc.colorcat.netbird.sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import cc.colorcat.netbird.Call;
import cc.colorcat.netbird.GenericPlatform;
import cc.colorcat.netbird.Listener;
import cc.colorcat.netbird.NetBird;
import cc.colorcat.netbird.Parser;
import cc.colorcat.netbird.Platform;
import cc.colorcat.netbird.Request;
import cc.colorcat.netbird.StateIOException;
import cc.colorcat.netbird.logging.LoggingTailInterceptor;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private static final NetBird BIRD;
    private static final Gson GSON;

    static {
        GSON = new GsonBuilder().create();
        Platform platform = new GenericPlatform();

        BIRD = new NetBird.Builder("http://www.imooc.com/")
                .platform(platform)
                .addTailInterceptor(new LoggingTailInterceptor())
                .enableGzip(true)
                .build();
    }


    @Test
    public void testMooc() throws IOException {
        Parser<List<Course>> parser = new ResultParser<List<Course>>(GSON) {};
        Request request = new Request.Builder()
                .path("api/teacher")
                .add("num", "30")
                .add("type", "4")
                .get()
                .build();

        final Call call = BIRD.newCall(request);
//        List<Course> result = call.execute(parser);
//        System.out.println(result);
        call.enqueue(parser, new Listener<List<Course>>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(List<Course> result) {
                System.out.println(result);
            }

            @Override
            public void onFailure(StateIOException cause) {
                cause.printStackTrace();
            }

            @Override
            public void onFinish() {
                goon();
            }
        });
        block();
    }

    private static void block() {
        synchronized (BIRD) {
            try {
                BIRD.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void goon() {
        synchronized (BIRD) {
            BIRD.notify();
        }
    }
}