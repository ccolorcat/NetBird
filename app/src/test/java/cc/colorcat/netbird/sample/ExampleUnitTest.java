package cc.colorcat.netbird.sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import cc.colorcat.netbird.GenericPlatform;
import cc.colorcat.netbird.MRequest;
import cc.colorcat.netbird.NetBird;
import cc.colorcat.netbird.Parser;
import cc.colorcat.netbird.Platform;
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
        MRequest<List<Course>> request = new MRequest.Builder<>(parser)
                .path("api/teacher")
                .add("num", "30")
                .add("type", "4")
                .get()
                .build();

        List<Course> result = BIRD.execute(request);
        System.out.println(result);
    }
}