package com.github.houbb.pinyin.support.tone;

import com.github.houbb.heaven.util.io.StreamUtil;
import com.github.houbb.pinyin.constant.PinyinConst;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TemporaryDictionaryLoader {

    private static InputStream dictFileInputStream;

    public static void setDictFileInputStream(InputStream dictFileInputStream) {
        if (dictFileInputStream!=null){
            System.err.println("dictFileInputStream已经有值，不能再次设置");
        }
        TemporaryDictionaryLoader.dictFileInputStream = dictFileInputStream;
    }

    public static List<String> load() {
        try {
            if (dictFileInputStream != null) {
                return StreamUtil.readAllLines(dictFileInputStream);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
