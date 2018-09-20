package cc.colorcat.netbird.sample;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import org.junit.Test;
import org.junit.runner.RunWith;

import cc.colorcat.netbird.Level;
import cc.colorcat.netbird.MRequest;
import cc.colorcat.netbird.NetBird;
import cc.colorcat.netbird.Request;
import cc.colorcat.netbird.StringParser;
import cc.colorcat.netbird.android.AndroidPlatform;
import cc.colorcat.netbird.logging.LoggingTailInterceptor;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private static final NetBird BIRD;

    static {
        BIRD = new NetBird.Builder("https://api.github.com/")
                .platform(new AndroidPlatform())
                .connectTimeOut(10000)
                .readTimeOut(10000)
                .enableGzip(true)
                .logLevel(Level.VERBOSE)
                .addTailInterceptor(new LoggingTailInterceptor(false) {
                    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    private JsonParser parser = new JsonParser();

                    @Override
                    protected String formatResponse(String content, String contentType) {
                        return '\n' + gson.toJson(parser.parse(content));
                    }
                })
                .build();
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("cc.colorcat.netbird.sample", appContext.getPackageName());
    }

    @Test
    public void testGithubRepos() throws Exception {
        Request request = new Request.Builder()
                .path("users/ccolorcat/repos")
                .get()
                .build();
        BIRD.newCall(request).execute();
    }

    @Test
    public void testImooc() throws Exception {
        MRequest<String> request = new MRequest.Builder<>(StringParser.getDefault())
                .url("http://www.imooc.com/api/teacher")
                .add("type", "4")
                .add("num", "30")
                .get()
                .build();
        BIRD.execute(request);
    }
}
