package com.github.houbb.pinyin.support.tone;

import com.github.houbb.heaven.annotation.ThreadSafe;
import com.github.houbb.heaven.constant.PunctuationConst;
import com.github.houbb.heaven.support.handler.IHandler;
import com.github.houbb.heaven.util.guava.Guavas;
import com.github.houbb.heaven.util.io.StreamUtil;
import com.github.houbb.heaven.util.lang.ObjectUtil;
import com.github.houbb.heaven.util.lang.StringUtil;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.pinyin.constant.PinyinConst;
import com.github.houbb.pinyin.constant.enums.PinyinToneNumEnum;
import com.github.houbb.pinyin.model.CharToneInfo;
import com.github.houbb.pinyin.model.ToneItem;
import com.github.houbb.pinyin.spi.IPinyinToneStyle;
import com.github.houbb.pinyin.util.ToneHelper;

import java.util.*;

/**
 * 正常的拼音注音形式
 * <p>
 * （1）单个字的词库与词组的词库分离
 * （2）二者词库都是懒加载，只有使用的时候才会去初始化。
 *
 * @author binbin.hou
 * @since 0.0.1
 */
@ThreadSafe
public class DefaultPinyinTone extends AbstractPinyinTone {

    /**
     * 单个字的 Map
     * DCL 单例，惰性加载。
     * <p>
     * （1）注意多音字的问题
     * （2）默认只返回第一个
     * （3）为了提升读取的性能，在初始化的时候，直接设计好。
     *
     * @since 0.0.1
     */
    private static volatile Map<String, List<String>> charMap;

    /**
     * 词组 map
     * <p>
     * DCL 单例，惰性加载。
     *
     * @since 0.0.1
     */
    private static volatile Map<String, String> phraseMap;

    @Override
    protected List<String> getCharTones(String chinese, final IPinyinToneStyle toneStyle) {
        List<String> defaultList = getCharMap().get(chinese);

        return CollectionUtil.toList(defaultList, new IHandler<String, String>() {
            @Override
            public String handle(String s) {
                return toneStyle.style(s);
            }
        });
    }

    @Override
    protected String getCharTone(String segment, final IPinyinToneStyle toneStyle) {
        // 大部分拼音都是单个字，不是多音字。
        // 直接在初始化的时候，设置好。
        List<String> pinyinList = getCharMap().get(segment);
        if (CollectionUtil.isNotEmpty(pinyinList)) {
            final String firstPinyin = pinyinList.get(0);
            return toneStyle.style(firstPinyin);
        }

        // 没有则返回空
        return null;
    }

    @Override
    protected String getPhraseTone(String segment,
                                   final IPinyinToneStyle toneStyle,
                                   final String connector) {
        String phrasePinyin = getPhraseMap().get(segment);

        // 直接返回空
        if (StringUtil.isEmptyTrim(phrasePinyin)) {
            return StringUtil.EMPTY;
        }

        String[] strings = phrasePinyin.split(StringUtil.BLANK);
        List<String> resultList = Guavas.newArrayList(strings.length);

        for (String string : strings) {
            final String style = toneStyle.style(string);
            resultList.add(style);
        }
        return StringUtil.join(resultList, connector);
    }

    /**
     * 获取单个字符的 map
     *
     * @return map
     * @since 0.0.1
     */
    private Map<String, List<String>> getCharMap() {
        if (ObjectUtil.isNotNull(charMap)) {
            return charMap;
        }

        synchronized (DefaultPinyinTone.class) {
            if (ObjectUtil.isNull(charMap)) {
                final long startTime = System.currentTimeMillis();

                final LinkedList<String> lines = new LinkedList<>();
                // 系统词库
                List<String> sysDictLine = StreamUtil.readAllLines(PinyinConst.PINYIN_DICT_CHAR_SYSTEM);
                lines.addAll(sysDictLine);

                // 自定义词库
                List<String> defineDictLines = StreamUtil.readAllLines(PinyinConst.PINYIN_DICT_CHAR_DEFINE);
                lines.addAll(defineDictLines);

                // 临时词库加载器
                List<String> tempDictLines = TemporaryDictionaryLoader.loadCharDictFile();
                lines.addAll(tempDictLines);

                charMap = Guavas.newHashMap(lines.size());
                for (String line : lines) {
                    String[] strings = line.split(PunctuationConst.COLON);
                    final String word = strings[0];
                    final List<String> pinyinList = StringUtil.splitToList(strings[1]);
                    charMap.put(word, pinyinList);
                }

                final long endTime = System.currentTimeMillis();
                System.out.println("[Pinyin] char dict loaded, cost time " + (endTime - startTime) + " ms!");
            }
        }

        return charMap;
    }

    /**
     * 获取词组的拼音 Map
     * （1）词组的拼音是确定的。
     *
     * @return map
     * @since 0.0.1
     */
    private Map<String, String> getPhraseMap() {
        if (ObjectUtil.isNotNull(phraseMap)) {
            return phraseMap;
        }
        synchronized (DefaultPinyinTone.class) {
            if (ObjectUtil.isNull(phraseMap)) {
                final long startTime = System.currentTimeMillis();

                final LinkedList<String> lines = new LinkedList<>();
                // 系统词库
                List<String> sysDictLine = StreamUtil.readAllLines(PinyinConst.PINYIN_DICT_PHRASE_SYSTEM);
                lines.addAll(sysDictLine);

                // 自定义词库
                List<String> defineDictLines = StreamUtil.readAllLines(PinyinConst.PINYIN_DICT_PHRASE_DEFINE);
                lines.addAll(defineDictLines);

                // 临时词库加载器
                List<String> tempDictLines = TemporaryDictionaryLoader.loadPhraseDictFile();
                lines.addAll(tempDictLines);

                phraseMap = Guavas.newHashMap(lines.size());

                for (String line : lines) {
                    String[] strings = line.split(PunctuationConst.COLON);
                    String word = strings[0];
                    phraseMap.put(word, strings[1]);
                }

                final long endTime = System.currentTimeMillis();
                System.out.println("[Pinyin] phrase dict loaded, cost time " + (endTime - startTime) + " ms!");
            }
        }

        return phraseMap;
    }

    @Override
    public Set<String> phraseSet() {
        Map<String, String> map = getPhraseMap();
        return map.keySet();
    }

    @Override
    public int toneNum(String defaultPinyin) {
        //1. 获取拼音
        if (StringUtil.isNotEmpty(defaultPinyin)) {
            CharToneInfo toneInfo = this.getCharToneInfo(defaultPinyin);

            int index = toneInfo.getIndex();

            // 轻声
            if (index < 0) {
                return PinyinToneNumEnum.FIVE.num();
            }

            // 直接返回对应的音标
            return toneInfo.getToneItem().getTone();
        }

        // 默认返回未知
        return PinyinToneNumEnum.UN_KNOWN.num();
    }

    /**
     * 获取对应的声调信息
     *
     * @param tone 拼音信息
     * @return 声调信息
     * @since 0.0.3
     */
    protected CharToneInfo getCharToneInfo(final String tone) {
        CharToneInfo charToneInfo = new CharToneInfo();
        charToneInfo.setIndex(-1);

        int length = tone.length();
        for (int i = 0; i < length; i++) {
            char currentChar = tone.charAt(i);
            ToneItem toneItem = ToneHelper.getToneItem(currentChar);

            if (ObjectUtil.isNotNull(toneItem)) {
                charToneInfo.setToneItem(toneItem);
                charToneInfo.setIndex(i);
                break;
            }
        }

        return charToneInfo;
    }

}
