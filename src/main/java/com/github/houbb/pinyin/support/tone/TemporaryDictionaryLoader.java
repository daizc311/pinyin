package com.github.houbb.pinyin.support.tone;

import com.github.houbb.heaven.util.io.StreamUtil;
import com.github.houbb.pinyin.constant.PinyinConst;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TemporaryDictionaryLoader {

    private static Supplier<InputStream> dictFileInputStreamProvider;

    public static void setDictFileInputStreamProvider(Supplier<InputStream> dictFileInputStreamProvider) {
        TemporaryDictionaryLoader.dictFileInputStreamProvider = dictFileInputStreamProvider;
    }

    public static List<String> load() {
        try {
            if (dictFileInputStreamProvider != null) {
                return StreamUtil.readAllLines(dictFileInputStreamProvider.get());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
