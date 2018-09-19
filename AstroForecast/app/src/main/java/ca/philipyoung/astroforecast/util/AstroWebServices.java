package ca.philipyoung.astroforecast.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.http.AndroidHttpClient;
import android.net.http.HttpResponseCache;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ca.philipyoung.astroforecast.R;

/**
 * Created by Philip Young on 2018-06-02.
 * Anything that requires getting data from the internet:
 *     JSON, XML
 *     Unformatted HTML, phusking, scraping, nose-mining
 *     Sending source non-compliance updates
 *     Handling off-line data
 *
 *     Sources:
 *     philipyoung.ca
 *     theweathernetwork
 *     timeanddate
 *     http://heavens-above.com/
 *     https://maps.googleapis.com/
 *     https://www.aavso.org/
 *     http://www.aphayes.pwp.blueyonder.co.uk/
 *     http://www.gi.alaska.edu/
 *
 */

public class AstroWebServices {
    private static final String TAG = "AstroWebServices";

    private static final int WEATHER_CONST = 905;
    private static final int EVENTS_CONST = 416;
    private static final String SITE_PATH = "http://www.philipyoung.ca/philslab/";
    private static final String WEATHER_FUNC = "astroforecastXML.php";
    private static final String EVENTS_FUNC = "astroforecast_events.php";
    private static final String OBSERVATORY_GET = "obs";
    private static final String COORDINATES_GET = "latlng";
    /* **************************************************

    Report if the next night is good for viewing stars.
    Parameters:
                obs    - pick an known observatory or name one
                latlng - set the geo location
                aurora - set the aurora level 1-9
                ts     - set the timestamp specific observation time (Julian)
                hr     - set the hour specific observation time ( ##h, ##h##, ##h##m, ##h##m##, ##h##m##s )
                sun    - display sunrise sunset times
                moon   - display moonrise moonset times
                verbose - display extra information for error tracking

    ***************************************************** */
    private static final String AURORA_GET = "aurora";
    private static final String TIMESTAMP_GET = "ts";
    private static final String TIMEHOUR_GET = "hr";
    private static final String SUNTIMES_GET = "sun";
    private static final String MOONTIMES_GET = "moon";
    /* **
     *  Events lookup page
     * Parameters:
     * 			obs    - pick an known observatory or name one
     * 			latlng - set the geo location
     * 			latlngalt - set the extended geo location
     * 			aurora - set the aurora level 1-9
     * 			dist   - set the angular distance between conjuncting planets, in degrees
     * 			datetime - set the specific observation time ( YYYYMMDDhhmmss )
     * 			verbose - display extra information for error tracking
     ** */
    private static final String COORDINATES_EXT_GET = "latlngalt";
    private static final String DATETIMEHOUR_GET = "datetime";
    private static final String CONJUNCTION_DISTANCE_GET = "dist";

    // Shared Preferences from Settings
    private static final String OBSERVATORY_KEY = "observatory_text";
    private static final String COORDINATES_KEY = "coordinate_text";
    private static final String COORDINATES_LAT_KEY = "coordinate_latitude";
    private static final String COORDINATES_LON_KEY = "coordinate_longitude";
    private static final String COORDINATES_ALT_KEY = "coordinate_altitude";
    private static final String AURORA_KEY = "notifications_key_5";
    private static final String AURORA_LEVEL_KEY = "aurora";
    private static final String TIMESTAMP_KEY = "ts";
    private static final String TIMEHOUR_KEY = "hr";
    private static final String SUNTIMES_KEY = "notifications_key_9";
    private static final String MOONTIMES_KEY = "notifications_key_2";
    private static final String MOON_PHASE_KEY = "moon_phase_name";
    private static final String MOON_ILLUMINATION_KEY = "moon_illumination_fraction";
    private static final String MOON_RISE_KEY = "moon_rise";
    private static final String MOON_SET_KEY = "moon_set";

    private Context mContext;

    // shared preference keys

    public AstroWebServices(Context context) {
        this.mContext = context;
    }

    private String getJSONRequest(String strCity) {
        // DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
        // HttpWebRequest request = CreateWebRequest();
        // InputStream content = null;
        // AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        // HttpGet httpGet = new HttpGet(myurl);
        // try {
        //     HttpResponse execute = client.execute(httpGet);
        //     if(execute == null){ return null;} // null check to see if execute is null
        //     content = execute.getEntity().getContent();
        // } catch (Exception e) {
        //     xmldownloaderror = e.getMessage();
        //     Log.d("mylogitem", e.getMessage());
        // }
        // HttpResponseCache httpResponseCache;
        // HttpURLConnection httpURLConnection;
        // HttpClient client = new DefaultHttpClient();

        // HttpResponse response;
        // DefaultHttpClient httpClient = new DefaultHttpClient();
        // BufferedReader bufferedReader = null;
        // HttpParams params = client.getParams();

        // HttpConnectionParams.setConnectionTimeout(params, 20000);
        // HttpConnectionParams.setSoTimeout(params, 20000);
        // final HttpParams params = new BasicHttpParams();
        // HttpClientParams.setRedirecting(params, true);
        // HttpClientParams.setCookiePolicy(params, CookiePolicy.ACCEPT_ORIGINAL_SERVER);

        // DefaultHttpClient httpClient = new DefaultHttpClient(params);
        // httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:23.0) Gecko/20131011 Firefox/23.0");

        // HttpGet httpGet = new HttpGet(parsingWebURL);
        // HttpURLConnection httpURLConnection;
        // AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        // HttpGet request = new HttpGet(linkk);
        // HttpResponse response = client.execute(request); //here is where the exception is thrown
        // response.getEntity().writeTo(new FileOutputStream(f));
        // HttpResponse httpResponse = httpClient.execute(httpGet);
        // HttpEntity httpEntity = httpResponse.getEntity();
        // strXML = EntityUtils.toString(httpEntity);
        // return strXML;
        return null;
    }

    private String getXMLRequest(Integer intType) {
        String strURL, sPValue;
        Map<String,String> lstGets = new HashMap<>();
        ArrayList<String> lstGetKeys = new ArrayList<>();
        ArrayList<String> lstGetValues = new ArrayList<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        switch (intType) {
            case WEATHER_CONST:
                if(sharedPreferences!=null) {
                    sPValue = sharedPreferences.getString(OBSERVATORY_KEY,"");
                    if( !sPValue.isEmpty() ) {
                        lstGetKeys.add(OBSERVATORY_GET);
                        lstGetValues.add(sPValue);
                    }
                    sPValue = sharedPreferences.getString(COORDINATES_KEY, "");
                    if( !sPValue.isEmpty() ) {
                        lstGetKeys.add(COORDINATES_GET);
                        lstGetValues.add(sPValue);
                    }
                }
                strURL = SITE_PATH + WEATHER_FUNC;
                if( lstGetKeys.size()>0 && lstGetKeys.size()==lstGetValues.size() ) {
                    strURL += "?";
                    for (int intI = 0; intI < lstGetKeys.size(); intI++) {
                        strURL += lstGetKeys.get(intI) +"="+ lstGetValues.get(intI)
                                .replaceAll("\\s","+")
                                .replaceAll(",","%2C")
                                .replaceAll("\"","%22");
                    }
                }
                break;
            case EVENTS_CONST:
                if(sharedPreferences!=null) {
                    sPValue = sharedPreferences.getString(OBSERVATORY_KEY,"");
                    if( !sPValue.isEmpty() ) {
                        lstGetKeys.add(OBSERVATORY_GET);
                        lstGetValues.add(sPValue);
                    }
                    sPValue = sharedPreferences.getString(COORDINATES_KEY, "");
                    if( !sPValue.isEmpty() ) {
                        lstGetKeys.add(COORDINATES_GET);
                        lstGetValues.add(sPValue);
                    }
                }
                strURL = SITE_PATH + EVENTS_FUNC;
                if( lstGetKeys.size()>0 && lstGetKeys.size()==lstGetValues.size() ) {
                    strURL += "?";
                    for (int intI = 0; intI < lstGetKeys.size(); intI++) {
                        strURL += lstGetKeys.get(intI) +"="+ lstGetValues.get(intI)
                                .replaceAll("\\s","+")
                                .replaceAll(",","%2C")
                                .replaceAll("\"","%22");
                    }
                }
                break;
        }
        return null;
    }

    public void parseXML(){

        AstroDatabase astroDatabase = new AstroDatabase(mContext);
        astroDatabase.astroDBclose();
        astroDatabase = null;

        try {
            InputStream inputStream = mContext.openFileInput("myFile");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);

            Element element=doc.getDocumentElement();
            element.normalize();

            NodeList nViewing = doc.getElementsByTagName("viewing");
            NodeList nEvents = doc.getElementsByTagName("events");
            NodeList nlObservatory = doc.getElementsByTagName("observatory");
            String strObservatory = null;
            if(nViewing.getLength()>1) {
                // This is a weather file, I hope.
                // Set the observatory. Should match default. Let's see.
                if(nlObservatory.getLength()>0) {
                    /*
                    * <observatory>
                    *     <observatory>Phil's Balcony Observatory</observatory>
                    *     <latitude>43.6515952</latitude>
                    *     <longitude>-79.5692296</longitude>
                    *     <elevation>145.12217712402</elevation>
                    *     <timezone>-4</timezone>
                    *     <suntimes>
                    *         <transit atomic="1528132605">20180604 13h16</transit>
                    *         <sunset atomic="1528160141">20180604 20h55</sunset>
                    *         <civiltwilightends atomic="1528162258">20180604 21h30</civiltwilightends>
                    *         <nauticaltwilightends atomic="1528165012">20180604 22h16</nauticaltwilightends>
                    *         <astrotwilightends atomic="1528168408">20180604 23h13</astrotwilightends>
                    *         <opposition atomic="1528175810">20180605 01h16</opposition>
                    *         <astrotwilightbegins atomic="1528183148">20180605 03h19</astrotwilightbegins>
                    *         <nauticaltwilightbegins atomic="1528186565">20180605 04h16</nauticaltwilightbegins>
                    *         <civiltwilightbegins atomic="1528189327">20180605 05h02</civiltwilightbegins>
                    *         <sunrise atomic="1528191449">20180605 05h37</sunrise>
                    *     </suntimes>
                    *     <moontimes>
                    *         <moonrise atomic="1528176310">20180605 01h25</moonrise>
                    *         <ra radian="5.7736163117278">22.053589812659</ra>
                    *         <dec radian="-0.24597343214307">-14.093239534145</dec>
                    *         <phase>0.70414268859795</phase>
                    *         <illum>0.64207989798302</illum>
                    *         <phasename>Third Quarter</phasename>
                    *         <ra radian="5.6669414234402">21.646121754065</ra>
                    *         <dec radian="-0.27237196189988">-15.605763874561</dec>
                    *         <moonset atomic="1528214288">20180605 11h58</moonset>
                    *         <ra radian="5.865737773478">22.405467876717</ra>
                    *         <dec radian="-0.22056048078219">-12.637184676196</dec>
                    *     </moontimes>
                    * </observatory>
                    * */

                    Float fltLat, fltLng, fltAlt, fltMoonIllumination;
                    String strCity,strMoonPhase;
                    strObservatory = ((Element)nlObservatory.item(0))
                            .getElementsByTagName("observatory").item(0).getTextContent();
                    fltLat = Float.valueOf(
                            ((Element)nlObservatory.item(0))
                                    .getElementsByTagName("latitude").item(0).getTextContent()
                    ) ;
                    fltLng = Float.valueOf(
                            ((Element)nlObservatory.item(0))
                                    .getElementsByTagName("longitude").item(0).getTextContent()
                    ) ;
                    String strCoordinates = String.format(
                            Locale.US,
                            "%1$.6f%3$s %2$.6f%4$s",
                            Math.abs(fltLat),
                            Math.abs(fltLng),
                            fltLat>0?"N":"S",
                            fltLng>0?"E":"W"
                    );
                    fltAlt = Float.valueOf(
                            ((Element)nlObservatory.item(0))
                                    .getElementsByTagName("elevation").item(0).getTextContent()
                    ) ;
                    strCity = ((Element) nlObservatory.item(0))
                            .getElementsByTagName("city").getLength()>0?
                            ((Element) nlObservatory.item(0))
                            .getElementsByTagName("city").item(0).getTextContent():null;
                    fltMoonIllumination = Float.valueOf(
                            ((Element)nlObservatory.item(0))
                                    .getElementsByTagName("illum").item(0)
                                    .getTextContent()
                    ) ;
                    strMoonPhase = ((Element)nlObservatory.item(0))
                                    .getElementsByTagName("phasename").item(0)
                                    .getTextContent();
                    astroDatabase = new AstroDatabase(mContext,strObservatory);
                    if(strCity==null) {
                        astroDatabase.setObservatory(strObservatory, fltLat, fltLng, fltAlt);
                    } else {
                        astroDatabase.setObservatory(strObservatory, fltLat, fltLng, fltAlt, strCity);
                    }
                    astroDatabase.astroDBclose();
                    astroDatabase = null;
                    NodeList nlMoonRise = ((Element)nlObservatory.item(0))
                            .getElementsByTagName("moonrise");
                    NodeList nlMoonSet = ((Element)nlObservatory.item(0))
                            .getElementsByTagName("moonset");

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    if(sharedPreferences!=null) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(OBSERVATORY_KEY, strObservatory);
                        editor.putFloat(COORDINATES_LAT_KEY, fltLat);
                        editor.putFloat(COORDINATES_LON_KEY, fltLng);
                        editor.putFloat(COORDINATES_ALT_KEY, fltAlt);
                        editor.putString(COORDINATES_KEY, strCoordinates);
                        editor.putString(MOON_PHASE_KEY, strMoonPhase);
                        editor.putFloat(MOON_ILLUMINATION_KEY, fltMoonIllumination);
                        if(nlMoonRise.getLength()==1) editor.putLong(MOON_RISE_KEY,Long.valueOf(nlMoonRise.item(0).getAttributes().getNamedItem("atomic").getNodeValue()));
                        if(nlMoonSet.getLength()==1) editor.putLong(MOON_SET_KEY,Long.valueOf(nlMoonSet.item(0).getAttributes().getNamedItem("atomic").getNodeValue()));
                        editor.apply();
                    }
                }

                // Get the rest of the weather
                NodeList nlNight = doc.getElementsByTagName("night");
                NodeList nlWeatherHours = doc.getElementsByTagName("hour");
                String strMidnight, strTwilight;
                Long dteWeatherReportStart=0L,dteWeatherReportEnd=0L;
                Integer intCloudCover=-1, intTransparency=-1, intSeeing=-1,
                        intWind=-1, intHumidity=-1, intTemperature=-1;
                Map<String,Long> mapTimes = new HashMap<>();
                for (int i = 0; i < nlNight.getLength(); i++) {
                    Node node = nlNight.item(i);
                    if(node.getNodeType()==Node.ELEMENT_NODE) {
                        NodeList nlHours = ((Element) node).getElementsByTagName("hour");
                        NodeList nlTimes = ((Element) node).getElementsByTagName("atom");
                        if(nlHours.getLength()==nlTimes.getLength()) {
                            for (int j = 0; j < nlHours.getLength(); j++) {
                                if(nlHours.item(j).getChildNodes().item(0).getNodeValue()!=null){
                                    if(!mapTimes.containsKey(nlHours.item(j).getChildNodes().item(0).getNodeValue())) {
                                        mapTimes.put(nlHours.item(j).getChildNodes().item(0).getNodeValue(),
                                                Long.valueOf(nlTimes.item(j).getChildNodes().item(0).getNodeValue()));
                                    }
                                    if(nlHours.item(j).getChildNodes().item(0).getNodeValue().equals("00h00")) {
                                        strMidnight = nlTimes.item(j).getChildNodes().item(0).getNodeValue();
                                    }
                                }
                            }
                        }
                    }
                }
                for (int i = 0; i < nlWeatherHours.getLength(); i++) {
                    Node node = nlWeatherHours.item(i);
                    intCloudCover=-1; intTransparency=-1; intSeeing=-1;
                            intWind=-1; intHumidity=-1; intTemperature=-1;

                    // what is the weather forecast for this hour?
                    if(node.getNodeType()==Node.ELEMENT_NODE) {
                        NodeList nlWeather = ((Element) node).getElementsByTagName("weather");
                        if (nlWeather.getLength() > 0) {
                            // what time is it? Look up <hour> attribute "time" and lookup the long value
                            //  todo: check for null pointer
                            dteWeatherReportStart = mapTimes.get(nlWeatherHours.item(i).getAttributes().getNamedItem("time").getNodeValue());
                            dteWeatherReportEnd = dteWeatherReportStart + 3600L;

                            for (int j = 0; j < nlWeather.getLength(); j++) {
                                NodeList nlRating = ((Element) nlWeather.item(j)).getElementsByTagName("rating");
                                String strRating = nlRating.item(0).getChildNodes().item(0).getNodeValue();
                                // Got the weather rating, what about the weather type?
                                switch (nlWeather.item(j).getAttributes().item(0).getNodeValue()) {
                                    case "northeast":
                                    case "northwest":
                                    case "southeast":
                                    case "southwest":
                                        intCloudCover = Integer.valueOf(strRating);
                                        break;
                                    case "transparency":
                                        intTransparency = Integer.valueOf(strRating);
                                        break;
                                    case "seeing":
                                        intSeeing = Integer.valueOf(strRating);
                                        break;
                                    case "wind":
                                        intWind = Integer.valueOf(strRating);
                                        break;
                                    case "humidity":
                                        intHumidity = Integer.valueOf(strRating);
                                        break;
                                    case "temperature":
                                        intTemperature = Integer.valueOf(strRating);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        if (dteWeatherReportStart!=0L && intCloudCover!=-1 && intSeeing!=-1) {
                            // save the weather
                            astroDatabase = new AstroDatabase(mContext,strObservatory);
                            astroDatabase.saveWeatherRecord(dteWeatherReportStart,dteWeatherReportEnd,
                                    intCloudCover,intTransparency,intSeeing,
                                    intWind,intHumidity,intTemperature);
                            astroDatabase.astroDBclose();
                            astroDatabase = null;
                        }
                    }
                }
                for (int i=0; i<nViewing.getLength(); i++) {
                    Node node = nViewing.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element2 = (Element) node;
                        String strSummary, string = element2.getNodeValue();
                        NodeList nlEvening = element2.getElementsByTagName("evening");
                        NodeList nlDarkest = element2.getElementsByTagName("darkestnight");
                        NodeList nlOvernight = element2.getElementsByTagName("overnight");
                        NodeList nlMorning = element2.getElementsByTagName("morning");
                        for (String strNightTime :
                                new String[]{"evening", "overnight", "morning", "darkestnight"}) {
                            NodeList nlNightTime = element2.getElementsByTagName(strNightTime);
                            if( nlNightTime.getLength()>0 ) {
                                if( nlNightTime.item(0).getNodeType()==Node.ELEMENT_NODE) {
                                    dteWeatherReportStart=Long.valueOf(((Element)nlNightTime.item(0))
                                            .getElementsByTagName("clockbegin").item(0)
                                            .getAttributes().getNamedItem("atomic").getNodeValue());
                                    dteWeatherReportEnd=Long.valueOf(((Element)nlNightTime.item(0))
                                            .getElementsByTagName("clockend").item(0)
                                            .getAttributes().getNamedItem("atomic").getNodeValue());
                                    strSummary = ((Element)nlNightTime.item(0))
                                            .getElementsByTagName("view").item(0).getNodeValue();
                                    astroDatabase = new AstroDatabase(mContext,strObservatory);
                                    astroDatabase.saveWeatherTwilight(strNightTime,dteWeatherReportStart,dteWeatherReportEnd,strSummary);
                                    astroDatabase.astroDBclose();
                                    astroDatabase = null;
                                }
                            }
                        }
                    }
                }
            }

            if(nEvents.getLength()>0) {
                // Set the observatory. Should match default. Let's see.
                if (nlObservatory.getLength() > 0) {
                    /*
                    * <observatory>
                    *     <latitude>43.6515952</latitude>
                    *     <longitude>-79.5692296</longitude>
                    *     <elevation>145.12217712402</elevation>
                    * </observatory>
                    * */

                    Float fltLat, fltLng, fltAlt;
                    fltLat = Float.valueOf(
                            ((Element) nlObservatory.item(0))
                                    .getElementsByTagName("latitude").item(0).getTextContent()
                    );
                    fltLng = Float.valueOf(
                            ((Element) nlObservatory.item(0))
                                    .getElementsByTagName("longitude").item(0).getTextContent()
                    );
                    fltAlt = Float.valueOf(
                            ((Element) nlObservatory.item(0))
                                    .getElementsByTagName("elevation").item(0).getTextContent()
                    );
                    astroDatabase = new AstroDatabase(mContext);
                    strObservatory = astroDatabase.getObservatory(fltLat, fltLng, fltAlt).get("location_name");
                    astroDatabase.astroDBclose();
                    astroDatabase = null;

                }

                // Get the rest of the events
                for (int intI = 0; intI < nEvents.getLength(); intI++) {
                    Node node = nEvents.item(intI);// This is an events file, I hope.
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element2 = (Element) node;
                        String string = element2.getNodeValue();
                        NodeList nlAurora = element2.getElementsByTagName("aurora");
                        NodeList nlConjunction = element2.getElementsByTagName("conjunction");
                        NodeList nlSatellite = element2.getElementsByTagName("satellite");
                        NodeList nlVariable = element2.getElementsByTagName("variable");
                        NodeList nlShower = element2.getElementsByTagName("shower");
                        NodeList nlComet = element2.getElementsByTagName("comet");
                        NodeList nlEclipse = element2.getElementsByTagName("eclipse");

                        // Got aurora?  Single item.  No need for database.
                        /*
                        *  <aurora>
                        *   <auroranight>Low</auroranight>
                        *   <aurorahour>0</aurorahour>
                        *   <start atomic="1533954416">22h26 EDT</start>
                        *   <stop atomic="1533975670">04h21 EDT</stop>
                        *  </aurora>
                        * */
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                        if(sharedPreferences!=null && nlAurora.getLength()>0) {
                            String strAurora = ((Element)nlAurora.item(0))
                                    .getElementsByTagName("auroranight").item(0).getAttributes()
                                    .getNamedItem("level").getNodeValue();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(AURORA_LEVEL_KEY, strAurora);
                            editor.apply();
                        }

                        // Conjunctions. Lots of them.
                        /*
                        *  <conjunction>
                        *   <begins atomic="1528507694">21h28 EDT</begins>
                        *   <ends atomic="1528511799">22h36 EDT</ends>
                        *   <object>
                        *    <starname>El Nath</starname>
                        *    <ra>5.436666667</ra>
                        *    <dec>28.6075</dec>
                        *    <mag>1.68</mag>
                        *    <alt>74.955712259404</alt>
                        *    <az>180.32114989007</az>
                        *    <rise atomic="1528450599">05h36 EDT</rise>
                        *    <set atomic="1528511799">22h36 EDT</set>
                        *    <start atomic="1528507694">21h28 EDT</start>
                        *    <stop atomic="1528535191">05h06 EDT</stop>
                        *   </object>
                        *   <object>
                        *    <starname>Mercury</starname>
                        *    <ra>5.3270564388543</ra>
                        *    <dec>24.226428483652</dec>
                        *    <mag>0.5</mag>
                        *    <alt>70.522540195963</alt>
                        *    <az>184.76111050057</az>
                        *    <rise atomic="1528454199">06h36 EDT</rise>
                        *    <set atomic="1528508199">21h36 EDT</set>
                        *    <start atomic="1528506435">21h07 EDT</start>
                        *    <stop atomic="1528536452">05h27 EDT</stop>
                        *   </object>
                        *   <angdistance value="4.6218998905607">4.62</angdistance><statement> deg</statement>
                        *  </conjunction>
                        * */
                        if(nlConjunction.getLength()>0) {
                            astroDatabase = new AstroDatabase(mContext, strObservatory);
                            for (int i = 0; i < nlConjunction.getLength(); i++) {
                                if(nlConjunction.item(i) instanceof Element) {
                                    NodeList nlBegin = ((Element)nlConjunction.item(i)).getElementsByTagName("begins");
                                    NodeList nlEnd = ((Element)nlConjunction.item(i)).getElementsByTagName("ends");
                                    if(nlBegin.getLength()==1 && nlEnd.getLength()==1) {
                                        NodeList nlObjects = ((Element) nlConjunction.item(i)).getElementsByTagName("object");
                                        NodeList nlDistance = ((Element)nlConjunction.item(i)).getElementsByTagName("angdistance");
                                        if(nlObjects.getLength()==2 && nlDistance.getLength()==1) {
                                            // Get begin and end times
                                            Long dteBegin, dteEnd;
                                            dteBegin = Long.valueOf(nlBegin.item(0).getAttributes().
                                                    getNamedItem("atomic").getNodeValue());
                                            dteEnd = Long.valueOf(nlEnd.item(0).getAttributes().
                                                    getNamedItem("atomic").getNodeValue());
                                            // Get object names and locations
                                            String strObject1Name, strObject2Name, strObject1Location, strObject2Location;
                                            Float fltObject1RightAscension, fltObject1Declination, fltObject1Altitude, fltObject1Azimuth,
                                                    fltObject2RightAscension, fltObject2Declination, fltObject2Altitude, fltObject2Azimuth;
                                            strObject1Name = ((Element)nlObjects.item(0))
                                                    .getElementsByTagName("starname").item(0).getTextContent();
                                            fltObject1RightAscension = Float.valueOf(((Element)nlObjects.item(0))
                                                    .getElementsByTagName("ra").item(0).getTextContent());
                                            fltObject1Declination = Float.valueOf(((Element)nlObjects.item(0))
                                                    .getElementsByTagName("dec").item(0).getTextContent());
                                            fltObject1Altitude = Float.valueOf(((Element)nlObjects.item(0))
                                                    .getElementsByTagName("alt").item(0).getTextContent());
                                            fltObject1Azimuth = Float.valueOf(((Element)nlObjects.item(0))
                                                    .getElementsByTagName("az").item(0).getTextContent());
                                            /*
                                            strObject1Location = (fltObject1Altitude<30?"Low":
                                                    (fltObject1Altitude>60?"High":""));
                                            * */
                                            strObject2Name = ((Element)nlObjects.item(1))
                                                    .getElementsByTagName("starname").item(0).getTextContent();
                                            fltObject2RightAscension = Float.valueOf(((Element)nlObjects.item(1))
                                                    .getElementsByTagName("ra").item(0).getTextContent());
                                            fltObject2Declination = Float.valueOf(((Element)nlObjects.item(1))
                                                    .getElementsByTagName("dec").item(0).getTextContent());
                                            fltObject2Altitude = Float.valueOf(((Element)nlObjects.item(1))
                                                    .getElementsByTagName("alt").item(0).getTextContent());
                                            fltObject2Azimuth = Float.valueOf(((Element)nlObjects.item(1))
                                                    .getElementsByTagName("az").item(0).getTextContent());
                                            /*
                                            strObject2Location = (fltObject2Altitude<30?"Low":
                                                    (fltObject2Altitude>60?"High":""));
                                            * */
                                            // Get distance between objects
                                            Float fltDistance;
                                            fltDistance = Float.valueOf(nlDistance.item(0).getAttributes().
                                                    getNamedItem("value").getNodeValue());
                                            astroDatabase.saveConjunction(
                                                    strObject1Name,fltObject1RightAscension,fltObject1Declination,
                                                    strObject2Name,fltObject2RightAscension,fltObject2Declination,
                                                    fltDistance,dteBegin,dteEnd
                                            );
                                        }
                                    }
                                }
                            }
                            astroDatabase.astroDBclose();
                            astroDatabase = null;
                        }

                        // Satellites. Thanks to Heavens-Above.
                        /*
                        *  <satellite>
                        *   <vehicle>Mayak</vehicle>
                        *   <mag>1.1</mag>
                        *   <rise atomic="1528514659">23h24 EDT</rise>
                        *   <peak atomic="1528514717">23h25 EDT</peak>
                        *   <set atomic="1528514717">23h25 EDT</set>
                        *   <link>http://heavens-above.com/passdetails.aspx?lat=43.6515952&amp;lng=-79.5692296&amp;loc=Unspecified&amp;alt=145.12217712402&amp;tz=EST&amp;cul=en&amp;satid=42830&amp;mjd=58278.1425644292</link>
                        *  </satellite>
                        *
                        *  <satellite>
                        *   <vehicle>Iridium 83</vehicle>
                        *   <mag>-6.4</mag>
                        *   <peak atomic="1528512806">20180608 22h53 EDT</peak>
                        *   <distance>7 km</distance>
                        *   <link>http://heavens-above.com/flaredetails.aspx?fid=0&amp;lat=43.6515952&amp;lng=-79.5692296&amp;loc=Astronomy+Forecast&amp;alt=145.12217712402&amp;tz=EST&amp;cul=en</link>
                        *  </satellite>
                        * */
                        if(nlSatellite.getLength()>0) {
                            astroDatabase = new AstroDatabase(mContext,strObservatory);
                            String strSatelliteName, strSatelliteType;
                            Float fltMagnitude;
                            Long dteStart, dtePeak, dteEnd;
                            for (int i = 0; i < nlSatellite.getLength(); i++) {
                                if(nlSatellite.item(i) instanceof Element) {
                                    Element element3 = (Element) nlSatellite.item(i);
                                    // is this an Iridium flash?
                                    if(element3.getElementsByTagName("distance").getLength()==1) {
                                        // This is an Iridium flash
                                        strSatelliteName = element3.getElementsByTagName("vehicle").item(0).getTextContent();
                                        strSatelliteType = "Iridium";
                                        fltMagnitude = Float.valueOf(element3.getElementsByTagName("mag").item(0).getTextContent());
                                        dtePeak = Long.valueOf(element3.getElementsByTagName("peak").item(0).getAttributes().
                                                getNamedItem("atomic").getNodeValue());
                                        dteStart = dtePeak;
                                        dteEnd = dtePeak;
                                    } else {
                                        // This is a regular satellite or the ISS
                                        strSatelliteName = element3.getElementsByTagName("vehicle").item(0).getTextContent();
                                        strSatelliteType = (strSatelliteName.equals("ISS")?"ISS":"normal");
                                        fltMagnitude = Float.valueOf(element3.getElementsByTagName("mag").item(0).getTextContent());
                                        dteStart = Long.valueOf(element3.getElementsByTagName("rise").item(0).getAttributes().
                                                getNamedItem("atomic").getNodeValue());
                                        dtePeak = Long.valueOf(element3.getElementsByTagName("peak").item(0).getAttributes().
                                                getNamedItem("atomic").getNodeValue());
                                        dteEnd = Long.valueOf(element3.getElementsByTagName("set").item(0).getAttributes().
                                                getNamedItem("atomic").getNodeValue());
                                    }
                                    astroDatabase.saveSatellite(strSatelliteName,strSatelliteType,fltMagnitude,dteStart,dtePeak,dteEnd);
                                }
                            }
                            astroDatabase.astroDBclose();
                            astroDatabase = null;
                        }

                        // Variables. A new one each week.
                        /*
                        *
                        *  <variable>
                        *   <starname>Chi Cyg</starname>
                        *   <ra>32.91405556</ra>
                        *   <dec>117.5163417</dec>
                        *   <alt>29.399946174093</alt>
                        *   <az>330.38554680054</az>
                        *   <rise atomic="">19h00 EST</rise>
                        *   <set atomic="">19h00 EST</set>
                        *   <mag>13.23110318389</mag>
                        *   <magmax>3.3</magmax>
                        *   <magmin>14.2</magmin>
                        *   <minimum atomic="1530403920">20180630 20h12 EDT</minimum>
                        *   <maximum atomic="1508525999">20171020 14h59 EDT</maximum>
                        *  </variable>
                        *
                        * */
                        if(nlVariable.getLength()>0) {
                            astroDatabase = new AstroDatabase(mContext,strObservatory);
                            for (int i = 0; i < nlVariable.getLength(); i++) {

                                // astroDatabase.setVariables();
                            }
                            astroDatabase.astroDBclose();
                            astroDatabase = null;
                        }

                        // Meteor shower.  Countdown to next peak.
                        /*
                        *
                        *  <shower>
                        *   <radiantname>Arietids</radiantname>
                        *   <ra>2.4</ra>
                        *   <dec>22.8</dec>
                        *   <begin atomic="1526968800">20180522 02h00 EDT</begin>
                        *   <peak atomic="1528524000">20180609 02h00 EDT</peak>
                        *   <end atomic="1530511200">20180702 02h00 EDT</end>
                        *   <mag>2</mag>
                        *   <rate>54</rate>
                        *  </shower>
                        *
                        * */
                        if(nlShower.getLength()>0) {
                            astroDatabase = new AstroDatabase(mContext, strObservatory);
                            String strShowerName = null;
                            Float fltRightAscension=0f, fltDeclination=0f, fltMagnitude=0f;
                            Long dteRise=0L, dteSet=0L, dteStart=0L, dtePeak=0L, dteStop=0L;
                            Integer intHourlyRate=0;

                            for (int i = 0; i < nlShower.getLength(); i++) {
                                strShowerName = ((Element)nlShower.item(0)).getElementsByTagName("radiantname").item(0).getTextContent();
                                dteStart = Long.valueOf(((Element)nlShower.item(0)).getElementsByTagName("begin").item(0).getAttributes().getNamedItem("atomic").getNodeValue());
                                dtePeak = Long.valueOf(((Element)nlShower.item(0)).getElementsByTagName("peak").item(0).getAttributes().getNamedItem("atomic").getNodeValue());
                                dteStop = Long.valueOf(((Element)nlShower.item(0)).getElementsByTagName("end").item(0).getAttributes().getNamedItem("atomic").getNodeValue());

                                astroDatabase.saveMeteorShower(strShowerName, fltRightAscension, fltDeclination,
                                        dteRise, dteSet, fltMagnitude, intHourlyRate,
                                        dteStart, dtePeak, dteStop);
                            }
                            astroDatabase.astroDBclose();
                            astroDatabase = null;
                        }

                        // Comet pass.
                        /*
                        *
                        *  <comet>
                        *   <cometname>21P/Giacobini-Zinner</cometname>
                        *   <ra>3.7823888888889</ra>
                        *   <dec>59.866277777778</dec>
                        *   <mag>7.56</mag>
                        *  </comet>
                        *
                        * */
                        if(nlComet.getLength()>0) {
                            astroDatabase = new AstroDatabase(mContext, strObservatory);
                            String strCometName = null;
                            Float fltRightAscension=0f, fltDeclination=0f, fltMagnitude=0f;
                            Long dteRise=0L, dteSet=0L, dteStart=0L, dtePeak=0L, dteStop=0L;
                            Integer intHourlyRate=0;

                            for (int i = 0; i < nlComet.getLength(); i++) {
                                strCometName = ((Element)nlComet.item(i)).getElementsByTagName("cometname").item(0).getTextContent();
                                fltRightAscension = Float.valueOf(((Element)nlComet.item(i)).getElementsByTagName("ra").item(0).getTextContent());
                                fltDeclination = Float.valueOf(((Element)nlComet.item(i)).getElementsByTagName("dec").item(0).getTextContent());
                                fltMagnitude = Float.valueOf(((Element)nlComet.item(i)).getElementsByTagName("mag").item(0).getTextContent());

                                astroDatabase.saveComet(strCometName, fltRightAscension, fltDeclination,
                                        dteRise, dteSet, fltMagnitude,
                                        dteStart, dtePeak, dteStop);
                            }
                            astroDatabase.astroDBclose();
                            astroDatabase = null;
                        }

                        if(nlEclipse.getLength()>0) {
                            astroDatabase = new AstroDatabase(mContext, strObservatory);
                            String strEclipseType = null;
                            Float fltRightAscension=0f, fltDeclination=0f;
                            Long dteRise=0L, dteSet=0L, dteStart=0L, dtePeak=0L, dteStop=0L;
                            for (int i = 0; i < nlEclipse.getLength(); i++) {
                                strEclipseType = ((Element)nlEclipse.item(i)).getElementsByTagName("eclipsetype").item(0).getTextContent();
                                dteStart = Long.valueOf(((Element)nlEclipse.item(i)).getElementsByTagName("begin").item(0).getAttributes().
                                        getNamedItem("atomic").getNodeValue());
                                dtePeak = Long.valueOf(((Element)nlEclipse.item(i)).getElementsByTagName("peak").item(0).getAttributes().
                                        getNamedItem("atomic").getNodeValue());
                                dteStop = Long.valueOf(((Element)nlEclipse.item(i)).getElementsByTagName("end").item(0).getAttributes().
                                        getNamedItem("atomic").getNodeValue());

                                astroDatabase.saveEclipse(strEclipseType, fltRightAscension, fltDeclination,
                                        dteRise, dteSet,
                                        dteStart, dtePeak, dteStop);
                            }
                            astroDatabase.astroDBclose();
                            astroDatabase = null;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "parseXML: "+ e.getLocalizedMessage());
            if(astroDatabase!=null) astroDatabase.astroDBclose();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            Log.e(TAG, "parseXML: "+ e.getLocalizedMessage());
            if(astroDatabase!=null) astroDatabase.astroDBclose();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "parseXML: "+ e.getLocalizedMessage());
            if(astroDatabase!=null) astroDatabase.astroDBclose();
        } catch (SAXException e) {
            e.printStackTrace();
            Log.e(TAG, "parseXML: "+ e.getLocalizedMessage());
            if(astroDatabase!=null) astroDatabase.astroDBclose();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "parseXML: "+ e.getLocalizedMessage());
            if(astroDatabase!=null) astroDatabase.astroDBclose();
        }
    }

    /**
     * Set the observatory
     * Get the observatory and set the weather forecast
     * Get the observatory and set the event forecast
     *
     */
    /*private AstroDatabase.AstroRecord getObservatory( NodeList nlObservatory ) {
        AstroDatabase astroDatabase = new AstroDatabase(mContext);
        AstroDatabase.AstroRecord astroRecord = new AstroDatabase.AstroRecord();
        return astroRecord;
    }*/

    public Boolean getInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo networkinfo = cm.getActiveNetworkInfo();
        if (networkinfo != null && networkinfo.isConnected()) {
            return true;
        }
        Toast.makeText(mContext,mContext.getString(R.string.ws_no_internet),Toast.LENGTH_SHORT).show();
        View view = ((Activity)mContext).findViewById(R.id.fab_reload);
        if(view!=null && view instanceof FloatingActionButton) {
            ((FloatingActionButton) view).setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sync_problem_antares_24dp));
        }
        return false;
    }

    private String getXMLattribute(Element element, String attribute, String... path) {
        String strAttribute = null;
        NodeList nodeList = element.getElementsByTagName(path[0]);
        Element eleSubPath = element;
        for (int intI = 0; intI < path.length; intI++) {
            if(path.length==intI+1) {
                strAttribute = eleSubPath.getAttribute(attribute);
            } else {
                nodeList = element.getElementsByTagName(path[intI]);
                eleSubPath = ((Element)nodeList.item(0));
            }
        }
        return strAttribute;
    }
}
