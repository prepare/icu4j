/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/demo/rbbi/Attic/DBBIDemo.java,v $ 
 * $Date: 2000/03/10 03:47:43 $ 
 * $Revision: 1.4 $
 *
 *****************************************************************************************
 */
package com.ibm.demo.rbbi;

import com.ibm.demo.*;
import java.applet.Applet;
import java.awt.*;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import java.util.*;

import com.ibm.text.BreakIterator;

public class DBBIDemo extends DemoApplet
{
    public static void main(String argv[]) {
        Locale.setDefault(new Locale("en", "US", "DEMO"));
		new DBBIDemo().showDemo();
    }

    public Frame createDemoFrame(DemoApplet applet) {
        return new DBBIFrame(applet);
    }
}



class DBBIFrame extends Frame
{
    private static final String creditString =
        "v1.1a9, Demo";

    private static final int FIELD_COLUMNS = 45;
    private static final Font choiceFont = null;
    private static final boolean DEBUG = false;
    private DemoApplet applet;

    final String right = "-->";
    final String left = "<--";

    private BreakIterator enum;

JTextArea text;
//    TextArea text;
    Choice bound;

    public DBBIFrame(DemoApplet applet)
    {
        this.applet = applet;
        init();
        start();
    }



    public void run()
    {
        /*
        while (true) {
            try {
                checkChange();
                Thread.sleep(250);
            }
            catch (InterruptedException e) {
            }
            catch (Exception e) {
            }
            catch (Throwable e) {
            }
        }
        */
    }

    int s, e;
    int ts, te;

    public void checkChange()
    {
//        System.out.println("checkChange...");
        if ((text.getSelectionStart() & 0x7FFF) != ts ||
            (text.getSelectionEnd() & 0x7FFF) != te) {

            int tempS = text.getSelectionStart() & 0x7FFF;
            int tempE = text.getSelectionEnd() & 0x7FFF;

//          System.out.println(">");
//          select(0, 0);
//          select(tempS, tempE);
            //select(tempS - (ts - s), tempE - (te - e));
//          System.out.println("<");


//          if (s != ts || e != te) System.out.println("     s("+s+") ts("+ts+") e("+e+") te("+te+")");
//          if (tempS != ts || tempE != te) System.out.println(">s("+s+") tempS("+tempS+") e("+e+") tempE("+tempE+")");
//          select(s - (ts - s), e - (te - e));
//          if (tempS != ts || tempE != te) System.out.println("s("+s+") tempS("+tempS+") e("+e+") tempE("+tempE+")");

//          System.out.println("lkdslksj");
        }
    }

    public void select(int sIn, int eIn)
    {
        s = sIn;
        e = eIn;
        text.select(s, e);
        ts = text.getSelectionStart() & 0x7FFF;
        te = text.getSelectionEnd() & 0x7FFF;
//        if (s != ts || e != te) {
//            System.out.println(">s("+s+") ts("+ts+") e("+e+") te("+te+")");
//            System.out.println("   "+(ts-s)+","+(te-e));
//        }
    }

    public int getSelectionStart()
    {
        checkChange();
//      return s;
        return text.getSelectionStart() & 0x7FFF;
    }


    public int getSelectionEnd()
    {
        checkChange();
//      return e;
        return text.getSelectionEnd() & 0x7FFF;
    }

    public final synchronized void selectRange(int s, int e)
    {
        try {
            //if (getSelectionStart() != s || getSelectionEnd() != e) {
                //text.select(s, e);
                select(s,e);
            //}
//          if (getSelectionStart() != s || getSelectionEnd() != e) {
//              System.out.println("AGH! select("+s+","+e+") -> ("+
//              getSelectionStart()+","+getSelectionEnd()+")");
//              text.select(s - (getSelectionStart() - s), e - (getSelectionEnd() - e));
//          }
        } catch (Exception exp) {
            errorText(exp.toString());
        }
    }



    public void init()
    {
        buildGUI();
    }


    public void start()
    {
    }


    void addWithFont(Container container, Component foo, Font font) {
        if (font != null)
            foo.setFont(font);
        container.add(foo);
    }



   public void buildGUI()
    {
        setBackground(DemoUtility.bgColor);
        setLayout(new BorderLayout());

       Panel topPanel = new Panel();

            Label titleLabel =
                new Label("Text Boundary Demo", Label.CENTER);
            titleLabel.setFont(DemoUtility.titleFont);
            topPanel.add(titleLabel);

            //Label demo=new Label(creditString, Label.CENTER);
            //demo.setFont(DemoUtility.creditFont);
            //topPanel.add(demo);

            Panel choicePanel = new Panel();

            Label demo1=new Label("Boundaries", Label.LEFT);
            demo1.setFont(DemoUtility.labelFont);
            choicePanel.add(demo1);

            bound = new Choice();
                bound.setBackground(DemoUtility.choiceColor);
            bound.addItem("Sentence");
            bound.addItem("Line Break");
            bound.addItem("Word");
            bound.addItem("Char");
            if (choiceFont != null)
                bound.setFont(choiceFont);

            choicePanel.add(bound);
            topPanel.add(choicePanel);

            DemoUtility.fixGrid(topPanel,1);


        add("North", topPanel);


            int ROWS = 15;
            int COLUMNS = 50;
//            text = new TextArea(getInitialText(), ROWS, COLUMNS);
text = new JTextArea(getInitialText(), ROWS, COLUMNS);
text.setLineWrap(true);
text.setWrapStyleWord(true);
            text.setEditable(true);
            text.selectAll();
            text.setFont(DemoUtility.editFont);
            text.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        add("Center", new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        Panel copyrightPanel = new Panel();
        addWithFont (copyrightPanel,
            new Label(DemoUtility.copyright1, Label.LEFT),DemoUtility.creditFont);
        addWithFont (copyrightPanel,
            new Label(DemoUtility.copyright2, Label.LEFT),DemoUtility.creditFont);
        DemoUtility.fixGrid(copyrightPanel,1);
        add("South", copyrightPanel);

        //layout();
        handleEnumChanged();

        // (new Thread(this)).start();
    }



    public String getInitialText()
    {
        return
"When,inthecourseofhumanevents,itbecomesnecessaryforonepeopletodissolvethepoliticalbondswhichhave"
+ "connectedthemwithanother,andtoassumeamongthepowersoftheearth,theseparateandequalstationtowhichthelaws"
+ "ofnatureandofnature'sGodentitlethem,adecentrespecttotheopinionsofmankindrequiresthattheyshoulddeclarethe"
+ "causeswhichimpelthemtotheseparation.\n"
+ "Weholdthesetruthstobeself-evident,thatallmenarecreatedequal,thattheyareendowedbytheirCreatorwithcertain"
+ "unalienablerights,thatamongthesearelife,libertyandthepursuitofhappiness.Thattosecuretheserights,governmentsare"
+ "institutedamongmen,derivingtheirjustpowersfromtheconsentofthegoverned.Thatwheneveranyformofgovernment"
+ "becomesdestructivetotheseends,itistherightofthepeopletoalterortoabolishit,andtoinstitutenewgovernment,laying"
+ "itsfoundationonsuchprinciplesandorganizingitspowersinsuchform,astothemshallseemmostlikelytoeffecttheirsafety"
+ "andhappiness.Prudence,indeed,willdictatethatgovernmentslongestablishedshouldnotbechangedforlightandtransient"
+ "causes;andaccordinglyallexperiencehathshownthatmankindaremoredisposedtosuffer,whileevilsaresufferable,than"
+ "torightthemselvesbyabolishingtheformstowhichtheyareaccustomed.Butwhenalongtrainofabusesandusurpations,"
+ "pursuinginvariablythesameobjectevincesadesigntoreducethemunderabsolutedespotism,itistheirright,itistheirduty,"
+ "tothrowoffsuchgovernment,andtoprovidenewguardsfortheirfuturesecurity.--Suchhasbeenthepatientsufferanceof"
+ "thesecolonies;andsuchisnowthenecessitywhichconstrainsthemtoaltertheirformersystemsofgovernment.Thehistory"
+ "ofthepresentKingofGreatBritainisahistoryofrepeatedinjuriesandusurpations,allhavingindirectobjectthe"
+ "establishmentofanabsolutetyrannyoverthesestates.Toprovethis,letfactsbesubmittedtoacandidworld.\n"
+ "Hehasrefusedhisassenttolaws,themostwholesomeandnecessaryforthepublicgood.\n"
+ "Hehasforbiddenhisgovernorstopasslawsofimmediateandpressingimportance,unlesssuspendedintheiroperationtill"
+ "hisassentshouldbeobtained;andwhensosuspended,hehasutterlyneglectedtoattendtothem.\n"
+ "Hehasrefusedtopassotherlawsfortheaccommodationoflargedistrictsofpeople,unlessthosepeoplewouldrelinquish"
+ "therightofrepresentationinthelegislature,arightinestimabletothemandformidabletotyrantsonly.\n"
+ "Hehascalledtogetherlegislativebodiesatplacesunusual,uncomfortable,anddistantfromthedepositoryoftheirpublic"
+ "records,forthesolepurposeoffatiguingthemintocompliancewithhismeasures.\n"
+ "Hehasdissolvedrepresentativehousesrepeatedly,foropposingwithmanlyfirmnesshisinvasionsontherightsofthepeople.\n"
+ "Hehasrefusedforalongtime,aftersuchdissolutions,tocauseotherstobeelected;wherebythelegislativepowers,"
+ "incapableofannihilation,havereturnedtothepeopleatlargefortheirexercise;thestateremaininginthemeantimeexposed"
+ "toallthedangersofinvasionfromwithout,andconvulsionswithin.\n"
+ "Hehasendeavoredtopreventthepopulationofthesestates;forthatpurposeobstructingthelawsfornaturalizationof"
+ "foreigners;refusingtopassotherstoencouragetheirmigrationhither,andraisingtheconditionsofnewappropriationsof"
+ "lands.\n"
+ "Hehasobstructedtheadministrationofjustice,byrefusinghisassenttolawsforestablishingjudiciarypowers.\n"
+ "Hehasmadejudgesdependentonhiswillalone,forthetenureoftheiroffices,andtheamountandpaymentoftheirsalaries.\n"
+ "Hehaserectedamultitudeofnewoffices,andsenthitherswarmsofofficerstoharassourpeople,andeatouttheir"
+ "substance.\n"
+ "Hehaskeptamongus,intimesofpeace,standingarmieswithouttheconsentofourlegislature.\n"
+ "Hehasaffectedtorenderthemilitaryindependentofandsuperiortocivilpower.\n"
+ "Hehascombinedwithotherstosubjectustoajurisdictionforeigntoourconstitution,andunacknowledgedbyourlaws;"
+ "givinghisassenttotheiractsofpretendedlegislation:\n"
+ "Forquarteringlargebodiesofarmedtroopsamongus:\n"
+ "Forprotectingthem,bymocktrial,frompunishmentforanymurderswhichtheyshouldcommitontheinhabitantsofthese"
+ "states:\n"
+ "Forcuttingoffourtradewithallpartsoftheworld:\n"
+ "Forimposingtaxesonuswithoutourconsent:\n"
+ "Fordeprivingusinmanycases,ofthebenefitsoftrialbyjury:\n"
+ "Fortransportingusbeyondseastobetriedforpretendedoffenses:\n"
+ "ForabolishingthefreesystemofEnglishlawsinaneighboringprovince,establishingthereinanarbitrarygovernment,and"
+ "enlargingitsboundariessoastorenderitatonceanexampleandfitinstrumentforintroducingthesameabsoluteruleinthese"
+ "colonies:\n"
+ "Fortakingawayourcharters,abolishingourmostvaluablelaws,andalteringfundamentallytheformsofourgovernments:\n"
+ "Forsuspendingourownlegislatures,anddeclaringthemselvesinvestedwithpowertolegislateforusinallcaseswhatsoever.\n"
+ "Hehasabdicatedgovernmenthere,bydeclaringusoutofhisprotectionandwagingwaragainstus.\n"
+ "Hehasplunderedourseas,ravagedourcoasts,burnedourtowns,anddestroyedthelivesofourpeople.\n"
+ "Heisatthistimetransportinglargearmiesofforeignmercenariestocompletetheworksofdeath,desolationandtyranny,"
+ "alreadybegunwithcircumstancesofcrueltyandperfidyscarcelyparalleledinthemostbarbarousages,andtotalyunworth"
+ "theheadofacivilizednation.\n"
+ "Hehasconstrainedourfellowcitizenstakencaptiveonthehighseastobeararmsagainsttheircountry,tobecomethe"
+ "executionersoftheirfriendsandbrethren,ortofallthemselvesbytheirhands.\n"
+ "Hehasexciteddomesticinsurrectionsamongstus,andhasendeavoredtobringontheinhabitantsofourfrontiers,the"
+ "mercilessIndiansavages,whoseknownruleofwarfare,isundistinguisheddestructionofallages,sexesandconditions.\n"
+ "Ineverystageoftheseoppressionswehavepetitionedforredressinthemosthumbleterms:ourrepeatedpetitionshave"
+ "beenansweredonlybyrepeatedinjury.Aprince,whosecharacteristhusmarkedbyeveryactwhichmaydefineatyrant,is"
+ "unfittobetherulerofafreepeople.\n"
+ "NorhavewebeenwantinginattentiontoourBritishbrethren.Wehavewarnedthemfromtimetotimeofattemptsbytheir"
+ "legislaturetoextendanunwarrantablejurisdictionoverus.Wehaveremindedthemofthecircumstancesofouremigration"
+ "andsettlementhere.Wehaveappealedtotheirnativejusticeandmagnanimity,andwehaveconjuredthembythetiesofour"
+ "commonkindredtodisavowtheseusurpations,which,wouldinevitablyinterruptourconnectionsandcorrespondence.We"
+ "must,therefore,acquiesceinthenecessity,whichdenouncesourseparation,andholdthem,asweholdtherestofmankind,"
+ "enemiesinwar,inpeacefriends.\n"
+ "We,therefore,therepresentativesoftheUnitedStatesofAmerica,inGeneralCongress,assembled,appealingtothe"
+ "SupremeJudgeoftheworldfortherectitudeofourintentions,do,inthename,andbytheauthorityofthegoodpeopleof"
+ "thesecolonies,solemnlypublishanddeclare,thattheseunitedcoloniesare,andofrightoughttobefreeandindependent"
+ "states;thattheyareabsolvedfromallallegiancetotheBritishCrown,andthatallpoliticalconnectionbetweenthemandthe"
+ "stateofGreatBritain,isandoughttobetotallydissolved;andthatasfreeandindependentstates,theyhavefullpowerto"
+ "leveywar,concludepeace,contractalliances,establishcommerce,andtodoallotheractsandthingswhichindependent"
+ "statesmayofrightdo.Andforthesupportofthisdeclaration,withafirmrelianceontheprotectionofDivineProvidence,we"
+ "mutuallypledgetoeachotherourlives,ourfortunesandoursacredhonor.\n";
    }


    public void handleEnumChanged()
    {
        String s = bound.getSelectedItem();
        if (s.equals("Char")) {
            errorText("getCharacterInstance");
            enum = BreakIterator.getCharacterInstance();
        }
        else if (s.equals("Word")) {
            errorText("tWordBreak");
            enum = BreakIterator.getWordInstance();
        }
        else if (s.equals("Line Break")) {
            errorText("getLineInstance");
            enum = BreakIterator.getLineInstance();
        }
        else /* if (s.equals("Sentence")) */ {
            errorText("getSentenceInstance");
            enum = BreakIterator.getSentenceInstance();
        }
        enum.setText(text.getText());
        selectRange(0, 0);
        //text.select(0,0);
    }

    public void handleForward()
    {
        try {
//          System.out.println("entering handleForward");
            enum.setText(text.getText());
            int oldStart = getSelectionStart();
            int oldEnd = getSelectionEnd();

//          System.out.println("handleForward: oldStart=" + oldStart + ", oldEnd=" + oldEnd);

            if (oldEnd < 1) {
                selectRange(0, enum.following(0));
            }
            else {
                int s = enum.following(oldEnd-1);
                int e = enum.next();
                if (e == -1) {
                    e = s;
                }
                selectRange(s, e);
            }
            //text.select(s, e);
            errorText("<" + oldStart + "," + oldEnd + "> -> <" +
                s + "," + e + ">");
        }
        catch (Exception exp) {
            errorText(exp.toString());
        }
    }

    public void handleBackward()
    {
        try {
            enum.setText(text.getText());
            int oldStart = getSelectionStart();
            int oldEnd = getSelectionEnd();
            if (oldStart < 1) {
                selectRange(0, 0);
            }
            else {
                int e = enum.following(oldStart-1);
                int s = enum.previous();
                selectRange(s, e);
            }
            //text.select(s, e);
            errorText("<" + oldStart + "," + oldEnd + "> -> <" + s + "," + e + ">");
        }
        catch (Exception exp) {
            errorText(exp.toString());
        }
    }

    public boolean action(Event evt, Object obj)
    {

        if(evt.target instanceof Button && left.equals(obj))
        {
            handleBackward();
            return true;
        }
        else if(evt.target instanceof Button && right.equals(obj))
        {
            handleForward();
            return true;
        }
        else if(evt.target instanceof Choice)
        {
            handleEnumChanged();
            return true;
        }
        return false;
    }

    public boolean handleEvent(Event evt)
    {
        if (evt.id == Event.KEY_PRESS || evt.id == Event.KEY_ACTION) {
            if (evt.key == Event.RIGHT || (evt.key == 0x0E && evt.controlDown())) {
                handleForward();
                return true;
            }
            else if (evt.key == Event.LEFT || (evt.key == 0x10 && evt.controlDown())) {
                handleBackward();
                return true;
            }
        }
        else
        if (evt.id == Event.WINDOW_DESTROY && evt.target == this) {
            this.hide();
            this.dispose();
                if (applet != null) {
                  applet.demoClosed();
               } else System.exit(0);
            return true;
        }
        return super.handleEvent(evt);
    }

    public void errorText(String s)
    {
       if (DEBUG)
           System.out.println(s);
    }
}
