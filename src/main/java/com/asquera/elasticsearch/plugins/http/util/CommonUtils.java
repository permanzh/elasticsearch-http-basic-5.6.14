package com.asquera.elasticsearch.plugins.http.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Booleans;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.SettingsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * created by yangyu on 2020-09-15
 */
public class CommonUtils {

    private static final Logger logger = Loggers.getLogger(CommonUtils.class);

    public static String removeQuote(String str,String split){
        if (StringUtils.isNotBlank(str)){
            String start = StringUtils.removeStart(str, split);
            if (StringUtils.isNotBlank(start)){
                return StringUtils.removeEnd(start, split);
            }
            return start;
        }
        return str;
    }

    public static Boolean getAsBoolean(Properties prop, String setting, Boolean defaultValue) {
        String rawValue = prop.getProperty(setting);
        if (StringUtils.isNotBlank(rawValue)){
            try{
                return Booleans.parseBooleanExact(rawValue, defaultValue);
            }catch (Exception ex){
                logger.error("Get ["+setting+"] Boolean Value Failed ",ex);
                return false;
            }
        } else {
            return false;
        }
    }

    public static String[] getAsArray(Properties prop,String settingPrefix, String[] defaultArray, Boolean commaDelimited) throws SettingsException {
        List<String> result = new ArrayList<>();

        final String valueFromPrefix = prop.getProperty(settingPrefix);
        final String valueFromPreifx0 = prop.getProperty(settingPrefix + ".0");

        if (valueFromPrefix != null && valueFromPreifx0 != null) {
            final String message = String.format(
                    Locale.ROOT,
                    "settings object contains values for [%s=%s] and [%s=%s]",
                    settingPrefix,
                    valueFromPrefix,
                    settingPrefix + ".0",
                    valueFromPreifx0);
            throw new IllegalStateException(message);
        }

        if (prop.get(settingPrefix) != null) {
            if (commaDelimited) {
                String[] strings = Strings.splitStringByCommaToArray(prop.getProperty(settingPrefix));
                if (strings.length > 0) {
                    for (String string : strings) {
                        result.add(string.trim());
                    }
                }
            } else {
                result.add(prop.getProperty(settingPrefix).trim());
            }
        }

        int counter = 0;
        while (true) {
            String value = prop.getProperty(settingPrefix + '.' + (counter++));
            if (value == null) {
                break;
            }
            result.add(value.trim());
        }
        if (result.isEmpty()) {
            return defaultArray;
        }

        List<String> res  = new ArrayList<>();
        for (String item : result) {
            while (StringUtils.contains(item,"\"")){
                item = StringUtils.substringBeforeLast(item,"\"");
                item = StringUtils.substringAfterLast(item,"\"");
            }
            res.add(item);
        }
        return res.toArray(new String[res.size()]);
    }

}
