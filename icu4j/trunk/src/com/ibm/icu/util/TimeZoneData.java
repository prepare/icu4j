/*********************************************************************
 * Copyright (C) 2001, International Business Machines Corporation and
 * others. All Rights Reserved.
 *********************************************************************
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/util/Attic/TimeZoneData.java,v $
 * $Date: 2001/01/09 22:48:25 $
 * $Revision: 1.2 $
 */
package com.ibm.util;
import java.util.Hashtable;

/**
 * Internal class that encapsulates data about the system time zones.
 * This includes the names of all zones, their raw offsets, their
 * rules (for zones observing DST), and index tables that group
 * together zones with the same raw offset, as well as zones that are
 * equivalent (same raw offset and rules).
 * 
 * <p>This class contains two parts: A narrow API for instantiating
 * the system zone objects and for obtaining metainformation
 * (available zone lists and equivalency group lists), and blocks of
 * compressed data that take the form of static strings or string
 * arrays.  The compressed data is generated source code that cannot
 * be hand edited.  It is derived from the standard UNIX zone data at
 * ftp://elsie.nci.nih.gov/pub and is processed by the tool gentz
 * located at icu/source/tools/gentz.  When the UNIX zone data
 * changes, the gentz tool must be run to generate the new data and
 * the compressed data blocks in this file and the new blocks must be
 * pasted in.  For step-by-step instructions, see
 * icu/source/tools/gentz/readme.txt.
 *
 * @author Alan Liu
 */
class TimeZoneData {

    /**
     * Construct and return the given system TimeZone, or return null if
     * the given ID does not refer to a system TimeZone.
     */
    public static TimeZone get(String ID) {
        int i = lookup(ID);
        return (i<0) ? null : new SimpleTimeZone(ID, DATA, i);
    }

    /**
     * Return a new String array containing all system TimeZone IDs.
     * These IDs (and only these IDs) may be passed to
     * <code>get()</code> to construct the corresponding TimeZone
     * object.
     * @return an array of all system TimeZone IDs
     */
    public static String[] getAvailableIDs() {
        String[] result = new String[IDS.length];
        System.arraycopy(IDS, 0, result, 0, IDS.length);
        return result;
    }

    /**
     * Return a new String array containing all system TimeZone IDs
     * with the given raw offset from GMT.  These IDs may be passed to
     * <code>get()</code> to construct the corresponding TimeZone
     * object.
     * @param rawOffset the offset in milliseconds from GMT
     * @return an array of IDs for system TimeZones with the given
     * raw offset.  If there are none, return a zero-length array.
     */
    public static String[] getAvailableIDs(int rawOffset) {
        // Do a linear search; there are < 200 entries
        for (int i=0; i<INDEX_BY_OFFSET.length;) {
            int offset = INDEX_BY_OFFSET[i] * 1000;
            if (offset > rawOffset) {
                break;
            }
            if (offset == rawOffset) {
                // Found our desired offset
                int n = INDEX_BY_OFFSET[i+2];
                String[] result = new String[n];
                for (int j=0; j<n; ++j) {
                    result[j] = IDS[INDEX_BY_OFFSET[i+3+j]];
                }
                return result;
            }
            // Advance to the next entry
            i += INDEX_BY_OFFSET[i+2] + 3;
        }
        // Failed to find any; return empty array
        return new String[0];
    }

    /**
     * Returns the number of IDs in the equivalency group that
     * includes the given ID.  An equivalency group contains zones
     * that have the same GMT offset and rules.
     *
     * <p>The returned count includes the given ID; it is always >= 1
     * for valid IDs.  The given ID must be a system time zone.  If it
     * is not, returns zero.
     * @param ID a system time zone ID
     * @return the number of zones in the equivalency group containing
     * 'ID', or zero if 'ID' is not a valid system ID
     * @see #getEquivalentID
     */
    public static int countEquivalentIDs(String ID) {
        int i = lookup(ID);
        return (i<0) ? 0 : DATA[i + ((DATA[i+1]==0)?2:13)];
    }

    /**
     * Returns an ID in the equivalency group that
     * includes the given ID.  An equivalency group contains zones
     * that have the same GMT offset and rules.
     *
     * <p>The given index must be in the range 0..n-1, where n is the
     * value returned by <code>countEquivalentIDs(id)</code>.  For
     * some value of 'index', the returned value will be equal to the
     * given id.  If the given id is not a valid system time zone, or
     * if 'index' is out of range, then returns an empty string.
     * @param ID a system time zone ID
     * @param index a value from 0 to n-1, where n is the value
     * returned by <code>countEquivalentIDs(id)</code>
     * @return the ID of the index-th zone in the equivalency group
     * containing 'ID', or an empty string if 'ID' is not a valid
     * system ID or 'index' is out of range
     * @see #countEquivalentIDs
     */
    public static String getEquivalentID(String ID, int index) {
        int i = lookup(ID);
        if (i<0) {
            return "";
        }
        int j = i + ((DATA[i+1]==0)?2:13);
        return (index<DATA[j]) ? IDS[DATA[j+index+1]] : "";
    }

    /**
     * Given an ID, return the equivalency group index (index of
     * DATA[] for the start of the group's data) or -1 if not found.
     */
    static int lookup(String ID) {
        Integer i = (Integer) INDEX_BY_NAME.get(ID);
        return (i == null) ? -1 : i.intValue();
    }

    /**
     * Equivalency group data in int[] form.  This is represented as a
     * single flat array of ints for compactness.  Each equivalency
     * group is represented by a sequence of ints of variable length.
     * Groups are identified by a single integer, the offset to the
     * start of the group.  If i is the offset to the start of the
     * group, then the layout of data is:
     *
     * Offset  Data       Notes
     * i+0     isDST      0 for standard zones, 1 for DST
     * i+1     rawOffset  in seconds from GMT
     * <DST zones contain additional entries here; see below>
     * j+0     count      number of IDs to follow, >= 1
     * j+1..k  IDs        indices into IDS[] array
     * j+k+1   (next entry start)
     *
     * For standard zones, j = i+2.  For DST zones, j = i+13
     * and the following entries apply:
     *
     * Offset  Data       Notes
     * i+2     startMonth 0-based
     * i+3     startDowim
     * i+4     startDow  
     * i+5     startTime  in minutes
     * i+6     startMode  w/s/u encoded as 0/1/2
     * i+7     endMonth   0-based
     * i+8     endDowim
     * i+9     endDow  
     * i+10    endTime    in minutes
     * i+11    endMode    w/s/u encoded as 0/1/2
     * i+12     dstSavings in minutes
     *
     * A standard zone entry is 3+n ints long, and a DST zone entry is
     * 14+n ints long, where n is count of IDs to follow (the value at
     * j+0).
     */
    static int[] DATA;

    /**
     * Map from TimeZone ID to equivalency group offset.  This is an
     * index into DATA.  The equivalency group data starts at DATA[i]
     * and occupies the m ints that follow, where the value of m depends
     * on whether the zone is standard or DST and how many IDs are in
     * the group.
     */
    static final Hashtable INDEX_BY_NAME = new Hashtable();

    /**
     * Index of rawOffset to list of zones.  Entries are in order of 
     * ascending rawOffset.  Format:
     *
     * Offset  Data       Notes
     * i+0     rawOffset  in milliseconds from GMT
     * i+1     default    default zone for this offset
     * i+2     count      number of IDs to follow
     * i+3..j  IDs        indices into IDS
     * i+j+1   (next entry start)
     *
     * If the number of equivalency groups is n, then j=n+2.
     *
     * Equivalency group IDs are indices in DATA to the start of each
     * equivalency group.
     */
    static int[] INDEX_BY_OFFSET;

    //----------------------------------------------------------------
    // BEGIN GENERATED SOURCE CODE
    // Date: Tue Jan 09 10:51:18 PST 2001
    // Version: tzdata2000h from ftp://elsie.nci.nih.gov/pub
    // Tool: icu/source/tools/gentz
    // See: icu/source/tools/gentz/readme.txt
    // DO NOT EDIT THIS SECTION

    /**
     * Array of IDs in lexicographic order.  The INDEX_BY_OFFSET and DATA
     * arrays refer to zones using indices into this array.  To map from ID
     * to equivalency group, use the INDEX_BY_NAME Hashtable.
     * >> GENERATED DATA: DO NOT EDIT <<
     */
    static final String[] IDS = {
        "ACT",
        "AET",
        "AGT",
        "ART",
        "AST",
        "Africa/Abidjan",
        "Africa/Accra",
        "Africa/Addis_Ababa",
        "Africa/Algiers",
        "Africa/Asmera",
        "Africa/Bamako",
        "Africa/Bangui",
        "Africa/Banjul",
        "Africa/Bissau",
        "Africa/Blantyre",
        "Africa/Brazzaville",
        "Africa/Bujumbura",
        "Africa/Cairo",
        "Africa/Casablanca",
        "Africa/Ceuta",
        "Africa/Conakry",
        "Africa/Dakar",
        "Africa/Dar_es_Salaam",
        "Africa/Djibouti",
        "Africa/Douala",
        "Africa/El_Aaiun",
        "Africa/Freetown",
        "Africa/Gaborone",
        "Africa/Harare",
        "Africa/Johannesburg",
        "Africa/Kampala",
        "Africa/Khartoum",
        "Africa/Kigali",
        "Africa/Kinshasa",
        "Africa/Lagos",
        "Africa/Libreville",
        "Africa/Lome",
        "Africa/Luanda",
        "Africa/Lubumbashi",
        "Africa/Lusaka",
        "Africa/Malabo",
        "Africa/Maputo",
        "Africa/Maseru",
        "Africa/Mbabane",
        "Africa/Mogadishu",
        "Africa/Monrovia",
        "Africa/Nairobi",
        "Africa/Ndjamena",
        "Africa/Niamey",
        "Africa/Nouakchott",
        "Africa/Ouagadougou",
        "Africa/Porto-Novo",
        "Africa/Sao_Tome",
        "Africa/Timbuktu",
        "Africa/Tripoli",
        "Africa/Tunis",
        "Africa/Windhoek",
        "America/Adak",
        "America/Anchorage",
        "America/Anguilla",
        "America/Antigua",
        "America/Araguaina",
        "America/Aruba",
        "America/Asuncion",
        "America/Barbados",
        "America/Belem",
        "America/Belize",
        "America/Boa_Vista",
        "America/Bogota",
        "America/Boise",
        "America/Buenos_Aires",
        "America/Cambridge_Bay",
        "America/Cancun",
        "America/Caracas",
        "America/Catamarca",
        "America/Cayenne",
        "America/Cayman",
        "America/Chicago",
        "America/Chihuahua",
        "America/Cordoba",
        "America/Costa_Rica",
        "America/Cuiaba",
        "America/Curacao",
        "America/Dawson",
        "America/Dawson_Creek",
        "America/Denver",
        "America/Detroit",
        "America/Dominica",
        "America/Edmonton",
        "America/Eirunepe",
        "America/El_Salvador",
        "America/Fortaleza",
        "America/Glace_Bay",
        "America/Godthab",
        "America/Goose_Bay",
        "America/Grand_Turk",
        "America/Grenada",
        "America/Guadeloupe",
        "America/Guatemala",
        "America/Guayaquil",
        "America/Guyana",
        "America/Halifax",
        "America/Havana",
        "America/Hermosillo",
        "America/Indiana/Knox",
        "America/Indiana/Marengo",
        "America/Indiana/Vevay",
        "America/Indianapolis",
        "America/Inuvik",
        "America/Iqaluit",
        "America/Jamaica",
        "America/Jujuy",
        "America/Juneau",
        "America/Kentucky/Monticello",
        "America/La_Paz",
        "America/Lima",
        "America/Los_Angeles",
        "America/Louisville",
        "America/Maceio",
        "America/Managua",
        "America/Manaus",
        "America/Martinique",
        "America/Mazatlan",
        "America/Mendoza",
        "America/Menominee",
        "America/Merida",
        "America/Mexico_City",
        "America/Miquelon",
        "America/Monterrey",
        "America/Montevideo",
        "America/Montreal",
        "America/Montserrat",
        "America/Nassau",
        "America/New_York",
        "America/Nipigon",
        "America/Nome",
        "America/Noronha",
        "America/Panama",
        "America/Pangnirtung",
        "America/Paramaribo",
        "America/Phoenix",
        "America/Port-au-Prince",
        "America/Port_of_Spain",
        "America/Porto_Acre",
        "America/Porto_Velho",
        "America/Puerto_Rico",
        "America/Rainy_River",
        "America/Rankin_Inlet",
        "America/Recife",
        "America/Regina",
        "America/Rosario",
        "America/Santiago",
        "America/Santo_Domingo",
        "America/Sao_Paulo",
        "America/Scoresbysund",
        "America/St_Johns",
        "America/St_Kitts",
        "America/St_Lucia",
        "America/St_Thomas",
        "America/St_Vincent",
        "America/Swift_Current",
        "America/Tegucigalpa",
        "America/Thule",
        "America/Thunder_Bay",
        "America/Tijuana",
        "America/Tortola",
        "America/Vancouver",
        "America/Whitehorse",
        "America/Winnipeg",
        "America/Yakutat",
        "America/Yellowknife",
        "Antarctica/Casey",
        "Antarctica/Davis",
        "Antarctica/DumontDUrville",
        "Antarctica/Mawson",
        "Antarctica/McMurdo",
        "Antarctica/Palmer",
        "Antarctica/Syowa",
        "Asia/Aden",
        "Asia/Almaty",
        "Asia/Amman",
        "Asia/Anadyr",
        "Asia/Aqtau",
        "Asia/Aqtobe",
        "Asia/Ashgabat",
        "Asia/Baghdad",
        "Asia/Bahrain",
        "Asia/Baku",
        "Asia/Bangkok",
        "Asia/Beirut",
        "Asia/Bishkek",
        "Asia/Brunei",
        "Asia/Calcutta",
        "Asia/Chungking",
        "Asia/Colombo",
        "Asia/Damascus",
        "Asia/Dhaka",
        "Asia/Dili",
        "Asia/Dubai",
        "Asia/Dushanbe",
        "Asia/Gaza",
        "Asia/Harbin",
        "Asia/Hong_Kong",
        "Asia/Hovd",
        "Asia/Irkutsk",
        "Asia/Jakarta",
        "Asia/Jayapura",
        "Asia/Jerusalem",
        "Asia/Kabul",
        "Asia/Kamchatka",
        "Asia/Karachi",
        "Asia/Kashgar",
        "Asia/Katmandu",
        "Asia/Krasnoyarsk",
        "Asia/Kuala_Lumpur",
        "Asia/Kuching",
        "Asia/Kuwait",
        "Asia/Macao",
        "Asia/Magadan",
        "Asia/Manila",
        "Asia/Muscat",
        "Asia/Nicosia",
        "Asia/Novosibirsk",
        "Asia/Omsk",
        "Asia/Phnom_Penh",
        "Asia/Pyongyang",
        "Asia/Qatar",
        "Asia/Rangoon",
        "Asia/Riyadh",
        "Asia/Riyadh87",
        "Asia/Riyadh88",
        "Asia/Riyadh89",
        "Asia/Saigon",
        "Asia/Samarkand",
        "Asia/Seoul",
        "Asia/Shanghai",
        "Asia/Singapore",
        "Asia/Taipei",
        "Asia/Tashkent",
        "Asia/Tbilisi",
        "Asia/Tehran",
        "Asia/Thimphu",
        "Asia/Tokyo",
        "Asia/Ujung_Pandang",
        "Asia/Ulaanbaatar",
        "Asia/Urumqi",
        "Asia/Vientiane",
        "Asia/Vladivostok",
        "Asia/Yakutsk",
        "Asia/Yekaterinburg",
        "Asia/Yerevan",
        "Atlantic/Azores",
        "Atlantic/Bermuda",
        "Atlantic/Canary",
        "Atlantic/Cape_Verde",
        "Atlantic/Faeroe",
        "Atlantic/Jan_Mayen",
        "Atlantic/Madeira",
        "Atlantic/Reykjavik",
        "Atlantic/South_Georgia",
        "Atlantic/St_Helena",
        "Atlantic/Stanley",
        "Australia/Adelaide",
        "Australia/Brisbane",
        "Australia/Broken_Hill",
        "Australia/Darwin",
        "Australia/Hobart",
        "Australia/Lindeman",
        "Australia/Lord_Howe",
        "Australia/Melbourne",
        "Australia/Perth",
        "Australia/Sydney",
        "BET",
        "BST",
        "CAT",
        "CET",
        "CNT",
        "CST",
        "CTT",
        "EAT",
        "ECT",
        "EET",
        "EST",
        "Etc/GMT",
        "Etc/GMT+1",
        "Etc/GMT+10",
        "Etc/GMT+11",
        "Etc/GMT+12",
        "Etc/GMT+2",
        "Etc/GMT+3",
        "Etc/GMT+4",
        "Etc/GMT+5",
        "Etc/GMT+6",
        "Etc/GMT+7",
        "Etc/GMT+8",
        "Etc/GMT+9",
        "Etc/GMT-1",
        "Etc/GMT-10",
        "Etc/GMT-11",
        "Etc/GMT-12",
        "Etc/GMT-13",
        "Etc/GMT-14",
        "Etc/GMT-2",
        "Etc/GMT-3",
        "Etc/GMT-4",
        "Etc/GMT-5",
        "Etc/GMT-6",
        "Etc/GMT-7",
        "Etc/GMT-8",
        "Etc/GMT-9",
        "Etc/UCT",
        "Etc/UTC",
        "Europe/Amsterdam",
        "Europe/Andorra",
        "Europe/Athens",
        "Europe/Belfast",
        "Europe/Belgrade",
        "Europe/Berlin",
        "Europe/Brussels",
        "Europe/Bucharest",
        "Europe/Budapest",
        "Europe/Chisinau",
        "Europe/Copenhagen",
        "Europe/Dublin",
        "Europe/Gibraltar",
        "Europe/Helsinki",
        "Europe/Istanbul",
        "Europe/Kaliningrad",
        "Europe/Kiev",
        "Europe/Lisbon",
        "Europe/London",
        "Europe/Luxembourg",
        "Europe/Madrid",
        "Europe/Malta",
        "Europe/Minsk",
        "Europe/Monaco",
        "Europe/Moscow",
        "Europe/Oslo",
        "Europe/Paris",
        "Europe/Prague",
        "Europe/Riga",
        "Europe/Rome",
        "Europe/Samara",
        "Europe/Simferopol",
        "Europe/Sofia",
        "Europe/Stockholm",
        "Europe/Tallinn",
        "Europe/Tirane",
        "Europe/Tiraspol",
        "Europe/Uzhgorod",
        "Europe/Vaduz",
        "Europe/Vienna",
        "Europe/Vilnius",
        "Europe/Warsaw",
        "Europe/Zaporozhye",
        "Europe/Zurich",
        "GMT",
        "HST",
        "IET",
        "IST",
        "Indian/Antananarivo",
        "Indian/Chagos",
        "Indian/Christmas",
        "Indian/Cocos",
        "Indian/Comoro",
        "Indian/Kerguelen",
        "Indian/Mahe",
        "Indian/Maldives",
        "Indian/Mauritius",
        "Indian/Mayotte",
        "Indian/Reunion",
        "JST",
        "MET",
        "MIT",
        "MST",
        "NET",
        "NST",
        "PLT",
        "PNT",
        "PRT",
        "PST",
        "Pacific/Apia",
        "Pacific/Auckland",
        "Pacific/Chatham",
        "Pacific/Easter",
        "Pacific/Efate",
        "Pacific/Enderbury",
        "Pacific/Fakaofo",
        "Pacific/Fiji",
        "Pacific/Funafuti",
        "Pacific/Galapagos",
        "Pacific/Gambier",
        "Pacific/Guadalcanal",
        "Pacific/Guam",
        "Pacific/Honolulu",
        "Pacific/Johnston",
        "Pacific/Kiritimati",
        "Pacific/Kosrae",
        "Pacific/Kwajalein",
        "Pacific/Majuro",
        "Pacific/Marquesas",
        "Pacific/Midway",
        "Pacific/Nauru",
        "Pacific/Niue",
        "Pacific/Norfolk",
        "Pacific/Noumea",
        "Pacific/Pago_Pago",
        "Pacific/Palau",
        "Pacific/Pitcairn",
        "Pacific/Ponape",
        "Pacific/Port_Moresby",
        "Pacific/Rarotonga",
        "Pacific/Saipan",
        "Pacific/Tahiti",
        "Pacific/Tarawa",
        "Pacific/Tongatapu",
        "Pacific/Truk",
        "Pacific/Wake",
        "Pacific/Wallis",
        "Pacific/Yap",
        "SST",
        "UTC",
        "VST",
        "WET",
    };

    /**
     * RLE encoded form of DATA.
     * @see com.ibm.util.Utility.RLEStringToIntArray
     * >> GENERATED DATA: DO NOT EDIT <<
     */
    static final String DATA_RLE =
        "\000\u0698\000\000\uFFFF\u5740\000\001\000\u011F\000\000\uFFFF\u6550\000"+
        "\006\000\u011E\000\u0175\000\u017D\000\u0191\000\u0193\000\u0196\000\000"+
        "\uFFFF\u7360\000\006\000\u011D\000\u0165\000\u0183\000\u018A\000\u018B"+
        "\000\u019D\000\001\uFFFF\u7360\000\003\000\001\uFFFF\uFFFF\000x\000\000"+
        "\000\011\uFFFF\uFFFF\000\001\000x\000\000\000<\000\001\0009\000\001\uFFFF"+
        "\u7360\000\011\uFFFF\uFFFF\000\001\000\000\000\000\000\002\000\001\uFFFF"+
        "\uFFFF\000\000\000\000\000\036\000\001\000\u019B\000\000\uFFFF\u7A68\000"+
        "\001\000\u0190\000\000\uFFFF\u8170\000\002\000\u0127\000\u0187\000\001"+
        "\uFFFF\u8170\000\003\000\001\uFFFF\uFFFF\000x\000\000\000\011\uFFFF\uFFFF"+
        "\000\001\000x\000\000\000<\000\005\000\004\000:\000p\000\u0087\000\u00A9"+
        "\000\000\uFFFF\u8F80\000\002\000\u0126\000\u0198\000\001\uFFFF\u8F80\000"+
        "\003\000\001\uFFFF\uFFFF\000x\000\000\000\011\uFFFF\uFFFF\000\001\000"+
        "x\000\000\000<\000\006\000S\000t\000\u00A4\000\u00A6\000\u00A7\000\u017C"+
        "\000\000\uFFFF\u9D90\000\005\000T\000g\000\u008C\000\u0125\000\u017A\000"+
        "\001\uFFFF\u9D90\000\003\000\001\uFFFF\uFFFF\000x\000\000\000\011\uFFFF"+
        "\uFFFF\000\001\000x\000\000\000<\000\010\000E\000N\000U\000X\000l\000"+
        "z\000\u00AA\000\u0176\000\000\uFFFF\uABA0\000\005\000w\000\u0095\000\u00A0"+
        "\000\u0124\000\u0186\000\001\uFFFF\uABA0\000\003\000\001\uFFFF\uFFFF\000"+
        "x\000\000\000\011\uFFFF\uFFFF\000\001\000x\000\000\000<\000\011\000G\000"+
        "H\000M\000|\000}\000~\000\u0080\000\u0092\000\u0115\000\001\uFFFF\uABA0"+
        "\000\003\000\001\uFFFF\uFFFF\000x\000\000\000\011\uFFFF\uFFFF\000\001"+
        "\000x\000\001\000<\000\001\000\u00A8\000\001\uFFFF\uABA0\000\013\000\022"+
        "\000\000\000\000\000\000\000\001\000\014\000\000\000\000\000\000\000<"+
        "\000\001\000B\000\001\uFFFF\uABA0\000\000\000\017\uFFFF\uFFF9\000\000"+
        "\000\000\000\002\000\017\000\000\000\000\000\000\000<\000\001\000P\000"+
        "\001\uFFFF\uABA0\000\002\000\027\000\000\000\000\000\000\000\010\000\007"+
        "\000\000\000\000\000\000\000<\000\001\000b\000\001\uFFFF\uABA0\000\004"+
        "\000\001\uFFFF\uFFFF\000\000\000\000\000\010\uFFFF\uFFFF\000\001\000\000"+
        "\000\000\000<\000\002\000Z\000\u00A1\000\001\uFFFF\uABA0\000\011\000\011"+
        "\uFFFF\uFFFF\000\000\000\000\000\002\000\011\uFFFF\uFFFF\000\000\000\000"+
        "\000<\000\001\000\u0180\000\000\uFFFF\uB9B0\000\017\000L\000Y\000c\000"+
        "h\000i\000j\000k\000m\000n\000\u0089\000\u008A\000\u008F\000\u0093\000"+
        "\u0123\000\u0166\000\001\uFFFF\uB9B0\000\003\000\001\uFFFF\uFFFF\000\000"+
        "\000\000\000\011\uFFFF\uFFFF\000\001\000\000\000\000\000<\000\001\000"+
        "_\000\001\uFFFF\uB9B0\000\003\000\001\uFFFF\uFFFF\000\000\000\001\000"+
        "\011\uFFFF\uFFFF\000\001\000\000\000\001\000<\000\001\000f\000\001\uFFFF"+
        "\uB9B0\000\003\000\001\uFFFF\uFFFF\000<\000\001\000\011\uFFFF\uFFFF\000"+
        "\001\000<\000\001\000<\000\001\000\u008D\000\001\uFFFF\uB9B0\000\003\000"+
        "\001\uFFFF\uFFFF\000x\000\000\000\011\uFFFF\uFFFF\000\001\000x\000\000"+
        "\000<\000\011\000V\000q\000u\000\u0082\000\u0084\000\u0085\000\u0086\000"+
        "\u00A3\000\u011A\000\001\uFFFF\uB9B0\000\000\000\001\000\000\000\000\000"+
        "\000\000\003\000\001\000\000\000\000\000\000\000<\000\001\000s\000\001"+
        "\uFFFF\uB9B0\000\004\000\002\000\000\000\000\000\000\000\013\000\037\000"+
        "\000\000\000\000\000\000<\000\001\000D\000\000\uFFFF\uC7C0\000\031\000"+
        ";\000<\000>\000C\000I\000R\000W\000`\000a\000d\000r\000x\000y\000\u0083"+
        "\000\u008E\000\u0090\000\u0091\000\u0098\000\u009C\000\u009D\000\u009E"+
        "\000\u009F\000\u00A5\000\u0122\000\u017B\000\001\uFFFF\uC7C0\000\003\000"+
        "\001\uFFFF\uFFFF\000\001\000\000\000\011\uFFFF\uFFFF\000\001\000\001\000"+
        "\000\000<\000\001\000^\000\001\uFFFF\uC7C0\000\003\000\001\uFFFF\uFFFF"+
        "\000x\000\000\000\011\uFFFF\uFFFF\000\001\000x\000\000\000<\000\004\000"+
        "\134\000e\000\u00A2\000\u00FC\000\001\uFFFF\uC7C0\000\003\000\017\uFFFF"+
        "\uFFFF\000x\000\000\000\010\000\031\000\000\000x\000\000\000<\000\001"+
        "\000@\000\001\uFFFF\uC7C0\000\011\000\001\uFFFF\uFFFF\000\000\000\000"+
        "\000\001\uFFFF\uFFFF\000\001\000\000\000\000\000<\000\001\000?\000\001"+
        "\uFFFF\uC7C0\000\011\000\010\uFFFF\uFFFF\000\000\000\000\000\001\000\017"+
        "\uFFFF\uFFFF\000\000\000\000\000<\000\001\000Q\000\001\uFFFF\uC7C0\000"+
        "\011\000\011\uFFFF\uFFFF\000\000\000\000\000\002\000\011\uFFFF\uFFFF\000"+
        "\000\000\000\000<\000\002\000\u0097\000\u00B0\000\001\uFFFF\uC7C0\000"+
        "\010\000\010\uFFFF\uFFFF\000\000\000\000\000\003\000\020\uFFFF\uFFFF\000"+
        "\000\000\000\000<\000\001\000\u0105\000\001\uFFFF\uCEC8\000\003\000\001"+
        "\uFFFF\uFFFF\000\001\000\000\000\011\uFFFF\uFFFF\000\001\000\001\000\000"+
        "\000<\000\002\000\u009B\000\u0114\000\000\uFFFF\uD5D0\000\016\000\002"+
        "\000A\000F\000J\000K\000O\000[\000o\000v\000{\000\u008B\000\u0094\000"+
        "\u0096\000\u0121\000\001\uFFFF\uD5D0\000\003\000\001\uFFFF\uFFFF\000x"+
        "\000\000\000\011\uFFFF\uFFFF\000\001\000x\000\000\000<\000\001\000\u007F"+
        "\000\001\uFFFF\uD5D0\000\002\uFFFF\uFFFF\000\001\000<\000\002\000\011"+
        "\uFFFF\uFFFF\000\001\000<\000\002\000<\000\001\000]\000\001\uFFFF\uD5D0"+
        "\000\011\000\022\000\000\000\000\000\000\000\001\000\034\000\000\000\000"+
        "\000\000\000<\000\001\000\u0081\000\001\uFFFF\uD5D0\000\011\000\010\uFFFF"+
        "\uFFFF\000\000\000\000\000\001\000\017\uFFFF\uFFFF\000\000\000\000\000"+
        "<\000\003\000=\000\u0099\000\u0110\000\000\uFFFF\uE3E0\000\003\000\u0088"+
        "\000\u0103\000\u0120\000\000\uFFFF\uF1F0\000\003\000\u00FE\000\u0100\000"+
        "\u011C\000\001\uFFFF\uF1F0\000\002\uFFFF\uFFFF\000\001\000<\000\002\000"+
        "\011\uFFFF\uFFFF\000\001\000<\000\002\000<\000\002\000\u009A\000\u00FB"+
        "\000\000\000\000\000\025\000\005\000\012\000\014\000\015\000\022\000\024"+
        "\000\025\000\031\000$\000-\0001\0002\0004\0005\000\u0102\000\u0104\000"+
        "\u011B\000\u0136\000\u0137\000\u0164\000\u01A5\000\001\000\000\000\005"+
        "\000\001\000\000\000\000\000\000\000\010\000\001\000\000\000\000\000\000"+
        "\000<\000\001\000\032\000\001\000\000\000\002\uFFFF\uFFFF\000\001\000"+
        "<\000\002\000\011\uFFFF\uFFFF\000\001\000<\000\002\000<\000\010\000\u00FD"+
        "\000\u00FF\000\u0101\000\u013B\000\u0143\000\u0149\000\u014A\000\u01A7"+
        "\000\001\000\000\000\010\000\001\000\000\000\000\000\000\000\013\000\037"+
        "\000\000\000\000\000\000\000\024\000\001\000\006\000\000\000\u0E10\000"+
        "\015\000\010\000\013\000\017\000\030\000!\000\042\000#\000%\000(\000/"+
        "\0000\0003\000\u0128\000\001\000\u0E10\000\002\uFFFF\uFFFF\000\001\000"+
        "<\000\002\000\011\uFFFF\uFFFF\000\001\000<\000\002\000<\000\030\000\023"+
        "\000\u0118\000\u0138\000\u0139\000\u013C\000\u013D\000\u013E\000\u0140"+
        "\000\u0142\000\u0144\000\u014B\000\u014C\000\u014D\000\u014F\000\u0151"+
        "\000\u0152\000\u0153\000\u0155\000\u0159\000\u015B\000\u015E\000\u015F"+
        "\000\u0161\000\u0163\000\001\000\u0E10\000\002\uFFFF\uFFFF\000\001\000"+
        "x\000\001\000\011\uFFFF\uFFFF\000\001\000x\000\001\000<\000\002\000\u0113"+
        "\000\u0174\000\001\000\u0E10\000\004\000\001\000\000\000\000\000\001\000"+
        "\010\uFFFF\uFFFF\000\001\000\000\000\001\000<\000\001\0007\000\001\000"+
        "\u0E10\000\010\000\001\uFFFF\uFFFF\000x\000\000\000\003\000\001\uFFFF"+
        "\uFFFF\000x\000\000\000<\000\001\0008\000\000\000\u1C20\000\020\000\016"+
        "\000\020\000\033\000\034\000 \000&\000'\000)\000*\000+\0006\000\u0112"+
        "\000\u012E\000\u0154\000\u015A\000\u0160\000\001\000\u1C20\000\003\000"+
        "\001\000\000\000\000\000\000\000\011\000\001\000\000\000\000\000\000\000"+
        "<\000\001\000\u00C3\000\001\000\u1C20\000\003\000\001\000\000\000<\000"+
        "\000\000\010\000\030\000\000\000<\000\000\000<\000\001\000\u00CF\000\001"+
        "\000\u1C20\000\003\000\017\uFFFF\uFFFA\000\000\000\000\000\011\000\017"+
        "\uFFFF\uFFFA\000\000\000\000\000<\000\001\000\u00C8\000\001\000\u1C20"+
        "\000\003\uFFFF\uFFFF\000\006\000\000\000\001\000\010\uFFFF\uFFFF\000\005"+
        "\000\u0564\000\001\000<\000\002\000\003\000\021\000\001\000\u1C20\000"+
        "\002\uFFFF\uFFFF\000\001\000\000\000\000\000\011\uFFFF\uFFFF\000\001\000"+
        "\000\000\000\000<\000\001\000\u00BD\000\001\000\u1C20\000\002\uFFFF\uFFFF"+
        "\000\001\000<\000\002\000\011\uFFFF\uFFFF\000\001\000<\000\002\000<\000"+
        "\014\000\u00DD\000\u0119\000\u013A\000\u013F\000\u0141\000\u0145\000\u0146"+
        "\000\u0148\000\u0157\000\u0158\000\u015D\000\u0162\000\001\000\u1C20\000"+
        "\002\uFFFF\uFFFF\000\001\000x\000\001\000\011\uFFFF\uFFFF\000\001\000"+
        "x\000\001\000<\000\002\000\u0147\000\u014E\000\001\000\u1C20\000\002\uFFFF"+
        "\uFFFF\000\005\000\000\000\001\000\010\uFFFF\uFFFF\000\005\000\000\000"+
        "\001\000<\000\001\000\u00B4\000\001\000\u1C20\000\010\000\017\uFFFF\uFFFF"+
        "\000x\000\000\000\002\000\017\uFFFF\uFFFF\000x\000\000\000<\000\001\000"+
        "\035\000\000\000\u2A30\000\023\000\007\000\011\000\026\000\027\000\036"+
        "\000\037\000,\000.\000\u00B1\000\u00B2\000\u00BA\000\u00D8\000\u00E2\000"+
        "\u00E4\000\u0117\000\u012F\000\u0168\000\u016C\000\u0171\000\001\000\u2A30"+
        "\000\003\000\001\000\000\000\u00B4\000\001\000\011\000\001\000\000\000"+
        "\u00B4\000\001\000<\000\001\000\u00B9\000\001\000\u2A30\000\002\uFFFF"+
        "\uFFFF\000\001\000x\000\001\000\011\uFFFF\uFFFF\000\001\000x\000\001\000"+
        "<\000\002\000\u0150\000\u015C\000\000\000\u2BD8\000\003\000\u00E5\000"+
        "\u00E6\000\u00E7\000\001\000\u3138\000\002\000\024\000\000\000\000\000"+
        "\000\000\010\000\027\000\000\000\000\000\000\000<\000\001\000\u00F0\000"+
        "\000\000\u3840\000\006\000\u00C6\000\u00DC\000\u0130\000\u016E\000\u0170"+
        "\000\u0172\000\001\000\u3840\000\002\uFFFF\uFFFF\000\001\000\000\000\000"+
        "\000\011\uFFFF\uFFFF\000\001\000\000\000\000\000<\000\002\000\u00B6\000"+
        "\u00EF\000\001\000\u3840\000\002\uFFFF\uFFFF\000\001\000<\000\000\000"+
        "\011\uFFFF\uFFFF\000\001\000<\000\000\000<\000\001\000\u00BB\000\001\000"+
        "\u3840\000\002\uFFFF\uFFFF\000\001\000x\000\001\000\011\uFFFF\uFFFF\000"+
        "\001\000x\000\001\000<\000\003\000\u00FA\000\u0156\000\u0177\000\000\000"+
        "\u3F48\000\001\000\u00D0\000\000\000\u4650\000\012\000\u00B8\000\u00C7"+
        "\000\u00D2\000\u00E9\000\u00EE\000\u0131\000\u0169\000\u016D\000\u016F"+
        "\000\u0179\000\001\000\u4650\000\002\uFFFF\uFFFF\000\001\000\000\000\000"+
        "\000\011\uFFFF\uFFFF\000\001\000\000\000\000\000<\000\001\000\u00B7\000"+
        "\001\000\u4650\000\002\uFFFF\uFFFF\000\001\000x\000\001\000\011\uFFFF"+
        "\uFFFF\000\001\000x\000\001\000<\000\001\000\u00F9\000\001\000\u4650\000"+
        "\002\uFFFF\uFFFF\000\001\000\u0096\000\000\000\011\uFFFF\uFFFF\000\001"+
        "\000\u0096\000\000\000<\000\001\000\u00BE\000\000\000\u4D58\000\002\000"+
        "\u00C0\000\u0167\000\000\000\u50DC\000\001\000\u00D4\000\000\000\u5460"+
        "\000\006\000\u00AE\000\u00C2\000\u00C4\000\u00F1\000\u0111\000\u0132\000"+
        "\001\000\u5460\000\002\uFFFF\uFFFF\000\001\000\000\000\000\000\011\uFFFF"+
        "\uFFFF\000\001\000\000\000\000\000<\000\001\000\u00B3\000\001\000\u5460"+
        "\000\002\uFFFF\uFFFF\000\001\000x\000\001\000\011\uFFFF\uFFFF\000\001"+
        "\000x\000\001\000<\000\002\000\u00DE\000\u00DF\000\000\000\u5B68\000\002"+
        "\000\u00E3\000\u016B\000\000\000\u6270\000\011\000\u00AC\000\u00BC\000"+
        "\u00CD\000\u00E0\000\u00E8\000\u00F6\000\u0133\000\u016A\000\u01A6\000"+
        "\001\000\u6270\000\002\uFFFF\uFFFF\000\001\000\000\000\000\000\010\uFFFF"+
        "\uFFFF\000\001\000\000\000\000\000<\000\001\000\u00CB\000\001\000\u6270"+
        "\000\002\uFFFF\uFFFF\000\001\000x\000\001\000\011\uFFFF\uFFFF\000\001"+
        "\000x\000\001\000<\000\001\000\u00D5\000\000\000\u7080\000\010\000\u00AB"+
        "\000\u00BF\000\u00D6\000\u00D7\000\u00EC\000\u00F3\000\u010E\000\u0134"+
        "\000\001\000\u7080\000\003\000\012\uFFFF\uFFFF\000\000\000\000\000\010"+
        "\000\013\uFFFF\uFFFF\000\000\000\000\000<\000\007\000\u00C1\000\u00C9"+
        "\000\u00D3\000\u00D9\000\u00EB\000\u00F5\000\u0116\000\001\000\u7080\000"+
        "\005\000\036\000\000\000\000\000\000\000\010\000\036\000\000\000\000\000"+
        "\000\000<\000\001\000\u00ED\000\001\000\u7080\000\002\000\026\000\000"+
        "\000\000\000\000\000\010\000\025\000\000\000\000\000\000\000<\000\001"+
        "\000\u00DB\000\001\000\u7080\000\002\uFFFF\uFFFF\000\001\000\000\000\000"+
        "\000\010\uFFFF\uFFFF\000\001\000\000\000\000\000<\000\001\000\u00F4\000"+
        "\001\000\u7080\000\002\uFFFF\uFFFF\000\001\000x\000\001\000\011\uFFFF"+
        "\uFFFF\000\001\000x\000\001\000<\000\001\000\u00CC\000\001\000\u7080\000"+
        "\004\000\010\uFFFF\uFFFF\000\u00D2\000\000\000\011\000\020\uFFFF\uFFFF"+
        "\000\u00D2\000\000\000<\000\001\000\u00CA\000\000\000\u7E90\000\007\000"+
        "\u00C5\000\u00CE\000\u00E1\000\u00F2\000\u0135\000\u0173\000\u0197\000"+
        "\001\000\u7E90\000\002\uFFFF\uFFFF\000\001\000x\000\001\000\011\uFFFF"+
        "\uFFFF\000\001\000x\000\001\000<\000\001\000\u00F8\000\001\000\u7E90\000"+
        "\004\uFFFF\uFFF2\uFFFF\uFFFF\000\000\000\000\000\011\uFFFF\uFFF2\uFFFF"+
        "\uFFFF\000\000\000\000\000<\000\001\000\u00EA\000\001\000\u8598\000\011"+
        "\000\003\000\000\000x\000\000\000\002\uFFFF\uFFFF\000\001\000x\000\000"+
        "\000<\000\002\000\000\000\u0109\000\001\000\u8598\000\011\uFFFF\uFFFF"+
        "\000\001\000x\000\001\000\002\uFFFF\uFFFF\000\001\000x\000\001\000<\000"+
        "\002\000\u0106\000\u0108\000\000\000\u8CA0\000\007\000\u00AD\000\u0129"+
        "\000\u0189\000\u019A\000\u019C\000\u01A0\000\u01A3\000\001\000\u8CA0\000"+
        "\002\uFFFF\uFFFF\000\001\000x\000\001\000\011\uFFFF\uFFFF\000\001\000"+
        "x\000\001\000<\000\001\000\u00F7\000\001\000\u8CA0\000\011\uFFFF\uFFFF"+
        "\000\001\000x\000\001\000\002\uFFFF\uFFFF\000\001\000x\000\001\000<\000"+
        "\003\000\001\000\u010D\000\u010F\000\001\000\u8CA0\000\011\uFFFF\uFFFF"+
        "\000\001\000x\000\001\000\002\000\001\uFFFF\uFFFF\000x\000\001\000<\000"+
        "\002\000\u0107\000\u010B\000\001\000\u8CA0\000\011\000\001\uFFFF\uFFFF"+
        "\000x\000\001\000\002\uFFFF\uFFFF\000\001\000x\000\001\000<\000\001\000"+
        "\u010A\000\001\000\u93A8\000\011\uFFFF\uFFFF\000\001\000x\000\001\000"+
        "\002\uFFFF\uFFFF\000\001\000x\000\001\000\036\000\001\000\u010C\000\000"+
        "\000\u9AB0\000\005\000\u012A\000\u0188\000\u018D\000\u0199\000\u01A4\000"+
        "\001\000\u9AB0\000\013\000\001\000\000\000x\000\001\000\002\000\002\000"+
        "\000\000x\000\001\000<\000\001\000\u0195\000\001\000\u9AB0\000\002\uFFFF"+
        "\uFFFF\000\001\000x\000\001\000\011\uFFFF\uFFFF\000\001\000x\000\001\000"+
        "<\000\001\000\u00DA\000\001\000\u9AB0\000\011\000\027\uFFFF\uFFFF\000"+
        "\000\000\000\000\000\000\027\uFFFF\uFFFF\000\000\000\000\000<\000\001"+
        "\000\u0181\000\000\000\uA1B8\000\001\000\u0194\000\000\000\uA8C0\000\010"+
        "\000\u012B\000\u0185\000\u018E\000\u018F\000\u0192\000\u019E\000\u01A1"+
        "\000\u01A2\000\001\000\uA8C0\000\002\uFFFF\uFFFF\000\001\000x\000\001"+
        "\000\011\uFFFF\uFFFF\000\001\000x\000\001\000<\000\002\000\u00B5\000\u00D1"+
        "\000\001\000\uA8C0\000\012\000\001\uFFFF\uFFFF\000x\000\000\000\001\uFFFF"+
        "\uFFFF\000\001\000\u00B4\000\000\000<\000\001\000\u0184\000\001\000\uA8C0"+
        "\000\011\000\001\uFFFF\uFFFF\000x\000\001\000\002\000\017\uFFFF\uFFFF"+
        "\000x\000\001\000<\000\003\000\u00AF\000\u0178\000\u017E\000\001\000\uB34C"+
        "\000\011\000\001\uFFFF\uFFFF\000\u00A5\000\001\000\002\000\017\uFFFF\uFFFF"+
        "\000\u00A5\000\001\000<\000\001\000\u017F\000\000\000\uB6D0\000\002\000"+
        "\u012C\000\u0182\000\001\000\uB6D0\000\012\000\004\000\000\000x\000\001"+
        "\000\000\000\033\000\000\000x\000\001\000<\000\001\000\u019F\000\000\000"+
        "\uC4E0\000\002\000\u012D\000\u018C"
        ;

    /**
     * RLE encoded form of INDEX_BY_NAME_ARRAY.
     * @see com.ibm.util.Utility.RLEStringToIntArray
     * >> GENERATED DATA: DO NOT EDIT <<
     */
    static final String INDEX_BY_NAME_ARRAY_RLE =
        "\000\u01A8\000\u0584\000\u05BD\000\u020E\000\u0369\000=\000\u0279\000"+
        "\u02B6\000\u03D0\000\u02C5\000\u03D0\000\u0279\000\u02C5\000\u0279\000"+
        "\u0279\000\u0329\000\u02C5\000\u0329\000\u0369\000\u0279\000\u02D5\000"+
        "\u0279\000\u0279\000\u03D0\000\u03D0\000\u02C5\000\u0279\000\u0291\000"+
        "\u0329\000\u0329\000\u03C1\000\u03D0\000\u03D0\000\u0329\000\u02C5\000"+
        "\u02C5\000\u02C5\000\u0279\000\u02C5\000\u0329\000\u0329\000\u02C5\000"+
        "\u0329\000\u0329\000\u0329\000\u03D0\000\u0279\000\u03D0\000\u02C5\000"+
        "\u02C5\000\u0279\000\u0279\000\u02C5\000\u0279\000\u0279\000\u0329\000"+
        "\u030B\000\u031A\000\026\000=\000\u0175\000\u0175\000\u024C\000\u0175"+
        "\000\u01C1\000\u01B2\000\u020E\000\u00B5\000\u0175\000\u0166\000q\000"+
        "\u020E\000\u008F\000\u008F\000\u0175\000\u020E\000\u020E\000\u0101\000"+
        "\u008F\000q\000\u020E\000\u00C4\000\u01D0\000\u0175\000U\000i\000q\000"+
        "\u0140\000\u0175\000q\000\u0101\000\u00E2\000\u020E\000\u01A0\000\u022E"+
        "\000\u0191\000\u0113\000\u0175\000\u0175\000\u00D3\000\u0101\000\u0175"+
        "\000\u01A0\000\u0122\000i\000\uA5A5\000\004\000\u0101\000q\000\u0101\000"+
        "\u0101\000\u020E\000=\000\u0140\000\u0175\000\u0157\000U\000\u0140\000"+
        "\u020E\000\u0087\000\u0175\000\u0175\000q\000\u020E\000\u008F\000\u008F"+
        "\000\u008F\000\u021F\000\u008F\000\u023D\000\u0140\000\u0175\000\u0140"+
        "\000\u0140\000\u0140\000=\000\u025D\000\u0101\000\u0101\000\u020E\000"+
        "i\000\u0131\000\u0175\000\u0101\000\u0175\000\u0175\000\u008F\000\u0101"+
        "\000\u020E\000\u0087\000\u020E\000\u01DF\000\u0175\000\u024C\000\u0269"+
        "\000\u01FE\000\uA5A5\000\004\000\u0175\000\u0087\000\u00E2\000\u01A0\000"+
        "\u0140\000U\000\u0175\000U\000U\000\u00A6\000=\000q\000\u04F1\000\u04C7"+
        "\000\u05A4\000\u049A\000\u065F\000\u01DF\000\u03D0\000\u03D0\000\u04A3"+
        "\000\u03B2\000\u0640\000\u0423\000\u0464\000\u0457\000\u03E6\000\u03D0"+
        "\000\u0433\000\u04C7\000\u0379\000\u0482\000\u04F1\000\u0491\000\u04FC"+
        "\000\u049A\000\u033C\000\u049A\000\u055C\000\u041A\000\u0457\000\u035A"+
        "\000\u04FC\000\u054D\000\u04D3\000\u053E\000\u04C7\000\u055C\000\u034B"+
        "\000\u0453\000\u0640\000\u0457\000\u04FC\000\u0496\000\u04E2\000\u04F1"+
        "\000\u04F1\000\u03D0\000\u04FC\000\u0613\000\u0520\000\u041A\000\u0388"+
        "\000\u04B2\000\u04B2\000\u04C7\000\u055C\000\u03D0\000\u04C2\000\u03D0"+
        "\000\u0405\000\u0405\000\u0405\000\u04C7\000\u0457\000\u0575\000\u04FC"+
        "\000\u04F1\000\u0511\000\u0457\000\u0423\000\u040B\000\u049A\000\u055C"+
        "\000\u04F1\000\u052F\000\u04FC\000\u04C7\000\u05AE\000\u0566\000\u0473"+
        "\000\u0442\000\u0269\000\u01A0\000\u02A0\000\u0263\000\u02A0\000\u0263"+
        "\000\u02A0\000\u0279\000\u025D\000\u0279\000\u01EF\000\u0594\000\u05CE"+
        "\000\u0594\000\u0584\000\u05DE\000\u05CE\000\u05ED\000\u05BD\000\u04F1"+
        "\000\u05BD\000\u024C\000\u049A\000\u0329\000\u02FB\000\u01FE\000\u008F"+
        "\000\u04FC\000\u03D0\000\u02D5\000\u0388\000\u0140\000\u0279\000\u0263"+
        "\000\015\000\004\000\000\000\u025D\000\u020E\000\u0175\000\u0101\000\u0087"+
        "\000i\000P\0008\000\u02C5\000\u05A4\000\u05FC\000\u0635\000\u067F\000"+
        "\u0693\000\u0329\000\u03D0\000\u041A\000\u0457\000\u049A\000\u04C7\000"+
        "\u04F1\000\u055C\000\u0279\000\u0279\000\u02D5\000\u02D5\000\u0388\000"+
        "\u02A0\000\u02D5\000\u02D5\000\u02D5\000\u0388\000\u02D5\000\u0388\000"+
        "\u02D5\000\u02A0\000\u02D5\000\u0388\000\u0388\000\u03A2\000\u0388\000"+
        "\u02A0\000\u02A0\000\u02D5\000\u02D5\000\u02D5\000\u03A2\000\u02D5\000"+
        "\u03F5\000\u02D5\000\u02D5\000\u02D5\000\u0329\000\u02D5\000\u0442\000"+
        "\u0388\000\u0388\000\u02D5\000\u0329\000\u02D5\000\u03F5\000\u0388\000"+
        "\u02D5\000\u02D5\000\u0329\000\u02D5\000\u0388\000\u02D5\000\u0279\000"+
        "\015\000\u0101\000\u0491\000\u03D0\000\u0457\000\u04C7\000\u04C2\000\u03D0"+
        "\000\u0457\000\u041A\000\u0457\000\u041A\000\u03D0\000\u041A\000\u055C"+
        "\000\u02FB\000\004\000q\000\u0442\000\u065F\000\u0457\000i\000\u0175\000"+
        "U\000\004\000\u065F\000\u0670\000\u00F2\000\u0622\000\u067F\000\015\000"+
        "\u0650\000\u0635\000\u0087\0008\000\u05FC\000\u05A4\000\015\000\015\000"+
        "\u0693\000\u05FC\000\u0635\000\u0635\0004\000\004\000\u0635\000\004\000"+
        "\u0631\000\u0604\000\004\000\u055C\000P\000\u05FC\000\u05A4\000%\000\u05A4"+
        "\000\015\000\u0635\000\u0684\000\u05A4\000\u0635\000\u0635\000\u05A4\000"+
        "\u05FC\000\u0279\000\u04C7\000\u02A0"
        ;

    /**
     * RLE encoded form of INDEX_BY_OFFSET.
     * @see com.ibm.util.Utility.RLEStringToIntArray
     * >> GENERATED DATA: DO NOT EDIT <<
     */
    static final String INDEX_BY_OFFSET_RLE =
        "\000\u021D\uFFFF\u5740\000\u011F\000\001\000\u011F\uFFFF\u6550\000\u017D"+
        "\000\006\000\u011E\000\u0175\000\u017D\000\u0191\000\u0193\000\u0196\uFFFF"+
        "\u7360\000\u018A\000\010\0009\000\u011D\000\u0165\000\u0183\000\u018A"+
        "\000\u018B\000\u019B\000\u019D\uFFFF\u7A68\000\u0190\000\001\000\u0190"+
        "\uFFFF\u8170\000:\000\007\000\004\000:\000p\000\u0087\000\u00A9\000\u0127"+
        "\000\u0187\uFFFF\u8F80\000t\000\010\000S\000t\000\u00A4\000\u00A6\000"+
        "\u00A7\000\u0126\000\u017C\000\u0198\uFFFF\u9D90\000U\000\015\000E\000"+
        "N\000T\000U\000X\000g\000l\000z\000\u008C\000\u00AA\000\u0125\000\u0176"+
        "\000\u017A\uFFFF\uABA0\000M\000\025\000B\000G\000H\000M\000P\000Z\000"+
        "b\000w\000|\000}\000~\000\u0080\000\u0092\000\u0095\000\u00A0\000\u00A1"+
        "\000\u00A8\000\u0115\000\u0124\000\u0180\000\u0186\uFFFF\uB9B0\000k\000"+
        "\035\000D\000L\000V\000Y\000_\000c\000f\000h\000i\000j\000k\000m\000n"+
        "\000q\000s\000u\000\u0082\000\u0084\000\u0085\000\u0086\000\u0089\000"+
        "\u008A\000\u008D\000\u008F\000\u0093\000\u00A3\000\u011A\000\u0123\000"+
        "\u0166\uFFFF\uC7C0\000\u0091\000$\000;\000<\000>\000?\000@\000C\000I\000"+
        "Q\000R\000W\000\134\000^\000`\000a\000d\000e\000r\000x\000y\000\u0083"+
        "\000\u008E\000\u0090\000\u0091\000\u0097\000\u0098\000\u009C\000\u009D"+
        "\000\u009E\000\u009F\000\u00A2\000\u00A5\000\u00B0\000\u00FC\000\u0105"+
        "\000\u0122\000\u017B\uFFFF\uCEC8\000\u009B\000\002\000\u009B\000\u0114"+
        "\uFFFF\uD5D0\000F\000\024\000\002\000=\000A\000F\000J\000K\000O\000[\000"+
        "]\000o\000v\000{\000\u007F\000\u0081\000\u008B\000\u0094\000\u0096\000"+
        "\u0099\000\u0110\000\u0121\uFFFF\uE3E0\000\u0088\000\003\000\u0088\000"+
        "\u0103\000\u0120\uFFFF\uF1F0\000\u00FB\000\005\000\u009A\000\u00FB\000"+
        "\u00FE\000\u0100\000\u011C\000\000\000\u0164\000\037\000\005\000\006\000"+
        "\012\000\014\000\015\000\022\000\024\000\025\000\031\000\032\000$\000"+
        "-\0001\0002\0004\0005\000\u00FD\000\u00FF\000\u0101\000\u0102\000\u0104"+
        "\000\u011B\000\u0136\000\u0137\000\u013B\000\u0143\000\u0149\000\u014A"+
        "\000\u0164\000\u01A5\000\u01A7\000\u0E10\000\u0152\000)\000\010\000\013"+
        "\000\017\000\023\000\030\000!\000\042\000#\000%\000(\000/\0000\0003\000"+
        "7\0008\000\u0113\000\u0118\000\u0128\000\u0138\000\u0139\000\u013C\000"+
        "\u013D\000\u013E\000\u0140\000\u0142\000\u0144\000\u014B\000\u014C\000"+
        "\u014D\000\u014F\000\u0151\000\u0152\000\u0153\000\u0155\000\u0159\000"+
        "\u015B\000\u015E\000\u015F\000\u0161\000\u0163\000\u0174\000\u1C20\000"+
        "\021\000&\000\003\000\016\000\020\000\021\000\033\000\034\000\035\000"+
        " \000&\000'\000)\000*\000+\0006\000\u00B4\000\u00BD\000\u00C3\000\u00C8"+
        "\000\u00CF\000\u00DD\000\u0112\000\u0119\000\u012E\000\u013A\000\u013F"+
        "\000\u0141\000\u0145\000\u0146\000\u0147\000\u0148\000\u014E\000\u0154"+
        "\000\u0157\000\u0158\000\u015A\000\u015D\000\u0160\000\u0162\000\u2A30"+
        "\000\007\000\026\000\007\000\011\000\026\000\027\000\036\000\037\000,"+
        "\000.\000\u00B1\000\u00B2\000\u00B9\000\u00BA\000\u00D8\000\u00E2\000"+
        "\u00E4\000\u0117\000\u012F\000\u0150\000\u015C\000\u0168\000\u016C\000"+
        "\u0171\000\u2BD8\000\u00E7\000\003\000\u00E5\000\u00E6\000\u00E7\000\u3138"+
        "\000\u00F0\000\001\000\u00F0\000\u3840\000\u00FA\000\014\000\u00B6\000"+
        "\u00BB\000\u00C6\000\u00DC\000\u00EF\000\u00FA\000\u0130\000\u0156\000"+
        "\u016E\000\u0170\000\u0172\000\u0177\000\u3F48\000\u00D0\000\001\000\u00D0"+
        "\000\u4650\000\u00D2\000\015\000\u00B7\000\u00B8\000\u00BE\000\u00C7\000"+
        "\u00D2\000\u00E9\000\u00EE\000\u00F9\000\u0131\000\u0169\000\u016D\000"+
        "\u016F\000\u0179\000\u4D58\000\u00C0\000\002\000\u00C0\000\u0167\000\u50DC"+
        "\000\u00D4\000\001\000\u00D4\000\u5460\000\u00C4\000\011\000\u00AE\000"+
        "\u00B3\000\u00C2\000\u00C4\000\u00DE\000\u00DF\000\u00F1\000\u0111\000"+
        "\u0132\000\u5B68\000\u00E3\000\002\000\u00E3\000\u016B\000\u6270\000\u00E8"+
        "\000\013\000\u00AC\000\u00BC\000\u00CB\000\u00CD\000\u00D5\000\u00E0\000"+
        "\u00E8\000\u00F6\000\u0133\000\u016A\000\u01A6\000\u7080\000\u00EB\000"+
        "\024\000\u00AB\000\u00BF\000\u00C1\000\u00C9\000\u00CA\000\u00CC\000\u00D3"+
        "\000\u00D6\000\u00D7\000\u00D9\000\u00DB\000\u00EB\000\u00EC\000\u00ED"+
        "\000\u00F3\000\u00F4\000\u00F5\000\u010E\000\u0116\000\u0134\000\u7E90"+
        "\000\u00F2\000\011\000\u00C5\000\u00CE\000\u00E1\000\u00EA\000\u00F2\000"+
        "\u00F8\000\u0135\000\u0173\000\u0197\000\u8598\000\u0109\000\004\000\000"+
        "\000\u0106\000\u0108\000\u0109\000\u8CA0\000\u010F\000\016\000\001\000"+
        "\u00AD\000\u00F7\000\u0107\000\u010A\000\u010B\000\u010D\000\u010F\000"+
        "\u0129\000\u0189\000\u019A\000\u019C\000\u01A0\000\u01A3\000\u93A8\000"+
        "\u010C\000\001\000\u010C\000\u9AB0\000\u0188\000\010\000\u00DA\000\u012A"+
        "\000\u0181\000\u0188\000\u018D\000\u0195\000\u0199\000\u01A4\000\uA1B8"+
        "\000\u0194\000\001\000\u0194\000\uA8C0\000\u017E\000\016\000\u00AF\000"+
        "\u00B5\000\u00D1\000\u012B\000\u0178\000\u017E\000\u0184\000\u0185\000"+
        "\u018E\000\u018F\000\u0192\000\u019E\000\u01A1\000\u01A2\000\uB34C\000"+
        "\u017F\000\001\000\u017F\000\uB6D0\000\u0182\000\003\000\u012C\000\u0182"+
        "\000\u019F\000\uC4E0\000\u018C\000\002\000\u012D\000\u018C"
        ;

    // END GENERATED SOURCE CODE
    //----------------------------------------------------------------

    static {
        // Unpack the int[] DATA array
        DATA = Utility.RLEStringToIntArray(DATA_RLE);

        // Unpack the int[] INDEX_BY_OFFSET array
        INDEX_BY_OFFSET = Utility.RLEStringToIntArray(INDEX_BY_OFFSET_RLE);

        // Construct the index by name.  We unpack this array and then
        // discard it after we're done with it.
        int[] index_by_name_array = Utility.RLEStringToIntArray(INDEX_BY_NAME_ARRAY_RLE);
        for (int i=0; i<IDS.length; ++i) {
            INDEX_BY_NAME.put(IDS[i], new Integer(index_by_name_array[i]));
        }
    }
}
