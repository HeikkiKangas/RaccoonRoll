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

    /**
     * Initializes the options from saved Preferences and reads the options to variables
     */
    public Options() {
        options = Gdx.app.getPreferences("options");
        readOptions();
    }

    /**
     * Reads the options variables from the Preferences and sets the Locale variable
     */
    private void readOptions() {
        musicVolume = options.getFloat("musicVolume", 1f);
        effectsVolume = options.getFloat("effectsVolume", 1f);
        language = options.getString("language", Locale.getDefault().getLanguage());
        setLocale();
    }

    /**
     * Sets to locale to English if selected language is not Finnish
     */
    private void setLocale() {
        if (language.equals("fi")) {
            locale = new Locale("fi");
        } else {
            locale = new Locale("en");
        }
    }

    /**
     * Getter for the music volume
     *
     * @return the music volume
     */
    public float getMusicVolume() {
        return musicVolume;
    }

    /**
     * Getter for the effects volume
     * @return the effects volume
     */
    public float getEffectsVolume() {
        return effectsVolume;
    }

    /**
     * Getter for the selected language
     * @return the selected language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Getter for the locale to be used with bundles
     * @return the locale to be used with bundles
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Saves the options to the options Preferences file
     * @param effectsVolume volume of sound effects
     * @param musicVolume volume of the background music
     * @param language the language to use
     */
    public void saveOptions(float effectsVolume, float musicVolume, String language) {
        options.putFloat("effectsVolume", effectsVolume);
        options.putFloat("musicVolume", musicVolume);
        options.putString("language", language);
        options.flush();
        readOptions();
    }
}
