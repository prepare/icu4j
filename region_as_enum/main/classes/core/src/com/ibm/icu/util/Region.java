/*
 *******************************************************************************
 * Copyright (C) 2011-2012, International Business Machines Corporation        *
 * All Rights Reserved.                                                        *
 *******************************************************************************
 */
package com.ibm.icu.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.text.NumberFormat;

/**
 * <code>Region</code> is the class representing a Unicode Region Code, also known as a 
 * Unicode Region Subtag, which is defined based upon the BCP 47 standard. We often think of
 * "regions" as "countries" when defining the characteristics of a locale.  Region codes There are different
 * types of region codes that are important to distinguish.
 * <p>
 *  Macroregion - A code for a "macro geographical (continental) region, geographical sub-region, or 
 *  selected economic and other grouping" as defined in 
 *  UN M.49 (http://unstats.un.org/unsd/methods/m49/m49regin.htm). 
 *  These are typically 3-digit codes, but contain some 2-letter codes, such as the LDML code QO 
 *  added for Outlying Oceania.  Not all UNM.49 codes are defined in LDML, but most of them are.
 *  Macroregions are represented in ICU by one of three region types: WORLD ( region code 001 ),
 *  CONTINENTS ( regions contained directly by WORLD ), and SUBCONTINENTS ( things contained directly
 *  by a continent ).
 *  <p>
 *  TERRITORY - A Region that is not a Macroregion. These are typically codes for countries, but also
 *  include areas that are not separate countries, such as the code "AQ" for Antarctica or the code 
 *  "HK" for Hong Kong (SAR China). Overseas dependencies of countries may or may not have separate 
 *  codes. The codes are typically 2-letter codes aligned with the ISO 3166 standard, but BCP47 allows
 *  for the use of 3-digit codes in the future.
 *  <p>
 *  UNKNOWN - The code ZZ is defined by Unicode LDML for use to indicate that the Region is unknown,
 *  or that the value supplied as a region was invalid.
 *  <p>
 *  DEPRECATED - Region codes that have been defined in the past but are no longer in modern usage,
 *  usually due to a country splitting into multiple territories or changing its name.
 *  <p>
 *  GROUPING - A widely understood grouping of territories that has a well defined membership such
 *  that a region code has been assigned for it.  Some of these are UNM.49 codes that do't fall into 
 *  the world/continent/sub-continent hierarchy, while others are just well known groupings that have
 *  their own region code. Region "EU" (European Union) is one such region code that is a grouping.
 *  Groupings will never be returned by the getContainingRegion() API, since a different type of region
 *  ( WORLD, CONTINENT, or SUBCONTINENT ) will always be the containing region instead.
 *  
 * @author       John Emmons
 * @draft ICU 50
 */

public enum Region {
    R001(1),
    R002(2),
    R003(3),
    R005(5),
    R009(9),
    R011(11),
    R013(13),
    R014(14),
    R015(15),
    R017(17),
    R018(18),
    R019(19),
    R021(21),
    R029(29),
    R030(30),
    R034(34),
    R035(35),
    R039(39),
    R053(53),
    R054(54),
    R057(57),
    R061(61),
    R142(142),
    R143(143),
    R145(145),
    R150(150),
    R151(151),
    R154(154),
    R155(155),
    R419(419),
    AC(-1),
    AD(20),
    AE(784),
    AF(4),
    AG(28),
    AI(660),
    AL(8),
    AM(51),
    AN(530),
    AO(24),
    AQ(10),
    AR(32),
    AS(16),
    AT(40),
    AU(36),
    AW(533),
    AX(248),
    AZ(31),
    BA(70),
    BB(52),
    BD(50),
    BE(56),
    BF(854),
    BG(100),
    BH(48),
    BI(108),
    BJ(204),
    BL(652),
    BM(60),
    BN(96),
    BO(68),
    BQ(535),
    BR(76),
    BS(44),
    BT(64),
    BU(104),
    BV(74),
    BW(72),
    BY(112),
    BZ(84),
    CA(124),
    CC(166),
    CD(180),
    CF(140),
    CG(178),
    CH(756),
    CI(384),
    CK(184),
    CL(152),
    CM(120),
    CN(156),
    CO(170),
    CP(-1),
    CR(188),
    CU(192),
    CV(132),
    CW(531),
    CX(162),
    CY(196),
    CZ(203),
    DE(276),
    DG(-1),
    DJ(262),
    DK(208),
    DM(212),
    DO(214),
    DZ(12),
    EA(-1),
    EC(218),
    EE(233),
    EG(818),
    EH(732),
    ER(232),
    ES(724),
    ET(231),
    EU(967),
    FI(246),
    FJ(242),
    FK(238),
    FM(583),
    FO(234),
    FR(250),
    FX(249),
    GA(266),
    GB(826),
    GD(308),
    GE(268),
    GF(254),
    GG(831),
    GH(288),
    GI(292),
    GL(304),
    GM(270),
    GN(324),
    GP(312),
    GQ(226),
    GR(300),
    GS(239),
    GT(320),
    GU(316),
    GW(624),
    GY(328),
    HK(344),
    HM(334),
    HN(340),
    HR(191),
    HT(332),
    HU(348),
    IC(-1),
    ID(360),
    IE(372),
    IL(376),
    IM(833),
    IN(356),
    IO(86),
    IQ(368),
    IR(364),
    IS(352),
    IT(380),
    JE(832),
    JM(388),
    JO(400),
    JP(392),
    KE(404),
    KG(417),
    KH(116),
    KI(296),
    KM(174),
    KN(659),
    KP(408),
    KR(410),
    KW(414),
    KY(136),
    KZ(398),
    LA(418),
    LB(422),
    LC(662),
    LI(438),
    LK(144),
    LR(430),
    LS(426),
    LT(440),
    LU(442),
    LV(428),
    LY(434),
    MA(504),
    MC(492),
    MD(498),
    ME(499),
    MF(663),
    MG(450),
    MH(584),
    MK(807),
    ML(466),
    MM(104),
    MN(496),
    MO(446),
    MP(580),
    MQ(474),
    MR(478),
    MS(500),
    MT(470),
    MU(480),
    MV(462),
    MW(454),
    MX(484),
    MY(458),
    MZ(508),
    NA(516),
    NC(540),
    NE(562),
    NF(574),
    NG(566),
    NI(558),
    NL(528),
    NO(578),
    NP(524),
    NR(520),
    NT(536),
    NU(570),
    NZ(554),
    OM(512),
    PA(591),
    PE(604),
    PF(258),
    PG(598),
    PH(608),
    PK(586),
    PL(616),
    PM(666),
    PN(612),
    PR(630),
    PS(275),
    PT(620),
    PW(585),
    PY(600),
    QA(634),
    QO(961),
    RE(638),
    RO(642),
    RS(688),
    RU(643),
    RW(646),
    SA(682),
    SB(90),
    SC(690),
    SD(729),
    SE(752),
    SG(702),
    SH(654),
    SI(705),
    SJ(744),
    SK(703),
    SL(694),
    SM(674),
    SN(686),
    SO(706),
    SR(740),
    SS(728),
    ST(678),
    SU(810),
    SV(222),
    SX(534),
    SY(760),
    SZ(748),
    TA(-1),
    TC(796),
    TD(148),
    TF(260),
    TG(768),
    TH(764),
    TJ(762),
    TK(772),
    TL(626),
    TM(795),
    TN(788),
    TO(776),
    TP(626),
    TR(792),
    TT(780),
    TV(798),
    TW(158),
    TZ(834),
    UA(804),
    UG(800),
    UM(581),
    US(840),
    UY(858),
    UZ(860),
    VA(336),
    VC(670),
    VE(862),
    VG(92),
    VI(850),
    VN(704),
    VU(548),
    WF(876),
    WS(882),
    YD(720),
    YE(887),
    YT(175),
    YU(891),
    ZA(710),
    ZM(894),
    ZR(180),
    ZW(716),
    ZZ(999);
    
    /**
     * RegionType is an enumeration defining the different types of regions.  Current possible
     * values are WORLD, CONTINENT, SUBCONTINENT, TERRITORY, GROUPING, DEPRECATED, and UNKNOWN.
     * 
     * @draft ICU 50
     */

    public enum RegionType {
        /**
         * Type representing the unknown region.
         * @draft ICU 50
         */
        UNKNOWN,

        /**
         * Type representing a territory.
         * @draft ICU 50
         */
        TERRITORY,

        /**
         * Type representing the whole world.
         * @draft ICU 50
         */
        WORLD,
        /**
         * Type representing a continent.
         * @draft ICU 50
         */
        CONTINENT,
        /**
         * Type representing a sub-continent.
         * @draft ICU 50
         */
        SUBCONTINENT,
        /**
         * Type representing a grouping of territories that is not to be used in
         * the normal WORLD/CONTINENT/SUBCONTINENT/TERRITORY containment tree.
         * @draft ICU 50
         */
        GROUPING,
        /**
         * Type representing a region whose code has been deprecated, usually
         * due to a country splitting into multiple territories or changing its name.
         * @draft ICU 50
         */
        DEPRECATED,
    }


    private String id;
    private int code;
    private RegionType type;
    
    private static boolean hasData = false;
    private static boolean hasContainmentData = false;
    
    private static Set<String> knownRegions = null;             // Set of known regions ( according to ICU data )
    private static Map<Integer,Integer> numericIndexMap = null; // Map from numeric code to position in the enum
    private static Map<String,String> territoryAliasMap = null; // Aliases
    private static Map<String,Integer> numericCodeMap = null;   // Map of all possible IDs to numeric codes

    private static EnumMap<Region,EnumSet<Region>> subRegionData = new EnumMap<Region,EnumSet<Region>>(Region.class);
    private static EnumMap<Region,Region> containingRegionData = new EnumMap<Region,Region>(Region.class);
    private static ArrayList<EnumSet<Region>> availableRegions = null;

    /**
     * A constant used for the top level (WORLD) region ID.
     * @draft ICU 50
     */    
    private static final String WORLD_ID = "001";

    /**
     * A constant used for the UNKNOWN region ID.
     * @draft ICU 50
     */    
    private static final String UNKNOWN_REGION_ID = "ZZ";
    /**
     * A constant used for a region with an unassigned region code.
     * @see #getNumericCode()
     * @draft ICU 50
     */
    private static final int UNDEFINED_NUMERIC_CODE = -1;
    
    /*
     * Private constructor.  Use factory methods only.
     */
    private Region (int code) {
        this.code = code;
    }
    
    /*
     * Initializes the region data from the ICU resource bundles.  The region data
     * contains the basic relationships such as which regions are known, what the numeric
     * codes are, and any known aliases.  It does not contain the territory containment data.
     * Territory containment data only gets loaded if someone calls an API that is actually
     * going to use that data.
     * 
     * If the region data has already loaded, then this method simply returns without doing
     * anything meaningful.
     * 
     */
    private static synchronized void initRegionData() {
        
        if ( hasData ) {
            return;
        }
        
        territoryAliasMap = new HashMap<String,String>();
        numericCodeMap = new HashMap<String,Integer>();
        knownRegions = new HashSet<String>();
        numericIndexMap = new HashMap<Integer,Integer>();
        availableRegions = new ArrayList<EnumSet<Region>>(RegionType.values().length);
        
        for ( int i = 0 ; i < RegionType.values().length ; i++) {
            availableRegions.add(EnumSet.noneOf(Region.class));
        }
        UResourceBundle territoryAlias = null;
        UResourceBundle codeMappings = null;
        UResourceBundle worldContainment = null;
        UResourceBundle territoryContainment = null;
        UResourceBundle groupingContainment = null;
        UResourceBundle rb = UResourceBundle.getBundleInstance(
                                    ICUResourceBundle.ICU_BASE_NAME,
                                    "metadata",
                                    ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        territoryAlias = rb.get("territoryAlias");

        UResourceBundle rb2 = UResourceBundle.getBundleInstance(
                    ICUResourceBundle.ICU_BASE_NAME,
                    "supplementalData",
                    ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        codeMappings = rb2.get("codeMappings");

        // Right now only fetch as much territory containment as we need in order to determine
        // types.  Only fetch the rest if we have to.
        //
        territoryContainment = rb2.get("territoryContainment");
        worldContainment = territoryContainment.get("001");
        groupingContainment = territoryContainment.get("grouping");
        
        String[] continentsArr = worldContainment.getStringArray();
        List<String> continents = Arrays.asList(continentsArr);
        String[] groupingArr = groupingContainment.getStringArray();
        List<String> groupings = Arrays.asList(groupingArr);

        // First put alias mappings for iso3 and numeric code mappings
        for ( int i = 0 ; i < codeMappings.getSize(); i++ ) {
            UResourceBundle mapping = codeMappings.get(i);
            if ( mapping.getType() == UResourceBundle.ARRAY ) {
                String [] codeStrings = mapping.getStringArray();
                if ( !territoryAliasMap.containsKey(codeStrings[1])) {
                    territoryAliasMap.put(codeStrings[1],codeStrings[0]); // Put alias from the numeric to the iso2 code
                }
                territoryAliasMap.put(codeStrings[2],codeStrings[0]); // Put alias from the iso3 to the iso2 code.
                numericCodeMap.put(codeStrings[0], Integer.valueOf(codeStrings[1])); // Create the mapping from the iso2 code to its numeric value
            }
        }

        for ( int i = 0 ; i < territoryAlias.getSize(); i++ ) {
            UResourceBundle res = territoryAlias.get(i);
            String key = res.getKey();
            String value = res.getString();
            if ( !territoryAliasMap.containsKey(key)) {
                territoryAliasMap.put(key, value);
            }
        }

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(3);
        for ( Region r : Region.values() ) {
            String id = r.name();
            if ( id.length() > 2 ) {
                id = nf.format(r.code);
            }
            r.id = id;
            knownRegions.add(r.id);

            if ( id.matches("[0-9]{3}")) {
                numericIndexMap.put(r.code, Integer.valueOf(r.ordinal()));
            } else if (numericCodeMap.containsKey(id)) {
                if ( !numericIndexMap.containsKey(r.code)) {
                    numericIndexMap.put(r.code, Integer.valueOf(r.ordinal()));
                }
            } else {
                r.code = UNDEFINED_NUMERIC_CODE;
            }

            if ( territoryAliasMap.containsKey(id)){
                r.type = RegionType.DEPRECATED;
            } else if ( id.equals(WORLD_ID) ) {
                r.type = RegionType.WORLD;
            } else if ( id.equals(UNKNOWN_REGION_ID) ) {
                r.type = RegionType.UNKNOWN;
            } else if ( continents.contains(id) ) {
                r.type = RegionType.CONTINENT;
            } else if ( groupings.contains(id) ) {
                r.type = RegionType.GROUPING;
            } else if ( id.matches("[0-9]{3}|QO") ) {
                r.type = RegionType.SUBCONTINENT;
            } else {
                r.type = RegionType.TERRITORY;
            }
            
            EnumSet<Region> av = availableRegions.get(r.type.ordinal());
            av.add(r);
            availableRegions.set(r.type.ordinal(), av);
        }
        
        hasData = true;
    }

    /*
     * Initializes the containment data from the ICU resource bundles.  The containment data
     * defines the relationships between different regions, such as which regions are contained
     * within other regions.
     * 
     * Territory containment data only gets loaded if someone calls an API that is actually
     * going to use that data.  Since you have to have the basic region data as well, this
     * method will attempt to load the basic region data if it hasn't been loaded already.
     * 
     * If the containment data has already loaded, then this method simply returns without doing
     * anything meaningful.
     * 
     */

    private static synchronized void initContainmentData() {
        if ( hasContainmentData ) {
            return;
        }
        
        initRegionData();

        // Initialize sub-region data with empty EnumSets across the board.

        EnumSet<Region> noRegions = EnumSet.noneOf(Region.class);
        for ( Region r : Region.values() ) {
            subRegionData.put(r,noRegions.clone());
            containingRegionData.put(r,Region.ZZ);
        }

        UResourceBundle territoryContainment = null;

        UResourceBundle rb = UResourceBundle.getBundleInstance(
                    ICUResourceBundle.ICU_BASE_NAME,
                    "supplementalData",
                    ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        territoryContainment = rb.get("territoryContainment");

        
        // Get territory containment info from the supplemental data.
        for ( int i = 0 ; i < territoryContainment.getSize(); i++ ) {
            UResourceBundle mapping = territoryContainment.get(i);
            String parent = mapping.getKey();
            Region parentRegion = null;
            try {
                parentRegion = Region.getInstance(parent);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            for ( int j = 0 ; j < mapping.getSize(); j++ ) {
                String child = mapping.getString(j);
                Region childRegion = Region.getInstance(child);
                if ( parentRegion != null && childRegion != null ) {                    
                    EnumSet<Region> containedRegions = subRegionData.get(parentRegion);
                    containedRegions.add(childRegion);
                    subRegionData.put(parentRegion, containedRegions);
                    // Regions of type GROUPING can't be set as the parent, since another region
                    // such as a SUBCONTINENT, CONTINENT, or WORLD must always be the parent.
                    if ( parentRegion.getType() != RegionType.GROUPING) {
                        containingRegionData.put(childRegion,parentRegion);
                    }
                }
            }
        }
        hasContainmentData = true;
    }
    
    
    /** Returns a Region using the given region ID.  The region ID can be either a 2-letter ISO code,
     * 3-letter ISO code,  UNM.49 numeric code, or other valid Unicode Region Code as defined by the CLDR.
     * @param id The id of the region to be retrieved.
     * @return The corresponding region.
     * @throws NullPointerException if the supplied id is null.
     * @throws IllegalArgumentException if the supplied ID is not known by ICU.
     * @draft ICU 50
     */
    
    public static Region getInstance(String id) {
        initRegionData();
        if ( id == null ) {
            throw new NullPointerException();
        }
        String canonicalID = null;

        String preferredID = territoryAliasMap.get(id);
        if ( preferredID != null && knownRegions.contains(preferredID)) {
            canonicalID = preferredID;
        } else if ( knownRegions.contains(id)) {
            canonicalID = id;
            if (canonicalID.length() > 2) {
                canonicalID = "R" + canonicalID; // Add the leading R - like R001 for world
            }
        }
 
        if (canonicalID == null) {
            throw new IllegalArgumentException("Unknown region id: " + id);
        }
        
        return Region.valueOf(canonicalID);
    }
    
    
    /** Returns a Region using the given numeric code as defined by UNM.49
     * @param code The numeric code of the region to be retrieved.
     * @return The corresponding region.
     * @throws IllegalArgumentException if the supplied numeric code is not recognized.
     * @draft ICU 50
     */

    public static Region getInstance(int code) {
        initRegionData();
        Integer index = numericIndexMap.get(Integer.valueOf(code));
        if ( index != null ) {
            Region r = Region.values()[index];
            // Since a deprecated region will have the same numeric code as its new region code
            // we get by id which will make sure we get the canonicalized one.
            return Region.getInstance(r.id);
        } else {
            throw new IllegalArgumentException("Unknown region code: " + code);
        }
    }
   
    
    /** Used to retrieve all available regions of a specific type.
     * 
     * @param type The type of regions to be returned ( TERRITORY, MACROREGION, etc. )
     * @return An unmodifiable set of all known regions that match the given type.
     * @draft ICU 50
     */

    public static Set<Region> getAvailable(RegionType type) {
        initRegionData();
        return availableRegions.get(type.ordinal());
    }

    
    /** Used to determine the macroregion that geographically contains this region.
     * 
     * @return The region that geographically contains this region.  Returns NULL if this region is
     *  code "001" (World) or "ZZ" (Unknown region).  For example, calling this method with region "IT" (Italy)
     *  returns the region "039" (Southern Europe).    
     * @draft ICU 50
     */

    public Region getContainingRegion() {
        initContainmentData();
        Region result = containingRegionData.get(this);
        if ( result == Region.ZZ ) {
            return null;
        } else {
            return result;
        }
    }

    /** Used to determine the sub-regions that are contained within this region.
     * 
     * @return An unmodifiable set containing all the regions that are immediate children
     * of this region in the region hierarchy.  These returned regions could be either macro
     * regions, territories, or a mixture of the two, depending on the containment data as defined
     * in CLDR.  This API may return an empty set if this region doesn't have any sub-regions.
     * For example, calling this method with region "150" (Europe) returns a set containing
     * the various sub regions of Europe - "039" (Southern Europe) - "151" (Eastern Europe) 
     * - "154" (Northern Europe) and "155" (Western Europe).
     *
     * @draft ICU 50
     */

    public Set<Region> getSubRegions() {
        initContainmentData();        
        return subRegionData.get(this);
    }
    
    /** Used to determine all the territories that are contained within this region.
     * 
     * @return An unmodifiable set containing all the territories that are children of this
     *  region anywhere in the region hierarchy.  If this region is already a territory,
     *  the empty set is returned, since territories by definition do not contain other regions.
     *  For example, calling this method with region "150" (Europe) returns a set containing all
     *  the territories in Europe ( "FR" (France) - "IT" (Italy) - "DE" (Germany) etc. )
     *
     * @draft ICU 50
     */
    
    public Set<Region> getContainedTerritories() {
        initContainmentData();
        Set<Region> result = new TreeSet<Region>();
        Set<Region> subRegions = getSubRegions();
        Iterator<Region> it = subRegions.iterator();
        while ( it.hasNext() ) {
            Region r = it.next();
            if ( r.getType() == RegionType.TERRITORY ) {
                result.add(r);
            } else if ( r.getType() == RegionType.CONTINENT || r.getType() == RegionType.SUBCONTINENT) {
                result.addAll(r.getContainedTerritories()); // Recursion!!!
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /** Returns the string representation of this region
     * 
     * @return The string representation of this region, which is its canonical ID.
     *
     * @draft ICU 50
     */
 
    public String toString() {
        return id;
    }
    
    /** Returns the numeric code for this region
     * 
     * @return The numeric code for this region.   Returns UNDEFINED_NUMERIC_CODE (-1) if the
     * given region does not have a numeric code assigned to it.  This is a very rare case and
     * only occurs for a few very small territories.
     *
     * @draft ICU 50
     */
   
    public int getNumericCode() {
        return code;
    }
    
    /** Returns this region's type.
     * 
     * @return This region's type classification, such as MACROREGION or TERRITORY.
     *
     * @draft ICU 50
     */
  
    public RegionType getType() {
        return type;
    }
    
    /** Checks to see if this region is of a specific type.
     * 
     * @return Returns TRUE if this region matches the supplied type.
     *
     * @draft ICU 50
     */
  
 //   public boolean isOfType(RegionType type) {
 //       return this.type.equals(type);
 //   }


}
