package mdteam.ait.tardis.data;

import java.util.HashMap;

public class ProtocolConstants {
    public static final String HAS_CLOAK = "has_cloak";
    public static final String HAS_SIEGE_MODE = "has_siege_mode";
    public static final String HAS_AUTOPILOT = "has_autopilot";
    public static final String HAS_GROUND_SEARCHING = "has_ground_searching";
    public static final String HAS_TELEPATHIC_LOCATOR = "has_telepathic_locator";
    public static final String HAS_SECURITY = "has_security";
    public static final String HAS_ANTIGRAVS = "has_antigravs";
    public static final String HAS_HAIL_MARY = "has_hail_mary";

    public static HashMap<String, Object> init() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(HAS_CLOAK, false);
        map.put(HAS_SIEGE_MODE, false);
        map.put(HAS_AUTOPILOT, false);
        map.put(HAS_GROUND_SEARCHING, false);
        map.put(HAS_TELEPATHIC_LOCATOR, false);
        map.put(HAS_SECURITY, false);
        map.put(HAS_ANTIGRAVS, false);
        map.put(HAS_HAIL_MARY, false);
        return map;
    }

}
