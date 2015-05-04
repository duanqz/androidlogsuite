package com.androidlogsuite.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.androidlogsuite.util.Log;

public class ParseConfiguration extends BaseConfiguration {
    static final private String TAG = "ParseConfiguration";
    static final public int PARSE_TYPE_ALL = 0;

    public int mParseType = PARSE_TYPE_ALL;

    public HashMap<String, ParseRule> mParseRulesWithKeyWords;
    public ArrayList<ParseRule> mParseRulesWithoutKeyWord;

    private String mCachedKeyWords;
    private Pattern mCachedPattern;
    private ParseRule mCachedParseRule;

    public ParseConfiguration() {
        mParseRulesWithKeyWords = new HashMap<String, ParseRule>();
        mParseRulesWithoutKeyWord = new ArrayList<ParseRule>();
    }

    public void addParseRule(ParseRule newParseRule) {
        if (newParseRule.startWith != null) {
            String key = newParseRule.startWith;
            if (mParseRulesWithKeyWords.containsKey(key)) {
                Log.d(TAG, "Parseconfig with key = " + key
                        + " is already exist");
                return;
            }
            mParseRulesWithKeyWords.put(key, newParseRule);
        } else {
            BaseConfiguration.replaceConfig(mParseRulesWithoutKeyWord, newParseRule);
        }

    }

    static public class ParseRule extends BaseConfiguration {
        public String startWith;
        public String regx;
        public boolean bCaseSensitive = false;
        public boolean bExclusive = true;
        public int[] groups = null;

        public String[] getGroupInfoWithouKeyWord(Matcher matcher, String line) {
            if (groups == null) {
                return null;
            }
            String values[] = new String[groups.length + 1];
            values[0] = line;
            for (int i = 0; i < groups.length; i++) {
                values[i + 1] = matcher.group(groups[i]);
            }

            return values;
        }

        public String[] getGroupInfo(Matcher matcher) {
            if (groups == null) {
                return null;
            }
            String values[] = new String[groups.length];
            for (int i = 0; i < groups.length; i++) {
                values[i] = matcher.group(groups[i]);
            }

            return values;
        }

        public boolean isMatched(String line) {
            return line.matches(regx);
        }
        public ParseRule Clone() {
            ParseRule parseRule = new ParseRule();
            Copy(parseRule);
            parseRule.startWith = startWith;
            parseRule.regx  = regx;
            parseRule.bCaseSensitive = bCaseSensitive;
            parseRule.bExclusive = bExclusive;
            if(groups != null){
                parseRule.groups = new int[groups.length];
                System.arraycopy(groups, 0, parseRule.groups, 0, groups.length);
            }
            return parseRule;
        }
    }

    public String getCachedKeyWord() {
        return mCachedKeyWords;
    }

    public ParseRule getCachedParseRule() {
        return mCachedParseRule;
    }

    private String[] getParseReulstFormCache(String line) {
        Matcher matcher = mCachedPattern.matcher(line);
        if (matcher.matches() == false) {
            Log.d(TAG, line + " does not have any mather, error happens");
            return null;
        }
        return mCachedParseRule.getGroupInfo(matcher);
    }

    private String[] getParseReulstFormCacheWithoutKeyWord(String line) {
        Matcher matcher = mCachedPattern.matcher(line);
        if (matcher.matches() == false) {
            Log.d(TAG, line + " does not have any mather, error happens");
            return null;
        }
        return mCachedParseRule.getGroupInfoWithouKeyWord(matcher, line);
    }

    private void createPatternFromCache() {
        if (mCachedParseRule.bCaseSensitive) {
            mCachedPattern = Pattern.compile(mCachedParseRule.regx);
        } else {
            mCachedPattern = Pattern.compile(mCachedParseRule.regx,
                    Pattern.CASE_INSENSITIVE);
        }
        return;
    }

    public String[] getParseResult(String line) {
        // if we have cached keywords...
        if (mCachedKeyWords != null && line.startsWith(mCachedKeyWords)) {
            return getParseReulstFormCache(line);
        } else {
            mCachedKeyWords = null;// reset mCachedKeyWords
        }
        if (mCachedPattern != null) {
            Matcher matcher = mCachedPattern.matcher(line);
            if (matcher.matches() == false) {
                mCachedPattern = null;
                mCachedParseRule = null;
            } else {
                return getParseReulstFormCacheWithoutKeyWord(line);
            }
        }
        if (mParseRulesWithKeyWords.size() != 0) {
            Iterator<String> keyWordsIterable = mParseRulesWithKeyWords
                    .keySet().iterator();
            while (keyWordsIterable.hasNext()) {
                String keyWord = keyWordsIterable.next();
                if (line.startsWith(keyWord)) {
                    mCachedKeyWords = keyWord;
                    break;
                }
            }
            if (mCachedKeyWords != null) {
                mCachedParseRule = mParseRulesWithKeyWords.get(mCachedKeyWords);
                createPatternFromCache();
                return getParseReulstFormCache(line);
            } else {
                mCachedKeyWords = null;
                mCachedPattern = null;
                mCachedParseRule = null;
            }

        }
        // for parse rules without keywords, we must return original line in
        // result[0]
        if (mParseRulesWithoutKeyWord.size() != 0) {
            int N = mParseRulesWithoutKeyWord.size();
            for (int i = 0; i < N; i++) {
                ParseRule parseRule = mParseRulesWithoutKeyWord.get(i);
                if (parseRule.isMatched(line)) {
                    mCachedParseRule = parseRule;
                    createPatternFromCache();
                    return getParseReulstFormCacheWithoutKeyWord(line);
                }
            }
        }

        return null;
    }
    public ParseConfiguration Clone() {
        ParseConfiguration newConfig = new ParseConfiguration();
        Copy(newConfig);
        if(mParseRulesWithKeyWords != null)
            newConfig.mParseRulesWithKeyWords = 
                new HashMap<String, ParseRule>(mParseRulesWithKeyWords);
        if(mParseRulesWithoutKeyWord != null)
            newConfig.mParseRulesWithoutKeyWord = new ArrayList<ParseRule>(mParseRulesWithoutKeyWord);


        return newConfig;
    }

}
