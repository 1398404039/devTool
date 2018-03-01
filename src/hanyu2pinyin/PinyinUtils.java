package hanyu2pinyin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * @author: lwz
 * @description: 工具类，用于汉字转拼音
 * @date: 10:18 2018/1/15
 */
public class PinyinUtils {

    private static final Logger logger = LoggerFactory.getLogger(PinyinUtils.class);
    public static Map<String, String> dictionary = new HashMap<String, String>();

    //加载多音字词典
    static {
        BufferedReader br = null;
        try {
            File file = ResourceUtils.getFile("classpath:py4j.txt");
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split("#");
                if (!StringUtils.isEmpty(arr[1])) {
                    String[] sems = arr[1].split(" ");
                    for (String sem : sems) {
                        if (!StringUtils.isEmpty(sem)) {
                            dictionary.put(sem, arr[0]);
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(String.format("编码格式不支持"), e);
        } catch (FileNotFoundException e) {
            logger.error(String.format("File未找到"), e);
        } catch (IOException e) {
            logger.error(String.format("File读取失败"), e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 汉字转成拼音，返回拼音数组
     *
     * @param chineseCharacter 需要转化的中文内容
     * @return
     * @throws BadHanyuPinyinOutputFormatCombination
     */
    public static String[] chineseToPinYin(char chineseCharacter) throws BadHanyuPinyinOutputFormatCombination {
        HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
        outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        outputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
        if (String.valueOf(chineseCharacter).matches("[\u4e00-\u9fa5]+")) {
            // 如果字符是中文,则将中文转为汉语拼音
            return PinyinHelper.toHanyuPinyinStringArray(chineseCharacter, outputFormat);
        } else {
            return new String[]{String.valueOf(chineseCharacter)};
        }
    }

    /**
     * 获取汉字拼音的全拼
     *
     * @param chineseCharacter
     * @return
     * @throws BadHanyuPinyinOutputFormatCombination
     */
    public static String chineseToPinYin(String chineseCharacter) throws BadHanyuPinyinOutputFormatCombination {
        if (StringUtils.isEmpty(chineseCharacter)) {
            return null;
        }
        char[] chs = chineseCharacter.toCharArray();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < chs.length; i++) {
            String[] arr = chineseToPinYin(chs[i]);
            if (arr == null) {
                result.append("");
            } else if (arr.length == 1) {
                result.append(arr[0]);
            } else if (arr[0].equals(arr[1])) {
                //多音字，但是读音一样（在这里排除声调不同）
                result.append(arr[0]);
            } else {
                //多音字处理
                String polyphone = getPolyphone(chineseCharacter,i,arr);
                if (!StringUtils.isEmpty(polyphone)) {
                    result.append(polyphone);
                }
            }
        }
        return result.toString().toUpperCase();
    }

    /**
     * 汉字转拼音，获取全拼 首字母串（大写）
     *
     * @param chineseCharacter 需要转化的中文串
     * @return 全拼 首字母串（ps：重庆一中  CQYZ）
     */
    public static String chineseToPinYinInitials(String chineseCharacter) throws BadHanyuPinyinOutputFormatCombination {
        if (StringUtils.isEmpty(chineseCharacter)) {
            return null;
        }
        char[] chs = chineseCharacter.toCharArray();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < chs.length; i++) {
            String[] arr = chineseToPinYin(chs[i]);
            if (arr == null) {
                result.append("");
            } else if (arr.length == 1) {
                result.append(arr[0].charAt(0));
            } else if (arr[0].equals(arr[1])) {
                //多音字，但是读音一样（在这里排除声调不同）
                result.append(arr[0].charAt(0));
            } else {
                //多音字处理方案
                String answer = getPolyphone( chineseCharacter,i,arr);
                if (!StringUtils.isEmpty(answer)) {
                    result.append(answer.charAt(0));
                }
            }
        }
        return result.toString().toUpperCase();
    }

    /**
     * 汉字转拼音，获取第一个字符的大写字母
     * @param str
     * @return 返回首字母  （ps：重庆一中  C）
     */
    public static String chineseToPinYinFirstInitial(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        try {
            if (str.length() > 3) {
                str = str.substring(0, 3);
            }
            String initials = chineseToPinYinInitials(str);
            if (!StringUtils.isEmpty(initials)) {
                return initials.substring(0, 1);
            }
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        return null;
    }

    /**
     * 内部方法 获取多音字的情景读音（ps：重庆  chong）
     * @param chineseCharacter 原需要转化拼音的汉字串
     * @param i 多音字的再汉字串中的位置脚标
     * @param arr 多音字的所有读音 数组
     * @return
     */
    private static String getPolyphone( String chineseCharacter,int i, String[] arr) {
        String prim = chineseCharacter.substring(i, i + 1);
        String lst = null, rst = null;
        //判断，如果该指定的多音字不是原汉子串中的最后一个字，取后相邻一个字做情景判断
        if (i <= chineseCharacter.length() - 2) {
            rst = chineseCharacter.substring(i, i + 2);
        }
        //判断，如果该指定的多音字不是原汉子串中的第一个字，取前相邻一个字做情景判断
        if (i >= 1 && i + 1 <= chineseCharacter.length()) {
            lst = chineseCharacter.substring(i - 1, i + 1);
        }
        String answer = null;
        for (String py : arr) {
            if (StringUtils.isEmpty(py)) {
                continue;
            }
            //根据字典对比，查找情景词
            if (lst != null && py.equals(dictionary.get(lst))) {
                answer = py;
                break;
            }
            if (rst != null && py.equals(dictionary.get(rst))) {
                answer = py;
                break;
            }
            if (py.equals(dictionary.get(prim))) {
                answer = py;
            }
        }
        if (answer != null) {
            return answer;
        } else {
            return "";
        }
    }

    /**
     * @author: lwz
     * @description: 判断，当str1不为空的时候，返回str1的首字母；反之，返回str2的首字母
     * @param: [str1, str2]
     * @return: java.lang.String
     * @date: 9:00 2018/1/16
     */
    public static String chineseToPinYinFirstInitial(String str1,String str2) {
        if(!StringUtils.isBlank(str1)){
            return chineseToPinYinFirstInitial(str1);
        }
        return chineseToPinYinFirstInitial(str2);
    }
