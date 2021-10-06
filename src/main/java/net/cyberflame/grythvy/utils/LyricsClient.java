package net.cyberflame.grythvy.utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LyricsClient
{
    private final Config config = ConfigFactory.load();
    private final HashMap<String, Lyrics> cache = new HashMap<>();
    private final OutputSettings noPrettyPrint = new OutputSettings().prettyPrint(false);
    private final Safelist newlineSafelist = Safelist.none().addTags("br", "p");
    private final Executor executor;
    private final String defaultSource, userAgent;
    private final int timeout;

    /**
     * Constructs a new {@link LyricsClient} using all defaults
     */
    public LyricsClient()
    {
        this(null, null);
    }

    /**
     * Constructs a new {@link LyricsClient}, specifying the default source for lyrics as well as an {@link Executor} to
     * be used for making {@link CompletableFuture}s
     *
     * @param defaultSource
     *         the default source for lyrics
     * @param executor
     *         the executor to use internally
     */
    public LyricsClient(String defaultSource, Executor executor)
    {
        this.defaultSource = defaultSource == null ? config.getString("lyrics.default") : defaultSource;
        this.userAgent     = config.getString("lyrics.user-agent");
        this.timeout       = config.getInt("lyrics.timeout");
        this.executor      = executor == null ? Executors.newCachedThreadPool() : executor;
    }

    /**
     * Gets the lyrics for the provided search from the default source. To get lyrics asynchronously, call {@link
     * CompletableFuture#thenAccept(java.util.function.Consumer)}. To block and return lyrics, use {@link
     * CompletableFuture#get()}.
     *
     * @param search
     *         the song info to search for
     *
     * @return a {@link CompletableFuture} to access the lyrics. The Lyrics object may be null if no lyrics were found.
     */
    public CompletableFuture<Lyrics> getLyrics(String search)
    {
        return getLyrics(search, defaultSource);
    }

    /**
     * Gets the lyrics for the provided search from the provided source. To get lyrics asynchronously, call {@link
     * CompletableFuture#thenAccept(java.util.function.Consumer)}. To block and return lyrics, use {@link
     * CompletableFuture#get()}.
     *
     * @param search
     *         the song info to search for
     * @param source
     *         the source to use (must be defined in config)
     *
     * @return a {@link CompletableFuture} to access the lyrics. The Lyrics object may be null if no lyrics were found.
     */
    public CompletableFuture<Lyrics> getLyrics(String search, String source)
    {
        String cacheKey = source + "||" + search;
        if (cache.containsKey(cacheKey))
            return CompletableFuture.completedFuture(cache.get(cacheKey));
        try
            {
                String searchUrl = String.format(config.getString("lyrics." + source + ".search.url"), search);
                boolean jsonSearch = config.getBoolean("lyrics." + source + ".search.json");
                String select = config.getString("lyrics." + source + ".search.select");
                return CompletableFuture.supplyAsync(() ->
                                                         {
                                                         try
                                                             {
                                                                 Document doc;
                                                                 Connection connection =
                                                                         Jsoup.connect(searchUrl).userAgent(userAgent)
                                                                              .timeout(timeout);
                                                                 if (jsonSearch)
                                                                     {
                                                                         String body =
                                                                                 connection.ignoreContentType(true)
                                                                                           .execute().body();
                                                                         JSONObject json = new JSONObject(body);
                                                                         doc = Jsoup.parse(XML.toString(json));
                                                                     }
                                                                 else
                                                                     doc = connection.get();

                                                                 Element urlElement = doc.selectFirst(select);
                                                                 String url;
                                                                 if (jsonSearch)
                                                                     {
                                                                         assert urlElement != null;
                                                                         url = urlElement.text();
                                                                     }
                                                                 else
                                                                     {
                                                                         assert urlElement != null;
                                                                         url = urlElement.attr("abs:href");
                                                                     }
                                                                 if (url.isEmpty())
                                                                     return null;
                                                                 doc = Jsoup.connect(url).userAgent(userAgent)
                                                                            .timeout(timeout).get();
                                                                 Lyrics lyrics = new Lyrics(
                                                                         Objects.requireNonNull(parseAlternatives("title", source,
                                                                                                   doc)).ownText(),
                                                                         Objects.requireNonNull(parseAlternatives("author", source,
                                                                                           doc)).ownText(),
                                                                         cleanWithNewlines(
                                                                                 Objects.requireNonNull(parseAlternatives("content",
                                                                                                       source,
                                                                                                   doc))),
                                                                         url,
                                                                         source);
                                                                 cache.put(cacheKey, lyrics);
                                                                 return lyrics;
                                                             }
                                                         catch (IOException | NullPointerException | JSONException ex)
                                                             {
                                                                 return null;
                                                             }
                                                         }, executor);
            }
        catch (ConfigException ex)
            {
                throw new IllegalArgumentException(
                        String.format("Source '%s' does not exist or is not configured correctly", source));
            }
        catch (Exception ignored)
            {
                return null;
            }
    }

    private Element parseAlternatives(String key, String source, Document doc)
    {
        List<String> titleSelectors = config.getStringList("lyrics." + source + ".parse." + key);
        for (String selector : titleSelectors)
            {
                Element element = doc.selectFirst(selector);
                if (element != null)
                    return element;
            }
        return null;
    }

    private String cleanWithNewlines(Element element)
    {
        return Jsoup.clean(Jsoup.clean(element.html(), newlineSafelist), "", Safelist.none(), noPrettyPrint);
    }
}