package net.hypercubemc.iris_installer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
public class GithubMeta {
    private final String metaUrl;
    private final List<String> gameVersions = new ArrayList<>();
    private final List<String> releases = new ArrayList<>();


    public GithubMeta(String metaUrl) {
        this.metaUrl = metaUrl;
    }

    public void load() throws IOException, JSONException {
        JSONArray json = readJsonFromUrl(this.metaUrl + "tags");
        //get name
        for (int i = 0; i < json.length(); i++) {
            JSONObject object = json.getJSONObject(i);
            String name = object.getString("name");
            releases.add(name);
        }
    }

    public String getReleaseLink(String gameVersion) throws IOException {

        //set sorted to the releases list
        String shortenedGameVersion = gameVersion.substring(0, gameVersion.length() - 1) + "x";
        List<String> sorted = new ArrayList<>();
        //only add the releases that match the game version
        if (!gameVersion.equals("latest")) {
            for (int i = 0; i < releases.size(); i++) {
                if (releases.get(i).contains(gameVersion) || releases.get(i).contains(shortenedGameVersion)) {
                    sorted.add(releases.get(i));
                }
            }
        }
        else {
            sorted.add("latest");
        }
        System.out.println("Sorted for game version " + gameVersion + ": " + sorted);
        return getUrl(sorted);
    }


    public String getUrl(List versionTag) throws IOException, JSONException {
        JSONObject json;
        //try the first tag and if it fails, try the second tag
        for (int i = 0; i < versionTag.size(); i++) {
            try {
            if (!versionTag.get(i).equals("latest")) {
                json = readJsonObjectFromUrl(this.metaUrl + "releases/tags/" + versionTag.get(i));
            } else {
                json = readJsonObjectFromUrl(this.metaUrl + "releases/latest");
            }
            //get_download_url from assets
            JSONArray assets = json.getJSONArray("assets");
            //find the first browser download link that ends with .jar
            for (int z = 0; z < assets.length(); z++) {
                JSONObject object = assets.getJSONObject(z);
                if (object.getString("name").endsWith(".jar")) {
                    return object.getString("browser_download_url");
                }
            }

            } catch (FileNotFoundException e) {
                System.out.println("Failed to get url for " + versionTag.get(i));
                System.out.println("Trying next tag");
            }
            catch (IOException e) {
                System.out.println("GiHub API limit reached");
                break;
            }
        }
        return null;
    }

    public static String readAll(Reader reader) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int codePoint;
        while ((codePoint = reader.read()) != -1) {
            stringBuilder.append((char) codePoint);
        }
        return stringBuilder.toString();
    }

    public static JSONArray readJsonFromUrl(String url) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8));
        return new JSONArray(readAll(bufferedReader));
    }

    public static JSONObject readJsonObjectFromUrl(String url) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8));
        return new JSONObject(readAll(bufferedReader));
    }

    public static class Release {
        List<String> names = new ArrayList<>();

        public Release(JSONObject jsonObject) {
            for (int i = 0; i < jsonObject.getJSONArray("name").toList().size(); i++){
                names.add(jsonObject.getJSONArray("name").toList().get(i).toString());
            }
        }
    }
}
