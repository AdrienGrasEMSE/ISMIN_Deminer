// Package declaration
package deminer_graphic;


/**
 * Enum DeminerFont, created in order to select font
 * 
 * @author  AdrienG
 * @version 0.0
 */
public enum DeminerFont {

    /**
     * Main attribute
     */
    JOST_LIGHT      ("./font/Jost-Light.ttf"),
    JOST_REGULAR    ("./font/Jost-Regular.ttf"),
    JOST_SEMIBOLD   ("./font/Jost-SemiBold.ttf");
    

    /**
     * Attribute to stock the path of the font
     */
    private final String fontPath;




    /**
     * Constructor
     * 
     * @param fontPath path to the local font
     */
    DeminerFont(String fontPath) {
        this.fontPath = fontPath;
    }




    /**
     * Getter : to get the font path
     * 
     * @return font path
     */
    public String getFontPath() {
        return fontPath;
    }

}
