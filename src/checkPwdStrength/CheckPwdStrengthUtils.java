package checkPwdStrength;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: 0
 * @create: 2018-01-23 13:12
 * @description: 校验密码强度打分工具类
 **/
public class CheckPwdStrengthUtils {

    private static final Pattern pattenUpCase = Pattern.compile("[A-Z]");
    private static final Pattern pattenLowCase = Pattern.compile("[a-z]");
    private static final Pattern pattenNum = Pattern.compile("[0-9]");

    /**
     * 密码
     */
    private String psw;
    /**
     * 密码长度
     */
    private int length;
    /**
     * 大写字母长度
     */
    private int upperAlp = 0;
    /**
     * 小写字母长度
     */
    private int lowerAlp = 0;
    /**
     * 数字长度
     */
    private int numLen = 0;
    /**
     * 特殊字符长度
     */
    private int charLen = 0;

    public CheckPwdStrengthUtils(String psw) {
        this.psw = psw;
    }

    /**
     * 获取密码等级校验得分
     *
     * @return
     */
    public int getScope() {
        if (StringUtils.isBlank(this.psw)) {
            return 0;
        }
        this.psw = this.psw.replaceAll("\\s", "");
        this.length = this.psw.length();
        int scope = checkPswLength() +
                checkPswUpper() +
                checkPwsLower() +
                checkNum() +
                checkChar() +
                checkNumOrCharInStr() +
                lowerQuest() +
                onlyHasAlp() +
                onlyHasNum() +
                repeatDex() +
                seriseUpperAlp() +
                seriseLowerAlp() +
                seriseNum() +
                seriesAlp2Three() +
                seriesNum2Three();
        if (scope < 0) {
            scope = 0;
        }
        if (scope > 100) {
            scope = 100;
        }
        return scope;
    }

    /**
     * 密码长度积分
     *
     * @return
     */
    private int checkPswLength() {
        return this.length * 4;
    }

    /**
     * 检测大写字母
     *
     * @return
     */
    private int checkPswUpper() {
        Matcher matcher = pattenUpCase.matcher(psw);
        int j = 0;
        while (matcher.find()) {
            j++;
        }
        this.upperAlp = j;
        if (j <= 0) {
            return 0;
        }
        return (this.length - j) * 2;
    }

    /**
     * 检测小写字母
     *
     * @return
     */
    private int checkPwsLower() {
        Matcher matcher = pattenLowCase.matcher(this.psw);
        int j = 0;
        while (matcher.find()) {
            j++;
        }
        this.lowerAlp = j;
        if (j <= 0) {
            return 0;
        }
        return (this.length - j) * 2;
    }

    /**
     * 检测数字
     *
     * @return
     */
    private int checkNum() {
        Matcher matcher = pattenNum.matcher(this.psw);
        int j = 0;
        while (matcher.find()) {
            j++;
        }
        this.numLen = j;
        if (this.numLen == this.length) {
            return 0;
        }
        return j * 4;
    }

    /**
     * 检测符号
     *
     * @return
     */
    private int checkChar() {
        charLen = this.length - this.upperAlp - this.lowerAlp - this.numLen;
        return this.charLen * 6;
    }

    /**
     * 检测密码中穿插的数字或符号
     */
    private int checkNumOrCharInStr() {
        char[] c = this.psw.toLowerCase().toCharArray();
        int j = this.numLen + this.charLen;
        if (!pattenLowCase.matcher(c[0] + "").find()) {
            j -= 1;
        }
        if (!pattenLowCase.matcher(c[c.length - 1] + "").find()) {
            j -= 1;
        }
        if (j < 0) {
            j = 0;
        }
        if (this.numLen + this.charLen == this.length) {
            j = this.length - 2;
        }
        return j * 2;
    }

    /**
     * 最低要求标准
     * 该方法需要在以上加分方法使用后才可以使用
     * 达标要求：
     * 1。长度至少6位；
     * 2.大写   小写   数字  符号 结合（至少三项）
     *
     * @return
     */
    private int lowerQuest() {
        //达标指数
        int j = 0;
        if (this.length < 6) {
            return 0;
        }
        if (this.upperAlp > 0) {
            j++;
        }
        if (this.lowerAlp > 0) {
            j++;
        }
        if (this.numLen > 0) {
            j++;
        }
        if (this.charLen > 0) {
            j++;
        }
        if (j < 3) {
            return 0;
        }
        return (j + 1) * 2;
    }


    /**=================分割线===扣分项目=====================**/
    /**
     * 只包含英文字母
     *
     * @return
     */
    private int onlyHasAlp() {
        if (this.length == (this.upperAlp + this.lowerAlp)) {
            return -this.length;
        }
        return 0;
    }

    /**
     * 只包含数字
     *
     * @return
     */
    private int onlyHasNum() {
        if (this.length == this.numLen) {
            return -this.length;
        }
        return 0;
    }

    /**
     * 重复字符扣分
     *
     * @return
     */
    private int repeatDex() {
        char[] c = this.psw.toLowerCase().toCharArray();
        HashMap<Character, Integer> hashMap =
                new HashMap<Character, Integer>();
        for (int i = 0; i < c.length; i++) {
            if (hashMap.containsKey(c[i])) {
                hashMap.put(c[i], hashMap.get(c[i]) + 1);
            } else {
                hashMap.put(c[i], 1);
            }
        }
        int sum = 0;
        Iterator<Map.Entry<Character, Integer>> iterator =
                hashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            int j = iterator.next().getValue();
            if (j > 0) {
                sum = sum + j * (j - 1);
            }
        }
        return -sum;
    }

    /**
     * 连续英文大写
     *
     * @return
     */
    private int seriseUpperAlp() {
        int j = 0;
        char[] c = this.psw.toCharArray();
        for (int i = 0; i < c.length - 1; i++) {
            if (pattenUpCase.matcher(c[i] + "").find() && pattenUpCase.matcher(c[i + 1] + "").find()) {
                j++;
            }
        }
        return -2 * j;
    }

    /**
     * 连续英文小写
     *
     * @return
     */
    private int seriseLowerAlp() {
        int j = 0;
        char[] c = this.psw.toCharArray();
        for (int i = 0; i < c.length - 1; i++) {
            if (pattenLowCase.matcher(c[i] + "").find() && pattenLowCase.matcher(c[i + 1] + "").find()) {
                j++;
            }
        }
        return -2 * j;
    }

    /**
     * 连续数字
     *
     * @return
     */
    private int seriseNum() {
        char[] c = this.psw.toCharArray();
        int j = 0;
        for (int i = 0; i < c.length - 1; i++) {
            if (pattenNum.matcher(c[i] + "").matches() && pattenNum.matcher(c[i + 1] + "").matches()) {
                j++;
            }
        }
        return -2 * j;
    }

    /**
     * 连续字母abc def之类超过3个扣分  不区分大小写字母
     *
     * @return
     */
    private int seriesAlp2Three() {
        int j = 0;
        char[] c = this.psw.toLowerCase().toCharArray();
        for (int i = 0; i < c.length - 2; i++) {
            if (pattenLowCase.matcher(c[i] + "").find()) {
                if ((c[i + 1] == c[i] + 1) && (c[i + 2] == c[i] + 2)) {
                    j++;
                }
            }
        }
        return -3 * j;
    }


    /**
     * 连续数字123 234之类超过3个扣分
     *
     * @return
     */
    private int seriesNum2Three() {
        int j = 0;
        char[] c = this.psw.toLowerCase().toCharArray();
        for (int i = 0; i < c.length - 2; i++) {
            if (pattenNum.matcher(c[i] + "").find()) {
                if ((c[i + 1] == c[i] + 1) && (c[i + 2] == c[i] + 2)) {
                    j++;
                }
            }
        }
        return -3 * j;
    }


    public static void main(String[] args) {
        CheckPwdStrengthUtils checkPwdStrengthUtils = new CheckPwdStrengthUtils("LwZ&19930516");
        System.out.println(checkPwdStrengthUtils.getScope());
        checkPwdStrengthUtils.addPoints();
        checkPwdStrengthUtils.decreasePoints();
    }

    public int addPoints() {
        System.out.println("密码长度=" + checkPswLength());
        System.out.println("大写英文字母数=" + checkPswUpper());
        System.out.println("小写英文字母书=" + checkPwsLower());
        System.out.println("数字数=" + checkNum());
        System.out.println("符号数=" + checkChar());
        System.out.println("密码之间穿插的符号或数字数（不包括开头结尾）=" + checkNumOrCharInStr());
        System.out.println("已达最低要求=" + lowerQuest());
        return 0;
    }

    public int decreasePoints() {
        System.out.println("只有英文字符=" + onlyHasAlp());
        System.out.println("只有数字字符=" + onlyHasNum());
        System.out.println("重复字符 (Case Insensitive)=" + repeatDex());
        System.out.println("连续大写英文数=" + seriseUpperAlp());
        System.out.println("连续小写英文数=" + seriseLowerAlp());
        System.out.println("连续数字数=" + seriseNum());
        System.out.println("连续字母超过三个(如abc,def)=" + seriesAlp2Three());
        System.out.println("连续数字超过三个(如123,234)=" + seriesNum2Three());
        return 0;
    }

}
