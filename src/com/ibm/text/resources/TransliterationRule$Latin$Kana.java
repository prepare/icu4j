package com.ibm.text.resources;

import java.util.ListResourceBundle;

/**
 * Rewritten April 1999 to implement Hepburn (kebon shiki)
 * transliteration.  Reference: CJKV Information Processing, Lunde,
 * 1999, pp. 30-35.
 * @author Alan Liu
 */
public class TransliterationRule$Latin$Kana extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    public Object[][] getContents() {
        return new Object[][] {
            { "Description",
                "Lowercase Latin to Hiragana; Uppercase Latin to Katakana" },

            {   "Rule",

                //------------------------------------------------------------
                // Variables
                //------------------------------------------------------------

                // Hiragana.  These are named according to the
                // regularized Nippon romanization (the naming system
                // used by Unicode).  Thus \u3062 is called "di", not
                // "ji".  "x_" is the small form of "_", e.g. "xa" is
                // small "a".

                "xa=\u3041\n"
                + "a=\u3042\n"
                + "xi=\u3043\n"
                + "i=\u3044\n"
                + "xu=\u3045\n"
                + "u=\u3046\n"
                + "xe=\u3047\n"
                + "e=\u3048\n"
                + "xo=\u3049\n"
                + "o=\u304A\n"

                + "ka=\u304B\n"
                + "ga=\u304C\n"
                + "ki=\u304D\n"
                + "gi=\u304E\n"
                + "ku=\u304F\n"
                + "gu=\u3050\n"
                + "ke=\u3051\n"
                + "ge=\u3052\n"
                + "ko=\u3053\n"
                + "go=\u3054\n"

                + "sa=\u3055\n"
                + "za=\u3056\n"
                + "si=\u3057\n"
                + "zi=\u3058\n"
                + "su=\u3059\n"
                + "zu=\u305A\n"
                + "se=\u305B\n"
                + "ze=\u305C\n"
                + "so=\u305D\n"
                + "zo=\u305E\n"

                + "ta=\u305F\n"
                + "da=\u3060\n"
                + "ti=\u3061\n"
                + "di=\u3062\n"
                + "xtu=\u3063\n"
                + "tu=\u3064\n"
                + "du=\u3065\n"
                + "te=\u3066\n"
                + "de=\u3067\n"
                + "to=\u3068\n"
                + "do=\u3069\n"

                + "na=\u306A\n"
                + "ni=\u306B\n"
                + "nu=\u306C\n"
                + "ne=\u306D\n"
                + "no=\u306E\n"

                + "ha=\u306F\n"
                + "ba=\u3070\n"
                + "pa=\u3071\n"
                + "hi=\u3072\n"
                + "bi=\u3073\n"
                + "pi=\u3074\n"
                + "hu=\u3075\n"
                + "bu=\u3076\n"
                + "pu=\u3077\n"
                + "he=\u3078\n"
                + "be=\u3079\n"
                + "pe=\u307A\n"
                + "ho=\u307B\n"
                + "bo=\u307C\n"
                + "po=\u307D\n"

                + "ma=\u307E\n"
                + "mi=\u307F\n"
                + "mu=\u3080\n"
                + "me=\u3081\n"
                + "mo=\u3082\n"

                + "xya=\u3083\n"
                + "ya=\u3084\n"
                + "xyu=\u3085\n"
                + "yu=\u3086\n"
                + "xyo=\u3087\n"
                + "yo=\u3088\n"

                + "ra=\u3089\n"
                + "ri=\u308A\n"
                + "ru=\u308B\n"
                + "re=\u308C\n"
                + "ro=\u308D\n"

                + "xwa=\u308E\n"
                + "wa=\u308F\n"
                + "wi=\u3090\n"
                + "we=\u3091\n"
                + "wo=\u3092\n"

                + "n=\u3093\n"
                + "vu=\u3094\n"

                // Katakana.  "X_" is the small form of "_", e.g. "XA"
                // is small "A".

                + "XA=\u30A1\n"
                + "A=\u30A2\n"
                + "XI=\u30A3\n"
                + "I=\u30A4\n"
                + "XU=\u30A5\n"
                + "U=\u30A6\n"
                + "XE=\u30A7\n"
                + "E=\u30A8\n"
                + "XO=\u30A9\n"
                + "O=\u30AA\n"

                + "KA=\u30AB\n"
                + "GA=\u30AC\n"
                + "KI=\u30AD\n"
                + "GI=\u30AE\n"
                + "KU=\u30AF\n"
                + "GU=\u30B0\n"
                + "KE=\u30B1\n"
                + "GE=\u30B2\n"
                + "KO=\u30B3\n"
                + "GO=\u30B4\n"

                + "SA=\u30B5\n"
                + "ZA=\u30B6\n"
                + "SI=\u30B7\n"
                + "ZI=\u30B8\n"
                + "SU=\u30B9\n"
                + "ZU=\u30BA\n"
                + "SE=\u30BB\n"
                + "ZE=\u30BC\n"
                + "SO=\u30BD\n"
                + "ZO=\u30BE\n"

                + "TA=\u30BF\n"
                + "DA=\u30C0\n"
                + "TI=\u30C1\n"
                + "DI=\u30C2\n"
                + "XTU=\u30C3\n"
                + "TU=\u30C4\n"
                + "DU=\u30C5\n"
                + "TE=\u30C6\n"
                + "DE=\u30C7\n"
                + "TO=\u30C8\n"
                + "DO=\u30C9\n"

                + "NA=\u30CA\n"
                + "NI=\u30CB\n"
                + "NU=\u30CC\n"
                + "NE=\u30CD\n"
                + "NO=\u30CE\n"

                + "HA=\u30CF\n"
                + "BA=\u30D0\n"
                + "PA=\u30D1\n"
                + "HI=\u30D2\n"
                + "BI=\u30D3\n"
                + "PI=\u30D4\n"
                + "HU=\u30D5\n"
                + "BU=\u30D6\n"
                + "PU=\u30D7\n"
                + "HE=\u30D8\n"
                + "BE=\u30D9\n"
                + "PE=\u30DA\n"
                + "HO=\u30DB\n"
                + "BO=\u30DC\n"
                + "PO=\u30DD\n"

                + "MA=\u30DE\n"
                + "MI=\u30DF\n"
                + "MU=\u30E0\n"
                + "ME=\u30E1\n"
                + "MO=\u30E2\n"

                + "XYA=\u30E3\n"
                + "YA=\u30E4\n"
                + "XYU=\u30E5\n"
                + "YU=\u30E6\n"
                + "XYO=\u30E7\n"
                + "YO=\u30E8\n"

                + "RA=\u30E9\n"
                + "RI=\u30EA\n"
                + "RU=\u30EB\n"
                + "RE=\u30EC\n"
                + "RO=\u30ED\n"

                + "XWA=\u30EE\n"
                + "WA=\u30EF\n"
                + "WI=\u30F0\n"
                + "WE=\u30F1\n"
                + "WO=\u30F2\n"

                + "N=\u30F3\n"
                + "VU=\u30F4\n"

                + "XKA=\u30F5\n"
                + "XKE=\u30F6\n"

                + "VA=\u30F7\n"
                + "VI=\u30F8\n"
                + "VE=\u30F9\n"
                + "VO=\u30FA\n"

                + "DOT=\u30FB\n"  // Middle dot
                + "LONG=\u30FC\n" // Prolonged sound mark
 
                // Categories and programmatic variables
                
                + "vowel=[aiueo]\n"
                + "small=\uE000\n"
                + "hvr=\uE001\n"
                + "hv=[{xya}{xi}{xyu}{xe}{xyo}]\n"

                //------------------------------------------------------------
                // Rules
                //------------------------------------------------------------
                /*
// Hepburn equivalents

shi>|si
ji>|zi
chi>|ti
// ji>|di // By default we use the ji-zi mapping
tsu>|tu
fu>|hu

sh[{vowel}>|sy
ja>|zya
// ji = zi
ju>|zyu
je>|zye
jo>|zyo
cha>|tya
// chi = ti
chu>|tyu
che>|tye
cho>|tyo
// j[{vowel} = dy{vowel}, but we use zy{vowel} by default

// Historically, m preceded b, p, or m; now n is used
// in all cases
m[b>n
m[p>n
m[m>n

// Compatibility

// 'f' group
fa>{fu}{xa}
fi>{fu}{xi}
// fu = hu
fe>{fu}{xe}
fo>{fu}{xo}

// 'jy' group; these will not round-trip, except for "jyi"
// See also the 'j' group.
jya>|zya
jyi>{zi}{xyi}
jyu>|zyu
jye>|zye
jyo>|zyo

// Nippon romanized forms

a>{a}
i>{i}
u>{u}
e>{e}
o>{o}
ka>{ka}
ki>{ki}
ku>{ku}
ke>{ke}
ko>{ko}
ga>{ga}
gi>{gi}
gu>{gu}
ge>{ge}
go>{go}
sa>{sa}
si>{si}
su>{su}
se>{se}
so>{so}
za>{za}
zi>{zi}
zu>{zu}
ze>{ze}
zo>{zo}
ta>{ta}
ti>{ti}
tu>{tu}
te>{te}
to>{to}
da>{da}
di>{di}
du>{du}
de>{de}
do>{do}
na>{na}
ni>{ni}
nu>{nu}
ne>{ne}
no>{no}
ha>{ha}
hi>{hi}
hu>{hu}
he>{he}
ho>{ho}
ba>{ba}
bi>{bi}
bu>{bu}
be>{be}
bo>{bo}
pa>{pa}
pi>{pi}
pu>{pu}
pe>{pe}
po>{po}
ma>{ma}
mi>{mi}
mu>{mu}
me>{me}
mo>{mo}
ya>{ya}
yu>{yu}
yo>{yo}
ra>{ra}
ri>{ri}
ru>{ru}
re>{re}
ro>{ro}
wa>{wa}
wi>{wi}
// No "wu"
we>{we}
wo>{wo} // Reverse {wo} to "o", not "wo"
n''>{n}
n>{n}

// Palatized Nippon romanized syllables

ky[{vowel}>{ki}|{small}
gy[{vowel}>{gi}|{small}
sy[{vowel}>{si}|{small}
zy[{vowel}>{zi}|{small}
ty[{vowel}>{ti}|{small}
dy[{vowel}>{di}|{small}
ny[{vowel}>{ni}|{small}
my[{vowel}>{mi}|{small}
hy[{vowel}>{hi}|{small}
by[{vowel}>{bi}|{small}
py[{vowel}>{pi}|{small}
ry[{vowel}>{ri}|{small}

// Doubled consonants

c[c>{xtu}
k[k>{xtu}
g[g>{xtu}
s[s>{xtu}
z[z>{xtu}
j[j>{xtu}
t[t>{xtu}
d[d>{xtu}
h[h>{xtu}
f[f>{xtu}
p[p>{xtu}
b[b>{xtu}
m[m>{xtu}
y[y>{xtu}
r[r>{xtu}
w[w>{xtu}
                */

                + "a>{a}\n"

                + "ba>{ba}\n"
                + "bi>{bi}\n"
                + "bu>{bu}\n"
                + "be>{be}\n"
                + "bo>{bo}\n"
                + "by[{vowel}>{bi}|{small}\n"
                + "b[b>{xtu}\n"

                + "da>{da}\n"
                + "di>{di}\n"
                + "du>{du}\n"
                + "de>{de}\n"
                + "do>{do}\n"
                + "dy[{vowel}>{di}|{small}\n"
                + "dh[{vowel}>{de}|{small}\n"
                + "d[d>{xtu}\n"

                + "e>{e}\n"

                + "fa>{hu}{xa}\n"
                + "fi>{hu}{xi}\n"
                + "fe>{hu}{xe}\n"
                + "fo>{hu}{xo}\n"
                + "fya>{hu}{xya}\n"
                + "fyu>{hu}{xyu}\n"
                + "fyo>{hu}{xyo}\n"
                + "f[f>{xtu}\n"

                + "ga>{ga}\n"
                + "gi>{gi}\n"
                + "gu>{gu}\n"
                + "ge>{ge}\n"
                + "go>{go}\n"
                + "gy[{vowel}>{gi}|{small}\n"
                + "gwa>{gu}{xwa}\n"
                + "gwi>{gu}{xi}\n"
                + "gwu>{gu}{xu}\n"
                + "gwe>{gu}{xe}\n"
                + "gwo>{gu}{xo}\n"
                + "g[g>{xtu}\n"

                + "ha>{ha}\n"
                + "hi>{hi}\n"
                + "hu>{hu}\n"
                + "he>{he}\n"
                + "ho>{ho}\n"
                + "hy[{vowel}>{hi}|{small}\n"
                + "h[h>{xtu}\n"

                + "i>{i}\n"

                + "ka>{ka}\n"
                + "ki>{ki}\n"
                + "ku>{ku}\n"
                + "ke>{ke}\n"
                + "ko>{ko}\n"
                + "kwa>{ku}{xwa}\n"
                + "kwi>{ku}{xi}\n"
                + "kwu>{ku}{xu}\n"
                + "kwe>{ku}{xe}\n"
                + "kwo>{ku}{xo}\n"
                + "ky[{vowel}>{ki}|{small}\n"
                + "k[k>{xtu}\n"

                + "ma>{ma}\n"
                + "mi>{mi}\n"
                + "mu>{mu}\n"
                + "me>{me}\n"
                + "mo>{mo}\n"
                + "my[{vowel}>{mi}|{small}\n"
                + "m[b>{n}\n"
                + "m[f>{n}\n"
                + "m[m>{n}\n"
                + "m[p>{n}\n"
                + "m[v>{n}\n"
                + "m''>{n}\n"

                + "na>{na}\n"
                + "ni>{ni}\n"
                + "nu>{nu}\n"
                + "ne>{ne}\n"
                + "no>{no}\n"
                + "ny[{vowel}>{ni}|{small}\n"
                + "nn>{n}\n"
                + "n''>{n}\n"
                + "n>{n}\n"

                + "o>{o}\n"

                + "pa>{pa}\n"
                + "pi>{pi}\n"
                + "pu>{pu}\n"
                + "pe>{pe}\n"
                + "po>{po}\n"
                + "py[{vowel}>{pi}|{small}\n"
                + "p[p>{xtu}\n"

                + "qa>{ku}{xa}\n"
                + "qi>{ku}{xi}\n"
                + "qu>{ku}{xu}\n"
                + "qe>{ku}{xe}\n"
                + "qo>{ku}{xo}\n"
                + "qy[{vowel}>{ku}|{small}\n"
                + "q[q>{xtu}\n"

                + "ra>{ra}\n"
                + "ri>{ri}\n"
                + "ru>{ru}\n"
                + "re>{re}\n"
                + "ro>{ro}\n"
                + "ry[{vowel}>{ri}|{small}\n"
                + "r[r>{xtu}\n"

                + "sa>{sa}\n"
                + "si>{si}\n"
                + "su>{su}\n"
                + "se>{se}\n"
                + "so>{so}\n"
                + "sy[{vowel}>{si}|{small}\n"
                + "s[sh>{xtu}\n"
                + "s[s>{xtu}\n"

                + "ta>{ta}\n"
                + "ti>{ti}\n"
                + "tu>{tu}\n"
                + "te>{te}\n"
                + "to>{to}\n"
                + "th[{vowel}>{te}|{small}\n"
                + "tsa>{tu}{xa}\n"
                + "tsi>{tu}{xi}\n"
                + "tse>{tu}{xe}\n"
                + "tso>{tu}{xo}\n"
                + "ty[{vowel}>{ti}|{small}\n"
                + "t[ts>{xtu}\n"
                + "t[ch>{xtu}\n"
                + "t[t>{xtu}\n"

                + "u>{u}\n"

                + "va>{VA}\n"
                + "vi>{VI}\n"
                + "vu>{vu}\n"
                + "ve>{VE}\n"
                + "vo>{VO}\n"
                + "vy[{vowel}>{VI}|{small}\n"
                + "v[v>{xtu}\n"

                + "wa>{wa}\n"
                + "wi>{wi}\n"
                + "we>{we}\n"
                + "wo>{wo}\n"
                + "w[w>{xtu}\n"

                + "ya>{ya}\n"
                + "yu>{yu}\n"
                + "ye>{i}{xe}\n"
                + "yo>{yo}\n"
                + "y[y>{xtu}\n"

                + "za>{za}\n"
                + "zi>{zi}\n"
                + "zu>{zu}\n"
                + "ze>{ze}\n"
                + "zo>{zo}\n"
                + "zy[{vowel}>{zi}|{small}\n"
                + "z[z>{xtu}\n"

                + "xa>{xa}\n"
                + "xi>{xi}\n"
                + "xu>{xu}\n"
                + "xe>{xe}\n"
                + "xo>{xo}\n"
                + "xka>{XKA}\n"
                + "xke>{XKE}\n"
                + "xtu>{xtu}\n"
                + "xwa>{xwa}\n"
                + "xya>{xya}\n"
                + "xyu>{xyu}\n"
                + "xyo>{xyo}\n"

                // optional mappings
                + "wu>{u}\n"

                + "ca>{ka}\n"
                + "ci>{si}\n"
                + "cu>{ku}\n"
                + "ce>{se}\n"
                + "co>{ko}\n"
                + "cha>{ti}{xya}\n"
                + "chi>{ti}\n"
                + "chu>{ti}{xyu}\n"
                + "che>{ti}{xe}\n"
                + "cho>{ti}{xyo}\n"
                + "cy[{vowel}>{ti}|{small}\n"
                + "c[k>{xtu}\n"
                + "c[c>{xtu}\n"

                + "fu>{hu}\n"

                + "ja>{zi}{xya}\n"
                + "ji>{zi}\n"
                + "ju>{zi}{xyu}\n"
                + "je>{zi}{xe}\n"
                + "jo>{zi}{xyo}\n"
                + "jy[{vowel}>{zi}|{small}\n"
                + "j[j>{xtu}\n"

                + "la>{ra}\n"
                + "li>{ri}\n"
                + "lu>{ru}\n"
                + "le>{re}\n"
                + "lo>{ro}\n"
                + "ly[{vowel}>{ri}|{small}\n"
                + "l[l>{xtu}\n"

                + "sha>{si}{xya}\n"
                + "shi>{si}\n"
                + "shu>{si}{xyu}\n"
                + "she>{si}{xe}\n"
                + "sho>{si}{xyo}\n"

                + "tsu>{tu}\n"

                + "yi>{i}\n"

                + "xtsu>{xtu}\n"
                + "xyi>{xi}\n"
                + "xye>{xe}\n"







                // Convert vowels to small form
                + "{small}a>{xya}\n"
                + "{small}i>{xi}\n"
                + "{small}u>{xyu}\n"
                + "{small}e>{xe}\n"
                + "{small}o>{xyo}\n"




                + "gy|{hvr}<{gi}[{hv}\n"
                + "gwa<{gu}{xwa}\n"
                + "gwi<{gu}{xi}\n"
                + "gwu<{gu}{xu}\n"
                + "gwe<{gu}{xe}\n"
                + "gwo<{gu}{xo}\n"
                + "ga<{ga}\n"
                + "gi<{gi}\n"
                + "gu<{gu}\n"
                + "ge<{ge}\n"
                + "go<{go}\n"

                + "ky|{hvr}<{ki}[{hv}\n"
                + "kwa<{ku}{xwa}\n"
                + "kwi<{ku}{xi}\n"
                + "kwu<{ku}{xu}\n"
                + "kwe<{ku}{xe}\n"
                + "kwo<{ku}{xo}\n"
                + "qa<{ku}{xa}\n"
                + "qya<{ku}{xya}\n"
                + "qyu<{ku}{xyu}\n"
                + "qyo<{ku}{xyo}\n"
                + "ka<{ka}\n"
                + "ki<{ki}\n"
                + "ku<{ku}\n"
                + "ke<{ke}\n"
                + "ko<{ko}\n"

                + "j|{hvr}<{zi}[{hv}\n" // Hepburn
                + "za<{za}\n"
                + "ji<{zi}\n" // Hepburn
                + "zu<{zu}\n"
                + "ze<{ze}\n"
                + "zo<{zo}\n"

                + "sh|{hvr}<{si}[{hv}\n" // Hepburn
                + "sa<{sa}\n"
                + "shi<{si}\n"
                + "su<{su}\n"
                + "se<{se}\n"
                + "so<{so}\n"

                + "j|{hvr}<{di}[{hv}\n" // Hepburn
                + "dh|{hvr}<{de}[{hv}\n" 
                + "da<{da}\n"
                + "ji<{di}\n" // Hepburn
                + "de<{de}\n"
                + "do<{do}\n"
                + "zu<{du}\n" // Hepburn

                + "ch|{hvr}<{ti}[{hv}\n" // Hepburn
                + "tsa<{tu}{xa}\n"
                + "tsi<{tu}{xi}\n"
                + "tse<{tu}{xe}\n"
                + "tso<{tu}{xo}\n"
                + "th|{hvr}<{te}[{hv}\n"
                + "ta<{ta}\n"
                + "chi<{ti}\n" // Hepburn
                + "tsu<{tu}\n" // Hepburn
                + "te<{te}\n"
                + "to<{to}\n"

                + "ny|{hvr}<{ni}[{hv}\n"
                + "na<{na}\n"
                + "ni<{ni}\n"
                + "nu<{nu}\n"
                + "ne<{ne}\n"
                + "no<{no}\n"

                + "by|{hvr}<{bi}[{hv}\n"
                + "ba<{ba}\n"
                + "bi<{bi}\n"
                + "bu<{bu}\n"
                + "be<{be}\n"
                + "bo<{bo}\n"

                + "py|{hvr}<{pi}[{hv}\n"
                + "pa<{pa}\n"
                + "pi<{pi}\n"
                + "pu<{pu}\n"
                + "pe<{pe}\n"
                + "po<{po}\n"

                + "hy|{hvr}<{hi}[{hv}\n"
                + "fa<{hu}{xa}\n"
                + "fi<{hu}{xi}\n"
                + "fe<{hu}{xe}\n"
                + "fo<{hu}{xo}\n"
                + "fya<{hu}{xya}\n"
                + "fyu<{hu}{xyu}\n"
                + "fyo<{hu}{xyo}\n"
                + "ha<{ha}\n"
                + "hi<{hi}\n"
                + "fu<{hu}\n" // Hepburn
                + "he<{he}\n"
                + "ho<{ho}\n"

                + "my|{hvr}<{mi}[{hv}\n"
                + "ma<{ma}\n"
                + "mi<{mi}\n"
                + "mu<{mu}\n"
                + "me<{me}\n"
                + "mo<{mo}\n"

                + "ya<{ya}\n"
                + "yu<{yu}\n"
                + "ye<{i}{xe}\n"
                + "yo<{yo}\n"
                + "xya<{xya}\n"
                + "xyu<{xyu}\n"
                + "xyo<{xyo}\n"

                + "ry|{hvr}<{ri}[{hv}\n"
                + "ra<{ra}\n"
                + "ri<{ri}\n"
                + "ru<{ru}\n"
                + "re<{re}\n"
                + "ro<{ro}\n"

                + "wa<{wa}\n"
                + "wi<{wi}\n"
                + "we<{we}\n"
                + "wo<{wo}\n"

                + "vu<{vu}\n"
                + "vy|{hvr}<{VI}[{hv}\n"
                + "v<{xtu}[{vu}\n"

                + "xa<{xa}\n"
                + "xi<{xi}\n"
                + "xu<{xu}\n"
                + "xe<{xe}\n"
                + "xo<{xo}\n"

                + "n''<{n}[{a}\n"
                + "n''<{n}[{i}\n"
                + "n''<{n}[{u}\n"
                + "n''<{n}[{e}\n"
                + "n''<{n}[{o}\n"
                + "n''<{n}[{na}\n"
                + "n''<{n}[{ni}\n"
                + "n''<{n}[{nu}\n"
                + "n''<{n}[{ne}\n"
                + "n''<{n}[{no}\n"
                + "n''<{n}[{ya}\n"
                + "n''<{n}[{yu}\n"
                + "n''<{n}[{yo}\n"
                + "n''<{n}[{n}\n"
                + "n<{n}\n"


                + "g<{xtu}[{ga}\n"
                + "g<{xtu}[{gi}\n"
                + "g<{xtu}[{gu}\n"
                + "g<{xtu}[{ge}\n"
                + "g<{xtu}[{go}\n"
                + "k<{xtu}[{ka}\n"
                + "k<{xtu}[{ki}\n"
                + "k<{xtu}[{ku}\n"
                + "k<{xtu}[{ke}\n"
                + "k<{xtu}[{ko}\n"

                + "z<{xtu}[{za}\n"
                + "z<{xtu}[{zi}\n"
                + "z<{xtu}[{zu}\n"
                + "z<{xtu}[{ze}\n"
                + "z<{xtu}[{zo}\n"
                + "s<{xtu}[{sa}\n"
                + "s<{xtu}[{si}\n"
                + "s<{xtu}[{su}\n"
                + "s<{xtu}[{se}\n"
                + "s<{xtu}[{so}\n"

                + "d<{xtu}[{da}\n"
                + "d<{xtu}[{di}\n"
                + "d<{xtu}[{du}\n"
                + "d<{xtu}[{de}\n"
                + "d<{xtu}[{do}\n"
                + "t<{xtu}[{ta}\n"
                + "t<{xtu}[{ti}\n"
                + "t<{xtu}[{tu}\n"
                + "t<{xtu}[{te}\n"
                + "t<{xtu}[{to}\n"


                + "b<{xtu}[{ba}\n"
                + "b<{xtu}[{bi}\n"
                + "b<{xtu}[{bu}\n"
                + "b<{xtu}[{be}\n"
                + "b<{xtu}[{bo}\n"
                + "p<{xtu}[{pa}\n"
                + "p<{xtu}[{pi}\n"
                + "p<{xtu}[{pu}\n"
                + "p<{xtu}[{pe}\n"
                + "p<{xtu}[{po}\n"
                + "h<{xtu}[{ha}\n"
                + "h<{xtu}[{hi}\n"
                + "h<{xtu}[{hu}\n"
                + "h<{xtu}[{he}\n"
                + "h<{xtu}[{ho}\n"


                + "r<{xtu}[{ra}\n"
                + "r<{xtu}[{ri}\n"
                + "r<{xtu}[{ru}\n"
                + "r<{xtu}[{re}\n"
                + "r<{xtu}[{ro}\n"

                + "w<{xtu}[{wa}\n"
                + "xtu<{xtu}\n"

                + "a<{a}\n"
                + "i<{i}\n"
                + "u<{u}\n"
                + "e<{e}\n"
                + "o<{o}\n"



                // Convert small forms to vowels
                + "a<{hvr}{xya}\n"
                + "i<{hvr}{xi}\n"
                + "u<{hvr}{xyu}\n"
                + "e<{hvr}{xe}\n"
                + "o<{hvr}{xyo}\n"              
            }
        };
    }
}



