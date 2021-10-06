package net.cyberflame.grythvy.utils;

public class Lyrics
{
    private final String title;
    private final String author;
    private final String content;
    private final String url;

    Lyrics(String title, String author, String content, String url, @SuppressWarnings("unused") String source)
    {
        this.title = title;
        this.author = author;
        this.content = content;
        this.url = url;
    }

    /**
     * The title of the song
     *
     * @return the title of the song
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * The author/artist of the song
     *
     * @return the author/artist of the song
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * The content of the lyrics
     *
     * @return the lyrics
     */
    public String getContent()
    {
        return content;
    }

    /**
     * The URL the lyrics can be found at
     *
     * @return the URL the lyrics can be found at
     */
    public String getURL()
    {
        return url;
    }

}