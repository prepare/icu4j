/*
 *******************************************************************************
 * Copyright (C) 1996-2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.demo.rbbi;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import java.util.*;
import com.ibm.icu.dev.demo.impl.*;
import com.ibm.icu.text.BreakIterator;

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



class DBBIFrame extends Frame implements ItemListener
{
    private static final String creditString =
        "v1.1a9, Demo";

    private static final int FIELD_COLUMNS = 45;
    private static final Font choiceFont = null;
    private static final boolean DEBUG = false;
    private DemoApplet applet;

    final String right = "-->";
    final String left = "<--";

    private BreakIterator iter;
    private static boolean isctrldown_ = false;

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

            // not used int tempS = text.getSelectionStart() & 0x7FFF;
            // not used int tempE = text.getSelectionEnd() & 0x7FFF;

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
            
            bound.addItemListener(this);
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
        DemoUtility.fixGrid(copyrightPanel,1);
        add("South", copyrightPanel);

        //layout();
        handleEnumChanged();
        
        enableEvents(WindowEvent.WINDOW_CLOSING);
        enableEvents(KeyEvent.KEY_PRESSED);
        enableEvents(KeyEvent.KEY_RELEASED);

    text.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
            if (e.isControlDown()) {
            int kc = e.getKeyCode();
            switch (e.getKeyCode()) {
            case KeyEvent.VK_N:
            case KeyEvent.VK_RIGHT:
                handleForward();
                break;
            case KeyEvent.VK_P:
            case KeyEvent.VK_LEFT:
                handleBackward();
                break;
            default:
                break;
            }
            e.consume();
            }
        }
        public void keyReleased(KeyEvent e) {
            if (e.isControlDown()) {
            e.consume();
            }
        }
        public void keyTyped(KeyEvent e) {
            if (e.isControlDown()) {
            e.consume();
            }
        }
        });

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
+ "alreadybegunwithcircumstancesofcrueltyandperfidyscarcelyparalleledinthemostbarbarousages,andtotallyunworthy"
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
            iter = BreakIterator.getCharacterInstance();
        }
        else if (s.equals("Word")) {
            errorText("getWordInstance");
            iter = BreakIterator.getWordInstance();
        }
        else if (s.equals("Line Break")) {
            errorText("getLineInstance");
            iter = BreakIterator.getLineInstance();
        }
        else /* if (s.equals("Sentence")) */ {
            errorText("getSentenceInstance");
            iter = BreakIterator.getSentenceInstance();
        }
        iter.setText(text.getText());
        selectRange(0, 0);
        //text.select(0,0);
    }

    public void handleForward()
    {
        try {
//          System.out.println("entering handleForward");
            iter.setText(text.getText());
            int oldStart = getSelectionStart();
            int oldEnd = getSelectionEnd();

//          System.out.println("handleForward: oldStart=" + oldStart + ", oldEnd=" + oldEnd);

            if (oldEnd < 1) {
                selectRange(0, iter.following(0));
            }
            else {
                int s = iter.following(oldEnd-1);
                int e = iter.next();
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
            iter.setText(text.getText());
            int oldStart = getSelectionStart();
            int oldEnd = getSelectionEnd();
            if (oldStart < 1) {
                selectRange(0, 0);
            }
            else {
                int e = iter.following(oldStart-1);
                int s = iter.previous();
                selectRange(s, e);
            }
            //text.select(s, e);
            errorText("<" + oldStart + "," + oldEnd + "> -> <" + s + "," + e + ">");
        }
        catch (Exception exp) {
            errorText(exp.toString());
        }
    }

    public void itemStateChanged(ItemEvent evt)
    {
        if (evt.getSource() instanceof Choice) {
            handleEnumChanged();
        }
    }

    public void errorText(String s)
    {
       if (DEBUG)
           System.out.println(s);
    }
    
    protected void processWindowEvent(WindowEvent evt)
    {
        if (evt.getID() == WindowEvent.WINDOW_CLOSING && 
            evt.getWindow() == this) {
            hide();
            dispose();
            if (applet != null) {
                applet.demoClosed();
            } else System.exit(0);
        }
    }
    
    protected void processKeyEvent(KeyEvent evt)
    {
        switch (evt.getID()) {
            case KeyEvent.KEY_PRESSED :
                if (evt.getKeyCode() == KeyEvent.VK_CONTROL) {
                    isctrldown_ = true;
                }
                break;
            case KeyEvent.KEY_RELEASED :
                // key detection for left and right buttons are removed
                // to emulate the old release behaviour
                int key = evt.getKeyCode();
                if (key == KeyEvent.VK_N && isctrldown_) {
                    handleForward();
                }
                else 
                if (key == KeyEvent.VK_P && isctrldown_) {
                    handleBackward();
                }
                else 
                if (key == KeyEvent.VK_CONTROL) {
                    isctrldown_ = false;
                }
                break;
        }
    }
}
