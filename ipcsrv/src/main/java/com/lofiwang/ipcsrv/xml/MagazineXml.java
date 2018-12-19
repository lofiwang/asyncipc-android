package com.lofiwang.ipcsrv.xml;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by chunsheng.wang on 2018/12/19.
 */

public class MagazineXml {
    private static final String TAG = "MagazineXml";
    private void readMagazine(File magazineFile) {
        FileReader magazineReader = null;
        try {
            magazineReader = new FileReader(magazineFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (magazineReader == null) {
            return;
        }
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(magazineReader);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

    }

    private void readMagazine(InputStream magazineFile) {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(magazineFile, Xml.Encoding.UTF_8.name());

            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
                ;
            }
            if (type != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("No start tag found");
            }
            while (true) {
                Log.d(TAG, "test xml:" + parser.getName());
                XmlUtils.nextElement(parser);
                if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {

        } finally {

        }

    }
}
