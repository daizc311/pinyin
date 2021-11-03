package com.github.houbb.pinyin.support.tone;

import com.github.houbb.heaven.util.io.StreamUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TemporaryDictionaryLoader {

    private static Supplier<InputStream> charDictProvider;
    private static Supplier<InputStream> phraseDictProvider;

    /**
     * 设置字符提供器
     */
    public static void setCharDictFileProvider(Supplier<InputStream> dictFileInputStreamProvider) {
        TemporaryDictionaryLoader.charDictProvider = dictFileInputStreamProvider;
    }

    /**
     * 设置词组提供器
     */
    public static void setPhraseDictProvider(Supplier<InputStream> phraseDictProvider) {
        TemporaryDictionaryLoader.phraseDictProvider = phraseDictProvider;
    }

    public static List<String> loadCharDictFile() {
        try {
            if (charDictProvider != null) {
                return StreamUtil.readAllLines(charDictProvider.get());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static List<String> loadPhraseDictFile() {
        try {
            if (phraseDictProvider != null) {
                return StreamUtil.readAllLines(phraseDictProvider.get());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
