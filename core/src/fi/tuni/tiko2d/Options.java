package fi.tuni.tiko2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.Locale;

public class Options {
    private float musicVolume;
    private float effectsVolume;
    private String language;
    private Preferences options;
    private Locale locale;

    public Options() {
        options = Gdx.app.getPreferences("options");
        readOptions();
    }

    private void readOptions() {
        musicVolume = options.getFloat("musicVolume", 1f);
        effectsVolume = options.getFloat("effectsVolume", 1f);
        language = options.getString("language", Locale.getDefault().getLanguage());
        setLocale();
    }

    private void setLocale() {
        if (language.equals("fi")) {
            locale = new Locale("fi");
        } else {
            locale = new Locale("en");
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getEffectsVolume() {
        return effectsVolume;
    }

    public String getLanguage() {
        return language;
    }

    public Locale getLocale() {
        return locale;
    }

    public void saveOptions(float effectsVolume, float musicVolume, String language) {
        options.putFloat("effectsVolume", effectsVolume);
        options.putFloat("musicVolume", musicVolume);
        options.putString("language", language);
        options.flush();
        readOptions();
    }
}
